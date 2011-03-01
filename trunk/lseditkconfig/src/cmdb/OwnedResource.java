/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cmdb;

/**
 *
 * @author nadsa02
 */
public class OwnedResource {

    private String resource_uuid;
    private String persid;
    private String resource_name;
    private String resource_type;

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public String getPersid() {
        return persid;
    }

    public void setPersid(String persid) {
        this.persid = persid;
    }

    public String getResource_name() {
        return resource_name;
    }

    public void setResource_name(String resource_name) {
        this.resource_name = resource_name;
    }

    public String getResource_uuid() {
        return resource_uuid;
    }

    public void setResource_uuid(String resource_uuid) {
        this.resource_uuid = resource_uuid;
    }
}
