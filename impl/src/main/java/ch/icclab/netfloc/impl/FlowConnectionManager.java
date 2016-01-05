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
import ch.icclab.netfloc.iface.IBroadcastListener;
import ch.icclab.netfloc.iface.IFlowBroadcastPattern;
import ch.icclab.netfloc.iface.IFlowPathPattern;
import ch.icclab.netfloc.iface.IFlowBridgePattern;
import ch.icclab.netfloc.iface.INetworkPathListener;
import ch.icclab.netfloc.iface.IFlowprogrammer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import com.google.common.util.concurrent.FutureCallback;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

public class FlowConnectionManager implements IBroadcastListener, INetworkPathListener, IBridgeListener {
	
	// how do we decide which pattern to map to which path?
	private List<INetworkPath> networkPaths = new LinkedList<INetworkPath>();
	private List<IFlowPathPattern> flowPathPatterns = new LinkedList<IFlowPathPattern>();
	private List<IFlowBroadcastPattern> broadcastPatterns = new LinkedList<IFlowBroadcastPattern>();

	// how do we decide which pattern to map to which bridge?
	// mb reference them with strings or sth (map) ???
	private List<IBridgeOperator> bridges = new LinkedList<IBridgeOperator>();
	private List<IFlowBridgePattern> flowBridgePatterns = new LinkedList<IFlowBridgePattern>();

	private final Map<Set<INetworkPath>, IFlowBroadcastPattern> programBroadcastSuccess = new HashMap<Set<INetworkPath>, IFlowBroadcastPattern>();
	private final Map<Set<INetworkPath>, IFlowBroadcastPattern> programBroadcastFailure = new HashMap<Set<INetworkPath>, IFlowBroadcastPattern>();
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

	public void registerBroadcastPattern(IFlowBroadcastPattern pattern) {
		this.broadcastPatterns.add(pattern);
	}

	@Override
	public void broadcastCreated(final Set<INetworkPath> nps) {
		final IFlowBroadcastPattern pattern = this.broadcastPatterns.get(0);

		this.programBroadcastFlows(nps, pattern);
		this.updateBroadcastFlows(nps, pattern);
	}

	private void programBroadcastFlows(final Set<INetworkPath> nps, final IFlowBroadcastPattern pattern) {
		for (Map.Entry<IBridgeOperator, List<Flow>> flowEntry : pattern.apply(nps).entrySet()) {
			for (Flow flow : flowEntry.getValue()) {
				flowprogrammer.programFlow(flow, flowEntry.getKey(), new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programBroadcastSuccess.put(nps, pattern);
					}

					public void onFailure(Throwable t) {
						programBroadcastFailure.put(nps, pattern);
					}
				});
			}
		}
	}

	private void deleteBroadcastFlows(final Set<INetworkPath> nps, final IFlowBroadcastPattern pattern) {
		for (Map.Entry<IBridgeOperator, List<Flow>> flowEntry : pattern.apply(nps).entrySet()) {
			for (Flow flow : flowEntry.getValue()) {
				flowprogrammer.deleteFlow(flow, flowEntry.getKey(), new FutureCallback<Void>() {
					public void onSuccess(Void result) {
						programBroadcastSuccess.remove(nps);
					}

					public void onFailure(Throwable t) {
						// todo
						//deleteBroadcastFailure.put(nps, pattern);
					}
				});
			}
		}
	}

	private void updateBroadcastFlows(final Set<INetworkPath> nps, final IFlowBroadcastPattern pattern) {
		List<Set<INetworkPath>> toDelete = new LinkedList<Set<INetworkPath>>();
		List<Set<INetworkPath>> toProgram = new LinkedList<Set<INetworkPath>>();
		for (Map.Entry<Set<INetworkPath>, IFlowBroadcastPattern> successEntry : this.programBroadcastSuccess.entrySet()) {

			if (!successEntry.getValue().equals(pattern)) {
				continue;
			}

			boolean found = false;
			for (INetworkPath newPath : nps) {
				for (INetworkPath oldPath : successEntry.getKey()) {
					if (oldPath.getBeginPort().equals(newPath.getEndPort())) {
						toDelete.add(successEntry.getKey());
						Set<INetworkPath> updatedSet = new HashSet<INetworkPath>(successEntry.getKey());
						updatedSet.add(newPath);
						toProgram.add(updatedSet);
						found = true;
						continue;
					}
				}
				if (found) {
					continue;
				}
			}
		}

		for (Set<INetworkPath> deleteSet : toDelete) {
			this.deleteBroadcastFlows(deleteSet, pattern);
		}

		for (Set<INetworkPath> programSet : toProgram) {
			this.programBroadcastFlows(programSet, pattern);
		}
	}

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