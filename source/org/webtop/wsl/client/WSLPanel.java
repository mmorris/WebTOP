/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

//Updated March 12 2004

package org.webtop.wsl.client;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import org.webtop.component.*;
import org.webtop.wsl.script.*;
import org.webtop.wsl.event.*;
import javax.swing.*;

import org.webtop.util.WTString;


/**
 * This class is the user interface of WSLAPI.	It exposes various operations
 * of WSLAPI through its graphical interface.	 This class merely translates
 * user interactions into calls to <code>WSLPlayer</code> methods and provides
 * feedback on the results.	 The look of the user interface is shown in Figure
 * 1.
 *
 * <p align="center"><img src="doc-files/WSLPanel-1.jpg"><br>
 * <b>Figure 1</b>. Appearance of <code>WSLPanel</code>, the scripting user
 * interface.
 *
 * <p>An instance of <code>WSLPanel</code> is created by the WebTOP module when
 * it constructs its user interface. The <code>WSLPanel</code> has a preferred
 * height of 25 pixels. Its length in the X direction is flexible.
 *
 * @author Yong Tze Chi
 * @author Davis Herring
 */
public class WSLPanel extends Panel
	implements ActionListener,WSLProgressListener,WSLPlayerListener {
	private static final String MENU_VIEWPOINT="Allow viewpoint changes during playback",MENU_ABOUT="About...";

	private static final Dimension minimumSize = new Dimension(200, 25);
	private static final Dimension preferredSize = new Dimension(640, 25);

	private final WSLPlayer player;

	private final Image background;

	private final ImageButton resetButton,recordButton,playButton,pauseButton,
														stopButton,openButton,optionsButton;
														

	private final ProgressBar progressBar=new ProgressBar();

	private final PopupMenu menu=new PopupMenu();

	private final WSLIOManager ioManager;

	/**
	 * Constructs a <code>WSLPanel</code> for the given player.  It verifies
	 * that <code>WSLIOManager</code> is trusted; if not, opening and saving
	 * scripts is disabled.
	 *
	 * @param player <code>WSLPlayer</code> with which to interact
	 */
	public WSLPanel(WSLPlayer p) {
		if(p==null) throw new NullPointerException("null WSLPlayer reference");

		player=p;
		player.addListener(this);

		//We attempt to use the new method; fall back (on its definition, natch)
		//if it's not there.  Eventually WSLIOManager version 2 will be a
		//standard, and all the try/catch stuff can be removed -- or at least
		//replaced with a message telling the user to upgrade, then removed
		//sometime after that.
		boolean trusted;
		try {trusted=WSLIOManager.isTrusted();}
		catch(NoSuchMethodError e) {
			//Only use the IOManager if it was loaded by the bootstrap loader --
			//that implies that it has the permissions that make it worthwhile.
			trusted=WSLIOManager.class.getClassLoader()==
							Object.class.getClassLoader();
		}
		ioManager=trusted?new WSLIOManager():null;

		setLayout(null);

		progressBar.setBackground(Color.black);
		progressBar.setBounds(287, 6, 330, 16);
		add(progressBar);

		MediaTracker tracker = new MediaTracker(this);

		background = getImage("images/background.gif");
		tracker.addImage(background, 0);

		resetButton = createImageButton("reset", 7, 3, 33, 20, true, tracker);
		recordButton = createImageButton("record", 42, 3, 33, 20, false, tracker);
		playButton = createImageButton("play", 77, 3, 33, 20, false, tracker);
		pauseButton = createImageButton("pause", 112, 3, 33, 20, false, tracker);
		stopButton = createImageButton("stop", 147, 3, 33, 20, true, tracker);
		openButton = createImageButton("open", 192, 3, 33, 20, true, tracker);
		optionsButton = createImageButton("options", 237, 3, 46, 20, true, tracker);
		if(ioManager==null) openButton.setEnabled(false);

		while(true)
			try {tracker.waitForAll(); break;} catch(InterruptedException e) {}

		updateButtonStates();

		//A listener routine to adjust the size of the ProgressBar
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				progressBar.setBounds(287, 6, getSize().width-287-
															(int) (50*(getSize().width/600.0f)), 16);
			}});

		final OptionMenuListener menuListener = new OptionMenuListener();

		MenuItem item=new CheckboxMenuItem(MENU_VIEWPOINT,
																			 !player.isViewpointEventEnabled());
		((CheckboxMenuItem)item).addItemListener(menuListener);
		menu.add(item);

		//menu.addSeparator();

		/*item = new MenuItem(MENU_ABOUT);
		item.addActionListener(menuListener);
		menu.add(item);*/

		add(menu);
	}

