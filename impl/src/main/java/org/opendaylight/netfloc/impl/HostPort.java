/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netfloc.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbTerminationPointAugmentation;

import org.opendaylight.neutron.spi.NeutronPort;
import org.opendaylight.neutron.spi.Neutron_IPs;
import org.opendaylight.netfloc.iface.IBridgeOperator;
import org.opendaylight.netfloc.iface.IHostPort;

import java.util.List;
import java.util.LinkedList;

// idk
public class HostPort extends Port implements IHostPort {

	private NeutronPort neutronPort;

	public HostPort(NeutronPort neutronPort) {
		this.neutronPort = neutronPort;
		this.tenant = new Tenant(neutronPort.getTenantID());
	}

	public String getMacAddress() {
		return this.neutronPort.getMacAddress();
	}

	public List<String> getIPAddresses() {
		List<String> ipAddr = new LinkedList<String>();
		for (Neutron_IPs nip : this.neutronPort.getFixedIPs()) {
			ipAddr.add(nip.getIpAddress());
		}
		return ipAddr;
	}

	public void update(NeutronPort changes) {
		this.neutronPort = changes;
	}

	public String getNeutronUuid() {
		return this.neutronPort.getPortUUID();
	}
}