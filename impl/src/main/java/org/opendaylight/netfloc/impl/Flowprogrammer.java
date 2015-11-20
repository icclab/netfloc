/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import ch.icclab.netfloc.iface.IFlowprogrammer;
import ch.icclab.netfloc.iface.INetworkPath;
import ch.icclab.netfloc.iface.IFlowBridgePattern;
import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.IFlowPathPattern;

public class Flowprogrammer implements IFlowprogrammer {

	// this class might be static when we don't need flow references (per ID or sth)

	public void programFlows(IFlowPathPattern flowPattern, INetworkPath networkPath) {
		// TODO
		// the programmed flows have to be referenced for deletion
		// is the pattern allready sufficient or do we need a flow id ?
	}

	public void deleteFlows(IFlowPathPattern flowPattern, INetworkPath networkPath) {
		// TODO
	}

	public void programFlows(IFlowBridgePattern flowPattern, IBridgeOperator bridge) {
		// TODO
		// the programmed flows have to be referenced for deletion ?
		// is the pattern allready sufficient or do we need a flow id ?
	}
	
	public void deleteFlows(IFlowBridgePattern flowPattern, IBridgeOperator bridge) {
		// TODO
	}

	// draft methods private
	/*
		push flow to bridge
			- takes instruction
			- takes bridge
			- does mdsal transaction

		delete flow on bridge
			- takes instruction or flow id
			- takes bridge
			- either deletes based on flow id or on instruction?
			
		traverse pattern
			- takes callback (push/delete)
			- applies callback onto bridges
	*/	
}