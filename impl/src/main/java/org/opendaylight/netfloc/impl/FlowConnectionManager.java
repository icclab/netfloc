/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import ch.icclab.netfloc.iface.INetworkPath;
import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.IFlowPathPattern;
import ch.icclab.netfloc.iface.IFlowBridgePattern;
import ch.icclab.netfloc.iface.INetworkPathListener;
import ch.icclab.netfloc.iface.IFlowprogrammer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;

public class FlowConnectionManager implements INetworkPathListener {
	
	// how do we decide which pattern to map to which path?
	private List<INetworkPath> networkPaths = new LinkedList<INetworkPath>();
	private List<IFlowPathPattern> flowPathPatterns = new LinkedList<IFlowPathPattern>();

	// how do we decide which pattern to map to which bridge?
	// mb reference them with strings or sth (map) ???
	private List<IBridgeOperator> bridges = new LinkedList<IBridgeOperator>();
	private List<IFlowBridgePattern> flowBridgePatterns = new LinkedList<IFlowBridgePattern>();

	private IFlowprogrammer flowprogrammer;

	// TODO standard flow pattern
	// endpoints:
	// - automatic arp response:
	// 		match arp, src mac dst ip (?) and ingress port
	//		action: mod dst mac etc to create response, output packet back to sender
	// - forwarding:
	//		match ingress port, src dst mac
	//		action: output to next port
	//		same for ingress traffic
	// aggregation: forwarding is the same

	// todo singleton
	public FlowConnectionManager(IFlowprogrammer flowprogrammer) {
		this.flowprogrammer = flowprogrammer;
	}

	// TODO: utility methods for querying the flow status
	// examples:
	// get all flow connections!
	// does x have connection?

	@Override
	public void networkPathCreated(INetworkPath np) {
		// TODO: decide which pattern
		IFlowPathPattern pattern = null;
		for (Map.Entry<IBridgeOperator, Flow> flowEntry : pattern.apply(np).entrySet()) {
			// TODO: handle status, mb retry etc.
			flowprogrammer.programFlow(flowEntry.getValue(), flowEntry.getKey());
		}
		// TODO: reference the flows
	}

	@Override
	public void networkPathUpdated(INetworkPath oldNp, INetworkPath nNp) {
		IFlowPathPattern pattern = null;

		for (Map.Entry<IBridgeOperator, Flow> flowEntry : pattern.apply(oldNp).entrySet()) {
			// TODO: handle status, mb retry etc.
			flowprogrammer.deleteFlow(flowEntry.getValue(), flowEntry.getKey());
		}

		for (Map.Entry<IBridgeOperator, Flow> flowEntry : pattern.apply(nNp).entrySet()) {
			// TODO: handle status, mb retry etc.
			flowprogrammer.programFlow(flowEntry.getValue(), flowEntry.getKey());
		}
	}
	
	@Override
	public void networkPathDeleted(INetworkPath np) {
		// TODO: decide which pattern
		IFlowPathPattern pattern = null;

		for (Map.Entry<IBridgeOperator, Flow> flowEntry : pattern.apply(np).entrySet()) {
			// TODO: handle status, mb retry etc.
			flowprogrammer.deleteFlow(flowEntry.getValue(), flowEntry.getKey());
		}
	}

}