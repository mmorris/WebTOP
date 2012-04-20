package org.webtop.module.waves;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import org.webtop.component.*;
import org.webtop.wsl.client.*;
import org.webtop.wsl.event.*;
import org.webtop.wsl.script.*;

public class Controls extends JPanel implements ActionListener, ItemListener{
	
	private static final int DEF_QUALITY = 4;	
	
	//Indices into the dropdown
	private static final int POINT_SOURCE=0,LINE_SOURCE=1,SAMPLING_STICK=2,PLUCKED=3,STRUCK=4;
	
	//Other class objects here
	private Engine engine; 
	private ResolutionDialog resolutionDialog; //Must implement later [JD]
	private Waves applet;
	
	private JComboBox sourceType; 
	private JButton addButton; 
	private JButton playButton; 
	private JButton prevButton; 
	private JButton nextButton; 
	
	private JButton linearLayout1; 
	private JButton linearLayout2; 
	private JButton linearLayout3; 
	private JButton linearLayout4; 
	
	private JComboBox quality; 
	
	//The old source passed in a WSLPlayer player object...don't think we need to do this now [JD]
	public Controls(Waves ap){
		applet = ap; 
		setLayout(new FlowLayout());
		
		add(new JLabel("Add Source: "));
		
		sourceType = new JComboBox();
		sourceType.addItem("Point");
		sourceType.addItem("Line");
		sourceType.addItem("Sampling Stick");
		sourceType.addItem("Plucked");
		sourceType.addItem("Struck");
		add(sourceType);
		
		
		addButton = new JButton(" Add ");
		addButton.addActionListener(this);
		add(addButton);
		
		prevButton = new JButton("<");
		prevButton.addActionListener(this);
		add(prevButton);
		
		playButton = new JButton(" Play ");
		playButton.addActionListener(this);
		add(playButton);
		
		nextButton = new JButton(">");
		nextButton.addActionListener(this);
		add(nextButton);
		
		add(new JLabel("Linear Layout:"));
		
		linearLayout1 = new JButton("1");
		linearLayout1.addActionListener(this);
		add(linearLayout1);
		
		linearLayout2 = new JButton("2");
		linearLayout2.addActionListener(this);
		add(linearLayout2);

		linearLayout3 = new JButton("3");
		linearLayout3.addActionListener(this);
		add(linearLayout3);
		
		linearLayout4 = new JButton("4");
		linearLayout4.addActionListener(this);
		add(linearLayout4);
		
		quality = new JComboBox();
		quality.addItem("Fastest");
		quality.addItem("Fast");
		quality.addItem("Medium");
		quality.addItem("Smooth");
		quality.addItem("Very Smooth");
		quality.addItem("Custom...");
		quality.setSelectedIndex(DEF_QUALITY);
		quality.addItemListener(this);
		add(quality);
		
		resolutionDialog = new ResolutionDialog();
	}
	
	public void reset(){
		quality.setSelectedIndex(DEF_QUALITY);
		setLayoutButtonsEnabled(false);
		playButton.setText(" Play ");
	}
	
	public void setPlaying(boolean playing){
		if(playing) 
			playButton.setText(" Stop ");
		else
			playButton.setText(" Play ");
	}
	
	public void setLayoutButtonsEnabled(boolean enabled){
		linearLayout1.setEnabled(enabled);
		linearLayout2.setEnabled(enabled);
		linearLayout3.setEnabled(enabled);
		linearLayout4.setEnabled(enabled);
	}
	
	//This method is called when a WSL Script sets the resolution.  Makes the list box and settings
	//agree.
	public void setResolution(int resolution, boolean normalPerVertex){
		if(resolution == 50 && !normalPerVertex)
			quality.setSelectedIndex(0);
		else if(resolution ==50 && normalPerVertex)
			quality.setSelectedIndex(1);
		else if(resolution == 100 && normalPerVertex)
			quality.setSelectedIndex(2);
		else if(resolution == 200 && !normalPerVertex)
			quality.setSelectedIndex(3);
		else if(resolution == 200 && normalPerVertex)
			quality.setSelectedIndex(4);
		else{
			quality.setSelectedIndex(5);
			resolutionDialog.setup(resolution, normalPerVertex);
		}
	}
	public void setEngine(Engine e){
		engine = e;
		resolutionDialog.setEngine(e);
		setLayoutButtonsEnabled(engine.getLinearCount()>0);
	}
	
	//For random placement of new objects
	public static float randomPosition()
	{return (float)(Math.random()-.5)*Engine.POOL_SIZE; }
	
	//Event Handling Methods 
	public void actionPerformed(ActionEvent e){
		if(applet.getWSLPlayer().isPlaying()) return; 
		
		Object source = e.getSource();
		
		if(source == playButton){
			if(engine.isPlaying()){
				engine.pause();
				playButton.setText(" Play "); 
				prevButton.setEnabled(true);
				nextButton.setEnabled(true);
			}
			else {
				engine.play();
				playButton.setText(" Stop "); 
				prevButton.setEnabled(false);
				nextButton.setEnabled(false);	
			}
		}
		else if(source == prevButton){
			engine.prevFrame();
		}
		else if(source == nextButton){
			engine.nextFrame();
		}
		else if(source == addButton){
			//PoolWidget pw; 
			switch(sourceType.getSelectedIndex()){
			//Uncomment these when working with other widgets
			case LINE_SOURCE: 
				System.out.println("Creating line source: "+ sourceType.getSelectedIndex());
				PoolWidget pw = engine.addSource(4, 8, 0, 0);
				engine.selectWidget(pw);
				break; 
			/*
			case POINT_SOURCE: 
				pw = engine.addSource(4, 8, 0, randomPosition(), randomPosition());
				engine.selectWidget(pw);
				break; 
			case SAMPLING_STICK:
				pw = engine.addSamplingStick(randomPosition(), randomPosition());
				engine.selectWidget(pw);
				break;
			case PLUCKED: 
				float fpos[] = {randomPosition(), randomPosition()};
				pw = engine.addSource(20, 8, fpos);
				engine.selectWidget(pw);
				break;
			case STRUCK: 
				float tpos[] = {randomPosition(), randomPosition() };
				pw = engine.addStruckSource(40, 8, tpos);
				engine.selectWidget(pw);
				break;
			*/
			default:
				System.out.println("ControlPanel: Unexpected drop down index: " + 
						sourceType.getSelectedIndex());
				return;
			}
		}
		else if(source == linearLayout1){
			engine.setLinearLayout(1);
		}
		else if(source == linearLayout2){
			engine.setLinearLayout(2);
		}
		else if(source == linearLayout3){
			engine.setLinearLayout(3);
		}
		else if(source == linearLayout4){
			engine.setLinearLayout(4);
		}
	}
	
	public void itemStateChanged(ItemEvent e){
		Object source = e.getSource();
		
		if(source == quality){
			int selected = quality.getSelectedIndex();
			switch(selected){
			case 0: 
				engine.setPoolOptions(50, false); 
				break;
			case 1: 
				engine.setPoolOptions(50, true);
				break;
			case 2: 
				engine.setPoolOptions(100, true);
				break; 
			case 3: 
				engine.setPoolOptions(200, false);
				break;
			case 4: 
				engine.setPoolOptions(200, true);
				break;
			case 5: 
				resolutionDialog.setVisible(true);
			}
		}
	}

}
