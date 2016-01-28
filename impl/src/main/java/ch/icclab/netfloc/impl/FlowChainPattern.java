/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import ch.icclab.netfloc.iface.IFlowChainPattern;
import ch.icclab.netfloc.iface.IServiceChain;
import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.IPortOperator;
import ch.icclab.netfloc.iface.IHostPort;
import ch.icclab.netfloc.iface.INetworkPath;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowChainPattern implements IFlowChainPattern {

	// TODO manage this somewhere, possibly in the flowconneciton manager
	private static final int CHAIN_PRIORITY = 10;
	
	private static final Logger logger = LoggerFactory.getLogger(FlowChainPattern.class);

	public List<Map<IBridgeOperator, List<Flow>>> apply(IServiceChain sc) {
		List<Map<IBridgeOperator, List<Flow>>> flows = new LinkedList<Map<IBridgeOperator, List<Flow>>>();

		INetworkPath beginPath = sc.getBegin();
		int hop = 0;
		flows.add(this.createForwardPathFlows(beginPath, sc.getChainId(), hop));
		INetworkPath endPath = sc.getEnd();
		logger.info("apply to begin path {}", beginPath);

		INetworkPath path = sc.getNext(beginPath);
		while (path != null && !path.equals(endPath)) {
			logger.info("getting next path {}", path);
			hop++;
			flows.add(this.createRewritePathFlows(path, sc.getChainId(), hop));
			logger.info("apply to next path {}", path);
			path = sc.getNext(path);
		}

		flows.add(this.createEndRewritePathFlows(endPath, sc.getChainId(), hop));
		logger.info("apply to end path {}", endPath);

		logger.info("FlowChainPattern apply flows: {}", flows);

		return flows;
	}

	private Map<IBridgeOperator, List<Flow>> createForwardPathFlows(INetworkPath path, int chainId, int hop) {
		Map<IBridgeOperator, List<Flow>> flows = new HashMap<IBridgeOperator, List<Flow>>();

		IBridgeOperator beginBridge = path.getBegin();
		flows.put(beginBridge, createAggregationBridgeFlows(beginBridge, chainId, hop, path.getBeginPort(), path.getNextLink(beginBridge), CHAIN_PRIORITY));
		IBridgeOperator endBridge = path.getEnd();
		flows.put(endBridge, createAggregationBridgeFlows(endBridge, chainId, hop, path.getPreviousLink(endBridge), path.getEndPort(), CHAIN_PRIORITY));

		IBridgeOperator bridge = path.getNext(beginBridge);
		while (bridge != null && !bridge.equals(endBridge)) {
			flows.put(bridge, createAggregationBridgeFlows(bridge, chainId, hop, path.getPreviousLink(bridge), path.getNextLink(bridge), CHAIN_PRIORITY));
		}

		return flows;
	}

	private Map<IBridgeOperator, List<Flow>> createRewritePathFlows(INetworkPath path, int chainId, int hop) {
		Map<IBridgeOperator, List<Flow>> flows = new HashMap<IBridgeOperator, List<Flow>>();

		IBridgeOperator beginBridge = path.getBegin();
		flows.put(beginBridge, createBeginBridgeFlows(beginBridge, chainId, hop, path.getBeginPort(), path.getNextLink(beginBridge), CHAIN_PRIORITY));
		IBridgeOperator endBridge = path.getEnd();
		flows.put(endBridge, createAggregationBridgeFlows(endBridge, chainId, hop, path.getPreviousLink(endBridge), path.getEndPort(), CHAIN_PRIORITY));

		IBridgeOperator bridge = path.getNext(beginBridge);
		while (bridge != null && !bridge.equals(endBridge)) {
			flows.put(bridge, createAggregationBridgeFlows(bridge, chainId, hop, path.getPreviousLink(bridge), path.getNextLink(bridge), CHAIN_PRIORITY));
		}

		return flows;
	}

	private Map<IBridgeOperator, List<Flow>> createEndRewritePathFlows(INetworkPath path, int chainId, int hop) {
		Map<IBridgeOperator, List<Flow>> flows = new HashMap<IBridgeOperator, List<Flow>>();

		IBridgeOperator beginBridge = path.getBegin();
		flows.put(beginBridge, createBeginBridgeFlows(beginBridge, chainId, hop, path.getBeginPort(), path.getNextLink(beginBridge), CHAIN_PRIORITY));
		IBridgeOperator endBridge = path.getEnd();
		flows.put(endBridge, createEndBridgeFlows(endBridge, chainId, hop, path.getPreviousLink(endBridge), path.getEndPort(), CHAIN_PRIORITY));

		IBridgeOperator bridge = path.getNext(beginBridge);
		while (bridge != null && !bridge.equals(endBridge)) {
			flows.put(bridge, createAggregationBridgeFlows(bridge, chainId, hop, path.getPreviousLink(bridge), path.getNextLink(bridge), CHAIN_PRIORITY));
		}

		return flows;
	}

	private List<Flow> createAggregationBridgeFlows(IBridgeOperator bridge, int chainId, int hop, IPortOperator inPort, IPortOperator outPort, int priority) {
		List<Flow> flows = new LinkedList<Flow>();

		flows.add(this.createChainForwardFlow(bridge, chainId, hop, inPort, outPort, priority));

		return flows;
	}

	private List<Flow> createBeginBridgeFlows(IBridgeOperator bridge, int chainId, int hop, IPortOperator inPort, IPortOperator outPort, int priority) {
		List<Flow> flows = new LinkedList<Flow>();

		flows.add(this.createHopRewriteFlow(bridge, chainId, hop, inPort, outPort, priority));

		return flows;
	}

	private List<Flow> createEndBridgeFlows(IBridgeOperator bridge, int chainId, int hop, IPortOperator inPort, IPortOperator outPort, int priority) {
		List<Flow> flows = new LinkedList<Flow>();

		flows.add(this.createEndRewriteFlow(bridge, chainId, hop, inPort, outPort, priority));

		return flows;
	}

	private MacAddress getVirtualMac(int chainId, int hop) {
		String chainIdHex = Integer.toHexString(chainId);
		String hopHex = Integer.toHexString(hop);
		return new MacAddress(((chainIdHex.length() == 2) ? chainIdHex : "0" + chainIdHex) +
			":" + ((hopHex.length() == 2) ? hopHex : "0" + hopHex) +
			":ff:ff:ff:ff");
	}

	private Flow createChainForwardFlow(IBridgeOperator bridge, int chainId, int hop, IPortOperator inPort, IPortOperator outPort, int priority) {

		NodeConnectorId ncidIn = new NodeConnectorId("openflow:" + Long.parseLong(bridge.getDatapathId().replace(":", ""), 16) + ":" + outPort.getOfport());
		MatchBuilder matchBuilder = new MatchBuilder();
		EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
        ethDestinationBuilder.setAddress(this.getVirtualMac(chainId, hop));
        ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

		matchBuilder.setInPort(ncidIn);

		// Prepare Instruction
		InstructionsBuilder isb = new InstructionsBuilder();
		List<Instruction> instructions = new LinkedList<Instruction>();
		InstructionBuilder ib = new InstructionBuilder();
		ApplyActionsBuilder aab = new ApplyActionsBuilder();
		ActionBuilder ab = new ActionBuilder();
		List<Action> actionList = new LinkedList<Action>();

		// Output Action
		OutputActionBuilder output = new OutputActionBuilder();

		NodeConnectorId ncidOut = new NodeConnectorId("openflow:" + Long.parseLong(bridge.getDatapathId().replace(":", ""), 16) + ":" + outPort.getOfport());
		output.setOutputNodeConnector(ncidOut);

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
		String flowId = "ServiceChainForward_" + chainId + "_" + hop + "_" + bridge.getDatapathId();
		flowBuilder.setId(new FlowId(flowId));
		FlowKey key = new FlowKey(new FlowId(flowId));

		flowBuilder.setBarrier(true);
		flowBuilder.setTableId((short)0);
		flowBuilder.setKey(key);
		flowBuilder.setPriority(priority);
		flowBuilder.setFlowName(flowId);
		flowBuilder.setHardTimeout(0);
		flowBuilder.setIdleTimeout(0);

		return flowBuilder.setInstructions(isb.setInstruction(instructions).build()).build();
	}

	private Flow createHopRewriteFlow(IBridgeOperator bridge, int chainId, int hop, IPortOperator inPort, IPortOperator outPort, int priority) {

		NodeConnectorId ncidIn = new NodeConnectorId("openflow:" + Long.parseLong(bridge.getDatapathId().replace(":", ""), 16) + ":" + outPort.getOfport());
		MatchBuilder matchBuilder = new MatchBuilder();
		EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
		EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
		ethDestinationBuilder.setAddress(this.getVirtualMac(chainId, hop - 1));
		ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
		matchBuilder.setEthernetMatch(ethernetMatch.build());

		matchBuilder.setInPort(ncidIn);

		// Prepare Instruction
		InstructionsBuilder isb = new InstructionsBuilder();
		List<Instruction> instructions = new LinkedList<Instruction>();
		InstructionBuilder ib = new InstructionBuilder();
		ApplyActionsBuilder aab = new ApplyActionsBuilder();
		ActionBuilder abOutput = new ActionBuilder();
		ActionBuilder abRewrite = new ActionBuilder();
		List<Action> actionList = new LinkedList<Action>();

		// Rewrite Action
		SetDlDstActionBuilder rewrite = new SetDlDstActionBuilder();
		rewrite.setAddress(this.getVirtualMac(chainId, hop));

		abRewrite.setAction(new SetDlDstActionCaseBuilder().setSetDlDstAction(rewrite.build()).build());
		abRewrite.setOrder(0);
		abRewrite.setKey(new ActionKey(0));
		actionList.add(abRewrite.build());

		// Output Action
		OutputActionBuilder output = new OutputActionBuilder();

		NodeConnectorId ncidOut = new NodeConnectorId("openflow:" + Long.parseLong(bridge.getDatapathId().replace(":", ""), 16) + ":" + outPort.getOfport());
		output.setOutputNodeConnector(ncidOut);

		output.setMaxLength(65535);

		abOutput.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
		abOutput.setOrder(1);
		abOutput.setKey(new ActionKey(1));
		actionList.add(abOutput.build());

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
		String flowId = "ServiceChainRewrite_" + chainId + "_" + hop + "_" + bridge.getDatapathId();
		flowBuilder.setId(new FlowId(flowId));
		FlowKey key = new FlowKey(new FlowId(flowId));

		flowBuilder.setBarrier(true);
		flowBuilder.setTableId((short)0);
		flowBuilder.setKey(key);
		flowBuilder.setPriority(priority);
		flowBuilder.setFlowName(flowId);
		flowBuilder.setHardTimeout(0);
		flowBuilder.setIdleTimeout(0);

		return flowBuilder.setInstructions(isb.setInstruction(instructions).build()).build();
	}

	private Flow createEndRewriteFlow(IBridgeOperator bridge, int chainId, int hop, IPortOperator inPort, IPortOperator outPort, int priority) {

		NodeConnectorId ncidIn = new NodeConnectorId("openflow:" + Long.parseLong(bridge.getDatapathId().replace(":", ""), 16) + ":" + outPort.getOfport());
		MatchBuilder matchBuilder = new MatchBuilder();
		EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
		EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
		ethDestinationBuilder.setAddress(this.getVirtualMac(chainId, hop - 1));
		ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
		matchBuilder.setEthernetMatch(ethernetMatch.build());

		matchBuilder.setInPort(ncidIn);

		// Prepare Instruction
		InstructionsBuilder isb = new InstructionsBuilder();
		List<Instruction> instructions = new LinkedList<Instruction>();
		InstructionBuilder ib = new InstructionBuilder();
		ApplyActionsBuilder aab = new ApplyActionsBuilder();
		ActionBuilder abOutput = new ActionBuilder();
		ActionBuilder abRewrite = new ActionBuilder();
		List<Action> actionList = new LinkedList<Action>();

		// Rewrite Action
		SetDlDstActionBuilder rewrite = new SetDlDstActionBuilder();
		IHostPort hostPort = (IHostPort)outPort;
		rewrite.setAddress(new MacAddress(hostPort.getMacAddress()));

		abRewrite.setAction(new SetDlDstActionCaseBuilder().setSetDlDstAction(rewrite.build()).build());
		abRewrite.setOrder(0);
		abRewrite.setKey(new ActionKey(0));
		actionList.add(abRewrite.build());

		// Output Action
		OutputActionBuilder output = new OutputActionBuilder();

		NodeConnectorId ncidOut = new NodeConnectorId("openflow:" + Long.parseLong(bridge.getDatapathId().replace(":", ""), 16) + ":" + outPort.getOfport());
		output.setOutputNodeConnector(ncidOut);

		output.setMaxLength(65535);

		abOutput.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
		abOutput.setOrder(1);
		abOutput.setKey(new ActionKey(1));
		actionList.add(abOutput.build());

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
		String flowId = "ServiceChainEndRewrite_" + chainId + "_" + hop + "_" + bridge.getDatapathId();
		flowBuilder.setId(new FlowId(flowId));
		FlowKey key = new FlowKey(new FlowId(flowId));

		flowBuilder.setBarrier(true);
		flowBuilder.setTableId((short)0);
		flowBuilder.setKey(key);
		flowBuilder.setPriority(priority);
		flowBuilder.setFlowName(flowId);
		flowBuilder.setHardTimeout(0);
		flowBuilder.setIdleTimeout(0);

		return flowBuilder.setInstructions(isb.setInstruction(instructions).build()).build();
	}
}