/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cmdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author snadi
 */
public class CausalityRelationships {

    private static final String FORWARD_CAUSALITY_KEY = "forward";
    private static final String BACKWARD_CAUSALITY_KEY = "backward";

    private static String propertiesFileName;
    private static CausalityRelationships instance;
    private static Vector<String> forwardCausality;
    private static Vector<String> backwardCausality;
    private static Properties properties;

    private static void print(Vector<String> v) {
        for(int i=0; i < v.size(); i++){
            System.out.println(v.get(i) + "X");
        }
    }

    private CausalityRelationships(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
        forwardCausality = new Vector<String>();
        backwardCausality = new Vector<String>();
        properties = new Properties();
    }

    public static CausalityRelationships getInstance(String propertiesFileName) {

        if (instance == null) {
            try {
                File file = new File(propertiesFileName);
                System.out.println(file.getAbsolutePath());
                instance = new CausalityRelationships(propertiesFileName);
                properties.load(new FileInputStream(propertiesFileName));
                forwardCausality = readRelations(FORWARD_CAUSALITY_KEY);
                backwardCausality = readRelations(BACKWARD_CAUSALITY_KEY);
              
            } catch (IOException ex) {
                Logger.getLogger(CausalityRelationships.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return instance;
    }

  

    private static Vector readRelations(String propertyName){
        //read forward relationships
            String readProperty = properties.getProperty(propertyName);

            String relationshipIds[] = readProperty.split(",");
            Vector<String> tempVector = new Vector<String>();

            //optimize later
            for(int i =0; i < relationshipIds.length; i++){
                tempVector.add(relationshipIds[i].trim());
                //the pdm conversion puts ci_rel_type infront of the relationship id
                tempVector.add("ci_rel_type" + relationshipIds[i].trim());
            }


            return tempVector;
    }

    public boolean isForwardCausality(String relationshipId){
        return (forwardCausality.contains(relationshipId));
    }

    public boolean isBackwardCausality(String relationshipId){
        return backwardCausality.contains(relationshipId);
    }
}
