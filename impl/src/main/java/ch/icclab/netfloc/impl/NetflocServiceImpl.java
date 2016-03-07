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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netfloc.rev150105.CreateServiceChainOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netfloc.rev150105.DeleteServiceChainInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

import ch.icclab.netfloc.iface.IBridgeOperator;
import ch.icclab.netfloc.iface.ILinkPort;
import ch.icclab.netfloc.iface.INetworkPath;
import ch.icclab.netfloc.iface.IServiceChain;
import ch.icclab.netfloc.iface.IHostPort;
import ch.icclab.netfloc.iface.IServiceChainListener;

import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.google.common.util.concurrent.Futures;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetflocServiceImpl implements NetflocService, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(NetflocServiceImpl.class);
	private final ExecutorService executor;
    private Map<String,IServiceChain> activeChains = new HashMap<String, IServiceChain>();
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

    private RpcError idNotFoundError() {
        return RpcResultBuilder.newError( ErrorType.APPLICATION, "not-found",
            "Service Chain Id not found", null, null, null );
    }

    private RpcError wrongAmoutOfPortsError() {
        return RpcResultBuilder.newError( ErrorType.APPLICATION, "input-condition",
            "Service Chain Input cannot have an odd number of Neutron Ports", null, null, null );
    }

    private RpcError portNotFoundError(List<String> ports) {
        return RpcResultBuilder.newError( ErrorType.APPLICATION, "graph-state",
            "Did not find all Neutron Ports in the Network Graph " + ports.toString(), null, null, null );
    }

    private RpcError pathNotClosedError() {
        return RpcResultBuilder.newError( ErrorType.APPLICATION, "graph-state",
            "Path is not closed in the Network Graph", null, null, null );
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

        if (Arrays.asList(input.getNeutronPorts().split(",")).size() % 2 != 0) {
            logger.error("Service Chain Input cannot have an odd number of Neutron Ports");
            RpcError error = wrongAmoutOfPortsError();
            return null;
        }

        List<INetworkPath> chainNetworkPaths = new LinkedList<INetworkPath>();
        List<IHostPort> chainPorts = new LinkedList<IHostPort>();
        List<String> portsNotFound = new LinkedList<String>();
        // get the host ports based on neutron port id from the graph.getHostPorts(...)
        logger.info("createServiceChain: {}", input);
        for (String portID : Arrays.asList(input.getNeutronPorts().split(","))) {
        	boolean found = false;
            for (IHostPort port : graph.getHostPorts()) {
                if (portID.equals(port.getNeutronUuid())) {
                    chainPorts.add(port);
                    found = true;
                    break;
                }
            }
            if (!found) {
            	portsNotFound.add(portID);
            }
        }
        logger.info("NetflocServiceImpl chainNetworkPorts: {}", chainPorts);

        if (portsNotFound.size() > 0) {
            RpcError error = portNotFoundError(portsNotFound);
            logger.error("Did not find all Neutron Ports in the Network Graph " + portsNotFound.toString());
            return Futures.immediateFuture( RpcResultBuilder.<CreateServiceChainOutput> failed().withRpcError(error).build() );
        }

        for (int i = 0; i < chainPorts.size(); i = i + 2) {
            INetworkPath path = this.graph.getNetworkPath(chainPorts.get(i), chainPorts.get(i+1));
            if (path == null) {
                logger.error("Path is not closed between {} and {}", chainPorts.get(i), chainPorts.get(i+1));
                return Futures.immediateFuture( RpcResultBuilder.<CreateServiceChainOutput> failed().withRpcError(pathNotClosedError()).build() );
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
        this.activeChains.put("" + chainID, chainInstance);

        //return chainID;
        return Futures.immediateFuture(RpcResultBuilder.<CreateServiceChainOutput> success(new CreateServiceChainOutputBuilder().setServiceChainId("" + chainID).build()).build());
    }

    /**
     * Delete a Service Chain
     *
     */
    @Override
    public Future<RpcResult<java.lang.Void>> deleteServiceChain(DeleteServiceChainInput input) {
        String id = input.getServiceChainId();
        IServiceChain sc = activeChains.get(id);
        if (sc == null) {
            return Futures.immediateFuture( RpcResultBuilder.<Void> failed().withRpcError(idNotFoundError()).build() );
        }

        for (IServiceChainListener scl : this.serviceChainListeners) {
            scl.serviceChainDeleted(sc);
        }

        activeChains.remove(id);

    	return Futures.immediateFuture(RpcResultBuilder.<Void> success().build());
    }
}