package cmdb;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import lsedit.Ta;
import lsedit.LandscapeObject;
import lsedit.EntityClass;
import lsedit.RelationClass;
import lsedit.EntityInstance;
import lsedit.RelationInstance;
import lsedit.ResultBox;
import lsedit.SpecialPath;

/* This code extends LandscapeEditorFrame so that PDM ascii files can be loaded to TA
 */
 
class TableRow
{
	int			m_target = 0;
	String[]	m_fields;
	
	public TableRow(int columns)
	{
		m_fields = new String[columns];
	}
	
	public void addValue(String value)
	{
		m_fields[m_target++] = value;
	}
	
	public String getColumn(int column)
	{
		if (column >= m_fields.length || column < 0) {
			System.out.println("Row with " + m_fields.length + " fields has no column " + column);
			return null;
		} 
		return m_fields[column];
	}
}

class PdmTable
{
	String		m_name;
	Vector		m_columnNames;
	Vector		m_rows;
	TableRow	m_row;
	int			m_at              = -1;
	boolean		m_seen            = false;
	int			m_primary_key     = -1;
	boolean		m_entityInstances = true;
	
	public PdmTable(String name, int expectedColumns, int expectedRows)
	{
		m_name = name;
		m_columnNames = new Vector(expectedColumns);
		m_rows        = new Vector(expectedRows);
	}
	
	public String getName()
	{
		return m_name;
	}

	public void setPrimaryKey(int primary_key)
	{
		m_primary_key = primary_key;
	}
	
	public int getPrimaryKey()
	{
		return m_primary_key;
	}
	
	public String getPrimaryString()
	{
		String	ret;
		int		primary_key = m_primary_key;
		
		if (0 <= primary_key) {
			return getString(primary_key);
		} 
		return "" + getRowNumber();
	}
		
	public void identifyPrimaryKey()
	{
		Vector	columnNames = m_columnNames;
		int		i;

		for (i = 0; i < columnNames.size(); ++i) {
			if (((String) columnNames.elementAt(i)).equalsIgnoreCase("id")) {
				setPrimaryKey(i);
				return;
		}	}
	}
	
	public void show(PrintWriter ps)
	{
		Vector	columnNames = m_columnNames;
		int		primary_key = getPrimaryKey();
		int		i;

		ps.print("TABLE " + m_name);
		if (primary_key >= 0) {
			ps.print(" [" + getColumnName(primary_key) + "]");
		}
		ps.print(" {");
		for (i = 0; i < columnNames.size(); ++i) {
			if (i > 0) {
				ps.print(", ");
			}
			ps.print(columnNames.elementAt(i));
		}
		ps.println("}");
	}
	
	public void notEntityInstances()
	{
		m_entityInstances = false;
	}
	
	public boolean isEntityInstances()
	{
		return m_entityInstances;
	}
	
	public void addColumnName(String name)
	{
		m_columnNames.add(name);
	}
	
	public String getColumnName(int i)
	{
		return (String) m_columnNames.elementAt(i);
	}
	
	public int ordinal1(String columnName)
	{
		int		column = m_columnNames.size();
		
		for (; ; ) {
			if (--column < 0) {
				break;
			}
			if (columnName.equalsIgnoreCase(getColumnName(column))) {
				break;
		}	}
		return column;
	}
	
	public int ordinal(String columnName)
	{
		int		column = ordinal1(columnName);
		
		if (column < 0) {
			System.out.println("Table " + m_name + " has no column named '" + columnName + "' (");
			for (column = 0; column < m_columnNames.size(); ++column) {
				System.out.println(getColumnName(column));
			}
			System.out.println(")");
		}
		return column;
	}
	
	public void haveSeen()
	{
		m_seen = true;
	}
	
	public int getColumnCount()
	{
		return m_columnNames.size();
	}
	
	public boolean isSeen()
	{
		return m_seen;
	}
	
	public void addRow()
	{
		m_row = new TableRow(m_columnNames.size());
		m_rows.add(m_row);
	}
	
	public void addValue(String value)
	{
		m_row.addValue(value);
	}
	
	public String getString(int column)
	{
		return (String) m_row.getColumn(column);
	}
	
	public int getInt(int column)
	{
		String	value = getString(column);
		int		ret;
		
		try {
			ret = Integer.parseInt(value);
		} catch (Exception e) {
			System.out.println(getName() + " column=" + column + " has value='" + value + "' - Can't convert to int");
			ret = -1;
		}
		return ret;
	}
	
	private static final int sortCompare(TableRow o1, TableRow o2, int sortColumn) 
	{
		String s1 = o1.getColumn(sortColumn);
		String s2 = o2.getColumn(sortColumn);
		
		return s1.compareTo(s2);
	}
	
	private static final int partition( Vector v, int l, int r, int sortColumn)
	{
		// Arbitrarily pick the left element as the pivot element:

		TableRow p = (TableRow) v.elementAt(l);
		TableRow o;
		TableRow q;

		l--; 
		r++;

		for (;;) {
			// Figure out what's before and after the pivot:

			do {
				o = (TableRow) v.elementAt(--r);
			} while (sortCompare(o, p, sortColumn) > 0);

			do {
				q = (TableRow) v.elementAt(++l);
			} while (sortCompare(q, p, sortColumn) < 0);

			// Swap elements if we can:

			if (r <= l) {
				return r;
			}
			if (sortCompare(q, o, sortColumn) > 0) {
				v.setElementAt(o, l);
				v.setElementAt(q, r);
	}	}	}

	private static final void qsort( Vector v, int l, int r, int sortColumn)
	{
		// If we haven't reached a termination condition...

		if (l < r) {

			//	Partition the vector into left and right halves:
			int p = partition(v, l, r, sortColumn);

			//	Recursively sort each half:
			qsort(v, l,   p, sortColumn);
			qsort(v, p+1, r, sortColumn);
	}	}

	private final void sort(Vector v, int l, int r, int sortColumn)
	{
		switch(r) {
		case 0:
			return;
		case 1:
			TableRow o = (TableRow) v.elementAt(0);
			TableRow q = (TableRow) v.elementAt(1);
			if (sortCompare(o, q, sortColumn) > 0) {
				v.setElementAt(q, 0);
				v.setElementAt(o, 1);
			}
			return;
		default:
			qsort(v, l, r, sortColumn);
			return;
	}	}

	public final void sortByColumn(String columnName)
	{
		int	column = ordinal(columnName);
		
		if (column >= 0) {
			Vector	v = m_rows;
				
			sort(v, 0, v.size()-1, column);
	}	}
	
	public void resetNext()
	{
		m_at = -1;
	}
	
	public boolean next()
	{
		Vector rows = m_rows;
		
		if (rows.size() <= ++m_at) {
			return false;
		}
		m_row = (TableRow) rows.elementAt(m_at);
		return true;
	}
	
	public int getRowNumber()
	{
		return m_at;
	}

	public void noNext()
	{
		m_at = m_rows.size();
	}
}

class PdmTables extends Hashtable
{
	public PdmTables(int size)
	{
		super(size);
	}
	
	public PdmTable	getTable(String name)
	{
		Object	o = get(name);
		PdmTable table;
		
		if (o == null) {
			System.out.println("Requested table '" + name + "' not found");
			return null;
		}
		table = (PdmTable) o;
		table.resetNext();
		return table;
	}
}

public class PdmReader extends BasicReader {
	
	static PdmTables			m_tables    = new PdmTables(1000);
	static Hashtable			m_uuids     = new Hashtable(100000);

	private static boolean isUUID(String value)
	{
		int		i = value.length();
		char	c;
		
		if (i != 16 && i != 32) {
			return false;
		}
		while (--i >= 0) {
			c = value.charAt(i);
			if (c >= '0' && c <= '9') {
				continue;
			}
			if (c >= 'A' && c <= 'F') {
				continue;
			}
			return false;
		}
		return true;
	}
		
	private static EntityInstance
	newCachedEntity(EntityClass ec, String tableName, String key)
	{
		EntityInstance	e = m_diagram.newCachedEntity(ec, tableName + key);
		if (isUUID(key)) {
			m_uuids.put(key, e);
		}
		return e;
	} 
	
	private void notEntityInstances(String tableName)
	{
		PdmTable	table              = m_tables.getTable(tableName);
		
		if (table == null) {
			System.out.println("Can't find table" + tableName + " even though hasn't entities in it");
			return;
		}
		table.notEntityInstances();
	}

/*
 * ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
 key           1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 nvarchar     YES table_extension_name                    
               5 nvarchar     YES creation_user                           
               6 int          YES creation_date                           
               7 nvarchar     YES last_update_user                        
               8 int          YES last_update_date                        
               9 int          YES version_number                          
              10 nvarchar     YES description                             
              11 int          YES exclude_registration                    
              12 int          YES delete_time                             
              13 int          NO  include_reconciliation                  
              14 nvarchar     YES physical_table_name            
*/
	private static void
	load_ca_resource_family() throws Exception
	{
		String				tableName          = "ca_resource_family";
		PdmTable			table              = m_tables.getTable(tableName);
		
		if (table == null) {
			return;
		}
		int					id_column          = table.ordinal("id");
		int					inactive_column    = table.ordinal("inactive");
		int					name_column        = table.ordinal("name");
		int					description_column = table.ordinal("description");
		int					columns            = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec, parent;
		String				name, value;
		int					i;
		
		table.haveSeen();
		table.notEntityInstances();
		
		parent = diagram.addEntityClass(tableName); 

		parent.addAttribute("color", "(255 0 0)");
		parent.addAttribute("labelcolor", "(0 0 0)");
		parent.addAttribute("class_label", tableName);
		
		table.sortByColumn("name");
		
		while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			value = table.getString(id_column);	// id
			ec = diagram.addEntityClass(tableName + value);
			ec.addAttribute("color",		"(255 0 0)");
			ec.addAttribute("labelcolor",	"(  0 0 0)");

			setInherits(ec, parent);
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column || i == id_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "class_label";
					} else if (i == description_column) {
						name = "class_description";
					} 					
					addAttribute(ec, name, value);
		}	}	}
	}
	
	private static boolean m_first_resource_family;
	
	private static EntityClass
	get_ca_resource_family(String value) throws Exception
	{
		if (m_first_resource_family) {
			m_first_resource_family = false;
			load_ca_resource_family();
		}
		return m_diagram.getEntityClass("ca_resource_family" + value);
	}
		
	
