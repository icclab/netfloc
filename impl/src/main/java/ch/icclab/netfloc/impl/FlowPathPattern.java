/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import ch.icclab.netfloc.iface.IFlowPathePattern;
import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.INetworkPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import java.util.Map;

public class FlowPathPattern implements IFlowPathPattern {

	public Map<IBridgeOperator, List<Flow>> apply(INetworkPath path) {

		Map<IBridgeOperator, Flow> flows = new Map<IBridgeOperator, Flow>();

		IBridgeOperator begin = path.getBegin();
		flows.put(bridge, this.createBeginFlows(bridge, path.getBeginPort(), path.getNextLink(begin)));

		IBridgeOperator end = path.getEnd();
		flows.put(bridge, this.createEndFlows(bridge, path.getPreviousLink(end), path.getEndPort()));

		IBridgeOperator bridge = begin.getNext();
		while (bridge != null && !bridge.equals(end)) {
			flows.put(bridge, this.createAggregationFlows(bridge, path.getPreviousLink()));
			bridge = path.getNext(bridge);
		}

		return flows;
	}

	private List<Flow> createBeginFlows(IBridgeOperator bridge) {
		//Creating match object
		MatchBuilder matchBuilder = new MatchBuilder();

		// Match src & dst MAC
		// TODO
		// MatchUtils.createEthDstMatch(matchBuilder, new MacAddress(dstMac), null);
		// MatchUtils.createInPortMatch(matchBuilder, ingressNodeConnectorId);

		// Prepare Instuction
		InstructionsBuilder isb = new InstructionsBuilder();
		List<Instruction> instructions = Lists.newArrayList();
		InstructionBuilder ib = new InstructionBuilder();
		ApplyActionsBuilder aab = new ApplyActionsBuilder();
		ActionBuilder ab = new ActionBuilder();
		List<Action> actionList = Lists.newArrayList();

		// Output Action
		OutputActionBuilder output = new OutputActionBuilder();
		output.setOutputNodeConnector(egressNodeConnectorId);
		output.setMaxLength(65535);
		ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
		ab.setOrder(0);
		ab.setKey(new ActionKey(0));
		actionList.add(ab.build());

		// Apply Actions Instruction
		aab.setAction(actionList);
		ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
		ib.setOrder(0);
		ib.setKey(new InstructionKey(0));
		instructions.add(ib.build());

		// Create Flow
		FlowBuilder flowBuilder = new FlowBuilder();
		flowBuilder.setMatch(matchBuilder.build());

		// TODO generate flow id
		String flowId = "L2_Rule_" + dstMac;
		flowBuilder.setId(new FlowId(flowId));
		FlowKey key = new FlowKey(new FlowId(flowId));

		flowBuilder.setBarrier(true);
		flowBuilder.setTableId((short)0);
		flowBuilder.setKey(key);
		flowBuilder.setPriority(32768);
		flowBuilder.setFlowName(flowId);
		flowBuilder.setHardTimeout(0);
		flowBuilder.setIdleTimeout(0);

		return flowBuilder.setInstructions(isb.setInstruction(instructions).build()).build();
	}

}