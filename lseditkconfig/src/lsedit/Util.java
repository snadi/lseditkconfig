package lsedit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;

public class Util
{
	// 5 Relation styles can be represented in 3 bits
	public static final int LINE_STYLE_NORMAL			= 0;
	public static final int LINE_STYLE_DASHED			= 1;
	public static final int LINE_STYLE_DOTTED			= 2;
	public static final int LINE_STYLE_INSCRIBED		= 3;
	public static final int LINE_STYLE_UNDEFINED        = 7;


	public static final String[] lineStyleName =
		{ 
				"Normal", 
				"Dashed", 
				"Dotted", 
				"Inscribed"
		};

	protected static final int DASH_LEN			= 4;
	protected static final int DASH_GAP			= 3;

	protected static final int DOT_LEN			= 0;
	protected static final int DOT_GAP			= 4;

	protected static final int GAP		        = 5;
	protected static final int INSET	        = 12;
	protected static final int OUTSET	        = 5;

	// Also used by edgePoint

//	static boolean first = true;

	// --------------
	// Public methods
	// --------------

	public static String toLocaleString()
	{
		DateFormat	dateFormat = DateFormat.getDateTimeInstance();
		Date		date       = new Date();
		
		String ret = dateFormat.format(date);
		date = null;
		return(ret);
	}

	/* Computes the angle to the horizontal for point at (w,h) from (0,0) 
	 * Result returned always in the range 0 to 360 degrees
	 */

	public static double degrees(double w, double h)
	{
		double rotate;

/*
		if (first) {
			double	w1, h1;
			first = false;
			for (w1 = -1.0; w1 <= 1.0; w1 += 0.25) {
				for (h1 = -1.0; h1 <= 1.0; h1 += 0.25) {
					System.out.println("w=" + w1 + " h=" + h1 + " angle=" + degrees(w1, h1));
		}	}	}
*/

		if (h == 0) {
			if (w < 0) {
				return 180.0;		// Center left
			}
			return 0;				// Center right
		}
		if (w == 0) {
			if (h < 0) {
				return 270.0;		// Center top
			}
			return 90.0;			// Center bottom
		}

		if (w >= 0) {
			if (h >= 0) {
				rotate = 0;
			} else {
				rotate = 360.0;
			}
		} else {
			rotate = 180.0;
		}
		return rotate + Math.atan(h/w)*180.0/Math.PI;
	}

	public static void beep() 
	{
		System.out.print("\007");
		System.out.flush();
	}	 

	public static String formatFraction(double val)
	{
		if (val >= 1.0) {
			if (val == 1.0) {
				return("1");
			}
			return "" + val;
		}
		if (val <= 0) {
			if (val == 0) {
				return("0");
			}
			return "" + val;
		}
		int	e1, e2;

		e1 = (int) (val*10);
		e2 = ((int) (val*100)) - (e1*10);
		if (e2 == 0) {
			return "0." + e1;
		}
		return ("0." + e1) + e2;
	}

	public static boolean isBlank(String s)
	{
		for (int i = s.length(); i > 0; ) {
			if (s.charAt(--i) != ' ') {
				return false;
		}	}
		return true;
	}

	public static String formFileName(String dir, String name)
	{
		if (dir == null || dir.length() == 0) {
			return name;
		}
		String separator = Version.Detail("file.separator");
		if (separator == null || separator.length() == 0) {
			separator = "/";
		}
		return dir + separator + name;
	}

	public static String getLineStyleName(int style)
	{
		if (style < 0 || style >= lineStyleName.length) {
			return "";
		}
		return lineStyleName[style];
	}

