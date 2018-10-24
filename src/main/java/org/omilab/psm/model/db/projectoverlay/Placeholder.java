package org.omilab.psm.model.db.projectoverlay;

import org.omilab.psm.model.db.AbstractProject;
import org.omilab.psm.model.db.ProjectProposal;

/**
 * Created by dopplers90cs on 22.12.2016.
 */
public class Placeholder extends AbstractProject {
    private String text;

    public Placeholder(){
        super("", "", "", null);
        text = "";
        super.setInConfig(false);
    }

    public Placeholder(String name, String abbreviation, String urlId, ProjectProposal proposal, String text){
        super(name, abbreviation, urlId, proposal);
        this.text = text;
        super.setInConfig(false);
    }

    public String getText(){
        return this.text;
    }

    public void setText(String text){
        this.text = text;
    }
}
