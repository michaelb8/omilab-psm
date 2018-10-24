// File:         RoleService.java
// Created:      12.05.15
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2015 by OMiLAB.ORG
//

package org.omilab.psm.service.role;

import java.util.List;

public interface RoleService {

	public List<String> getRoles(String projecturl, String username);

	public Boolean isFranchiseAdmin(String username);
}
