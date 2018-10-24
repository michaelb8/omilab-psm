// File:         GlobalNetworkService.java
// Created:      05.08.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//


package org.omilab.psm.service;

public interface GlobalNetworkService {

    public boolean check();

    public Boolean register();

	public void sync();

    public void remove();

	public boolean uptodate();
}