// File:         MainNavigationItemLink.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("PROJECT")
public final class MainNavigationItemProject extends MainNavigationItem {

	@ManyToOne
	@JsonIgnore
	private AbstractProject project;

	public MainNavigationItemProject(final String displayname, final String link, final AbstractProject project) {
		super(displayname, link);
		this.project = project;
	}

	public MainNavigationItemProject() {
	}

	public AbstractProject getProject() {
		return project;
	}

	public void setProject(final AbstractProject project) {
		this.project = project;
	}
}
