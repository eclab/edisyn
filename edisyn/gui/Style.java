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
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;
    static Color BACKGROUND_COLOR = DEFAULT_BACKGROUND_COLOR;
    public static Color BACKGROUND_COLOR() { return BACKGROUND_COLOR; }
    /** Text color */
    public final static Color DEFAULT_TEXT_COLOR = Color.WHITE;
    static Color TEXT_COLOR = DEFAULT_TEXT_COLOR;
    public static Color TEXT_COLOR() { return TEXT_COLOR; }
    /** Small font, primarily for labels, button and combo box text. */
    public static Font SMALL_FONT() { return new Font(Font.SANS_SERIF, Font.PLAIN, isUnix() ? 9 : 10); }
    /** Medium-sized font, used primarily in the center of a Dial. */
    public static Font SEMI_MEDIUM_FONT() { return new Font(Font.SANS_SERIF, Font.PLAIN, 12); }
    /** Medium-sized font, used primarily in the center of a Dial. */
    public static Font MEDIUM_FONT() { return new Font(Font.SANS_SERIF, Font.PLAIN, 14); }
    /** Large Font, primarily for category headers. */
    public static Font LARGE_FONT() { return new Font(Font.SANS_SERIF, Font.PLAIN, 17); }
    public static Color DEFAULT_DYNAMIC_COLOR = Color.RED;
    static Color DYNAMIC_COLOR = DEFAULT_DYNAMIC_COLOR;
    public static Color DYNAMIC_COLOR() { return DYNAMIC_COLOR; }
    
    /////// SYNTH PANEL CONSTANTS
    
    /** The synth panel's insets */
    public static Insets SYNTH_PANEL_INSETS() { return new Insets(4, 4, 8, 4); }

    /////// CATEGORY CONSTANTS
    
    /** Font for category borders. */
    public static Font CATEGORY_FONT() { return LARGE_FONT(); }
    /** Stroke width for category borders. */
    public static int CATEGORY_STROKE_WIDTH() { return 3; }
    /** The category border. */
    public static Border CATEGORY_BORDER() { return BorderFactory.createEmptyBorder(0, -2, 4, 4); }
    /** Color of the first category on a page. */
    public final static Color DEFAULT_COLOR_A = new Color(0, 210, 0);
    static Color COLOR_A = DEFAULT_COLOR_A;
    public static Color COLOR_A() { return COLOR_A; } 
    /** Color of the second category on a page. */
    public final static Color DEFAULT_COLOR_B = new Color(150, 150, 255);
    static Color COLOR_B = DEFAULT_COLOR_B;
    public static Color COLOR_B() { return COLOR_B; } 
    /** Color of the third category on a page. */
    public final static Color DEFAULT_COLOR_C = new Color(200, 200, 0);
    static Color COLOR_C = DEFAULT_COLOR_C;
    public static Color COLOR_C() { return COLOR_C; } 
    /** Color for the category holding critical global stuff like patch name, patch number, etc. */
    public static Color COLOR_GLOBAL() { return TEXT_COLOR(); }
    /** Actual inset distance in case a JLabel is the first item.  Used to make a strut in Microwave. */
    public static int CATEGORY_INSET_DISTANCE() { return 8; }
    public static int CATEGORY_INSETS_BOTTOM_OFFSET() { return -2; }
    
    
    /////// CHOOSER CONSTANTS
    
    public static Insets CHOOSER_INSETS() 
        { 
        if (isUnix())
            return new Insets(0, 0, 2, 4); 
        else
            return new Insets(-1, 0, -2, 0);  // no insets
        }
    public static Insets CHOOSER_WINDOWS_INSETS() { return new Insets(-1, 6, -2, 0); }  // no insets
    

    /////// VBOX AND HBOX CONSTANTS

    /** Insets for VBoxes, by default zero. */
    public static Insets VBOX_INSETS() { return new Insets(0, 0, 0, 0); }
    /** Insets for HBoxes, by default zero. */
    public static Insets HBOX_INSETS() { return new Insets(0, 0, 0, 0); }


    /////// DIAL CONSTANTS

    /** Color of the unset region in Dials etc. */ 
    public final static Color DEFAULT_UNSET_COLOR = Color.GRAY; 
    static Color UNSET_COLOR = DEFAULT_UNSET_COLOR; 
    public static Color DIAL_UNSET_COLOR() { return UNSET_COLOR; }
    /** Color of the set region in Dials etc. when being updated. */
    public static Color DIAL_DYNAMIC_COLOR() { return DYNAMIC_COLOR(); }
    /** Width of the set region in Dials etc.  Should be a multiple of 2, ideally 4*/
    public static float DIAL_STROKE_WIDTH() { return 4.0f; }
    /** The stroke for the set region in Dials etc. */
    public static BasicStroke DIAL_THIN_STROKE() { return new BasicStroke(DIAL_STROKE_WIDTH() / 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL); }
    /** The stroke for the unset region in Dials etc. */
    public static BasicStroke DIAL_THICK_STROKE() { return new BasicStroke(DIAL_STROKE_WIDTH(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL); }
    /** Font used in the center of a dial. */
    public static Font DIAL_FONT() { return MEDIUM_FONT(); }
    /** Insets for labelled dials to set them apart slightly from one another. */
    public static Insets LABELLED_DIAL_INSETS() { return new Insets(1, 3, 1, 3); }
    /** Width of the dial **/
    public static int LABELLED_DIAL_WIDTH() { return 55; }

    /////// ENVELOPE DISPLAY CONSTANTS

    /** Degree of Transparency of the fill region with respect to the stroked lines. */
    public static double ENVELOPE_DISPLAY_FILL_TRANSPARENCY() { return 0.5; }
    /** Thickness of effective border around the Envelope Display, except the top. */
    public static int ENVELOPE_DISPLAY_BORDER_THICKNESS() { return 10; }
    /** Thickness of effective top border above the Envelope Display. */
    public static int ENVELOPE_DISPLAY_TOP_BORDER_THICKNESS() { return 2; }  // enough space for the dots
    /** Thickness of the marker circles. */
    public static float ENVELOPE_DISPLAY_MARKER_WIDTH() { return 4; }
    /** Stroke for the X axis. */
    public static BasicStroke ENVELOPE_AXIS_STROKE() { return new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 4.0f }, 0.0f); }
    public static Color ENVELOPE_COLOR() { return DYNAMIC_COLOR(); }
    public static Color ENVELOPE_UNSET_COLOR() { return DIAL_UNSET_COLOR(); }
        
    /////// CHECKBOX CONSTANTS
    /** Border around arpeggiator checkboxes */
    public static Border CHECKBOX_HIGHLIGHTED_BORDER() { return BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 2, 2, 2),
            BorderFactory.createLineBorder(DYNAMIC_COLOR(), 1, true)); }
    public static Border CHECKBOX_NON_HIGHLIGHTED_BORDER() { return BorderFactory.createEmptyBorder(3, 3, 3, 3); }
    

    /////// PATCH CONSTANTS
    //public static Color PATCH_UNSYNCED_TEXT_COLOR() { return DYNAMIC_COLOR() ; }
    public static Border PATCH_BORDER() { return BorderFactory.createEmptyBorder(4, 4, 6, 4); }
    
    
    /////// KEYBOARD CONSTANTS
    public static Color KEYBOARD_WHITE_COLOR() { return Color.WHITE; }
    public static Color KEYBOARD_BLACK_COLOR() { return Color.BLACK; }
    public static Color KEYBOARD_DYNAMIC_COLOR() { return DYNAMIC_COLOR(); }
    public static int KEYBOARD_DEFAULT_WHITE_KEY_WIDTH() { return 16; }
    public static int KEYBOARD_DEFAULT_WHITE_KEY_HEIGHT() { return 80; }
    
    /////// JOYSTICK CONSTANTS
    public static int JOYSTICK_WIDTH() { return 20; }

    /////// HTML DISPLAY CONSTANTS
    
    /** Base Font */
    public static Font HTML_DISPLAY_BASE_FONT() { return MEDIUM_FONT(); }
    public static Insets HTML_DISPLAY_INSETS() { return new Insets(20, 20, 20, 20); }  // bottom compensates for SYNTH_PANEL_INSETS


    /////// BATCH DOWNLOAD WINDOW
    public static int BATCH_WINDOW_BORDER() { return 16; }



    /////// GRAPHICS PREPARATION
        
    /** Updates the graphics rendering hints before drawing.  Called by a few widgets.  */
    public static void prepareGraphics(Graphics g)
        {
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
        
    static
        {
        updateColors();
        }
         
    public static void updateColors()
        {
        BACKGROUND_COLOR = Synth.getLastColor("background-color", DEFAULT_BACKGROUND_COLOR);
        TEXT_COLOR = Synth.getLastColor("text-color", DEFAULT_TEXT_COLOR);
        COLOR_A = Synth.getLastColor("a-color", DEFAULT_COLOR_A);
        COLOR_B = Synth.getLastColor("b-color", DEFAULT_COLOR_B);
        COLOR_C = Synth.getLastColor("c-color", DEFAULT_COLOR_C);
        DYNAMIC_COLOR = Synth.getLastColor("dynamic-color", DEFAULT_DYNAMIC_COLOR);
        UNSET_COLOR = Synth.getLastColor("unset-color", DEFAULT_UNSET_COLOR);
        }

    /////// OS DISTINGUISHING PROCEDURES

    private static String OS() { return System.getProperty("os.name").toLowerCase(); }

    public static boolean isWindows() 
        {
        return (OS().indexOf("win") >= 0);
        }

    public static boolean isMac() 
        {
        return (OS().indexOf("mac") >= 0 || System.getProperty("mrj.version") != null);
        }

    public static boolean isUnix() 
        {
        return (OS().indexOf("nix") >= 0 || OS().indexOf("nux") >= 0 || OS().indexOf("aix") > 0 );
        }
    }
