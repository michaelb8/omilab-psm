// File:         ProjectType.java
// Created:      29.05.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
public final class ProjectType {

	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String name;

	@Column
	private String description;

	@Column
	private Date created;

	@Column
	private Date updated;

	@Column
	private Boolean fullPHeader;

	@Column
	private Boolean reducedPHeader;

	@Column
	private Boolean navigationBar;

	@Column
	private String overlay;

	@Column
	private Boolean wRepoStatus;

	@Column(columnDefinition = "TEXT")
	private String wRepoString;

	@Column
	private Boolean wRoleStatus;

	@Column(columnDefinition = "TEXT")
	private String wRoleString;

	@Column
	private String backgroundColor;

	@Column(columnDefinition="LONGTEXT")
	private String backgroundTile;

	@Column(columnDefinition="LONGTEXT")
	private String foregroundTile;

	@OneToMany (mappedBy="projecttype",fetch = FetchType.EAGER)
	private List<AbstractProject> projects;

	@OneToMany(mappedBy = "type")
	@JsonIgnore
	private Set<ProjectProposal> proposals;

	@OneToMany(mappedBy = "type",cascade = CascadeType.ALL)
	@JsonIgnore
	private List<WizardConfigurationEntry> wizard_config;

	@ManyToMany
	@JoinTable(name = "PROJECTTYPE_NAVIGATION")
	@JsonIgnore
	private List<DBNavigationItem> items;

	@ManyToMany(mappedBy = "types")
	@JsonIgnore
	private List<MainNavigationItemTypes> page;

	public ProjectType() {
	}

	public ProjectType(final String name, final String description, final String overlay) {
		this.name = name;
		this.description = description;
		this.fullPHeader = true;
		this.reducedPHeader = false;
		this.navigationBar = true;
		this.backgroundTile = "";
		this.foregroundTile = "";
		this.wRepoStatus = false;
		this.wRoleStatus = true;
		this.overlay = overlay;
	}

	public Long getId() {
		return id;
	}

	private void setId(final Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	private void setName(final String name) {
		this.name = name;
	}

	public List<AbstractProject> getProjects() {
		return projects;
	}

	private void setProjects(final List<AbstractProject> projects) {
		this.projects = projects;
	}

	public List<DBNavigationItem> getItems() {
		return items;
	}

	private void setItems(final List<DBNavigationItem> items) {
		this.items = items;
	}

	public void addNavigationItem(DBNavigationItem item) {
		if( items == null)
			items = new ArrayList<>();
		getItems().add(item);
	}

	public void removeNavigationItem(DBNavigationItem item) {
		if( items == null)
			return;
		getItems().remove(item);
	}

	public void removeAllNavigationItem() {
		if( items == null)
			return;
		getItems().clear();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@PrePersist
	protected void onCreate() {
		created = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		updated = new Date();
	}

	public Boolean getFullPHeader() {
		return fullPHeader;
	}

	public void setFullPHeader(final Boolean fullPHeader) {
		this.fullPHeader = fullPHeader;
	}

	public Boolean getReducedPHeader() {
		return reducedPHeader;
	}

	public void setReducedPHeader(final Boolean reducedPHeader) {
		this.reducedPHeader = reducedPHeader;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getBackgroundTile() {
		return backgroundTile;
	}

	public void setBackgroundTile(final String backgroundTile) {
		this.backgroundTile = backgroundTile;
	}

	public Boolean getNavigationBar() {
		return navigationBar;
	}

	public void setNavigationBar(final Boolean navigationBar) {
		this.navigationBar = navigationBar;
	}

	public Set<ProjectProposal> getProposals() {
		return proposals;
	}

	public void setProposals(final Set<ProjectProposal> proposals) {
		this.proposals = proposals;
	}

	public List<WizardConfigurationEntry> getWizard_config() {
		return wizard_config;
	}

	public void setWizard_config(final List<WizardConfigurationEntry> wizard_config) {
		this.wizard_config = wizard_config;
	}

	public Boolean getwRepoStatus() {
		return wRepoStatus;
	}

	public void setwRepoStatus(final Boolean wRepoStatus) {
		this.wRepoStatus = wRepoStatus;
	}

	public Boolean getwRoleStatus() {
		return wRoleStatus;
	}

	public void setwRoleStatus(final Boolean wRoleStatus) {
		this.wRoleStatus = wRoleStatus;
	}

	public String getwRepoString() {
		return wRepoString;
	}

	public void setwRepoString(final String wRepoString) {
		this.wRepoString = wRepoString;
	}

	public String getwRoleString() {
		return wRoleString;
	}

	public void setwRoleString(final String wRoleString) {
		this.wRoleString = wRoleString;
	}

	public String getOverlay() {
		if(overlay == null)
			return "org.omilab.psm.model.db.projectoverlay.MMProject";
		return overlay;
	}

	public void setOverlay(String overlay) {
		this.overlay = overlay;
	}

	public List<MainNavigationItemTypes> getPage() {
		return page;
	}

	public void setPage(List<MainNavigationItemTypes> page) {
		this.page = page;
	}

	public String getForegroundTile() {
		return foregroundTile;
	}

	public void setForegroundTile(String foregroundTile) {
		this.foregroundTile = foregroundTile;
	}
}
