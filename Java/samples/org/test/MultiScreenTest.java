package org.test;

import loon.LObject;
import loon.LSystem;
import loon.LTransition;
import loon.Screen;
import loon.action.ActionBind;
import loon.action.ActionListener;
import loon.action.ActionTween;
import loon.canvas.LColor;
import loon.component.LClickButton;
import loon.component.LComponent;
import loon.component.layout.LayoutManager;
import loon.event.ActionKey;
import loon.event.ClickListener;
import loon.event.GameTouch;
import loon.font.BMFont;
import loon.font.LFont;
import loon.opengl.GLEx;
import loon.utils.TArray;
import loon.utils.processes.RealtimeProcess;
import loon.utils.timer.LTimerContext;

public class MultiScreenTest extends Screen {

	@Override
	public LTransition onTransition() {
		return LTransition.newPixelThunder(LColor.yellow);
	}

	@Override
	public void draw(GLEx g) {


	}

	public static LClickButton getBackButton(final Screen screen) {

		LClickButton back = new LClickButton("Back", screen.getWidth() - 100,
				screen.getHeight() - 70, 80, 50);
		back.SetClick(new ClickListener() {

			@Override
			public void UpClick(LComponent comp, float x, float y) {

			}

			@Override
			public void DragClick(LComponent comp, float x, float y) {

			}

			@Override
			public void DownClick(LComponent comp, float x, float y) {

			}

			// 事件锁，让点击唯一化
			ActionKey click = new ActionKey(ActionKey.DETECT_INITIAL_PRESS_ONLY);

			@Override
			public void DoClick(LComponent comp) {
				if (!click.isPressed()) {

					// 为按钮设置一个旋转动画，每次前进36度
					set(comp).rotateTo(360, 36f).start()
							.setActionListener(new ActionListener() { // 监听动作事件

										// 事件完毕后，调用screen标记为main的
										@Override
										public void stop(ActionBind o) {
											screen.runScreen("main");
											// 还原当前动作角色旋转角度为0
											o.setRotation(0);
										}

										@Override
										public void start(ActionBind o) {

										}

										@Override
										public void process(ActionBind o) {

										}
									});
					click.press();
				}

			}
		});
		return back;
	}

	// 制作一个按钮监听器
	private class MyClickListener implements ClickListener {

		@Override
		public void DoClick(LComponent comp) {

		}

		@Override
		public void DownClick(LComponent comp, float x, float y) {
			if (comp instanceof LClickButton) {
				// 查看LObject状态设置
				switch (comp.getStatus()) {
				case LObject.NOT: // 如果对象无状态设置
					LClickButton click = (LClickButton) comp;
					String text = click.getText();
					// 由于将按钮名与Screen名设定的一样，所以直接调用按钮名就可以运行指定Scrren了
					runScreen(text);
					break;
				case LObject.FALSE: // 切换新的示例Screen
					// wait code
					break;
				case LObject.TRUE:// 退出
					LSystem.exit();
					break;
				default:
					break;
				}
			}
		}

		@Override
		public void UpClick(LComponent comp, float x, float y) {

		}

		@Override
		public void DragClick(LComponent comp, float x, float y) {

		}

	}

	final String[] names = { "MessageBox", "Live2d", "Action", "Effect",
			"Stage", "TileMap", "SpriteBatch", "BatchScreen", "BMFont",
			"Layout", "Table", "Menu", "Names", "Toast", "List", "Sprite",
			"TexturePack", "LNode", "Scroll", "Cycle", "TextArea", "Progress",
			"Particle", "SelectIcon", "Control", "JsonRes", "SheetFont",
			"ParConfig", "RippleTouch", "Sound", "Gesture", "Physical",
			"LNode2", "Input", "Depth", "Canvas", "PlayerClick", "MoveClip",
			"TextureImage","Session" };

	static BMFont info_font;

