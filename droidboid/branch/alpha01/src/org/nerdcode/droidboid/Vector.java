package org.nerdcode.droidboid;

public class Vector {
	float x, y;

	public Vector()
	{
		x = 0;
		y = 0;
	}

	public Vector(float initX, float initY)
	{
		x = initX;
		y = initY;
	}

	public void setXY(float x, float y) {
		this.x = x;
		this.y = y;
	}

}
