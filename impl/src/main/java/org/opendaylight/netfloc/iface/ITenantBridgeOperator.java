/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netfloc.iface;

import java.util.List;


// no idea what this class is for tbh
public interface ITenantBridgeOperator extends ITenantOwner {
	public IBridgeOperator getBridge();

	public void addPort(IPortOperator port);
	public void removePort(IPortOperator port);
	public List<IPortOperator> getPorts();

	public List<IInternalPort> getInternalPorts();
	public List<IHostPort> getHostPorts();
	public List<ILinkPort> getLinkPorts();
	public void addInternalPort(IInternalPort internalPort);
	public void addHostPort(IHostPort hostPort);
	public void addLinkPort(ILinkPort linkPort);
	public void removeInternalPort(IInternalPort internalPort);
	public void removeHostPort(IHostPort hostPort);
	public void removeLinkPort(ILinkPort linkPort);
}