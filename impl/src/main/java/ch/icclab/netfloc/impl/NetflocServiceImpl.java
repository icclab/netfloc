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
    private static final Logger logger = LoggerFactory.getLogger(NetflocServiceImpl.class);
	private final ExecutorService executor;
    private List<IServiceChainListener> serviceChainListeners = new LinkedList<IServiceChainListener>();
    private NetworkGraph graph;
    private int chainID = 0;


	public NetflocServiceImpl(NetworkGraph graph) {
        this.graph = graph;
		this.executor = Executors.newFixedThreadPool(1);
	}

	public void close() {
		this.executor.shutdown();
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

        if (input.getNeutronPorts().size() % 2 != 0) {
            logger.error("Service Chain Input cannot have an odd number of Neutron Ports");
            return null;
        }

        List<INetworkPath> chainNetworkPaths = new LinkedList<INetworkPath>();
        List<IHostPort> chainPorts = new LinkedList<IHostPort>();

        // get the host ports based on neutron port id from the graph.getHostPorts(...)
        logger.info("createServiceChain: {}", input);
        for (String portID : input.getNeutronPorts()) {
            for (IHostPort port : graph.getHostPorts()) {
                if (portID.equals(port.getNeutronUuid())) {
                    chainPorts.add(port);
                    break;
                }
            }
        }
        logger.info("NetflocServiceImpl chainNetworkPorts: {}", chainPorts);

        if (chainPorts.size() != input.getNeutronPorts().size()) {
            logger.error("Did not find all Neutron Ports in the Network Graph");
            return null;
        }

        for (int i = 0; i < chainPorts.size(); i = i + 2) {
            INetworkPath path = this.graph.getNetworkPath(chainPorts.get(i), chainPorts.get(i+1));
            if (path == null) {
                logger.error("Path is not closed between {} and {}", chainPorts.get(i), chainPorts.get(i+1));
                return null;
            }
            logger.info("Found path between {} and {}", chainPorts.get(i), chainPorts.get(i+1));
            chainNetworkPaths.add(path);
        }

        // instantiate ServiceChain
        chainID = chainID+1;
        ServiceChain chainInstance = new ServiceChain(chainNetworkPaths, chainID);
        logger.info("chainID: {}", chainID);

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