// File:         MainNavigationItemLink.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("TYPES")
public final class MainNavigationItemTypes extends MainNavigationItem {

	public MainNavigationItemTypes(final String displayname, final String link, final List<ProjectType> types,
								   final String caption, final String newprojectlabel) {
		super(displayname, link);
		this.types = types;
		this.caption = caption;
		this.newproject = false;
		this.newprojectlabel = newprojectlabel;
	}

	public MainNavigationItemTypes() {
	}

	@Column
	private Boolean newproject;

	@ManyToMany(fetch = FetchType.EAGER)
	private List<ProjectType> types;

	@Column
	private String caption;

	@Column
	private String newprojectlabel;

	public List<ProjectType> getTypes() {
		return types;
	}

	public void setTypes(final List<ProjectType> types) {
		this.types = types;
	}

	public void removeAll(final List<ProjectType> types) {
		this.types.removeAll(types);
	}

	public void remove(final ProjectType type) {
		this.types.remove(type);
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(final String caption) {
		this.caption = caption;
	}

	public Boolean getNewproject() {
		return newproject;
	}

	public void setNewproject(final Boolean newproject) {
		this.newproject = newproject;
	}

	public String getNewprojectlabel() {
		return newprojectlabel;
	}

	public void setNewprojectlabel(String newprojectlabel) {
		this.newprojectlabel = newprojectlabel;
	}
}
