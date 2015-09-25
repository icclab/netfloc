/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netfloc.iface;

import org.opendaylight.neutron.spi.NeutronPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbTerminationPointAugmentation;

import java.util.List;

// idk
public interface IHostPort extends IPortOperator {
	/*
	public L2Address getL2Address();
	public List<L3Address> getL3Addresses();
	public L3Address getL3Address(Tenant tenant); // maybe network???*/

	public String getMacAddress();
	public List<String> getIPAddresses();
	public void update(NeutronPort changes);
	public String getNeutronUuid();
}