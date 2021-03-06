package loon.component;

import loon.LTexture;
import loon.canvas.LColor;
import loon.event.SysTouch;
import loon.opengl.GLEx;
import loon.utils.MathUtils;
import loon.utils.TArray;

public class LCheckGroup extends LComponent {

	private LCheckBox selectedBtn;

	private final TArray<LCheckBox> checks;

	private float minX = -1, minY = -1, maxX = -1, maxY = -1;

	public LCheckGroup() {
		super(0, 0, 1, 1);
		this.customRendering = false;
		this.checks = new TArray<LCheckBox>(10);
	}

	@Override
	public void createUI(GLEx g, int x, int y, LComponent component,
			LTexture[] buttonImage) {
		for (LCheckBox check : checks) {
			check.createUI(g);
		}
	}

	@Override
	public void update(long elapsedTime) {
		for (LCheckBox check : checks) {
			check.update(elapsedTime);
		}
	}

	public void add(LCheckBox check) {
		if (minX == -1) {
			minX = check.getX();
		}
		if (minY == -1) {
			minY = check.getY();
		}
		minX = MathUtils.min(minX, check.getX());
		minY = MathUtils.min(minY, check.getY());
		maxX += MathUtils.max(maxY, check.getWidth());
		maxY += MathUtils.max(maxY, check.getHeight());
		setLocation(minX, minY);
		setSize((int) maxX, (int) maxY);
		checks.add(check);
	}

	public void setColor(LColor c) {
		for (LCheckBox check : checks) {
			check.setColor(c);
		}
	}

	public TArray<LCheckBox> getCheckBoxs() {
		return checks;
	}

	@Override
	protected void processTouchDragged() {
		for (LCheckBox check : checks) {
			check.processTouchDragged();
		}
	}

	@Override
	protected void processTouchEntered() {
		for (LCheckBox check : checks) {
			check.processTouchEntered();
		}
	}

	@Override
	protected void processTouchExited() {
		for (LCheckBox check : checks) {
			check.processTouchExited();
		}
	}

	@Override
	protected void processKeyPressed() {
		for (LCheckBox check : checks) {
			check.processKeyPressed();
		}
	}

	@Override
	protected void processKeyReleased() {
		for (LCheckBox check : checks) {
			check.processKeyReleased();
		}
	}

	@Override
	protected void processTouchClicked() {
		for (LCheckBox check : checks) {
			check.processTouchClicked();
		}
	}

	@Override
	protected void processTouchPressed() {
		for (LCheckBox check : checks) {
			if (check.contains(SysTouch.getX(), SysTouch.getY())) {
				check.processTouchPressed();
				selectedBtn = check;
			}
		}
	}

	@Override
	protected void processTouchReleased() {
		if (selectedBtn != null) {
			if (selectedBtn.contains(SysTouch.getX(), SysTouch.getY())) {
				selectedBtn.processTouchReleased();
			}
			for (LCheckBox check : checks) {
				if (selectedBtn != check) {
					check.setTicked(false);
				}
			}
		}
	}

	@Override
	public String getUIName() {
		return "CheckGroup";
	}

}
