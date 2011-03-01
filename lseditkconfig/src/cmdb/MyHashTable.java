/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cmdb;

import java.util.Hashtable;
import java.util.Vector;
import lsedit.EntityInstance;

/**
 *
 * @author nadsa02
 */
public class MyHashTable extends Hashtable {

    @Override
    public synchronized Object put(Object key, Object value) {
        Vector<EntityInstance> instances = (Vector<EntityInstance>) get(key);

        if(instances == null){
         //   System.out.println(key + " was empty ");
            instances = new Vector<EntityInstance>();
        }
        
        instances.add((EntityInstance) value);
        return super.put(key, instances);
    }



}
