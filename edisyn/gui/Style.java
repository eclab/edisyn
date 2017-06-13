/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.gui;

import edisyn.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.*;

/**** 
      Constants which determine the look and feel of the software.
*/

public class Style
    {
    
    /////// GLOBAL CONSTANTS
    
    /** Background color */
    public static final Color BACKGROUND_COLOR = Color.BLACK;
    /** Text color */
    public static final Color TEXT_COLOR = Color.WHITE;
    /** Transparent color. */
    public static final Color TRANSPARENT = new Color(0,0,0,0);
    /** Small font, primarily for labels, button and combo box text. */
    public static Font SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
    /** Medium-sized font, used primarily in the center of a Dial. */
    public static final Font MEDIUM_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    /** Large Font, primarily for category headers. */
    public static final Font LARGE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

    
    /////// SYTH PANEL CONSTANTS
    
    /** The synth panel's insets */
    public static final Insets SYNTH_PANEL_INSETS = new Insets(40, 40, 40, 40);


    /////// CATEGORY CONSTANTS
    
    /** Font for category borders. */
    public static final Font CATEGORY_FONT = LARGE_FONT;
    /** Stroke width for category borders. */
    public static final int CATEGORY_STROKE_WIDTH = 3;
    /** The category border. */
    public static final Border CATEGORY_BORDER = BorderFactory.createEmptyBorder(0, -2, 4, 4);
    /** Color of the first category on a page. */
    public static final Color COLOR_A = Color.green;
    /** Color of the second category on a page. */
    public static final Color COLOR_B = new Color(128, 128, 255);
    /** Color of the third category on a page. */
    public static final Color COLOR_C = Color.yellow;
    /** Color for the category holding critical global stuff like patch name, patch number, etc. */
    public static final Color COLOR_GLOBAL = Color.white;
    
    
    /////// CHOOSER CONSTANTS
    
    public static Insets CHOOSER_INSETS = null;  // no insets
    

    /////// VBOX AND HBOX CONSTANTS

    /** Insets for VBoxes, by default zero. */
    public static final Insets VBOX_INSETS = new Insets(0, 0, 0, 0);
    /** Insets for HBoxes, by default zero. */
    public static final Insets HBOX_INSETS = new Insets(0, 0, 0, 0);


    /////// DIAL CONSTANTS

    /** Color of the unset region in Dials etc. */ 
    public static final Color DIAL_UNSET_COLOR = Color.GRAY;
    /** Color of the set region in Dials etc. when being updated. */
    public static final Color DIAL_DYNAMIC_COLOR = Color.RED;
    /** Width of the set region in Dials etc.  */
    public static final float DIAL_STROKE_WIDTH = 4.0f;
    /** The stroke for the set region in Dials etc. */
    public static final BasicStroke DIAL_THIN_STROKE = new BasicStroke(DIAL_STROKE_WIDTH / 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    /** The stroke for the unset region in Dials etc. */
    public static final BasicStroke DIAL_THICK_STROKE = new BasicStroke(DIAL_STROKE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    /** Font used in the center of a dial. */
    public static final Font DIAL_FONT = MEDIUM_FONT;
    /** Insets for labelled dials to set them apart slightly from one another. */
    public static final Insets LABELLED_DIAL_INSETS = new Insets(0, 3, 0, 3);


    /////// ENVELOPE DISPLAY CONSTANTS

    /** Degree of Transparency of the fill region with respect to the stroked lines. */
    public static final double ENVELOPE_DISPLAY_FILL_TRANSPARENCY = 0.25;
    /** Thickness of effective border around the Envelope Display, except the top. */
    public static final int ENVELOPE_DISPLAY_BORDER_THICKNESS = 10;
    /** Thickness of effective top border above the Envelope Display. */
    public static final int ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS = 0;
    

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() 
    	{
        return (OS.indexOf("win") >= 0);
	    }

    public static boolean isMac() 
    	{
        return (OS.indexOf("mac") >= 0);
	    }

    public static boolean isUnix() 
    	{
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    	}
    	
    static
    	{
    	if (true) //isUnix())
    		{
    		SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
    		CHOOSER_INSETS = new Insets(0, 0, 2, 4); 
    		}
    	}    
    }
