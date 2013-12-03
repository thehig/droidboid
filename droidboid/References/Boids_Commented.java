/*
 * Original code found at http://processing.org/discourse/yabb_beta/YaBB.cgi?board=Syntax;action=display;num=1193175897
 * Commenting based on http://www.vergenet.net/~conrad/Boids/pseudocode.html
 * 
 * Comments added by David Higgins - hig 'at' itnet 'dot' ie
 */

import processing.core.PApplet;
import processing.core.PFont;


@SuppressWarnings("serial")
public class Boids_Commented extends PApplet
{
	// ************************* DRAWING VARIABLES **************************  
	final int WINDOW_WIDTH  = 500;
	final int WINDOW_HEIGHT =  500;
	final int FRAME_RATE = 100;

	
	final int NUM_BOIDS = 200; 					//Number of Boids to create
	final int NUM_OBSTICLES = 15; 				//Number of Obstacles to create
	final int NUM_PREDATORS = 10; 				//Number of Predators to create
	
	final int CENTER_PULL_FACTOR = 800;			//How powerful the pull of the Center of the flock is for Rule 1
	final int TARGET_PULL_FACTOR = 800;			//How powerful the mouse is at directing the Boids for Rule 4
	final int OBSTICLE_DISTANCE = 15;			//How 'repulsive' regular Obstacles are
	final int PREDATOR_DISTANCE = 30;			//How 'repulsive' Predator Obstacles are
	final float BOUNCE_ABSORPTION = .75f;		//Something to do with the boundary conditions. Probably for smoothing the bouncing on the walls
	final int VELOCITY_PULL_FACTOR = 8;			//How powerful the speed matching with the flock is
	final int MIN_DISTANCE = 18;				//The minimum distance to be held between Boids for Rule 2
	final float VELOCITY_LIMITER = 2.0f;		//Limits the maximum velocity of the Boid

												// Damping variables affect the scale to which the rule takes an effect against the overall result of the rules

	float r1Damping = .5f; 						//Rule 1: Boids try to fly towards the centre of mass of neighbouring Boids. 
	float r2Damping = 0.1f;						//Rule 2: Boids try to keep a small distance away from other objects (including other Boids). 
	float r3Damping = 0.5f;						//Rule 3: Boids try to match velocity with near Boids. 
	float r4Damping = 1.0f;						//Rule 4: Boids move towards the mouse
	float r5Damping = 1.0f;						//Rule 5: Avoid Obstacles

	boolean showObstacles = true;

	final int backColor = 80;

	PFont font;

	Boid[] flock = new Boid[NUM_BOIDS];
	Obsticle[] obstacles = new Obsticle[NUM_OBSTICLES+NUM_PREDATORS];



	public void setup()  
	{
	  //size(500, 500);  									//Authors code. Used for embedding only
	  size(WINDOW_WIDTH, WINDOW_HEIGHT, P3D);				//Should be openGL, but I'm lazy
	  
	  rectMode(CENTER);
	  ellipseMode(CENTER);
	  randomSeed((int)random(1,1000));						//Seeds random with a random. Yeah. Random

	  font = createFont("Courier", 12);

	  for(int i=0; i<NUM_BOIDS; i++)						//Creates the flock of Boids with random x,y and random vx, vy velocities
	  {
	    flock[i] = new Boid();
	    flock[i].xpos = width/2+random(-300, 300);
	    flock[i].ypos = height/2+random(-300, 300);

	    flock[i].vx = random(-.5f, .5f);
	    flock[i].vy = random(-.5f, .5f);

	  } // end for

	  for (int i=0; i<NUM_OBSTICLES; i++) 					//Adds the obstacles to the obstacles array
	    obstacles[i] = new Obsticle();
	  

	  for (int i=0; i<NUM_PREDATORS; i++)					//Adds the predators into the obstacles array
	    obstacles[NUM_OBSTICLES+i] = new Predator();
	  
	  frameRate(FRAME_RATE);
	  //smooth();											//Doesn't work with P3D
	}


	public void draw()  
	{    
	  background(backColor);
	  
	  for(Boid currentBoid : flock)							//Updates and then draws the flock
	  {
		  currentBoid.updateBoid();
		  currentBoid.drawMe();
	  }

	  if (showObstacles) 									//Updates and draws the obstacles. Toggleable
		  for(Obsticle currentObsticle : obstacles)
			  currentObsticle.draw();
	  
	  fill(150);
	  textFont(font);
	  text("FPS: " + frameRate, 10, height-12);
	}

