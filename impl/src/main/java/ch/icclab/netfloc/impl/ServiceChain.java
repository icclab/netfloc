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
import ch.icclab.netfloc.iface.INetworkPath;
import ch.icclab.netfloc.iface.IServiceChain;
import ch.icclab.netfloc.iface.IHostPort;

import java.util.LinkedList;

public class ServiceChain implements IServiceChain {

	private List<INetworkPath> paths;
	private int chainId;

	public ServiceChain(List<INetworkPath> paths, int chainId) {
		this.paths = paths;
		this.chainId = chainId;
	}

	public int getChainId() {
		return this.chainId;
	}

	public INetworkPath getBegin() {
		return paths.get(0);
	}
	
	public INetworkPath getEnd() {
		return paths.get(paths.size()-1);
	}

	public INetworkPath getPrevious(INetworkPath np) {
		int index = paths.lastIndexOf(np);
		if (index > 0) {
			return paths.get(index - 1);
		}
		return null;
	}

	public INetworkPath getNext(INetworkPath np) {
		int index = paths.lastIndexOf(np);
		if (index < paths.size() - 1) {
			return paths.get(index + 1);
		}
		return null;
	}

	public void append(INetworkPath np) {
		this.paths.add(np);
	}

	public void addPaths(List<INetworkPath> nps) {
		this.paths.addAll(nps);
	}

	public boolean isEqualConnectionChain(IServiceChain sc) {
		return sc.getChainId() == this.getChainId();
	}

}