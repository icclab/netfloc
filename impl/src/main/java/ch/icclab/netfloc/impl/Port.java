/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.IPortOperator;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ovsdb.rev150105.OvsdbTerminationPointAugmentation;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.node.TerminationPoint;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;

import org.opendaylight.yangtools.yang.binding.util.BindingReflections;

import java.util.Map;
import java.lang.IllegalStateException;
import java.lang.IllegalArgumentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// idk
public class Port implements IPortOperator {

	private static final Logger LOG = LoggerFactory.getLogger(Port.class);

	private IBridgeOperator bridge;
	private TerminationPoint tp;
	private OvsdbTerminationPointAugmentation ovsdbTerminationPointAugmentation;
	
	// inheritance crap
	public Port() {}

	public Port(IBridgeOperator bridge, TerminationPoint tp, OvsdbTerminationPointAugmentation ovsdbTerminationPointAugmentation) {
		if (tp == null) {
			throw new IllegalArgumentException("tp is null");
		}
		if (bridge == null) {
			throw new IllegalArgumentException("bridge is null");
		}
		if (ovsdbTerminationPointAugmentation == null) {
			throw new IllegalArgumentException("ovsdbTerminationPointAugmentation is null");
		}
		this.bridge = bridge;
		this.tp = tp;
		this.ovsdbTerminationPointAugmentation = ovsdbTerminationPointAugmentation;
	}

	public IBridgeOperator getBridge() {
		return this.bridge;
	}

	public Uuid getPortUuid() {
		return this.ovsdbTerminationPointAugmentation.getPortUuid();
	}

	public void update(TerminationPoint tp, OvsdbTerminationPointAugmentation changes) {
		if (ovsdbTerminationPointAugmentation == null) {
			throw new IllegalArgumentException("ovsdbTerminationPointAugmentation is null");
		}
		this.ovsdbTerminationPointAugmentation = changes;
		if (tp == null) {
			return;
		}
		this.tp = tp;
	}

	public java.lang.Long getOfport() {
		return this.ovsdbTerminationPointAugmentation.getOfport();
	}

	public void relateSouthbound(IBridgeOperator bridge, TerminationPoint tp, OvsdbTerminationPointAugmentation ovsdbTerminationPointAugmentation) {
		if (tp == null) {
			throw new IllegalArgumentException("tp is null");
		}
		if (bridge == null) {
			throw new IllegalArgumentException("bridge is null");
		}
		if (ovsdbTerminationPointAugmentation == null) {
			throw new IllegalArgumentException("ovsdbTerminationPointAugmentation is null");
		}
		this.bridge = bridge;
		this.tp = tp;
		this.ovsdbTerminationPointAugmentation = ovsdbTerminationPointAugmentation;
	}

	public boolean isOvsPort(TpId tpid) {
		if (tpid == null) {
			throw new IllegalArgumentException("tpid is null");
		}

		LOG.info("compare OF TpId <{}>, to gen. <{}>", tpid.getValue(), this.getOFTpIdValue());
		return tpid.getValue().equals(this.getOFTpIdValue());
	}

	public String getOFTpIdValue() {
		return "openflow:" + Long.parseLong(this.bridge.getDatapathId().replace(":", ""), 16) + ":" + this.getOfport();
	}
}