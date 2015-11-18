/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import org.opendaylight.neutron.spi.NeutronPort;
import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.INodeOperator;
import ch.icclab.netfloc.iface.ITenantBridgeOperator;
import ch.icclab.netfloc.iface.IHostPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbBridgeAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;

import java.util.List;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

public class TenantBridgeTest {

	ITenantBridgeOperator tenantBridge;
	IHostPort cachedHostPort;
	IBridgeOperator cachedBridge;

	@Before
    public void setUp() {
  //   	Node tpnode = mock(Node.class);
  //   	INodeOperator node = mock(INodeOperator.class);
		// OvsdbBridgeAugmentation bridgeAugmentation = mock(OvsdbBridgeAugmentation.class);

		// this.cachedBridge = new Bridge(node, tpnode, bridgeAugmentation);

		// final String tenantID = "MyId";

		// Tenant tenant = new Tenant(tenantID);
		// this.tenantBridge = new TenantBridge(tenant, cachedBridge);

		// NeutronPort neutronPort = mock(NeutronPort.class);
		// when(neutronPort.getTenantID()).thenReturn(tenantID);

		// this.cachedHostPort = new HostPort(neutronPort);

		// tenantBridge.addHostPort(this.cachedHostPort);
    }

	@Test
	public void testGetHostPorts() {
		// assertTrue(cachedBridge.getHostPorts().get(0).equals(cachedHostPort));
		// assertTrue(tenantBridge.getHostPorts().get(0).equals(cachedHostPort));
	}

	@Test
	public void testRemoveHostPort() {
		// IBridgeOperator bridge = tenantBridge.getBridge();

		// IHostPort hostPort = bridge.getHostPorts().get(0);

		// tenantBridge.removeHostPort(hostPort);

		// assertTrue(tenantBridge.getHostPorts().isEmpty());
	}
}