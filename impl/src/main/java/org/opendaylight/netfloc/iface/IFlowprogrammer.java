/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.iface;

public interface IFlowprogrammer {
	public void programFlows(IFlowPathPattern flowPattern, INetworkPath networkPath);
	public void deleteFlows(IFlowPathPattern flowPattern, INetworkPath networkPath);
	public void programFlows(IFlowBridgePattern flowPattern, IBridgeOperator bridge);
	public void deleteFlows(IFlowBridgePattern flowPattern, IBridgeOperator bridge);
}