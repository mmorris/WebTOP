package webtop.wave;

import java.awt.*;

import webtop.wsl.client.WSLPlayer;

public class WidgetSwitcher extends Panel {
	private static final int NONE = 0,
													 LINEAR = 1,
													 RADIAL = 2,
													 SAMPLE = 3,
											  	 PLUCKED = 4,
		                       STRUCK = 5;

	private final LinearPanel linear;
	private final RadialPanel radial;
	private final SamplePanel sample;
	private final PluckedPanel plucked;
	private final PluckedPanel struck;
	private final CardLayout layout;

	private int showing;

	private Panel activePanel;
	private SourcePanel activeSourcePanel;	//null if no panel or if not source panel
	private PluckedPanel activeTSPanel; //noll if not a TS panel

	public WidgetSwitcher(WSLPlayer player) {
		setLayout(layout = new CardLayout());
		linear = new LinearPanel(player);
		radial = new RadialPanel(player);
		sample = new SamplePanel(player);
		plucked = new PluckedPanel(player);
		struck = new PluckedPanel(player);
		add("none", new Panel());
		add("linear", linear);
		add("radial", radial);
		add("sample", sample);
		add("plucked", plucked);
		add("struck", struck);
	}

	public void setEngine(Engine e) {
		linear.setEngine(e);
		radial.setEngine(e);
	}

	private void show(int which) {
		switch (which) {
		case NONE:
			layout.show(this, "none");
			showing = NONE;
			activePanel=activeSourcePanel=null;
			activeTSPanel=null;
			break;
		case LINEAR:
			layout.show(this, "linear");
			showing = LINEAR;
			activePanel=activeSourcePanel=linear;
			break;
		case RADIAL:
			layout.show(this, "radial");
			showing = RADIAL;
			activePanel=activeSourcePanel=radial;
			break;
		case SAMPLE:
			layout.show(this, "sample");
			showing = SAMPLE;
			activePanel=sample;
			activeSourcePanel=null;
			activeTSPanel=null;
			break;
		case PLUCKED:
			layout.show(this, "plucked");
			showing = PLUCKED;
			activePanel=plucked;
			activeTSPanel=plucked;
			activeSourcePanel=null;
			break;
		case STRUCK:
			layout.show(this, "struck");
			showing = STRUCK;
			activePanel=struck;
			activeTSPanel=struck;
			activeSourcePanel=null;
		}
	}

	public void show(PoolWidget s) {
		if(s == null) {
			show(NONE);
		} else if(s instanceof LinearSource) {
			show(LINEAR);
			linear.show((LinearSource) s);
		} else if(s instanceof RadialSource) {
			show(RADIAL);
			radial.show((RadialSource)s);
		} else if(s instanceof SamplingStick) {
			show(SAMPLE);
			sample.show((SamplingStick)s);
		}else if (s instanceof PluckedSource) {
			show(PLUCKED);
			plucked.show((PluckedSource)s);
		}
		else if (s instanceof StruckSource) {
			show(STRUCK);
			struck.show((StruckSource)s);
		}
	}

	//Is this needed?	 Won't CardLayout do it for us?
	/*public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		linear.setBounds(0, 0, width, height);
		radial.setBounds(0, 0, width, height);
	}*/

	public int getShowing() {
		return showing;
	}

	public Panel getActivePanel() {return activePanel;}
	public SourcePanel getActiveSourcePanel() { return activeSourcePanel; }
	public PluckedPanel getActiveSSourcePanel() { return activeTSPanel; }
}
