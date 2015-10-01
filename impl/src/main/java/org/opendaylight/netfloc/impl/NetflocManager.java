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
import ch.icclab.netfloc.iface.ITenantNetworkOperator;
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

	private List<IHostPort> neutronPortCache = new LinkedList<IHostPort>();
	private Map<String, NeutronSubnet> neutronSubnetCache = new HashMap<String, NeutronSubnet>();
	private Map<String, NeutronNetwork> neutronNetworkCache = new HashMap<String, NeutronNetwork>();
	private List<INetworkPathListener> networkPathListeners = new LinkedList<INetworkPathListener>();

	public NetflocManager(NetworkGraph graph) {
		this.graph = graph;
	}

	public void registerNetworkPathListener(INetworkPathListener npl) {
		this.networkPathListeners.add(npl);
	}

	@Override
	public void handleBridgeCreate(Node node, OvsdbBridgeAugmentation ovsdbBridgeAugmentation) {
		INodeOperator no = graph.getParentNode(node.getNodeId());
		IBridgeOperator bo = new Bridge(no, node, ovsdbBridgeAugmentation);
		no.addBridge(bo);
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
		INodeOperator no = graph.getParentNode(node.getNodeId());

		if (no == null) {
			throw new IllegalStateException("node is null on port create");
		}

		IBridgeOperator bo = null;

		for (IBridgeOperator br : no.getBridges()) {
			if (br.getNodeId().equals(node.getNodeId())) {
				bo = br; break;
			}
		}

		IPortOperator port = this.createPort(bo, tp, tpa);

		bo.addPort(port);

	}

	private IPortOperator createPort(IBridgeOperator bo, TerminationPoint tp, OvsdbTerminationPointAugmentation tpa) {
		IPortOperator port = SouthboundHelper.maybeCreateHostPort(this.neutronPortCache, bo, tp, tpa);

		if (port != null) {
			logger.info("is a neutron port");
			IHostPort srcPort = (IHostPort)port;
			for (IHostPort dstPort : this.neutronPortCache) {
				if (srcPort.canConnectTo(dstPort)) {
					for (INetworkPathListener npl : this.networkPathListeners) {
						npl.networkPathCreated(graph.getNetworkPath(srcPort, dstPort));
					}
				}
			}
			return port;
		}

		port = SouthboundHelper.maybeCreateInternalPort(bo, tp, tpa);

		if (port != null) {
			logger.info("is an internal port");
			return port;
		}

		logger.info("is a link port");
		return new LinkPort(bo, tp, tpa);
	}

	@Override
	public void handlePortDelete(Node node, OvsdbTerminationPointAugmentation tpa) {
		INodeOperator no = graph.getParentNode(node.getNodeId());

		if (no == null) {
			throw new IllegalStateException("node is null on port delete");
		}
		
		IBridgeOperator bo = null;

		for (IBridgeOperator br : no.getBridges()) {
			if (br.getNodeId().equals(node.getNodeId())) {
				bo = br; break;
			}
		}

		IPortOperator po = bo.getPort(tpa.getPortUuid());

		if (po instanceof IHostPort) {
			IHostPort hostPort = (IHostPort)po;
			for (INetworkPath removedPath : graph.getConnectableNetworkPaths(hostPort, this.neutronPortCache)) {
				for (INetworkPathListener npl : this.networkPathListeners) {
					npl.networkPathDeleted(removedPath);
				}
			}
		}

		bo.removePort(po);

	}
	
	@Override
	public void handlePortUpdate(Node node, TerminationPoint tp, OvsdbTerminationPointAugmentation tpa) {
		INodeOperator no = graph.getParentNode(node.getNodeId());

		if (no == null) {
			throw new IllegalStateException("node is null on port update");
		}

		IBridgeOperator bo = null;

		for (IBridgeOperator br : no.getBridges()) {
			if (br.getNodeId().equals(node.getNodeId())) {
				bo = br; break;
			}
		}

		IPortOperator po = bo.getPort(tpa.getPortUuid());

		po.update(tp, tpa);

		// todo test connections

	}

	@Override
	public void handleLinkCreate(Link link) {
		TpId tpIdSrc = link.getSource().getSourceTp();
		TpId tpIdDst = link.getDestination().getDestTp();

		if (tpIdSrc == null || tpIdDst == null) {
			logger.error("TpId is null for <{}>, <{}>", tpIdSrc, tpIdDst);
		}

		ILinkPort portSrc = graph.getLinkPort(tpIdSrc);
		ILinkPort portDst = graph.getLinkPort(tpIdDst);

		portSrc.setLinkedPort(portDst);

		// todo: update network paths?
	}

	@Override
	public void handleLinkDelete(Link link) {
		TpId tpIdSrc = link.getSource().getSourceTp();
		TpId tpIdDst = link.getDestination().getDestTp();

		if (tpIdSrc == null || tpIdDst == null) {
			logger.error("TpId is null for <{}>, <{}>", tpIdSrc, tpIdDst);
		}

		ILinkPort portSrc = graph.getLinkPort(tpIdSrc);
		ILinkPort portDst = graph.getLinkPort(tpIdDst);

		// old paths
		List<INetworkPath> oldPaths = graph.getConnectableNetworkPaths(this.neutronPortCache);

		portSrc.removeLinkedPort(portDst);
		
		// notify network path listeners by comparing old to new path list
		List<INetworkPath> newPaths = graph.getConnectableNetworkPaths(this.neutronPortCache);
		
		for (INetworkPath oldPath : oldPaths) {
			boolean found = false;
			for (INetworkPath newPath : newPaths) {
				if (oldPath.isEqualConnection(newPath)) {
					found = true;

					if (!oldPath.equals(newPath)) {
						// link is broken but connection can be recovered
						for (INetworkPathListener npl : this.networkPathListeners) {
							npl.networkPathUpdated(newPath);
						}
					}
				}
			}
			if (!found) {
				// this is a broken link and connection cannot be recovered
				for (INetworkPathListener npl : this.networkPathListeners) {
					npl.networkPathDeleted(oldPath);
				}
			}
		}
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
    	IHostPort po = new HostPort(port);
    	this.neutronPortCache.add(po);
    }

    /**
     * Services provide this interface method for taking action after a port has been updated
     *
     * @param port
     *            instance of modified Neutron Port object
     */
    @Override
    public void neutronPortUpdated(NeutronPort port) {
    	IHostPort cachedPort = this.getHostPortByNeutronId(port.getPortUUID());
    	cachedPort.update(port);
    }

    /**
     * Services provide this interface method for taking action after a port has been deleted
     *
     * @param port
     *            instance of deleted Port Network object
     */
    @Override
    public void neutronPortDeleted(NeutronPort port) {
    	IHostPort cachedPort = this.getHostPortByNeutronId(port.getPortUUID());
    	this.neutronPortCache.remove(cachedPort);
    	for (IBridgeOperator bo : graph.getBridges()) {
    		IHostPort po = bo.getHostPort(port.getPortUUID());
    		if (po != null) {
    			bo.removeHostPort(po);
    			return;
    		}
    	}
    }

    private IHostPort getHostPortByNeutronId(String id) {
    	for (IHostPort cachedPort : this.neutronPortCache) {
    		if (cachedPort.getNeutronUuid().equals(id)) {
    			return cachedPort;
    		}
    	}
    	return null;
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