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
 
import javax.swing.*;
import java.awt.*;

/**
 * This allows one to run JFLAP as an applet.  All it actually does is
 * display a small message, and runs the application normally.
 * 
 * @author Thomas Finley
 */

public class JFLAPplet extends JApplet {
    /**
     * This instantiates a new JFLAPplet.
     */
    public JFLAPplet() {
        getRootPane().putClientProperty("defeatSystemEventQueueCheck",
                                        Boolean.TRUE);
    }
    
    /**
     * This will modify the applet display to show a short message,
     * and then call the <CODE>JFLAP</CODE> class's main method so the
     * program can run as normal.
     */
    public void init() {
	// Show the message.
	JTextArea text = new JTextArea
	    ("Welcome to JFLAP "+gui.AboutBox.VERSION+"!\n"+
	     "Report bugs to rodger@cs.duke.edu!");
	text.setEditable(false);
        text.setWrapStyleWord(true);
	JScrollPane scroller = new JScrollPane
	    (text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(text, BorderLayout.CENTER);
	// Start the application.
	JFLAP.main(new String[0]);

    }
}
