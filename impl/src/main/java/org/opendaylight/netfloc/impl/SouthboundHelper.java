/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netfloc.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbTerminationPointAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.ovsdb.port._interface.attributes.InterfaceExternalIds;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.netfloc.iface.IBridgeOperator;
import org.opendaylight.netfloc.iface.IPortOperator;
import org.opendaylight.netfloc.iface.IHostPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.InterfaceTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.InterfaceTypeInternal;

import java.util.List;
import java.util.LinkedList;

public class SouthboundHelper {
	
	public static IHostPort maybeCreateHostPort(List<IHostPort> ports, IBridgeOperator bo, TerminationPoint tp, OvsdbTerminationPointAugmentation terminationPointAugmentation) {
		String value = SouthboundHelper.getInterfaceExternalIdsValue(terminationPointAugmentation, Constants.EXTERNAL_ID_INTERFACE_ID);
		for (IHostPort port : ports) {
			if (value != null && value.equalsIgnoreCase(port.getNeutronUuid())) {
                port.relateSouthbound(bo, tp, terminationPointAugmentation);
				return port;
			}
		}
		return null;
	}

    public static IPortOperator maybeCreateInternalPort(IBridgeOperator bo, TerminationPoint tp, OvsdbTerminationPointAugmentation terminationPointAugmentation) {
        java.lang.Class<? extends InterfaceTypeBase> type = terminationPointAugmentation.getInterfaceType();
        if (type == InterfaceTypeInternal.class) {
            IPortOperator po = new InternalPort(bo, tp, terminationPointAugmentation);
        }
        return null;
    }

	public static String getInterfaceExternalIdsValue(
            OvsdbTerminationPointAugmentation terminationPointAugmentation, String key) {
        String value = null;
        List<InterfaceExternalIds> pairs = terminationPointAugmentation.getInterfaceExternalIds();
        if (pairs != null && !pairs.isEmpty()) {
            for (InterfaceExternalIds pair : pairs) {
                if (pair.getExternalIdKey().equals(key)) {
                    value = pair.getExternalIdValue();
                    break;
                }
            }
        }
        return value;
    }
}