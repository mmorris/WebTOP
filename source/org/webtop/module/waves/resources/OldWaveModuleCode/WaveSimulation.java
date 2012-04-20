package webtop.wave;

import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;

import webtop.component.*;
import webtop.vrml.*;
import webtop.vrml.widget.NavigationPanel;
import webtop.util.script.NavigationPanelScripter;
import webtop.wsl.client.*;
import webtop.wsl.script.WSLNode;

public class WaveSimulation extends Applet implements WSLModule,EAI.InvalidEventListener {
	private EAI eai;
	private Engine engine;
	private ControlPanel controlPanel;
	private WidgetsPanel widgetsPanel;
	private StatusBar statusBar;

	private WSLPanel wslPanel;
	private WSLPlayer wslPlayer;
	//Remove once WAppletized:
	private NavigationPanelScripter nps;

	public void init() {
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.MIDDLE, 0));

		wslPlayer = new WSLPlayer(this);
		wslPanel = new WSLPanel(wslPlayer);

		setForeground(Color.white);
		setBackground(Color.darkGray.darker());

		controlPanel = new ControlPanel(wslPlayer);
		controlPanel.setForeground(Color.white);
		controlPanel.setBackground(Color.darkGray.darker());
		add(controlPanel);

		widgetsPanel = new WidgetsPanel(this);
		widgetsPanel.setForeground(Color.white);
		widgetsPanel.setBackground(Color.darkGray.darker());
		add(widgetsPanel);

		statusBar = new StatusBar("Welcome to the Wave Simulation Module");
		statusBar.setPreferredSize(new Dimension(getSize().width, 20));
		statusBar.setForeground(Color.yellow);
		statusBar.setBackground(Color.darkGray.darker());
		add(statusBar);

		add(wslPanel);
	}

	public void start() {
		eai = new EAI(this,this);
		engine = new Engine(this);

		NavigationPanel navPanel=new NavigationPanel(eai,eai.getNode("NavPanel"),(short)0,"Changes viewpoints.");
		nps=new NavigationPanelScripter(navPanel,wslPlayer);
		new RecursiveListener(this,new ViewpointReader(navPanel,KeyEvent.VK_F12,KeyEvent.SHIFT_MASK)).setup();
		new RecursiveListener(this,new ViewpointReset(navPanel,KeyEvent.VK_F12,0)).setup();

		wslPlayer.loadParameter(this);

		engine.start();
	}

	public synchronized void stop() {
		//Snap the engine out of any stupor it may be in, and kill off its thread:
		synchronized(engine) {engine.notifyAll();}
		engine.exit();
		engine = null;
		eai.world = null;
	}

	public EAI getEAI() {return eai;}

	public WidgetsPanel getSourcePanel() {return widgetsPanel;}

	public ControlPanel getControlPanel() {return controlPanel;}

	public StatusBar getStatusBar() {return statusBar;}

	public WSLPlayer getWSLPlayer() {return wslPlayer;}

	//VRML errors go here
	public void invalidEvent(String node, String event) {
		statusBar.setWarningText("ERROR: Cannot load module.  See Java Console for details.");
		setEnabled(false);
	}

	// -----------------------------------------------------
	// WSL Methods
	// -----------------------------------------------------

	public String getWSLModuleName() {return "wavesimulation";}

	public WSLNode toWSLNode() {
		final WSLNode node=engine.toWSLNode();
		nps.addTo(node);
		return node;
	}
}