	public void keyPressed() {

	  // switch mouse attraction on/off
	  if (key== ' ' ) {										//Toggles Rule 4: Boids move towards the mouse
	    if (r4Damping==0.0) {
	      println("mouse on");
	      r4Damping = 1.0f;
	    } 
	    else {
	      println("mouse off");
	      r4Damping = 0.0f; 
	    }
	  } 
	  else if (key == 'o') {								//Toggles the Obstacles
	    if (!showObstacles) {
	      println("obstacles on");
	      showObstacles = true;
	      r5Damping = 1.0f;
	    } 
	    else {
	      println("obstacles off");
	      showObstacles = false;
	      r5Damping = 0.0f; 
	    }
	  }
	}



	class Vector											//Stores an x and a y value. More like a struct with set/get
	{
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

	class Boid												//Boids go in the flock.
	{
	  final int TRAIL_SCALE = 8;							//The size of the trail on the image
	  														//Each of the following vectors will store the X and Y values of the result of each Rule being run
	  Vector v1 = new Vector();
	  Vector v2 = new Vector();
	  Vector v3 = new Vector();
	  Vector v4 = new Vector();
	  Vector v5 = new Vector();

	  int myColor = color(255-random(255), 200, 155,200);
	  int mySize = 4;

	  float xpos, ypos;										//X and Y positions of the Boid
	  float vx, vy;											//The velocity along the x and y axis. Negative and Positive values possible

	  public void drawMe()
	  {
	    stroke(myColor);
	    strokeWeight(1);

	    line(xpos, ypos, xpos-TRAIL_SCALE*vx, ypos-TRAIL_SCALE*vy);  	//Draws the Boids tail
	    noStroke();
	    fill(myColor);
	    rect(xpos,ypos, mySize, mySize);								//Draws the Boids body
	  }


	  public void updateBoid()
	  {
		  																//Runs all the rules resulting in the following X,Y values
	    rule1();														//X and Y are coordinates of the Centre of the flock
	    rule2();														//X and Y are how far along each axis to move to be away from all other Boids
	    rule3();														//X and Y are how fast on each axis to go based on the flock
	    rule4();														//X and Y are the new coordinates based on the Mouse
	    rule5();														//X and Y are how far to move to be clear of Obstacles
	    																//v1 - v5 now contain X and Y variables
	    																
	    																//Add vectors to velocities weighted by their damping values
	    vx += r1Damping*v1.x + r2Damping*v2.x + r3Damping*v3.x + r4Damping*v4.x + r5Damping*v5.x;
	    vy += r1Damping*v1.y + r2Damping*v2.y + r3Damping*v3.y + r4Damping*v4.y+ r5Damping*v5.y;

	    limitVelocity();												//Prevents the Boid from moving too fast

	    if (xpos + vx < 0 || xpos + vx > WINDOW_WIDTH)					//Boundary constraint on the X axis
	      vx = -vx*BOUNCE_ABSORPTION;

	    if (ypos + vy < 0 || ypos + vy > WINDOW_HEIGHT)					//Boundary constraint on the Y axis
	      vy = -vy*BOUNCE_ABSORPTION;

	    																//Update new Boid position with previously calculated velocities
	    xpos += vx;
	    ypos += vy;

	  }

	  public void limitVelocity()
	  {
	    float vlim = VELOCITY_LIMITER;									

	    float velocity = sqrt(sq(vx) + sq(vy)); 						//Pythagoras theorm, applied from the Velocity on the X axis and the Velocity on the Y axis giving a representation of the overall velocity independent of direction

	    if (velocity > vlim)
	    {
	      vx = (vx/velocity)*vlim;										//Reduces the X velocity by a factor of the overall velocity, and applies a damping value
	      vy = (vy/velocity)*vlim;										//Reduces the Y velocity by a factor of the overall velocity, and applies a damping value
	    }  

	  } // end limitVelocity()


	  public void rule1()												//Rule 1: Boids try to fly towards the centre of mass of neighbouring Boids. 
	  {
	    for (int i=0; i < NUM_BOIDS; ++i)								//Sums the x and y values of the flock
	    {
	      if (this != flock[i]) 										//Don't include the current Boid in the average of the Boids position
	      {
	        v1.x += flock[i].xpos;
	        v1.y += flock[i].ypos;
	      } // end if
	    } // end for

	    v1.x /= (NUM_BOIDS-1);											//Divides the Sum by the count, resulting in average X value	
	    v1.y /= (NUM_BOIDS-1);											//Divides the Sum by the count, resulting in average Y value

	    v1.x = (v1.x - xpos) / CENTER_PULL_FACTOR;						//Sets the X value to the distance away from the average of the flock with a damper
	    v1.y = (v1.y - ypos) / CENTER_PULL_FACTOR;						//Sets the Y value to the distance away from the average of the flock with a damper

	  } // end rule1()

