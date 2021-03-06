package org.test;

import loon.Screen;
import loon.action.sprite.node.LNEase;
import loon.action.sprite.node.LNEnd;
import loon.action.sprite.node.LNLabel;
import loon.action.sprite.node.LNMoveBy;
import loon.action.sprite.node.LNRotateBy;
import loon.action.sprite.node.LNSequence;
import loon.action.sprite.node.LNSprite;
import loon.event.GameTouch;
import loon.font.LFont;
import loon.opengl.GLEx;
import loon.utils.Easing;
import loon.utils.timer.LTimerContext;

public class Node2Test extends Screen {

	@Override
	public void draw(GLEx g) {

	}

	@Override
	public void onLoad() {
		// LNode可以分别渲染在SpriteBatch和GLEx之上，在SpriteBatchScreen中SpriteBatch渲染，在Screen中即GLEx渲染
		// 而最大的差异在于，普通Screen中的LNode默认不响应触屏事件（因为只在SpriteBatchScreen中做了处理）
		// 设置默认字体大小为20号字
		LFont.setDefaultFont(LFont.getFont(20));
		// 直接载入图片到节点(直接加载大图)
		LNSprite sprite = LNSprite.GInitWithFilename("assets/ccc.png");
		// 支持拖拽
		sprite.setLocked(false);
		sprite.setLimitMove(false);
		sprite.Tag = "Test";

		add(sprite);

		LNLabel label = new LNLabel();
		label.setString("测试中");
		label.setRotation(60);
		label.setLocation(120, 120);
		add(label);

		// 执行node动画
		sprite.runAction(LNSequence.Action(LNEase.Action(Easing.BACK_IN_OUT,
				LNMoveBy.Action(1f, 225, 125)), LNRotateBy.Action(0.5f, 360),
				LNEnd.Action()));

		add(MultiScreenTest.getBackButton(this));

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