/*	
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
 key           1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 int          YES family_id     -> ca_resource_family                               
               5 int          YES parent_id     -> ca_resource_class                              
               6 int          YES usp_nsm_class -> business_management_class                           
               7 nvarchar     YES creation_user                           
               8 int          YES creation_date                           
               9 nvarchar     YES last_update_user                        
              10 int          YES last_update_date                        
              11 int          YES version_number                          
              12 nvarchar     YES description                             
              13 int          YES exclude_registration                    
              14 int          YES delete_time           
*/
	
	private final static String[]	g_icon_types = {
/*   0 */	"Datacenter",
/*   1 */	"Web Farm",
/*   2 */	"Linux",
/*   3 */	"Windows",
/*   4 */	"Other Operating System",
/*   5 */	"COTS",
/*   6 */	"Projects",
/*   7 */	"Project",
/*   8 */	"Portfolio Project",
/*   9 */	"Portfolio Program",
/*  10 */	"Portfolio Idea",
/*  11 */	"Portfolio Application",
/*  12 */	"Portfolio Asset",	
/*  13 */	"Portfolio Product",
/*  14 */	"Portfolio Service",
/*  15 */	"Portfolio Work",
/*  16 */	"Managerial",
/*  17 */	"Technical",
/*  18 */	"Firewall",
/*  19 */	"Network Interface Card",
/*  20 */	"Service",
/*  21 */	"Windows OS",
/*  22 */	"Router",
/*  23 */	"Transaction",
/*  24 */	"Hard Drive",
/*  25 */	"SQL",	
/*  26 */	"Switch",
/*  27 */	"File System",
/*  28 */	"Cluster",
/*  29 */	"3270 Terminal",
/*  30 */	"Air Conditioning",
/*  31 */   "Application Security",
/*  32 */	"Application Server",
/*  33 */	"Bespoke",
/*  34 */	"Business Service",
/*  35 */	"Communication Circuit",
/*  36 */	"DB2",
/*  37 */   "Equipment Rack",
/*  38 */	"File Cabinet",
/*  39 */	"HP UX",
/*  40 */	"Infrastructure Service",
/*  41 */	"Interface",
/*  42 */	"IVR",
/*  43 */	"Legacy",
/*  44 */	"Microsoft Virtual Server",
/*  45 */	"Other Service",
/*  46 */	"Server",
/*  47 */	"Sybase",
/*  48 */	"Virtual Tape System"
	};
		
	private final static String[]	g_icon_files = {
/*   0 */	"site_32.jpg",
/*   1 */	"net_clux_32.jpg",
/*   2 */	"opsysx_32.jpg",
/*   3 */	"har_worx_32.jpg",
/*   4 */	"opsysx_32.jpg",
/*   5 */	"app_extxl_32.jpg",
/*   6 */	"projx_32.jpg",
/*   7 */	"projx_32.jpg",
/*   8 */	"projx_32.jpg",
/*   9 */	"app_inhx_32.jpg",
/*  10 */	"slax_32.jpg",
/*  11 */	"conx_32.jpg",
/*  12 */	"conx_32.jpg",
/*  13 */	"conx_32.jpg",
/*  14 */	"serx_32.jpg",
/*  15 */	"projx_32.jpg",
/*  16 */	"contx_32.jpg",
/*  17 */	"contx_32.jpg",
/*  18 */	"secx_32.jpg",
/*  19 */	"net_nicx_32.jpg",
/*  20 */	"serx_32.jpg",
/*  21 */	"opsysx_32.jpg",
/*  22 */	"net_roux_32.jpg",
/*  23 */	"xact_32.jpg",
/*  24 */	"har_stox.jpg",
/*  25 */	"dat_basx_32.jpg",
/*  26 */	"net_hubx.jpg",
/*  27 */	"fac_acx_32.jpg",
/*  28 */	"net_clux_32.jpg",
/*  29 */	"har_monx_32.jpg",
/*  30 */	"har_lparx_32.jpg",
/*  31 */	"secx_32.jpg",
/*  32 */	"har_serx_32.jpg",
/*  33 */	"app_inhx_32.jpg",
/*  34 */	"serx_32.jpg",
/*  35 */	"net_forx_32.jpg",
/*  36 */	"dat_basx_32.jpg",
/*  37 */	"tel_othx_32.jpg",
/*  38 */	"fac_furnx_32.jpg",
/*  39 */	"opsysx_32.jpg",
/*  40 */	"sersx_32.jpg",
/*  41 */	"tel_radx_32.jpg",
/*  42 */	"tel_voix_32.jpg",
/*  43 */	"default_32.jpg",
/*  44 */	"har_virx_32.jpg",
/*  45 */	"serx_32.jpg",
/*  46 */	"har_serx_32.jpg",
/*  47 */	"dat_basx_32.jpg",
/*  48 */	"fac_acx_32.jpg"


	};	
	
	private static void
	load_ca_resource_class() throws Exception
	{
		String				tableName          = "ca_resource_class";
		PdmTable			table              = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column    = table.ordinal("inactive");
		int					id_column          = table.ordinal("id");
		int					family_id_column   = table.ordinal("family_id");
		int					parent_id_column   = table.ordinal("parent_id");
		int					name_column        = table.ordinal("name");
		int					description_column = table.ordinal("description");
		int					columns            = table.getColumnCount();
		
		Ta					diagram = m_diagram;
		EntityClass			resource_class = null;
		EntityClass			ec, parent;
		int					i, j, ival;
		String				name, value;
		
		table.haveSeen();
		table.notEntityInstances();
		table.sortByColumn("name");
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			value = table.getString(id_column);	// id
			ec   = m_diagram.addEntityClass(tableName + value);
			ec.addAttribute("color",		"(255 0 0)");
			ec.addAttribute("labelcolor",	"(0 0 0)");
		
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column || i == id_column || i == family_id_column || i == parent_id_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "class_label";
			
						for (j = 0; j < g_icon_types.length; ++j) {
							if (value.equals(g_icon_types[j])) {
								addAttribute(ec, "class_icon", g_icon_files[j]);
								break;
						}	}
					} else if (i == description_column) {
						name = "class_description";
					}
					addAttribute(ec, name, value);
 			}	}
		}
			
        for (table.resetNext(); table.next(); ) {
			value  = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			
			parent = null;
			value  = table.getString(id_column);	// id
			ec	   = get_ca_resource_class(value);
			
			ival = table.getInt(parent_id_column);	// parent_id	
			if (ival >= 0) {
				parent = get_ca_resource_class("" + ival);
			} else {
				ival = table.getInt(family_id_column);// family_id
				if (ival >= 0) {
					parent = get_ca_resource_family("" + ival);
					if (parent == null) {
						report("ca_resource_class id=" + value + " family_id=" + ival + " not found in ca_resource_family");
					}
				}
				if (parent == null) {
					if (resource_class == null) {
						resource_class = m_diagram.addEntityClass(tableName);
						resource_class.addAttribute("color",		"(255 0 0)");
						resource_class.addAttribute("class_label",	"ca_resource_class");
						resource_class.addAttribute("labelcolor",	"(  0 0 0)");
					}
					parent = resource_class;				
			}	}
			setInherits(ec, parent);
		}
	}

	private static boolean m_first_resource_class;
	
	private static EntityClass
	get_ca_resource_class(String value) throws Exception
	{
		EntityClass	ec;		
		
		if (m_first_resource_class) {
			m_first_resource_class = false;
			load_ca_resource_class();
		}
			
		ec = m_diagram.getEntityClass("ca_resource_class" + value);
		if (ec == null) {
			throw new Exception("Can't find entity class " + value);
		}
		return ec;
	}

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 int          YES view_internal                           
               5 nvarchar     YES creation_user                           
               6 int          YES creation_date                           
               7 nvarchar     YES last_update_user                        
               8 int          YES last_update_date                        
               9 int          YES version_number                          
              10 nvarchar     YES description                             
              11 binary       YES user_uuid                               
              12 int          YES exclude_registration                    
              13 int          YES delete_time                             
              14 money        YES hourly_cost         
*/
	private static void
	load_ca_contact_type() throws Exception
	{
		String				tableName		   = "ca_contact_type";
		PdmTable			table              = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}

		int					inactive_column    = table.ordinal("inactive");
		int					id_column          = table.ordinal("id");
		int					name_column        = table.ordinal("name");
		int					description_column = table.ordinal("description");
		int					columns            = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			parent  = null;
		EntityClass			ec;
		String				name, value;
		int					i;
		
		table.sortByColumn("name");
		table.haveSeen();
		table.notEntityInstances();
		
		while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			if (parent == null) {
				parent = diagram.addEntityClass(tableName); 
				parent.addAttribute("color", "(255 0 0)");
				parent.addAttribute("labelcolor", "(0 0 0)");
				parent.addAttribute("class_label", tableName);
			}
			
			value = table.getString(id_column);	// id
			ec    = diagram.addEntityClass(tableName + value);
			ec.addAttribute("color",		"(0 0 255)");
			ec.addAttribute("labelcolor",	"(  0 0 0)");

			setInherits(ec, parent);
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column || i == id_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "class_label";
					} else if (i == description_column) {
						name = "class_description";
					} 
					addAttribute(ec, name, value);
		}	}	}
	}
	
	private static boolean m_first_contact_type;
	
	private static EntityClass
	get_ca_contact_type(String value) throws Exception
	{
		EntityClass	ec;		
		
		if (m_first_contact_type) {
			m_first_contact_type = false;
			load_ca_contact_type();
		}
			
		ec = m_diagram.getEntityClass("ca_contact_type" + value);
		if (ec == null) {
			throw new Exception("Can't find contact_type class " + value);
		}
		return ec;
	}
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 nvarchar     YES creation_user                           
               5 int          YES creation_date                           
               6 nvarchar     YES last_update_user                        
               7 int          YES last_update_date                        
               8 int          YES version_number                          
               9 nvarchar     YES description           
*/

	private static EntityClass m_location_ec;

	private static void
	load_ca_location_type() throws Exception
	{
		EntityClass location_ec = m_location_ec;
		
		if (location_ec != null) {
			return;
		}

		String				tableName		   = "ca_location_type";
		PdmTable			table              = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}

		int					inactive_column    = table.ordinal("inactive");
		int					id_column          = table.ordinal("id");
		int					name_column        = table.ordinal("name");
		int					description_column = table.ordinal("description");
		int					columns            = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec;
		String				name, value;
		int					i;

		table.haveSeen();
		
		m_location_ec = location_ec = diagram.addEntityClass(tableName); 
		location_ec.addAttribute("color", "(0 0 255)");
		location_ec.addAttribute("labelcolor", "(0 0 0)");
		location_ec.addAttribute("class_label", tableName);
		
		table.notEntityInstances();
		table.sortByColumn("name");
		
		while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			value = table.getString(id_column);	// id
			ec    = diagram.addEntityClass(tableName + value);
			ec.addAttribute("color",		"(255 0 0)");
			ec.addAttribute("labelcolor",	"(  0 0 0)");
			
			
			setInherits(ec, location_ec);
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column || i == id_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "class_label";
					} else if (i == description_column) {
						name = "class_description";
					} 
					addAttribute(ec, name, value);
		}	}	}
	}
	
	private static EntityClass
	get_ca_location_type(String value) throws Exception
	{
		EntityClass	ec;		
		
		if (m_location_ec == null) {
			load_ca_location_type();
		}
		if (value == null) {
			ec = m_location_ec;
		} else {
			ec = m_diagram.getEntityClass("location" + value);
			if (ec == null) {
				throw new Exception("Can't find location_type class " + value);
		}	}
		return ec;
	}
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 nvarchar     YES creation_user                           
               5 int          YES creation_date                           
               6 nvarchar     YES last_update_user                        
               7 int          YES last_update_date                        
               8 int          YES version_number                          
               9 nvarchar     YES description    
*/
 
	private static void		
	load_ca_company_type() throws Exception
	{
		String				tableName          = "ca_company_type";
		PdmTable			table              = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column    = table.ordinal("inactive");
		int					id_column          = table.ordinal("id");
		int					name_column        = table.ordinal("name");
		int					description_column = table.ordinal("description");
		int					columns            = table.getColumnCount();
		
		EntityClass			company_type = null;
		EntityClass			ec;
		int					i;
		String				name, value, type;

		table.haveSeen();
		
		company_type = m_diagram.addEntityClass(tableName);

		company_type.addAttribute("color",		"(0 255 255)");
		company_type.addAttribute("class_label",tableName);
		company_type.addAttribute("labelcolor",	"(  0 0 0)");
		
		table.notEntityInstances();
		table.sortByColumn("name");
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			value = table.getString(id_column);	// id
			ec   = m_diagram.addEntityClass(tableName + value);
			ec.addAttribute("color",		"(255 0 0)");
			ec.addAttribute("labelcolor",	"(0 0 0)");
			setInherits(ec, company_type);
		
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column || i == id_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "class_label";
					} else if (i == description_column) {
						name = "class_description";
					} 
					addAttribute(ec, name, value);
 			}	}
			setInherits(ec, company_type);
		}
	}
	
	private static EntityClass 
	get_ca_company_type(String value) throws Exception
	{
		EntityClass	ec = m_diagram.getEntityClass("company" + value);
		
		if (ec == null) {
			throw new Exception("Can't find company_type " + value);
		}
		return ec;
	}
	
