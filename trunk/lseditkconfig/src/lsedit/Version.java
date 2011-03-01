package lsedit;


public class Version {
	public final static int MAJOR = 7;
	public final static int MINOR = 3;
	public final static int BUILD = 12;
	public final static String COMPILED = "June 29nd, 2009";

	public static String Number()
	{
		return(MAJOR + "." + MINOR + "." + BUILD);
	}

	public static String formatNumber(long l)
	{
		String	val  = Long.toString(l);
		String  ret  = "";
		int		lth  = val.length();

		for (int i = 0;; ) {
			ret += val.charAt(i);
			if (++i >= lth) {
				break;
			}
			if (((lth - i) % 3) == 0) {
				ret += ',';
		}	}
		return ret;
	}

	public static int	InternalNumber()
	{
		return (((MAJOR * 1000) + MINOR) * 1000) + BUILD;
	}

	public static String CompileDate()
	{
		return("Compiled: " + COMPILED);
	}

	public static String Detail(String property)
	{
		String result;

		try {
			// This will fail if an applet
			result = System.getProperty(property);
		} catch (Exception e) {
			result = "**Denied**";
			System.out.println("System.getProperty(\"" + property + "\"): " + e.getMessage());
		};
		return(result);
	}

	public static String authorsAndCopyright()
	{
		String  result;

		result	= Version.Number() + "\n\n" + Version.CompileDate() + "\n"
		        + "Developed at the University of Waterloo under the supervision of Prof. Ric Holt\n"
		        + "Original Author: Gary Farmaner (Rewritten in Swing by Ian Davis)\n";

		return result;
	}

	public static String Details(Diagram diagram)
	{
		String  result;
		Runtime	r;

		// System.getProperties().list(System.out);

/*
		for (int i = 0x7FFFFFFF; i != 0; i >>= 1) {
			System.out.println("" + i + "'" + formatNumber(i) + "'");
		}
*/

		result  = authorsAndCopyright()
				+ "\nRight click menu items to request context sensitive help specific to their action.\n\n";

		try		{ //set if possible
				result += "LSEDIT is part of the Software Landscape tool suite\n"
				        + " licensed to CTBTO by Telepresence Systems, Inc. (www.telepres.com)\n"
				        + " under contact No. 01/2/20/163\n\n"
				        + "Run Time Engine: " + Detail("java.vendor") + " " + Detail("java.version") + "\n"
				        + "Virtual Machine: " + Detail("java.vm.name") + " " + Detail("java.vm.version") + "\n"
				        + "V/M Vendor: " + Detail("java.vm.vendor") + "\n" 
				        + "Operating System: '" + Detail("os.name") + "' " + Detail("os.arch") + " " + Detail("os.version") + "\n"
				        + "Patch level: " + Detail("sun.os.patch.level") + "\n"
				        + "User id: " + Detail("user.name") + "\n"
				        + "Directory: " + Detail("user.dir") + "\n";

				r       = Runtime.getRuntime();
				result += "Memory: " + formatNumber(r.totalMemory()) + " - Free: " + formatNumber(r.freeMemory()) + " = " + formatNumber(r.totalMemory() - r.freeMemory()) + " Max: " + formatNumber(r.maxMemory()) + "\n";
				r.gc();
				result += "Memory: " + formatNumber(r.totalMemory()) + " - Free: " + formatNumber(r.freeMemory()) + " = " + formatNumber(r.totalMemory() - r.freeMemory()) + " Max: " + formatNumber(r.maxMemory()) + "\n";
				if (diagram != null) {
					result += "Entities: " + formatNumber(diagram.getNumberEntitiesLoaded()) + " Relations: " + formatNumber(diagram.getNumberRelationsLoaded())
					        + " Attributes: " + formatNumber(AttributeCache.actualAttributes()) + " (" + formatNumber(AttributeCache.requestedAttributes()) + ")\n";
//					result += "Sizes: " + sizeOf(EntityInstance) + "+" + sizeOf(EntityComponent) + " " + sizeOf(RelationInstance) + "+" + sizeOf(RelationComponent) + "\n";
					result += diagram.parameterDetails();
				}

		} catch (Exception e) {}

		return(result);
	}
}

