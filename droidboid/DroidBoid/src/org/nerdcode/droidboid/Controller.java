package org.nerdcode.droidboid;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class Controller {
	
	//====================================================================================
	//====================================================================================
	//-------------------------------Variable Declerations--------------------------------
	//====================================================================================
	//====================================================================================
	
	//OpenGl stuff
	private int[] textures = new int[1];	//Stores the PNG
	private float zoom = -10.0f;			//Distance Away From Boids
	
	//Used for touch control
	private static Vector2D[] contactPointsAbsolute;	//The relative X,Y positions of touch events
	private static int TOUCH_TICK_LENGTH = 8;	//How many frames a touch lasts for.
	private static int touchCounter = TOUCH_TICK_LENGTH;	//Sets the initial counter.
	
	//Size of the visible world
	private static float BOX_WIDTH;// = 14.61f;
	private static float BOX_HEIGHT;// = 24.85f;

	static int NUM_BOIDS; 					//Number of Boids to create
	static Boid[] flock;								//The flock of the Boids
	
	
	private QuadTree quadFlock;
	private int boidsToSubdivideAt = 5;
	private int maximumDepth = 3;
	
	//----------World Constants---------
	static int CENTER_PULL_FACTOR = 0;//80;			//How powerful the pull of the Center of the flock is for Rule 1
	static int TARGET_PULL_FACTOR = 0;//80;			//How powerful the mouse is at directing the Boids for Rule 4
	static float BOUNCE_ABSORPTION = 0;//.5f;		//How much speed is left when you bounce off a wall
	static int VELOCITY_PULL_FACTOR = 0;//80;			//How powerful the speed matching with the flock is
	static int MIN_DISTANCE = 0;//10;				//The minimum distance to be held between Boids for Rule 2
	static float VELOCITY_LIMITER = 0;//1.5f;		//Limits the maximum velocity of the Boid
	
	//-----------Deprecated Constants-----------
	//final int NUM_OBSTICLES = 15; 				//Number of Obstacles to create
	//final int NUM_PREDATORS = 10; 				//Number of Predators to create
	//final int OBSTICLE_DISTANCE = 15;			//How 'repulsive' regular Obstacles are
	//final int PREDATOR_DISTANCE = 30;			//How 'repulsive' Predator Obstacles are

	// Damping variables affect the scale to which the rule takes an effect against the overall result of the rules
	static private float r1Damping = 0.0f; 						//Rule 1: Boids try to fly towards the centre of mass of neighbouring Boids. 
	static private float r2Damping = 0.0f;						//Rule 2: Boids try to keep a small distance away from other objects (including other Boids). 
	static private float r3Damping = 0.0f;						//Rule 3: Boids try to match velocity with near Boids. 
	static private float r4Damping = 0.0f;						//Rule 4: Boids move towards the mouse
	static private float r6Damping = 0.0f;
	//static private float r5Damping = 1.0f;					//Rule 5: Avoid Obstacles
	
	//Accelerometer
	static private float accelx = 0.0f;
	static private float accely = 0.0f;
	
	//Colors options
	static int[] colorOptions;
	
	
	static Random rand;
	
	
	//====================================================================================
	//====================================================================================
	//--------------------------------Constructor-----------------------------------------
	//====================================================================================
	//====================================================================================
	
	/**
	 * Constructor
	 * @param numBoids How many boids to create
	 * @param WINDOW_WIDTH The width of the screen
	 * @param WINDOW_HEIGHT The height of the screen
	 */
	public Controller(int numBoids, int WINDOW_WIDTH, int WINDOW_HEIGHT)
	{
		NUM_BOIDS = numBoids;
		
		flock = new Boid[NUM_BOIDS];
		rand = new Random();
		BOX_HEIGHT = WINDOW_HEIGHT;
		BOX_WIDTH = WINDOW_WIDTH;
		
		for(int i=0; i<NUM_BOIDS; i++)						//Creates the flock of Boids with random x,y and random vx, vy velocities
		{			
			flock[i] = new Boid();
			//Random spawn
			flock[i].xpos = rand.nextFloat() * BOX_WIDTH;
			flock[i].ypos = rand.nextFloat() * BOX_HEIGHT;
			
			flock[i].vx = (float) (rand.nextFloat() - 0.5f);
			flock[i].vy = (float) (rand.nextFloat() - 0.5f);
		}
		
		//TODO: Implement the Quad Tree Rules
		quadFlock = new QuadTree(BOX_WIDTH, BOX_HEIGHT, boidsToSubdivideAt, maximumDepth);
		for(Boid b : flock)
			quadFlock.addBoid(b);
		
		System.out.println("===============================================");
		System.out.println("==========initial quad tree====================");
		System.out.println("===============================================");
		quadFlock.printQuadTree();
		//===========Convert the int[] flock into a quad=====================
		/*
		long startTime = System.currentTimeMillis();
		quadFlock = new QuadTree(BOX_WIDTH, BOX_HEIGHT, 2, 5);
		for(Boid b : flock)
			quadFlock.addBoid(b);
		long endTime = System.currentTimeMillis();
		long inMS = endTime - startTime;
		System.out.println("Quad tree created in " + inMS + " ms with a max depth of " + quadFlock.getTreeDepth() + " and " + quadFlock.size() + " items");
		//quadFlock.printQuadTree();
		*/
		
		/*
		startTime = System.currentTimeMillis();
		Vector<Boid> flock = quadFlock.getFlock();
		endTime = System.currentTimeMillis();

		System.out.println("Flock Size: " + flock.size());
		for(Boid b : flock)
		{
			System.out.println("Got Boid " + b);
		}		
		inMS = endTime - startTime;
		System.out.println("Took " + inMS + " ms");
		*/
	}
	
	//====================================================================================
	//====================================================================================
	//-------------------------------------Draw-------------------------------------------
	//====================================================================================
	//====================================================================================
	
	public void draw(GL10 gl) {
		//Bind the boid texture for all boids
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
				
		for(int loop = 0; loop < NUM_BOIDS; loop++) {
			Boid currentBoid = flock[loop];			
			gl.glLoadIdentity();							//Reset The Current Modelview Matrix
			gl.glTranslatef(0.0f, 0.0f, zoom); 				//Zoom Into The Screen (Using The Value In 'zoom')			
			gl.glTranslatef(currentBoid.xpos, currentBoid.ypos, 0.0f);	//Translate to the boids position		
			
			//Calculate the angle the image should be pointing in
			//float angle = (float) Math.atan2(currentBoid.vy, currentBoid.vx);			
			//gl.glRotatef(180, 0, 0, 1);			//Correct the images orientation
			//gl.glRotatef(angle * 180, 0, 0, 1);	//Apply the movement orientation to the corrected image
			
			currentBoid.draw(gl);
		}
	}

	//====================================================================================
	//====================================================================================
	//--------------------------------Update Functions------------------------------------
	//====================================================================================
	//====================================================================================
	
	public void updateTouch(Vector2D[] contactPointsInput)
	{
		contactPointsAbsolute = contactPointsInput;
		touchCounter = TOUCH_TICK_LENGTH;
		

		/*
		
		if(contactPointsInput.length > 1)
		{
			System.out.println("=================================================================");
			System.out.println("=======================Touch event quad tree=====================");
			System.out.println("=================================================================");
			quadFlock.printQuadTree();
		}
		*/
	}

	public void updateAll() {
		//TODO: Convert this over to the GameTimer system
		//Decrement the touch counter
		if(contactPointsAbsolute != null)	//No point in decrementing the counter if there's no valid touch information anyway
		{
			touchCounter--;
			if(touchCounter <= 0)
			{
				//When the counter hits zero, erase the previous touch information and reset the counter for its next use
				contactPointsAbsolute = null;
				touchCounter = TOUCH_TICK_LENGTH;
			}
		}
		
		//TODO: This is utterly horrible.
		quadFlock = new QuadTree(BOX_WIDTH, BOX_HEIGHT, boidsToSubdivideAt, maximumDepth);
		for(Boid b : flock)
			quadFlock.addBoid(b);
		
		//TODO: Rewrite this section using the quad tree
		
		//Rules:
		//			1 - Flock - n^2 - Code A
		//			2 - Distance - area(quad) - Code B
		//			3 - Velocity - n^2 - Code A
		//			4 - Touch - n * t - Code C
		//			5 - Obsticles - n * o - area(quad) - Code B
		//			6 - Accel - n - Code C
		
		//Code A functions are applied based on the average of all the boids.
		//Code B functions are done based on distance to other objects
		//Code C functions are done based on simple events with the same input to all boids
		
		
		/*
		 * Option 1:
		 * 				The easy way. Do Code A first.
		 */

		try
		{
			//System.out.println("Tree contains " + quadFlock.getFlock().size());
			//Attempt at 'Way 0: Flock everything at once'
			applyCodeATo(quadFlock.getFlock());
			
			//My attempt at 'Way 1: Flock on Leaves only"
			
			for(Vector<Boid> localFlock : quadFlock.getLeafFlocks())
			{
				//System.out.println("Applying Code A to a flock of " + localFlock.size());
				//applyCodeATo(localFlock);
				applyCodeBTo(localFlock);
			}
			
			for(Boid currentBoid : flock)
			{
				currentBoid.updateWithVector(getRuleVector(currentBoid));
				
				currentBoid = constrainBounds(currentBoid);
				currentBoid = limitVelocity(currentBoid);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Code A rules are based on the average of the flock
	 * @param flock A flock of Boids to perform averaging on
	 */
	public void applyCodeATo(Vector<Boid> flock)
	{
		//What are we doing here?
		//We're getting the sum of all the xpos, ypos and vx, vy to simplify the averaging process
		ruleReturnStruct codeAResult = new ruleReturnStruct();
		for(Boid b : flock)
		{
			codeAResult.sumPosition.x += b.xpos;
			codeAResult.sumPosition.y += b.ypos;
			
			codeAResult.sumVelocity.x += b.vx;
			codeAResult.sumVelocity.y += b.vy;
			
			codeAResult.countItems++;
		}
		
		//System.out.println("Counted " + codeAResult.countItems + " items");
		
		//Then for each Boid we want to apply this as both Rules 1 and 3		
		for(Boid b : flock)
		{
			//TODO: Write methods that take in a codeAResult and uses it to apply rules 1 and 2 to the Boids in the flock
			//float averageXpos = rule1Result
			
			if(codeAResult.countItems > 1)
			{
				//=====Calculate Local Averages========
				Vector2D myAveragePosition = new Vector2D();
				myAveragePosition.x = (codeAResult.sumPosition.x - b.xpos) / (codeAResult.countItems - 1);
				myAveragePosition.y = (codeAResult.sumPosition.y - b.ypos) / (codeAResult.countItems - 1);
				
				Vector2D myAverageVelocity = new Vector2D();
				myAverageVelocity.x = (codeAResult.sumVelocity.x - b.vx) / (codeAResult.countItems - 1);
				myAverageVelocity.y = (codeAResult.sumVelocity.y - b.vy) / (codeAResult.countItems - 1);
				//System.out.println("Calculated Local Average as " + myAveragePosition.x + ", " + myAveragePosition.y);
				//=======Calculate Local Results======
				Vector2D positionVector = new Vector2D();
				positionVector.x = (myAveragePosition.x - b.xpos) / CENTER_PULL_FACTOR;
				positionVector.y = (myAveragePosition.y - b.ypos) / CENTER_PULL_FACTOR;
				
				Vector2D velocityVector = new Vector2D();
				velocityVector.x = (myAverageVelocity.x - b.vx) / VELOCITY_PULL_FACTOR;
				velocityVector.y = (myAverageVelocity.y - b.vy) / VELOCITY_PULL_FACTOR;
				
				//=======Apply Some Results========
				Vector2D resultantVector = new Vector2D();
				resultantVector.x = positionVector.x * r1Damping + velocityVector.x * r3Damping;
				resultantVector.y = positionVector.y * r1Damping + velocityVector.y * r3Damping;
				//System.out.println("Resultant Vector: " + resultantVector.x + ", " + resultantVector.y);
				//======Apply to boid========
				//System.out.println("==================Resultant Vector " + resultantVector);
				b.updateWithVector(resultantVector);
			}
		}
	}
	
	public void applyCodeBTo(Vector<Boid> flock)
	{
		for(Boid currentBoid : flock)
		{
			Vector2D resultantVector = new Vector2D();
			for(Boid otherBoid : flock)
			{
				if(currentBoid != otherBoid)
				{
					if (dist(currentBoid.xpos, currentBoid.ypos, otherBoid.xpos, otherBoid.ypos) < MIN_DISTANCE)		//If the distance to Boid[i] < Min_Distance
					{															//Move double the distance to the Boid, away from the Boid
						resultantVector.x -= otherBoid.xpos - currentBoid.xpos;								
						resultantVector.y -= otherBoid.ypos - currentBoid.ypos;	
					}
				}
			}
			
			resultantVector.x *= r2Damping;
			resultantVector.y *= r2Damping;
			currentBoid.updateWithVector(resultantVector);
		}
	}
	
	
	protected class ruleReturnStruct
	{
		protected Vector2D sumPosition;
		protected Vector2D sumVelocity;
		protected int countItems;
		
		protected ruleReturnStruct()
		{
			sumPosition = new Vector2D();
			sumVelocity = new Vector2D();
		}
	}
	
	public static Vector2D getRuleVector(Boid currentBoid)
	{
		Vector2D v1 = new Vector2D();
		Vector2D v2 = new Vector2D();
		Vector2D v3 = new Vector2D(); 
		Vector2D v4 = new Vector2D();
		Vector2D v6 = new Vector2D();
		
		//--------------------------------------------Runs all the rules resulting in the following X,Y values-------------------------------------------
		//if(r1Damping >= 0.01 && CENTER_PULL_FACTOR != 0)
		//	v1 = rule1(currentBoid);														//X and Y are coordinates of the Centre of the flock
		//if(r2Damping >= 0.01)
		//	v2 = rule2(currentBoid);														//X and Y are how far along each axis to move to be away from all other Boids
		//if(r3Damping >= 0.01 && VELOCITY_PULL_FACTOR != 0)
		//	v3 = rule3(currentBoid);														//X and Y are how fast on each axis to go based on the flock
		if(r4Damping >= 0.01 && TARGET_PULL_FACTOR != 0)
			v4 = rule4(currentBoid);														//X and Y are the new coordinates based on the Mouse
		if(r6Damping >= 0.01)
			v6 = rule6(currentBoid);														//X and Y are the new coordinates based on the Mouse
		//Rule 5 is the obstacles rule, not necessary at the moment
		//Vector v5 = rule5(currentBoid);														//X and Y are how far to move to be clear of Obstacles

		Vector2D resultantVector = new Vector2D();
		//Add vectors to velocities weighted by their damping values
		resultantVector.x = r1Damping*v1.x + r2Damping*v2.x + r3Damping*v3.x + r4Damping*v4.x + r6Damping*v6.x;// + r5Damping*v5.x;
		resultantVector.y = r1Damping*v1.y + r2Damping*v2.y + r3Damping*v3.y + r4Damping*v4.y + r6Damping*v6.y;//+ r5Damping*v5.y;

		return resultantVector;
	}
	
	//====================================================================================
	//====================================================================================
	//-----------------------------------Setters------------------------------------------
	//====================================================================================
	//====================================================================================
	
	/**
	 * This is the only method for setting the rules in the controller.
	 * @param rules An integer array of values from 0 - 100 for each rule
	 */
	public static void setDampingValues(int[] rules)
	{		
		r1Damping = (float) rules[0] / 100;
		r2Damping = (float) rules[1] / 100;
		r3Damping = (float) rules[2] / 100;
		r4Damping = (float) rules[3] / 100;
		r6Damping = (float) rules[4] / 100;
	}
	
	public static void setConsts(int CENTER_PULL_FACTOR, int TARGET_PULL_FACTOR, int BOUNCE_ABSORBTION, int VELOCITY_PULL_FACTOR, int MIN_DISTANCE, int VELOCITY_LIMITER)
	{
		Controller.CENTER_PULL_FACTOR = CENTER_PULL_FACTOR;
		Controller.TARGET_PULL_FACTOR = TARGET_PULL_FACTOR;
		Controller.BOUNCE_ABSORPTION = (float) BOUNCE_ABSORBTION / 100;
		Controller.VELOCITY_PULL_FACTOR = VELOCITY_PULL_FACTOR;
		Controller.MIN_DISTANCE = MIN_DISTANCE;
		Controller.VELOCITY_LIMITER = (float) VELOCITY_LIMITER / 10;
		//System.out.println("New Consts " + VELOCITY_LIMITER);
	}
	
	public static void setAccelerometer(float x, float y)
	{
		accelx = x;
		accely = y;
		//System.out.println("Recieved update - \t\tx : " +x+ " y : "+y);
	}

	public static void setColors(int[] colors) {
		colorOptions = colors;		
	}	
	
	
	//====================================================================================
	//====================================================================================
	//------------------------------------Helper Functions--------------------------------
	//====================================================================================
	//====================================================================================
	
	/**
	 * Uses pythagorean theorem to get the distance between two points
	 */
	public static float dist(float x1, float y1, float x2, float y2)
	{
		return (float) Math.sqrt( ((x2 - x1) * (x2 - x1)) +	((y2 - y1) * (y2 - y1))	);
	}
	
	public static Vector2D limitVelocity(Vector2D velocityVector)
	{
		float velocity = (float)Math.sqrt((velocityVector.x * velocityVector.x) +(velocityVector.y * velocityVector.y)); 						//Pythagoras theorm, applied from the Velocity on the X axis and the Velocity on the Y axis giving a representation of the overall velocity independent of direction
		if (velocity > VELOCITY_LIMITER)
		{
			velocityVector.x /= velocity * VELOCITY_LIMITER;										//Reduces the X velocity by a factor of the overall velocity, and applies a damping value
			velocityVector.y /= velocity * VELOCITY_LIMITER;										//Reduces the Y velocity by a factor of the overall velocity, and applies a damping value
		}  
		return velocityVector;
	} // end limitVelocity()
	
	public static Boid limitVelocity(Boid currentBoid)
	{
		float velocity = (float)Math.sqrt((currentBoid.vx * currentBoid.vx) +(currentBoid.vy * currentBoid.vy));
		if (velocity > VELOCITY_LIMITER)
		{
			currentBoid.vx /= velocity * VELOCITY_LIMITER;										//Reduces the X velocity by a factor of the overall velocity, and applies a damping value
			currentBoid.vy /= velocity * VELOCITY_LIMITER;										//Reduces the Y velocity by a factor of the overall velocity, and applies a damping value
		}  
		return currentBoid;
	}
	
	public static Boid constrainBounds(Boid currentBoid)
	{
		//TODONE: Implement the bounce mechanic here
		if (currentBoid.xpos + currentBoid.vx < 0 || currentBoid.xpos + currentBoid.vx > BOX_WIDTH)					//Boundary constraint on the X axis
			currentBoid.vx = -currentBoid.vx*BOUNCE_ABSORPTION;

		if (currentBoid.ypos + currentBoid.vy < 0 || currentBoid.ypos + currentBoid.vy > BOX_HEIGHT )					//Boundary constraint on the Y axis
			currentBoid.vy = -currentBoid.vy*BOUNCE_ABSORPTION;
		
		//If beyond screen, respawn
		int respawnBuffer = 2;	//How far beyond the border you have to be to respawn
		if (currentBoid.xpos < (0 - respawnBuffer) || currentBoid.xpos > (BOX_WIDTH + respawnBuffer))
		{
			if(currentBoid.xpos < (0 - respawnBuffer))//Going Off Left
				currentBoid.xpos = 5;
			else if(currentBoid.xpos > (BOX_WIDTH + respawnBuffer))//Going Off Right
				currentBoid.xpos = BOX_WIDTH - 5;
		}
		if (currentBoid.ypos < (0 - respawnBuffer) || currentBoid.ypos > (BOX_HEIGHT + respawnBuffer))
		{
			if(currentBoid.ypos < (0 - respawnBuffer))//Going Off Top
				currentBoid.ypos = 5;
			else if(currentBoid.ypos > (BOX_HEIGHT + respawnBuffer))//Going Off Bottom
				currentBoid.ypos = BOX_HEIGHT - 5;
		}
		return currentBoid;
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
	
	//====================================================================================
	//====================================================================================
	//-------------------------------------RULES------------------------------------------
	//====================================================================================
	//====================================================================================

	public static Vector2D rule1(Boid currentBoid)												//Rule 1: Boids try to fly towards the centre of mass of neighbouring Boids. 
	{
		Vector2D resultantVector = new Vector2D();
		for (Boid boidFlock : flock)
		{
			if (currentBoid != boidFlock) 										//Don't include the current Boid in the average of the Boids position
			{
				resultantVector.x += boidFlock.xpos;
				resultantVector.y += boidFlock.ypos;
			} // end if
		}
		//Resultant Vector now contains the sum of the xPos, yPos of the flock		
		resultantVector.x /= (NUM_BOIDS-1);											//Divides the Sum by the count, resulting in average X value	
		resultantVector.y /= (NUM_BOIDS-1);											//Divides the Sum by the count, resulting in average Y value
		//Resultant Vector now contains the average xPos, yPos of the flock
		resultantVector.x = (resultantVector.x - currentBoid.xpos) / CENTER_PULL_FACTOR;						//Sets the X value to the distance away from the average of the flock with a damper
		resultantVector.y = (resultantVector.y - currentBoid.ypos) / CENTER_PULL_FACTOR;						//Sets the Y value to the distance away from the average of the flock with a damper

		return resultantVector;
	} // end rule1()

	public static Vector2D rule2(Boid currentBoid)												//Rule 2: Boids try to keep a small distance away from other objects (including other Boids). 
	{
		Vector2D resultantVector = new Vector2D();
		for(Boid boidFlock: flock)
		{
			if (currentBoid != boidFlock)											//That isn't itself
			{
				if (dist(currentBoid.xpos, currentBoid.ypos, boidFlock.xpos, boidFlock.ypos) < MIN_DISTANCE)		//If the distance to Boid[i] < Min_Distance
				{															//Move double the distance to the Boid, away from the Boid
					resultantVector.x -= boidFlock.xpos - currentBoid.xpos;								
					resultantVector.y -= boidFlock.ypos - currentBoid.ypos;	
				} // end if
			}
		}
		
		return resultantVector;
	} // end rule2()

	public static Vector2D rule3(Boid currentBoid)												//Rule 3: Boids try to match velocity with near Boids.
	{
		Vector2D resultantVector = new Vector2D();
		for (Boid boidFlock : flock)
		{
			if (currentBoid != boidFlock)											
			{								
				resultantVector.x +=  boidFlock.vx;									
				resultantVector.y +=  boidFlock.vy;									
			}
		}
		
		//Average the Sum of the vx and vy
		resultantVector.x /= (NUM_BOIDS - 1);
		resultantVector.y /= (NUM_BOIDS - 1);
		
		resultantVector.x = (resultantVector.x - currentBoid.vx)/VELOCITY_PULL_FACTOR;						//Current vx is the average vx minus the current vx with a damper
		resultantVector.y = (resultantVector.y - currentBoid.vy)/VELOCITY_PULL_FACTOR;						//Current vy is the average vy minus the current vy with a damper
		
		return resultantVector;
	} // end rule3()

	public static Vector2D rule4(Boid currentBoid)												//Rule 4: Boids move towards the mouse
	{
		//This rule states that the boids will be attracted to touch events
		Vector2D resultantVector = new Vector2D();
		
		if(contactPointsAbsolute != null)
		{
			Vector2D closestPoint = null;
			float closestPointDist = Float.MAX_VALUE;
			for(Vector2D touchPoint : contactPointsAbsolute)
			{
				float distToPoint = dist(currentBoid.xpos, currentBoid.ypos, touchPoint.x, touchPoint.y);
				if(distToPoint < closestPointDist)
				{
					closestPointDist = distToPoint;
					closestPoint = touchPoint;
				}
			}
			resultantVector.x = (closestPoint.x - currentBoid.xpos) / TARGET_PULL_FACTOR;
			resultantVector.y = (closestPoint.y - currentBoid.ypos) / TARGET_PULL_FACTOR;
		}		
		return resultantVector;
	} // end rule4()
	
	public static Vector2D rule6(Boid currentBoid)
	{
		Vector2D resultantVector = new Vector2D();
		//This method will be follow the accelerometer
		//Lets assume gravity is 0, 2, so thats down on the Y axis
		resultantVector.x = -accelx/10;
		resultantVector.y = accely/10;
		return resultantVector;
	}
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

	public void drawQuadTree(GL10 gl) {
		if(quadFlock != null)
		{
			quadFlock.drawQuadTree(gl);
		}		
	}
}