/*	
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 nvarchar     YES creation_user                           
               5 int          YES creation_date                           
               6 nvarchar     YES last_update_user                        
               7 int          YES last_update_date                        
               8 int          YES version_number                          
               9 nvarchar     YES description                             
              10 int          YES exclude_registration                    
              11 int          YES delete_time  
*/
    
	private static void
	load_ca_resource_status() throws Exception
	{
		String				tableName       = "ca_resource_status";
		PdmTable			table           = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column = table.ordinal("inactive");
		int					id_column       = table.ordinal("id");
		int					name_column     = table.ordinal("name");
		int					columns         = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityInstance		e;
		EntityClass			ec = null;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			if (ec == null) {
				ec    = m_diagram.addEntityClass("resource_status");
				ec.addAttribute("color",		"(0 0 255)");
				ec.addAttribute("class_label",	"resource_status");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
			}

			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value); 
 		 
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column || i == id_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "label";
					} 
					addAttribute(e, name, value);
 		}	}	}
	}
	
	private static RelationClass m_resource_status_rc;
	
	private static void
	resource_status(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_resource_status_rc == null) {
			m_resource_status_rc = diagram.addRelationClass("resource_status");
			load_ca_resource_status();
		}
		e1 = diagram.getCache("resource_status" + value);
		if (e1 == null) {
			throw new Exception("Can't find ca_resource_status " + value);
		}
		addEdge(m_resource_status_rc, e, e1);
	}	

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 binary       NO  contact_uuid                            
               2 nvarchar     YES middle_name                             
               3 nvarchar     YES alias                                   
               4 nvarchar     NO  last_name                               
               5 nvarchar     YES first_name                              
               6 nvarchar     YES pri_phone_number                        
               7 nvarchar     YES alt_phone_number                        
               8 nvarchar     YES fax_number                              
               9 nvarchar     YES mobile_phone_number                     
              10 nvarchar     YES pager_number                            
              11 nvarchar     YES email_address                           
              12 binary       YES location_uuid                           
              13 nvarchar     YES floor_location                          
              14 nvarchar     YES pager_email_address                     
              15 nvarchar     YES room_location                           
              16 int          YES contact_type                            
              17 int          NO  inactive                                
              18 nvarchar     YES creation_user                           
              19 int          YES creation_date                           
              20 nvarchar     YES last_update_user                        
              21 int          YES last_update_date                        
              22 int          YES version_number                          
              23 int          YES department                              
              24 nvarchar     YES comments                                
              25 binary       YES company_uuid                            
              26 binary       YES organization_uuid                       
              27 binary       YES admin_organization_uuid                 
              28 nvarchar     YES alternate_identifier                    
              29 int          YES job_title                               
              30 int          YES job_function                            
              31 nvarchar     YES mail_stop                               
              32 int          YES cost_center                             
              33 nvarchar     YES userid                                  
              34 binary       YES supervisor_contact_uuid                 
              35 int          YES exclude_registration                    
              36 int          YES delete_time                             
 */
 
	private static RelationClass m_contact_rc;
	
	private static void	
	load_ca_contact() throws Exception
	{
		if (m_contact_rc != null) {
			return;
		}
		
		String				tableName                      = "ca_contact";
		PdmTable			table                          = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column                = table.ordinal("inactive");
		int					contact_type_column            = table.ordinal("contact_type");
		int					id_column                      = table.ordinal("id");
		int					last_name_column               = table.ordinal("last_name");
		int					columns                        = table.getColumnCount();

		Ta					diagram = m_diagram;
		
		table.haveSeen();
		
		m_contact_rc = diagram.addRelationClass(tableName);

		EntityClass			ec    = null;
		EntityInstance		e, parent;
		int					i;
		String				name, value;

        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			
			value = table.getString(contact_type_column);		// Contact_type
			ec    = get_ca_contact_type(value);
			value = table.getString(id_column);	
        	e     = newCachedEntity(ec, tableName, value);
        	
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column     || 
				    i == id_column           ||
				    i == contact_type_column) {
				    continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == last_name_column) {
						addAttribute(e, "label", value);
					}
					addAttribute(e, name, value);
 		}	}	}
	}

	private static void
	contact(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_contact_rc == null) {
			load_ca_contact();
		}
		e1 = diagram.getCache("contact" + value);
		if (e1 == null) {
			throw new Exception("Can't find contact " + value);
		}
		addEdge(m_contact_rc, e, e1);
	}	
	
	private static RelationClass m_primary_contact_rc;
	
	private static void
	primary_contact(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_primary_contact_rc == null) {
			m_primary_contact_rc = diagram.addRelationClass("supervisor_contact");
			load_ca_contact();
			setInherits(m_primary_contact_rc, m_contact_rc);
		}
		e1 = diagram.getCache("contact" + value);
		if (e1 == null) {
			throw new Exception("Can't find primary contact " + value);
		}
		addEdge(m_primary_contact_rc, e, e1);
	}	
	
	private static RelationClass m_supervisor_contact_rc;
	
	private static void
	supervisor_contact(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_supervisor_contact_rc == null) {
			m_supervisor_contact_rc = diagram.addRelationClass("supervisor_contact");
			load_ca_contact();
			setInherits(m_supervisor_contact_rc, m_contact_rc);
		}
		e1 = diagram.getCache("contact" + value);
		if (e1 == null) {
			throw new Exception("Can't find resource contact " + value);
		}
		addEdge(m_supervisor_contact_rc, e, e1);
	}	
	
	private static RelationClass m_resource_contact_rc;
	
	private static void
	resource_contact(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_resource_contact_rc == null) {
			m_resource_contact_rc = diagram.addRelationClass("resource_contact");
			load_ca_contact();
			setInherits(m_resource_contact_rc, m_contact_rc);
		}
		e1 = diagram.getCache("contact" + value);
		if (e1 == null) {
			throw new Exception("Can't find resource contact " + value);
		}
		addEdge(m_resource_contact_rc, e, e1);
	}	
	
	private static RelationClass m_resource_owner_rc;
	
	private static void
	resource_owner(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_resource_owner_rc == null) {
			m_resource_owner_rc = diagram.addRelationClass("resource_owner");
			load_ca_contact();
			setInherits(m_resource_owner_rc, m_contact_rc);
		}
		e1 = diagram.getCache("contact" + value);
		if (e1 == null) {
			throw new Exception("Can't find resource owner " + value);
		}
		addEdge(m_resource_status_rc, e, e1);
	}
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 binary       NO  location_uuid                           
               2 nvarchar     NO  location_name                           
               3 int          NO  inactive                                
               4 nvarchar     YES pri_phone_number                        
               5 nvarchar     YES fax_number                              
               6 nvarchar     YES address_1                               
               7 nvarchar     YES address_2                               
               8 nvarchar     YES mail_address_1                          
               9 nvarchar     YES mail_address_2                          
              10 nvarchar     YES mail_address_3                          
              11 nvarchar     YES city                                    
              12 int          YES state                                   
              13 nvarchar     YES address_3                               
              14 nvarchar     YES mail_address_6                          
              15 nvarchar     YES mail_address_5                          
              16 nvarchar     YES mail_address_4                          
              17 nvarchar     YES address_6                               
              18 nvarchar     YES address_5                               
              19 nvarchar     YES address_4                               
              20 binary       YES primary_contact_uuid                    
              21 nvarchar     YES zip                                     
              22 int          YES country                                 
              23 nvarchar     YES county                                  
              24 int          YES geo_coord_type                          
              25 nvarchar     YES geo_coords                              
              26 int          YES contact_address_flag                    
              27 nvarchar     YES creation_user                           
              28 int          YES site_id                                 
              29 int          YES creation_date                           
              30 nvarchar     YES comments                                
              31 nvarchar     YES last_update_user                        
              32 int          YES last_update_date                        
              33 int          YES version_number                          
              34 int          YES exclude_registration                    
              35 int          YES delete_time                             
              36 int          YES location_type_id                        
              37 binary       YES parent_location_uuid                    
              38 binary       YES organization_uuid                       
 */
 
	private static void	
	load_ca_location() throws Exception
	{
		String				tableName					= "ca_location";
		PdmTable			table                       = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column             = table.ordinal("inactive");
		int					location_type_id_column     = table.ordinal("location_type_id");
		int					location_uuid_column        = table.ordinal("location_uuid");
		int					location_name_column        = table.ordinal("location_name");
		int					columns                     = table.getColumnCount();
		
		Ta					diagram = m_diagram;
		EntityClass			default_location_type = null;
		
		EntityClass			ec    = null;
		EntityInstance		e, parent;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			value = table.getString(location_type_id_column);
			ec    = get_ca_location_type(value);
			value = table.getString(location_uuid_column);
        	e     = newCachedEntity(ec, tableName, value);
 		 
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column || i == location_uuid_column) { 
				     continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == location_name_column) {
						name = "label";
					} 
					addAttribute(e, name, value);
 	}	}	}	}
		
	private static RelationClass m_location_rc;
	
	private static void
	location(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_location_rc == null) {
			m_location_rc = diagram.addRelationClass("location");
			load_ca_location();
		}
		e1 = diagram.getCache("location" + value);
		if (e1 == null) {
			throw new Exception("Can't find location " + value);
		}
		addEdge(m_location_rc, e, e1);
	}	
		
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 binary       NO  company_uuid                            
               2 binary       YES parent_company_uuid                     
               3 nvarchar     YES company_name                            
               4 int          YES inactive                                
               5 nvarchar     YES description                             
               6 int          YES company_type                            
               7 nvarchar     YES alias                                   
               8 int          YES month_fiscal_year_ends                  
               9 nvarchar     YES web_address                             
              10 nvarchar     YES bbs                                     
              11 nvarchar     YES creation_user                           
              12 int          YES creation_date                           
              13 nvarchar     YES last_update_user                        
              14 binary       YES location_uuid                           
              15 binary       YES primary_contact_uuid                    
              16 int          YES version_number                          
              17 int          YES last_update_date                        
              18 int          YES exclude_registration                    
              19 int          YES delete_time                             
              20 nvarchar     YES authentication_user_name                
              21 nvarchar     YES authentication_password                 
              22 int          NO  source_type_id                          
              23 timestamp    YES auto_rep_version                        
              24 binary       YES domain_uuid                    
*/
	private static RelationClass m_company_rc;
	
	private static void
	load_ca_company() throws Exception
	{
		if (m_company_rc != null) {
			return;
		}
		
		String				tableName					= "ca_company";
		PdmTable			table                       = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column             = table.ordinal("inactive");
		int					company_type_column         = table.ordinal("company_type");
		int					company_uuid_column         = table.ordinal("company_uuid");
		int					company_name_column         = table.ordinal("company_name");
		int					columns                     = table.getColumnCount();
		
		Ta					diagram = m_diagram;
		EntityClass			ec;
		EntityInstance		e, parent;
		int					i;
		String				name, value;

		table.haveSeen();
		
		m_company_rc = diagram.addRelationClass(tableName);

		load_ca_company_type();

        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
       
			value = table.getString(company_type_column);
			ec    = get_ca_company_type(value);

			value = table.getString(company_uuid_column);
			e     = newCachedEntity(ec, tableName, value); 
 		 
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column ||
				    i == company_uuid_column ||
				    i == company_type_column) {
				    continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == company_name_column) {
						name = "label";
					} 
					addAttribute(e, name, value);
 		}	}	}
	}
	
	private static void
	company(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_company_rc == null) {
			load_ca_company();
		}
		e1 = diagram.getCache("company" + value);
		if (e1 == null) {
			throw new Exception("Can't find manufacturer " + value);
		}
		addEdge(m_company_rc, e, e1);
	}	
	
	private static RelationClass m_manufacturer_rc;
	
	private static void
	manufacturer(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_manufacturer_rc == null) {
			m_manufacturer_rc = diagram.addRelationClass("manufacturer");
			load_ca_company();
			setInherits(m_manufacturer_rc, m_company_rc);
		}
		e1 = diagram.getCache("company" + value);
		if (e1 == null) {
			throw new Exception("Can't find manufacturer " + value);
		}
		addEdge(m_manufacturer_rc, e, e1);
	}	
	
	private static RelationClass m_responsible_vendor_rc;
	
	private static void
	responsible_vendor(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_responsible_vendor_rc == null) {
			m_responsible_vendor_rc = diagram.addRelationClass("responsible_vendor");
			load_ca_company();
			setInherits(m_responsible_vendor_rc, m_company_rc);
		}
		e1 = diagram.getCache("company" + value);
		if (e1 == null) {
			throw new Exception("Can't find responsible vendor " + value);
		}
		addEdge(m_responsible_vendor_rc, e, e1);
	}	
	
	private static RelationClass m_supply_vendor_rc;
	
	private static void
	supply_vendor(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_supply_vendor_rc == null) {
			m_supply_vendor_rc = diagram.addRelationClass("supply_vendor");
			load_ca_company();
			setInherits(m_supply_vendor_rc, m_company_rc);
		}
		e1 = diagram.getCache("company" + value);
		if (e1 == null) {
			throw new Exception("Can't find supply vendor " + value);
		}
		addEdge(m_supply_vendor_rc, e, e1);
	}	
	
	private static RelationClass m_maintenance_vendor_rc;
	
	private static void
	maintenance_vendor(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_maintenance_vendor_rc == null) {
			m_maintenance_vendor_rc = diagram.addRelationClass("maintenance_vendor");
			load_ca_company();
			setInherits(m_maintenance_vendor_rc, m_company_rc);
		}
		e1 = diagram.getCache("maintenance_vendor" + value);
		if (e1 == null) {
			throw new Exception("Can't find maintenance vendor " + value);
		}
		addEdge(m_maintenance_vendor_rc, e, e1);
	}	
	
	private static RelationClass m_company_bought_for_rc;
	
	private static void
	company_bought_for(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_company_bought_for_rc == null) {
			m_company_bought_for_rc = diagram.addRelationClass("bought_for");
			load_ca_company();
			setInherits(m_company_bought_for_rc, m_company_rc);
		}
		e1 = diagram.getCache("bought_for" + value);
		if (e1 == null) {
			throw new Exception("Can't find bought_for " + value);
		}
		addEdge(m_company_bought_for_rc, e, e1);
	}	
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 binary       NO  organization_uuid                       
               2 binary       YES parent_org_uuid                         
               3 nvarchar     YES description                             
               4 nvarchar     NO  org_name                                
               5 nvarchar     YES abbreviation                            
               6 int          YES pri_phone_cc                            
               7 nvarchar     YES pri_phone_number                        
               8 int          YES alt_phone_cc                            
               9 nvarchar     YES alt_phone_number                        
              10 int          YES fax_cc                                  
              11 nvarchar     YES fax_number                              
              12 nvarchar     YES email_address                           
              13 binary       YES location_uuid                           
              14 nvarchar     YES pager_email_address                     
              15 int          NO  inactive                                
              16 nvarchar     YES creation_user                           
              17 int          YES creation_date                           
              18 nvarchar     YES last_update_user                        
              19 int          YES last_update_date                        
              20 int          YES version_number                          
              21 binary       YES company_uuid                            
              22 nvarchar     YES comments                                
              23 binary       YES contact_uuid                            
              24 int          YES cost_center                             
              25 int          YES exclude_registration                    
              26 int          YES delete_time        
 */
	private static boolean m_organization_first;
	
	private static RelationClass m_organization_rc;

	private static void	
	load_ca_organization() throws Exception
	{
		if (m_organization_rc != null) {
			return;
		}
		
		String				tableName						= "ca_organization";
		PdmTable			table                           = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column                 = table.ordinal("inactive");
		int					id_column                       = table.ordinal("id");
		int					cost_center_column              = table.ordinal("cost_center");
		int					org_name_column                 = table.ordinal("org_name");
		int					columns                         = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec        = null;
		EntityInstance		e, parent;
		int					i;
		String				name, value;

		table.haveSeen();
		
		m_organization_rc = diagram.addRelationClass(tableName);

        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
        
			if (ec == null) {
				ec = m_diagram.addEntityClass(tableName);

				ec.addAttribute("color",		"(255 0 0)");
				ec.addAttribute("class_label",tableName);
				ec.addAttribute("labelcolor",	"(  0 0 0)");
			}
        
			value = table.getString(id_column);
			e     = newCachedEntity(ec, tableName, value); 
			
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column ||
				    i == id_column ||
				    i == cost_center_column) {
				    continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == org_name_column) {
						name = "label";
					} 
 		}	}	}
		
        for (table.resetNext(); table.next(); ) {
			value  = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			parent = null;
			value  = table.getString(id_column);	
			e      = m_diagram.getCache(tableName + value);
			
			value = table.getString(cost_center_column);
			if (value != null && value.length() > 0) {
				cost_center(e, value);
			}
		}
	}

	static void	
	organization(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_organization_rc == null) {
			load_ca_organization();
		}
		e1 = diagram.getCache("organization" + value);
		if (e1 == null) {
			throw new Exception("Can't find organization " + value);
		}
		addEdge(m_organization_rc, e, e1);
	}	
	
	private static RelationClass m_admin_organization_rc;
	
	static void	
	admin_organization(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_admin_organization_rc == null) {
			m_admin_organization_rc = diagram.addRelationClass("admin_organization");
			load_ca_company();
			setInherits(m_admin_organization_rc, m_organization_rc);
		}
		e1 = diagram.getCache("organization" + value);
		if (e1 == null) {
			throw new Exception("Can't find maintenance " + value);
		}
		addEdge(m_admin_organization_rc, e, e1);
	}	
	
	private static RelationClass m_maintenance_rc;
	
	static void	
	maintenance_org(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_maintenance_rc == null) {
			m_maintenance_rc = diagram.addRelationClass("maintenance");
			load_ca_company();
			setInherits(m_maintenance_rc, m_organization_rc);
		}
		e1 = diagram.getCache("organization" + value);
		if (e1 == null) {
			throw new Exception("Can't find maintenance " + value);
		}
		addEdge(m_maintenance_rc, e, e1);
	}	
	
	private static RelationClass m_responsible_org_rc;
	
	private static void	
	responsible_org(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_responsible_org_rc == null) {
			m_responsible_org_rc = diagram.addRelationClass("responsible");
			load_ca_company();
			setInherits(m_responsible_org_rc, m_organization_rc);
		}
		e1 = diagram.getCache("organization" + value);
		if (e1 == null) {
			throw new Exception("Can't find responsible " + value);
		}
		addEdge(m_responsible_org_rc, e, e1);
	}	

	private static RelationClass m_org_bought_for_rc;
	
	private static void	
	org_bought_for(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_org_bought_for_rc == null) {
			m_org_bought_for_rc = diagram.addRelationClass("bought_for");
			load_ca_company();
			setInherits(m_org_bought_for_rc, m_organization_rc);
		}
		e1 = diagram.getCache("organization" + value);
		if (e1 == null) {
			throw new Exception("Can't find bought for " + value);
		}
		addEdge(m_org_bought_for_rc, e, e1);
	}	

