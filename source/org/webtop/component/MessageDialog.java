/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Todo: Clean up code
//Version 1.2.1
//Updated July 23 2002 [Davis]

package org.webtop.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class MessageDialog extends JDialog implements ActionListener {
	private String message;

	private static final String OK_LABEL = "    OK    ";
	private static final String CANCEL_LABEL = "  Cancel  ";
	private static final String YES_LABEL = "    Yes    ";
	private static final String NO_LABEL = "    No    ";

	public static final int OK_BUTTON = 1,CANCEL_BUTTON = 2,
													YES_BUTTON = 4,NO_BUTTON = 8,
													KILLED_WINDOW = -1;
	private int buttons;

	private int response;

	//Omitting buttons argument assumes just OK and autosize/center
	public MessageDialog(JFrame owner, String title, String mess) {
		this(owner,title,mess,OK_BUTTON);
		autoSize();
		center();
	}

	public MessageDialog(JFrame owner, String title, String mess, int b) {
		super(owner, title, true);
		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {response=KILLED_WINDOW; setVisible(false);}
			});
		setLayout(new VerticalLayout(0));
		setTitle(title);
		message = mess;
		setButtons(b);		//Also sets up GUI
	}

	public int getResponse() {
		return response;
	}

	public void setMessage(String mess) {
		message = mess;
		setupGUI();
	}

	public void setButtons(int b) {
		//Ensure that we at least have a button:
		buttons = ((b&15)==0)?OK_BUTTON:b;
		setupGUI();
	}

	private void setupGUI() {
		removeAll();
		try {	//Read string, make labels
			BufferedReader br=new BufferedReader(new StringReader(message));
			String line;
			while((line=br.readLine())!=null)
				add(new Label(line,Label.CENTER));
		}
		catch(IOException e) {
			System.err.print("MessageDialog (while setting up message labels) encountered ");
			e.printStackTrace();
		}
		//add(new Label(message, Label.CENTER), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton button;

		if((buttons & OK_BUTTON) == OK_BUTTON) {
			buttonPanel.add(button = new JButton(OK_LABEL));
			button.addActionListener(this);
		}

		if((buttons & YES_BUTTON) == YES_BUTTON) {
			buttonPanel.add(button = new JButton(YES_LABEL));
			button.addActionListener(this);
		}

		if((buttons & NO_BUTTON) == NO_BUTTON) {
			buttonPanel.add(button = new JButton(NO_LABEL));
			button.addActionListener(this);
		}

		if((buttons & CANCEL_BUTTON) == CANCEL_BUTTON) {
			buttonPanel.add(button = new JButton(CANCEL_LABEL));
			button.addActionListener(this);
		}

		add(buttonPanel/*, BorderLayout.SOUTH*/);
	}

	//(Calling autoSize() before center() is probably the most useful tack.)
	public void autoSize() {
		pack();		//Force creation of actual components for the window (so size is meaningful)

		setSize(getLayout().minimumLayoutSize(this));
	}

	public void center() {
		pack();

		Dimension mySize=getSize(),
							screenSize=Toolkit.getDefaultToolkit().getScreenSize();

		setLocation((screenSize.width-mySize.width)/2,(screenSize.height-mySize.height)/2);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command==null) return;

		if(command.equals(OK_LABEL)) {
			response = OK_BUTTON;
		} else if(command.equals(CANCEL_LABEL)) {
			response = CANCEL_BUTTON;
		} else if(command.equals(YES_LABEL)) {
			response = YES_BUTTON;
		} else if(command.equals(NO_LABEL)) {
			response = NO_BUTTON;
		}

		setVisible(false);
	}

	public String toString() {
		return getClass().getName()+"[title=\""+getTitle()+
				"\",message=\""+message+"\",buttons: { "+
				(((buttons&OK_BUTTON)!=0)?"OK ":"")+
				(((buttons&YES_BUTTON)!=0)?"YES ":"")+
				(((buttons&NO_BUTTON)!=0)?"NO ":"")+
				(((buttons&CANCEL_BUTTON)!=0)?"CANCEL ":"")+
				"},response: "+
				((response==KILLED_WINDOW)?"<killed window>":
				 ((response==OK_BUTTON)?"OK ":
					((response==YES_BUTTON)?"YES ":
					 ((response==NO_BUTTON)?"NO ":
						((response==CANCEL_BUTTON)?"CANCEL ":"nil")))))
				+']';
	}
}
