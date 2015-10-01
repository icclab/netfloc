/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.iface;

import java.util.List;

// no idea what this class is for tbh
public interface ITenantNetworkOperator extends ITenantOwner {
	public INetworkOperator getNetwork();

	public List<ITenantBridgeOperator> getTenantBridges();
}