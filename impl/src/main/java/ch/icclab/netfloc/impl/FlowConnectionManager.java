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
import ch.icclab.netfloc.iface.IBridgeListener;
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

public class FlowConnectionManager implements INetworkPathListener, IBridgeListener {
	
	// how do we decide which pattern to map to which path?
	private List<INetworkPath> networkPaths = new LinkedList<INetworkPath>();
	private List<IFlowPathPattern> flowPathPatterns = new LinkedList<IFlowPathPattern>();

	// how do we decide which pattern to map to which bridge?
	// mb reference them with strings or sth (map) ???
	private List<IBridgeOperator> bridges = new LinkedList<IBridgeOperator>();
	private List<IFlowBridgePattern> flowBridgePatterns = new LinkedList<IFlowBridgePattern>();

	private final Map<INetworkPath, IFlowPathPattern> programPathSuccess = new HashMap<INetworkPath, IFlowPathPattern>();
	private final Map<INetworkPath, IFlowPathPattern> programPathFailure = new HashMap<INetworkPath, IFlowPathPattern>();
	private final Map<IBridgeOperator, IFlowBridgePattern> programBridgeSuccess = new HashMap<IBridgeOperator, IFlowBridgePattern>();
	private final Map<IBridgeOperator, IFlowBridgePattern> programBridgeFailure = new HashMap<IBridgeOperator, IFlowBridgePattern>();

	private IFlowprogrammer flowprogrammer;

	// todo singleton
	public FlowConnectionManager(IFlowprogrammer flowprogrammer) {
		this.flowprogrammer = flowprogrammer;
	}

	// API
	public IFlowPathPattern getSuccessfulConnection(INetworkPath np) {
		return this.programPathSuccess.get(np);
	}

	public IFlowPathPattern getFailedConnection(INetworkPath np) {
		return this.programPathFailure.get(np);
	}

	public void registerPathPattern(IFlowPathPattern pattern) {
		// currently we have no way to use more than one pattern (TODO)
		this.flowPathPatterns.add(pattern);
	}

	public void registerBridgePattern(IFlowBridgePattern pattern) {
		this.flowBridgePatterns.add(pattern);
	}

	// Callbacks
	@Override
	public void networkPathCreated(final INetworkPath np) {
		// TODO: decide which pattern
		final IFlowPathPattern pattern = this.flowPathPatterns.get(0);

		for (Map.Entry<IBridgeOperator, List<Flow>> flowEntry : pattern.apply(np).entrySet()) {
			for (Flow flow : flowEntry.getValue()) {
				flowprogrammer.programFlow(flow, flowEntry.getKey(), new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programPathSuccess.put(np, pattern);
					}

					public void onFailure(Throwable t) {
						programPathFailure.put(np, pattern);
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
			for (Flow flow : flowEntry.getValue()) {
				flowprogrammer.deleteFlow(flow, flowEntry.getKey(), new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programPathSuccess.put(oldNp, pattern);
					}

					public void onFailure(Throwable t) {
						programPathFailure.put(oldNp, pattern);
					}
				});
			}
		}

		for (Map.Entry<IBridgeOperator, List<Flow>> flowEntry : pattern.apply(nNp).entrySet()) {
			for (Flow flow : flowEntry.getValue()) {
				flowprogrammer.programFlow(flow, flowEntry.getKey(), new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programPathSuccess.put(nNp, pattern);
					}

					public void onFailure(Throwable t) {
						programPathFailure.put(nNp, pattern);
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
			for (Flow flow : flowEntry.getValue()) {
				flowprogrammer.deleteFlow(flow, flowEntry.getKey(), new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programPathSuccess.put(np, pattern);
					}

					public void onFailure(Throwable t) {
						programPathFailure.put(np, pattern);
					}
				});
			}
		}
	}
	
	@Override	
	public void bridgeCreated(final IBridgeOperator bo) {
		final IFlowBridgePattern pattern = this.flowBridgePatterns.get(0);

		for (Flow flow : pattern.apply(bo)) {
			flowprogrammer.programFlow(flow, bo, new FutureCallback<Void>() {
				public void onSuccess(Void result) {
					programBridgeSuccess.put(bo, pattern);
				}

				public void onFailure(Throwable t) {
					programBridgeFailure.put(bo, pattern);
				}
			});
		}
	}
	
	@Override		
	public void bridgeUpdated(final IBridgeOperator oldBo, final IBridgeOperator nBo) {
		// not needed?
	}
	
	@Override		
	public void bridgeDeleted(final IBridgeOperator bo) {
		// not needed?
	}

	// TODO
	// apply FlowChainPattern to ServiceChain : for delete and for create

}