/*
 * abbreviation
 * class_id,
 * creation_date,
 * creation_user,
 * current_as_of_date,
 * delete_time,
 * description,
 * exclude_registration,
 * family_id,
 * id,
 * inactive,
 * last_update_date,
 * last_update_user,
 * manufacturer_uuid,
 * name,
 * operating_system,
 * preferred_seller_uuid,
 * version_number
 */


	private static void
	load_ca_model_def() throws Exception
	{
		String				tableName          = "ca_model_def";
		PdmTable			table              = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column    = table.ordinal("inactive");	
		int					id_column          = table.ordinal("id");
		int					columns            = table.getColumnCount();

		Ta					diagram            = m_diagram;
		EntityClass			ec    = null;
		EntityInstance		e, parent;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
      
			value = table.getString(id_column);
			e     = newCachedEntity(ec, tableName, value); 
 
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column || i == id_column) {
				    continue;
				}

				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
	
	private static RelationClass m_model_rc;
	
	private static void	
	model(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_model_rc == null) {
			m_model_rc = diagram.addRelationClass("model");
			load_ca_model_def();
		}
		e1 = diagram.getCache("organization" + value);
		if (e1 == null) {
			throw new Exception("Can't find bought for " + value);
		}
		addEdge(m_model_rc, e, e1);
	}	
	
/*	
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 nvarchar     YES description                             
               5 nvarchar     YES creation_user                           
               6 int          YES creation_date                           
               7 nvarchar     YES last_update_user                        
               8 int          YES last_update_date                        
               9 int          YES version_number                          
              10 int          YES exclude_registration                    
              11 int          YES delete_time
 */
	private static void
	load_ca_resource_operating_system() throws Exception
	{
		String				tableName       = "ca_resource_operating_system";
		PdmTable			table           = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column = table.ordinal("inactive");
		int					id_column       = table.ordinal("id");
		int					name_column     = table.ordinal("name");
		int					columns         = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec    = null;
		EntityInstance		e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}

			if (ec == null) {
				ec        = diagram.getEntityClass(tableName);
			}
			value = table.getString(id_column);
			e  = newCachedEntity(ec, tableName, value);
 		 
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column ||
					i == id_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "label";
					} 
					addAttribute(e, name, value);
 		}	}	}
	}
	
	private static RelationClass m_operating_system_rc;
	
	private static void	
	operating_system(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_operating_system_rc == null) {
			m_operating_system_rc = diagram.addRelationClass("os");
			load_ca_resource_operating_system();
		}
		e1 = diagram.getCache("os" + value);
		if (e1 == null) {
			throw new Exception("Can't find operating_system " + value);
		}
		addEdge(m_operating_system_rc, e, e1);
	}

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 nvarchar     YES description                             
               5 nvarchar     YES creation_user                           
               6 int          YES creation_date                           
               7 nvarchar     YES last_update_user                        
               8 int          YES last_update_date                        
               9 int          YES version_number                          
              10 int          YES exclude_registration                    
              11 int          YES delete_time                             
              12 binary       YES organization_uuid     
 */
 
	private static void
	load_ca_resource_cost_center() throws Exception
	{
		String				tableName       = "ca_resource_cost_center";
		PdmTable			table           = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column = table.ordinal("inactive");
		int					id_column       = table.ordinal("id");
		int					name_column     = table.ordinal("name");
		int					columns         = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec    = null;
		EntityInstance		e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}

			if (ec == null) {
				ec = diagram.getEntityClass(tableName);
			}
			value = table.getString(id_column);
			e  = newCachedEntity(ec, tableName, value);
 		 
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column ||
				    i == id_column) {
				    continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "label";
					}
					addAttribute(e, name, value);
 		}	}	}
	}
	
	private static RelationClass m_cost_center_rc;

	private static void	
	cost_center(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_cost_center_rc == null) {
			m_cost_center_rc = diagram.addRelationClass("cost_center");
			load_ca_resource_cost_center();
		}
		e1 = diagram.getCache("cost_center" + value);
		if (e1 == null) {
			throw new Exception("Can't find cost_center " + value);
		}
		addEdge(m_cost_center_rc, e, e1);
	}		

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 nvarchar     YES description                             
               5 nvarchar     YES creation_user                           
               6 int          YES creation_date                           
               7 nvarchar     YES last_update_user                        
               8 int          YES last_update_date                        
               9 int          YES version_number                          
              10 int          YES exclude_registration                    
              11 int          YES delete_time             
 */
 
	private static void
	load_ca_resource_gl_code() throws Exception
	{
		String				tableName       = "ca_resource_gl_code";
		PdmTable			table           = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column = table.ordinal("inactive");
		int					id_column       = table.ordinal("id");
		int					name_column     = table.ordinal("name");
		int					columns         = table.getColumnCount();

		Ta					diagram   = m_diagram;
		EntityClass			ec        = null;
		EntityInstance		e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			if (ec == null) {
				ec = diagram.getEntityClass(tableName);
			}
			value = table.getString(id_column);
			e  = newCachedEntity(ec, tableName, value);
 		 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == inactive_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "label";
					} 
					addAttribute(e, name, value);
 		}	}	}
	}

	private static RelationClass m_gl_code_rc;
	
	private static void	
	gl_code(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_gl_code_rc == null) {
			m_gl_code_rc = diagram.addRelationClass("gl_code");
			load_ca_resource_gl_code();
		}
		e1 = diagram.getCache("gl_code" + value);
		if (e1 == null) {
			throw new Exception("Can't find gl_code " + value);
		}
		addEdge(m_gl_code_rc, e, e1);
	}
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 int          NO  inactive                                
               3 nvarchar     NO  name                                    
               4 nvarchar     YES description                             
               5 nvarchar     YES creation_user                           
               6 int          YES creation_date                           
               7 nvarchar     YES last_update_user                        
               8 int          YES last_update_date                        
               9 int          YES version_number                          
              10 int          YES exclude_registration                    
              11 int          YES delete_time                             
              12 binary       YES organization_uuid     
 */
 
	private static void
	load_ca_resource_department() throws Exception
	{
		String				tableName                = "ca_resource_department";
		PdmTable			table                    = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					id_column                = table.ordinal("id");
		int					inactive_column          = table.ordinal("inactive");
		int					name_column              = table.ordinal("name");
		int					columns                  = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec    = null;
		EntityInstance		e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}

			if (ec == null) {
				ec = diagram.getEntityClass(tableName);
			}
			value = table.getString(id_column);
			e     = newCachedEntity(ec, tableName, value);
 		 
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column ||
					i == id_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == name_column) {
						name = "label";
					} 
					addAttribute(e, name, value);
 		}	}	}
 	}

	private static RelationClass m_department_rc;
	
	private static void	
	department(EntityInstance e, String value) throws Exception
	{
		Ta				diagram = m_diagram;
		EntityInstance	e1;
		
		if (m_department_rc == null) {
			m_department_rc = diagram.addRelationClass("department");
			load_ca_resource_department();
		}
		e1 = diagram.getCache("diagram" + value);
		if (e1 == null) {
			throw new Exception("Can't department " + value);
		}
		addEdge(m_department_rc, e, e1);
	}   
              
/*
TABLE ca_owned_resource LARGE LOW_VOLATILE CA_COMMON { 
1: owned_resource_uuid UNIQUE NOT_NULL KEY ; 
2: inactive integer ; 
3: asset_type_id integer not null;
4: resource_name STRING 100 NOT_NULL ; 
5: resource_description STRING 255 ; 
6: resource_family integer REF ca_resource_family ; 
7: resource_class integer REF ca_resource_class ; 
8: resource_status integer REF ca_resource_status ; 
9: manufacturer_uuid UUID REF ca_company ;				// missing ca_company
10:responsible_vendor_uuid UUID REF ca_company ;		// missing ca_company
11:maintenance_org_uuid UUID REF ca_organization ; 
12:responsible_org_uuid UUID REF ca_organization ; 
13:org_bought_for_uuid UUID REF ca_organization ; 
14:resource_contact_uuid UUID REF ca_contact ; 
15:resource_owner_uuid UUID REF ca_contact ; 
16:location_uuid UUID REF ca_location ; 
17:floor_location STRING 30 ; 
18:room_location STRING 30 ; 
19:cabinet_location STRING 30 ; 
20:shelf_location STRING 30 ; 
21:slot_location STRING 30 ; 
22:model_uuid UUID REF ca_model_def ; 
23:host_name STRING 255 ; 
24:mac_address STRING 64 ; 
25:ip_address STRING 64 ; 
26:resource_tag STRING 64 ; 
27:operating_system integer REF ca_resource_operating_system ; 
28:product_version STRING 16 ; 
29:serial_number STRING 64 ; 
30:acquire_date LOCAL_TIME ; 
31:installation_date LOCAL_TIME ; 
32:cost_center integer REF ca_resource_cost_center ; 
33:gl_code integer REF ca_resource_gl_code ; 
34:resource_quantity integer ; 
35:requisition_id STRING 50 ; 
36:purchase_order_id STRING 20 ; 
37 :ufam integer
38:creation_user STRING 64 ; 
39:creation_date LOCAL_TIME ; 
40:last_update_user STRING 64 ; 
41:last_update_date LOCAL_TIME ; 
42:version_number integer ; 
43:supply_vendor_uuid UUID REF ca_company ; 
44:maintenance_vendor_uuid UUID REF ca_company ; 
45:company_bought_for_uuid UUID REF ca_company ; 
46:resource_capacity_unit int
47:resource_capacity
48:resource_alias STRING 30 ; 
49:asset_source_uuid UUID ; 
50:license_uuid UUID ; 
51:exclude_registration integer ; 
52 delete_time int
53:department INTEGER REF ca_resource_department ; 
54:status_date LOCAL_TIME ; 
55:license_information STRING 32 ;
56:resource_subclass int
57:audit_date int
58:exclude_registration
59:dsn_name
60:alternate_host_name
61:discovery_last_run_date
62:previous_resource_tag
63:processor_count
64:processor_speed
65:processor_speed_unit
66:processor_type
67:reconciliation_date
68:total_disk_space
69:total_disk_space_unit
70:total_memory
71:total_memory_unit
72:billing_contact_uuid
73:support_contact1_uuid
74:support_contact2_uuid
75:support_contact3_uuid
76:disaster_recovery_contact_uuid
77:backup_services_contact_uuid
78:network_contact_uuid
} 
*/