//===================
// Image acquisition
//===================

	/*private Image getImage(URL url) {
		if(module!=null && module instanceof Applet) {
			try {
				return ((Applet) module).getImage(url);
			}
			catch(Exception e) {}	//Try other method if applet has problems
		}
		return Toolkit.getDefaultToolkit().getImage(url);
	}*/

	//This should be more universal than the above.	 (adapted from JavaWorld)
	private Image getImage(String name) {
		InputStream in = getClass().getResourceAsStream(name);

		try{
			//If this isn't reliable, use while and read(b[],i,i)
			byte[] buffer=new byte[in.available()];
			in.read(buffer);
			return Toolkit.getDefaultToolkit().createImage(buffer);
		}
		catch(IOException e){
			System.err.println(e+" while acquiring image "+name);
			return null;
		}
	}

	private ImageButton createImageButton(String name, int x, int y, int width,
																				int height, boolean normOff,
																				MediaTracker tracker) {
		Image normalImage, pressedImage, overImage, disabledImage;

		normalImage = getImage("images/" + name + "_normal.gif");
		tracker.addImage(normalImage, 1);
		overImage = getImage("images/" + name + "_highlighted.gif");
		tracker.addImage(overImage, 1);
		pressedImage = getImage("images/" + name + "_pressed.gif");
		tracker.addImage(pressedImage, 1);
		disabledImage = getImage("images/" + name + "_disabled.gif");
		tracker.addImage(disabledImage, 1);

		ImageButton button = new ImageButton(normalImage, pressedImage, overImage);
		button.setNormallyOff(normOff);
		button.setDisabledImage(disabledImage);
		button.setBounds(x, y, width, height);
		Dimension d = new Dimension(width, height);
		button.setMinimumSize(d);
		button.setPreferredSize(d);
		button.setMaximumSize(d);
		button.addActionListener(this);
		add(button);

		return button;
	}

	private Frame getFrame() {
		Container c=this;
		while(c!=null && !(c instanceof Frame)) c = c.getParent();
		return (Frame)c;
	}

	/**
	 * Paints the <code>WSLPanel</code>'s background.
	 *
	 * @param g the graphics context onto which to paint
	 */
	public void paint(Graphics g) {
		Dimension d = getSize();
		Image buffer = createImage(d.width, d.height);
		Graphics bg = buffer.getGraphics();
		if(background !=null)
			bg.drawImage(background, 0, 0, d.width, d.height, this);
		super.paint(bg);
		bg.dispose();
		g.drawImage(buffer, 0, 0, this);
	}

	//Sets the availability/pressed-ness of buttons according to the player's state.
	private void updateButtonStates() {
		//It is worth noting that buttons which are 'on' do not generate
		//ActionEvents when clicked; they are already 'as far down as they will
		//go'.
		if(!player.loaded()) {
			if(player.isRecording()) {
				resetButton.setEnabled(false);	recordButton.setEnabled(true);
				recordButton.setOn(true);				playButton.setEnabled(false);
				playButton.setOn(false);				pauseButton.setEnabled(false);
				pauseButton.setOn(false);				stopButton.setEnabled(true);
				if(ioManager!=null) openButton.setEnabled(false);
				progressBar.setVisible(false);
			} else {
				resetButton.setEnabled(false);	recordButton.setEnabled(true);
				recordButton.setOn(false);			playButton.setEnabled(false);
				playButton.setOn(false);				pauseButton.setEnabled(false);
				pauseButton.setOn(false);				stopButton.setEnabled(false);
				if(ioManager!=null) openButton.setEnabled(true);
				progressBar.setVisible(false);
			}
		} else if(player.isPlaying()) {
			if(player.isPaused()) {
				resetButton.setEnabled(false);	recordButton.setEnabled(false);
				recordButton.setOn(false);			playButton.setEnabled(true);
				playButton.setOn(false);				pauseButton.setEnabled(true);
				pauseButton.setOn(true);				stopButton.setEnabled(true);
				if(ioManager!=null) openButton.setEnabled(false);
				progressBar.setVisible(true);
			} else {
				resetButton.setEnabled(false);	recordButton.setEnabled(false);
				recordButton.setOn(false);			playButton.setEnabled(true);
				playButton.setOn(true);					pauseButton.setEnabled(true);
				pauseButton.setOn(false);				stopButton.setEnabled(true);
				if(ioManager!=null) openButton.setEnabled(false);
				progressBar.setVisible(true);
			}
		} else {
			//Should non-playable scripts even stay in LOADED?
			//They're meant to be ephemeral... [Davis]
			boolean playable = player.getScript().isPlayable();
			resetButton.setEnabled(playable);	recordButton.setEnabled(true);
			recordButton.setOn(false);				playButton.setEnabled(playable);
			playButton.setOn(false);					pauseButton.setEnabled(false);
			pauseButton.setOn(false);					stopButton.setEnabled(false);
			if(ioManager!=null) openButton.setEnabled(true);
			progressBar.setVisible(playable);
		}

		repaint();
		resetButton.repaint();	recordButton.repaint();		playButton.repaint();
		pauseButton.repaint();		stopButton.repaint();		progressBar.repaint();
		if(ioManager!=null) openButton.repaint();
	}

	public void progressChanged(WSLProgressEvent event) {
		progressBar.setProgress(event.getProgress());
	}

	/**
	 * Loads script files. This method is called by the actionPerformed
	 * method when a script is selected and also when an example is
	 * loaded from the secondary window.
	 *
	 * @param fin FileInputStream to the script to load
	 */
	public void loadScript(FileInputStream fin)
	{
		try
		{
			//Unload old script (if there is one)
			if(player.loaded()) player.unload();
			player.load(fin);
			WSLScript script = player.getScript();
			if(script != null) {
				script.setTitle(ioManager.getLastFilename());
				progressBar.setTitle(ioManager.getLastFilename());
			}
		}
		catch(WSLParser.InvalidScriptException e) {
			System.err.print("WSLPanel load routine encountered ");
			e.printStackTrace();
			//new MessageDialog(getFrame(),"Error",
			//									"Error loading script: "+e.getMessage()).show();
		}
		
	}
	
		/**
		 * Loads script files. This method is called by the actionPerformed
		 * method when a script is selected and also when an example is
		 * loaded from the secondary window.
		 *
		 * @param fin InputStream to the script to load
		 */
		public void loadScript(InputStream fin)
		{
			try
			{
				//Unload old script (if there is one)
				if(player.loaded()) player.unload();
				player.load(fin);
				WSLScript script = player.getScript();
				if(script != null) {
					script.setTitle(ioManager.getLastFilename());
					progressBar.setTitle(ioManager.getLastFilename());
				}
			}
			catch(WSLParser.InvalidScriptException e) {
				System.err.print("WSLPanel load routine encountered ");
				e.printStackTrace();
				//new MessageDialog(getFrame(),"Error",
				//									"Error loading script: "+e.getMessage()).show();
			}
			
		}

	/**
	 * Handles GUI events. This method is called by the Java AWT Framework
	 * whenever a button in <code>WSLPanel</code> is pressed.
	 *
	 * @param evt the event describing the interaction
	 */
	public void actionPerformed(ActionEvent evt) {
		Object c = evt.getSource();
		if(c==resetButton) player.reset();
		else if(c==recordButton) {
			//Unload any old script first
			if(player.loaded()) player.unload();
			player.record();
		} else if(c==playButton) player.play();
		else if(c==stopButton) player.stop();
		else if(c==pauseButton) player.pause();
		else if(c==openButton) {
			try {
				FileInputStream fin = ioManager.getWSLScriptInputStream(getFrame());
				if(fin==null) return;

				loadScript(fin);
			}
			catch(IOException e) {
				System.err.print("WSLPanel load routine encountered ");
				e.printStackTrace();
				//TODO: brian- make these messagedialog things work
				//new MessageDialog(getFrame(),"Error","Unable to open "+ioManager.getLastFilename()).show();
			}
		} else if(c==optionsButton) menu.show(optionsButton, 46, 0);
	}

	public void playerStateChanged(WSLPlayerEvent event) {
		switch(event.getID()) {
		case WSLPlayerEvent.SCRIPT_LOADED:
			progressBar.setTotal(player.getTotalScriptTime());
			progressBar.setProgress(0);
			progressBar.setTitle(player.getScript().getTitle());
			break;
		case WSLPlayerEvent.PLAYER_RESET:
			progressBar.setProgress(0);
			break;
		case WSLPlayerEvent.RECORDER_STOPPED:
			progressBar.setTotal(player.getTotalScriptTime());
			progressBar.setProgress(0);
			boolean tryAgain=true,success=false;
			if(ioManager!=null)
				while(true)
					try {
						FileOutputStream fout = ioManager.getWSLScriptOutputStream(getFrame());
						if(fout!=null) {
							fout.write(player.getScript().toXMLTag().getBytes());
							fout.close();
							progressBar.setTitle(ioManager.getLastFilename());
							player.getScript().setTitle(ioManager.getLastFilename());
							success=true;
						}	// otherwise, user canceled save
						break;
					} catch(IOException e) {
						System.err.print("Failed to save to file; encountered ");
						e.printStackTrace();
						//TODO: Brian- fix this
						//MessageDialog md=new MessageDialog(getFrame(),"Save Failed",
						//	"Unable to save script."+(WTString.isNull(e.getMessage())?"":
						//														"\n("+e.getMessage()+')')+
						//																	 "\nTry again?",
						//																	 MessageDialog.YES_BUTTON|
						//																	 MessageDialog.NO_BUTTON);
						//md.autoSize(); md.center(); md.show();
						//start over unless user gives up
						//if(md.getResponse()==MessageDialog.NO_BUTTON) break;
					}
			if(!success) { //No save occurred -- indicate this
				progressBar.setTitle("Untitled");
				player.getScript().setTitle("Untitled");
			}
			break;
		}
		updateButtonStates();
	}

	/**
	 * Returns the minimum size of this <code>WSLPanel</code> object. Used by
	 * Java AWT Framework when resizing GUI components.
	 *
	 * @return a <code>Dimension</code> object specifying the minimum dimension
	 *				 of <code>WSLPanel</code>.
	 */
	public Dimension getMinimumSize() {
		return minimumSize;} 
	/**
	 * Returns the preferred size of this <code>WSLPanel</code> object. Used by
	 * Java AWT Framework when resizing GUI components.
	 *
	 * @return a <code>Dimension</code> object specifying the preferred
	 *				 dimension of <code>WSLPanel</code>.
	 */
	public Dimension getPreferredSize() {
		final Dimension d = getParent().getSize();
		if(d.width>preferredSize.width) preferredSize.width = d.width;
		return preferredSize;
	}

	private static final String ABOUT_MSG;
	static {
		//TODO: Brian- fix this
		//final StringBuffer sb=new StringBuffer("WebTOP version "+WApplication.WEBTOP_VERSION+"\nWSL version "+WSLPlayer.VERSION+"\nWSLIO ");
		final StringBuffer sb = new StringBuffer("Temporary string");
		try {
			//The following is one append so that if there is a NoSuchMethodError
			//the "is version " is aborted.
			sb.append("is version "+WSLIOManager.getVersion());
			sb.append(" (");
			if(!WSLIOManager.isTrusted()) sb.append("not ");
			sb.append("installed)");
		} catch(NoSuchMethodError e) {
			sb.append("is an old installed version");
		}
		ABOUT_MSG=sb.toString();
	}
	private class OptionMenuListener implements ItemListener,ActionListener {
		public void itemStateChanged(ItemEvent e) {
			if(e.getItem().equals(MENU_VIEWPOINT)) {
				player.setViewpointEventEnabled(!player.isViewpointEventEnabled());
			}
		}

		public void actionPerformed(ActionEvent e) {
			//TODO: Fixme Brian
			//if(e.getActionCommand().equals(MENU_ABOUT))
				//new MessageDialog(getFrame(),"About WebTOP",ABOUT_MSG).show();
		}
	}
}
