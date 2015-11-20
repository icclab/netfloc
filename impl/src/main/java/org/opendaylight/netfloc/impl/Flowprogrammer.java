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

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;

	//org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.CheckedFuture;

public class Flowprogrammer implements IFlowprogrammer {
	static final Logger logger = LoggerFactory.getLogger(Flowprogrammer.class);
	private DataBroker dataBroker;

	public Flowprogrammer(DataBroker dataBroker) {
		this.dataBroker = dataBroker;
	}

	public void programFlow(Flow flow, IBridgeOperator bridge) {
		WriteTransaction wt = this.dataBroker.newWriteOnlyTransaction();
		InstanceIdentifier<Flow> flowId = buildFlowId(flow, bridge.getDatapathId());
		wt.merge(LogicalDatastoreType.CONFIGURATION, flowId, flow, true);
        commitWriteTransaction(wt, flow);
	}

	public void deleteFlow(Flow flow, IBridgeOperator bridge) {
		WriteTransaction wt = this.dataBroker.newWriteOnlyTransaction();
		InstanceIdentifier<Flow> flowId = buildFlowId(flow, bridge.getDatapathId());
		wt.delete(LogicalDatastoreType.CONFIGURATION, flowId);
		commitWriteTransaction(wt, null);
	}

	private void commitWriteTransaction(WriteTransaction wt, Flow flow) {
		CheckedFuture<Void, TransactionCommitFailedException> commitFuture = wt.submit();
        try {
            commitFuture.checkedGet();
            logger.debug("Transaction success for object {}", flow);
        } catch (Exception e) {
            logger.error("Transaction failed with error {} of object {}", e.getMessage(), flow);
            wt.cancel();
        }
	}

	private InstanceIdentifier<Node> buildNodeId(Node node) {
		return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, node.getKey()).build();
	}

	private InstanceIdentifier<Flow> buildFlowId(Flow flow, String datapathId) {
		return InstanceIdentifier.builder(Nodes.class)
			.child(Node.class, new NodeKey(buildNode("openflow:" + datapathId).getKey()))
			.augmentation(FlowCapableNode.class)
			.child(Table.class, new TableKey(flow.getTableId()))
			.child(Flow.class, flow.getKey())
			.build();
	}

	private Node buildNode(String nodeId) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeId));
        builder.setKey(new NodeKey(builder.getId()));
        return builder.build();
    }
}