	public static boolean drawStringClipped(Graphics g, String str,	double x, double y, double width, double height, boolean centered, boolean underlined, boolean invertBackground)
	{
		FontMetrics fm = g.getFontMetrics();


		int len = str.length();
		int pos = len-1;

		if (fm.stringWidth(str) > width) {
			while(pos >= 0 && fm.stringWidth(str.substring(0, pos) + "...") > width) {
				pos--;
			}
		}

		if (pos < 0) {
			return false;
		}


		String dstr;
		double xpos;

		if (pos == len-1) {
			dstr = str;

			double sw = (double) fm.stringWidth(str);

			xpos = (centered ? x+width/2-sw/2 : x); 
		} else {
			dstr = str.substring(0, pos) + "...";

			xpos = x;
		}

		int	x1, y1;

		x1 = (int) (xpos + 0.5);
		if (centered) {
			y1 = (int) (y + height/2 + fm.getHeight()/2 + 0.5);
		} else {
			y1 = (int) (y + fm.getHeight() + 0.5);
		}


		if (invertBackground) {
			Color	color  = g.getColor();
			int		height1 = fm.getHeight();
			g.setColor(ColorCache.getInverse(color.getRGB()));
			g.fillRect(x1, y1-height1, fm.stringWidth(dstr), height1);
			g.setColor(color);
		}

		g.drawString(dstr, x1, y1);

		if (underlined) {
			int sw = fm.stringWidth(dstr);

			g.drawLine(x1, y1, x1+sw, y1);
		}

		return (pos == len-1);
	}

	protected static int longestSubStr(String str, FontMetrics fm, int width) 
	{
		int lpos, pos = str.indexOf('\n');

		if (pos > 0) {
			// Consider only the next line of text
			str = str.substring(0, pos);
		}
		if (fm.stringWidth(str) <= width) {
			return str.length();
		}

		for (lpos = -1;; lpos = pos) {
			pos = str.indexOf(' ', lpos+1);
			if (pos < 0) {
				break;
			}
			if (fm.stringWidth(str.substring(0, pos)) > width) {
				break;
		}	}
		if (lpos == -1) {
			return str.length();
		}
		return lpos;
	}


	public static Dimension stringWrappedDim(Graphics g, String str)
	{
		FontMetrics fm = g.getFontMetrics();
		int			w     = 0;
		int			lines = 0;
		int			lth   = str.length();
		int			start, toend, w1, fmh, h;

		for (start = 0;start < lth;start = toend+1) {
			toend = str.indexOf('\n', start);
			if (toend < 0) {
				toend = lth;
			}
			w1 = fm.stringWidth(str.substring(start, toend));
			if (w1 > w) {
				w = w1;
			}
			++lines;
		}
		if (lines == 0) {
			h = 0;
		} else {
			fmh = fm.getHeight();
			h   = ((3 * lines + 1) * fmh)/4;
		}
		return (new Dimension(w, h));
	}

	public static boolean drawStringWrapped(Graphics g, String str,	double x, double y, double width, double height, boolean centered, boolean underlined, boolean invertBackground)
	{
		FontMetrics fm = g.getFontMetrics();
		Vector		strs = new Vector();


		if (str.indexOf(' ') < 0 && str.indexOf('\n') < 0) {
			return drawStringClipped(g, str, x, y, width, height, centered, underlined, invertBackground);
		}

		String		dstr;
		double		xpos;
		int			pos;
		int			x1, y1;
		Enumeration	en;

		for (;;) {
			pos = longestSubStr(str, fm, (int) width); 
			strs.addElement(str.substring(0, pos));
			if (pos == str.length()) {
				break;
			}
			str = str.substring(pos + 1);
		}

		int fh = (fm.getHeight() * 3)/4;
		int ht = strs.size() * fh;

		double ypos = (centered ? y + (height - ht)/2 + fh : y + fh);

		if (invertBackground) {
			int		minx, maxx, miny, maxy;
			double	ypos1 = ypos;

			minx = miny = 0x7FFFFFFF;
			maxx = maxy = 0;

			for (en = strs.elements(); en.hasMoreElements(); ) {
				str     = (String) en.nextElement();
				int len = str.length();
				pos     = len-1;

				while(pos >= 0 && fm.stringWidth(str.substring(0, pos)) > width) {
					pos--;
				}

				if (pos >= 0) {

					if (pos == len-1) {
						dstr = str;

						double sw = (double) fm.stringWidth(str);
						xpos = (centered ? x+width/2-sw/2 : x); 
					} else {
						dstr = str.substring(0, pos);

						xpos = x;
					}

					x1 = (int) (xpos  + 0.5);
					y1 = (int) (ypos1 + 0.5);

					if (x1 < minx) {
						minx = x1;
					}
					if (y1 < miny) {
						miny = y1;
					}

					x1 += fm.stringWidth(dstr);
					if (x1 > maxx) {
						maxx = x1;
					}
					if (y1 > maxy) {
						maxy = y1;
					}
					ypos1 += fh;
			}	}

			if (minx <= maxx) {
				Color	color   = g.getColor();
				g.setColor(ColorCache.getInverse(color.getRGB()));
				miny -= fh;
				g.fillRect(minx, miny, maxx-minx, maxy-miny);
				g.setColor(color);
		}	}

		for (en = strs.elements(); en.hasMoreElements(); ) {
			str     = (String) en.nextElement();
			int len = str.length();
			pos     = len-1;

			while(pos >= 0 && fm.stringWidth(str.substring(0, pos)) > width) {
				pos--;
			}

			if (pos >= 0) {
				

				if (pos == len-1) {
					dstr = str;

					double sw = (double) fm.stringWidth(str);
					xpos = (centered ? x+width/2-sw/2 : x); 
				} else {
					dstr = str.substring(0, pos);

					xpos = x;
				}

				x1 = (int) (xpos + 0.5);
				y1 = (int) (ypos + 0.5);

				g.drawString(dstr, x1, y1);
				if (underlined) {
					int sw = fm.stringWidth(dstr);
					g.drawLine(x1, y1, x1+sw, y1);
				}
				ypos += fh;
		}	}
		return true;
	}

