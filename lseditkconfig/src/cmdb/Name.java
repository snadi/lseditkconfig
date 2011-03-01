/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cmdb;

/**
 *
 * @author nadsa02
 */
public class Name {

    private String firstName;
    private String lastName;

    public String toString(){
        return firstName + " " + lastName;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


}
