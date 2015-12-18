/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import ch.icclab.netfloc.iface.IFlowPathPattern;
import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.INetworkPath;
import ch.icclab.netfloc.iface.IPortOperator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

public class FlowPathPattern implements IFlowPathPattern {

	// TODO... this should be managed by the flow connection manager
	private static final int FORWARDING_PRIORITY = 10;
	private static final int BROADCAST_PRIORITY = 10;

	public Map<IBridgeOperator, List<Flow>> apply(INetworkPath path) {

		Map<IBridgeOperator, List<Flow>> flows = new HashMap<IBridgeOperator, List<Flow>>();

		// begin & end bridges
		IBridgeOperator begin = path.getBegin();
		IBridgeOperator end = path.getEnd();
		flows.put(begin, this.createBidirectionalFlows(begin, path.getBeginPort(), path.getNextLink(begin), path.getBeginPort().getMacAddress(), path.getEndPort().getMacAddress()));
		flows.put(end, this.createBidirectionalFlows(end, path.getPreviousLink(end), path.getEndPort(), path.getBeginPort().getMacAddress(), path.getEndPort().getMacAddress()));

		// aggregation bridges
		IBridgeOperator bridge = path.getNext(begin);
		List<Flow> flowList = new LinkedList<Flow>();
		while (bridge != null && !bridge.equals(end)) {
			flowList.addAll(this.createBidirectionalFlows(bridge, path.getPreviousLink(bridge), path.getNextLink(bridge), path.getBeginPort().getMacAddress(), path.getEndPort().getMacAddress()));
			bridge = path.getNext(bridge);
		}

		flows.put(bridge, flowList);

		return flows;
	}

	private List<Flow> createBidirectionalFlows(IBridgeOperator bridge, IPortOperator srcPort, IPortOperator dstPort, String srcMac, String dstMac) {
		List<Flow> flows = new LinkedList<Flow>();

		flows.add(OpenFlowUtil.createForwardFlow(bridge, srcPort, dstPort, srcMac, dstMac, FORWARDING_PRIORITY));
		flows.add(OpenFlowUtil.createForwardFlow(bridge, dstPort, srcPort, dstMac, srcMac, FORWARDING_PRIORITY));
		List<IPortOperator> bcOutSrc = new LinkedList<IPortOperator>();
		bcOutSrc.add(dstPort);
		flows.add(OpenFlowUtil.createBroadcastFlow(bridge, srcPort, bcOutSrc, srcMac, BROADCAST_PRIORITY));
		List<IPortOperator> bcOutDst = new LinkedList<IPortOperator>();
		bcOutDst.add(srcPort);
		flows.add(OpenFlowUtil.createBroadcastFlow(bridge, dstPort, bcOutDst, dstMac, BROADCAST_PRIORITY));

		return flows;
	}
}