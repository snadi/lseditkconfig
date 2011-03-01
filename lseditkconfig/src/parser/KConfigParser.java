/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import lsedit.EntityInstance;
import lsedit.Ta;
import ta.TAEntityClass;
import ta.TARelation;
import ta.TaAttribute;
import kconfig.keywords.Keywords;
import lsedit.RelationClass;
import lsedit.RelationInstance;

/**
 *
 * @author snadi
 */
public class KConfigParser {

    private String fileName;
    private Ta diagram;
    private DataInputStream dis;

    public KConfigParser(String fileName, Ta diagram) {
        this.fileName = fileName;
        this.diagram = diagram;
        addBaseClassesAndRelns();
    }

    private void addBaseClassesAndRelns() {

        diagram.addRelationClass(TARelation.CONTAINS);
        diagram.addRelationClass(TARelation.DEPENDS_ON);
        diagram.addRelationClass(TARelation.SELECT);
        diagram.addRelationClass(TARelation.DEPENDS_ON_NS);
        diagram.addRelationClass(TARelation.CONFIG_IF_NOT_SELECTED);
        diagram.addRelationClass(TARelation.CONFIG_IF_SELECTED);
        diagram.addRelationClass(TARelation.HAS_DEFAULT_VALUE);
        diagram.addRelationClass(TARelation.HAS_DEFAULT_VALUE_NOT);

        diagram.addEntityClass(TAEntityClass.CONFIG_CLASS);
        diagram.addEntityClass(TAEntityClass.MENU_CLASS);
        diagram.addEntityClass(TAEntityClass.CHOICE_CLASS);
        diagram.addEntityClass(TAEntityClass.IF_CLASS);
        diagram.addEntityClass(TAEntityClass.MENU_CONFIG_CLASS);
    }

    public void parse() throws Exception {
        System.out.println("enter parse()");

        File file = new File(fileName);
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try {
            fis = new FileInputStream(file);

            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);

            EntityInstance parentContainer = null;


            while (dis.available() != 0) {
                String line = dis.readLine();
                line = line.trim();

                parentContainer = parseContainer(parentContainer, line, null);
                System.out.println("return to parse");
            }


            fis.close();
            bis.close();
            dis.close();

            System.out.println("exit parse()");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private EntityInstance parseContainer(EntityInstance parent, String line, RelationClass relnClass) throws Exception {
        System.out.println("enter parseContainer() with: " + line);
        EntityInstance entityInstance = null;
        line = line.trim();
        String parts[] = line.split(" ");

        if (isNewEntry(line)) {
            if (Pattern.matches(Keywords.MENU + "\\s\".*\"", line)) {
                entityInstance = parseMenu(parent, line);
            } else if (Pattern.matches(Keywords.MENUCONFIG + "\\s.*", line) && parts.length == 2) {
                entityInstance = parseMenuConfig(parent, line);
                System.out.println("returned from menuconfgi");
            } else if (line.startsWith(Keywords.IF) && parts.length == 2) {
                entityInstance = parseIF(parent, line);
                System.out.println("returned from if");
            } else if (line.equals(Keywords.CHOICE)) {
                entityInstance = parseChoice(parent, line);
            } else if (Pattern.matches(Keywords.CONFIG + "\\s.*", line.trim()) && parts.length == 2) {
                entityInstance = parseConfig(parent, line);
            } else {
                System.out.println("IGNORED: " + line);
            }
        } else {
            System.out.println("IGNORED" + line);
        }

        //  if (parent != null && entityInstance != null && relnClass != null) {
        //    diagram.addEdge(relnClass, parent, entityInstance);
        //}

        System.out.println("exit parseContainer()");
        return entityInstance;
    }

    private EntityInstance parseMenu(EntityInstance parent, String line) throws Exception {
        System.out.println("enter parseMenu() with: " + line);
        String menuName = line.trim().substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));

        EntityInstance menuInstance = diagram.getCache(removeSpacesAndQuotes(menuName));