	  public void rule2()												//Rule 2: Boids try to keep a small distance away from other objects (including other Boids). 
	  {
	    v2.setXY(0,0);
	    for (int i=0; i < NUM_BOIDS; ++i)								//For every Boid
	    {
	      if (this != flock[i])											//That isn't itself
	      {
	        if (dist(xpos, ypos, flock[i].xpos, flock[i].ypos) < MIN_DISTANCE)		//If the distance to Boid[i] < Min_Distance
	        {															//Move double the distance to the Boid, away from the Boid
	          v2.x -= flock[i].xpos - xpos;								
	          v2.y -= flock[i].ypos - ypos;	
	        } // end if
	      } // end if
	    } // end for

	  } // end rule2()


	  public void rule3()												//Rule 3: Boids try to match velocity with near Boids.
	  {
	    for (int i=0; i < NUM_BOIDS; ++i)								//Get the sum of all the vx and vy except the current Boid
	    {
	      if (this != flock[i])											
	      {								
	        v3.x +=  flock[i].vx;									
	        v3.y +=  flock[i].vy;									
	      } // end if
	    } // end for
	    																//Average the Sum of the vx and vy
	    v3.x /= (NUM_BOIDS - 1);
	    v3.y /= (NUM_BOIDS - 1);

	    v3.x = (v3.x - vx)/VELOCITY_PULL_FACTOR;						//Current vx is the average vx minus the current vx with a damper
	    v3.y = (v3.y - vy)/VELOCITY_PULL_FACTOR;						//Current vy is the average vy minus the current vy with a damper
	  } // end rule3()

	  public void rule4()												//Rule 4: Boids move towards the mouse
	  {
	    v4.setXY(0,0);
	    v4.x = (mouseX - xpos) / TARGET_PULL_FACTOR;
	    v4.y = (mouseY - ypos) / TARGET_PULL_FACTOR;
	  } // end rule4()

	  public void rule5() 												//Rule 5: Avoid Obstacles
	  {
	    v5.setXY(0,0);
	    for (int i=0; i < obstacles.length; ++i)						//For every OBSTACLE
	    {
	      if (dist(xpos, ypos, obstacles[i].x, obstacles[i].y) < obstacles[i].minDistance) //If the distance to the next Obstacle is less than Minimum Distance for that Obstacle
	      {
	        v5.x -= obstacles[i].x - xpos;								//How far to move to be clear
	        v5.y -= obstacles[i].y - ypos;
	      } // end if
	    } // end for
	  } //end rule5()
	} // end class Boid


	class Obsticle 														//Obstacles are static
	{
	  public int x, y, minDistance;

	  Obsticle() 														//Give the Obstacles a random position
	  {
	    x = (int) random(width);
	    y = (int) random(height);
	    minDistance = OBSTICLE_DISTANCE; 								//How far the Boids must stay away
	  } 

	  public void draw() 
	  {
	    strokeWeight(3);
	    stroke(0);
	    fill(255,0,0);
	    ellipse(x, y, 10,10); 
	  }
	}

	class Predator extends Obsticle 									//Predators are Obstacles that move
	{
	  float vx = 0;
	  float vy = 0;

	  Predator() 														//Give the Predator a random velocity
	  {
	    super();														//Super gives the obstacle a random position
	    while(vx==0 || vy ==0) 											//Makes sure that all Predators have a velocity
	    {
	      vx = (int) random(-1.1f, 1.1f);
	      vy = (int) random(-1.1f, 1.1f);
	    }
	    minDistance = PREDATOR_DISTANCE;

	  }
	  
	  public void draw() 
	  {
	    if (x+vx > width || x+vx < 0)									//Retains boundary condition 
	      vx*=-1;
	    if (y+vy > height || y+vy<0) 									//Retains boundary contidion
	      vy*=-1; 
	    
	    x += vx;														//Add velocity to position
	    y += vy;

	    strokeWeight(1.5f);
	    stroke(255,0,200);
	    fill(150);
	    ellipse(x, y, 15,15); 											//Draw at new position
	  }
	}
}