	public static boolean isHTTP(String path) 
	{
		return path.length() > 7 && (path.substring(0,7).equals("http://") || path.substring(0,7).equals("HTTP://"));
	}
	
	public static String prefixOf(String name) 
	{
		int ind = name.lastIndexOf('.');


		if (ind >= 0) {
			return name.substring(0, ind);
		}
		return name;
	}

 	public static boolean	defined(short val)
	{
		return(val != Short.MIN_VALUE);
	}
	
	public static short undefined()
	{
		return Short.MIN_VALUE;
	}
	
	// Translate a short to a double in the range 0.0 -> 1.0 or -1.0 if undefined
	
	public static double shortToRelative(short value)
	{
		if (!defined(value)) {
			// Undefined value
			return -1.0d;
		}
		return 0.5d + (((double) value) / 65534.0d);	// shorts range from -32767 (-2^15+1) to 32767 (2^15-1)
														// so the factor here ranges from -0.5 to +0.5
	}
	
	public static short relativeToShort(double value)
	{
		if (value < 0.0 || value > 1.0) {
			return undefined();
		}
		return (short) ((value - 0.5d) * 65534.0d);
	}
	
	public static short parseShort(String str) 
	{
		short val;

		try {
			val = Short.parseShort(str);
		} catch (Exception e) {
			System.out.println("Can't convert '" + str + "' to a short");
			val = Short.MIN_VALUE;
		}
		return val;
	}

	public static double parseDouble(String str, double default0) 
	{
		double val;

		try {
			val = Double.parseDouble(str);
		} catch (Exception e) {
			System.out.println("Can't convert '" + str + "' to a double");
			val = default0;
		}
		return val;
	}
	
	public static double parseDouble(String str) 
	{
		return parseDouble(str, 0.0d);
	}
	
	public static int parseInt(String str, int default0) 
	{
		int	val;

		try {
			val = Integer.parseInt(str);
		} catch (Exception e) {
			System.out.println("Can't convert '" + str + "' to an int");
			val = default0;
		}
		return val;
	}
	
	public static int parseInt(String str)
	{
		return parseInt(str, 0);
	}

	public static boolean parseBoolean(String str) 
	{
		return str.equals("true");
	}

	/* Handle legacy way of encoding relative values */
	
	public static short parseRelativeValue(String str)
	{
		if (str.indexOf('.') >= 0) {
			double dval = parseDouble(str, -1.0d);
			return relativeToShort(dval);
		}
		return parseShort(str);
	}
	
	public static String quoted(String str) 
	{
		if (str.indexOf(' ') >= 0) {
			return "\"" + str + "\"";
		}
		return str;
	}


