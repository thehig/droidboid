package org.nerdcode.droidboid;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class Controller {
	
	// ************************* DRAWING VARIABLES **************************  

	//OpenGl stuff
	private int[] textures = new int[1];
	private float zoom = -30.0f;			//Distance Away From Boids
	private float tilt = 90.0f;				//Tilt The View(Display viewport
	private float spin;						//Spin Stars
	
	private static float lastTouchX = 0;
	private static float lastTouchY = 0;
	private static int TOUCH_TICK_LENGTH = 30;	//How many frames a touch lasts for.
	private static int touchCounter = TOUCH_TICK_LENGTH;
	
	private static float BOX_WIDTH = 14.61f;
	private static float BOX_HEIGHT = 24.85f;

	static int NUM_BOIDS; 					//Number of Boids to create
	//final int NUM_OBSTICLES = 15; 				//Number of Obstacles to create
	//final int NUM_PREDATORS = 10; 				//Number of Predators to create

	final static int CENTER_PULL_FACTOR = 80;			//How powerful the pull of the Center of the flock is for Rule 1
	final static int TARGET_PULL_FACTOR = 80;			//How powerful the mouse is at directing the Boids for Rule 4
	//final int OBSTICLE_DISTANCE = 15;			//How 'repulsive' regular Obstacles are
	//final int PREDATOR_DISTANCE = 30;			//How 'repulsive' Predator Obstacles are
	final static float BOUNCE_ABSORPTION = .75f;		//Something to do with the boundary conditions. Probably for smoothing the bouncing on the walls
	final static int VELOCITY_PULL_FACTOR = 20;			//How powerful the speed matching with the flock is
	final static int MIN_DISTANCE = 1;				//The minimum distance to be held between Boids for Rule 2
	final static float VELOCITY_LIMITER = 0.15f;		//Limits the maximum velocity of the Boid

	// Damping variables affect the scale to which the rule takes an effect against the overall result of the rules

	static float r1Damping = .5f; 						//Rule 1: Boids try to fly towards the centre of mass of neighbouring Boids. 
	static float r2Damping = 0.3f;						//Rule 2: Boids try to keep a small distance away from other objects (including other Boids). 
	static float r3Damping = 0.8f;						//Rule 3: Boids try to match velocity with near Boids. 
	static float r4Damping = 1.0f;						//Rule 4: Boids move towards the mouse
	static float r5Damping = 1.0f;						//Rule 5: Avoid Obstacles

	/*
	Boid[] flock = new Boid[NUM_BOIDS];
	Obsticle[] obstacles = new Obsticle[NUM_OBSTICLES+NUM_PREDATORS];
	 */

	static Boid[] flock;
	
	public Controller(int numBoids, int WINDOW_WIDTH, int WINDOW_HEIGHT)
	{
		NUM_BOIDS = numBoids;
		
		flock = new Boid[NUM_BOIDS];
		Random rand = new Random();
		CalculateBoundingBox(45.f, WINDOW_WIDTH, WINDOW_HEIGHT);
		for(int i=0; i<NUM_BOIDS; i++)						//Creates the flock of Boids with random x,y and random vx, vy velocities
		{			
			flock[i] = new Boid();
			//TODO: Get the width and height parameters then set up the random positions of the Boids
			flock[i].xpos = (rand.nextFloat()-0.5f)* BOX_HEIGHT;
			flock[i].ypos = (rand.nextFloat()-0.5f)* BOX_WIDTH;

			flock[i].vx = (float) Math.random() -0.5f;
			flock[i].vy = (float) Math.random() -0.5f;
		} // end for
	}

	//TODONE: Put this bit into the Main event handler bit from lesson9
		
	//TODONE: Here begins the controller. Make this shit all static and awesome and full of win
	public static Vector updateBoid(Boid currentBoid)
	{
		float vx = currentBoid.vx;
		float vy = currentBoid.vy;
		
		//TODONE: Make the rules static
		//Runs all the rules resulting in the following X,Y values
		Vector v1 = rule1(currentBoid);														//X and Y are coordinates of the Centre of the flock
		Vector v2 = rule2(currentBoid);														//X and Y are how far along each axis to move to be away from all other Boids
		Vector v3 = rule3(currentBoid);														//X and Y are how fast on each axis to go based on the flock
		//Rule 4 is the mouse, not relevant at the moment
		Vector v4 = rule4(currentBoid);														//X and Y are the new coordinates based on the Mouse
		//Rule 5 is the obsticles rule, not necessary at the moment
		//Vector v5 = rule5(currentBoid);														//X and Y are how far to move to be clear of Obstacles
		//v1 - v5 now contain X and Y variables

		Vector resultantVector = new Vector();
		//Add vectors to velocities weighted by their damping values
		//TODONE: Apply the rules to the boids position and velocity
		resultantVector.x += r1Damping*v1.x + r2Damping*v2.x + r3Damping*v3.x + r4Damping*v4.x;// + r5Damping*v5.x;
		resultantVector.y += r1Damping*v1.y + r2Damping*v2.y + r3Damping*v3.y + r4Damping*v4.y;//+ r5Damping*v5.y;

		resultantVector = limitVelocity(resultantVector);												//Prevents the Boid from moving too fast

		//TODO: Implement the bounce mechanic here
		if (currentBoid.xpos + resultantVector.x < (-BOX_WIDTH / 2) || currentBoid.xpos + resultantVector.x > (BOX_WIDTH / 2))					//Boundary constraint on the X axis
			resultantVector.x = -resultantVector.x*BOUNCE_ABSORPTION;

		if (currentBoid.ypos + resultantVector.y < (-BOX_HEIGHT/2) || currentBoid.ypos + resultantVector.y > (BOX_HEIGHT / 2))					//Boundary constraint on the Y axis
			resultantVector.y = -resultantVector.y*BOUNCE_ABSORPTION;
			
		//Update new Boid position with previously calculated velocities
		//xpos += vx;
		//ypos += vy;

		return resultantVector;
	}
	
	public static Vector limitVelocity(Vector velocityVector)
	{
		float vlim = VELOCITY_LIMITER;									

		float velocity = (float)Math.sqrt((velocityVector.x * velocityVector.x) +(velocityVector.y * velocityVector.y)); 						//Pythagoras theorm, applied from the Velocity on the X axis and the Velocity on the Y axis giving a representation of the overall velocity independent of direction

		if (velocity > vlim)
		{
			velocityVector.x = (velocityVector.x/velocity)*vlim;										//Reduces the X velocity by a factor of the overall velocity, and applies a damping value
			velocityVector.y = (velocityVector.y/velocity)*vlim;										//Reduces the Y velocity by a factor of the overall velocity, and applies a damping value
		}  
		return velocityVector;

	} // end limitVelocity()

	public static Vector rule1(Boid currentBoid)												//Rule 1: Boids try to fly towards the centre of mass of neighbouring Boids. 
	{
		Vector resultantVector = new Vector();
		for (int i=0; i < NUM_BOIDS; ++i)								//Sums the x and y values of the flock
		{
			if (currentBoid != flock[i]) 										//Don't include the current Boid in the average of the Boids position
			{
				resultantVector.x += flock[i].xpos;
				resultantVector.y += flock[i].ypos;
			} // end if
		} // end for

		resultantVector.x /= (NUM_BOIDS-1);											//Divides the Sum by the count, resulting in average X value	
		resultantVector.y /= (NUM_BOIDS-1);											//Divides the Sum by the count, resulting in average Y value

		resultantVector.x = (resultantVector.x - currentBoid.xpos) / CENTER_PULL_FACTOR;						//Sets the X value to the distance away from the average of the flock with a damper
		resultantVector.y = (resultantVector.y - currentBoid.ypos) / CENTER_PULL_FACTOR;						//Sets the Y value to the distance away from the average of the flock with a damper
		
		return resultantVector;
	} // end rule1()

	public static Vector rule2(Boid currentBoid)												//Rule 2: Boids try to keep a small distance away from other objects (including other Boids). 
	{
		Vector resultantVector = new Vector();
		resultantVector.setXY(0,0);
		for (int i=0; i < NUM_BOIDS; ++i)								//For every Boid
		{
			if (currentBoid != flock[i])											//That isn't itself
			{
				if (dist(currentBoid.xpos, currentBoid.ypos, flock[i].xpos, flock[i].ypos) < MIN_DISTANCE)		//If the distance to Boid[i] < Min_Distance
				{															//Move double the distance to the Boid, away from the Boid
					resultantVector.x -= flock[i].xpos - currentBoid.xpos;								
					resultantVector.y -= flock[i].ypos - currentBoid.ypos;	
				} // end if
			} // end if
		} // end for
		return resultantVector;
	} // end rule2()

	public static Vector rule3(Boid currentBoid)												//Rule 3: Boids try to match velocity with near Boids.
	{
		Vector resultantVector = new Vector();
		for (int i=0; i < NUM_BOIDS; ++i)								//Get the sum of all the vx and vy except the current Boid
		{
			if (currentBoid != flock[i])											
			{								
				resultantVector.x +=  flock[i].vx;									
				resultantVector.y +=  flock[i].vy;									
			} // end if
		} // end for
		//Average the Sum of the vx and vy
		resultantVector.x /= (NUM_BOIDS - 1);
		resultantVector.y /= (NUM_BOIDS - 1);

		resultantVector.x = (resultantVector.x - currentBoid.vx)/VELOCITY_PULL_FACTOR;						//Current vx is the average vx minus the current vx with a damper
		resultantVector.y = (resultantVector.y - currentBoid.vy)/VELOCITY_PULL_FACTOR;						//Current vy is the average vy minus the current vy with a damper
		
		return resultantVector;
	} // end rule3()

	public static Vector rule4(Boid currentBoid)												//Rule 4: Boids move towards the mouse
	{
		Vector resultantVector = new Vector();
		resultantVector.setXY(0,0);
		//TODO: Add mouseX, mouseY information in here somehow
		
		if(lastTouchX != 0 && lastTouchY != 0)
		{
			float worldPositionX = BOX_WIDTH * lastTouchX - (BOX_WIDTH/2);
			float worldPositionY = -(BOX_HEIGHT * lastTouchY - (BOX_HEIGHT/2));
			//System.out.println("X "+lastTouchX + " Y " + lastTouchY);
			resultantVector.setXY(0,0);
			//TODONE: Add mouseX, mouseY information in here somehow
			resultantVector.x = (worldPositionX - currentBoid.xpos) / TARGET_PULL_FACTOR;
			resultantVector.y = (worldPositionY - currentBoid.ypos) / TARGET_PULL_FACTOR;
			
			//TODONE: Add in some kind of timer so that they don't just follow the thing forever.
		}		
		return resultantVector;
	} // end rule4()
	
	/*
	public static Vector rule5(Boid currentBoid) 												//Rule 5: Avoid Obstacles
	{
		Vector resultantVector = new Vector();
		resultantVector.setXY(0,0);
		for (int i=0; i < obstacles.length; ++i)						//For every OBSTACLE
		{
			if (dist(xpos, ypos, obstacles[i].x, obstacles[i].y) < obstacles[i].minDistance) //If the distance to the next Obstacle is less than Minimum Distance for that Obstacle
			{
				v5.x -= obstacles[i].x - xpos;								//How far to move to be clear
				v5.y -= obstacles[i].y - ypos;
			} // end if
		} // end for

	} //end rule5()
	*/
	
	public static float dist(float x1, float y1, float x2, float y2)
	{
		return (float) Math.sqrt((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1));
	}
	
	public void draw(GL10 gl) {
		//Bind the star texture for all stars
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
				
		//Iterate through all stars
		for(int loop = 0; loop < NUM_BOIDS; loop++) {
			//Recover the current star into an object
			Boid currentBoid = flock[loop];			
			gl.glLoadIdentity();							//Reset The Current Modelview Matrix
			gl.glTranslatef(0.0f, 0.0f, zoom); 				//Zoom Into The Screen (Using The Value In 'zoom')			
			gl.glTranslatef(currentBoid.xpos, currentBoid.ypos, 0.0f);			
			
			//Draw
			currentBoid.draw(gl);

		}
	}
	
	public void loadGLTexture(GL10 gl, Context context) {
		//Get the texture from the Android resource directory
		//Can open png, jpg and bmp 
		InputStream is = context.getResources().openRawResource(R.drawable.boid);
		Bitmap bitmap = null;
		try {
			//BitmapFactory is an Android graphics utility for images
			bitmap = BitmapFactory.decodeStream(is);

		} finally {
			//Always clear and close
			try {
				is.close();
				is = null;
			} catch (IOException e) {
			}
		}

		//Generate there texture pointer
		gl.glGenTextures(1, textures, 0);

		//Create Linear Filtered Texture and bind it to texture
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		//Clean up
		bitmap.recycle();
	}
	
	public void CalculateBoundingBox(float fov, int width, int height)
	{
		BOX_HEIGHT = (float)Math.tan(fov/2)*Math.abs(zoom)*2;
		float aspect = height/width;
		
		BOX_WIDTH = BOX_HEIGHT/aspect;		
	}
	
	public void updateTouch(float touchX, float touchY)
	{
		lastTouchX = touchX;
		lastTouchY = touchY;
		touchCounter = TOUCH_TICK_LENGTH;
	}

	public void updateAll() {
		touchCounter--;
		if(touchCounter == 0)
		{
			lastTouchX = 0;
			lastTouchY = 0;
			touchCounter = TOUCH_TICK_LENGTH;
		}
		
		for(Boid currentBoid : flock)
		{
			Vector movementVector = updateBoid(currentBoid);
			currentBoid.vx = movementVector.x;
			currentBoid.vy = movementVector.y;
			
			currentBoid.xpos += currentBoid.vx;
			currentBoid.ypos += currentBoid.vy;
		}		
	}
	
}
