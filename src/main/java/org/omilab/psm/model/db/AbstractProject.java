// File:         AbstractProject.java
// Created:      13.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.imageio.ImageIO;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.List;

@Entity
@Indexed
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class AbstractProject {

	@Column
	private Date created;

	@Column
	private String backgroundColor;

	@Column
	private Boolean inConfig;

	@OneToOne(cascade = CascadeType.ALL)
	@JsonIgnore
	private ProjectProposal proposal;

	@Id
	@GeneratedValue
	private Long id;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	@JsonIgnore
	private Set<ServiceInstance> instances;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "PROJECT_KEYWORD")
	private Set<Keyword> keywords;

	@Field
	@Size(min = 4, max = 140)
	@Column(unique = true, nullable = false)
	private String name;

	@Field
	@Size(min = 2, max = 8)
	@Column(unique = true, nullable = false)
	private String abbreviation;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = CascadeType.ALL)
	@JsonIgnore
	private Set<Project_Navigation> project_navigation;

	@Column
	private Date updated;

	@Field
	@Size(min = 2, max = 25)
	@Column(unique = true, nullable = false)
	private String urlidentifier;

	@ManyToOne
	@JoinColumn
	@JsonIgnore
	private ProjectType projecttype;

	@OneToMany( mappedBy = "project", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<MainNavigationItemProject> navigation;

	@Field
	@Column
	private String uniqueID;

	public AbstractProject(String name, String abbreviation, String urlidentifier, ProjectProposal proposal) {
		this.name = name;
		this.abbreviation = abbreviation;
		this.urlidentifier = urlidentifier;
		this.uniqueID = UUID.randomUUID().toString();
		this.proposal = proposal;
		this.inConfig = true;
	}

	public AbstractProject() {
		this.inConfig = true;
		this.uniqueID = UUID.randomUUID().toString();
	}

	public void addKeyword(Keyword keyword) {
		if(this.keywords == null) {
			this.keywords = new HashSet<>();
		}
		this.keywords.add(keyword);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<ServiceInstance> getInstances() {
		return instances;
	}

	public Set<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<Keyword> keywords) {
		this.keywords = keywords;
	}

	public String getUrlidentifier() {
		return urlidentifier;
	}

	public void setUrlidentifier(String urlidentifier) {
		this.urlidentifier = urlidentifier;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	@PrePersist
	protected void onCreate() {
		created = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		updated = new Date();
	}

	public void removeKeyword(Keyword keyword) {
		if(this.keywords != null) {
			keywords.remove(keyword);
		}
	}

	public void removeAllKeywords() {
		if(this.keywords != null) {
			keywords.clear();
		}
	}

	public void update(AbstractProject project) {
		this.setName(project.getName());

		this.setAbbreviation(project.getAbbreviation());
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public ProjectType getProjecttype() {
		return projecttype;
	}

	public void setProjecttype(final ProjectType projecttype) {
		this.projecttype = projecttype;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(final String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public List<MainNavigationItemProject> getNavigation() {
		return navigation;
	}

	public void setNavigation(final List<MainNavigationItemProject> navigation) {
		this.navigation = navigation;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public ProjectProposal getProposal() {
		return proposal;
	}

	public void setProposal(final ProjectProposal proposal) {
		this.proposal = proposal;
	}

	public Boolean getInConfig() {
		return inConfig;
	}

	public void setInConfig(final Boolean inConfig) {
		this.inConfig = inConfig;
	}
}
