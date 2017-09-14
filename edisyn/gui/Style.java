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
    public static final Font LARGE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 17);
    
    /////// SYNTH PANEL CONSTANTS
    
    /** The synth panel's insets */
    public static final Insets SYNTH_PANEL_INSETS = new Insets(4, 4, 8, 4);

    /////// CATEGORY CONSTANTS
    
    /** Font for category borders. */
    public static final Font CATEGORY_FONT = LARGE_FONT;
    /** Stroke width for category borders. */
    public static final int CATEGORY_STROKE_WIDTH = 3;
    /** The category border. */
    public static final Border CATEGORY_BORDER = BorderFactory.createEmptyBorder(0, -2, 4, 4);
    /** Color of the first category on a page. */
    public static final Color COLOR_A = new Color(0, 210, 0); 
    /** Color of the second category on a page. */
    public static final Color COLOR_B = new Color(150, 150, 255); 
    /** Color of the third category on a page. */
    public static final Color COLOR_C = new Color(200, 200, 0); 
    /** Color for the category holding critical global stuff like patch name, patch number, etc. */
    public static final Color COLOR_GLOBAL = Color.white;
    /** Actual inset distance in case a JLabel is the first item.  Used to make a strut in Microwave. */
    public static final int CATEGORY_INSET_DISTANCE = 8;
    public static final int CATEGORY_INSETS_BOTTOM_OFFSET = -2;
    
    
    /////// CHOOSER CONSTANTS
    
    public static Insets CHOOSER_INSETS = new Insets(-1, 0, -2, 0);  // no insets
    

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
    public static final Insets LABELLED_DIAL_INSETS = new Insets(1, 3, 1, 3);


    /////// ENVELOPE DISPLAY CONSTANTS

    /** Degree of Transparency of the fill region with respect to the stroked lines. */
    public static final double ENVELOPE_DISPLAY_FILL_TRANSPARENCY = 0.5;
    /** Thickness of effective border around the Envelope Display, except the top. */
    public static final int ENVELOPE_DISPLAY_BORDER_THICKNESS = 10;
    /** Thickness of effective top border above the Envelope Display. */
    public static final int ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS = 2;  // enough space for the dots
    /** Thickness of the marker circles. */
    public static final float ENVELOPE_DISPLAY_MARKER_WIDTH = 4;
    /** Stroke for the X axis. */
    public static final BasicStroke ENVELOPE_AXIS_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 4.0f }, 0.0f);
            
    /////// CHECKBOX CONSTANTS
    /** Border around arpeggiator checkboxes */
    public static final Border CHECKBOX_HIGHLIGHTED_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(2, 2, 2, 2),
        BorderFactory.createLineBorder(Color.red, 1, true));
    public static final Border CHECKBOX_NON_HIGHLIGHTED_BORDER = BorderFactory.createEmptyBorder(3, 3, 3, 3);
    

    /////// PATCH CONSTANTS
    public static final Color PATCH_UNSYNCED_TEXT_COLOR = Color.RED;
    public static final Border PATCH_BORDER = BorderFactory.createEmptyBorder(4, 4, 6, 4);
    
    
    /////// KEYBOARD CONSTANTS
    public static final Color KEYBOARD_WHITE_COLOR = Color.WHITE;
    public static final Color KEYBOARD_BLACK_COLOR = Color.BLACK;
    public static final Color KEYBOARD_DYNAMIC_COLOR = DIAL_DYNAMIC_COLOR;
    public static final int KEYBOARD_DEFAULT_WHITE_KEY_WIDTH = 16;
    public static final int KEYBOARD_DEFAULT_WHITE_KEY_HEIGHT = 80;
    

    /////// HTML DISPLAY CONSTANTS
    
    /** Base Font */
    public static final Font HTML_DISPLAY_BASE_FONT = MEDIUM_FONT;
    public static final Insets HTML_DISPLAY_INSETS = new Insets(20, 20, 20, 20);  // bottom compensates for SYNTH_PANEL_INSETS


	/////// BATCH DOWNLOAD WINDOW
	public static final int BATCH_WINDOW_BORDER = 16;



    /////// GRAPHICS PREPARATION
        
    /** Updates the graphics rendering hints before drawing.  Called by a few widgets.  */
    public static void prepareGraphics(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
        

    /////// OS DISTINGUISHING PROCEDURES

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() 
        {
        return (OS.indexOf("win") >= 0);
        }

    public static boolean isMac() 
        {
        return (OS.indexOf("mac") >= 0 || System.getProperty("mrj.version") != null);
        }

    public static boolean isUnix() 
        {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
        }
        
    static
        {
        if (isUnix())
            {
            SMALL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
            CHOOSER_INSETS = new Insets(0, 0, 2, 4); 
            }
        }    
    }
