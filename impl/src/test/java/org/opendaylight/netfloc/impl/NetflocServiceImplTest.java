/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import ch.icclab.netfloc.iface.IServiceChain;
import ch.icclab.netfloc.iface.IServiceChainListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netfloc.rev150105.*;

import org.opendaylight.neutron.spi.NeutronPort;
import ch.icclab.netfloc.iface.IBridgeIterator;
import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.ILinkPort;
import ch.icclab.netfloc.iface.INetworkPath;
import ch.icclab.netfloc.iface.INetworkPathListener;
import ch.icclab.netfloc.iface.INodeOperator;
import ch.icclab.netfloc.iface.IPortOperator;
import ch.icclab.netfloc.iface.IHostPort;
import ch.icclab.netfloc.iface.ITraversableBridge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.DatapathId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbBridgeAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbTerminationPointAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbNodeAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Source;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Destination;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.neutron.spi.Neutron_IPs;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Matchers.any;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

public class NetflocServiceImplTest {

	final NetworkGraph network = new NetworkGraph();
	NetflocServiceImpl service;
	IBridgeOperator aggregationBridge;
	List<IBridgeOperator> hostBridges = new LinkedList<IBridgeOperator>();
	List<IHostPort> vmPorts = new LinkedList<IHostPort>();

	@Before
	public void setUp() {
		service = new NetflocServiceImpl(network);

		// memorize the link ports
		List<IPortOperator> linkPorts = new LinkedList<IPortOperator>();

		// add host bridges
		for (int i = 0; i < 3; i++) {
			Node topologyNodeNode = mock(Node.class);

			when(topologyNodeNode.getNodeId()).thenReturn(new NodeId("node:" + i));

			OvsdbNodeAugmentation nodeAugmentation = mock(OvsdbNodeAugmentation.class);
			INodeOperator node = new Datapath(network, topologyNodeNode, nodeAugmentation);
			network.addNode(node);

			Node topologyNodeBridge = mock(Node.class);

			when(topologyNodeBridge.getNodeId()).thenReturn(new NodeId("node:" + i + ":bridge:" + i));

			OvsdbBridgeAugmentation bridgeAugmentation = mock(OvsdbBridgeAugmentation.class);
			DatapathId dpId = mock(DatapathId.class);
			when(dpId.getValue()).thenReturn("00:00:00:00:0" + i);
			when(bridgeAugmentation.getDatapathId()).thenReturn(dpId);
			IBridgeOperator bridge = new Bridge(node, topologyNodeBridge, bridgeAugmentation);
			node.addBridge(bridge);

			// add vm's ports
			for (int j = 0; j < 5; j++) {
				NeutronPort neutronPort = mock(NeutronPort.class);
				when(neutronPort.getPortUUID()).thenReturn("" + i + j);
				OvsdbTerminationPointAugmentation terminationPointAugmentation1 = mock(OvsdbTerminationPointAugmentation.class);
				when(terminationPointAugmentation1.getOfport()).thenReturn(new Long(j));
				IHostPort vmPort = new HostPort(bridge, mock(TerminationPoint.class), terminationPointAugmentation1, neutronPort);
				bridge.addHostPort(vmPort);
				vmPorts.add(vmPort);
			}
			// add a link port
			OvsdbTerminationPointAugmentation terminationPointAugmentation2 = mock(OvsdbTerminationPointAugmentation.class);
			when(terminationPointAugmentation2.getOfport()).thenReturn(new Long(i));
			IPortOperator linkPort = new LinkPort(bridge, mock(TerminationPoint.class), terminationPointAugmentation2);
			bridge.addPort(linkPort);

			// memorize link ports
			linkPorts.add(linkPort);

			hostBridges.add(bridge);
		}

		// add an aggregation bridge
		Node topologyNode = mock(Node.class);
		when(topologyNode.getNodeId()).thenReturn(new NodeId("node:" + "a" + ":bridge:" + "a"));
		OvsdbNodeAugmentation nodeAugmentation = mock(OvsdbNodeAugmentation.class);
		INodeOperator node = new Datapath(network, topologyNode, nodeAugmentation);
		network.addNode(node);

		OvsdbBridgeAugmentation bridgeAugmentation = mock(OvsdbBridgeAugmentation.class);
		DatapathId dpId = mock(DatapathId.class);
		when(dpId.getValue()).thenReturn("00:00:10:10:10");
		when(bridgeAugmentation.getDatapathId()).thenReturn(dpId);
		aggregationBridge = new Bridge(node, topologyNode, bridgeAugmentation);
		node.addBridge(aggregationBridge);

		// add link ports to aggregation bridge
		for (int i = 0; i < 3; i++) {
			OvsdbTerminationPointAugmentation terminationPointAugmentation = mock(OvsdbTerminationPointAugmentation.class);
			when(terminationPointAugmentation.getOfport()).thenReturn(new Long(i));
			ILinkPort linkPort = new LinkPort(aggregationBridge, mock(TerminationPoint.class), terminationPointAugmentation);
			aggregationBridge.addPort(linkPort);
			IPortOperator linkedPort = linkPorts.get(i);
			linkPort.setLinkedPort((ILinkPort)linkedPort);
		}
	}

	@Test
	public void testCreateServiceChain() {
		final List<IServiceChain> chains = new LinkedList<IServiceChain>();
		IServiceChainListener listener = new IServiceChainListener() {
			public void serviceChainCreated(IServiceChain sc) {
				chains.add(sc);
			}
			public void serviceChainDeleted(IServiceChain sc) {
				// NOOP
			}
		};
		service.registerServiceChainListener(listener);
		CreateServiceChainInput input = mock(CreateServiceChainInput.class);
		when(input.getNeutronPorts()).thenReturn("11,22,23,12");
		service.createServiceChain(input);

		assertTrue("Amount of chains created should be 1 instead of " + chains.size(), chains.size() == 1);
		assertTrue(chains.get(0) != null);
		assertTrue(chains.get(0).getBegin().getLength() == 3);
	}
}