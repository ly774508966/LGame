﻿package org.test.zombiedefence;

import loon.LTexture;
import loon.action.sprite.SpriteBatch;
import loon.geom.Vector2f;
import loon.opengl.BlendState;
import loon.utils.MathUtils;

public class Smoke extends DrawableObject {
	private float angleInc;
	public int damage;
	public boolean isActive;
	private Vector2f positionInc;
	public int radius;

	public Smoke(LTexture texture, Vector2f position) {
		super(texture, position);
		super.life = 60;
		super.scale = new Vector2f(0.3f, 0.3f);
		this.isActive = true;
		this.positionInc = new Vector2f(
				((MathUtils.random()) * 2f) - 1f,
				(-(MathUtils.random()) * 3f) - 0.3f);
		this.angleInc = 0.1570796f * ((MathUtils.random()) - 0.5f);
	}

	@Override
	public void Draw(SpriteBatch batch) {
		super.Draw(batch);
		batch.submit(BlendState.Additive);
	}

	@Override
	public void Update() {
		super.Update();
		super.position.addSelf(this.positionInc);
		super.scale.addSelf(0.02f, 0.02f);
		super.angle += this.angleInc;
		super.alpha -= 0.018f;
	}
}