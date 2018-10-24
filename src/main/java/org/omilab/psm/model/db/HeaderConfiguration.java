// File:         HeaderConfiguration.java
// Created:      14.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public final class HeaderConfiguration {

	@Id
	@GeneratedValue
	private long id;

	@Column(nullable = false)
	private int seq;

	@Column
	private Boolean starter;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column
	private Boolean visible;

	@Column
	private String title;

	@Column
	private String type;

	@Column
	private Date updated;

	@Column
	private Date created;

	public HeaderConfiguration() {
	}

	public HeaderConfiguration(String content, String title, String type, Boolean visibility) {
		this.content = content;
		this.title = title;
		this.type = type;
		this.visible = visibility;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public Boolean getStarter() {
		return starter;
	}

	private void setStarter(Boolean starter) {
		this.starter = starter;
	}

	public void disableStarter() {
		setStarter(false);
	}

	public void makeStarter() {
		setStarter(true);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean isVisible() {
		return visible;
	}

	private void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public void makeVisible() {
		setVisible(true);
	}

	public void makeInVisible() {
		setVisible(false);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
