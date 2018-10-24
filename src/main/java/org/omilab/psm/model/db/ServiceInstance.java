// File:         ServiceInstance.java
// Created:      18.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import javax.persistence.*;
import java.util.Date;

@Entity
public final class ServiceInstance {

	@Column
	private Date created;

	@Id
	@GeneratedValue
	@Column
	private Long instanceidlocal;

	@Column
	private Long instanceidremote;

	@ManyToOne
	private AbstractProject project;

	@ManyToOne
	private ServiceDefinition servicedefinition;

	public ServiceInstance(AbstractProject project, ServiceDefinition service) {
		this.project = project;
		this.servicedefinition = service;
	}

	public ServiceInstance(long instanceidremote, AbstractProject project, ServiceDefinition service) {
		this.instanceidremote = instanceidremote;
		this.project = project;
		this.servicedefinition = service;
	}

	public ServiceInstance() {
	}

	public Long getInstanceidlocal() {
		return instanceidlocal;
	}

	private void setInstanceidlocal(Long instanceidlocal) {
		this.instanceidlocal = instanceidlocal;
	}

	public Long getInstanceidremote() {
		return instanceidremote;
	}

	private void setInstanceidremote(Long instanceidremote) {
		this.instanceidremote = instanceidremote;
	}

	public AbstractProject getProject() {
		return project;
	}

	private void setProject(AbstractProject project) {
		this.project = project;
	}

	public ServiceDefinition getServicedefinition() {
		return servicedefinition;
	}

	private void setServicedefinition(ServiceDefinition servicedefinition) {
		this.servicedefinition = servicedefinition;
	}

	public ServiceDefinition getServiceid() {
		return servicedefinition;
	}

	private void setServiceid(ServiceDefinition service) {
		this.servicedefinition = service;
	}

	@PrePersist
	protected void onCreate() {
		created = new Date();
	}

}
