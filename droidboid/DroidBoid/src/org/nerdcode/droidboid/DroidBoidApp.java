package org.nerdcode.droidboid;

import android.app.Application;

public class DroidBoidApp extends Application
{	
	//-----------------Variables---------------
	private int rule1 = 50;		//Controller values are 0 - 1f
	private int rule2 = 50;		//Controller values are 0 - 1f
	private int rule3 = 50;		//Controller values are 0 - 1f
	private int rule4 = 50;		//Controller values are 0 - 1f
	private int rule6 = 50;		//Controller values are 0 - 1f
	//-----------------Consts------------------
	private int CENTER_PULL_FACTOR = 80;			//How powerful the pull of the Center of the flock is for Rule 1
	private int TARGET_PULL_FACTOR = 80;			//How powerful the mouse is at directing the Boids for Rule 4
	private int BOUNCE_ABSORPTION = 50;	//0.5f		//How much speed is left when you bounce off a wall
	private int VELOCITY_PULL_FACTOR = 80;			//How powerful the speed matching with the flock is
	private int MIN_DISTANCE = 10;				//The minimum distance to be held between Boids for Rule 2
	private int VELOCITY_LIMITER = 15; //1.5f		//Limits the maximum velocity of the Boid
	
	private int flockSize = 50;
	
	//-----------------Colors------------------
	
	//Type 0 is Manual
	private int rType = 0;
	private int gType = 0;
	private int bType = 0;
	
	//Default RGB Value
	private int rValue = 255;
	private int gValue = 0;
	private int bValue = 0;
	
	
	//-----------------Getters-----------------
	public int[] getRules() {
		int[] rules =  {rule1, rule2, rule3, rule4, rule6};
		return rules;
	}
	
	public int getFlockSize() {
		return flockSize;
	}	

	public int getRule1() {
		return rule1;
	}

	public int getRule2() {
		return rule2;
	}


	public int getRule3() {
		return rule3;
	}

	public int getRule4() {
		return rule4;
	}
	
	public int getRule6() {
		return rule6;
	}

	public int getCENTER_PULL_FACTOR() {
		return CENTER_PULL_FACTOR;
	}

	public int getTARGET_PULL_FACTOR() {
		return TARGET_PULL_FACTOR;
	}

	public int getBOUNCE_ABSORPTION() {
		return BOUNCE_ABSORPTION;
	}

	public int getVELOCITY_PULL_FACTOR() {
		return VELOCITY_PULL_FACTOR;
	}

	public int getMIN_DISTANCE() {
		return MIN_DISTANCE;
	}

	public int getVELOCITY_LIMITER() {
		return VELOCITY_LIMITER;
	}
	
	//----------------Setters-------------------
	public void setRules(int rule1, int rule2, int rule3, int rule4, int rule6)
	{
		this.rule1 = rule1;
		this.rule2 = rule2;
		this.rule3 = rule3;
		this.rule4 = rule4;
		this.rule6 = rule6;
	}

	public void setConsts(int CENTER_PULL_FACTOR, int TARGET_PULL_FACTOR, int BOUNCE_ABSORBTION, int VELOCITY_PULL_FACTOR, int MIN_DISTANCE, int VELOCITY_LIMITER)
	{
		this.CENTER_PULL_FACTOR = CENTER_PULL_FACTOR;
		this.TARGET_PULL_FACTOR = TARGET_PULL_FACTOR;
		this.BOUNCE_ABSORPTION = BOUNCE_ABSORBTION;
		this.VELOCITY_PULL_FACTOR = VELOCITY_PULL_FACTOR;
		this.MIN_DISTANCE = MIN_DISTANCE;
		this.VELOCITY_LIMITER = VELOCITY_LIMITER;
	}
	
	public void setFlockSize(int flockSize) {
		//System.out.println("SOMEONE CHANGED ME TO " + flockSize);
		this.flockSize = flockSize;
	}

	public void setColors(int[] spinnerValues) {
		rType = spinnerValues[0];
		gType = spinnerValues[1];
		bType = spinnerValues[2];
		
		rValue = spinnerValues[3];
		gValue = spinnerValues[4];
		bValue = spinnerValues[5];		
	}

	public int[] getColors() {
		int[] colors = {rType, gType, bType, rValue, gValue, bValue};
		return colors;
	}
}
