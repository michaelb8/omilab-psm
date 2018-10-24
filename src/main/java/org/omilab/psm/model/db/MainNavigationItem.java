// File:         MainNavigationItem.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class MainNavigationItem {

	@Id
	@GeneratedValue
	private Long id;

	@Column
	private Date updated;

	@Column
	private Date created;

	@Column(nullable = false)
	private String displayname;

	@Column(nullable = false)
	private int seq;

	@Column(unique = true)
	private String link;

	@Column
	private Boolean visible;

	@Column(nullable = false)
	private Boolean authentication;


	public MainNavigationItem() {
	}

	public MainNavigationItem(final String displayname, final String link) {
		this.displayname = displayname;
		this.link = link;
		this.visible = true;
		this.authentication = false;
	}

	public Long getId() {
		return id;
	}

	private void setId(final Long id) {
		this.id = id;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(final String displayname) {
		this.displayname = displayname;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(final int seq) {
		this.seq = seq;
	}

	public String getLink() {
		return link;
	}

	public void setLink(final String link) {
		this.link = link;
	}

	public Boolean isVisible() {
		return visible;
	}

	public void makeVisible() {
		setVisible(true);
	}

	public void makeInVisible() {
		setVisible(false);
	}

	public void setVisible(final Boolean visible) {
		this.visible = visible;
	}

	@PrePersist
	protected void onCreate() {
		created = new Date();
	}

	@PreUpdate
	protected void onUpdate() {
		updated = new Date();
	}

	public Boolean getAuthentication() {
		return authentication;
	}

	public void setAuthentication(final Boolean authentication) {
		this.authentication = authentication;
	}

	@Override
	public String toString() {
		return "MainNavigationItem{" +
				"id=" + id +
				", updated=" + updated +
				", created=" + created +
				", displayname='" + displayname + '\'' +
				", seq=" + seq +
				'}';
	}
}