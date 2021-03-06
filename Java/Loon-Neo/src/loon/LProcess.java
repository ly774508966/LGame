/**
 * Copyright 2008 - 2015 The Loon Game Engine Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.5
 */
package loon;

import loon.LGame.Status;
import loon.canvas.LColor;
import loon.event.GameKey;
import loon.event.GameTouch;
import loon.event.InputMake;
import loon.event.KeyMake;
import loon.event.MouseMake;
import loon.event.SysInputFactory;
import loon.event.TouchMake;
import loon.event.Updateable;
import loon.opengl.GLEx;
import loon.opengl.LSTRDictionary;
import loon.stage.PlayerUtils;
import loon.stage.RootPlayer;
import loon.stage.StageSystem;
import loon.stage.StageTransition;
import loon.utils.ListMap;
import loon.utils.MathUtils;
import loon.utils.TArray;
import loon.utils.processes.RealtimeProcess;
import loon.utils.processes.RealtimeProcessManager;
import loon.utils.reply.Port;
import loon.utils.timer.LTimerContext;

public class LProcess extends PlayerUtils {

	TArray<Updateable> loads;

	TArray<Updateable> unloads;

	EmulatorListener emulatorListener;

	private EmulatorButtons emulatorButtons;

	private final ListMap<CharSequence, Screen> _screenMap;

	private final TArray<Screen> _screens;

	private boolean isInstance;

	private int id, width, height;

	private boolean waitTransition;

	private boolean running;

	private Screen currentScreen, loadingScreen;

	private LTransition transition;

	private final SysInputFactory currentInput;

	protected final StageSystem stageSystem;

	protected final RootPlayer rootPlayer;

	private final LGame game;

	public LProcess(LGame game) {
		super();
		this.game = game;
		this.currentInput = new SysInputFactory(this);
		this._screens = new TArray<Screen>();
		this._screenMap = new ListMap<CharSequence, Screen>();
		this.clear();
		InputMake input = game.input();
		if (input != null) {
			if (!game.setting.emulateTouch && !game.isMobile()) {
				input.mouseEvents.connect(new MouseMake.ButtonSlot() {
					public void onEmit(MouseMake.ButtonEvent event) {
						currentInput.callMouse(event);
					}
				});
			} else {
				input.touchEvents.connect(new Port<TouchMake.Event[]>() {
					@Override
					public void onEmit(TouchMake.Event[] events) {
						currentInput.callTouch(events);
					}
				});
			}
			input.keyboardEvents.connect(new KeyMake.KeyPort() {
				@Override
				public void onEmit(KeyMake.KeyEvent e) {
					currentInput.callKey(e);
				}
			});
		}
		this.rootPlayer = new RootPlayer();
		this.stageSystem = new StageSystem(rootPlayer) {
			@Override
			protected StageTransition defaultPushTransition() {
				return newSlide();
			}

			@Override
			protected StageTransition defaultPopTransition() {
				return newSlide().right();
			}
		};
		game.status.connect(new Port<LGame.Status>() {

			@Override
			public void onEmit(Status event) {
				switch (event) {
				case EXIT:
					stop();
					break;
				case RESUME:
					LSystem.PAUSED = false;
					resume();
					break;
				case PAUSE:
					LSystem.PAUSED = true;
					pause();
					break;
				default:
					break;
				}
			}
		});
	}

	private final static void callUpdateable(final TArray<Updateable> list) {
		TArray<Updateable> loadCache;
		synchronized (list) {
			loadCache = new TArray<Updateable>(list);
			list.clear();
		}
		for (int i = 0; i < loadCache.size; i++) {
			Updateable running = loadCache.get(i);
			synchronized (running) {
				running.action(null);
			}
		}
		loadCache = null;
	}

	public final SysInputFactory getCurrentSysInput() {
		return currentInput;
	}

	// --- Load start ---//

	public void addLoad(Updateable u) {
		synchronized (loads) {
			loads.add(u);
		}
	}

	public void removeLoad(Updateable u) {
		synchronized (loads) {
			loads.remove(u);
		}
	}

	public void removeAllLoad() {
		synchronized (loads) {
			loads.clear();
		}
	}

	public void load() {
		if (isInstance) {
			final int count = loads.size;
			if (count > 0) {
				callUpdateable(loads);
			}
		}
	}

	// --- Load end ---//

