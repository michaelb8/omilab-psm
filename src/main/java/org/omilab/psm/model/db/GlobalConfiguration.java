// File:         GlobalConfiguration.java
// Created:      14.01.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class GlobalConfiguration {

	@Id
	private String id;

	@Column(columnDefinition="MEDIUMTEXT")
	private String value;

	public GlobalConfiguration() {
	}

	public GlobalConfiguration(final String id, final String value) {
		this.id = id;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
}
