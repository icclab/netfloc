/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbBridgeAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbNodeAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbTerminationPointAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.neutron.spi.NeutronPort;
import org.opendaylight.neutron.spi.NeutronSubnet;
import org.opendaylight.neutron.spi.NeutronNetwork;
import org.opendaylight.neutron.spi.NeutronRouter;
import org.opendaylight.neutron.spi.NeutronFloatingIP;
import ch.icclab.netfloc.iface.IBridgeIterator;
import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.ILinkPort;
import ch.icclab.netfloc.iface.INetworkOperator;
import ch.icclab.netfloc.iface.INetworkPath;
import ch.icclab.netfloc.iface.INetworkTraverser;
import ch.icclab.netfloc.iface.INodeOperator;
import ch.icclab.netfloc.iface.IPortOperator;
import ch.icclab.netfloc.iface.IHostPort;
import ch.icclab.netfloc.iface.ITraversableBridge;
import ch.icclab.netfloc.iface.INetworkPathListener;
import ch.icclab.netfloc.iface.nbhandlers.INeutronPortHandler;
import ch.icclab.netfloc.iface.nbhandlers.INeutronSubnetHandler;
import ch.icclab.netfloc.iface.nbhandlers.INeutronNetworkHandler;
import ch.icclab.netfloc.iface.nbhandlers.INeutronRouterHandler;
import ch.icclab.netfloc.iface.nbhandlers.INeutronFloatingIPHandler;
import ch.icclab.netfloc.iface.sbhandlers.IBridgeHandler;
import ch.icclab.netfloc.iface.sbhandlers.INodeHandler;
import ch.icclab.netfloc.iface.sbhandlers.IPortHandler;
import ch.icclab.netfloc.iface.ofhandlers.ILinkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;
import java.lang.System;
import java.lang.IllegalStateException;

