/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.threemedia;

import javax.swing.*;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.*;

import org.webtop.component.*;

import org.webtop.wsl.client.*;
import org.webtop.wsl.script.*;
import org.webtop.util.script.*;


public class Controls extends JPanel implements ItemListener, ActionListener {
	private static final long serialVersionUID = 0;//to get eclipse to shut up
	
	
	private ThreeMedia parent;
	private Engine engine;
	private WSLPlayer wslPlayer;
	
	private JComboBox sourceType;
	private JButton addButton;
	private StateButton playButton;
	private JButton prevButton;
	private JButton nextButton;

	private JComboBox quality;
	private int customResolution;
	private boolean customGouraud;
	private int linearCount = 0;
	private int mode = 0;
	private WaveSource selectedSource;
	private boolean arrowsVisible;

	public static final int WIDGET_HIDE = 0;
	public static final int WIDGET_ICON = 1;
	public static final int WIDGET_FULL = 2;

	private int widgetVisible = WIDGET_ICON;
	private boolean autoSelect = false;
	private boolean gridVisible = true;

	private ResolutionDialog resDialog;
	
	//Scritping stuffs
	private ButtonScripter prevButtonScripter, nextButtonScripter;
	private StateButtonScripter playButtonScripter; 
	private ChoiceScripter qualityScripter; 
	
	
	//Constructor ----- Double check to make sure all objects are swing objects [JD]
	public Controls(ThreeMedia wave) {
		parent = wave;
		setLayout(new FlowLayout());

		prevButton = new JButton("<");
		prevButton.addActionListener(this);
		add(prevButton);
		
		playButton = new StateButton(" Play ", " Stop ", false);
		playButton.addListener( new StateButton.Listener(){
			public void stateChanged(StateButton sb, int state){
				state = sb.getState();
				if(state == 0){
					engine.play();
					prevButton.setEnabled(false);
					nextButton.setEnabled(false);
				}
				else if(state == 1){
					engine.pause();
					prevButton.setEnabled(true);
					nextButton.setEnabled(true);
				}
			}
		});
		add(playButton);

		nextButton = new JButton(">");
		nextButton.addActionListener(this);
		add(nextButton);

		add(new JLabel("  Resolution:"));

		quality = new JComboBox();
		quality.addItem("Fastest");
		quality.addItem("Fast");
		quality.addItem("Medium");
		quality.addItem("Smooth");
		quality.addItem("Very Smooth");
		quality.addItem("Custom...");
		quality.setForeground(Color.black);
		quality.setBackground(Color.white);
		quality.setSelectedIndex(4);
		quality.addItemListener(this);
		add(quality);

		arrowsVisible = true;

		resDialog = new ResolutionDialog(parent);

		playButtonScripter = new StateButtonScripter(playButton, wave.getWSLPlayer(), null, 
				"play", new String[]{"Play", "Stop"}, 0);
		prevButtonScripter = new ButtonScripter(prevButton, wave.getWSLPlayer(),null, "prevFrame");
		nextButtonScripter = new ButtonScripter(nextButton, wave.getWSLPlayer(), null, "nextFrame");
		qualityScripter = new ChoiceScripter(quality, wave.getWSLPlayer(), null, "waveQuality", 
				new String[]{"Fastest", "Fast", "Medium", "Smooth", "Very Smooth", "Custom"}, 
				4, this);
		
	}
	
	public void reset() {
		quality.setSelectedIndex(4);
		prevButton.setEnabled(true);
		nextButton.setEnabled(true);
	}

	public void setEngine(Engine e) {
		engine = e;
		resDialog.setEngine(e);
	}
	
	public void setPlaying(boolean playing) {
		if(playing)
			playButton.setText(" Stop ");
		else 
			playButton.setText(" Play ");
	}

	public void setResolution(int resolution, boolean normalPerVertex) {
		if(resolution==50 && !normalPerVertex) 
			quality.setSelectedIndex(0);
		else if(resolution==50 && normalPerVertex) 
			quality.setSelectedIndex(1);
		else if(resolution==100 && normalPerVertex) 
			quality.setSelectedIndex(2);
		else if(resolution==200 && !normalPerVertex) 
			quality.setSelectedIndex(3);
		else if(resolution==200 && normalPerVertex) 
			quality.setSelectedIndex(4);
		else {
			customResolution = resolution;
			customGouraud = normalPerVertex;
			quality.setSelectedIndex(5);
		}
	}
	
	public boolean vectorsVisible() {
		return arrowsVisible;
		}
	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if(source==quality) {
			int selected = quality.getSelectedIndex();

			switch (selected) {
			case 0:
				engine.setPoolOptions(50, true);
				break;
			case 1:
				engine.setPoolOptions(50, true);
				break;
			case 2:
				engine.setPoolOptions(100, true);
				break;
			case 3:
				engine.setPoolOptions(200, true);
				break;
			case 4:
				engine.setPoolOptions(200, true);
				break;
			case 5:
				resDialog.setVisible(true);
			}
		}
	}

	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source==playButton) {
			if(engine.isPlaying()) {
				engine.pause();
				prevButton.setEnabled(true);
				nextButton.setEnabled(true);
			} else {
				engine.play();
				prevButton.setEnabled(false);
				nextButton.setEnabled(false);
			}
		} else if(source==prevButton) {
			engine.prevFrame();
		} else if(source==nextButton) {
			engine.nextFrame();
		}
		
		
	}
	
	/*
	 * Get the playButton and the state of the playButton so that when 
	 * the reset button is pressed the playButton's text will reset 
	 * properly
	 */
	public int getPlayButtonState(){
		return playButton.getState();
	}
	
	public StateButton getPlayButton(){
		return playButton;
	}
	
	//WSL Method used in ThreeMedia.java [JD]
	protected void toWSLNode(WSLNode node){
		playButtonScripter.addTo(node);
		qualityScripter.addTo(node);
	}

}
