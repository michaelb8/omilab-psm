// File:         MainNavigationItemHTML.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("HTML")
public final class MainNavigationItemHTML extends MainNavigationItem {

	public MainNavigationItemHTML(final String displayname, final String link) {
		super(displayname, link);
		this.html = "";
		this.carousel = false;
	}

	public MainNavigationItemHTML() {
	}

	@Column(columnDefinition="LONGTEXT")
	private String html;

	@Column
	private Boolean carousel;


	public String getHtml() {
		return html;
	}

	public void setHtml(final String html) {
		this.html = html;
	}

	public Boolean getCarousel() {
		return carousel;
	}

	public void setCarousel(final Boolean carousel) {
		this.carousel = carousel;
	}

}