/*
(acquire_date, 
 asset_source_uuid, 
 cabinet_location, 
 company_bought_for_uuid, 
 cost_center, 
 creation_date, 
 creation_user, 
 delete_time, 
 department, 
 dns_name, 
 exclude_registration, 
 floor_location, 
 gl_code, 
 host_name, 
 id, 
 inactive, 
 installation_date, 
 ip_address, 
 last_update_date, 
 last_update_user, 
 license_information, 
 license_uuid, 
 location_uuid, 
 mac_address, 
 maintenance_org_uuid, 
 maintenance_vendor_uuid, 
 manufacturer_uuid, 
 model_uuid, 
 operating_system, 
 org_bought_for_uuid, 
 product_version, 
 purchase_order_id, 
 requisition_id, 
 resource_alias, 
 resource_class, 
 resource_contact_uuid, 
 resource_description, 
 resource_family, 
 resource_name, 
 resource_owner_uuid, 
 resource_quantity, 
 resource_status, 
 resource_tag, 
 responsible_org_uuid, 
 responsible_vendor_uuid, 
 room_location, 
 serial_number, 
 shelf_location, 
 slot_location, 
 status_date, 
 supply_vendor_uuid, 
 ufam, 
 version_number)
*/

	private static void
	load_ca_owned_resource() throws Exception
	{
		String				tableName                      = "ca_owned_resource";
		PdmTable			table                          = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					inactive_column                = table.ordinal("inactive");
		int					resource_class_column          = table.ordinal("resource_class");
		int					id_column                      = table.ordinal("id");
		int					resource_name_column           = table.ordinal("resource_name");
		int					resource_description_column    = table.ordinal("resource_description");
		int					resource_status_column         = table.ordinal("resource_status");
		int					operating_system_column        = table.ordinal("operating_system");
		int					cost_center_column             = table.ordinal("cost_center");
		int					gl_code_column                 = table.ordinal("gl_code");
		int					department_column              = table.ordinal("department");
		int					columns                        = table.getColumnCount();
		
		Ta					diagram = m_diagram;
		EntityClass			ec;
		EntityInstance		e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(inactive_column);
			if (value.equals("1")) {
				continue;
			}
			value = table.getString(resource_class_column);	// resource_class not null
			ec    = get_ca_resource_class(value);
			value = table.getString(id_column);			
			e     = newCachedEntity(ec, tableName, value);
			for (i = 0; i < columns; ++i) {
				if (i == inactive_column ||
					i == id_column ||
					i == resource_class_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == resource_status_column) {
						resource_status(e, value);
						continue;
					}
					if (i == operating_system_column) {
						operating_system(e, value);
						continue;
					}
					if (i == cost_center_column) {
						cost_center(e, value);
						continue;
					}
					if (i == gl_code_column) {
						gl_code(e, value);
						continue;
					}
					if (i == department_column) {
						department(e, value);
						continue;
					}
					
					if (i == resource_name_column) {
						name = "label";
					} else if (i == resource_description_column) {
						name = "description";
					} 
					addAttribute(e, name, value);
 		}	}	}
	}

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 int          NO  del                                     
               6 nvarchar     NO  parenttochild                           
               7 nvarchar     NO  childtoparent                           
               8 int          YES is_peer                                 
               9 nvarchar     YES nx_desc   
*/
	
	private static void
	load_ci_rel_type() throws Exception
	{
		String				tableName            = "ci_rel_type";
		PdmTable			table                = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column           = table.ordinal("del");
		int					id_column            = table.ordinal("id");
		int					desc_column          = table.ordinal("desc");
		int					parenttochild_column = table.ordinal("parenttochild");
		int					childtoparent_column = table.ordinal("childtoparent");
		int					columns              = table.getColumnCount();

		Ta					diagram = m_diagram;
		RelationClass		rc;
		String				name, value;
		int					i;
		
		table.haveSeen();
		table.notEntityInstances();
		table.sortByColumn("parenttochild");
		
		while (table.next()) {
			value = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value = table.getString(id_column);	// id
			rc = diagram.addRelationClass(tableName + value);
			rc.addAttribute("color",		"(0 0 255)");
			rc.addAttribute("labelcolor",	"(  0 0 0)");

			for (i = 0; i < columns; ++i) {
				if (i == del_column ||
				    i == id_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}

				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == desc_column) {
						name = "class_description";
					} else {
						if (i == parenttochild_column) {
							addAttribute(rc, "class_label", value);
						} else if (i == childtoparent_column) {
							addAttribute(rc, "class_rlabel", value);
						}
					}
					addAttribute(rc, name, value);
		}	}	}
	}
	
	private static boolean m_first_ci_rel_type = true;
	
	private static RelationClass
	get_ci_rel_type(String value) throws Exception
	{
		RelationClass	rc;
		
		if (m_first_ci_rel_type) {
			m_first_ci_rel_type = false;
			load_ci_rel_type();
		}
		rc = m_diagram.getRelationClass("ci_rel_type" + value);
		if (rc == null) {
			report("Can't find ci_rel_type" + value);
		}
		return rc;
	}
		
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 binary       YES hier_parent                             
               4 binary       NO  hier_child                              
               5 int          YES last_mod_dt                             
               6 binary       YES last_mod_by                             
               7 int          YES cost                                    
               8 nvarchar     NO  sym                                     
               9 nvarchar     YES nx_desc                                 
              10 int          YES bm_rep                                  
              11 int          YES ci_rel_type                             
              12 int          NO  del                                     
*/

	private static void load_business_management() throws Exception
	{
		String				tableName          = "Business_Management";
		PdmTable			table              = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column         = table.ordinal("del");
		int					ci_rel_type_column = table.ordinal("ci_rel_type");
		int					hier_parent_column = table.ordinal("hier_parent");
		int					hier_child_column  = table.ordinal("hier_child");
		int					sym_column         = table.ordinal("sym");
		int					desc_column        = table.ordinal("desc");
		int					columns            = table.getColumnCount();

		Ta					diagram = m_diagram;
		RelationClass		rc;
		EntityInstance		from, to;
		RelationInstance	ri;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value = table.getString(ci_rel_type_column);	// ci_rel_type
			if (value != null) {
				rc = get_ci_rel_type(value);
			} else {
				rc = null;
			}
			value = table.getString(hier_parent_column);	// hier_parent
			from  = (EntityInstance) m_uuids.get(value);
			if (from == null) {
				report("Hier_parent" + value + " missing");
				continue;
			}
			
			value = table.getString(hier_child_column);	// hier_child
			to    = (EntityInstance) m_uuids.get(value);
			if (to == null) {
				report("Hier_child " + value + " missing");
				continue;
			}
			ri = diagram.addEdge(rc, from, to);
			
			for (i = 0; i < columns; ++i) {
				if (i == del_column ||
					i == hier_parent_column ||
					i == hier_child_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == sym_column) {
						name = "label";
					} else if (i == desc_column) {
						name = "description";
					} else {
					}
					addAttribute(ri, name, value);
 		}	}	}
	}
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES app_id                                  
               7 nvarchar     YES portfolio                               
               8 nvarchar     YES environment                             
               9 nvarchar     YES inhouse_or_vendor                       
              10 nvarchar     YES category                                
              11 nvarchar     YES type                                    
              12 nvarchar     YES status                                  
              13 nvarchar     YES version                                 
              14 nvarchar     YES server                                  
              15 nvarchar     YES install_dir                             
              16 nvarchar     YES main_process                            
              17 nvarchar     YES storage_used                            
              18 int          YES uptime                                  
              19 int          YES response_time                           
              20 nvarchar     YES highly_avail                            
              21 nvarchar     YES highavail_appl_resources                
              22 int          YES date_installed                          
              23 nvarchar     YES support_type                            
              24 int          YES support_start_date                      
              25 int          YES support_end_date                        
              26 int          YES priority                                
              27 nvarchar     YES SLA                                     
              28 nvarchar     YES leased_or_owned_status                  
              29 nvarchar     YES proj_code                               
              30 nvarchar     YES contract_number                         
              31 int          YES lease_start_date                        
              32 int          YES lease_end_date                          
              33 int          YES lease_renewal_date                      
              34 nvarchar     YES lease_cost_per_month                    
              35 int          YES purchase_amount                         
              36 nvarchar     YES mtce_type                               
              37 nvarchar     YES maintenance_period                      
              38 nvarchar     YES mtce_contract_number                    
              39 int          YES maintenance_fee                         
              40 int          NO  del                                     
              41 nvarchar     YES purchase_amountc                    
 */
	private static void load_ci_app_ext()throws Exception
	{
		String				tableName        = "ci_app_ext";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					app_id_column    = table.ordinal("app_id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == app_id_column) {
						addAttribute(e, "label", value);
					}
					addAttribute(e, name, value);
 		}	}	}
	}
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 binary       YES base_contact                            
               7 int          NO  del                
 */
	private static void load_ci_contact()throws Exception
	{
		String				tableName        = "ci_contact";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				name = table.getColumnName(i);
				if (name.endsWith("_uuid")) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					addAttribute(e, name, value);
 		}	}	}
	}

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES db_id                                   
               7 nvarchar     YES portfolio                               
               8 nvarchar     YES environment                             
               9 nvarchar     YES type                                    
              10 nvarchar     YES status                                  
              11 nvarchar     YES version                                 
              12 nvarchar     YES support_type                            
              13 int          YES support_start_date                      
              14 int          YES support_end_date                        
              15 int          YES priority                                
              16 nvarchar     YES SLA                                     
              17 nvarchar     YES leased_or_owned_status                  
              18 nvarchar     YES proj_code                               
              19 nvarchar     YES contract_number                         
              20 int          YES lease_start_date                        
              21 int          YES lease_end_date                          
              22 int          YES lease_renewal_date                      
              23 int          YES lease_cost_per_month                    
              24 int          YES purchase_amount                         
              25 nvarchar     YES mtce_type                               
              26 nvarchar     YES maintenance_period                      
              27 nvarchar     YES mtce_contract_number                    
              28 int          YES maintenance_fee                         
              29 nvarchar     YES server                                  
              30 int          NO  del                                     
              31 nvarchar     YES purchase_amountc                        

