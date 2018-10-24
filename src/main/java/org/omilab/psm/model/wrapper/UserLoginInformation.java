// File:         UserLoginInformation.java
// Created:      25.02.2016
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.model.wrapper;

import java.util.Date;

public class UserLoginInformation {

    private String ip;
    private Date loginDate;

    public UserLoginInformation(String ip) {
        this.ip = ip;
        this.loginDate = new Date();
    }

    public String getIp() {
        return ip;
    }

    public Date getLoginDate() {
        return (Date)loginDate.clone();
    }

}
