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

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

public class FlowPathPattern implements IFlowPathPattern {

	public Map<IBridgeOperator, List<Flow>> apply(INetworkPath path) {

		Map<IBridgeOperator, Flow> flows = new HashMap<IBridgeOperator, Flow>();

		IBridgeOperator begin = path.getBegin();
		IBridgeOperator end = path.getEnd();
		flows.put(bridge, this.createBeginFlows(bridge, path.getBeginPort(), path.getNextLink(begin), path.getBeginPort().getMacAddress(), path.getEndPort().getMacAddress()));
		flows.put(bridge, this.createEndFlows(bridge, path.getPreviousLink(end), path.getEndPort(), path.getBeginPort().getMacAddress(), path.getEndPort().getMacAddress()));

		IBridgeOperator bridge = begin.getNext();
		while (bridge != null && !bridge.equals(end)) {
			flows.put(bridge, this.createAggregationFlows(bridge, path.getPreviousLink(), path.getBeginPort().getMacAddress(), path.getEndPort().getMacAddress()));
			bridge = path.getNext(bridge);
		}

		return flows;
	}

	private List<Flow> createBeginFlows(IBridgeOperator bridge, IHostPort hostPort, ILinkPort linkPort, String srcMac, String dstMac) {
		List<Flow> flows = new LinkedList<Flow>();

		flows.add(this.createForwardFlow(bridge, linkPort, hostPort, srcMac, dstMac));
		flows.add(this.createForwardFlow(bridge, hostPort, linkPort, dstMac, srcMac));

		return flows;
	}

	private List<Flow> createEndFlows(IBridgeOperator bridge, ILinkPort linkPort, IHostPort hostPort, String srcMac, String dstMac) {
		List<Flow> flows = new LinkedList<Flow>();

		flows.add(this.createForwardFlow(bridge, hostPort, linkPort, srcMac, dstMac));
		flows.add(this.createForwardFlow(bridge, linkPort, hostPort, dstMac, srcMac));

		return flows;
	}

	private List<Flow> createAggregationFlows(IBridgeOperator bridge, ILinkPort prevLink, ILinkPort nextLink, String srcMac, String dstMac) {
		List<Flow> flows = new LinkedList<Flow>();

		flows.add(this.createForwardFlow(bridge, prevLink, nextLink, srcMac, dstMac));
		flows.add(this.createForwardFlow(bridge, nextLink, prevLink, dstMac, srcMac));

		return flows;
	}

	private Flow createARPResponseFlow(IBridgeOperator bridge, IPortOperator inPort, String srcMac, String dstMac) {
		//Creating match object
		MatchBuilder matchBuilder = new MatchBuilder();

		// Match src & dst MAC
		// TODO string to mac addresses
		// TODO do we need inport matching?
		matchBuilder.setEthernetMatch(this.ethernetMatch(srcMac, dstMac, null));

		// Prepare Instuction
		InstructionsBuilder isb = new InstructionsBuilder();
		List<Instruction> instructions = Lists.newArrayList();
		InstructionBuilder ib = new InstructionBuilder();
		ApplyActionsBuilder aab = new ApplyActionsBuilder();
		ActionBuilder ab = new ActionBuilder();
		List<Action> actionList = Lists.newArrayList();

		// TODO rewrite Action (ARP)

		// Output Action
		OutputActionBuilder output = new OutputActionBuilder();

		// TODO convert of port maybe
		output.setOutputNodeConnector(outPort);

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

	private Flow createForwardFlow(IBridgeOperator bridge, IPortOperator inPort, IPortOperator outPort, String srcMac, String dstMac) {
		//Creating match object
		MatchBuilder matchBuilder = new MatchBuilder();

		// Match src & dst MAC
		// TODO string to mac addresses
		// TODO ethertype
		// TODO do we need inport matching?
		matchBuilder.setEthernetMatch(this.ethernetMatch(srcMac, dstMac, null));

		// Prepare Instuction
		InstructionsBuilder isb = new InstructionsBuilder();
		List<Instruction> instructions = Lists.newArrayList();
		InstructionBuilder ib = new InstructionBuilder();
		ApplyActionsBuilder aab = new ApplyActionsBuilder();
		ActionBuilder ab = new ActionBuilder();
		List<Action> actionList = Lists.newArrayList();

		// Output Action
		OutputActionBuilder output = new OutputActionBuilder();

		// TODO convert of port maybe
		output.setOutputNodeConnector(outPort);

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

	private static EthernetMatch ethernetMatch(MacAddress srcMac,
                                              MacAddress dstMac,
                                              Long etherType) {
        EthernetMatchBuilder emb = new  EthernetMatchBuilder();
        if (srcMac != null)
            emb.setEthernetSource(new EthernetSourceBuilder()
                .setAddress(srcMac)
                .build());
        if (dstMac != null)
            emb.setEthernetDestination(new EthernetDestinationBuilder()
                .setAddress(dstMac)
                .build());
        if (etherType != null)
            emb.setEthernetType(new EthernetTypeBuilder()
                .setType(new EtherType(etherType))
                .build());
        return emb.build();
    }

}