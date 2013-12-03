package org.nerdcode.droidboid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Environment;
import android.view.MotionEvent;


public class Main extends GLSurfaceView implements Renderer {

	private Context context;
	private Controller boidController;
	private int num = 5;
	private boolean blend = true;

	private static int screen_width;
	private static int screen_height;
	
	TextRenderer test = new TextRenderer();
	private GameTime gameTime;
	private int desiredFrameRate = 60;
	private double frameTimeInMS = (double)1000/desiredFrameRate; //Set target fps of 24

	public Main(Context context, int screen_width, int screen_height, int flockSize)
	{
		super(context);
		this.num = flockSize;
		this.setRenderer(this);		//Set the Renderer		
		this.requestFocus();		//Get display focus(for catching touch events)
		this.setFocusableInTouchMode(true);
		this.context = context;		//Keep the context
		this.screen_width = screen_width;		//Retain width and height for the boidController so that the Boids are drawn within bounds
		this.screen_height = screen_height;	

		//Setup gameTime
		gameTime = new GameTime();
	}


	//====================================================================================
	//-------------------------------EVENT HANDELERS--------------------------------------
	//====================================================================================
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		//Settings
		gl.glEnable(GL10.GL_TEXTURE_2D);					//Enable Texture Mapping
		gl.glShadeModel(GL10.GL_SMOOTH); 					//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 			//Black Background
		gl.glClearDepthf(1.0f); 							//Depth Buffer Setup

		gl.glDepthFunc(GL10.GL_LEQUAL); 					//The Type Of Depth Testing To Do

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); //Really Nice Perspective Calculations

		gl.glEnable(GL10.GL_BLEND);							//Enable blending
		gl.glDisable(GL10.GL_DEPTH_TEST);					//Disable depth test
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);		//Set The Blending Function For Translucency		

		boidController = new Controller(num, screen_width, screen_height);
		test.GenerateText(gl, "Herp Derp");
		//Load the texture for the boids once during Surface creation		
		//boidController.loadGLTexture(gl, this.context);			//Uncommenting this line reenables the texture
	}

	public void onDrawFrame(GL10 gl)
	{

		//Clear Screen And Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	

		//Check if the blend flag has been set to enable/disable blending
		if(blend) {
			gl.glEnable(GL10.GL_BLEND);			//Turn Blending On
			gl.glDisable(GL10.GL_DEPTH_TEST);	//Turn Depth Testing Off

		} else {
			gl.glDisable(GL10.GL_BLEND);		//Turn Blending On
			gl.glEnable(GL10.GL_DEPTH_TEST);	//Turn Depth Testing Off
		}	

		boidController.updateAll();
		gl.glPushMatrix();
		boidController.draw(gl);
		gl.glPopMatrix();		
		test.RenderToScreen(gl);
		//boidController.drawQuadTree(gl);

		//Figure out how much time has elapsed since the last draw
		double timeToUpdate = gameTime.ElapsedGameTime();
		//If we have taken less time to update than we were allotted
		if(timeToUpdate < frameTimeInMS)
		{
			try
			{
				//Sleep for the remaining time
				//System.out.println("Took " + timeToUpdate + " ms");
				Thread.sleep((long)(frameTimeInMS - timeToUpdate));
			}
			catch(Exception e)
			{	
				e.printStackTrace();
			}
		}
	}

	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		//GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
		//GLU.gluOrtho2D(gl,-width/2, width/2, height/2, -height/2);
		//GLU.gluOrtho2D(gl, 0, width, height, 0);
		gl.glOrthof(0, width, height, 0, 0, 100);
		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity();

		this.screen_width = width;
		this.screen_height = height;
		//boidController.CalculateBoundingBox(45f, width, height);
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		int contactPoints = event.getPointerCount();
		Vector2D[] contactPointsXY = new Vector2D[contactPoints];		
		for(int i = 0; i < contactPoints; i++)
		{
			contactPointsXY[i] = new Vector2D();
			contactPointsXY[i].setXY(event.getX(i), event.getY(i));
		}		
		boidController.updateTouch(contactPointsXY);

		return true;
	}


	public static int getScreen_width() {
		return screen_width;
	}


	public static int getScreen_height() {
		return screen_height;
	}


}
