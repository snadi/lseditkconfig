package cmdb;



import lsedit.Ta;
import lsedit.LandscapeObject;
import lsedit.EntityClass;
import lsedit.RelationClass;
import lsedit.EntityInstance;
import lsedit.RelationInstance;
import lsedit.ResultBox;

public abstract class BasicReader {

    protected static Ta m_diagram = null;
    protected static ResultBox m_resultBox = null;

    protected static void setTitle(String text) {
        m_resultBox.addResultTitle(text);
        System.out.println(text);
    }

    protected static void report(String text) {
        m_resultBox.addText(text);
        System.out.println(text);
    }

    protected static void reportException(Throwable e) {
        String msg1;

        for (; e != null; e = e.getCause()) {
            report(e.getClass().toString());
            msg1 = e.getMessage();
            if (msg1 == null) {
                msg1 = e.toString();
            }
            report(msg1);
        }
    }

    protected static void done(String text) {
        m_resultBox.done(text);
        System.out.println(text);
    }

    protected static void setInherits(EntityClass ec, EntityClass parent) {
        if (parent != null) {
            ec.addParentClass(parent);
        }
    }

    protected static void setInherits(RelationClass rc, RelationClass parent) {
        if (parent != null) {
            rc.addParentClass(parent);
        }
    }
    private static final String[] g_root_attribute_name = {
        "label",
        "opencolor",
        "edgemode",
        "topclients",
        "wantclients",
        "wantcardinals",
        "wantoutcardinals",
        "arrow:iconrule",};
    private static final String[] g_root_attribute_value = {
        "root",
        "(255 255 255)",
        "3",
        "true",
        "true",
        "false",
        "false",
        "3",};

    protected static void addAttribute(LandscapeObject object, String id, String value) {
        object.addAttribute(id, LandscapeObject.qt(value));
    }

    protected static void addRootAttributes() {
        Ta diagram = m_diagram;
        EntityInstance root = diagram.getRootInstance();
        int i;

        if (root.getParentClass() == null) {
            root.setParentClass(diagram.getEntityBaseClass());
        }
        for (i = 0; i < g_root_attribute_name.length; ++i) {
            root.addAttribute(g_root_attribute_name[i], g_root_attribute_value[i]);
        }
    }

    protected static RelationInstance addEdge(RelationClass rc, EntityInstance from, EntityInstance to) throws Exception {
        if (rc == null) {
            throw new Exception("addEdge relation class null");
        }
        if (from == null) {
            throw new Exception("addEdge " + rc + " from null");
        }
        if (to == null) {
            throw new Exception("addEdge " + rc + " to null");
        }
        return m_diagram.addEdge(rc, from, to);
    }
    protected static RelationClass m_contains_rc;
    protected static int m_contains_order;

    protected static RelationInstance contains(EntityInstance parent, EntityInstance child) throws Exception {
        if (m_contains_rc == null) {
            m_contains_rc = m_diagram.getRelationClass(Ta.CONTAIN_ID);
            m_contains_rc.addAttribute("class_iscontains", "" + (++m_contains_order));
        }

        if (parent.hasDescendant(child)) {
            return null;
        }

        return addEdge(m_contains_rc, parent, child);
    }
    protected static int m_table_id;
    protected static EntityClass m_table_ec;

    protected static EntityInstance newContainer(String name) {
        EntityInstance table;
        EntityClass ec = m_table_ec;
        //Sarah
        if (ec == null) {
            m_table_ec = ec = m_diagram.addEntityClass("table");
            ec.addAttribute("color", "(255 51 0)");
            ec.addAttribute("class_label", "table");
            ec.addAttribute("labelcolor", "(  0 0 0)");
        }

        if (m_diagram.getCache("container:" + name) == null) {
            table = m_diagram.newCachedEntity(ec, "container:" + name);
            addAttribute(table, "label", name);
        } else {
            table = m_diagram.getCache("container:" + name);
        }

        System.out.println("returning " + table + " for " + name);
        return table;
    }

    public abstract String parseSpecialPath(Ta diagram, ResultBox resultBox, String path);
}