	@Override
	public void onLoad() {
		// 设置默认字体大小为15
		LFont.setDefaultFont(LFont.getFont(15));
		// 使用图片字体(如果不设置，则loon默认使用当前系统字体)
		if (info_font == null) {
			try {
				// 加载
				info_font = new BMFont("assets/info.fnt", "assets/info.png");
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 缩放为0.6倍
			info_font.setFontScale(0.6f);
		}
		int index = 0;
		// 构建一个通用的监听器
		MyClickListener clickListener = new MyClickListener();
		// 预先设定多个Screen，并赋予名称
		addScreen("main", this);
		addScreen(names[index++], new LMessageBoxTest());
		addScreen(names[index++], new Live2dTest());
		addScreen(names[index++], new ActionEventTest());
		addScreen(names[index++], new EffectTest());
		addScreen(names[index++], new StageTest.ScreenTest());
		addScreen(names[index++], new TileMapTest());
		addScreen(names[index++], new SpriteBatchTest());
		addScreen(names[index++], new SpriteBatchScreenTest());
		addScreen(names[index++], new BMFontTest());
		addScreen(names[index++], new LayoutTest());
		addScreen(names[index++], new TableTest());
		addScreen(names[index++], new MenuTest());
		addScreen(names[index++], new DecideNameTest());
		addScreen(names[index++], new ToastTest());
		addScreen(names[index++], new ListTest());
		addScreen(names[index++], new SpriteTest());
		addScreen(names[index++], new LTexturePackTest());
		addScreen(names[index++], new NodeTest());
		addScreen(names[index++], new ScrollTest());
		addScreen(names[index++], new CycleTest());
		addScreen(names[index++], new TextAreaTest());
		addScreen(names[index++], new ProgressTest());
		addScreen(names[index++], new ParticleTest());
		addScreen(names[index++], new SelectIconTest());
		addScreen(names[index++], new ControlTest());
		addScreen(names[index++], new JSonResTest());
		addScreen(names[index++], new SpriteSheetFontTest());
		addScreen(names[index++], new ParticleConfigTest());
		addScreen(names[index++], new RippleTouchTest());
		addScreen(names[index++], new SoundTest());
		addScreen(names[index++], new GestureTest());
		addScreen(names[index++], new PhysicalTest());
		addScreen(names[index++], new Node2Test());
		addScreen(names[index++], new SysInputTest());
		addScreen(names[index++], new DepthTest());
		addScreen(names[index++], new CanvasLayerTest());
		addScreen(names[index++], new PlayerClickTest());
		addScreen(names[index++], new MovieClipTest());
		addScreen(names[index++], new TextureImageTest());
		addScreen(names[index++], new SessionTest());
		
		// 默认按钮大小为100x25
		int btnWidth = 100;
		int btnHeight = 25;
		// 添加一组按钮布局，并返回按钮对象
		TArray<LClickButton> clicks = LayoutManager.elementButtons(this, names,
				15, 25, btnWidth, btnHeight, clickListener,
				LSystem.viewSize.getHeight() - btnHeight);

		final TArray<ActionTween> tweens = new TArray<ActionTween>();

		// 首先让按钮不可见
		for (LClickButton btn : clicks) {
			btn.setAlpha(0);
			// 使用图片字体
			btn.setFont(info_font);
			// 为按钮设置事件，并加载入一个集合
			tweens.add(set(btn));
		}

		// 设置一个退出按钮
		LClickButton exitClick = LClickButton.make(48, 48, "cross.png",
				"cross_effect.png", "cross_effect.png");
		// 设定一个特殊状态为true
		exitClick.setStatus(LObject.TRUE);
		// 设置监听
		exitClick.SetClick(clickListener);
		// 初始透明度0
		exitClick.setAlpha(0);
		// 按钮置顶
		topOn(exitClick);
		// 偏移Screen大小-按钮大小-5
		exitClick.setX(getWidth() - exitClick.getWidth() - 5);
		add(exitClick);
		tweens.add(set(exitClick));

		// 设置一个下页按钮
		LClickButton nextClick = LClickButton.make("NEXT", 45, 25);
		// 设定一个特殊状态为false
		nextClick.setStatus(LObject.FALSE);
		// 设置监听
		nextClick.SetClick(clickListener);
		// 初始透明度0
		nextClick.setAlpha(0);
		nextClick.setFont(info_font);
		// 偏移Screen大小-按钮大小-5
		nextClick.setX(getWidth() - nextClick.getWidth() - 5);
		nextClick.setY(getHeight() - nextClick.getHeight() - 24);
		add(nextClick);
		tweens.add(set(nextClick));

		// 设定一个游戏进程，半秒后让按钮导入
		RealtimeProcess process = new RealtimeProcess() {

			@Override
			public void run(LTimerContext time) {
				// 当Screen过渡动画播放完毕后
				if (isTransitionCompleted()) {
					// 穷举按钮事件
					for (ActionTween tween : tweens) {
						// 淡出事件，开始执行
						tween.fadeOut(10f).start();
						// 删除单独进程（否则会不断执行）
						kill();
					}
				}

			}
		};
		// 延迟半秒执行
		process.setDelay(LSystem.SECOND / 2);
		addProcess(process);

	}

	@Override
	public void alter(LTimerContext timer) {

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void touchDown(GameTouch e) {

	}

	@Override
	public void touchUp(GameTouch e) {

	}

	@Override
	public void touchMove(GameTouch e) {

	}

	@Override
	public void touchDrag(GameTouch e) {

	}

	@Override
	public void resume() {

	}

	@Override
	public void pause() {

	}

	@Override
	public void close() {

	}

}
