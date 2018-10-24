// File:         MServiceTool.java
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
@DiscriminatorValue("mservicetool")
public class MServiceTool extends AbstractProject {

    public MServiceTool(String name, String shortdescription, String urlidentifier, ProjectProposal proposal, String link, String abbreviation) {
        super(name,abbreviation,urlidentifier,proposal);
        this.link = link;
        this.shortdescription = shortdescription;
    }

    public MServiceTool() {
    }

    @Field
    @Size(min = 5, max = 300)
    @Column(nullable = true)
    private String link;

    @Column(nullable = true)
    private URL projectlogo;

    @Column(nullable = true)
    private String projectmodel;

    @Field
    @Size(min = 5, max = 250)
    @Column(nullable = true)
    private String shortdescription;

    @Column(nullable = true)
    private String download;

    @Column(nullable = true)
    private String usermanual;

    public void update(MServiceTool project) {
        super.update(project);
        this.setLink(project.getLink());
        this.setProjectlogo(project.getProjectlogo());
        this.setProjectmodel(project.getProjectmodel());
        this.setShortdescription(project.getShortdescription());
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getUsermanual() {
        return usermanual;
    }

    public void setUsermanual(String usermanual) {
        this.usermanual = usermanual;
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
}
