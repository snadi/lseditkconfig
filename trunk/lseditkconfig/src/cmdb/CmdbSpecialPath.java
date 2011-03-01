package cmdb;

import java.util.Vector;


import lsedit.Ta;
import lsedit.ResultBox;
import lsedit.SpecialPath;
import cmdb.KConfigDrawer;

public class CmdbSpecialPath implements SpecialPath {
    
    private PdmReader m_pdmReader = null;      
    private KConfigDrawer kconfigDrawer = null;        
    private Vector<String> predictedSet;

    public CmdbSpecialPath(Vector<String> predictedSet){
        this.predictedSet = predictedSet;
    }

    public String parseSpecialPath(Ta diagram, ResultBox resultBox, String path) {
        if (path.length() >= 5) {

            if(path.substring(0,6).equals("kconf:")){
                  if (kconfigDrawer == null) {
                    kconfigDrawer = new KConfigDrawer();
                }
                return kconfigDrawer.parseSpecialPath(diagram, resultBox, path);
            }
            if (path.substring(0, 4).equals("pdm:")) {
                if (m_pdmReader == null) {
                    m_pdmReader = new PdmReader();
                }
                return m_pdmReader.parseSpecialPath(diagram, resultBox, path);
            }                  
          
        }
        return ("Unknown path prefix " + path);
    }

    public boolean isSpecialPath(String path) {
        if (path.length() >= 5) {
            if(path.substring(0,6).equals("kconf:"))
                return true;
            if (path.substring(0, 4).equals("pdm:")) {
                return true;
            }
            if (path.substring(0, 5).equals("cmdb:")) {
                return true;
            }
            if (path.substring(0, 5).equals("rept:")) {                
                return true;
            }

              if (path.substring(0, 5).equals("serv:")) {
                return true;
            }

            if (path.substring(0, 4).equals("chg:")) {
                return true;
            }

             if (path.substring(0, 5).equals("grph:")) {
                return true;
            }
        }
        return false;
    }
}

