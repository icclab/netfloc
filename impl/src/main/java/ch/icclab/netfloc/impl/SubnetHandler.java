/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import ch.icclab.netfloc.iface.nbhandlers.INeutronSubnetHandler;

import org.opendaylight.neutron.spi.INeutronSubnetAware;
import org.opendaylight.neutron.spi.NeutronSubnet;

//import com.google.common.base.Preconditions;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;

public class SubnetHandler implements INeutronSubnetAware {

    static final Logger logger = LoggerFactory.getLogger(SubnetHandler.class);

    private INeutronSubnetHandler network;

    // The implementation for each of these services is resolved by the OSGi Service Manager

    public SubnetHandler(INeutronSubnetHandler network) {
        this.network = network;
    }

    @Override
    public int canCreateSubnet(NeutronSubnet subnet) {
        logger.info("can create subnet");
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    public void neutronSubnetCreated(NeutronSubnet subnet) {
        logger.info("neutron subnet created");
        network.neutronSubnetCreated(subnet);
    }

    @Override
    public int canUpdateSubnet(NeutronSubnet delta, NeutronSubnet original) {
        logger.info("can update subnet");
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    public void neutronSubnetUpdated(NeutronSubnet subnet) {
        logger.info("neutron subnet updated");
        network.neutronSubnetUpdated(subnet);
    }

    @Override
    public int canDeleteSubnet(NeutronSubnet subnet) {
        logger.info("can delete subnet");
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    public void neutronSubnetDeleted(NeutronSubnet subnet) {
        logger.info("neutron subnet deleted");
        network.neutronSubnetDeleted(subnet);
    }
}
