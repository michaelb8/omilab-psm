// File:         Keyword.java
// Created:      18.02.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
public final class Keyword {

	@Column(unique = true)
	private String content;

	@Column
	private Date created;

	@Id
	@GeneratedValue
	private Long id;

	@ManyToMany(mappedBy = "keywords")
	@JsonIgnore
	private Set<AbstractProject> projects;

	@Column
	private Date updated;

	public Keyword() {
	}

	public Keyword(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	private void setContent(String content) {
		this.content = content;
	}

	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	public Set<AbstractProject> getProjects() {
		return projects;
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
