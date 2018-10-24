// File:         MMProject.java
// Created:      13.02.2016
// Author:       David Götzinger (dgoetzinger@dke.univie.ac.at)
//
// Copyright (C) 2016 by OMiLAB.ORG
//

package org.omilab.psm.model.db.projectoverlay;


import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.ProjectProposal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Indexed
@DiscriminatorValue("event")
public class Event extends AbstractProject {

    public Event(String name, String shortdescription, String urlidentifier, ProjectProposal proposal, Date start, Date end, String abbreviation,
                 String location) {
        super(name,abbreviation,urlidentifier,proposal);
        this.start = (Date)start.clone();
        this.end = (Date)end.clone();
        this.location = location;
        this.shortdescription = shortdescription;
    }

    public Event() {
    }

    @Column(nullable = true)
    private Date start;

    @Column(nullable = true)
    private Date end;

    @Field
    @Column(nullable = true)
    private String location;

    @Field
    @Size(min = 5, max = 250)
    @Column(nullable = true)
    private String shortdescription;

    @Column
    private String homepage;

    public void update(Event project) {
        super.update(project);
        this.setStart(project.getStart());
        this.setEnd(project.getEnd());
        this.setLocation(project.location);
        this.setShortdescription(project.shortdescription);
        this.setHomepage(project.getHomepage());
    }

    public Date getStart() {
        if( start == null)
            return null;
        else
            return (Date)start.clone();
    }

    public void setStart(Date start) {
        this.start = (Date)start.clone();
    }

    public Date getEnd() {
        if( end == null)
            return null;
        else
            return (Date)end.clone();
    }

    public void setEnd(Date end) {
        this.end = (Date)end.clone();
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getShortdescription() {
        return shortdescription;
    }

    public void setShortdescription(String shortdescription) {
        this.shortdescription = shortdescription;
    }
}
