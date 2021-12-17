/* -- JFLAP 4.0 --
 *
 * Copyright information:
 *
 * Susan H. Rodger, Thomas Finley
 * Computer Science Department
 * Duke University
 * April 24, 2003
 * Supported by National Science Foundation DUE-9752583.
 *
 * Copyright (c) 2003
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the author.  The name of the author may not be used to
 * endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
 
import file.Codec;
import file.ParseException;
import gui.action.NewAction;
import gui.action.OpenAction;
import gui.environment.Universe;
import java.io.File;
import java.security.*;

/**
 * This is the class that starts JFLAP.
 * 
 * @author Thomas Finley
 */

public class JFLAP {
    /**
     * Starts JFLAP.  This sets various system properties.  If there
     * are command line arguments, this will attempt to open them as
     * JFLAP files.  If there are no arguments, this will call on
     * {@link gui.action.NewAction#showNew} to display a choice for a
     * new structure.
     * @param args the command line arguments, which may hold files to open
     */
    public static void main(String[] args) {
	// Make sure we're not some old version.
	try {
	    String v = System.getProperty("java.specification.version");
	    double version = Double.parseDouble(v)+0.00001;
	    if (version < 1.4) {
		javax.swing.JOptionPane.showMessageDialog
		    (null, "Java 1.4 or higher required to run JFLAP!\n"+
		     "You appear to be running Java "+v+".\n"+
		     "This program will now exit.");
		System.exit(0);
	    }
	} catch (SecurityException e) {
	    // Eh, that shouldn't happen.
	}

	// Set the AWT exception handler.  This may not work in future
	// Java versions.
	try {
	    // This is a useless statement that forces the catcher to 
	    // compile.
	    if (gui.ThrowableCatcher.class == null);
	    System.setProperty("sun.awt.exception.handler",
			       "gui.ThrowableCatcher");
	} catch (SecurityException e) {
	    System.err.println("Warning: could not set the "+
			       "AWT exception handler.");
	}

	// Apple is stupid.
	try {
	    // Well, Apple WAS stupid...
	    if (System.getProperty("os.name").startsWith("Mac OS") &&
		System.getProperty("java.specification.version").equals("1.3"))
	        System.setProperty("com.apple.hwaccel", "false");
	} catch (SecurityException e) {
	    // Bleh.
	}
	// Sun is stupider.
	try {
	    System.setProperty("java.util.prefs.syncInterval","2000000");
	} catch (SecurityException e) {
	    // Well, not key.
	}
	// Prompt the user for newness.
	NewAction.showNew();
	if (args.length > 0) {
	    for (int i=0; i<args.length; i++) {
		Codec[] codecs = (Codec[]) Universe.CODEC_REGISTRY
		    .getDecoders().toArray(new Codec[0]);
		try {
		    OpenAction.openFile(new File(args[i]), codecs);
		} catch (ParseException e) {
		    System.err.println("Could not open "+args[i]+": "+
				       e.getMessage());
		}
	    }
	}
    }
}
