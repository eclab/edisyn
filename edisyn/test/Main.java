/***
    Copyright 2017 by Sean Luke
    Licensed under the Apache License version 2.0
*/

package edisyn.test;

import java.util.*;

/** 
    A simple parsing and error-handling class for command-line arguments.
    I know this has been done to death, but here's my shot at it.
        
    <p>This class assumes that your arguments are organized with named arguments
    (like "-c" or "-n blah" or "--foo") first, and arbitrary unnamed arguments
    without hyphens second.  So this would be a legal organization:
        
    <p><tt>
        
    java Foo -n -c -o foo -i 3.5 --yo 17 blah blah blah
        
    </tt>
        
    <p>... but this would NOT be:
        
    <p><tt>
        
    java Foo blah blah -n -c -o foo -i 3.5 --yo 17 blah
        
    </tt>

    <h3>Usage</h3>
        
    <ol>
    <li>Class constructors provide various options for defining arguments.  They also do all the parsing
    and will exit(1) the application if the parsing fails.
    <li>Various hasFoo... and getFoo... methods are used to query the parsed database for argument values.
    <li>printCommandFormat(...) can be used to issue a warning or an error along with command-line usage instructions.
    </ol>
*/

public class Main
    {
    /** Argument type FLAG: bare arguments such as -n or --foo */
    public static final String FLAG =       "[flag]  ";
    /** Argument type INT: integer arguments such as -n 4 or --foo 32 */
    public static final String INT =        "[int]   ";
    /** Argument type DOUBLE: floating-point arguments such as -n -7.2 or --foo 32e19 */
    public static final String DOUBLE = "[float] ";
    /** Argument type STRING: arbitrary string arguments such as -n "whatever you want" or --foo hey_ya */
    public static final String STRING = "[string]";

    String name;
    String restDescription;
    String[] commands;
    String[] types;
    String[] descriptions;
    String[] args;
    String postamble;
    boolean helpFlag;
        
    // named arguments as keys, hashed to their values (as Strings, Integers, Doubles, or (for flags) themselves)
    HashMap<String, Object> arguments;
    // remaining arguments after the named arguments
    ArrayList<String> rest;
        
    /** 
        Returns whether the given flag argument exists.
        The argument must have a type of FLAG, or else it
        will be incorrectly reported as not existing.
    */
    public boolean hasFlag(String arg)
        {
        String val = (String)(arguments.get(arg));
        return (val != null && val.equals(arg));
        }
                
    /** 
        Returns the value of the given argument as an int.
        The argument must have a type of INT.
    */
    public int getInt(String arg)
        {
        return (Integer)(arguments.get(arg));
        }

    /** 
        Returns the value of the given argument as a double.
        The argument must have a type of DOUBLE.
    */
    public double getDouble(String arg)
        {
        return (Double)(arguments.get(arg));
        }

    /** 
        Returns the value of the given argument as a String.
        The argument must have a type of STRING.
    */
    public String getString(String arg)
        {
        return (String)(arguments.get(arg));
        }
                
    /** 
        Returns all unnamed arguments in order.
    */
    public ArrayList<String> getRest() 
        { 
        return rest; 
        }
        
    /** 
        Prints command-line usage.  <b>preamble</b> the text printed at the beginning: typically it's an error message but can be null or empty.
        <b>quit</b> indicates whether we should System.exit(1) after printing out the usage. 
    */
    public void printCommandFormat(String preamble, boolean quit)
        {
        if (preamble != null) System.err.println(preamble);
        System.err.println("\nUsage:  " + 
            (name == null ? "" : name) + 
                (commands.length == 0 ? "" : " [ARGUMENTS] " + 
                (restDescription == null ? "" : restDescription)));
        if (commands.length != 0)
            {
            System.err.println("\nArguments:");
            for(int j = 0; j < commands.length; j++)
                System.err.println("\t" + commands[j] + 
                    (FLAG.equals(types[j]) ? "\t         \t" : "\t" + types[j] + " \t") +
                    descriptions[j]);
            }
        if (helpFlag)
            {
            System.err.println("\t-h\t         \tShows this usage text");
            }
        if (postamble != null) System.err.println("\n" + postamble + "\n");
        if (quit) System.exit(1);

        }

    /** 
        Parses a command line and builds the database with no named arguments or postamble.  "-h" is presumed to bring up the argument usage and quit. 
        You provide:
        <ul>
        <li><b>name</b> is the application name as issued on the command line, like "java Flow".  It can be null but oughtn't.
        <li><b>args</b> are the application arguments you got from main(...)
        <li><b>restDescription</b> is a description of arguments AFTER the named arguments, like "[INFILE] OUTFILE1 OUTFILE2".  It can be null.
        </ul>
    */              

    public Main(String name, String[] args, String restDescription)
        {
        this(name, args, restDescription, new String[0], new String[0], new String[0], null, true);
        }
                
    /** 
        Parses a command line and builds the database with no named arguments.  "-h" is presumed to bring up the argument usage and quit. 
        You provide:
        <ul>
        <li><b>name</b> is the application name as issued on the command line, like "java Flow".  It can be null but oughtn't.
        <li><b>args</b> are the application arguments you got from main(...)
        <li><b>restDescription</b> is a description of arguments AFTER the named arguments, like "[INFILE] OUTFILE1 OUTFILE2".  It can be null.
        <li><b>postamble</b> is any special text you want to appear at the end of the command-line usage text printed after a parsing error.
        </ul>
    */              

    public Main(String name, String[] args, String restDescription, String postamble)
        {
        this(name, args, restDescription, new String[0], new String[0], new String[0], postamble, true);
        }

    /** 
        Parses a command line and builds the database with no postamble.  "-h" is presumed to bring up the argument usage and quit. 
        You provide:
        <ul>
        <li><b>name</b> is the application name as issued on the command line, like "java Flow".  It can be null but oughtn't.
        <li><b>args</b> are the application arguments you got from main(...)
        <li><b>restDescription</b> is a description of arguments AFTER the named arguments, like "[INFILE] OUTFILE1 OUTFILE2".  It can be null.
        <li><b>commands</b> is a list of legal named arguments, like { "-n", "-a", "--foo" }
        <li><b>types</b> is a list of the types (int, string, etc.) of the legal named arguments, like { Main.STRING, Main.INT, Main.FLAG }.
        Note that Main.FLAG refers to a named argument with no associated value, and which simply exists to turn on a feature.
        <li><b>descriptions</b> is a list of the descriptions of the legal named arguments, like { "input file", "number of widgets", "sorted" }
        </ul>
    */              

    public Main(String name, String[] args, String restDescription, String[] commands, String[] types, String[] descriptions)
        {
        this(name, args, restDescription, commands, types, descriptions, null, true);
        }

    /** 
        Parses a command line and builds the database.  You provide:
        <ul>
        <li><b>name</b> is the application name as issued on the command line, like "java Flow".  It can be null but oughtn't.
        <li><b>args</b> are the application arguments you got from main(...)
        <li><b>restDescription</b> is a description of arguments AFTER the named arguments, like "[INFILE] OUTFILE1 OUTFILE2".  It can be null.
        <li><b>commands</b> is a list of legal named arguments, like { "-n", "-a", "--foo" }
        <li><b>types</b> is a list of the types (int, string, etc.) of the legal named arguments, like { Main.STRING, Main.INT, Main.FLAG }.
        Note that Main.FLAG refers to a named argument with no associated value, and which simply exists to turn on a feature.
        <li><b>descriptions</b> is a list of the descriptions of the legal named arguments, like { "input file", "number of widgets", "sorted" }
        <li><b>postamble</b> is any special text you want to appear at the end of the command-line usage text printed after a parsing error.
        <li><b>helpFlag</b> indicates whether the presence of "-h" brings up the argument usage and quits.
        </ul>
    */              
                
    public Main(String name, String[] args, String restDescription, String[] commands, String[] types, String[] descriptions, String postamble, boolean helpFlag)
        {
        this.name = name;
        this.args = args;
        this.restDescription = restDescription;
        this.commands = commands;
        this.types = types;
        this.descriptions = descriptions;
        this.postamble = postamble;
        this.helpFlag = helpFlag;
        this.arguments = new HashMap<String, Object>();
        rest = new ArrayList<String>();
                
        boolean inRest = false;
        // nearly my entire life I've not needed an outer jump. But here it's useful!
        argsfor: for(int i = 0; i < args.length; i++)
            {
            if (helpFlag && args[i].equals("-h"))
                {
                printCommandFormat(null, true);
                }
                        
            for(int j = 0; j < commands.length; j++)
                {
                if (args[i].equals(commands[j]))  // got it
                    {
                    if (this.arguments.containsKey(commands[j]))
                        printCommandFormat("Duplicate argument " + args[i], true);
                    else if (inRest)  // uh oh
                        printCommandFormat("Invalid location for argument " + args[i], true);
                    else
                        {
                        try
                            {
                            if (FLAG.equals(types[j]))
                                this.arguments.put(args[i], args[i]);
                            else if (i == args.length - 1)          // notice this is AFTER checking for FLAG
                                printCommandFormat("Missing value for argument " + args[i], true);
                            else if (INT.equals(types[j]))
                                this.arguments.put(args[i], Integer.valueOf(args[++i]));
                            else if (DOUBLE.equals(types[j]))
                                this.arguments.put(args[i], Double.valueOf(args[++i]));
                            else if (STRING.equals(types[j]))
                                this.arguments.put(args[i], args[++i]);
                            else 
                                throw new RuntimeException("Internal error, unknown type " + types[i]);
                            }
                        catch (NumberFormatException e)
                            {
                            printCommandFormat("Improper format for value of argument " + args[i - 1] + " (" + args[i] + ")", true);
                            }
                        }
                    continue argsfor;
                    }
                }
            if (i < args.length)    // this could happen if the above parsing incremented i
                {
                if (args[i].startsWith("-")) // uh oh
                    {
                    System.err.println("Warning, possible misplaced or invalid argument " + args[i]);
                    }
                rest.add(args[i]);
                inRest = true;
                }
            }
        }
    }
        
