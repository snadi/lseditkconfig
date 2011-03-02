/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ta;

import lsedit.EntityInstance;

/**
 *
 * @author snadi
 */
public class TaAttribute {

    private String attributeName;
    private String attributeValue;
    private EntityInstance entityInstance;

    //constant attribute types
    public static String TYPE = "type";
    public static String DEFAULT_VALUE = "default";
    public static String USER_SELECTABLE = "user_selectable";
    public static String PROMPT = "prompt";
    public static String SELECT_CONDITION = "select_condition";
    public static String PATH = "path";

    public TaAttribute(String attributeName, String attributeValue) {
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    

}
