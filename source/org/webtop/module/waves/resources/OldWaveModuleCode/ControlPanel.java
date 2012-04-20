package webtop.wave;

import java.awt.*;
import java.awt.event.*;

import webtop.component.*;
import webtop.wsl.client.*;
import webtop.wsl.event.*;

public class ControlPanel extends Panel implements ActionListener,ItemListener {
	private static final int DEF_QUALITY=4;
	//Indices into the dropdown
	private static final int POINT_SOURCE=0,LINE_SOURCE=1,SAMPLING_STICK=2,PLUCKED=3,STRUCK=4;

	private Engine engine;
	private WSLPlayer wslPlayer;

	private Choice sourceType;
	private Button addButton;
	private Button playButton;
	private Button prevButton;
	private Button nextButton;

	private Button linearLayout1;
	private Button linearLayout2;
	private Button linearLayout3;
	private Button linearLayout4;

	private Choice quality;

	private ResolutionDialog resolutionDialog;

	public ControlPanel(WSLPlayer player) {
		wslPlayer=player;

		add(new Label("Add Source:", Label.RIGHT));

		sourceType = new Choice();
		sourceType.addItem("Point");
		sourceType.addItem("Line");
		sourceType.addItem("Sampling Stick");
		sourceType.addItem("Plucked");
		sourceType.addItem("Struck");
//		sourceType.setForeground(Color.black);
//		sourceType.setBackground(Color.white);
		add(sourceType);

		addButton = new Button(" Add ");
		addButton.addActionListener(this);
		add(addButton);

		prevButton = new Button("<");
		prevButton.addActionListener(this);
		add(prevButton);

		playButton = new Button(" Play ");
		playButton.addActionListener(this);
		add(playButton);

		nextButton = new Button(">");
		nextButton.addActionListener(this);
		add(nextButton);

		add(new Label("Linear Layout:", Label.RIGHT));

		linearLayout1 = new Button("1");
		linearLayout1.addActionListener(this);
		add(linearLayout1);

		linearLayout2 = new Button("2");
		linearLayout2.addActionListener(this);
		add(linearLayout2);

		linearLayout3 = new Button("3");
		linearLayout3.addActionListener(this);
		add(linearLayout3);

		linearLayout4 = new Button("4");
		linearLayout4.addActionListener(this);
		add(linearLayout4);

		quality = new Choice();
		quality.addItem("Fastest");
		quality.addItem("Fast");
		quality.addItem("Medium");
		quality.addItem("Smooth");
		quality.addItem("Very Smooth");
		quality.addItem("Custom...");
//		quality.setForeground(Color.black);
//		quality.setBackground(Color.white);
		quality.select(DEF_QUALITY);
		quality.addItemListener(this);
		add(quality);

		resolutionDialog = new ResolutionDialog();
	}

	public void reset() {
		quality.select(DEF_QUALITY);
		setLayoutButtonsEnabled(false);
		playButton.setLabel("Play");
	}

	public void setEngine(Engine e) {
		engine = e;
		resolutionDialog.setEngine(e);
		setLayoutButtonsEnabled(engine.getLinearCount()>0);
	}

	public void setPlaying(boolean playing) {
		if(playing) playButton.setLabel(" Stop ");
		else playButton.setLabel(" Play ");
	}

	public void setLayoutButtonsEnabled(boolean enabled) {
		linearLayout1.setEnabled(enabled);
		linearLayout2.setEnabled(enabled);
		linearLayout3.setEnabled(enabled);
		linearLayout4.setEnabled(enabled);
	}

	//This is called when a WSL script sets the resolution, so as to make the
	//list box and settings dialog agree.
	public void setResolution(int resolution, boolean normalPerVertex) {
		if(resolution==50 && !normalPerVertex) quality.select(0);
		else if(resolution==50 && normalPerVertex) quality.select(1);
		else if(resolution==100 && normalPerVertex) quality.select(2);
		else if(resolution==200 && !normalPerVertex) quality.select(3);
		else if(resolution==200 && normalPerVertex) quality.select(4);
		else {
			quality.select(5);
			resolutionDialog.setup(resolution,normalPerVertex);
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if(source==quality) {
			int selected = quality.getSelectedIndex();

			switch (selected) {
			case 0: engine.setPoolOptions(50, false); break;
			case 1: engine.setPoolOptions(50, true); break;
			case 2: engine.setPoolOptions(100, true); break;
			case 3: engine.setPoolOptions(200, false); break;
			case 4: engine.setPoolOptions(200, true); break;
			case 5: resolutionDialog.show(); break;
			}

			wslPlayer.recordActionPerformed("resolution", "" + engine.getResolution() + ":" + engine.getNormalPerVertex());
		}
	}

	//For random placement of new objects:
	private static float randomPosition()
	{return (float)(Math.random()-.5)*Engine.POOL_SIZE;}

	public void actionPerformed(ActionEvent e) {
		if(wslPlayer.isPlaying()) return;

		Object source = e.getSource();
		if(source==playButton) {
			if(engine.isPlaying()) {
				engine.pause();
				playButton.setLabel(" Play ");
				prevButton.setEnabled(true);
				nextButton.setEnabled(true);
				wslPlayer.recordActionPerformed("animation", "stop");
			} else {
				engine.play();
				playButton.setLabel(" Stop ");
				prevButton.setEnabled(false);
				nextButton.setEnabled(false);
				wslPlayer.recordActionPerformed("animation", "play");
			}
		} else if(source==prevButton) {
			engine.prevFrame();
			wslPlayer.recordActionPerformed("action", "prevFrame");
		} else if(source==nextButton) {
			engine.nextFrame();
			wslPlayer.recordActionPerformed("action", "nextFrame");
		} else if(source==addButton) {
			PoolWidget pw;
			switch(sourceType.getSelectedIndex()) {
			case POINT_SOURCE:
				pw = engine.addSource(4, 8, 0, randomPosition(), randomPosition());
				engine.selectWidget(pw);
				break;
			case LINE_SOURCE:
				pw = engine.addSource(4, 8, 0, 0);
				engine.selectWidget(pw);
				break;
			case SAMPLING_STICK:
				pw = engine.addSamplingStick(randomPosition(),randomPosition());
				engine.selectWidget(pw);
				break;
			case PLUCKED:
				float fpos[] = {randomPosition(),randomPosition()};
				pw = engine.addSource(20, 8, fpos);
				engine.selectWidget(pw);
				break;
			case STRUCK:
				float tpos[] = {randomPosition(),randomPosition()};
				pw = engine.addStruckSource(40, 8, tpos);
				engine.selectWidget(pw);
				break;
			default:
				System.err.println("ControlPanel: Unexpected dropdown index "+
													 sourceType.getSelectedIndex());
				return;
			}

			wslPlayer.recordObjectAdded(pw.toWSLNode());
		} else if(source==linearLayout1) {
			engine.setLinearLayout(1);
			wslPlayer.recordActionPerformed("linearLayout", "1");
		} else if(source==linearLayout2) {
			engine.setLinearLayout(2);
			wslPlayer.recordActionPerformed("linearLayout", "2");
		} else if(source==linearLayout3) {
			engine.setLinearLayout(3);
			wslPlayer.recordActionPerformed("linearLayout", "3");
		} else if(source==linearLayout4) {
			engine.setLinearLayout(4);
			wslPlayer.recordActionPerformed("linearLayout", "4");
		}
	}
}