*/
	private static void load_ci_database() throws Exception
	{
		String				tableName        = "ci_database";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          NO  del                                     
               4 int          YES last_mod_dt                             
               5 binary       YES last_mod_by                             
               6 binary       NO  ext_asset                               
               7 nvarchar     YES description                             
               8 nvarchar     YES category                                
               9 nvarchar     YES portfolio                               
              10 nvarchar     YES version                                 
              11 nvarchar     YES site                                    
              12 nvarchar     YES business_owner                          
              13 nvarchar     YES business_unit                           
              14 nvarchar     YES service_manager                         
              15 nvarchar     YES business_contacts                       
              16 nvarchar     YES escalation_contacts                     
              17 nvarchar     YES lifecycle_state                         
              18 nvarchar     YES lifecycle_status                        
              19 nvarchar     YES business_impact                         
              20 nvarchar     YES business_priority                       
              21 nvarchar     YES business_risk                           
              22 nvarchar     YES cobit_objective                         
              23 nvarchar     YES charge_code                             
              24 nvarchar     YES service_goal                            
              25 nvarchar     YES service_alignment                       
              26 nvarchar     YES SLA                                     
              27 nvarchar     YES service_hours                           
              28 int          YES design_start_date                       
              29 int          YES design_end_date                         
              30 int          YES transition_start_date                   
              31 int          YES transition_end_date                     
              32 int          YES operation_start_date                    
              33 int          YES operation_end_date                      
              34 int          YES availability_start                      
              35 int          YES availability_end                        
              36 int          YES unavailability_start                    
              37 int          YES unavailability_end                      
              38 int          YES cancel_date                             
*/

	private static void load_ci_enterprise_service()throws Exception
	{
		String				tableName        = "ci_enterprise_service";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          NO  del                                     
               4 int          YES last_mod_dt                             
               5 binary       YES last_mod_by                             
               6 binary       NO  ext_asset                               
               7 nvarchar     YES description                             
               8 nvarchar     YES category                                
               9 nvarchar     YES version                                 
              10 nvarchar     YES site                                    
              11 nvarchar     YES business_owner                          
              12 nvarchar     YES business_unit                           
              13 nvarchar     YES transaction_manager                     
              14 nvarchar     YES business_contacts                       
              15 nvarchar     YES escalation_contacts                     
              16 nvarchar     YES lifecycle_state                         
              17 nvarchar     YES lifecycle_status                        
              18 nvarchar     YES business_impact                         
              19 nvarchar     YES business_priority                       
              20 nvarchar     YES transaction_goal                        
              21 nvarchar     YES transaction_alignment                   
              22 int          YES design_start_date                       
              23 int          YES design_end_date                         
              24 int          YES transition_start_date                   
              25 int          YES transition_end_date                     
              26 int          YES operation_start_date                    
              27 int          YES operation_end_date                      
              28 int          YES availability_start                      
              29 int          YES availability_end                        
              30 int          YES unavailability_start                    
              31 int          YES unavailability_end                      
              32 int          YES cancel_date        
 */
 
	private static void load_ci_enterprise_transaction()throws Exception
	{
		String				tableName        = "ci_enterprise_transaction";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 int          YES last_mtce_date                          
               7 nvarchar     YES mtce_level                              
               8 int          YES active_date                             
               9 int          YES retire_date                             
              10 int          YES priority                                
              11 nvarchar     YES SLA                                     
              12 nvarchar     YES leased_or_owned_status                  
              13 nvarchar     YES proj_code                               
              14 nvarchar     YES contract_number                         
              15 int          YES lease_start_date                        
              16 int          YES lease_end_date                          
              17 int          YES lease_renewal_date                      
              18 nvarchar     YES lease_cost_per_month                    
              19 int          YES purchase_amount                         
              20 nvarchar     YES mtce_type                               
              21 nvarchar     YES maintenance_period                      
              22 nvarchar     YES mtce_contract_number                    
              23 int          YES maintenance_fee                         
              24 int          NO  del                                     
              25 nvarchar     YES purchase_amountc 
  */                
              	
	private static void load_ci_fac_ac()throws Exception
	{
		String				tableName        = "ci_fac_ac";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					id_column        = table.ordinal("id");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES phys_mem                                
               7 nvarchar     YES mem_capacity                            
               8 nvarchar     YES hard_drive_capacity                     
               9 nvarchar     YES proc_type                               
              10 nvarchar     YES proc_speed                              
              11 nvarchar     YES disk_type                               
              12 nvarchar     YES cd_rom_type                             
              13 nvarchar     YES net_card                                
              14 nvarchar     YES monitor                                 
              15 nvarchar     YES printer                                 
              16 nvarchar     YES technology                              
              17 int          YES number_slot_proc                        
              18 int          YES number_proc_inst                        
              19 nvarchar     YES mem_cache_proc                          
              20 nvarchar     YES slot_total_mem                          
              21 nvarchar     YES slot_mem_used                           
              22 nvarchar     YES type_net_conn                           
              23 nvarchar     YES number_net_port                         
              24 nvarchar     YES number_net_port_conn                    
              25 nvarchar     YES number_net_card                         
              26 nvarchar     YES bios_ver                                
              27 nvarchar     YES number_mips                             
              28 nvarchar     YES role                                    
              29 nvarchar     YES supervision_mode                        
              30 nvarchar     YES server_type                             
              31 int          YES processor_count                         
              32 nvarchar     YES swap_size                               
              33 nvarchar     YES security_patch_level                    
              34 int          YES active_date                             
              35 int          YES retire_date                             
              36 int          YES priority                                
              37 nvarchar     YES SLA                                     
              38 nvarchar     YES leased_or_owned_status                  
              39 nvarchar     YES proj_code                               
              40 nvarchar     YES contract_number                         
              41 int          YES lease_effective_date                    
              42 int          YES lease_termination_date                  
              43 int          YES lease_renewal_date                      
              44 nvarchar     YES lease_cost_per_month                    
              45 int          YES purchase_amount                         
              46 nvarchar     YES mtce_type                               
              47 nvarchar     YES maintenance_period                      
              48 nvarchar     YES mtce_contract_number                    
              49 int          YES maintenance_fee                         
              50 int          NO  del                                     
              51 nvarchar     YES purchase_amountc  
  */
  	
	private static void load_ci_hardware_server()throws Exception
	{
		String				tableName        = "ci_hardware_server";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES disk_type                               
               7 nvarchar     YES media_type                              
               8 nvarchar     YES media_drive_num                         
               9 nvarchar     YES total_capacity                          
              10 nvarchar     YES used_space                              
              11 nvarchar     YES array_name                              
              12 nvarchar     YES array_serial_num                        
              13 int          YES active_date                             
              14 int          YES retire_date                             
              15 int          YES priority                                
              16 nvarchar     YES SLA                                     
              17 nvarchar     YES leased_or_owned_status                  
              18 nvarchar     YES proj_code                               
              19 nvarchar     YES contract_number                         
              20 int          YES lease_effective_date                    
              21 int          YES lease_termination_date                  
              22 int          YES lease_renewal_date                      
              23 nvarchar     YES lease_cost_per_month                    
              24 int          YES purchase_amount                         
              25 nvarchar     YES mtce_type                               
              26 nvarchar     YES maintenance_period                      
              27 nvarchar     YES mtce_contract_number                    
              28 int          YES maintenance_fee                         
              29 int          NO  del                                     
              30 nvarchar     YES purchase_amountc      
 */
 
	private static void load_ci_hardware_storage()throws Exception
	{
		String				tableName        = "ci_hardware_storage";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		
		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES phys_mem                                
               7 nvarchar     YES mem_capacity                            
               8 nvarchar     YES hard_drive_capacity                     
               9 nvarchar     YES proc_type                               
              10 nvarchar     YES proc_speed                              
              11 nvarchar     YES disk_type                               
              12 nvarchar     YES media_type                              
              13 nvarchar     YES bios_ver                                
              14 nvarchar     YES number_mips                             
              15 nvarchar     YES security_patch_level                    
              16 int          YES virtual_processors                      
              17 nvarchar     YES processor_affinity                      
              18 nvarchar     YES cpu_shares                              
              19 nvarchar     YES memory_shares                           
              20 int          YES active_date                             
              21 int          YES retire_date                             
              22 int          YES priority                                
              23 nvarchar     YES SLA                                     
              24 nvarchar     YES leased_or_owned_status                  
              25 nvarchar     YES proj_code                               
              26 nvarchar     YES contract_number                         
              27 int          YES lease_effective_date                    
              28 int          YES lease_termination_date                  
              29 int          YES lease_renewal_date                      
              30 nvarchar     YES lease_cost_per_month                    
              31 int          YES purchase_amount                         
              32 nvarchar     YES mtce_type                               
              33 nvarchar     YES maintenance_period                      
              34 nvarchar     YES mtce_contract_number                    
              35 int          YES maintenance_fee                         
              36 int          NO  del                                     
              37 nvarchar     YES purchase_amountc      
 */
 
	private static void load_ci_hardware_virtual()throws Exception
	{
		String				tableName        = "ci_hardware_virtual";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == del_column ||
				    i == id_column ||
				    i == ext_asset_column) {
				    continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 int          NO  del                                     
               3 nvarchar     YES description                             
               4 nvarchar     YES hostname                                
               5 nvarchar     YES mdr_class                               
               6 nvarchar     NO  mdr_name                                
               7 nvarchar     NO  name                                    
               8 binary       YES owner                                   
               9 nvarchar     YES password                                
              10 nvarchar     YES encryptiontype                          
              11 nvarchar     YES encryptedpassword                       
              12 nvarchar     YES path                                    
              13 nvarchar     YES parameters                              
              14 int          YES port                                    
              15 nvarchar     YES launchurl                               
              16 int          YES last_mod_dt                             
              17 binary       YES last_mod_by                             
              18 nvarchar     YES userid                                  
              19 nvarchar     YES persid    
*/

	private static void load_ci_mdr_provider()throws Exception
	{
		String				tableName       = "ci_mdr_provider";
		PdmTable			table           = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column      = table.ordinal("del");
		int					id_column       = table.ordinal("id");
		int					mdr_name_column = table.ordinal("mdr_name");
		int					columns         = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		EntityInstance		e;
		int					i;
		String				name, value;

		table.haveSeen();
		table.setPrimaryKey(id_column);
		
        while (table.next()) {
			value = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
        
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 255)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
			}				
			
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == mdr_name_column) {
						addAttribute(e, "label", value);
					}
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}	

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES federated_asset_id	// ????                    
               3 int          NO  mdr_provider_id                         
               4 binary       YES cmdb_asset_id                           
               5 int          YES last_mod_dt                             
               6 binary       YES last_mod_by                             
               7 int          NO  del                                     
               8 nvarchar     YES persid       
*/
	private void load_ci_mdr_idmap() throws Exception
	{
		String				tableName              = "ci_mdr_idmap";
		PdmTable			table                  = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column             = table.ordinal("del");
		int					cmdb_asset_id_column   = table.ordinal("cmdb_asset_id");
		int					mdr_provider_id_column = table.ordinal("mdr_provider_id");
		int					columns                = table.getColumnCount();

		Ta					diagram = m_diagram;
		RelationClass		rc = null;
		EntityInstance		e1, e2;
		RelationInstance	ri;
		int					i;
		String				name, value;

		table.haveSeen();
		table.notEntityInstances();

        while (table.next()) {
			value = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value = table.getString(cmdb_asset_id_column);	// ext_asset
			e1    = (EntityInstance) m_uuids.get(value);
			if (e1 == null) {
				report(tableName + " missing cmdb_asset_id " + value);
				continue;
			}
			value = table.getString(mdr_provider_id_column);	// mdr_provider_id
			e2    = diagram.getCache("ci_mdr_provider" + value);
			if (e2 == null) {
				report(tableName + " missing mdr_provider_id " + value);
				continue;
			}
			
			if (rc == null) {
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("color", "(153 0 153)");
			}
			ri = addEdge(rc, e1, e2);

			for (i = 0; i < columns; ++i) {
				if (i == mdr_provider_id_column ||
					i == cmdb_asset_id_column ||
					i == del_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(ri, name, value);
 		}	}	}
	}

/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES network_name                            
               7 nvarchar     YES network_address                         
               8 nvarchar     YES gateway_id                              
               9 nvarchar     YES channel_address                         
              10 nvarchar     YES os_version                              
              11 nvarchar     YES virtual_ip                              
              12 nvarchar     YES quorum                                  
              13 int          YES last_mtce_date                          
              14 nvarchar     YES mtce_level                              
              15 nvarchar     YES security_patch_level                    
              16 int          YES active_date                             
              17 int          YES retire_date                             
              18 int          YES priority                                
              19 nvarchar     YES SLA                                     
              20 nvarchar     YES leased_or_owned_status                  
              21 nvarchar     YES proj_code                               
              22 nvarchar     YES contract_number                         
              23 int          YES lease_effective_date                    
              24 int          YES lease_termination_date                  
              25 int          YES lease_renewal_date                      
              26 nvarchar     YES lease_cost_per_month                    
              27 int          YES purchase_amount                         
              28 nvarchar     YES mtce_type                               
              29 nvarchar     YES maintenance_period                      
              30 nvarchar     YES mtce_contract_number                    
              31 int          YES maintenance_fee                         
              32 int          NO  del                                     
              33 nvarchar     YES purchase_amountc                      
*/
	private static void load_ci_network_cluster()throws Exception
	{
		String				tableName        = "ci_network_cluster";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES network_name                            
               7 nvarchar     YES network_address                         
               8 nvarchar     YES gateway_id                              
               9 nvarchar     YES addr_class                              
              10 nvarchar     YES subnet_mask                             
              11 nvarchar     YES technology                              
              12 int          YES number_ports                            
              13 int          YES number_ports_used                       
              14 nvarchar     YES type_net_conn                           
              15 nvarchar     YES number_net_port                         
              16 nvarchar     YES number_net_port_conn                    
              17 nvarchar     YES number_net_card                         
              18 nvarchar     YES role                                    
              19 nvarchar     YES ip_mgmt_addr                            
              20 nvarchar     YES os_version                              
              21 int          YES last_mtce_date                          
              22 nvarchar     YES mtce_level                              
              23 int          YES active_date                             
              24 int          YES retire_date                             
              25 int          YES priority                                
              26 nvarchar     YES SLA                                     
              27 nvarchar     YES leased_or_owned_status                  
              28 nvarchar     YES proj_code                               
              29 nvarchar     YES contract_number                         
              30 int          YES lease_effective_date                    
              31 int          YES lease_termination_date                  
              32 int          YES lease_renewal_date                      
              33 nvarchar     YES lease_cost_per_month                    
              34 int          YES purchase_amount                         
              35 nvarchar     YES mtce_type                               
              36 nvarchar     YES maintenance_period                      
              37 nvarchar     YES mtce_contract_number                    
              38 int          YES maintenance_fee                         
              39 int          NO  del                                     
              40 nvarchar     YES purchase_amountc                        
*/
	private static void load_ci_network_hub()throws Exception
	{
		String				tableName        = "ci_network_hub";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES network_name                            
               7 nvarchar     YES network_address                         
               8 nvarchar     YES gateway_id                              
               9 nvarchar     YES addr_class                              
              10 nvarchar     YES subnet_mask                             
              11 nvarchar     YES technology                              
              12 int          YES number_ports                            
              13 int          YES number_ports_used                       
              14 nvarchar     YES type_net_conn                           
              15 nvarchar     YES number_net_port                         
              16 nvarchar     YES number_net_port_conn                    
              17 nvarchar     YES number_net_card                         
              18 nvarchar     YES role                                    
              19 nvarchar     YES ip_mgmt_addr                            
              20 nvarchar     YES line_type                               
              21 nvarchar     YES line_speed                              
              22 nvarchar     YES protocol                                
              23 int          YES last_mtce_date                          
              24 nvarchar     YES mtce_level                              
              25 int          YES active_date                             
              26 int          YES retire_date                             
              27 int          NO  del                                     
              28 nvarchar     YES purchase_amountc                        
*/
	private static void load_ci_network_nic()throws Exception
	{
		String				tableName        = "ci_network_nic";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES network_name                            
               7 nvarchar     YES network_address                         
               8 nvarchar     YES gateway_id                              
               9 nvarchar     YES rout_prot                               
              10 nvarchar     YES addr_class                              
              11 nvarchar     YES flow                                    
              12 nvarchar     YES subnet_mask                             
              13 nvarchar     YES modem_type                              
              14 nvarchar     YES modem_card                              
              15 nvarchar     YES technology                              
              16 int          YES number_ports                            
              17 int          YES number_ports_used                       
              18 int          YES number_proc_inst                        
              19 nvarchar     YES mem_cache_proc                          
              20 nvarchar     YES slot_total_mem                          
              21 nvarchar     YES slot_mem_used                           
              22 nvarchar     YES type_net_conn                           
              23 nvarchar     YES number_net_port                         
              24 nvarchar     YES number_net_port_conn                    
              25 nvarchar     YES number_net_card                         
              26 nvarchar     YES role                                    
              27 int          YES processor_count                         
              28 nvarchar     YES ip_mgmt_addr                            
              29 nvarchar     YES protocol                                
              30 nvarchar     YES os_version                              
              31 int          YES last_mtce_date                          
              32 nvarchar     YES mtce_level                              
              33 int          YES active_date                             
              34 int          YES retire_date                             
              35 int          YES priority                                
              36 nvarchar     YES SLA                                     
              37 nvarchar     YES leased_or_owned_status                  
              38 nvarchar     YES proj_code                               
              39 nvarchar     YES contract_number                         
              40 int          YES lease_effective_date                    
              41 int          YES lease_termination_date                  
              42 int          YES lease_renewal_date                      
              43 nvarchar     YES lease_cost_per_month                    
              44 int          YES purchase_amount                         
              45 nvarchar     YES mtce_type                               
              46 nvarchar     YES maintenance_period                      
              47 nvarchar     YES mtce_contract_number                    
              48 int          YES maintenance_fee                         
              49 int          NO  del                                     
              50 nvarchar     YES purchase_amountc                        
*/
	private static void load_ci_network_router()throws Exception
	{
		String				tableName        = "ci_network_router";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES os_id                                   
               7 nvarchar     YES environment                             
               8 nvarchar     YES type                                    
               9 nvarchar     YES status                                  
              10 nvarchar     YES version                                 
              11 int          YES date_installed                          
              12 nvarchar     YES support_type                            
              13 int          YES support_start_date                      
              14 int          YES support_end_date                        
              15 int          YES priority                                
              16 nvarchar     YES SLA                                     
              17 nvarchar     YES leased_or_owned_status                  
              18 nvarchar     YES proj_code                               
              19 nvarchar     YES contract_number                         
              20 int          YES lease_effective_date                    
              21 int          YES lease_termination_date                  
              22 int          YES lease_renewal_date                      
              23 nvarchar     YES lease_cost_per_month                    
              24 int          YES purchase_amount                         
              25 nvarchar     YES mtce_type                               
              26 nvarchar     YES maintenance_period                      
              27 nvarchar     YES mtce_contract_number                    
              28 int          YES maintenance_fee                         
              29 nvarchar     YES server                                  
              30 int          NO  del                                     
              31 nvarchar     YES purchase_amountc                        
*/	
	private static void load_ci_operating_system()throws Exception
	{
		String				tableName        = "ci_operating_system";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
	
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES security_id                             
               7 nvarchar     YES integrity_level                         
               8 nvarchar     YES avail                                   
               9 nvarchar     YES confidentiality_level                   
              10 nvarchar     YES appl                                    
              11 int          YES priority                                
              12 nvarchar     YES SLA                                     
              13 int          NO  del    
*/                                 
	private static void load_ci_security()throws Exception
	{
		String				tableName        = "ci_security";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == ext_asset_column ||
					i == del_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES service_id                              
               7 nvarchar     YES portfolio                               
               8 nvarchar     YES category                                
               9 nvarchar     YES type                                    
              10 nvarchar     YES status                                  
              11 nvarchar     YES version                                 
              12 nvarchar     YES site                                    
              13 int          YES start_date                              
              14 int          YES end_date                                
              15 int          YES priority                                
              16 nvarchar     YES SLA                                     
              17 int          NO  del                
*/
	private static void load_ci_service()throws Exception
	{
		String				tableName        = "ci_service";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 nvarchar     YES persid                                  
               3 int          YES last_mod_dt                             
               4 binary       YES last_mod_by                             
               5 binary       NO  ext_asset                               
               6 nvarchar     YES network_name                            
               7 nvarchar     YES network_address                         
               8 nvarchar     YES phone_number                            
               9 nvarchar     YES carrier                                 
              10 nvarchar     YES circuit_number                          
              11 nvarchar     YES gateway_id                              
              12 nvarchar     YES subnet_mask                             
              13 nvarchar     YES domain                                  
              14 nvarchar     YES bios_ver                                
              15 nvarchar     YES line_id                                 
              16 nvarchar     YES server_id                               
              17 nvarchar     YES cpu_type                                
              18 nvarchar     YES memory_available                        
              19 nvarchar     YES memory_used                             
              20 nvarchar     YES harddrive_capacity                      
              21 nvarchar     YES harddrive_used                          
              22 nvarchar     YES monitor                                 
              23 nvarchar     YES nic_card                                
              24 nvarchar     YES bandwidth                               
              25 nvarchar     YES frequency                               
              26 nvarchar     YES license_number                          
              27 int          YES license_expiration_date                 
              28 int          YES last_mtce_date                          
              29 nvarchar     YES mtce_level                              
              30 int          YES active_date                             
              31 int          YES retire_date                             
              32 int          YES priority                                
              33 nvarchar     YES SLA                                     
              34 nvarchar     YES leased_or_owned_status                  
              35 nvarchar     YES proj_code                               
              36 nvarchar     YES contract_number                         
              37 int          YES lease_effective_date                    
              38 int          YES lease_termination_date                  
              39 int          YES lease_renewal_date                      
              40 nvarchar     YES lease_cost_per_month                    
              41 int          YES purchase_amount                         
              42 nvarchar     YES mtce_type                               
              43 nvarchar     YES maintenance_period                      
              44 nvarchar     YES mtce_contract_number                    
              45 int          YES maintenance_fee                         
              46 int          NO  del                                     
              47 nvarchar     YES purchase_amountc                        
*/
	private static void load_ci_telcom_other()throws Exception
	{
		String				tableName        = "ci_telcom_other";
		PdmTable			table            = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					del_column       = table.ordinal("del");
		int					ext_asset_column = table.ordinal("ext_asset");
		int					id_column        = table.ordinal("id");
		int					columns          = table.getColumnCount();


		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		EntityInstance		parent, e;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(del_column);
			if (value.equals("1")) {
				continue;
			}
			value  = table.getString(ext_asset_column);	// ext_asset
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == id_column ||
					i == del_column ||
					i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
/*
ordinal_position TYPE         NUL NAME                                    
---------------- ------------ --- ----------------------------------------
               1 int          NO  id                                      
               2 binary       NO  com_par_id                              
               3 int          NO  com_dt                                  
               4 nvarchar     NO  com_userid                              
               5 nvarchar     YES com_comment                             
               6 binary       YES writer_id                               
               7 nvarchar     YES attr_name                               
               8 nvarchar     YES old_value                               
               9 nvarchar     YES new_value                               
              10 nvarchar     YES mdr_name                                
              11 nvarchar     YES mdr_class         
 */		
 	private static void load_nr_com()throws Exception
	{
		String				tableName          = "nr_com";
		PdmTable			table              = m_tables.getTable(tableName);
		if (table == null) {
			return;
		}
		int					com_par_id_column  = table.ordinal("com_par_id");
		int					id_column          = table.ordinal("id");
		int					com_comment_column = table.ordinal("com_comment");
		int					writer_id_column   = table.ordinal("writer_id");
		int					columns            = table.getColumnCount();

		Ta					diagram = m_diagram;
		EntityClass			ec = null;
		RelationClass		rc = null;
		RelationClass		writer_rc = null;
		EntityInstance		parent, e, writer;
		int					i;
		String				name, value;

		table.haveSeen();
		
        while (table.next()) {
			value  = table.getString(com_par_id_column);	// com_par_id
			parent = diagram.getCache(value);
			if (parent == null) {
				report(tableName + " missing " + value);
				continue;
			}
			
			if (ec == null) {
				ec = diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 255 0)");
				ec.addAttribute("labelcolor",	"(  0 0 0)");
				ec.addAttribute("class_icon",	"conx_32.jpg");
				
				rc = diagram.addRelationClass(tableName);
				rc.addAttribute("class_iscontains", "" + (++m_contains_order));
			}
			value = table.getString(id_column);	// id
			e     = newCachedEntity(ec, tableName, value);

			addEdge(rc, parent, e); 
			for (i = 0; i < columns; ++i) {
				if (i == com_par_id_column ||
					i == id_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (i == com_comment_column) {
						addAttribute(e, "description", value);
						continue;
					}
					if (i == writer_id_column) {
						writer = diagram.getCache("contact" + value);
						if (writer == null) {
							report(table + " missing writer " + value);
							continue;
						}
			
						if (writer_rc == null) {
							writer_rc = diagram.addRelationClass("writer");
						}
						addEdge(writer_rc, e, writer); 
						continue;
					}
					name = table.getColumnName(i);
					addAttribute(e, name, value);
 		}	}	}
	}
	
	public void dump(PdmTable table) throws Exception
	{
		String	tableName        = table.getName();
		String	tableName1       = tableName;
		int		inactive_column  = table.ordinal1("inactive");
		int		id_column        = table.ordinal1("id");
		int		ext_asset_column = table.ordinal1("ext_asset");
		int		label_column     = table.ordinal1("name");		
		int		columns          = table.getColumnCount();

		if (tableName.startsWith("ca_")) {
			tableName1 = tableName.substring(3);
		}
		if (inactive_column < 0) {
			inactive_column = table.ordinal1("del");
		}
				
		Ta					diagram   = m_diagram;
		EntityClass			ec        = null;
		RelationClass		rc        = null;
		EntityInstance		parent;
		EntityInstance		e;
		String				name, value;
		int					i;
			
		table.haveSeen();
		
		while (table.next()) {
			if (inactive_column >= 0) {
				value = table.getString(inactive_column);
				if (value.equals("1")) {
					continue;
			}	}
			if (ec == null) {
				ec = m_diagram.addEntityClass(tableName);
				ec.addAttribute("color",		"(0 0 255)");
				ec.addAttribute("class_label",	tableName);
				ec.addAttribute("labelcolor",	"(  0 0 0)");
			}
			value = table.getString(id_column);	
			e = newCachedEntity(ec, tableName, value); 

			for (i = 0; i < columns; ++i) {
				if (i == inactive_column || i == id_column || i == ext_asset_column) {
					continue;
				}
				value = table.getString(i);
				if (value != null && value.length() > 0) {
					if (isUUID(value)) {
						continue;
					}
					name = table.getColumnName(i);
					if (i == label_column) {
						name = "label";
					}
					try {
						addAttribute(e, name, value);
					} catch (Exception exception) {
						System.out.println("Can't insert attribute " + name + "=" + value + " in " + tableName); 
					}
		}	}	}
	}	
	
	static	EntityClass		m_tables_class = null;
	static	RelationClass	m_extAsset     = null;
	
	public void addEdges(PdmTable table, PdmTable controlled) throws Exception
	{
		String	tableName        = table.getName();
		int		ext_asset_column = table.ordinal1("ext_asset");
		String	tableName1       = tableName;
		int		inactive_column  = table.ordinal1("inactive");
		int		primary_column   = table.getPrimaryKey();
		int		columns          = table.getColumnCount();
		String	color            = "(0 0 255)";
		boolean	special          = false;
		
		if (tableName.startsWith("ca_")) {
			tableName1 = tableName.substring(3);
			if (tableName.equals("ca_owned_resource")) {
				color = "(255 0 0)";
				special = true;
			}
		}
		if (inactive_column < 0) {
			inactive_column = table.ordinal1("del");
		}
				
		Ta					diagram   = m_diagram;
		EntityClass			ec        = null;
		RelationClass		rc;
		EntityInstance		container = null;
		String				columnName;
		String				relationName;
		EntityInstance		parent;
		EntityInstance		e, e1;
		String				name, value;
		int					i;
		
		if (0 <= ext_asset_column) {
			rc = m_extAsset;
			for (table.resetNext(); table.next(); ) {
				if (0 <= inactive_column) {
					value = table.getString(inactive_column);
					if (value.equals("1")) {
						continue;
				}	}
				
				value = table.getString(ext_asset_column);
				if (value == null || value.length() == 0) {
					continue;
				}
				e1    = (EntityInstance) m_uuids.get(value);
				if (e1 == null) {
					report(tableName + " missing ext_asset=" + value);
					continue;
				}
				value = table.getPrimaryString();
				e     = diagram.getCache(tableName + value);
				if (e == null) {
					report(tableName + " missing primary key=" + value);
				} 
				if (rc == null) {
					m_extAsset = rc = diagram.addRelationClass("ext_asset");
					rc.addAttribute("class_iscontains", "" + (++m_contains_order));
				}
				addEdge(rc, e1, e);
			}
		} else {
			
			e1 = null;
			for (table.resetNext(); table.next(); ) {
				if (0 <= inactive_column) {
					value = table.getString(inactive_column);
					if (value.equals("1")) {
						continue;
				}	}
				
				value = table.getPrimaryString();
				e     = diagram.getCache(tableName + value);
				if (e == null) {
					report(tableName + " missing primary key=" + value);
					continue;
				} 
				ec    = m_tables_class;
				if (ec == null) {
					m_tables_class = ec = m_diagram.addEntityClass("table");
					ec.addAttribute("color",		color);
					ec.addAttribute("class_label",	"table");
					ec.addAttribute("labelcolor",	"(  0 0 0)");
				}
				if (e1 == null) {
					e1     = diagram.newCachedEntity(ec, tableName);
					if (special) {
						e1.addAttribute("color",		color);
					}
					if (controlled != null && controlled != table) {
						int	sym_column = controlled.ordinal1("sym");
						if (0 <= sym_column) {
							for (controlled.resetNext(); controlled.next(); ) {
								value = controlled.getString(sym_column);
								if (tableName.equals(value)) {
									int desc_column = controlled.ordinal1("desc");
									if (0 <= desc_column) {
										value = controlled.getString(desc_column);
										if (value != null) {
											e1.addAttribute("description", value);
									}	}
									break;
					}	}	}	}
				}
				contains(e1, e);
		}	}
						
		for (i = 0; i < columns; ++i) {
			if (i == primary_column || i == inactive_column) {
				continue;
			}
			columnName = table.getColumnName(i);
			rc         = null;
			for (table.resetNext(); table.next(); ) {
				if (inactive_column >= 0) {
					value = table.getString(inactive_column);
					if (value.equals("1")) {
						continue;
				}	}
 				value  = table.getString(i);
 				if (value.length() == 0) {
 					continue;
 				}
 				if (!isUUID(value)) {
 					table.noNext();
 					continue;
 				}
				e1 = (EntityInstance) m_uuids.get(value);
				if (e1 == null) {
					report(tableName + "." + columnName + "=" + value + " not found");
					continue;
				}
				value = table.getString(primary_column);
				e     = diagram.getCache(tableName + value);
				if (e == null) {
					report(tableName + " missing primary key=" + value);
				} 
				if (rc == null) {
					relationName = tableName + "." + columnName;
					rc = diagram.addRelationClass(relationName);
					rc.addAttribute("color", "(0 153 0)");
				}
				addEdge(rc, e, e1);
		}	}	
	}	

	private static boolean readInput(String fileName)
	{
		final String[]		states = {
		/*  0 */	"^TABLE (Start state)",
		/*  1 */	"T^ABLE",
		/*  2 */	"TA^BLE",
		/*  3 */	"TAB^LE",
		/*  4 */	"TABL^E",
		/*  5 */	"TABLE^\\b",
		/*  6 */	"TABLE\\b^<tablename>\\n",
		/*  7 */	"^<columname> | {",
		/*  8 */	"^columnname",
		/*  9 */	"^<value>",
		/* 10 */	"^\"value\"",
		/* 11 */	"\"value\"^",
		/* 12 */	"{..}^"
		};
		
		File				file         = null;
		FileInputStream		is           = null;
		InputStreamReader	reader       = null;
		LineNumberReader	linenoReader = null;
		PdmTable			table        = null;
		StringBuilder		text         = new StringBuilder(10000);
		int					state        = 0;
		int					c            = '\n';
		boolean				ret;
		
		try {
			file		 = new File(fileName);
			is			 = new FileInputStream(file);
			reader		 = new InputStreamReader(is);
			linenoReader = new LineNumberReader(reader);
			
			boolean				comment   = false;
			int					lastc;

			linenoReader.setLineNumber(1);
			
			for (;;) {
				lastc = c;
				c     = linenoReader.read();
				if (c == -1) {
					break;
				}
				switch (c) {
				case '#':
					if (lastc == '\n') {
						comment = true;
					}
					break;
				case '\r':
					continue;
				case '\n':
					if (comment) {
						comment = false;
						continue;
					}
					break;
				}
				if (comment) {
					continue;
				}
				
				switch (state) {
				case 0:		// Start state
					switch (c) {
					case 'T':
						state = 1;
						continue;
					}
					break;
				case 1:		// seen T[ABLE]
					if (c == 'A') {
						state = 2;
						continue;
					}
					break;
				case 2:		// Seen TA[BLE]
					if (c == 'B') {
						state = 3;
						continue;
					}
					break;
				case 3:		// Seen TAB[LE]
					if (c == 'L') {
						state = 4;
						continue;
					}
					break;
				case 4:		// Seen TABL[E]
					if (c == 'E') {
						state = 5;
						continue;
					}
					break;
				case 5:		// Seen TABLE
					if (c == ' ') {
						state = 6;
						text.setLength(0);
						continue;
					}
					break;
				case 6:		// Scanning for end of table name
					switch (c) {
					case '\n':
						String name = text.toString();
						table = new PdmTable(name, 100, 100000);
						m_tables.put(name, table);
						state = 7;
						continue;
					}
					text.append((char) c);
					continue;
				case 7:		// Looking for column name or {
					switch (c) {
					case ' ':
					case '\t':
					case '\n':
						continue;
					case '{':
						table.addRow();
						state = 9;
						continue;
					}
					text.setLength(0);
					text.append((char) c);
					state = 8;
					continue;
				case 8:		// Gathering column name
					switch (c) {
					case '\t':
					case ' ':
					case '\n':
						table.addColumnName(text.toString());
						state = 7;
						continue;
					}
					text.append((char) c);
					continue;
				case 9:	// Seen { or ,
					switch (c) {
					case '"':
						text.setLength(0);
						state = 10;
					case ' ':
					case '\t':
					case '\n':
						continue;
					}
					break;
				case 10:	// Gathering value
					switch (c) {
					case '\\':
						c = linenoReader.read();
						switch (c) {
						case -1:
							System.out.println("Input terminated with a single '\\'");
							c = '\\';
						case '"':
						case '\\':
							break;
						default:
							System.out.println("Unknown escape \\" + (char) c);
						}
						break;
					case '"':
						table.addValue(text.toString());
						state = 11;
						continue;
					}
					text.append((char) c);
					continue;
				case 11:	// End of value
					switch (c) {
					case ',':
						state = 9;
						continue;
					case '}':
						state = 12;
						continue;
					case ' ':
					case '\t':
					case '\n':
						continue;
					}
					break;
				case 12:	// End of row
					switch (c) {
					case '{':
						table.addRow();
						state = 9;
					case ' ':
					case '\t':
					case '\n':
						continue;
					case 'T':
						state = 1;
						continue;
					}
					break;
				}
				break;
			}
		} catch (Throwable e) {
			System.out.print("Exception: ");
			System.out.println(e.getMessage());
			if (linenoReader != null) {
				System.out.print("At lineno =");
				System.out.println(linenoReader.getLineNumber());
			}
			System.out.print("State = ");
			System.out.print(states[state]);
			System.out.println(")");
			if (table != null) {
				System.out.print("Loading ");
				System.out.println(table.getName());
			}
			System.out.print(">> last textLength=");
			System.out.println(text.length());
			for (int i = 0; i < text.length(); ++i) {
				System.out.print(text.charAt(i));
			}
			System.out.println("");
			System.out.println("<<");
			return false;
		}
		if (c == -1 && state == 12) {
			System.out.println("Successfully read " + fileName);
			ret = true;
		} else {
			report ("Unexpected character '" + c + "' at lineno=" + linenoReader.getLineNumber() + " (state= " + states[state] + ")");
			ret = false;
		}
		if (linenoReader != null) {
			try {
				linenoReader.close();
			} catch (Exception e) {
				reportException(e);
				report("Can't close '" + fileName + "'");
		}	}
		return ret;
	}
	
	private static void init(Ta diagram)
	{
		m_diagram               = diagram;
		m_contains_rc           = null;
		m_table_id              = 0;
		m_table_ec              = null;
		m_first_resource_family = true;
		m_first_resource_class  = true;
		m_first_contact_type    = true;
		m_location_ec           = null;
		m_resource_status_rc    = null;
		m_resource_contact_rc   = null;
		m_resource_owner_rc     = null;
		m_location_rc           = null;
		m_manufacturer_rc       = null;
		m_responsible_vendor_rc = null;
		m_organization_first    = true;
		m_maintenance_rc        = null;
		m_responsible_org_rc    = null;
		m_org_bought_for_rc     = null;
		m_model_rc              = null;
		m_operating_system_rc   = null;
		m_cost_center_rc        = null;
		m_gl_code_rc            = null;
		m_supply_vendor_rc      = null;
		m_maintenance_vendor_rc = null;
		m_company_bought_for_rc = null;
		m_department_rc         = null;
		m_contact_rc            = null;
		m_company_rc            = null;
		m_organization_rc       = null;
		m_admin_organization_rc = null;
		m_supervisor_contact_rc = null;
		m_primary_contact_rc    = null;
		m_first_ci_rel_type     = true;
		m_contains_order        = -1;
	}
	
	public String parseSpecialPath(Ta diagram, ResultBox resultBox, String path)
	{
		String		parameters = path.substring(4);
		String		msg        = null;
		PdmTable	table;
		PdmTable	controlled = null;
		Enumeration	en;
		
		m_resultBox = resultBox;
		
		setTitle("Loading " + parameters);
		
		try {
			readInput(parameters);
			init(diagram);
			addRootAttributes();
			
			for (en = m_tables.elements(); en.hasMoreElements(); ) {
				table = (PdmTable) en.nextElement();
				table.identifyPrimaryKey();
			}
			
			try {
				File				file  = new File("tables.txt");
				FileOutputStream	os    = new FileOutputStream(file);
				PrintWriter			ps    = new PrintWriter(os);

					
				for (en = m_tables.elements(); en.hasMoreElements(); ) {
					table = (PdmTable) en.nextElement();
					table.show(ps);
				}
				ps.close();
				System.out.println("Tables shown in tables.txt");
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
				System.out.println("Can't show tables to table.txt");
			}

			notEntityInstances("Business_Management");
			notEntityInstances("ci_mdr_idmap");
			notEntityInstances("ci_rel_type");

			load_ca_owned_resource();
			load_ci_app_ext();
			load_ci_contact();
			load_ci_database();
			load_ci_enterprise_service();
			load_ci_enterprise_transaction();
			load_ci_fac_ac();
			load_ci_hardware_server();
			load_ci_hardware_storage();
			load_ci_hardware_virtual();
			load_ci_mdr_provider();
			load_ci_network_cluster();
			load_ci_network_hub();
			load_ci_network_nic();
			load_ci_network_router();
			load_ci_operating_system();
			load_ci_security();
			load_ci_service();
			load_ci_telcom_other();
			load_nr_com();
			
			for (en = m_tables.elements(); en.hasMoreElements(); ) {
				table = (PdmTable) en.nextElement();
				if (!table.isSeen() && table.isEntityInstances()) {
					dump(table);
			}	}

			load_business_management();
			load_ci_mdr_idmap();

			controlled = m_tables.getTable("Controlled_Table");	

			for (en = m_tables.elements(); en.hasMoreElements(); ) {
				table = (PdmTable) en.nextElement();
				if (table.isEntityInstances()) {
					addEdges(table, controlled);
			}	}			
			diagram.attachBaseClasses();

		} catch (Throwable e) {
			StackTraceElement[] stack = e.getStackTrace();
			String				trace;
			int					i;
			
			msg = "Unable to load CMDB";
			report(msg);
			reportException(e);
			
			for (i = stack.length; i > 0; ) {
				trace = stack[--i].toString();
				report(trace);
			}
		}
		m_tables = null;
		m_uuids  = null;
				
		if (msg != null) {
			done("Load failed!!");
		} else {
			done("Loaded mdb (" + parameters + ")");
		}
		return msg;
	}
	
	public boolean isSpecialPath(String path)
	{
		return path.length() >= 4 && path.substring(0,4).equals("pdm:");
	}
}

