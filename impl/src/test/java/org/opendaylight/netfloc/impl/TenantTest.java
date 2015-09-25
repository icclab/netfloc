/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netfloc.impl;

import java.util.List;
import java.util.LinkedList;

import org.junit.Test;
import org.opendaylight.netfloc.iface.ITenantOwner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TenantTest {

	@Test
	public void testFilterByTenant() {
		List<ITenantOwner> tenantOwners = new LinkedList<ITenantOwner>();

		final Tenant wrongTenant = new Tenant("wrong");
		final Tenant rightTenant = new Tenant("right");

		ITenantOwner wrongTenantOwner = new ITenantOwner() {
			public Tenant getTenant() {
				return wrongTenant;
			}
		};

		ITenantOwner rightTenantOwner = new ITenantOwner() {
			public Tenant getTenant() {
				return rightTenant;
			}
		};

		tenantOwners.add(wrongTenantOwner);
		tenantOwners.add(rightTenantOwner);

		List<ITenantOwner> filtered = Tenant.filterByTenant(tenantOwners, rightTenant);

		assertTrue(filtered.size() == 1);
		assertTrue(filtered.get(0).equals(rightTenantOwner));
		assertTrue(filtered.get(0).getTenant().equals(rightTenant));
	}
}