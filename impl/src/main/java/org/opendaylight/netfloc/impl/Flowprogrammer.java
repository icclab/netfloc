/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netfloc.impl;

import org.opendaylight.netfloc.iface.IFlowprogrammer;
import org.opendaylight.netfloc.iface.INetworkPath;
import org.opendaylight.netfloc.iface.IFlowBridgePattern;
import org.opendaylight.netfloc.iface.IBridgeOperator;
import org.opendaylight.netfloc.iface.IFlowPathPattern;

public class Flowprogrammer implements IFlowprogrammer {

	public void programFlows(IFlowPathPattern flowPattern, INetworkPath networkPath) {
		// TODO
	}

	public void programFlows(IFlowBridgePattern flowPattern, IBridgeOperator bridge) {
		// TODO
	}
}