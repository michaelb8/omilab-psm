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

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.Size;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

@Entity
@Indexed
@DiscriminatorValue("mmproject")
public class MMProject extends AbstractProject {

    public MMProject(String name, String shortdescription, String urlidentifier, ProjectProposal proposal, String affiliation, String abbreviation) {
        super(name,abbreviation,urlidentifier,proposal);
        this.affiliation = affiliation;
        this.shortdescription = shortdescription;
    }

    public MMProject() {
    }

    @Field
    @Size(min = 5, max = 100)
    @Column(nullable = true)
    private String affiliation;

    @Column(nullable = true)
    private URL projectlogo;

    @Column(nullable = true)
    private String projectmodel;

    @Field
    @Size(min = 5, max = 250)
    @Column(nullable = true)
    private String shortdescription;

    @Column
    private String officialdownload;

    @Column
    private String usermanual;

    @Column
    private String guidelines;

    @Column
    private Boolean released;

    @Column
    private String deploymentenv;

    @Column
    private Boolean publishedglobally;

    public void update(MMProject project) {
        super.update(project);
        this.setAffiliation(project.getAffiliation());
        this.setProjectlogo(project.getProjectlogo());
        this.setProjectmodel(project.getProjectmodel());
        this.setShortdescription(project.getShortdescription());
        this.setDeploymentenv(project.getDeploymentenv());
        this.setGuidelines(project.getGuidelines());
        this.setOfficialdownload(project.getOfficialdownload());
        this.setUsermanual(project.getUsermanual());
        this.setPublishedglobally(this.getPublishedglobally());
        this.setReleased(this.getReleased());
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public URL getProjectlogo() {
        return projectlogo;
    }

    public void setProjectlogo(URL projectlogo) {
        this.projectlogo = projectlogo;
        getMeanColor();
    }

    public String getProjectmodel() {
        return projectmodel;
    }

    public void setProjectmodel(final String projectmodel) {
        this.projectmodel = projectmodel;
    }

    public String getShortdescription() {
        return shortdescription;
    }

    public void setShortdescription(String shortdescription) {
        this.shortdescription = shortdescription;
    }

    private void getMeanColor() {
        try {
            BufferedImage image = ImageIO.read(projectlogo);
            Color col = averageColor(image,0,0,image.getWidth(),image.getHeight());
            String hex = "#"+Integer.toHexString(col.getRGB()).substring(2);
            setBackgroundColor(hex);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*
 * Source: http://stackoverflow.com/questions/28162488/get-average-color-on-bufferedimage-and-bufferedimage-portion-as-fast-as-possible
 */
    private Color averageColor(BufferedImage bi, int x0, int y0, int w,
                               int h) {
        int x1 = x0 + w;
        int y1 = y0 + h;
        long sumr = 0, sumg = 0, sumb = 0;
        int num = 0;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                Color pixel = new Color(bi.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
                num++;
            }
        }
        int red = (int) sumr / num;
        int green =  (int) sumg / num;
        int blue = (int) sumb / num;
        return new Color(red,green,blue);
    }

    public String getOfficialdownload() {
        return officialdownload;
    }

    public void setOfficialdownload(String officialdownload) {
        this.officialdownload = officialdownload;
    }

    public String getUsermanual() {
        return usermanual;
    }

    public void setUsermanual(String usermanual) {
        this.usermanual = usermanual;
    }

    public String getGuidelines() {
        return guidelines;
    }

    public void setGuidelines(String guidelines) {
        this.guidelines = guidelines;
    }

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }

    public String getDeploymentenv() {
        return deploymentenv;
    }

    public void setDeploymentenv(String deploymentenv) {
        this.deploymentenv = deploymentenv;
    }

    public Boolean getPublishedglobally() {
        return publishedglobally;
    }

    public void setPublishedglobally(Boolean publishedglobally) {
        this.publishedglobally = publishedglobally;
    }
}
