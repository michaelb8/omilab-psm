// File:         GenericServiceContent.java
// Created:      14.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.wrapper;

import java.util.Map;

public final class GenericServiceContent {

	private String content;

	private Map<String, String> submenu;

	private long responseTime;

	public GenericServiceContent() {
	}

	public GenericServiceContent(String content, Map<String, String> submenu) {
		this.content = content;
		this.submenu = submenu;
	}

	public String getContent() {
		return content;
	}

	public Map<String, String> getSubmenu() {
		return submenu;
	}

	public void setSubmenu(Map<String, String> submenu) {
		this.submenu = submenu;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	@Override
	public String toString() {
		return "GenericServiceContent{" +
				"content='" + content + '\'' +
				'}';
	}
}