	// --- UnLoad start ---//

	public void addUnLoad(Updateable u) {
		synchronized (unloads) {
			unloads.add(u);
		}
	}

	public void removeUnLoad(Updateable u) {
		synchronized (unloads) {
			unloads.remove(u);
		}
	}

	public void removeAllUnLoad() {
		synchronized (unloads) {
			unloads.clear();
		}
	}

	public void unload() {
		if (isInstance) {
			final int count = unloads.size;
			if (count > 0) {
				callUpdateable(unloads);
			}
		}
	}

	// --- UnLoad end ---//

	private void setScreen(final Screen screen, boolean put) {
		if (loadingScreen != null && loadingScreen.isOnLoadComplete()) {
			return;
		}
		synchronized (this) {
			if (screen == null) {
				this.isInstance = false;
				throw new RuntimeException(
						"Cannot create a [Screen] instance !");
			}
			if (!game.display().showLogo) {
				if (currentScreen != null) {
					setTransition(screen.onTransition());
				} else {
					// 为了防止画面单调，Loon默认为无设定Transition的，首个Screen随机增加一个特效
					// 不想使用，或者需要设定的话，请重载Screen的onTransition函数。
					// 不使用：返回: LTransition.newEmpty()
					// 使用：返回: 设定或者自定义一个LTransition对象.
					LTransition transition = screen.onTransition();
					if (transition == null) {
						int rad = MathUtils.random(0, 10);
						switch (rad) {
						case 0:
							transition = LTransition.newFadeIn();
							break;
						case 1:
							transition = LTransition.newArc();
							break;
						case 2:
							transition = LTransition
									.newSplitRandom(LColor.black);
							break;
						case 3:
							transition = LTransition
									.newCrossRandom(LColor.black);
							break;
						case 4:
							transition = LTransition
									.newFadeOvalIn(LColor.black);
							break;
						case 5:
							transition = LTransition.newPixelWind(LColor.white);
							break;
						case 6:
							transition = LTransition
									.newPixelDarkOut(LColor.black);
							break;
						case 7:
							transition = LTransition
									.newPixelThunder(LColor.black);
							break;
						case 8:
							transition = LTransition.newFadeDotIn(LColor.black);
							break;
						case 9:
							transition = LTransition
									.newFadeTileIn(LColor.black);
							break;
						case 10:
							transition = LTransition
									.newFadeSpiralIn(LColor.black);
							break;
						}
					}
					setTransition(transition);
				}
			}

			screen.setOnLoadState(false);
			if (currentScreen == null) {
				currentScreen = screen;
			} else {
				synchronized (currentScreen) {
					currentScreen.destroy();
					currentScreen = screen;
				}
			}
			this.isInstance = true;
			if (screen instanceof EmulatorListener) {
				setEmulatorListener((EmulatorListener) screen);
			} else {
				setEmulatorListener(null);
			}

			screen.onCreate(LSystem.viewSize.getWidth(),
					LSystem.viewSize.getHeight());

			RealtimeProcess process = new RealtimeProcess() {

				@Override
				public void run(LTimerContext time) {
					if (!LSystem._base.display().showLogo) {
						startTransition();
						screen.setClose(false);
						screen.resetBase();
						screen.onLoad();
						screen.onLoaded();
						screen.setOnLoadState(true);
						endTransition();
						kill();
					}
				}
			};
			process.setDelay(0);

			RealtimeProcessManager.get().addProcess(process);

			if (put) {
				_screens.add(screen);
			}
			loadingScreen = null;
		}
	}

	public void start() {
		if (!running) {
			if (loadingScreen != null) {
				setScreen(loadingScreen);
			}
			running = true;
		}
	}

	public void resize(int w, int h) {
		if (isInstance) {
			currentScreen.resize(w, h);
		}
	}

	public void resume() {
		if (isInstance) {
			currentScreen.resume();
		}
	}

	public void pause() {
		if (isInstance) {
			currentScreen.pause();
		}
	}

	public void stop() {
		running = false;
		if (isInstance) {
			currentScreen.stop();
		}
		endTransition();
		if (isInstance) {
			isInstance = false;
			unloads.clear();
			if (currentScreen != null) {
				currentScreen.destroy();
				currentScreen = null;
			}
			RealtimeProcessManager.get().close();
			LSTRDictionary.dispose();
			LTextures.destroyAll();
		}
		LSystem._base.log().debug("The Loon Game Engine is End");
	}

