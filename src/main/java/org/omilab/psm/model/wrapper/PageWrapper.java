// File:         PageWrapper.java
// Created:      15.02.2015
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.model.wrapper;

import org.springframework.data.domain.Page;

import java.util.ArrayList;

public final class PageWrapper<T> {

	private final static int PAGE_MENU_WIDTH = 10;

	private int begin;

	private int current;

	private int end;

	private int next;

	private ArrayList<Integer> pagesList = new ArrayList<Integer>();

	private int previous;

	public PageWrapper(Page<T> page) {
		this.current = page.getNumber() + 1;
		this.begin = Math.max(1, current - (PAGE_MENU_WIDTH / 2));
		this.end = Math.min(begin + PAGE_MENU_WIDTH, page.getTotalPages());
		this.next = current + 1;
		this.previous = current - 1;

		for(int i = begin; i <= end; i++) {
			pagesList.add(i);
		}
	}

	public int getBegin() {
		return begin;
	}

	public int getCurrent() {
		return current;
	}

	public int getEnd() {
		return end;
	}

	public int getNext() {
		return next;
	}

	public ArrayList<Integer> getPagesList() {
		return pagesList;
	}

	public int getPrevious() {
		return previous;
	}

}