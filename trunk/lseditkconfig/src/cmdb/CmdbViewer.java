package cmdb;

import lsedit.LandscapeViewer;
import lsedit.SpecialPath;

/* This code extends LandscapeViewer so that JDBC does not need to be made
 * part of an LSEDIT compile -- only a CMDB compile
 */
 
public class CmdbViewer extends LandscapeViewer {
	
	public SpecialPath getSpecialPath()
	{
		return new CmdbSpecialPath(null);
	}
}
