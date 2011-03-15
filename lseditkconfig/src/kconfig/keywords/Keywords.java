/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kconfig.keywords;

/**
 *
 * @author snadi
 */
public abstract class Keywords {

    //entries
    public static String CONFIG = "config";
    public static String SOURCE = "source";
    public static String MENU = "menu";
    public static String END_MENU = "endmenu";
    public static String CHOICE = "choice";
    public static String END_CHOICE = "endchoice";
    public static String MENUCONFIG = "menuconfig";
    public static String IF = "if";
    public static String END_IF = "endif";
    public static String END_SOURCE = "endsource";

    //config types
    public static String BOOL = "bool";
    public static String TRISTATE = "tristate";
    public static String INTEGER = "int";
    public static String STRING = "string";

    //relations
    public static String DEPENDS_ON = "depends on";
    public static String SELECT = "select";
    public static String VISIBLE_IF = "visible if";
    

    //keywords
    public static String DEFAULT = "default";
    public static String DEFAULT_BOOL = "def_bool";
    public static String PROMPT = "prompt";
    public static String HELP = "help";
    public static String OTHER_HELP = "---help---";
    public static String COMMENT = "comment";
    

    public static boolean isKeyword(String word){
        if (word.equals(CONFIG) || word.equals(SOURCE) || word.equals(CONFIG))
            return true;

        return false;


    }

    

}
