/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cmdb;

import java.util.Date;
import java.util.Hashtable;

/**
 *
 * @author nadsa02
 */
public class ChangeOrder {

    private String referenceNum;
    private String description;
    private Date startDate;
    private Date closeDate;
    private String justification;
    private String backoutPlan;
    private String assignee;
    private String affectedContact;
    private Hashtable<String, OwnedResource> affectedResources;

    public ChangeOrder(){
        affectedResources = new Hashtable<String, OwnedResource>();
    }

    public void addResource(String key, OwnedResource resource){
        affectedResources.put(key, resource);
    }

    public Hashtable<String, OwnedResource> getAffectedResources() {
        return affectedResources;
    }

    public void setAffectedResources(Hashtable<String, OwnedResource> affectedResources) {
        this.affectedResources = affectedResources;
    }


    public String getAffectedContact() {
        return affectedContact;
    }

    public void setAffectedContact(String affectedContact) {
        this.affectedContact = affectedContact;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getBackoutPlan() {
        return backoutPlan;
    }

    public void setBackoutPlan(String backoutPlan) {
        this.backoutPlan = backoutPlan;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public String getReferenceNum() {
        return referenceNum;
    }

    public void setReferenceNum(String referenceNum) {
        this.referenceNum = referenceNum;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

}