	protected static String doExpand(String src, String id, LandscapeEditorCore ls) 
	{
		String link = "";

		int pos = 0;
		int len = src.length();
		boolean found;

		MsgOut.dprintln("Expand: " + src);

		do {
			int ind = src.indexOf('$', pos);
			found = (ind == 0 || (ind > 0 && src.charAt(ind-1) != '\\')) &&	(ind+1 < len);
			if (found) {
				int endInd = src.indexOf('$', ind+1);
				if (endInd < ind) {
					MsgOut.println("Missing delimitting '$'" + " in expansion variable");
					return null;
				}
				link += src.substring(pos, ind);
				String var = src.substring(ind+1, endInd);

				if (var.equals("ID")) {
					link += quoted(id);
				} else if (var.equals("IDPREFIX")) {
					int dind = id.lastIndexOf('.');
					if (dind >= 0) {
						link += quoted(id.substring(0, dind));
					} else {
						link += quoted(id);
					}
				} else if (var.equals("IDSUFFIX")) {
					int dind = id.lastIndexOf('.');
					if (dind >= 0) {
						link += id.substring(dind+1);
					}
				} else if (var.equals("DGDIR")) {
					String path = ls.getDiagram().getAbsolutePath();
					String dir = dirFromPath(path);
					link += dir;
				} else if (var.equals("DGSUFFIX")) {
					String path = ls.getDiagram().getAbsolutePath();
					String name = nameFromPath(path);
					int dind = name.indexOf('.');

					if (dind >=0)  {
						link += name.substring(dind+1);
					}

				} else {
					// Assume it's a PARAM name
					String value = ls.getParameter(var);
					if (value != null) {
						link += quoted(value); 
					} else {
						MsgOut.println("Parameter not found: "	+ "'" + var + "'");
						return "";
					}
				}
				pos = endInd+1;
			}

		} while (found && pos < len);



		if (pos < len) {
			link += src.substring(pos);
		}
		MsgOut.dprintln("Result: " + link);
		return link;
	}



	public static String expand(String src, String id, LandscapeEditorCore ls)
	{
		String nstr1, nstr2;

		nstr2 = doExpand(src, id, ls);
		do {
			nstr1 = nstr2;
			nstr2 = doExpand(nstr1, id, ls);
		} while (!nstr1.equals(nstr2));
		return nstr1;
	}



	public static String expand(String src, LandscapeEditorCore ls) 
	{
		return expand(src, "", ls);
	}

	public static JFrame getFrame(Component c) 
	{
		for (; c != null; c = c.getParent()) {
			if (c instanceof JFrame) {
				return (JFrame) c;
		}	}
		return null;
	}

	public static String dirFromPath(String path) 
	{
		int ind1 = path.lastIndexOf('/');
		int ind2 = path.lastIndexOf('\\');

		if (ind1 > ind2) {
			return path.substring(0, ind1);
		} else if (ind2 > ind1) {
			return path.substring(0, ind2);
		}
		return "";
	}



	public static String nameFromPath(String path) 
	{
		int ind1 = path.lastIndexOf('/');
		int ind2 = path.lastIndexOf('\\');

		if (ind1 > ind2) {
			return path.substring(ind1+1);
		}

		if (ind2 > ind1) {
			return path.substring(ind2+1);
		}
		return path;
	}



	public static boolean isBlack(Color c) 
	{
		if (c == Color.black) {
			return true;
		}

		if (c.getRed() != 0) {
			return false;
		}
		if (c.getGreen() != 0) {
			return false;
		}
		return(c.getBlue() == 0);
	}

