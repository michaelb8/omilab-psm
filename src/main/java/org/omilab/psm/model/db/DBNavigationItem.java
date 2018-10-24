// File:         DBNavigationItem.java
// Created:      04.03.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
public final class DBNavigationItem {

	@Column
	private Date created;

	@Column(nullable = false, unique = true)
	private String endpoint;

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true, nullable = false)
	private String name;

	@Column(nullable = false)
	private Boolean mandatory;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "dbnavigationitem", cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<Project_Navigation> project_navigation;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "dbnavigationitem")
	@JsonIgnore
	private Set<WizardConfigurationEntry> wizard_config;

	@Column(nullable = false)
	private int seq;

	@ManyToOne
	@JsonIgnore
	private ServiceDefinition servicedefinition;

	@ManyToMany(mappedBy = "items")
	@JsonIgnore
	private Set<ProjectType> types;

	@Column
	private Date updated;

	public DBNavigationItem(String name, String endpoint, Boolean mandatory, int seq,
							ServiceDefinition servicedefinition) {
		this.name = name;
		this.endpoint = endpoint;
		this.mandatory = mandatory;
		this.seq = seq;
		this.servicedefinition = servicedefinition;
	}

	public DBNavigationItem() {
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public ServiceDefinition getServicedefinition() {
		return servicedefinition;
	}

	public void setServicedefinition(ServiceDefinition servicedefinition) {
		this.servicedefinition = servicedefinition;
	}

	public Set<ProjectType> getTypes() {
		return types;
	}

	private void setTypes(final Set<ProjectType> types) {
		this.types = types;
	}

	public Set<Project_Navigation> getProject_navigation() {
		return project_navigation;
	}

	public void setProject_navigation(final Set<Project_Navigation> project_navigation) {
		this.project_navigation = project_navigation;
	}

	public Set<WizardConfigurationEntry> getWizard_config() {
		return wizard_config;
	}

	public void setWizard_config(final Set<WizardConfigurationEntry> wizard_config) {
		this.wizard_config = wizard_config;
	}

	@PrePersist
	protected void onCreate() {
		created = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		updated = new Date();
	}

}
