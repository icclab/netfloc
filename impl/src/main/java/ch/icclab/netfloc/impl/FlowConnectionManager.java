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
import com.google.common.util.concurrent.FutureCallback;
import java.util.Map;
import java.util.HashMap;
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

	private final Map<INetworkPath, IFlowPathPattern> programSuccess = new HashMap<INetworkPath, IFlowPathPattern>();
	private final Map<INetworkPath, IFlowPathPattern> programFailure = new HashMap<INetworkPath, IFlowPathPattern>();

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

	// API
	public IFlowPathPattern getSuccessfulConnection(INetworkPath np) {
		return this.programSuccess.get(np);
	}

	public IFlowPathPattern getFailedConnection(INetworkPath np) {
		return this.programFailure.get(np);
	}

	public void registerPathPattern(IFlowPathPattern pattern) {
		// currently we have no way to use more than one pattern (TODO)
		this.flowPathPatterns.add(pattern);
	}

	// Callbacks
	@Override
	public void networkPathCreated(final INetworkPath np) {
		// TODO: decide which pattern
		final IFlowPathPattern pattern = this.flowPathPatterns.get(0);

		for (Map.Entry<IBridgeOperator, List<Flow>> flowEntry : pattern.apply(np).entrySet()) {
			for (Flow flow : flowEntry.getKey()) {
				flowprogrammer.programFlow(flowEntry.getValue(), flow, new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programSuccess.put(np, pattern);
					}

					public void onFailure(Throwable t) {
						programFailure.put(np, pattern);
					}
				});
			}
		}
	}

	@Override
	public void networkPathUpdated(final INetworkPath oldNp, final INetworkPath nNp) {
		// TODO: decide which pattern
		final IFlowPathPattern pattern = this.flowPathPatterns.get(0);

		for (Map.Entry<IBridgeOperator, List<Flow>> flowEntry : pattern.apply(oldNp).entrySet()) {
			for (Flow flow : flowEntry.getKey()) {
				flowprogrammer.deleteFlow(flowEntry.getValue(), flow, new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programSuccess.put(oldNp, pattern);
					}

					public void onFailure(Throwable t) {
						programFailure.put(oldNp, pattern);
					}
				});
			}
		}

		for (Map.Entry<IBridgeOperator, List<Flow>> flowEntry : pattern.apply(nNp).entrySet()) {
			for (Flow flow : flowEntry.getKey()) {
				flowprogrammer.programFlow(flowEntry.getValue(), flow, new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programSuccess.put(nNp, pattern);
					}

					public void onFailure(Throwable t) {
						programFailure.put(nNp, pattern);
					}
				});
			}
		}
	}
	
	@Override
	public void networkPathDeleted(final INetworkPath np) {
		// TODO: decide which pattern
		final IFlowPathPattern pattern = this.flowPathPatterns.get(0);

		for (Map.Entry<IBridgeOperator, List<Flow>> flowEntry : pattern.apply(np).entrySet()) {
			for (Flow flow : flowEntry.getKey()) {
				flowprogrammer.deleteFlow(flowEntry.getValue(), flow, new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programSuccess.put(np, pattern);
					}

					public void onFailure(Throwable t) {
						programFailure.put(np, pattern);
					}
				});
			}
		}
	}

}