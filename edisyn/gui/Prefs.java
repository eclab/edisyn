/***
    Copyright 2017 by Sean Luke
    Licensed under the Academic Free License version 3.0
    See the file "LICENSE" for more information
*/

package edisyn.gui;

import java.util.prefs.*;

/** 
    A simple cover class for Java's preferences system.
*/

public class Prefs
    {
    public static final String GLOBAL_PREFERENCES = "edisyn/global";
    public static final String EDITOR_PREFERENCES = "edisyn/editor";

    public static Preferences getGlobalPreferences(String namespace)
        {
        return Preferences.userRoot().node(GLOBAL_PREFERENCES + "/" + namespace.replace('.','/'));
        }

    public static Preferences getAppPreferences(String editor, String namespace)
        {
        return Preferences.userRoot().node(EDITOR_PREFERENCES + "/" + editor.replace('.','/') + "/" + namespace.replace('.','/')); 
        }
        
    public static boolean removeGlobalPreferences(String namespace)
        {
        try
            {
            getGlobalPreferences(namespace).removeNode();
            return true;
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            return false;
            }
        }

    public static boolean removeAppPreferences(String editor, String namespace)
        {
        try
            {
            getAppPreferences(editor, namespace).removeNode();
            return true;
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            return false;
            }
        }

    public static boolean save(Preferences prefs)
        {
        try 
            {
            prefs.sync();
            return true;
            }
        catch (Exception ex)
            {
            ex.printStackTrace();
            return false;
            }
        }
    }