        if (menuInstance == null) {
            menuInstance = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.MENU_CLASS), removeSpacesAndQuotes(menuName));
        }

        String newLine = readLine();
        //load menu attributes first
        while (newLine != null && !isEndMenu(newLine) && isMenuAttribute(newLine)) {
            if (isDependsOn(newLine)) {
                addDependency(menuInstance, newLine);
            } else if (isVisibeIf(newLine)) {
                addVisibility(menuInstance, newLine);
            }

            newLine = readLine();
        }

        System.out.println("line: " + newLine);
        if (!isEndMenu(newLine)) {
            dis.reset();
        }

        loadMenu(menuInstance);

        if (parent != null) {
            diagram.addEdge(diagram.getRelationClass(TARelation.CONTAINS), parent, menuInstance);
        }

        System.out.println("exit parseMenu()");
        return menuInstance;

    }

    private String readLine() throws Exception {
        dis.mark(10000);
        String line = dis.readLine();
        if (line != null) {
            line = line.trim();
        }
        return line;
    }

    private void addPrompt(EntityInstance entityInstace, String line) {
        String parts[] = line.trim().split("\"");
        entityInstace.addAttribute(TaAttribute.PROMPT, "\"" + removeSpacesAndQuotes(parts[1].trim()) + "\"");
    }

    private void addDefault(EntityInstance entityInstance, String line) {
        //TODO: ADD MULTIPLE DEFAULT VALUES
        String parts[] = line.split(" ");
        entityInstance.addAttribute(TaAttribute.DEFAULT_VALUE, "\"" + removeQuotes(parts[1].trim()) + "\"");
    }

    private boolean isEndChoice(String line) {
        return line != null && line.equals(Keywords.END_CHOICE);
    }

    private EntityInstance parseChoice(EntityInstance parent, String line) throws Exception {
        System.out.println("enter parseChoice() with: " + line);
        String prompt = "";
        EntityInstance choiceInstance = null;

        line = readLine();

        //load choice attributes first
        while (line != null && isChoiceAttribute(line)) {
            if (isPrompt(line)) {
                String parts[] = line.trim().split("\"");
                choiceInstance = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CHOICE_CLASS), removeSpacesAndQuotes(parts[1].trim()));
                choiceInstance.addAttribute(TaAttribute.PROMPT, "\"" + removeSpacesAndQuotes(parts[1].trim()) + "\"");

            } else if (isDefault(line)) {
                addDefault(choiceInstance, line);
            }

            line = readLine();
        }

        if (!isEndChoice(line)) {
            dis.reset();


            loadChoice(choiceInstance);
        }

        if (parent != null && choiceInstance != null) {
            diagram.addEdge(diagram.getRelationClass(TARelation.CONTAINS), parent, choiceInstance);
        }


        System.out.println("exit parseChoice() with: " + line);
        return choiceInstance;

    }

    private void loadChoice(EntityInstance choiceInstance) throws Exception {
        System.out.println("enter loadChoice()");
        String line = readLine();


        while (line != null && !isEndChoice(line)) {
            // if (line.startsWith(Keywords.CONFIG + " ")) {
            EntityInstance choiceEntry = null;

            if (isConfig(line)) {
                choiceEntry = parseConfig(choiceInstance, line);
            } else if (isIf(line)) {
                choiceEntry = parseIF(choiceInstance, line);
            } else if (isChoice(line)) {
                choiceEntry = parseChoice(choiceInstance, line);
            }

            if (choiceEntry != null && choiceInstance != null) {
                diagram.addEdge(diagram.getRelationClass(TARelation.CONTAINS), choiceInstance, choiceEntry);
            }

            line = readLine();
        }

        if (!isEndChoice(line)) {
            dis.reset();
        }

        System.out.println("exit loadChoice() with: " + line);

    }

    private boolean isGeneralAttribute(String line) {
        return isType(line) || isDefault(line) || isDefBool(line) || isRelation(line) || isHelp(line) || isPrompt(line);
    }

    private boolean isRelation(String line) {
        return isDependsOn(line) || isSelect(line);
    }

    private boolean isType(String line) {
        return (isBool(line) || isTristate(line) || isString(line) || isInteger(line));
    }

    private boolean isInteger(String line) {
        return line.equals(Keywords.INTEGER) || Pattern.matches(Keywords.INTEGER + "\\s\".*\"", line);
    }

    private boolean isBool(String line) {
        return line.equals(Keywords.BOOL)
                || Pattern.matches(Keywords.BOOL + "\\s\".*\"", line);
    }

    private boolean isTristate(String line) {
        return line.equals(Keywords.TRISTATE)
                || Pattern.matches(Keywords.TRISTATE + "\\s\".*\"", line);
    }

    private boolean isString(String line) {
        return line.equals(Keywords.STRING) || Pattern.matches(Keywords.STRING + "\\s\".*\"", line);
    }

    private boolean isChoiceAttribute(String line) {
        return (isPrompt(line) || isDefault(line));
    }

    private boolean isPrompt(String line) {
        return Pattern.matches(Keywords.PROMPT + "\\s\".*\"", line);
    }

    private boolean isDefault(String line) {
        String parts[] = line.split(" ");
        return (line.startsWith(Keywords.DEFAULT + " ") && parts.length > 1);
    }

    private boolean isEndMenu(String line) {
        return line.equals(Keywords.END_MENU);
    }

    private boolean isMenuAttribute(String line) {
        return (isDependsOn(line) || isVisibeIf(line));
    }

    private boolean isDependsOn(String line) {
        String parts[] = line.split(" ");
        return line.startsWith(Keywords.DEPENDS_ON + " ") && parts.length > 1;
    }

    private boolean isVisibeIf(String line) {
        String parts[] = line.split(" ");
        return line.startsWith(Keywords.VISIBLE_IF + " ") && parts.length > 1;
    }

    private void addDependency(EntityInstance parentInstance, String line) {
        String parts[] = line.split(" ");
        if (parts[2].trim().startsWith("!")) {
            EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[2].trim().substring(1)));
            if (relatedEntity == null) {
                relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[2].trim().substring(1)));
            }

            diagram.addEdge(diagram.getRelationClass(TARelation.DEPENDS_ON_NS), parentInstance, relatedEntity);
        } else {

            EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[2].trim()));
            if (relatedEntity == null) {
                relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[2].trim()));
            }

            diagram.addEdge(diagram.getRelationClass(TARelation.DEPENDS_ON), parentInstance, relatedEntity);
        }
    }

    private void addVisibility(EntityInstance parentInstance, String line) {
        String parts[] = line.split(" ");
        if (parts[2].trim().startsWith("!")) {
            EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[2].trim().substring(1)));
            if (relatedEntity == null) {
                relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[2].trim().substring(1)));
            }

            diagram.addEdge(diagram.getRelationClass(TARelation.VISIBLE_IF_NS), parentInstance, relatedEntity);
        } else {

            EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[2].trim()));
            if (relatedEntity == null) {
                relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[2].trim()));
            }

            diagram.addEdge(diagram.getRelationClass(TARelation.VISIBLE_IF_SELECTED), parentInstance, relatedEntity);
        }
    }

    private void loadMenu(EntityInstance menuInstance) throws Exception {
        System.out.println("enter loadMeny() with: ");

        String line = readLine();


        while (line != null && !line.equals(Keywords.END_MENU)) {
            EntityInstance menuEntry = null;
            if (isMenu(line)) {
                menuEntry = parseMenu(menuInstance, line);
            } else if (isConfig(line)) {
                menuEntry = parseConfig(menuInstance, line);
            } else if (isIf(line)) {
                menuEntry = parseIF(menuInstance, line);
            } else if (isMenuConfig(line)) {
                menuEntry = parseMenuConfig(menuInstance, line);
            } else if (isChoice(line)) {
                menuEntry = parseChoice(menuInstance, line);
            } else if (isComment(line)) {
                menuEntry = parseComment(menuInstance, line);
            } else if (isSource(line)) {
                System.out.println("SHOULD LOAD SOURCE: " + line);
            } else {
                System.out.println("IGNORED: " + line);
            }

            if (menuInstance != null && menuEntry != null) {
                diagram.addEdge(diagram.getRelationClass(TARelation.CONTAINS), menuInstance, menuEntry);
            }

            line = readLine();
            System.out.println("READ IN PARSEMENU:" + line);
        }

        System.out.println("exit loadMenu()");
    }

    private EntityInstance parseComment(EntityInstance parent, String line) {
        return null;
    }

    private boolean isConfig(String line) {
        String parts[] = line.split(" ");
        return (Pattern.matches(Keywords.CONFIG + "\\s.*", line.trim()) && parts.length == 2);
    }

    private boolean isMenu(String line) {
        return Pattern.matches(Keywords.MENU + "\\s\".*\"", line);
    }

    private boolean isIf(String line) {
        String parts[] = line.trim().split(" ");
        return Pattern.matches(Keywords.IF + "\\s.*", line) && parts.length == 2;
    }

    private boolean isMenuConfig(String line) {
        String parts[] = line.trim().split(" ");
        return line.startsWith(Keywords.MENUCONFIG) && parts.length == 2;
    }

    private boolean isChoice(String line) {
        return line.equals(Keywords.CHOICE);
    }

    private boolean isDefBool(String line) {
        return line.startsWith(Keywords.DEFAULT_BOOL);
    }

    private boolean isSelect(String line) {
        String parts[] = line.split(" ");
        return (line.startsWith(Keywords.SELECT + " ") && parts.length > 1);
    }

    private boolean isComment(String line) {
        return Pattern.matches(Keywords.COMMENT + "\\s\".*\"", line);

    }

    private boolean isSource(String line) {
        return Pattern.matches(Keywords.SOURCE + "\\s\".*\"", line);
    }

    private void loadSource(String fileName) {
    }

    private EntityInstance parseConfig(EntityInstance container, String configStart) throws Exception {
        System.out.println("enter parseConfig() with: " + configStart);
        EntityInstance configInstance = null;

        String parts[] = configStart.split(" ");


        configInstance = diagram.getCache(removeSpacesAndQuotes(parts[1].trim()));

        if (configInstance == null) {
            configInstance = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[1].trim()));

        } else {
            System.out.println("CONFIG APPEARING TWICE: " + removeSpacesAndQuotes(parts[1].trim()));
        }



        loadGeneralAttributes(configInstance);


        if (container != null) {
            diagram.addEdge(diagram.getRelationClass(TARelation.CONTAINS), container, configInstance);
        }

        //System.out.println("exit parseConfig() with: "+ line);
        return configInstance;
    }

    private void addRelation(EntityInstance entityInstance, String line) {
        String parts[] = line.split(" ");

        if (isSelect(line)) {
            System.out.println("SELECT LINE: " + line);
            EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[1].trim()));

            if (relatedEntity == null) {
                relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[1]));
            }

            RelationInstance relnInstance = diagram.addEdge(diagram.getRelationClass(TARelation.SELECT), entityInstance, relatedEntity);

            if (parts.length > 2) {

                String condName = parts[3].trim();
                if (condName.startsWith("(!")) {
                    condName = condName.substring(1, condName.length() - 1);
                }

                relnInstance.addAttribute(TaAttribute.SELECT_CONDITION, "\"" + removeQuotes(condName) + "\"");
            }
        } else if (isDependsOn(line)) {
            if (parts[2].trim().startsWith("!")) {
                EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[2].trim().substring(1)));
                if (relatedEntity == null) {
                    relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[2].trim().substring(1)));
                }

                diagram.addEdge(diagram.getRelationClass(TARelation.DEPENDS_ON_NS), entityInstance, relatedEntity);
            } else {

                EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[2].trim()));
                if (relatedEntity == null) {
                    relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[2].trim()));
                }

                diagram.addEdge(diagram.getRelationClass(TARelation.DEPENDS_ON), entityInstance, relatedEntity);
            }
        }
    }

    private boolean loadGeneralAttributes(EntityInstance entityInstance) throws Exception {

        boolean reset = false;
        String line = readLine();
        while (line != null && isGeneralAttribute(line)) {
            String parts[] = line.split(" ");
            if (isType(line)) {
                addType(entityInstance, line);
            } else if (isDefBool(line)) {

                entityInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.BOOL + "\"");
                entityInstance.addAttribute(TaAttribute.DEFAULT_VALUE, "\"" + removeQuotes(parts[1].trim()) + "\"");
                entityInstance.addAttribute(TaAttribute.USER_SELECTABLE, "\"false\"");
            } else if (isDefault(line)) {
                addDefaultValue(entityInstance, line);
            } else if (isPrompt(line)) {
                addPrompt(entityInstance, line);
            } else if (isRelation(line)) {
                addRelation(entityInstance, line);
            } else if (isHelp(line)) {
                addHelp(entityInstance);
                reset = true;
            }

            line = readLine();
        }

        if (line != null && (isNewEntry(line) || isEndOfEntry(line))) {
            System.out.println("Resetting in attr to:" + line);
            dis.reset();
        }

        System.out.println("Exiting read attr with: " + line);
        return true;
    }

    private boolean isEndOfEntry(String line) {
        return isEndChoice(line) || isEndMenu(line) || isEndIf(line);
    }

    private boolean isEndIf(String line) {
        return line != null && line.equals(Keywords.END_IF);
    }

    private void addDefaultValue(EntityInstance entityInstance, String line) {
        String defaultValue = "";

        String parts[] = line.split(" ");

//        if (!(parts[1].trim().equals("y") || parts[1].trim().equals("n"))) {
//            if (parts[1].startsWith("!")) {
//                defaultValue = parts[1].trim().substring(1);
//                relnClass = diagram.getRelationClass(TARelation.HAS_DEFAULT_VALUE_NOT);
//            } else if (parts[1].startsWith("(")) {
//                //TODO
//            } else {
//                defaultValue = parts[1].trim();
//                relnClass = diagram.getRelationClass(TARelation.HAS_DEFAULT_VALUE);
//            }
//
//            EntityInstance defaultInst = diagram.getCache(removeSpacesAndQuotes(defaultValue));
//
//            if (defaultInst == null) {
//                defaultInst = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(defaultValue));
//            }
//
//
//            diagram.addEdge(relnClass, parentInstance, defaultInst);
//        }

        entityInstance.addAttribute(TaAttribute.DEFAULT_VALUE, "\"" + removeQuotes(parts[1]) + "\"");
    }

    private boolean isTriState(String line) {
        return line.equals(Keywords.TRISTATE) || Pattern.matches(Keywords.TRISTATE + "\\s\".*\"", line);
    }

    private void addType(EntityInstance entityInstance, String line) {
        if (isBool(line)) {
            entityInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.BOOL + "\"");
            checkDefPrompt(entityInstance, line);
        } else if (isTriState(line)) {
            entityInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.TRISTATE + "\"");
            checkDefPrompt(entityInstance, line);
        } else if (isString(line)) {
            entityInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.STRING + "\"");
            checkDefPrompt(entityInstance, line);
        } else if (isInteger(line)) {
            entityInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.INTEGER + "\"");
            checkDefPrompt(entityInstance, line);
        }
    }

    private EntityInstance parseMenuConfig(EntityInstance parent, String line) throws Exception {
        System.out.println("enter parseMenuConfig() with: " + line);
        String parts[] = line.trim().split(" ");
        String menuName = parts[1].trim();

        EntityInstance menuInstance = diagram.getCache(removeSpacesAndQuotes(menuName));

        if (menuInstance == null) {
            menuInstance = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.MENU_CONFIG_CLASS), removeSpacesAndQuotes(menuName));
        }



        loadGeneralAttributes(menuInstance);

        //TO DO: HOW TO DEAL WITH MENU CONFIG
        //loadMenuConfig(menuInstance);

        if (parent != null) {
            diagram.addEdge(diagram.getRelationClass(TARelation.CONTAINS), parent, menuInstance);
        }

        System.out.println("exit parseMenuConfig()");
        return menuInstance;
    }

    private void addHelp(EntityInstance entityInstance) throws Exception {
        String inputLine = readLine();
        String helpText = "";

        while (inputLine != null && !(isNewEntry(inputLine) || isEndOfEntry(inputLine))) {
            helpText += inputLine.trim() + "\n";
            dis.mark(10000);
            inputLine = dis.readLine();
        }

        entityInstance.addAttribute(Keywords.HELP, "\"" + removeBracketsAndQuotes(helpText) + "\"");
        System.out.println("resetting in help: " + inputLine);
        dis.reset();
    }

    private EntityInstance parseIF(EntityInstance parent, String line) throws Exception {
        System.out.println("enter parseIf() with: " + line);
        String parts[] = line.trim().split(" ");
        System.out.println("Parsing if: " + line);

        String ifName = "";
        RelationClass relationClass = null;

        if (parts[1].trim().startsWith("!")) {
            ifName = parts[1].trim().substring(1);
            relationClass = diagram.getRelationClass(TARelation.DEPENDS_ON_NS);
        } else {
            ifName = parts[1].trim();
            relationClass = diagram.getRelationClass(TARelation.DEPENDS_ON);
        }


        EntityInstance ifInstance = diagram.getCache(removeSpacesAndQuotes(ifName));

        if (ifInstance == null) {
            System.out.println("null");
            ifInstance = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(ifName));
        } else {
            System.out.println("not null");
        }

        System.out.println("if instance: " + ifInstance + " " + ifInstance.getEntityClass().getId());

        String nextLine = readLine();

        while (nextLine != null && !nextLine.equals(Keywords.END_IF)) {
            EntityInstance ifEntry = null;
            if (isMenu(nextLine)) {
                ifEntry = parseMenu(ifInstance, nextLine);
            } else if (isConfig(nextLine)) {
                ifEntry = parseConfig(ifInstance, nextLine);
            }

            if (ifEntry != null && ifInstance != null) {
                diagram.addEdge(relationClass, ifInstance, ifEntry);
            }

            nextLine = readLine();
        }

        if (parent != null) {
            diagram.addEdge(diagram.getRelationClass(TARelation.CONTAINS), parent, ifInstance);
        }

        System.out.println("exit parseIF()");
        return ifInstance;
    }

    private void parseEntryContents(EntityInstance parentInstance, String line, RelationClass relnClass) throws Exception {
        System.out.println("enter parseEntryContents() with: " + line);
        line = line.trim();
        String parts[] = line.split(" ");


        //check type
        if (line.equals(Keywords.BOOL) || Pattern.matches(Keywords.BOOL + "\\s\".*\"", line)) {
            parentInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.BOOL + "\"");
            checkDefPrompt(parentInstance, line);
        } else if (line.equals(Keywords.TRISTATE) || Pattern.matches(Keywords.TRISTATE + "\\s\".*\"", line)) {
            parentInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.TRISTATE + "\"");
            checkDefPrompt(parentInstance, line);
        } else if (line.equals(Keywords.STRING) || Pattern.matches(Keywords.STRING + "\\s\".*\"", line)) {
            parentInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.STRING + "\"");
            checkDefPrompt(parentInstance, line);
        } else if (line.equals(Keywords.INTEGER) || Pattern.matches(Keywords.INTEGER + "\\s\".*\"", line)) {
            parentInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.INTEGER + "\"");
            checkDefPrompt(parentInstance, line);
        } else if (line.startsWith(Keywords.DEFAULT_BOOL)) {
            parentInstance.addAttribute(TaAttribute.TYPE, "\"" + Keywords.BOOL + "\"");
            parentInstance.addAttribute(TaAttribute.DEFAULT_VALUE, "\"" + removeQuotes(parts[1].trim()) + "\"");
            parentInstance.addAttribute(TaAttribute.USER_SELECTABLE, "\"false\"");
//             relnClass = diagram.getRelationClass(TARelation.HAS_DEFAULT_VALUE_NOT);
//                } else if (parts[1].startsWith("(")) {
//                    //TODO
//                } else {
//                    defaultValue = parts[1].trim();
//                    relnClass = diagram.getRelationClass(TARelation.HAS_DEFAULT_VALUE);
//      
//              String defaultValue = "";
//            RelationClass relnClass = null;
//
//             if (!(parts[1].trim().equals("y") || parts[1].trim().equals("n"))) {
//                if (parts[1].startsWith("!")) {
//                    defaultValue = parts[1].trim().substring(1);
//                   relnClass = diagram.getRelationClass(TARelation.HAS_DEFAULT_VALUE_NOT);
//                } else if (parts[1].startsWith("(")) {
//                    //TODO
//                } else {
//                    defaultValue = parts[1].trim();
//                    relnClass = diagram.getRelationClass(TARelation.HAS_DEFAULT_VALUE);
//                }
//
//                EntityInstance defaultInst = diagram.getCache(removeSpacesAndQuotes(defaultValue));
//
//                if (defaultInst == null) {
//                    defaultInst = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(defaultValue));
//                }
//
//                System.out.println("ADDING: " + relnClass + " " + parentInstance + " " + defaultInst);
//                diagram.addEdge(relnClass, parentInstance, defaultInst);
//            }else{
//                System.out.println("WRONG:"+ line + " " + parts[1]);
//            }

        } //check default values
        else if (line.startsWith(Keywords.DEFAULT + " ") && parts.length > 1) {
            String defaultValue = "";

            if (!(parts[1].trim().equals("y") || parts[1].trim().equals("n"))) {
                if (parts[1].startsWith("!")) {
                    defaultValue = parts[1].trim().substring(1);
                    relnClass = diagram.getRelationClass(TARelation.HAS_DEFAULT_VALUE_NOT);
                } else if (parts[1].startsWith("(")) {
                    //TODO
                } else {
                    defaultValue = parts[1].trim();
                    relnClass = diagram.getRelationClass(TARelation.HAS_DEFAULT_VALUE);
                }

                EntityInstance defaultInst = diagram.getCache(removeSpacesAndQuotes(defaultValue));

                if (defaultInst == null) {
                    defaultInst = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(defaultValue));
                }


                diagram.addEdge(relnClass, parentInstance, defaultInst);
            }
