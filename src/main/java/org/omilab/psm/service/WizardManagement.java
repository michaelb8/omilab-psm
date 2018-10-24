// File:         WizardManagement.java
// Created:      01.02.16
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.service;

import org.omilab.psm.model.db.*;
import org.omilab.psm.model.wrapper.UINavigationItem;
import org.springframework.expression.spel.ast.PropertyOrFieldReference;

import java.util.List;
import java.util.Locale;

public interface WizardManagement {

	public List<UINavigationItem> getStepsForProposal(ProjectProposal proposal, int pos,Locale locale);

	public List<DBNavigationItem> getEndpointsForProposal(ProjectProposal proposal);

	public List<String> getServiceNames(ProjectProposal proposal);

	public List<String> getServiceEndpointNames(ProjectProposal proposal);

	public void startWizard(ProjectProposal proposal);

	public void stopWizard(ProjectProposal proposal);

	public void instantiateWhenNecessary(ProjectProposal proposal, ServiceDefinition sd);

	public WizardConfigurationEntry getByDBNIAndType(Long dbni, Long type);

	public String parseVariables(ProjectProposal proposal, String entry);

	public String getCurrentStatus(ProjectProposal proposal, Locale locale);

}
