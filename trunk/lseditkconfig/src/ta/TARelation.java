/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ta;

/**
 *
 * @author snadi
 */
public class TARelation {

    //relation types
    public static String DEPENDS_ON = "depends_on";
    public static String SELECT = "selects";
    public static String CONTAINS = "contain";
    public static String DEPENDS_ON_NS = "depends_on_ns"; //depends on not selected
    public static String CONFIG_IF_SELECTED = "config_if_selected";
    public static String CONFIG_IF_NOT_SELECTED = "config_if_not_selected";
    public static String HAS_DEFAULT_VALUE = "has_default_value";
    public static String HAS_DEFAULT_VALUE_NOT = "has_default_value_not";
    public static String VISIBLE_IF_SELECTED = "visible_if_selected";
    public static String VISIBLE_IF_NS = "visible_if_not_selected";

    private String relationClass;
    private String relatedEntity;
    private TaAttribute attribute;

    public TARelation(String relationClass, String relatedEntity) {
        this.relationClass = relationClass;
        this.relatedEntity = relatedEntity;
        attribute = null;
    }

    public TaAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(TaAttribute attribute) {
        this.attribute = attribute;
    }

    

    public static String getSELECT() {
        return SELECT;
    }

    public static void setSELECTS(String SELECTS) {
        TARelation.SELECT = SELECTS;
    }

    public String getRelatedEntity() {
        return relatedEntity;
    }

    public void setRelatedEntity(String relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public String getRelationClass() {
        return relationClass;
    }

    public void setRelationClass(String relationClass) {
        this.relationClass = relationClass;
    }

    

}
