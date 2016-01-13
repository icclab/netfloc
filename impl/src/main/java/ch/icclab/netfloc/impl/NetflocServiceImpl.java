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
import ch.icclab.netfloc.iface.IServiceChainListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;
import java.util.LinkedList;

public class NetflocServiceImpl implements NetflocService, AutoCloseable {

	private final ExecutorService executor;
    private List<IServiceChainListener> serviceChainListeners = new LinkedList<IServiceChainListener>();
    private NetworkGraph graph = new NetworkGraph();
    private List<IHostPort> chainPorts = new LinkedList<IHostPort>();
    private INetworkPath networkPath;
    private List<INetworkPath> chainNetworkPaths = new LinkedList<INetworkPath>();
    private int chainID = 0;


	public NetflocServiceImpl() {
		executor = Executors.newFixedThreadPool(1);
	}

	public void close() {
		executor.shutdown();
	}

    public void registerServiceChainListener(IServiceChainListener nsl) {
        this.serviceChainListeners.add(nsl);
    }
	/**
     * Create a Service Chain
     *
     */
	@Override
    public Future<RpcResult<CreateServiceChainOutput>> createServiceChain(CreateServiceChainInput input) {
        // get the host ports based on neutron port id from the graph.getHostPorts(...)
        for (String portID : input.getNeutronPorts()) {
            for (IHostPort port : graph.getHostPorts()) {
                if (portID == port.getNeutronUuid()) {
                    chainPorts.add(port);
                    break;
                }
            }
        }
        int lastIndex = chainPorts.size() - 1;
        for (IHostPort port : chainPorts) {
            if (chainPorts.lastIndexOf(port) == lastIndex) {
                // create path between *every* hostport A -> B or B -> C, etc.
                networkPath = graph.getNetworkPath(port, chainPorts.get(chainPorts.lastIndexOf(port) + 1));
                // add each network path in list
                chainNetworkPaths.add(networkPath);
            }
        }
        // instantiate ServiceChain
        chainID = chainID+1;
        ServiceChain chainInstance = new ServiceChain(chainNetworkPaths, chainID);

        for (IServiceChainListener scl : this.serviceChainListeners) {
            scl.serviceChainCreated(chainInstance);
        }
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