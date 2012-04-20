package webtop.wave;

import java.awt.*;
import java.awt.event.*;

import webtop.util.WTMath;
import webtop.wsl.client.*;
import webtop.wsl.event.*;
import sdl.gui.numberbox.*;

public class LinearPanel extends SourcePanel implements NumberBox.Listener {
	private FloatBox angle;

	private LinearSource lSource;

	public LinearPanel(WSLPlayer player) {
		super(player);

		add(new Label("Angle:",Label.RIGHT));
		angle = new FloatBox(0,360,4,4);
		angle.addNumberListener(this);
		add(angle);
	}

	public void show(LinearSource s) {
		super.show(s);
		lSource=s;
		if(s!=null) {
			angle.setValue(WTMath.toDegs(s.getAngle()));
			angle.setEnabled(true);
		} else {
			angle.setEnabled(false);
		}
	}

	public void setAngle(float a) {angle.silence(); angle.setValue(WTMath.toDegs(a));}

	public void numChanged(NumberBox eventSource, Number newValue){
		if(wslPlayer.isPlaying()) return;

		if(eventSource==angle) {
			lSource.setAngle(WTMath.toRads(angle.getValue()), true);

			if(wslPlayer!=null)
				wslPlayer.recordActionPerformed(lSource.getID(), "angle", String.valueOf(WTMath.toDegs(lSource.getAngle())));
		}

		super.numChanged(eventSource, newValue);
	}

	public void invalidEntry(NumberBox src, Number badVal){
		if(src==angle)
			engine.getStatusBar().setText("Angle must be between 0 and 360 degrees.");
	}
	public void boundsForcedChange(NumberBox src, Number oldVal){}
}
