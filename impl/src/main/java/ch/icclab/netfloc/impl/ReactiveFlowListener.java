/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package ch.icclab.netfloc.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;

public class ReactiveFlowListener implements PacketProcessingListener {
	
	@Override
	public void onPacketReceived(PacketReceived notification) {
			byte[] dstMacRaw = Arrays.copyOfRange(notification.getPayload(), 0, 6);
			byte[] srcMacRaw = Arrays.copyOfRange(notification.getPayload(), 6, 12);
			logger.info("packet_in {}, {}", dstMacRaw, srcMacRaw);
	}
}