// File:         UINavigationItem.java
// Created:      06.03.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.wrapper;

import org.omilab.psm.model.db.DBNavigationItem;

public final class UINavigationItem {

	private Boolean active;

	private String name;

	private Boolean enabled;

	private Long id;

	private Boolean mandatory;

	private String url;

	private String endpoint;

	private String urlidentifier;


	public UINavigationItem() {
	}

	public UINavigationItem(final String name, Boolean active) {
		this.name = name;
		this.enabled = false;
		this.mandatory = false;
		this.active = active;
	}

	public UINavigationItem(String name, String url, Boolean active, Boolean enabled, Boolean mandatory, Long id) {
		this.name = name;
		this.url = url;
		this.active = active;
		this.enabled = enabled;
		this.mandatory = mandatory;
		this.id = id;
	}

	public UINavigationItem(DBNavigationItem dbitem, Boolean active) {
		this.name = dbitem.getName();
		this.url = dbitem.getServicedefinition().getUrlidentifier() + "?view=" + dbitem.getEndpoint();
		this.mandatory = dbitem.getMandatory();
		this.id = dbitem.getId();
		this.active = active;
		this.enabled = true;
		this.endpoint = dbitem.getEndpoint();
		this.urlidentifier = dbitem.getServicedefinition().getUrlidentifier();
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	private void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	private void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getUrl() {
		return url;
	}

	private void setUrl(String url) {
		this.url = url;
	}

	public String getEndpoint() {
		return endpoint;
	}

	private void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getUrlidentifier() {
		return urlidentifier;
	}

	private void setUrlidentifier(String urlidentifier) {
		this.urlidentifier = urlidentifier;
	}
}
