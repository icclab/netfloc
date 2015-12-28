/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netfloc.rev150105.NetflocService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netfloc.rev150105.CreateServiceChainInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netfloc.rev150105.CreateServiceChainOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netfloc.rev150105.DeleteServiceChainInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.ILinkPort;
import ch.icclab.netfloc.iface.INetworkPath;
import ch.icclab.netfloc.iface.IServiceChain;
import ch.icclab.netfloc.iface.IHostPort;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;
import java.util.LinkedList;

public class NetflocServiceImpl implements NetflocService, AutoCloseable {

	private final ExecutorService executor;

	public NetflocServiceImpl() {
		executor = Executors.newFixedThreadPool(1);
	}

	public void close() {
		executor.shutdown();
	}

	/**
     * Create a Service Chain
     *
     */
	@Override
    public Future<RpcResult<CreateServiceChainOutput>> createServiceChain(CreateServiceChainInput input) {
    	return null;
    }

    /**
     * Delete a Service Chain
     *
     */
    @Override
    public Future<RpcResult<java.lang.Void>> deleteServiceChain(DeleteServiceChainInput input) {
    	return null;
    }
}