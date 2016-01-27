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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetflocServiceImpl implements NetflocService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(NetflocServiceImpl.class);
	private final ExecutorService executor;
    private List<IServiceChainListener> serviceChainListeners = new LinkedList<IServiceChainListener>();
    private NetworkGraph graph = new NetworkGraph();
    private List<IHostPort> chainPorts = new LinkedList<IHostPort>();
    private INetworkPath networkPath;
    private List<INetworkPath> chainNetworkPaths = new LinkedList<INetworkPath>();
    private int chainID = 0;
    private Future<RpcResult<CreateServiceChainOutput>> result;


	public NetflocServiceImpl() {
		executor = Executors.newFixedThreadPool(1);
	}

	public void close() {
		executor.shutdown();
	}

    public void registerServiceChainListener(IServiceChainListener nsl) {
        this.serviceChainListeners.add(nsl);
    }
	/*
    * RestConf RPC call implemented from the NetflocService interface. Creates a service chain.
    * In Postman: to get the current SFC config: 
    * GET http://localhost:8181/restconf/config/netfloc:netfloc [not yet implemented]
    * To create new service chain:
    * POST http://localhost:8181/restconf/operations/netfloc:create-service-chain
    * { "input" : { "neutron-ports" : ["2a6f9ea7-dd2f-4ce1-8030-d999856fb558","5ec846bb-faf3-4f4e-83c1-fe253ff75ccb", ...] } 
    * { "output: {"chainID"} }
    */

	@Override
    public Future<RpcResult<CreateServiceChainOutput>> createServiceChain(CreateServiceChainInput input) {
        // get the host ports based on neutron port id from the graph.getHostPorts(...)
        LOG.info("createServiceChain: {}", input);
        for (String portID : input.getNeutronPorts()) {
            for (IHostPort port : graph.getHostPorts()) {
                if (portID.equals(port.getNeutronUuid())) {
                    chainPorts.add(port);
                    break;
                }
            }
        }
        LOG.info("NetflocServiceImpl chainNetworkPorts: {}", chainPorts);
        int lastIndex = chainPorts.size() - 1;
        for (IHostPort port : chainPorts) {
            if (chainPorts.lastIndexOf(port) == lastIndex) {
                // create path between *every* hostport A -> B or B -> C, etc.
                networkPath = graph.getNetworkPath(port, chainPorts.get(chainPorts.lastIndexOf(port) + 1));
                // add each network path in list
                chainNetworkPaths.add(networkPath);
                LOG.info("NetflocServiceImpl chainNetworkPaths: {}", chainNetworkPaths);
            }
        }
        // instantiate ServiceChain
        chainID = chainID+1;
        ServiceChain chainInstance = new ServiceChain(chainNetworkPaths, chainID);
        //result = new ServiceChain(chainNetworkPaths, chainID);
        LOG.info("chainID: {}", chainID);

        for (IServiceChainListener scl : this.serviceChainListeners) {
            scl.serviceChainCreated(chainInstance);
        }
        //return chainID;
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