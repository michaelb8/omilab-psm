// File:         WizardConfigurationEntry.java
// Created:      01.02.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import javax.persistence.*;

@Entity
public class WizardConfigurationEntry {

	@Id
	@GeneratedValue
	@Column
	private Long id;

	@ManyToOne
	private DBNavigationItem dbnavigationitem;

	@ManyToOne
	private ProjectType type;

	@Column(columnDefinition = "TEXT")
	private String instantiation;

	@Column
	private int seq;

	public WizardConfigurationEntry(final DBNavigationItem dbnavigationitem, final ProjectType type) {
		this.dbnavigationitem = dbnavigationitem;
		this.type = type;
		this.instantiation = null;
	}

	public WizardConfigurationEntry() {
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public DBNavigationItem getDbnavigationitem() {
		return dbnavigationitem;
	}

	public void setDbnavigationitem(final DBNavigationItem dbnavigationitem) {
		this.dbnavigationitem = dbnavigationitem;
	}

	public ProjectType getType() {
		return type;
	}

	public void setType(final ProjectType type) {
		this.type = type;
	}

	public String getInstantiation() {
		return instantiation;
	}

	public void setInstantiation(final String instantiation) {
		this.instantiation = instantiation;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(final int seq) {
		this.seq = seq;
	}
}