	public void resetTouch() {
		currentInput.resetSysTouch();
	}

	public void clear() {
		if (loads == null) {
			loads = new TArray<Updateable>(10);
		} else {
			loads.clear();
		}
		if (unloads == null) {
			unloads = new TArray<Updateable>(10);
		} else {
			unloads.clear();
		}
		clearScreens();
	}

	public void calls() {
		if (isInstance) {
			LTextureBatch.clearBatchCaches();
		}
	}

	public boolean next() {
		if (isInstance) {
			if (currentScreen.next()) {
				return true;
			}
		}
		return false;
	}

	public void runTimer(LTimerContext context) {
		if (isInstance) {
			if (waitTransition) {
				if (transition != null) {
					switch (transition.code) {
					default:
						if (!currentScreen.isOnLoadComplete()) {
							transition.update(context.timeSinceLastUpdate);
						}
						break;
					case 1:
						if (!transition.completed()) {
							transition.update(context.timeSinceLastUpdate);
						} else {
							endTransition();
						}
						break;
					}
				}
			} else {
				currentScreen.runTimer(context);
				return;
			}
		}
	}

	public void draw(GLEx g) {
		if (isInstance) {
			if (waitTransition) {
				if (transition != null) {
					if (transition.isDisplayGameUI) {
						currentScreen.createUI(g);
					}
					switch (transition.code) {
					default:
						if (!currentScreen.isOnLoadComplete()) {
							transition.draw(g);
						}
						break;
					case 1:
						if (!transition.completed()) {
							transition.draw(g);
						}
						break;
					}
				}
			} else {
				currentScreen.createUI(g);
				return;
			}
		}
	}

	public void drawEmulator(GLEx gl) {
		if (emulatorButtons != null) {
			emulatorButtons.draw(gl);
		}
	}

	public LColor getColor() {
		if (isInstance) {
			return currentScreen.getColor();
		}
		return null;
	}

	public LTexture getBackground() {
		if (isInstance) {
			return currentScreen.getBackground();
		}
		return null;
	}

	public int getRepaintMode() {
		if (isInstance) {
			return currentScreen.getRepaintMode();
		}
		return Screen.SCREEN_NOT_REPAINT;
	}

	/**
	 * 设定模拟按钮监听器
	 * 
	 * @param emulatorListener
	 */
	public void setEmulatorListener(EmulatorListener emulator) {
		this.emulatorListener = emulator;
		if (emulatorListener != null) {
			if (emulatorButtons == null) {
				emulatorButtons = new EmulatorButtons(emulatorListener,
						LSystem.viewSize.getWidth(),
						LSystem.viewSize.getHeight());
			} else {
				emulatorButtons.setEmulatorListener(emulator);
			}
		} else {
			emulatorButtons = null;
		}
	}

	/**
	 * 获得模拟器监听
	 * 
	 * @return
	 */
	public EmulatorListener getEmulatorListener() {
		return emulatorListener;
	}