// idk
public class NetflocManager implements
	IBridgeHandler,
	INodeHandler,
	IPortHandler,
	INeutronPortHandler,
	INeutronNetworkHandler,
	INeutronSubnetHandler,
	INeutronRouterHandler,
	INeutronFloatingIPHandler,
	ILinkHandler {

	static final Logger logger = LoggerFactory.getLogger(NetflocManager.class);

	private NetworkGraph graph;
	private Map<String, NeutronPort> neutronPortCache = new HashMap<String, NeutronPort>();
	private Map<String, NeutronSubnet> neutronSubnetCache = new HashMap<String, NeutronSubnet>();
	private Map<String, NeutronNetwork> neutronNetworkCache = new HashMap<String, NeutronNetwork>();

	// todo: singleton
	public NetflocManager(NetworkGraph graph) {
		this.graph = graph;
	}

	@Override
	public void handleBridgeCreate(Node node, OvsdbBridgeAugmentation ovsdbBridgeAugmentation) {
		INodeOperator no = graph.getParentNode(node.getNodeId());
		IBridgeOperator bo = new Bridge(no, node, ovsdbBridgeAugmentation);
		no.addBridge(bo);
        graph.notifyBridgeListenersCreate(bo);
	}

	@Override
	public void handleBridgeDelete(Node node, OvsdbBridgeAugmentation ovsdbBridgeAugmentation) {
		INodeOperator no = graph.getParentNode(node.getNodeId());
		IBridgeOperator bo = new Bridge(no, node, ovsdbBridgeAugmentation);
		no.removeBridge(bo);
	}

	@Override
	public void handleBridgeUpdate(Node node, OvsdbBridgeAugmentation ovsdbBridgeAugmentation) {
		INodeOperator no = graph.getParentNode(node.getNodeId());
		for (IBridgeOperator bo : no.getBridges()) {
			if (bo.getNodeId().equals(node.getNodeId())) {
				bo.update(node, ovsdbBridgeAugmentation);
				break;
			}
		}
	}
	
	@Override
	public void handleNodeConnect(Node node, OvsdbNodeAugmentation ovsdbNodeAugmentation) {
		INodeOperator no = new Datapath(graph, node, ovsdbNodeAugmentation);
		graph.addNode(no);
	}
	
	@Override
	public void handleNodeConnectionAttributeChange(Node node, OvsdbNodeAugmentation ovsdbNodeAugmentation) {
		INodeOperator no = graph.getNode(node.getNodeId());
		no.update(node, ovsdbNodeAugmentation);
	}
	
	@Override
	public void handleNodeDisconnect(Node node, OvsdbNodeAugmentation ovsdbNodeAugmentation) {
		INodeOperator no = new Datapath(graph, node, ovsdbNodeAugmentation);
		graph.removeNode(no.getNodeId());
	}

	@Override
	public void handlePortCreate(Node node, TerminationPoint tp, OvsdbTerminationPointAugmentation tpa) {
		// check if ovs port matches neutron port
		String value = SouthboundHelper.getInterfaceExternalIdsValue(tpa, Constants.EXTERNAL_ID_INTERFACE_ID);
		if (value != null) {
			for (NeutronPort neutronPort : this.neutronPortCache.values()) {
				if (value.equalsIgnoreCase(neutronPort.getPortUUID())) {
					graph.addPort(node, tp, tpa, neutronPort);
					return;
				}
			}
		}

		graph.addPort(node, tp, tpa);
	}

	@Override
	public void handlePortDelete(Node node, OvsdbTerminationPointAugmentation tpa) {
        logger.info("delete port {}", tpa);
		graph.removePort(node, tpa);
	}
	
	@Override
	public void handlePortUpdate(Node node, TerminationPoint tp, OvsdbTerminationPointAugmentation tpa) {
		graph.updatePort(node, tp, tpa);
	}

	@Override
	public void handleLinkCreate(Link link) {
		graph.createLink(link);
	}

	@Override
	public void handleLinkDelete(Link link) {
        logger.info("delete link {}", link);
		graph.deleteLink(link);
	}

	@Override
	public void handleLinkUpdate(Link link) {
		// todo
	}

    /**
     * Services provide this interface method for taking action after a port has been created
     *
     * @param port
     *            instance of new Neutron Port object
     */
    @Override
    public void neutronPortCreated(NeutronPort port) {
    	this.neutronPortCache.put(port.getPortUUID(), port);
    }

    /**
     * Services provide this interface method for taking action after a port has been updated
     *
     * @param port
     *            instance of modified Neutron Port object
     */
    @Override
    public void neutronPortUpdated(NeutronPort port) {
		this.neutronPortCache.put(port.getPortUUID(), port);
    }

    /**
     * Services provide this interface method for taking action after a port has been deleted
     *
     * @param port
     *            instance of deleted Port Network object
     */
    @Override
    public void neutronPortDeleted(NeutronPort port) {
    	this.neutronPortCache.remove(port.getPortUUID());
    }

    /**
     * Services provide this interface method for taking action after a subnet has been created
     *
     * @param subnet
     *            instance of new Neutron Subnet object
     */
    @Override
    public void neutronSubnetCreated(NeutronSubnet subnet) {
    	this.neutronSubnetCache.put(subnet.getSubnetUUID(), subnet);
    }

    /**
     * Services provide this interface method for taking action after a subnet has been updated
     *
     * @param subnet
     *            instance of modified Neutron Subnet object
     */
    @Override
    public void neutronSubnetUpdated(NeutronSubnet subnet) {
    	this.neutronSubnetCache.put(subnet.getSubnetUUID(), subnet);
    }

    /**
     * Services provide this interface method for taking action after a subnet has been deleted
     *
     * @param subnet
     *            instance of deleted Router Subnet object
     */
    @Override
    public void neutronSubnetDeleted(NeutronSubnet subnet) {
    	this.neutronSubnetCache.remove(subnet.getSubnetUUID());
    }

	/**
     * Invoked to take action after a network has been created.
     *
     * @param network  An instance of new Neutron Network object.
     */
	@Override
    public void neutronNetworkCreated(NeutronNetwork network) {
    	this.neutronNetworkCache.put(network.getNetworkUUID(), network);
    }

    /**
     * Invoked to take action after a network has been updated.
     *
     * @param network An instance of modified Neutron Network object.
     */
    @Override
    public void neutronNetworkUpdated(NeutronNetwork network) {
    	this.neutronNetworkCache.put(network.getNetworkUUID(), network);
    }

    /**
     * Invoked to take action after a network has been deleted.
     *
     * @param network  An instance of deleted Neutron Network object.
     */
    @Override
    public void neutronNetworkDeleted(NeutronNetwork network) {
    	this.neutronNetworkCache.remove(network.getNetworkUUID());
    }

    /**
     * Invoked to take action after a network has been created.
     *
     * @param network  An instance of new Neutron Network object.
     */
    @Override
    public void neutronRouterCreated(NeutronRouter router) {
    	// do something
    }

    /**
     * Invoked to take action after a router has been updated.
     *
     * @param router An instance of modified Neutron Router object.
     */
    @Override
    public void neutronRouterUpdated(NeutronRouter router) {
    	// do something
    }

    /**
     * Invoked to take action after a router has been deleted.
     *
     * @param router  An instance of deleted Neutron Router object.
     */
    @Override
    public void neutronRouterDeleted(NeutronRouter router) {
    	// do something
    }

    /**
     * Invoked to take action after a floatingIP has been created.
     *
     * @param floatingIP  An instance of new Neutron Network object.
     */
    @Override
    public void neutronFloatingIPCreated(NeutronFloatingIP floatingIP) {
    	// do something
    }

    /**
     * Invoked to take action after a floatingIP has been updated.
     *
     * @param floatingIP An instance of modified Neutron FloatingIP object.
     */
    @Override
    public void neutronFloatingIPUpdated(NeutronFloatingIP floatingIP) {
    	// do something
    }

    /**
     * Invoked to take action after a floatingIP has been deleted.
     *
     * @param floatingIP  An instance of deleted Neutron FloatingIP object.
     */
    @Override
    public void neutronFloatingIPDeleted(NeutronFloatingIP floatingIP) {
    	// do something
    }

}