/*
 *    Debrief - the Open Source Maritime Analysis Application
 *    http://debrief.info
 *
 *    (C) 2000-2014, PlanetMayo Ltd
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the Eclipse Public License v1.0
 *    (http://www.eclipse.org/legal/epl-v10.html)
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 */
package MWC.GUI.Swing.Spinner;

/* All import statements... */
import java.util.EventObject;

import javax.swing.JComponent;



/**
 * An event which is used to dispatch information regarding a Spinner
 * being 'spun'.
 *
 * This code is copyright Kevin Mayer (kmayer@layer9.com) 2001 and can
 * be copied and reproduced freely so long as all original comments in the
 * source code remain in tact. Comments may be added but not removed. If
 * you do modify the spinner classes, I would appreciate seeing your
 * modifications (though this is not compulsory).
 * Basically - here's something for all to use!
 *
 * @author Kevin Mayer
 *         (<A HREF="mailto:kmayer@layer9.com">kmayer@layer9.com</A>)
 * @version (RCS: $Revision: 1.1.1.1 $)
 */
public class SpinEvent extends EventObject {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		private final String command;
    private final JComponent component;


    /**
     * Create a new SpinEvent.
     *
     * @param src The source of the event.
     * @param cmd The action command of the Spinner causing the event.
     * @param comp The component that the Spinner is 'spinning'.
     */
    public SpinEvent(final Object src, final String cmd, final JComponent comp) {
	super(src);
	this.command = cmd;
	this.component = comp;
    }


    /**
     * Get the action command of this Spinner that caused this SpinEvent.
     *
     * @return The action command of this SpinEvent.
     */
    public String getActionCommand() {
	return this.command;
    }


    /**
     * Get the component that was 'spun' by the Spinner that
     * caused this event.
     *
     * @return The component that was 'spun'.
     */
    public JComponent getComponent() {
	return this.component;
    }
}
