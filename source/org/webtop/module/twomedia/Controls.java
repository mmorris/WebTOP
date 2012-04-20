/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.twomedia;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import org.webtop.component.*;
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;
import org.webtop.component.*;
import org.webtop.component.StateButton.Listener;


import org.webtop.util.*;
import org.webtop.util.script.*;

import org.webtop.module.twomedia.ResolutionDialog;
import javax.swing.*;

public class Controls extends JPanel implements ActionListener,ItemListener{
	private TwoMedia parent;
	private Engine engine;
	private StateButton playButton;
	private JButton prevButton;
	private JButton nextButton;
	
	private JComboBox quality;
	private int customResolution;
	private boolean customGouraud;
	
	private ResolutionDialog preference;
	
	//WSL SCRIPTING STUFFS
	private ButtonScripter prevButtonScripter, nextButtonScripter; 
	private StateButtonScripter playButtonScripter;
	private ChoiceScripter sourceTypeScripter; 
	

	public Controls(TwoMedia wave) {
		parent = wave;
		setLayout(new FlowLayout());
		
		
		JLabel empty1 = new JLabel("                                                                          ");
		add(empty1);  
		prevButton = new JButton("<");
		prevButton.addActionListener(this);
		add(prevButton);

		playButton = new StateButton(" Play ", " Stop ", false);
		playButton.addListener(new StateButton.Listener()
		  {
			public void stateChanged(StateButton sb, int state){
				state = sb.getState();
				if(state==0){
					engine.play();
					prevButton.setEnabled(false);
					nextButton.setEnabled(false);
				}
				else if(state ==1){
					engine.pause();
					prevButton.setEnabled(true);
					nextButton.setEnabled(true);
				}
			}
		  }
		);
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
		quality.setSelectedIndex(4);
		quality.setForeground(Color.black);
		quality.setBackground(Color.white);
		quality.addItemListener(this);
		add(quality);
		
		
		
		JLabel empty2 = new JLabel("                                                                        ");
		add(empty2);

		preference = new ResolutionDialog(wave);
		
		//Create the Play/Stop, previous, and next button scripters
		playButtonScripter = new StateButtonScripter(playButton, 
				wave.getWSLPlayer(), null, "play", new String[]{"Play", "Stop"},0);
		prevButtonScripter = new ButtonScripter(prevButton, wave.getWSLPlayer(), null, 
				"prevFrame");
		nextButtonScripter = new ButtonScripter(nextButton, wave.getWSLPlayer(), null, 
				"nextFrame");
		sourceTypeScripter = new ChoiceScripter(quality, wave.getWSLPlayer(), 
				null, "waveQuality", 
				new String[]{"Fastest", "Fast", "Medium", "Smooth", "Very Smooth", 
				"Custom"},4, this);
	}

	public void reset() {
		quality.setSelectedIndex(4);
		prevButton.setEnabled(true);
		nextButton.setEnabled(true);
	}

	public void setEngine(Engine e) {
		engine = e;
		preference.setEngine(e);
		if(engine.getLinearCount()>0) {
			setLayoutButtonsEnabled(true);
		} else {
			setLayoutButtonsEnabled(false);
		}
	}

	public void setLayoutButtonsEnabled(boolean enabled) {
		/*linearLayout1.setEnabled(enabled);
		linearLayout2.setEnabled(enabled);
		linearLayout3.setEnabled(enabled);
		linearLayout4.setEnabled(enabled);*/
	}

	public void setResolution(int resolution, boolean normalPerVertex) {
		//System.out.println(resolution + " " + normalPerVertex);
		if(resolution==50 && !normalPerVertex) //quality.select(0);
			quality.setSelectedIndex(0);
		else if(resolution==50 && normalPerVertex) //quality.select(1);
			quality.setSelectedIndex(1);
		else if(resolution==100 && normalPerVertex) //quality.select(2);
			quality.setSelectedIndex(2);
		else if(resolution==200 && !normalPerVertex) //quality.select(3);
			quality.setSelectedIndex(3);
		else if(resolution==200 && normalPerVertex) //quality.select(4);
			quality.setSelectedIndex(4);
		else {
			customResolution = resolution;
			customGouraud = normalPerVertex;
			quality.setSelectedIndex(5);
		}
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
				preference.setVisible(true);
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

	
	////////WSL ROUTINE USED IN TWOMEDIA.JAVA////////
	protected void toWSLNode(WSLNode node){
		playButtonScripter.addTo(node);
		sourceTypeScripter.addTo(node);
	}
	
	///////Get the state of the Play Button to use in SourcePanel/////////
	//////When the reset button is pressed, the Play Button will/////////
	/////now change text properly							[JD]////////
	public int getPlayButtonState(){
		return playButton.getState();
	}
	
	public StateButton getPlayButton(){
		return playButton;
	}
}
