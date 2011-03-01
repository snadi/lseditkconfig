package cmdb;

import java.util.Vector;
import lsedit.LandscapeEditorFrame;
import lsedit.SpecialPath;

/* This code extends LandscapeEditorFrame so that JDBC does not need to be made
 * part of an LSEDIT compile -- only a CMDB compile
 */
 
public class CmdbEditorFrame extends LandscapeEditorFrame {
   	
        
	public SpecialPath getSpecialPath()
	{
		return new CmdbSpecialPath(predictedSet);
	}

    public void setPredictedSet(Vector<String> predictedSet){        
        this.predictedSet = predictedSet;

    }
	public static void main(String args[]) 
	{
   
        CmdbEditorFrame  af = new CmdbEditorFrame();

		af.launch(args);

	}
}