	/**
	 * 获得模拟器按钮
	 * 
	 * @return
	 */
	public EmulatorButtons getEmulatorButtons() {
		return emulatorButtons;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public final void setTransition(LTransition t) {
		this.transition = t;
	}

	public final boolean isTransitioning() {
		return waitTransition;
	}

	public boolean isTransitionCompleted() {
		return !waitTransition;
	}

	public final LTransition getTransition() {
		return this.transition;
	}

	private final void startTransition() {
		if (transition != null) {
			waitTransition = true;
			if (isInstance) {
				currentScreen.setLock(true);
			}
		}
	}

	private final void endTransition() {
		if (transition != null) {
			switch (transition.code) {
			default:
				waitTransition = false;
				transition.close();
				break;
			case 1:
				if (transition.completed()) {
					waitTransition = false;
					transition.close();
				}
				break;
			}
			if (isInstance) {
				currentScreen.setLock(false);
			}
		} else {
			waitTransition = false;
		}
	}

	public float getX() {
		if (isInstance) {
			return currentScreen.getX();
		}
		return 0;
	}

	public float getY() {
		if (isInstance) {
			return currentScreen.getY();
		}
		return 0;
	}

	public Screen getScreen() {
		return currentScreen;
	}

	public void clearScreens() {
		_screenMap.clear();
		_screens.clear();
	}

	public void addScreen(CharSequence name, Screen screen) {
		if (!_screenMap.containsKey(name)) {
			_screenMap.put(name, screen);
			addScreen(screen);
		}
	}

	public Screen getScreen(CharSequence name) {
		Screen screen = _screenMap.get(name);
		if (screen != null) {
			return screen;
		}
		return null;
	}

	public Screen runScreen(CharSequence name) {
		Screen screen = getScreen(name);
		if (screen != null) {
			setScreen(screen);
			return screen;
		}
		return null;
	}

	public void runFirstScreen() {
		int size = _screens.size;
		if (size > 0) {
			Screen o = _screens.first();
			if (o != currentScreen) {
				setScreen((Screen) o, false);
			}
		}
	}

	public void runLastScreen() {
		int size = _screens.size;
		if (size > 0) {
			Screen o = _screens.last();
			if (o != currentScreen) {
				setScreen(o, false);
			}
		}
	}

	public void runPreviousScreen() {
		int size = _screens.size;
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				if (currentScreen == _screens.get(i)) {
					if (i - 1 > -1) {
						setScreen(_screens.get(i - 1), false);
						return;
					}
				}
			}
		}
	}

	public void runNextScreen() {
		int size = _screens.size;
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				if (currentScreen == _screens.get(i)) {
					if (i + 1 < size) {
						setScreen(_screens.get(i + 1), false);
						return;
					}
				}
			}
		}
	}

	public void runIndexScreen(int index) {
		int size = _screens.size;
		if (size > 0 && index > -1 && index < size) {
			Object o = _screens.get(index);
			if (currentScreen != o) {
				setScreen(_screens.get(index), false);
			}
		}
	}

	public boolean containsScreen(final Screen screen) {
		if (screen == null) {
			throw new RuntimeException("Cannot create a [IScreen] instance !");
		}
		return _screens.contains(screen);
	}

	public void addScreen(final Screen screen) {
		if (screen == null) {
			throw new RuntimeException("Cannot create a [IScreen] instance !");
		}
		if (!_screens.contains(screen)) {
			_screens.add(screen);
		}
	}

	public TArray<Screen> getScreens() {
		return _screens;
	}

	public int getScreenCount() {
		return _screens.size;
	}

	public void setScreen(final Screen screen) {
		if (screen.handler == null) {
			screen.resetBase();
		}
		if (game.setting.isLogo && game.display().showLogo) {
			loadingScreen = screen;
		} else {
			setScreen(screen, true);
		}
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setCurrentScreen(final Screen screen) {
		if (screen != null) {
			this.isInstance = false;
			if (currentScreen != null) {
				currentScreen.destroy();
			}
			this.currentScreen = screen;
			currentScreen.setLock(false);
			currentScreen.setLocation(0, 0);
			currentScreen.setClose(false);
			currentScreen.setOnLoadState(true);
			if (screen.getBackground() != null) {
				currentScreen.setRepaintMode(Screen.SCREEN_TEXTURE_REPAINT);
			}
			this.isInstance = true;
			if (screen instanceof EmulatorListener) {
				setEmulatorListener((EmulatorListener) screen);
			} else {
				setEmulatorListener(null);
			}
			this._screens.add(screen);
		}
	}

	public void keyDown(GameKey e) {
		if (isInstance) {
			currentScreen.keyPressed(e);
		}
	}

	public void keyUp(GameKey e) {
		if (isInstance) {
			currentScreen.keyReleased(e);
		}
	}

	public void keyTyped(GameKey e) {
		if (isInstance) {
			currentScreen.keyTyped(e);
		}
	}

	public void mousePressed(GameTouch e) {
		if (isInstance) {
			currentScreen.mousePressed(e);
		}
	}

	public void mouseReleased(GameTouch e) {
		if (isInstance) {
			currentScreen.mouseReleased(e);
		}
	}

	public void mouseMoved(GameTouch e) {
		if (isInstance) {
			currentScreen.mouseMoved(e);
		}
	}

	public void mouseDragged(GameTouch e) {
		if (isInstance) {
			currentScreen.mouseDragged(e);
		}
	}

	public RootPlayer getRootPlayer() {
		return rootPlayer;
	}

	public StageSystem getStageSystem() {
		return stageSystem;
	}

	public LGame getGame() {
		return game;
	}
}