	protected static void drawLine(Graphics g, LineWalker lw) 
	{
		ScreenPoint p1 = new ScreenPoint(0, 0);
		ScreenPoint p2 = new ScreenPoint(0, 0);

		while(lw.morePoints()) {
			 lw.nextPoints(p1, p2);
			 g.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
	}

	protected static void 
	drawInscribedLine(Graphics g, int x1, int y1, int x2, int y2) 
	{
		Color cc = g.getColor();

		g.setColor(Color.gray);
		g.drawLine(x1, y1, x2, y2);
		g.setColor(Color.white);

		if (x1 == x2) {
			g.drawLine(x1+1, y1, x2+1, y2);
		} else {
			g.drawLine(x1, y1+1, x2, y2+1);
		}
		g.setColor(cc);
	}

	public static void drawTailPoint(Graphics g, int x, int y) 
	{
		int hb = RelationInstance.NEAR_PIXEL_SIZE/2;
		g.fillOval(x-hb, y-hb, hb*2, hb*2);
	}

	public static void drawOutlineBox(Graphics g, int x, int y, int width, int height)
	{
//		g.setColor(Color.gray);

		// Has 3D look
		g.drawLine(x, y, x + width, y);
		g.drawLine(x + width -1, y+1, x+ width -1, y+ height -1);
		g.drawLine(x + width -1, y+height-1, x+1, y+height-1);
		g.drawLine(x, y+height, x, y);
		g.setColor(Color.white);

		g.drawLine(x+1, y+1, x+width, y+1);
		g.drawLine(x+width, y+1, x+width, y+height);
		g.drawLine(x+width, y+height, x+1, y+height);
		g.drawLine(x+1, y+height, x+1, y+1);
	}

	public static void drawOutlineRedBox(Graphics g, int x, int y, int width, int height)
	{
		Color	color = Color.red.darker();
		Color	rev   = ColorCache.getInverse(color.getRGB());
		int		x1    = x + width;
		int		y1    = y + height;
		int		xmax  = x + 4;
		int		ymax  = y + 4;
		
		if (xmax > x1) {
			xmax = x1;
		}
		if (ymax > y1) {
			ymax = y1;
		}
		
		while(x <= xmax && y <= ymax) {					
			g.setColor(color);
			g.drawLine(x, y,  x,  y1);
			g.drawLine(x1,y,  x1, y1);
			g.setColor(rev);
			g.drawLine(x, y,  x1, y);
			g.drawLine(x, y1, x1, y1);
			++x;
			--x1;
			++y;
			--y1;
	}	}

	public static void drawGroupBox(Graphics g, int x, int y, int w, int h, String label)
	{
		FontMetrics fm;
		int			len, ht, inset, x2;

		g.setFont(EntityInstance.getSmallFont());

		fm    = g.getFontMetrics();
		len   = fm.stringWidth(label);
		ht	  = fm.getHeight();
		inset = Math.min(INSET, w/12);

		if (w < inset + OUTSET) {
			drawOutlineBox(g, x, y, w, h);
		} else {
			if (len+GAP*2 > w-inset-OUTSET) {
				x2 = x + w - OUTSET;
			} else {
				x2 = x + inset + len + GAP*2;
			}

//			g.setColor(Color.gray);

			// Has 3D look
			g.drawLine(x, y+h, x, y);
			g.drawLine(x, y, x+inset, y);
			g.drawLine(x2, y, x+w, y);
			g.drawLine(x+w-1, y+1, x+w-1, y+h-1);
			g.drawLine(x+w-1, y+h-1, x+1, y+h-1);
			g.setColor(Color.white);
			g.drawLine(x+1, y+h, x+1, y+1);
			g.drawLine(x+1, y+1, x+inset, y+1);
			g.drawLine(x2, y+1, x+w, y+1);
			g.drawLine(x+w, y+1, x+w, y+h);
			g.drawLine(x+w, y+h, x+1, y+h);
		}
	}

	public static void drawGroupBoxLabel(Graphics g, int x, int y, int w, String label)
	{
		Option		option;
		FontMetrics fm;
		int			len, ht, inset, x2;

		option = Options.getDiagramOptions();
		g.setFont(EntityInstance.getSmallFont());

		fm    = g.getFontMetrics();
		len   = fm.stringWidth(label);
		ht	  = fm.getHeight();
		inset = Math.min(INSET, w/12);

		if (w >= inset + OUTSET) {
			if (len+GAP*2 > w-inset-OUTSET) {
				x2 = x + w - OUTSET;
			} else {
				x2 = x + inset + len + GAP*2;
			}
			drawStringClipped(g, label, x+inset+GAP, y - (ht*2)/3, x2-x-GAP*2, 100, false, false, option.isLabelInvertBackground());
	}	}

	public static String mungeName(String name) {
		String tmp = name.replace(' ', '_');

		tmp = tmp.replace('/', '-');
		tmp = tmp.replace(',', '_');
		return tmp.replace('\\', '-');
	}

	public static String hashEdge(RelationClass rc, EntityInstance src, EntityInstance dst)
	{
		return src.getId() + rc.getId() + dst.getId();
	}

	public static int round(double val) {
		return (int) (val + 0.5);
	}

	// Line drawing stuff

	protected static final int DOT_LEN_REG		= 0;
	protected static final int DOT_LEN_PRNT		= 1;

	protected static int getDotLen(Graphics g) 
	{
		return DOT_LEN_REG;
	}

	// --------------
	// Public methods
	// --------------


	public static void drawSegment(Graphics g, int style, int x1, int y1, int x2, int y2)
	{
		int	x, y, xshift, yshift, i, linewidth;

		x         = 0;
		y         = 0;
		xshift    = 0;
		yshift    = 0;
		linewidth = Options.getLineWidth();
		
		for (i = 0; i < linewidth; ++i) {
			switch(style) {
				case Util.LINE_STYLE_NORMAL:
				{
//					System.out.println("Util.drawSegment line (" + x1 + ", " + y1 + ")->(" + x2 + ", " + y2 + ")" );
					g.drawLine(x1+x, y1+y, x2+x, y2+y);
					break;
				}
				case Util.LINE_STYLE_DOTTED:
				{
//					System.out.println("Util.drawSegment dotted (" + x1 + ", " + y1 + ")->(" + x2 + ", " + y2 + ")" );
					LineWalker lw = new LineWalker(x1+x, y1+y, x2+x, y2+y, getDotLen(g), DOT_GAP);
					drawLine(g, lw);
					break;
				}
				case Util.LINE_STYLE_DASHED:
				{
//					System.out.println("Util.drawSegment dashed (" + x1 + ", " + y1 + ")->(" + x2 + ", " + y2 + ")" );
					LineWalker lw = new LineWalker(x1+x, y1+y, x2+x, y2+y, DASH_LEN, DASH_GAP);
					drawLine(g, lw);
					break;
				}
				case Util.LINE_STYLE_INSCRIBED:
				{
					drawInscribedLine(g, x1, y1, x2, y2);
			}	}

			x = -x;
			y = -y;

			if ((i & 1) == 0) {
				if (i == 0) {
					int w, h;

					w = x2 - x1;
					h = y2 - y1;

					if (w <= 0 && h <= 0) {
						w = -w;
						h = -h;
					}
					if (w >= 0 && h >= 0) {
						if (2*h >= w) {
							xshift = 1;
						} 
						if (2*w >= h) {
							yshift = -1;
						}
					} else {
						if (w < 0) {
							w = -w;
						}
						if (h < 0) {
							h = -h;
						}
						if (2*h >= w) {
							xshift = 1;
						} 
						if (2*w >= h) {
							yshift = 1;
					}	}
//					System.out.println("xshift=" + xshift + " yshift=" + yshift);
				} 
				x += xshift;
				y += yshift;
	}	}	}

	// Returns the ratio of the arrow length to the length

	public static double getArrow(int srcX, int srcY, int dstX, int dstY, int[] x, int[] y, int arrowWeight)
	{
		double	len;

		int dx   = srcX - dstX;
		int dy   = srcY - dstY;

		if (dx == 0 && dy == 0) {
			len = 0;
		} else {
			Option option      = Options.getDiagramOptions();
			double arrowLength = option.getArrowLength();
			double arrowArc    = option.getArrowArc();
			double theta  = Math.atan2(dy, dx);
			
			len   = Math.sqrt(dx*dx + dy*dy);
			while (arrowWeight > 1) {
				arrowLength += 4;
				arrowWeight >>= 1;
			}
			arrowLength  = Math.min(arrowLength, len);

			double ax    = dstX + arrowLength*Math.cos(theta-arrowArc);
			double ay    = dstY + arrowLength*Math.sin(theta-arrowArc);
			double bx    = dstX + arrowLength*Math.cos(theta+arrowArc);
			double by    = dstY + arrowLength*Math.sin(theta+arrowArc);

			x[0] = dstX;
			y[0] = dstY;
			x[1] = (int) Math.round(ax);
			y[1] = (int) Math.round(ay);
			x[2] = (int) Math.round(bx);
			y[2] = (int) Math.round(by);

			len = arrowLength/len;
		}
		return(len);
	}

	public static double drawArrowHead(Graphics g, int x1, int y1, int x2, int y2, int arrowWeight) 
	{
		int[]	x      = new int[3];
		int[]	y      = new int[3];
		double	len    = getArrow(x1, y1, x2, y2, x, y, arrowWeight);
		
		if (len != 0) {
			Option	option = Options.getDiagramOptions();

			if (option.isFillArrowhead()) {
				g.fillPolygon(x, y, 3);
			} else {
				g.drawPolygon(x, y, 3);
		}	}
		return len;
	}

	public static String encodedURLname(String name)
	{
		boolean first = true;
		String	ret   = "";
		char	c;
		int		i, lth;

		lth = name.length();
		for (i = 0; i < lth; ++i) {
			c = name.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				if (!first) {
					c += 'a' - 'A';
				}
			} else if  (c >= 'a' && c <= 'z') {
				if (first) {
					c += 'A' - 'a';
				}
			} else if (c < '0' || c > '9') {
				first = true;
				continue;
			}
			ret += c;
			first = false;
		}
		return ret;
	}