/* History:
   6.0.0 -- Changes by Ian Davis

   April 4th (All day) 9:30-10pm
   
   Placed all source in package lsedit.
   Fixed minor compile problems under latest version of sun.
   Added code to close window when x pressed in Top Right hand corner.
   Enhanced the version information.
   Changed file prompt to prompt using java.awt.FileDialog
   Made right tabbed box go away completely when turned off
   Activated appropriate right tab so will display properly when turned back on

   April 5th		   10:30-4pm
   Removed some debug output
   Made animation optional to improve refresh rate.
   By default now in options menu "Show animations" is set to false.
   Changed cursor back to default after double click of mouse
   Removed some null pointer exceptions (rather by guessing desired behaviour)

   6.0.1 

   April 7th
   Corrected bug in drawing edges introduced in 6.0.0
   Worked towards understanding lsedit layout strategy
   Cleaned up code
   Traced layout problem to	  Diagram.setToViewport()
   Attempting to decipher what occurs beneath this.
   Quite bemused since right now nothing makes much sense.
   Concerns include:
	(1) rootInstance.getChildBoundingBoxLocal();
		  this only gets the bounding box of its own children, not its decendants.
		  Is there a presumption that children strictly contained within parent.
	(2) old formula for y scale was bizarre.
		  yScale = (height-width/16)/bb.height;	 [ changed to function purely of height]
	(3) don't understand dx and dy at all..	 Don't see how using only height and width one
		can correct positions of children.
	(4) translateChildrenLocal changes local values using dx and dy for both children and bends
		But bends not considered in computing what the bounding box was

   April 8th-10
   Released 6.0.1

   6.0.2

   Began exploring the refresh problem
   Tracked bug down to repaints() on both mouse down and mouse up pairs
   Have changed code to paint group flags in tight relationship to setting/clearing group flag
   Have delayed painting of group to be moved until move actually begins to avoid need to refresh
   Removed debug messages
   Released 6.0.2

   6.0.3

   April 14th

   Corrected the way in which point positions were cached.	There was a problem that simply rescaling
   the point positions when want to actually say discard the cache of point positions doesn't work
   because the x,y positions of the objects containing these points may not yet be right.

   6.0.4

   April 16th

   Added source file provided by eric.
   Rewrote Stringlinizer.  Potentially some problems now with <A ...> </A> tags.. No idea what was
   being attempted before here.	 Investigated types of object.	Investigated setting color of
   objects manually.

   April 17th

   Added ability to assign open color to entity.  "opencolor=value".
   Added dynamic color changes to object through P or menu.
   Hopefully all works when saved.

   6.0.5

   Corrected displaying of descriptions in Legend box
   Corrected minor error in drawing flags on box.. An outline is one pixel wider than a fill.
   Enhanced color chooser to allow for an alpha
   Fixed bug in forward edge tracing

   6.0.6

   Wednesday April 24th

   Changed how flags were cleared
   Removed key codes from popup menus
   Removed U/u/S/s from entity popup menu
   Removed U/u/S/s from toolbar
   Removed fit child to container
   Removed save landscape raw from File menu
   Removed print all to eps files
   Moved towards handling resize of objects correctly
   Cleaned up forward/back reporting on result tab logic
   Improved/rewrote again stringLiner
   Corrected infinite loop in util

   6.0.7

   Wednesday April 25th 2002

   Added feedback message to description box when over feedback box, so can read entire error message.
   Refresh entire drawing if selecting a box clears highlighting anywhere
   Renamed edit button to menu button
   Removed duplication of this button
   Added code to remove menu popup on mouse down if no mouse up seen.	This is purely preventative code
   for a bug not well understood.

  6.0.8

  Thursday April 26th. 2002

  Fixed problems with Forward/Back refs..

  6.0.9

  Friday April 27th, 2002

  Removed minMax button on applet
  Changed client/suppliers so will compress rather than not draw if can't reduce while still being too wide

  6.0.10

  Added short cut keys back into menus and toolbar buttons u/s etc.

  6.0.11

  Added ability to change color of classes and relations from Legendbox
  Added ability to query things in the legend box
  Added popup menus

  6.0.12

  Changed to 4 character tabs in source code -- no release

  6.0.13

  Added CorrectBadLayout to handle irregularities in input TA file
  Huge amount of cleanup
  Completely rewrote how attributes were edited, etc

  6.0.14

  Allowed class style changes to impact on diagram
  Fixed bug in StringLinizer

  6.0.15

  Added ability to change class an entity belongs to through editing attributes
  Changed creation of new entity to use EditAttribute logic (simplifies code to maintain)

  6.0.16

  Very unclear whether this version was ever released. Work stopped so that a co-op
  student could implement 6.0.17. and 6.0.16 was where it stopped.  However, co-op
  student never did take on programming task.

  6.0.17

  Fix depth limit problems.  Converted code from using absolute external coordinate
  system to relative to parent coordinate system.  Huge rewrite of all graph drawing
  software. (Also reworked lslayout and lsadjust).

  6.0.18

  Minor cleanup.. stopped allowing drag of boxes into $ROOT box.
  Corrected problems saving and restoring raw dumps.

  6.0.19

  Changed RelationInstance.findNearestSeg to work with doubles instead of ints and
  to check for threshold^2 > dist^2 thus avoiding expensive sqrt operation.  N.B.
  Since working in pixels threshold^2 > dist^2 iff threshold > dist.

  Fixed:  PR 5. When attempting to highlight an arrow after a forward or back trace, 
  non-visible arrows may be selected that are in the same region.  Select a node,
  do a forward or back trace.  Try to select a visible arrow that overlaps with a 
  non-visible arrow (at their intersection), both arrows will be selected.

  Fixed:  PR 7. When doing an "same size" (or width/height), the entity will not 
  fully resize if the new size brings part of it outside the window.  Suggest maybe a 
  better course of action would be to do the full resize, then relocate the entity so 
  that it is entirely inside the window. To-repeat: Place an entity at the right edge 
  of the window.  Select it, then select a wider entity.  Click Arrange, then "Same width".

  (New rules: if width > 1 width = 1; if (width+x>1) x= 1-width; ) etc. 

  6.0.19a

  Fixed correction of PR5 fix.

  6.0.20

  Changed to not emit default values of attributes

  7.0.1

  Absolutely huge rewrite.. to work under swing..
  This is essentially an entirely new product..
  Key enhancements are -- interrupt handling quite different (interrupts now come in at listeners)
  Drawing of relations quite different  (Objects created to do the actual drawing)
  Management of draw src/dst quite different (The draw src/dst always indicates where an edge is drawn from/to)
  Management of heirachy -- this is now done using pre/post order numbers
  Integer arithmetic used rather than doubles everywhere.
  Scroll panes added all over the place
  TOC box redone as a tree..
  All keystrokes tied to menu accelerators and accessible from menus.
  The list goes on and on
  
  7.0.2
  
  Changed tokenizer to reuse strings to save memory

  7.0.3

  Fixed moving of edge cardinals in sync with box.

  7.0.4

  Fixed moving edge cardinals better :-)
  Fixed problems with TOC not first requested then requested
  Made Cntl-V immediate

  7.0.5

  Changed to use layout manager

  7.0.6

  Moved TOC into tab box, fixed minor bugs

  7.0.7

  Do is no longer a subclass of JPanel and this ripples up.
  LandscapeViewerCore adds to the originial contentPane() instead of being a replacement content pane... this fixes problems
  Changed lsedit to use split pane three ways..
  Changed -G to be <percent>x<percent> indicating fraction of layout to be occupied by diagram.  0 = none.. 100 = all.

  7.0.8

  Fixed bug when dragging into new box loosing edges (need call to prepostorder)
  Fixed bug with 0-9 not actually changing the diagram (changed to use doClick() so simulates precisely the action of a real click)
  Changed query box numbers to match legend box numbers

  7.0.9

  Rewrote printing from the ground up.

  7.0.10

  Cleaned up tab box images so don't scroll
  Fixed performance problem.(zero sized boxes are considered closed)
  Made TOC align with editorial changes to the main diagram
  Added back in notion of TOC visible or not loaded
  Allow cutting and pasting in TOC

  7.0.11

  Reworked LandscapeEditorFrame so could be invoked from arbitrary java code
  Reworked LandscapeEditorFrame so that could run either LandscapeEditorCore or LandscapeViewerCore
  Removed  LandscapeEditorApp
  Moved all TA read/write code into ta.java

  7.0.12

  Change Landscape objects so have JComponent rather than are a JComponent
  Reduced memory requirements for edges substantially
  Allowed editing of relations and relation classes
  Removed BaseEntity
  Added   EntityComponent/RelationComponent

  7.0.13

  Made avgX a constant inside a sort -- not recomputed for each compare
  Fixed a slew of bugs

  7.0.14

  Change Legend to handle hiding of entities
  
  7.0.15

  Added undo/redo

  7.0.16

  Added undo/redo for creating new entitites
  Added ability to change undo/redo limit

  7.0.17

  Added cut and paste to undo/redo set
  Moved m_clipboard to have separate copy for each diagram.

  7.0.18

  Can now load TA without first running through lslayout (automatically uses sugiyama layout on each level)
  Can now load TA without a schema
  Changed sugiyama algorithm to handle relative addressing
  Reworked color chooser in hope of eliminating keys not working subsequently under linux (also changed editAttribute)
  Cleaned up update/delete pasting operations so sync resultbox with TOC and with diagram
  Don't allow inserts into root if anything already there
  Added 'l' (layout) as shortcut for sugiyama algorithm
  Don't exclude base relations from diagram
  Changed minor bug in how relations converted to textual string description
  Result box now shows absent entities in black -- can't navigate to them
  Recursive copy when pasting into TOC
  Fixed numerous bugs

  7.0.19

  Fixed minor bugs with scroll bar, things not showing when inserted, and not being insertable when no schema

  7.0.20

  Changes made by Andrew Malton to handle creating edges

  7.0.21

  Retrofitted some of my ongoing changes into Andrews changes
  Added labels to class type
  Reworked drawing of labels for groups
  Encapsulated changes to xrel, yrel, widthrel and heightrel
  Removed fitto -- hardly needed with relative addressing

  7.0.22

  Fixed many PR's

  1. Changed History tab to Undo
  2. Show count of children in TOC if > 1
  3. Delete now deletes whatever is selected be it entity/relation/both
  4. Undoing delete containing updates TOC correctly
  5. Can undo deletion of edges
  6. Removed all logic to handle reading/writing raw TA binary encripted files
  7. Accept all class's and attribute in schema-less files
  8. Accept both single and double quote delimiters as quoted text.
  9. Remove keystrokes to change tab box (do with mouse)
 10. Same size/align commands now show up as single undo operation

 7.0.23

 1.  Removed FONT_CORRECTION as obsolete
 2.  Cleaned up mode handling 
 3.  Added idleModeHandler
 4.  Added newEdgeModeHandler

 7.0.24

 1.  Added outline for moveGroupHandler
 2.  Allowed modeHandlers to be activated by a move as well as by an enter
 3.  Cleaned up how cursors managed
 4.  Open the TOC automatically the first time it is tabbed to within a diagram
 5.  Cleaned up cutting of a container wrt result box and TOC
 6.  Allow repeated cuts
 
 7.0.25

 1.  Changed default layout so performed late when things drawn -- not when loaded
 2.  Added simplex algorithm
 3.  Added -n option to select simplex algorithm as default
 4.  Added 'n' command to layout using simplex algorithm
 5.  Added 'j' command to layout everything below a node using desired layout algorithm
 6.  Removed useless classes

 7.0.26

 1.  Added history to menu
 2.  Cleaned up drop down
 3.  Cleaned up \n in attribute data when saving
 4.  Removed distinct keys for editing entities/relations
 5.  Changed so that missing relative x,y,width,height computed when requested.

  7.0.27

  1. Removed LandscapeViewerCore merging functionality into LandscapeEditorCore
  2. Removed ViewModeHandler merging functionality into EditModeHandler
  3. Removed all action code from EditModeHandler moving into LandscapeEditorCore
     (This reduced 3 case statements into a single case statement on action)
  4. Moved all diagram intensive actions from LandscapeEditorCore into Diagram
  5. Cleaned up simplex algorithm graph layout
  6. Made simplex algorithm default layouter

  7.0.28

  1. Made further improvements to Relayoutall and Simplex algorithm
  2. TestForClose now sees changes
  3. Augmented reporting re saving of .lsedit
  4. Fixed problems with Fit label
  5. Fixed open/closing when drag and dropping into boxes
  6. Toggling containment now toggles all selected entities to open or closed

  7.0.29

  1. Created a cache of fonts
  2. Changed how closed fonts are computed -- no longer a fixed array of closed fonts since cached
  3. Allowed all parts of diagram to have font changed
  4. Reversed color of cardinals

  7.0.30

   0. Moved FontCache to its own class
   1. Allowed fonts to be changed
   2. Reworked ArrowDimensions so that resized fonts fit
   3. Created ColorCache
   4. Extended JColorChooser to allow clearing of color cache
   5. Created EntityCache
   6. Added Create New Diagram (Cntl-N)
   7. Changed EditAttributes to consider font size
   8. Changed menus to set Font
   9. Allow fonts to be reset to default
  10. Saved selected fonts on exit
  11. Added -b/ig option to automatically set all fonts large
  12. Improved performance by making EntityCache static and fixed hash
  13. Fixed edge creation problem
  14. Cleaned up code

 7.0.31

   1.  Moved cardinals[] to EntityComponent to save space
   2.  Added Src Cardinals (Alk-K)
   3.  Added ability to ask for open arrows (Alt-d)
   4.  Added ability to put arrows in center of edges (Alt-d)
   5.  Fixed PREV/NEXT handling
   6.  Added dynamic PREV/NEXT tool buttons
   7.  Remove things in find set as they are deleted elsewhere
   8.  Added History tab
   9.  Added matrix (m) layout
   10. Added close all boxes (Alt-r) reachable from layout.
   11. Added open all boxes (Alt-R)  reachable from layout
   12. Cleanded up code and fixed various PR's
   13. Added dynamic UP/DOWN history buttons

 7.0.32

   1.  Added Alt-M and Alt-Shift-M to zoom in and out on an entity
        (Exact positioning of diagram on centre of entity proved impossible to achieve --
		 No idea why????)
   2.  Got running as an applet -- sort of anyway
         www.textserver.com/lsedit

 7.0.33

   1.  Cleaned up problems with Applet failing because trying to use null frame in find
   2.  Cleaned up file menu items not appropriate for an applet.
   3.  Added rudimentary ability to load file into applet.. You have to know the name
       of the file to be loaded.
        (Would be very nice to have a server side JFileChooser component so that one
		 could see choices.  Perhaps this could be implemented on top of HTTP by using
		 some sort of low level get directory contents -- available solutions to this
		 problem all require a backend server application running to convey filenames
		 to applet which seems silly.)
   4.  Glitzed up the tab pane. Right clicking on the tabs allows changing of tab painting
       rules.  Problem.. if switch to scroll the scoll steals the mouse clicks and one has
	   to right click on the border of the JTabbedPanel to bring up menu to turn off
	   scrolling.
   5.  Added informative information to applet.
   6.  Set default action on F11 to be to navigate to www.swag.uwaterloo
   7.  Set default action on F12 to be to navigate to www.swag.uwaterloo/lsedit [Doesn't yet exist]

 7.0.34

   1.  Changed default cardinal point size from 7 to 9
   2.  Added tooltip to cardinals
   3.  Allowed layout algorithms to be customised (Configure Layout)
   4.  Fixed relayout logic
   5.  Added scroll tab option to menu so can turn off easily
   6.  Changed command line method of preselecting layouter to allow scaling to new layouters
   7.  Changed tool tip on prev history
   8.  Now showing cursors in applets
   9.  Allow relation class to default color from superclass
   10. Show label of relation class in relation class label color
   11. Reworked sugiyama layout to agree more closely with matrix layout rules.
   12. Consider only visible edges when laying out simplex graph.
   13. Dispose of dialogs when done

 7.0.35

   1.  Can now undo relayout of a subtree and relayout when inserting children
   2.  Removed ability to perform background loading
         Files to be loaded in background are now simply listed in file menu
   3.  Unified diagram and menu edge mode flags (so that menu reflects settings in diagram)
   4.  Added footer to results in results tab so can tell where end of results are
   5.  Show edges to/from drawRoot
   6.  Remember defaulted label values in entity versus actual label values in TA so can write out same
   7.  Queries now show true information in result box -- not lifted information
   8.  Forward/Back queries on suppliers and clients are specifically wrt diagram only
   9.  Can now select multiple edges using Shift and mouse click
   10. Result's now scroll properly
   11. Added Align Bottom to set of possible alignments (seemed to be missing)
   12. Made all alignments adjust to be forgiving where alignment requested impossible due to clipping
   13. Added new edge mode DIRECT+SIDE.  Same as direct but whenever an edge is from an ancestor->descendant
       edge starts on left side of ancestor.  Similarly whenever an edge is from descendant->ancestor edge
	   goes to right side of ancestor.  This helps visually to spot such types of edge.
   14  Convert newlines to \n in descriptions and class descriptions when writing TA
   15. Deleted entities now show in history as black names -- can't navigate to such deleted entities.
   16. History no longer adds things navigated to using prev/next or clicks on history.
   17. Changed CLOSE EDITOR to prompt for saving and then switch to blank new landscape
   18. Saves relation style in TA
   19. Results now align on left (not center)
   20. Replaced string cache with call to String.intern -- seems functionality already largely provided
       by java.lang.  Should save memory.

 7.0.36

1. Added ability to read from gzip, zip and jar compressed sources primarily
   to speed data transfer to applets.  Any path ending in ".zip", ".gzip" or
   ".jar" ignoring case is presumed to be in the corresponding internally
   compressed format.  For zip files and jar files the path name may be
   followed by [....] containing the name of the subfile within the zip to be
   loaded...

   eg. xyz/set.zip[c488.ls.ta] would load the c488.ls.ta subfile within the
       set.zip file.

   If no subfile is specified the first subfile found is presumed to be the
   one that is to be read.  This is primarily intended to simplify the case
   where the zip file contains only one subfile.

   gzip has not been tested, since I don't have gzip on my machine at home.

2. Added logic to invoke external browser.  The challenge here is that I
   have found no way of auto-detecting the location of the invokers
   internet browser.  For known os's (named when one presses F1 and when
   one is prompted for the required path) I can hard code any suitable
   default.

   Andrew -- Can you provide me with os name and your preferred default for
             Mac.

   Nikita -- Can you provide me with os name(s) and your preferred default
             for swag, et. al.


3. Added Cntl-B (Select Browser) to file menu to allow browser path to be
   changed.  This is needed if one wished to alternate browsers, upgrade
   browsers etc, and solves the problem of how to correct invalid browser
   path names.

4. Remembers browser in .lsedit and restores same.  The browser can be set
   to unknown, forcing prompt for browser by specifying the browser to be
   the zero length string.

7.0.37

1. Experimentally using BrowserLauncher

2. You can now change the relation class to use for constructing the tree hierarchy

7.0.38

1. Added information about containment relationship class to LegendBox

7.0.39

1. Removed autolayout when inserting new entity into container
2. Allow cutting of root and cutting/pasting into root
3. Fixes resizing following autolayout
4. Fixed layout of matrix layout
5. Removed root popup menu
6. Automatic converts forests to a tree on loading
7. Massive cleanup re how contains works
8. Changed entities so marked deleted
9. Lots of other correction of PR's

7.0.40

1  Removed ability to nest an attribute inside an attribute -- not used and not supported by lsedit anyway
2. Added tracking of position of an entity by each hierarchy
     A relation class now has two new attributes
	   class_hierarchy = <n> where n is an integer index >= 1 (n = 0)
	     This value indicates which entry in a 0 based list applies to this relation class
	   class_iscontains = true/false with false being the default
	     This indicates that this relation class is to assume the role of contains on loading the TA

	[Aside -- the contains relation class is not explicitly registered in the TA file].
	 The contains class has an implicit class_hierarchy=0 and a value of class_iscontains=true if no other relation class has this property

     The xrel,yrel,widthrel and heightrel attributes of an entity can now be either a single value as before or a list of values one per
	 contains_hierarchy.
3. Rewrote TA input parsing to avoid construction of AttributeRecords.  Attempting to speed loading
4, Active and visible status of a relation class now saved in output TA
5. Made iofactor a true first order value in relation class
6. Removed caching in hashtable all known atttribute names -- not used anywhere

7.0.41

1. Reworked organisation of menu's as per Nikita's proposal

7.0.42 August 18, 2004

1. Fancy new Set grid option (Shift G) replaces Increase grid and Decrease grid
2. Root instance is now a permanent root forming the root of a forest of trees 
    -- This simplifies switching of containment hierarchies
	-- Is more accomodating of TA representing a forest
	-- Avoids creating new edges to handle switching containment to a forest
	-- Allows insertion of logical root entity into other entitities
	-- Allows a logical tree having zero nodes
	-- Etc.
3. Removed complaint that selected containment hierarchy didn't span all nodes.
    -- Now the forest is simply presumed to be what was desired
4. No tooltip for drawRoot entity
    -- It was annoying
5. Removed Grid Horizontal and Vertical Layout.
    -- Code made redundant by align options and distribute options and move options
6. Changed appearance of open and closed folders
    -- Trying to avoid edges not going to visual part of entity
7. Matrix layout takes into consideration preferred min-widths of entities.
8. Tool tip buttons disabled rather than hidden
9. Move entity keys now move in accordance with specified grid.
10.Added show grid option (g) -- This shows the grid but only on the current draw instance
11.Removed Cntl+Click method of entering an entity.
12.Indirect loop edge now dotted..

   Lots more minor cleanup

7.0.43 August 19, 2004

1. Changes to contains relation now undoable
2. Results tab now shows why containment couldn't be established for an edge
3. Undo box now scrolls
4. Increased dotted space in arcs between lifted self edges

7.0.44

1. Switch heirarchies on legend/query tabs when undoing/redoing
2. Further attempts to fix keystrokes ignored following grid (Shift-G)

7.0.45

1. Various PR's addressed

7.0.46

1. Sort TOC
2. Add tooltip messages to menu items
3. Allow focusing on common ancestor when switching hierarchies
4. Fixed PR's

7.0.47

1. Improved handling of escape sequences in input and output
2. Fixed problems with previously opened files not showing in file menu
3. Navigating up changes feedback text
4. Changed right click on TOC to bring up menu
5. Created sub-layout menu inside options
6. Added ability to find by type of entity
7. Changed .lsedit to lsedit.ini on windows platforms
8. Rebuild TOC when contains edge changes
9. Show new diagram as "New diagram" in the window menu
10.Set root iofactor to 0.5
11.Changes to simplex layout algorithm

7.0.48

1. Arrow heads now sized when move over them to indicate number of lifted edges.  This
   costs 4 bytes of memory per edge to store a count in.
2. This can be made a permanent feature by changing arrow options.
3. There is now the ability to load settings from a user specified file (File/Load settings)
   N.B. Need to thrash out what constitute lsedit settings and what constitute ta settings
3. When switching heirarchies lift only to ancestor of selected entities
   else if none selected then lift to ancestor of all children
4. Moved show grid into Options/Layout
5. Description of an edge when hovered over is now more complete.  Shows et. al. if
   a lifted edge comes from/goes to more than one underlying entity.  Also shows count
   of number of edges merged into one lifted edge.
6. Fixed a number of PR's.

7.0.49

1.  Colors may now be entered in the TA using the standard 0-255 r 0-255 g 0-255 b 0-255 alpha java values
    Parsing of 1 is as integer if no decimal place but as decimal if 1.0. In this later case it is equivalent
	to 255.
2.  Option submenu for client/server options
3.  New client/server option to include/exclude invisible entity classes
4.  Removal of special semantics for ENTITY_STYLE_LABEL_GROUP
5.  Can now specify an alpha of 0 in the color chooser.
6.  Relayout all remembers the layouter used for undo/redo.
7.  Logic reworked for open all so really does open all (and not just all under the lifted edge selected)
7a. The results table now shows an indented hierachy for edges shown based on what their source hierarchy is
8.  In the attribute editor colors now have the word COLOR on them to distinguish from a NULL.. absent color
8a. When editing a color this word changes to EDITING.
9.  Cleaned up how inheritance of classes was computed
10. ENTITY_STYLE_GROUP and ENTITY_STYLE_LABELLED_GROUP now show their outline in their defined color
11. Cleaned up problem with dragging things out of boxes and then undoing
12. Simplex layout reworked to handle opening all.
13  New configuration options..
     a) Relative width of border 0 - 0.5
	 b) Fixed ratio checkBox
	     (If not checked the layout will linearly transform the laid out graph to fill the space available for it.
		  If     checked the layout will preserve when shown as root the graph layout width/height ratio.
		 )

7.0.50

1.  Added a clipboard tab showing the contents of the clipboard
2.  Removed Cut additional -- action now performed by setting check mark in clipboard tab
3.  Reworked how the clipboard was dynamically represented to handle partial undoing of things added to it.
4.  Fixed number of bugs

7.0.51

1.  Added shapes
2.  Added images

7.0.52

1.  Added generalisation to regular shapes -- allow to rotate

7.0.53

1.  Made edges always address edge of drawn object -- not edge of box containing drawn object.
2.  Change image to be a bitmap allowing selection of multiple concurrent images.
3.  Wrote software to allow the image to be dynamically edited within the AttributeEditor

7.0.54

1.  Made the highlighting parameter driven to allow control over how highlighting performed

7.0.55

1.  Made layout's operate on only visible entities
2.  Allowed for grouping by dragging on the root entity a grab box..
    a) With no keys pressed new group becomes those visible entities fully contained within the grab box
	   New key entity remains same if within grab box else some random entity in grab box
	b) With shift pressed the visible entities in grab box are added to the existing grouped entities
	   New key entity created if no prior one from some random entity in grab box
	c) With alt pressed the entities are subtracted from the existing group
	   New key entity is selected if resulting grouping is not empty and has no key entity
	   i) First choice is to use a visible entity as the new key entity
	   ii) Fall back choice is to use some currently not visible entity as the new key entity

7.0.56

1.  Fixed problem with TOC jumping when entries opened and closed
2.  Addressed a couple of other PR's
3.  Changed how self edges were painted so that multiple self edges for different relations do not overlap.
4   Added a new 'expand' layout operation ('r') which given a set of selected nodes with known positions
    attempts to linearly realign these nodes so that they fill the bounding box.  The bounding box may itself 
	be specified by using the reconfigure layouter options.  This feature is useful for spreading out a
	layout or whatever.
5.  Fixed ordering of nodes with a row problem in simplex layout algorithm.
6.  Added SpringLayout.

7.0.57
 
    Not released

7.0.58

1.  Restrict width of edge to be at most 100 pixels
2.  Smart allocation of new entities into available empty space
3.  Fallback to spring layout if simplex layout algorithm runs out of memory
4.  Clear selected entities if drag on backgound
5.  Correct simplex algorithm so doesn't presume 2 or more rows exist
6.  Change matrix layout so that gap is percentage of remaining space
7.  Added arrow heads and selected marker to self edges
8.  Halved size of self edges
9.  Draw raised self edges correctly

7.0.59 -- Released as 7.1.0

1.  Show style of relation in the legend and query box
2.  Allow entity and relation classes to be deleted by right clicking item in legend.
      Deletion only allowed if:
	    a) Not base entity/relationship class
		b) Nothing inherits from the class
	  Deletion of relation class causes deletion of all relations of this class.
	  Deletion of entity class causes deletion of all entities of this class.
3.  Allow entity and relation classes to be created by right clicking in whitespace of legend box.
    Also possible via edit menu.
4.  Now possible to show and change inheritance rules by right clicking on entity class/relation class
    on legend.
5.  Change accelerator codes for switching edge type so that ALT-E ALT-e could be used for the more
    logical create entity/relation class analogous to E and e to create instances of entity/relationship.
6.  Added an explicit exit(0) when the frame window closes.
7.  Allowed parsing of $INHERIT (x) (y)
8.  Now saves inheritance rules regarding relations.
9.  Removed makeinstanceofus logic -- since I think this is basically now undesirable.
10. Change drawing of self edges so it makes arcs larger -- not smaller for successive relationships.

    New source file ClassInherits.java

7.0.60 - 7.1.1 ?

1.  Added ability to specify the default entity/relation class to be used when creating entities/relations
2.  Removed automatic invocation of edit attributes following creation of entity/relation


7.1.2


1.  Added ability to validate all Alt-Shift-Z (Right click off item in LegendBox)
1a.   Added ability to validate entity attributes
1b.   Added ability to validate relation attributes
1c.   Added ability to validate relation constraints
2.  Added ability to show attributes
3.  Added ability to show relation constraints
4.  ToolTip for entities shows full label
5.  Removed validation messages during TA load
6.  Dragging entities into new container doesn't change their relative positioning information
7.  Perform automatic findNext after find.

7.1.3

1.  Fixed 7.1.2 so runs as applet.  Removed direct reference to System.getProperty() which throws exception when an applet.

7.1.4

1. Remove relationship constraints associated with entity classes
   when these are deleted.
2. Show first entity visited in History.
3. Correct fit to label when working with multi-line labels.
4. Show multiple line labels in both open and closed entities.
5. Reworked placement of entities when dragged into different
   containers.  By choice entities so dragged preserve their
   relative size and location.  However if this results in
   any dragged entity overlapping an entity already in the
   container, then the placement algorithm is used on all
   dragged entities to avoid overlapping with existing
   entities.

7.1.5

1. Added clustering

7.1.6 (Restructuring effort)

1. Added ability to invoke external clustering tool

2. Change all occurances of "colour" to "color"
3. Created EditableTa between UndoableTa and Ta         (encapsulates ability to edit Ta)
4. Created TemporalTa between Diagram    and UndoableTa (encapsulates ability to perform temporal edits)
5. Moved classes that assist Temporal actions next to the procedures that invoke each such class
6. All temporal actions have update at the start of their name
7. Avoided use of numeric values to identify attribute orders -- instead used final int names.
8. Used arrays rather than switch statements for recovering values/names of attributes
9. Moved image/angle into EntityClass where they correctly belonged
10.Changed all temporal classes so optionally created (via right clicking on Undo tab and enabling/disabling undo)
11.Removed cludges from switching containment
12.Change TA callbacks to LandscapeEditorCore so defined as calls to a generic interface
13 Changed TA error reporting so performed through generic interface

7.1.7

1. Fixed problems with Find (red boxes not being painted properly)
2. Added Attr tab

7.1.8

1. Reworked spring layout
2. Reworked cluster layout
3. Preserved old spring layout

7.1.9

1. Introduced light and dark bars into AttributeBox.java

7.1.10

Corrected a number of outstanding PR's.

1. Merely clicking on the background will now clear the current entity/relationship selection
2. Removed all munging when converting id's to labels
3. Find now begins when enter is pressed in the find pattern box.
4. Lifted edges now shown correctly as dotted.
5. Corrected conflicts so that lsedit now compiles under 5.0 (removed enum's etc as variable names)

7.1.11

1. Implemented the AAClusterLayout logic

7.1.12

1. Change construct column in AAClusterLayout so can parameterized

7.1.13

1. Added ACDC clusterer
2. Improved AA cluster

7.1.14

1. Enforce more sensible Z Ordering on EnitityComponents

7.1.15

1. Allow defaults to be changed on all cluster algorithms

7.1.16

1. Corrected how current values set in all layout algorithms
2. Saved state of all options
3. Reworked ArrowDimensions variables for cleanliness
4. Allow grid color to change

7.1.17

1. Save both the current and the default values
2. Added shading
3. Default open state now closed
4. Fixed bug in simplex configuration

7.1.18

1. Fixed bugs
2. Started work on bunch interface

7.1.19

1. Completed work on bunch interface

7.1.20

1. Allowed ability to duplicate edges/containment

7.1.21

1. Fixed bugs

7.1.22

1. Worked on making the clustering algorithms more friendly
2. Added some precustom configurations to AA
3. Improved the spring cluster algorithm

7.1.23

1. Improved the Cluster Metrics to cache old values
2. Implemented the redistribute function
3. Improved the spring cluster algorithm

7.1.24

1. Added ability to clear all elisions
2. Added ability to recursively open all nodes
3. Added ability to recursively close all nodes

7.1.25

1. Changed all references to java.exe to just this
2. Added group unconnected (W) operation
3. Added ability to select edges by identifying frame containing them
4. Added ability to use selected edges in recluster operation
5. Added ability to specify that want to see labels on edges (Visualisation Alt-D)
6. Added ability to see label/description of edge as tooltip for edge (Visualisation Alt-D)
   Edge attribute must be named label/description.
   N.B. Label is displayed 1/3 of way along a simple edge.
        Labels are not displayed on compound edges yet.
        The tooltip is displayed centrally.  
		This may not be ideal for some choices of edge layout.

7. Added edge label font to set of configurable fonts (Alt-Shift F)
8, Added ability to set labelcolor for an edge (on that edge or class of edge)
   Edge/edge class attribute must be named labelcolor
   Can also be specified manually as (r g b) as attribute of edge
9. Added ability to specify arrow head color
   Edge/edge class attribute must be named arrowcolor
   For a class this is a first order attribute
   Note that showing arrow colors is conditional on visualisation (Alt-D)
10.Added ability to align label direction relative to direction of edge

7.1.26

1. Added ability to have a fill color behind an edge label so can read when colors collide
2. Moved edge labels behind edges

7.1.27

1. Added ability to layout nodes horizontally within simplex algorithm

7.1.28

1. Made simplex layout handle planar graphs as a special case

7.1.29

1. Implemented ExecuteAction.java

7.1.30

1. Added ability to have up to 8 differing external commands

7.1.31

1. Added context sensitive help to LSEdit.

7.1.32

1. Added popup description rather than label
2. Added -T option to execution
3. Added '>' option to commands
4. Corrected logic for finding primary attributes when executing commands

7.1.33

1. Minor fix to how commands get invoked when edges are selected

7.1.34

1. Implemented snap to grid check box
2. Extended examples for command dialog box

7.1.35

1. Fixed problem with map box not repainting when use Window option to switch diagrams
2. Improved how examples were managed in commands
3. Introduced notion of strict TA
4. Permit $ROOT to be declared an instance of an arbitrary entity class, so that LSEdit
   can accept conforming TA.
5. Removed the legacy code to permit persisting of the RELN_HIDDEN attribute on the root
   
7.1.36

1. Save comments in the schema section of the TA

7.1.37

1. Allow legend labels to be shown in black
2. Implemented AVOID_COLLISION
3. Improved the network simplex algorithm handling of unconnected entities
4. Removed redraw after select all
5. Improved method of capturing comments in TA
6. Removed bug attempting to set font for non-existant text tree

7.1.38

1. Removed depreciated code
2. Made lsedit.ini loadable from an applet
3. Fixed some bugs

7.1.39 

 -- Massive changes --


1. Attribute.java

   Reworked structure of attributes to have only id and string value
   LSEdit now no longer stores attribute values in internal structures
   Enhanced parser so that records canonical form for attribute value
   so that greater likelyhood of attributes being duplicated
   Added routines to dynamically extract information of interest from
   the canonical form of the attribute value. 

2. Diagram.java

   Large cleanup of code.

3. EdgePoint.java

   Removed unnecessary fields from this structure

4. EditAttribute.java

   Made editing attribute values much simpler, since the attribute values
   are now simply a string, that can be editted in a text box.

5. EntityClass.java

   Reworked how known attribute values are bound to internal variables
   so as to avoid needless creation of attributes.

6. EntityInstance.java

   Changed elision logic from being a set of 5 vectors containing the names
   of the relation classes elided to a single BitSet in which bits indicate
   which types of possible elision and for which relation classes are elided.
   Added a new type of elision which is now used to convey when an entity
   is to be considered open, under each of the possible relation classes.
   Historically the open/close state of an entity was conveyed by eliding
   its destination edges to children, but this is problematic when the
   heirarchy is switched, since all closed entities became elided destination
   edges.
   Reversed the logic of elision so that absent elision entities are treated
   as closed.  This is the desirable way of initially showing a layout 
   particularly when dealing with huge layouts.
   Cleaned up a lot of the code

   Replaced 4 Edge Point vectors with a single one.

   Created a new "open" attribute to carry knowledge about the relation
   classes under which this entity is to be deemed open.

7. LandscapeEditorCore.java

   Cleaned up problem with menu fonts not being set early enough.
   Cleaned up error in computing font sizes.


8  LandscapeObject.java

   Change object holding attributes from a vector to an array to improve
   memory usage.  Encoded style by subclass to permit removal of style as
   physical attribute from relations.  Changed how attributes were stored
   in the array so that one could use caching to share attributes across
   entities when they had the same id and value.

9  LandscapeTokenStream.java

   Reworked parsing to handle two levels of parsing so as to capture the
   attribute value in entirety as a string.  Delay saving attributes to
   an entity until know how many need saving.  Under linenumberReader to
   avoid the need to track line numbers.

10 RelationInstance.java

   Encode style of relation in the bits reserved for flags.

11 StringCache.java

   Enhanced capabilities

12 Ta.java

   Changed update frequency for reporting loading to 1/1000 instead of
   1/250 to speed loads.
   Implemented attribute caching.

13 Ta_streamTokenizer.java

   Removed explicit input character buffering since this already handled
   by linenumberReader.
   Added formfeed \f to the list of legitimate delimiters.
   Added logic to read arbitrary attribute value and produce a
   canonical form of the same.

14 Util.java

   Improved method of parsing strings to ints and doubles.


Added source files

	AttributeCache.java
	AttributeValue.java
	ParseAttributeValue.java

Removed source files

	AttributeValueItem.java

7.1.40

1. Fixed problem with saving EdgePoints
2. Added version number to output TA

7.1.41

1. Fixed problems with class attributes not being seen

7.1.42

1. Put the default edgePoint's on the entity class to reduce frequency of edgePoint construction.
2. Add option to Legend to reset IO points for
    (a) Given relation class
	(b) All relation classes [right click blank area of legend box]
3. Improved logic so that changing IO factor for a relation class
   is reflected in the diagram as changed.
4. Removed ReallyDelete.java (Can be implemented by JOptionPane)
5. Removed ShowReallyDelete logic (Can be implemented by JOptionPane)

7.1.43

1. Allow color to be specified with +/- notation meaning adjust according to superclass color
2. Changed logic so that entity records the entity being drawn and relation derives this information
   indirectly from the entity instead of internally storing it.
   Reduces cost from 2 pointers per relation to 1 pointer per entity as expense of level of indirection
3. Allowed color to be specified using *
4. Allow label to inherit color from [inverted] background
5. Allow label color to move to black/white
6. Allow label color to be set on inverted background

7.1.44

1. Show icons of entities in TOC

7.1.45

1. Load TA in a thread other than the event thread so that messages show

7.1.46

1. Save the class declarations in the right order so appear in same order subsequently in legend
2. Added option to matrix layout to lay out in order vertically by default

7.1.47

1. Added ability to hide/show empty entities in the Legend Tab
2. Added ability to show instance counts in the legend tab.
3. Moved operations that do bulk fact base updates to active box
4. Added entities to the query box
5. Added ability to lift edges to the query box
6. Added option to delete active entities to the query box
7  Added option to delete active containers to the query box
8. Added option to delete active relations to the query box
9. Permitted these operations to be conditioned by the active flags
10. Permit relation lists in EntityInstance to be null (treat the same as empty)
11. Updated the online documentation

7.1.48

1. Added option to layout simplex algorithm using containment order or edge order

7.1.49

1. Reworked the forward/back trace to show real edges in the result box
2. Reworked the forward/back closure trace to employ forward/back trace recursively
3. Changed edge highlighting so should work even on lifted edges
4. Changed references to $(filename) in command examples to $(file) to conform with asx
5. Reworked how attributes are expanded in a command..
     If an attribute starts with "." then this is replaced by the value of the attribute
	 in the nearest ancestor entity having this same attribute.  For relations the parent
	 is presumed to be the source entity addressed by the relation.

	 Example:

	  DIR         file=/home/ijdavis"
       DIR        file=./src
	    SRC       file=./asx.cpp
		 FUNCTION file=.
		  CALL    file=.

      $(FILE) in the call would expand to:  /home/ijdavis/src/asx.cpp

6. Cleaned up resultbox.java logic to add in 1 and 2.
7. Changed springs layout so that if no edges connected within the diagram becomes a
   matrix layout.
8  Changed old springs layout similarly.

7.1.50

1. Changed navigation so that instead of navigating into empty boxes from results box etc
   navigate to parent and highlight item attempting to navigate to as a red box
2. Reworked attribute box to better handle sizing when all attributes are not fixed size
3. Rewrote how clients and suppliers are detected and compacted
4. Rewrote TOC to better size and removed the option to hide the TOC
5. Changed map highlight color to redbox color (to avoid collision with green color)
6. Reworked map box
7. Improved simplex layout
8. Show large integer numbers with suitable commas in the above dialog box.
9. Reworked the legend and query box
10.Added help to find operation

7.1.51 - 7.2.0 Massive rearchitecting

1. Changed interoperability by having EditableTa signal changes to listeners
2. Put all updates inside beginRedoUndo endRedoUndo pair
3. Have listeners repaint diagram on observed changes requiring it
4. Moved rescaling inward to the low level logic rather than being a highlevel operation
5. Where possible subclass tab boxes from generic TabBox.java
6. Removed DiagramCoordinates interface
7. Added clipboardListener.java
8. TabBoxes use changeListener to repaint
9. Keep vectors for specially flagged entities and relations
10.Implemented Query persists option
11.Added help button to edit constraints dialog
12.Move bounding sizes to entityComponent to save footprint
13.Move lifted edges to entityComponent to save footprint
14.Moved all update operations into TemporalTa where earlier in the object class files that are changed by update
15.Reworked shading so that doesn't cause race condition with painting
16.Reworked expand layout
17.Reworked frameModeHandler to make faster
18.Reworked groupModeHandler to make faster

7.2.1

1 Minor corrections to correct subtle problems

7.2.2

1. Fixed problem with relations getting grouped more than once
2. Fixed problems with Find not redboxing

7.2.3

1. Fixed problems with cardinals not showing
2. Moved m_freq into RelationComponent to save footprint
3. Fixed problems with freq not being computed because RelationComponent created too late

7.2.4

1. Fixed variety of problems
2. Extended find operation dialog considerably
3. Added ResultBox configuration options

7.2.5

1. Made the parser allow changes to entity instance classes on the fly

7.2.6

1. Fixed problem with saving version in TA

7.2.7
 
1. Added the class_icon property which permits entities to be represented by icons.
2. Added the ability to press ALT while clicking entity to rescale just this entity.
3. Added Icon Cache so that the same icon in different classes is not read multiple times.
4. Allowed labels to be placed at top/bottom/centre/nowhere using a visualisation combo box.
5. Put Edit Inheritance Rules inside a scroll pane so scrolls when list too long to fit.
 
7.2.8
 
1. Allowed legend classes to be shown indented as an inheritance hierarchy
2. Reworked selection of items when showing inheritance hierarchy in legend tab box.
3. Reworked LegendBox and QueryBox to use a common ERBox (Entity-Relation)
4. Added the ability to hover on entity from the results box, TOC or history box
5. Fixed reporting of multiple parents when trying to form a new contains hierarchy

7.3.0
 
1. A major revision to how options are managed.
2. Eliminated ArrowDimensions.java
3. Created OptionsDialog.java

7.3.1
 
1. Reworked how things were read/written
 
7.3.2

1. Formed a spanning tree rather than simply use the contains heirarchy or fail
2. Used shorts instead of doubles internally to represent range 0-1 or undefined
 
7.3.3

1. Further work to improve the notion of spanning trees
2. Permit multiple relations to participate in the spanning tree
 
7.3.4
 
1. Introduced notion of root cause analysis
2. Removed ability to reverse edges in legend
3. Fixed bug with persists flag in query box
4. Fixed problem with back tracing
5. Fixed problem with popup when diagram magnified
6. Permitted to group redboxes and add to group
7. Allow icons to scale cleanly according to original shape
 
7.3.5
 * 
1. Change things so that Cmdb could call all of lsedit functionality
2. Fixup applet software
3. Created view tab

7.3.6

1. Quoted attributes generated by CMDB so output TA written correctly
2. Don't write out attribute information "id { }" when there is no printable attributes
3. Fixed up how visible relation classes was computed to make it safer and work all the time
4. Added ability to see command line invocation parameters in help (for applet debugging)
5. Fixed startentity parameter so applet specification overrides option specification
6. Added ability to view attributes and class attributes from TOC.
7. Added ability to load and save views of data
8. Scrapped attempt to avoid collisions in springLayout2 (too expensive)
 
7.3.7
 
1. Permit relations to be dynamically reversed by checking the reverse checkbox in the legend
2. Handle the notion of a reversed label
 
7.3.8

1. Reworked the spring2 layout algorithm
2. Reworked options dialog to add yet more options
3. Improved reporting of reversed labels in legend
4. Rewrote logic to specify spanning edges
5. Got views working
6. Added -f option to allow a forward query on initial load (forward parameter in applet).
7. Changed contents query to v/Shift v to make more consistent with b/Shift B and f/Shift F
8. Permit closure to be a specified number of steps
9. Fixed bug in how tracing worked with nested paths
10.Reworked how redbox'ing is displayed
 * 
7.3.9
 * 
1. Cleaned up problems with split panes somehow introduced (really hard)
 * 
7.3.10
 * 
 * Removed 'e' from start of CMDB CI's
 * Redbox and group any entity on path from item being viewed to item now being viewed.. (ie show child we've come from)
 * Added option to not show toolbar
 
 7.3.11
 
 1. Fixed problems with view not rescaling when exit view
 2. Removed title from the list of primary attributes for an entity to save space
 3. Have a path query on no selected item be the same as the path query on the drawRoot
 4. Activate result box for any path query
 5. Integrated query content with other path queries
 6. Added Home to navigate to root
 7. Changed complex Alt-u and Shift-Alt-u navigation in history to PAGE-DOWN & PAGE_UP
 8. Got font changes working in TOC
 9. Added ability to configure Fonts for tabs.
 10.Added ability to change elisions for entityClass/relationClass/EntityInstance.
 11.Added ability to view elisions as icon
 12.Added ability to show elision icons by right clicking on legend relation classes
 13.Added ability to clear elision icons by right clicking legend box.
 
 */
