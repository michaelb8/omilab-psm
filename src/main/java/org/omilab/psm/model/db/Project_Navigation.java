// File:         Project_Navigation.java
// Created:      16.03.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;


import javax.persistence.*;
import java.util.Date;

@Entity
public final class Project_Navigation {

	@ManyToOne
	private DBNavigationItem dbnavigationitem;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private AbstractProject project;

	@Column
	private Date created;

	public Project_Navigation() {
	}

	public Project_Navigation(AbstractProject project, DBNavigationItem dbnavigationitem) {
		this.project = project;
		this.dbnavigationitem = dbnavigationitem;
	}

	public DBNavigationItem getDbnavigationitem() {
		return dbnavigationitem;
	}

	private void setDbnavigationitem(DBNavigationItem dbnavigationitem) {
		this.dbnavigationitem = dbnavigationitem;
	}

	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	public AbstractProject getProject() {
		return project;
	}

	private void setProject(AbstractProject project) {
		this.project = project;
	}

	@PrePersist
	protected void onCreate() {
		created = new Date();
	}

}
