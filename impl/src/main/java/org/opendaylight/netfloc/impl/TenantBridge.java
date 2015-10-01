/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import java.util.List;

import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.ILinkPort;
import ch.icclab.netfloc.iface.IInternalPort;
import ch.icclab.netfloc.iface.IPortOperator;
import ch.icclab.netfloc.iface.ITenantBridgeOperator;
import ch.icclab.netfloc.iface.IHostPort;

import java.util.LinkedList;

// no idea what this class is for tbh
public class TenantBridge implements ITenantBridgeOperator {
	private IBridgeOperator bridge;
	private Tenant tenant;

	public TenantBridge(Tenant tenant, IBridgeOperator bridge) {
		this.tenant = tenant;
		this.bridge = bridge;
	}

	public IBridgeOperator getBridge() {
		return this.bridge;
	}
	
	public Tenant getTenant() {
		return this.tenant;
	}

	public void addPort(IPortOperator port) {
		if (port instanceof IInternalPort) {
			this.addInternalPort((IInternalPort)port);
			return;
		}
		if (port instanceof IHostPort) {
			this.addHostPort((IHostPort)port);
			return;
		}
		if (port instanceof ILinkPort) {
			this.addLinkPort((ILinkPort)port);
			return;
		}
	}

	public void removePort(IPortOperator port) {
		if (port instanceof IInternalPort) {
			this.removeInternalPort((IInternalPort)port);
			return;
		}
		if (port instanceof IHostPort) {
			this.removeHostPort((IHostPort)port);
			return;
		}
		if (port instanceof ILinkPort) {
			this.removeLinkPort((ILinkPort)port);
			return;
		}
	}

	public List<IPortOperator> getPorts() {
		List<IPortOperator> ports = new LinkedList<IPortOperator>();
		for (IPortOperator port : this.getInternalPorts()) {
			ports.add(port);
		}
		for (IPortOperator port : this.getHostPorts()) {
			ports.add(port);
		}
		for (IPortOperator port : this.getLinkPorts()) {
			ports.add(port);
		}
		return ports;
	}

	public List<IInternalPort> getInternalPorts() {
		return Tenant.filterByTenant(this.bridge.getInternalPorts(), this.getTenant());
	}

	public List<IHostPort> getHostPorts() {
		return Tenant.filterByTenant(this.bridge.getHostPorts(), this.getTenant());
	}

	public List<ILinkPort> getLinkPorts() {
		return Tenant.filterByTenant(this.bridge.getLinkPorts(), this.getTenant());
	}

	public void addInternalPort(IInternalPort internalPort) {
		this.bridge.addInternalPort(internalPort);
	}
	
	public void addHostPort(IHostPort hostPort) {
		this.bridge.addHostPort(hostPort);
	}

	public void addLinkPort(ILinkPort linkPort) {
		this.bridge.addLinkPort(linkPort);
	}
	
	public void removeInternalPort(IInternalPort internalPort) {
		this.bridge.removeInternalPort(internalPort);
	}

	public void removeHostPort(IHostPort hostPort) {
		this.bridge.removeHostPort(hostPort);
	}

	public void removeLinkPort(ILinkPort linkPort) {
		this.bridge.removeLinkPort(linkPort);
	}

	
}