package org.nerdcode.droidboid;

public class Vector2D {
	float x, y;

	public Vector2D()
	{
		x = 0;
		y = 0;
	}

	public Vector2D(float initX, float initY)
	{
		x = initX;
		y = initY;
	}

	public void setXY(float x, float y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public String toString()
	{
		return "" + x + ", " + y;
	}

}
