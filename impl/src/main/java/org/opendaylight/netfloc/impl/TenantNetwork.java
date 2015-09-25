/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netfloc.impl;

import java.util.List;

import org.opendaylight.netfloc.iface.IBridgeOperator;
import org.opendaylight.netfloc.iface.INetworkOperator;
import org.opendaylight.netfloc.iface.ITenantBridgeOperator;
import org.opendaylight.netfloc.iface.ITenantNetworkOperator;

import java.util.LinkedList;

// no idea what this class is for tbh
public class TenantNetwork implements ITenantNetworkOperator {
	
	private INetworkOperator network;
	private ITenantBridgeOperator tenantBridges;
	private Tenant tenant;


	public TenantNetwork(INetworkOperator network, Tenant tenant) {
		this.network = network;
		this.tenant = tenant;
	}

	public Tenant getTenant() {
		return this.tenant;
	}

	public INetworkOperator getNetwork() {
		return this.network;
	}

	public List<ITenantBridgeOperator> getTenantBridges() {
		List<ITenantBridgeOperator> tenantBridges = new LinkedList<ITenantBridgeOperator>();
		for (IBridgeOperator bridge : this.network.getBridges()) {
			tenantBridges.add(bridge.getTenantBridge(this.getTenant()));
		}
		return tenantBridges;
	}
}