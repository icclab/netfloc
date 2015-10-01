/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import org.opendaylight.neutron.spi.NeutronPort;
import ch.icclab.netfloc.iface.IBridgeIterator;
import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.ILinkPort;
import ch.icclab.netfloc.iface.INetworkPath;
import ch.icclab.netfloc.iface.INodeOperator;
import ch.icclab.netfloc.iface.IPortOperator;
import ch.icclab.netfloc.iface.IHostPort;
import ch.icclab.netfloc.iface.ITraversableBridge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbBridgeAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbTerminationPointAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbNodeAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

import java.util.List;
import java.util.LinkedList;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

public class NetworkGraphTest {
	
	final NetworkGraph network = new NetworkGraph();
	IBridgeOperator aggregationBridge;
	List<IBridgeOperator> hostBridges = new LinkedList<IBridgeOperator>();
	List<IHostPort> vmPorts = new LinkedList<IHostPort>();
	
	@Before
	public void setUp() {

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
			IBridgeOperator bridge = new Bridge(node, topologyNodeBridge, bridgeAugmentation);
			node.addBridge(bridge);

			// add vm's ports
			for (int j = 0; j < 5; j++) {
				NeutronPort neutronPort = mock(NeutronPort.class);
				IHostPort vmPort = new HostPort(neutronPort);
				OvsdbTerminationPointAugmentation terminationPointAugmentation1 = mock(OvsdbTerminationPointAugmentation.class);
				vmPort.relateSouthbound(bridge, mock(TerminationPoint.class), terminationPointAugmentation1);
				bridge.addHostPort(vmPort);
				vmPorts.add(vmPort);
			}
			// add a link port
			OvsdbTerminationPointAugmentation terminationPointAugmentation2 = mock(OvsdbTerminationPointAugmentation.class);
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
		aggregationBridge = new Bridge(node, topologyNode, bridgeAugmentation);
		node.addBridge(aggregationBridge);

		// add link ports to aggregation bridge
		for (int i = 0; i < 3; i++) {
			OvsdbTerminationPointAugmentation terminationPointAugmentation = mock(OvsdbTerminationPointAugmentation.class);
			ILinkPort linkPort = new LinkPort(aggregationBridge, mock(TerminationPoint.class), terminationPointAugmentation);
			aggregationBridge.addPort(linkPort);
			IPortOperator linkedPort = linkPorts.get(i);
			linkPort.setLinkedPort((ILinkPort)linkedPort);
		}
	}

	@Test
	public void testGetAdjacentBridges() {
		List<ITraversableBridge> adjacentBridges =  network.getAdjacentBridges(new TraversableBridge(aggregationBridge));
		assertTrue(adjacentBridges.size() == 3);
	}
	
	@Test
	public void getNetworkGraphs() {
		List<INetworkPath> networkPaths = network.getNetworkPaths(); 
		for(INetworkPath path : networkPaths){
			assertTrue(!path.getBeginPort().equals(path.getEndPort()));
		
		}
	
		
		assertTrue(networkPaths!= null);
	}

	@Test
	public void testTraverse() {

		// test abortion condition
		IBridgeIterator<Integer> iterator1 = new IBridgeIterator<Integer>() {

			int result = 0;

			public boolean visitBridge(ITraversableBridge bridge) {
				result++;
				if (result == 4) {
					return false;
				}
				return true;
			}

			public Integer getResult() {
				return new Integer(result);
			}
		};
		network.traverse(iterator1);
		assertTrue(iterator1.getResult() == 4);

		// test BFS condition
		IBridgeIterator<Integer> iterator2 = new IBridgeIterator<Integer>() {

			int result = 0;

			public boolean visitBridge(ITraversableBridge bridge) {
				result++;

				// first bridge does not have root bridge
				if (bridge.getRoot() == null) {
					return true;
				}
				List<ITraversableBridge> adjBr =  network.getAdjacentBridges(bridge.getRoot());
				assertTrue(adjBr.contains(bridge));
				return true;
			}

			public Integer getResult() {
				return new Integer(result);
			}
		};
		network.traverse(iterator2);
		assertTrue(iterator2.getResult() == 4);
	}

	@Test
	public void testGetNetworkPath() {
		IBridgeOperator beginBridge = hostBridges.get(0);
		IBridgeOperator endBridge = hostBridges.get(1);

		IHostPort beginPort = beginBridge.getHostPorts().get(0);
		IHostPort endPort = endBridge.getHostPorts().get(0);
		
		assertTrue(beginPort != null);
		assertTrue(endPort != null);
		assertTrue(network != null);
		
		INetworkPath networkPath = network.getNetworkPath(beginPort, endPort);

		assertTrue(networkPath != null);
		assertTrue("length of nw path is " + networkPath.getLength() + " instead of 3.", networkPath.getLength() == 3);
		assertTrue(networkPath.getBegin().equals(beginBridge));
		assertTrue(networkPath.getEnd().equals(endBridge));
		assertTrue(networkPath.getPrevious(endBridge).equals(aggregationBridge));
		assertTrue(networkPath.getPrevious(aggregationBridge).equals(beginBridge));
	}
}