	public static String taColor(Color color1)
	{  
		int		red, green, blue, alpha; 
		String	ret;

		red    = color1.getRed();
		green  = color1.getGreen();
		blue   = color1.getBlue();
		alpha  = color1.getAlpha();

		ret  = "(" + red + " " + green + " " + blue;
		if (alpha != 255) {
			ret += " " + alpha;
		}
		ret += ")"; 
		return StringCache.get(ret);
	}
	
	public static Color colorTa(String string)
	{
		int		state = 0;
		int		r     = 0;
		int		g     = 0;
		int		b     = 0;
		int		a     = 255;
		int		i;
		char	c;
		
		for (i = 0; i < string.length(); ++i) {
			c = string.charAt(i);
			switch (state) {
			case 0:
				switch (c) {
				case '(':
					state = 1;
				case ' ':
					continue;
				}
				break;
			case 1:
				if (c == ' ') {
					continue;
				}
				if (c < '0' || c > '9') {
					break;
				}
				state = 2;
			case 2:
				if (c >= '0' && c <= '9') {
					r = r * 10 + (c - '0');
					continue;
				}
				if (r > 255) {
					break;
				}
				state = 3;
			case 3:
				if (c == ' ') {
					continue;
				}
				if (c < '0' || c > '9') {
					break;
				}
				state = 4;
			case 4:
				if (c >= '0' && c <= '9') {
					g = g * 10 + (c - '0');
					continue;
				}
				if (g > 255) {
					break;
				} 
				state = 5;
			case 5:
				if (c == ' ') {
					continue;
				}
				if (c < '0' || c > '9') {
					break;
				}
				state = 6; 
			case 6:
				if (c >= '0' && c <= '9') {
					b = b * 10 + (c - '0');
					continue;
				}
				if (b > 255) {
					break;
				}
				state = 7;
			case 7:
				switch (c) {
				case ')':
					state = 10;
				case ' ':
					continue;
				}	
				if (c < '0' || c > '9') {
					break;
				}
				a     = 0;
				state = 8;
			case 8:
				if (c >= '0' && c <= '9') {
					a = a * 10 + (c - '0');
					continue;
				}
				if (a > 255) {
					break;
				}
				state = 9;
			case 9:
				switch (c) {
				case ')':
					state = 10;
				case ' ':
					continue;
				}
				break;
			case 10:
				if (c == ' ') {
					continue;
				}
				break;
			}	
			return null;
		}
		if (state == 10) {
			return ColorCache.get(r, g, b, a);
		}
		return null;
	}
}

