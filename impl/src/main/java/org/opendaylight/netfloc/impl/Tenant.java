/*
 * Copyright (c) ZHAW and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netfloc.impl;

import java.util.List;

import org.opendaylight.netfloc.iface.ITenantOwner;

import java.util.LinkedList;

public class Tenant {

	private String id;

	public Tenant(String id) {
		this.id = id;
	}

	public String getId() {
		return this.id;
	}

	public static <T> List<T> filterByTenant(List<T> tol, Tenant tenant) {
		List<T> filtered = new LinkedList<T>();
		for (T to : tol) {
			if (!(to instanceof ITenantOwner)) {
				return filtered;
			}
			if (((ITenantOwner)to).getTenant().equals(tenant)) {
				filtered.add(to);
			}
		}
		return filtered;
	}

	public boolean equals(Object o) {
		return o instanceof Tenant && ((Tenant)o).getId().equals(this.getId());
	}
}