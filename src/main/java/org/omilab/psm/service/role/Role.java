// File:         Role.java
// Created:      23.04.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service.role;

public final class Role {

	private long id;

	private String role;

	private String display;

	public Role() {
	}

	public Role(String role) {
		this.role = role;
	}

	public long getId() {
		return id;
	}

	public String getRole() {
		return role;
	}

	public String getDisplay() {
		return display;
	}

	private void setDisplay(final String display) {
		this.display = display;
	}
}
