// File:         ServiceDefinition.java
// Created:      13.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(value = {"servicenavigationitems"})
public final class ServiceDefinition {

	@Column(nullable = true, length = 600)
	private String description;

	@Column
	private String developer;

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true, nullable = false)
	private String name;

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "servicedefinition")
	private Set<ServiceInstance> serviceinstance;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "servicedefinition")
	private Set<DBNavigationItem> servicenavigationitems;

	@Column(nullable = false)
	private String url;

	@Column(nullable = false)
	private String urlidentifier;

	@Column(nullable = false)
	private Boolean visible;

	@Column(nullable = true,unique = true)
	private String special;

	@Column
	private Date created;

	public ServiceDefinition(final String url, final String name) {
		this.url = url;
		this.name = name;
	}

	protected ServiceDefinition() {


	}

	public String getDescription() {
		return description;
	}

	private void setDescription(String description) {
		this.description = description;
	}

	public String getDeveloper() {
		return developer;
	}

	private void setDeveloper(String developer) {
		this.developer = developer;
	}

	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public Set<ServiceInstance> getServiceinstance() {
		return serviceinstance;
	}

	private void setServiceinstance(Set<ServiceInstance> serviceinstance) {
		this.serviceinstance = serviceinstance;
	}

	public Set<DBNavigationItem> getServicenavigationitems() {
		return servicenavigationitems;
	}

	public String getUrl() {
		return url;
	}

	private void setUrl(String url) {
		this.url = url;
	}

	public String getUrlidentifier() {
		return urlidentifier;
	}

	private void setUrlidentifier(String urlidentifier) {
		this.urlidentifier = urlidentifier;
	}

	public Boolean getVisible() {
		return visible;
	}

	private void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public String getSpecial() {
		return special;
	}

	private void setSpecial(final String special) {
		this.special = special;
	}

	public void makeInvisible() {
		this.setVisible(false);
	}

	public void makeVisible() {
		this.setVisible(true);
	}

	@PrePersist
	protected void onCreate() {
		created = new Date();
	}

}
