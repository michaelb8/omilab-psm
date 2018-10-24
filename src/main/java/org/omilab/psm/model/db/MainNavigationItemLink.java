// File:         MainNavigationItemLink.java
// Created:      13.08.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.db;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("LINK")
public final class MainNavigationItemLink extends MainNavigationItem {

	public MainNavigationItemLink(final String displayname, final String link) {
		super(displayname, link);
	}

	public MainNavigationItemLink() {
	}
}