//            } else {
//                System.out.println("WRONG:" + line + " " + parts[1]);
//            }

            parentInstance.addAttribute(TaAttribute.DEFAULT_VALUE, "\"" + removeQuotes(parts[1]) + "\"");
        } //check relations
        else if (line.startsWith(Keywords.DEPENDS_ON + " ") && parts.length > 1) {
            if (parts[2].trim().startsWith("!")) {
                EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[2].trim().substring(1)));
                if (relatedEntity == null) {
                    relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[2].trim().substring(1)));
                }

                diagram.addEdge(diagram.getRelationClass(TARelation.DEPENDS_ON_NS), parentInstance, relatedEntity);
            } else {

                EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[2].trim()));
                if (relatedEntity == null) {
                    relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[2].trim()));
                }

                diagram.addEdge(diagram.getRelationClass(TARelation.DEPENDS_ON), parentInstance, relatedEntity);
            }

        } else if (Pattern.matches(Keywords.CONFIG + "\\s.*", line.trim()) && parts.length == 2) {
            EntityInstance configInstance = parseConfig(parentInstance, line);
            if (relnClass != null) {
                diagram.addEdge(relnClass, parentInstance, configInstance);
            }
        } else if (line.startsWith(Keywords.SELECT + " ") && parts.length > 1) {
            try {

                EntityInstance relatedEntity = diagram.getCache(removeSpacesAndQuotes(parts[1].trim()));

                if (relatedEntity == null) {
                    relatedEntity = diagram.newCachedEntity(diagram.getEntityClass(TAEntityClass.CONFIG_CLASS), removeSpacesAndQuotes(parts[1]));
                }

                RelationInstance relnInstance = diagram.addEdge(diagram.getRelationClass(TARelation.SELECT), parentInstance, relatedEntity);

                if (parts.length > 2) {

                    String condName = parts[3].trim();
                    if (condName.startsWith("(!")) {
                        condName = condName.substring(1, condName.length() - 1);
                    }

                    relnInstance.addAttribute(TaAttribute.SELECT_CONDITION, "\"" + removeQuotes(condName) + "\"");
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
        } else if (line.trim().equals(Keywords.HELP) || line.trim().equals(Keywords.OTHER_HELP)) {
            dis.mark(10000);
            String inputLine = dis.readLine();
            String helpText = "";

            while (!isKeyword(inputLine.trim())) {
                helpText += inputLine.trim() + "\n";
                dis.mark(10000);
                inputLine = dis.readLine();
            }

            parentInstance.addAttribute(Keywords.HELP, "\"" + removeBracketsAndQuotes(helpText) + "\"");
            System.out.println("resetting: " + inputLine);
            dis.reset();
        } else if (line.startsWith(Keywords.IF) && parts.length == 2) {
            parseIF(parentInstance, line);
        }//default for now
        else {
            parseContainer(parentInstance, line, diagram.getRelationClass(TARelation.CONTAINS));
            System.out.println("Returned to parseentry");
        }

        System.out.println("exit parseEntryContents()");

    }

    private boolean isHelp(String line) {
        return (line.equals(Keywords.HELP) || line.equals(Keywords.OTHER_HELP));

    }

    private void checkDefPrompt(EntityInstance instance, String line) {
        String parts[] = line.trim().split("\"");

        if (parts.length > 1) {
            instance.addAttribute(TaAttribute.USER_SELECTABLE, "\"true\"");
            instance.addAttribute(TaAttribute.PROMPT, "\"" + parts[1] + "\"");
        } else {
            instance.addAttribute(TaAttribute.USER_SELECTABLE, "\"false\"");
        }

    }

    private boolean isNewEntry(String line) {

        if (line == null) {
            System.out.println("line is null");
            return false;
        }
        line = line.trim();
        String parts[] = line.split(" ");


        if ((Pattern.matches(Keywords.CONFIG + "\\s.*", line.trim()) && parts.length == 2)
                || Pattern.matches(Keywords.MENU + "\\s\".*\"", line.trim())
                || (line.startsWith(Keywords.MENUCONFIG) && parts.length == 2)
                || (line.startsWith(Keywords.IF) && parts.length == 2)
                || line.equals(Keywords.CHOICE)
                || Pattern.matches(Keywords.SOURCE + "\\s\".*\"", line.trim())) {

            return true;
        }

        return false;

    }

    private boolean isNotSource(String line) {
        return Pattern.matches(Keywords.SOURCE + "\\s\".*\"", line.trim());
    }

    private boolean isKeyword(String line) {
        String parts[] = line.trim().split(" ");
        line = line.trim();

        if (line.equals(Keywords.BOOL)
                || Pattern.matches(Keywords.BOOL + "\\s\".*\"", line)
                || line.equals(Keywords.TRISTATE)
                || Pattern.matches(Keywords.TRISTATE + "\\s\".*\"", line)
                || line.equals(Keywords.STRING) || Pattern.matches(Keywords.STRING + "\\s\".*\"", line)
                || line.equals(Keywords.INTEGER) || Pattern.matches(Keywords.INTEGER + "\\s\".*\"", line)
                || line.startsWith(Keywords.DEFAULT_BOOL)
                || (line.startsWith(Keywords.DEFAULT + " ") && parts.length > 1)
                || (line.startsWith(Keywords.DEPENDS_ON + " ") && parts.length > 1)
                || (Pattern.matches(Keywords.CONFIG + "\\s.*", line.trim()) && parts.length == 2)
                || (line.startsWith(Keywords.SELECT + " ") && parts.length > 1)
                || (line.trim().equals(Keywords.HELP)) || (line.trim().equals(Keywords.OTHER_HELP))
                || Pattern.matches(Keywords.MENU + "\\s\".*\"", line.trim())
                || (line.startsWith(Keywords.MENUCONFIG) && parts.length == 2)
                || line.startsWith(Keywords.IF) && parts.length == 2
                || line.equals(Keywords.END_CHOICE)
                || line.equals(Keywords.END_MENU)
                || line.equals(Keywords.END_IF)
                || Pattern.matches(Keywords.PROMPT + "\\s\".*\"", line)
                || line.equals(Keywords.CHOICE)
                || Pattern.matches(Keywords.SOURCE + "\\s\".*\"", line.trim())) {
            return true;
        }

        return false;
    }

    private String removeSpacesAndQuotes(String input) {
        String output = input.replaceAll("\"", " ");
        output = output.replaceAll("\\(", " ");
        output = output.replaceAll("\\)", " ");
        output = output.replaceAll("'", "");
        output = output.trim();
        return output.replaceAll(" ", "_");
    }

    private String removeBrackets(String input) {
        String output = input.replaceAll("\\(", "[");
        output = output.replaceAll("\\)", "]");
        output = output.replaceAll("'", "");
        output = output.trim();
        return output;

    }

    private String removeBracketsAndQuotes(String input) {
        return removeBrackets(removeQuotes(input));
    }

    private String removeQuotes(String input) {
        String output = input.replaceAll("\"", " ");
        output = output.replaceAll("'", "");
        output = output.trim();
        return output;
    }
}
