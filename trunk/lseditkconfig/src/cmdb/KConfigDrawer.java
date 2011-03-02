/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cmdb;

import cmdb.BasicReader;
import java.sql.ResultSet;
import javax.swing.text.Utilities;
import lsedit.EntityClass;
import lsedit.EntityInstance;
import lsedit.RelationClass;
import lsedit.ResultBox;
import lsedit.Ta;
import parser.KConfigParser;
import ta.TAEntityClass;
import ta.TARelation;

/**
 *
 * @author nadsa02
 */
public class KConfigDrawer extends BasicReader {

 

    @Override
    public String parseSpecialPath(Ta diagram, ResultBox resultBox, String path) {
        
        String msg = null;




        m_resultBox = resultBox;

        setTitle("Loading KConfig");

        try {

            System.out.println("connecting");
                        
        } catch (Throwable e) {

            msg = "Unable to connect to CMDB";
            report(msg);
            reportException(e);
        }

        if (msg == null) {
            try {
                init(diagram);
                                       
         
            KConfigParser parser = new KConfigParser("kconfig_full-clean.txt", diagram);
            parser.parse();

                addRootAttributes();

                m_diagram.attachBaseClasses();
            } catch (Throwable e) {
                StackTraceElement[] stack = e.getStackTrace();
                String trace;
                int i;

                msg = "Unable to load CMDB";
                report(msg);
                reportException(e);

                for (i = stack.length; i > 0;) {
                    trace = stack[--i].toString();
                    report(trace);
                }
            }           
        }

        if (msg != null) {
            done("Load failed!!");
        } else {
            done("Loaded mdb ");
        }
        return msg;
    }

   private static void init(Ta diagram) {
        m_diagram = diagram;
        m_contains_rc = null;
        m_table_id = 0;
        m_table_ec = null;
        m_contains_order = -1;
    }

}
