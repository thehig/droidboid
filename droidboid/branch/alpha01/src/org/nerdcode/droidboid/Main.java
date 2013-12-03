package org.nerdcode.droidboid;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.view.MotionEvent;
import android.view.WindowManager;


public class Main extends GLSurfaceView implements Renderer {
	
	private Context context;
	//Our boid controller with a default flock of 50
	private Controller boidController;
	private int num = 40;
	//private int num = 30;
	//Turn on nice blending
	private boolean blend = true;
	
	private int screen_width;
	private int screen_height;
		
	public Main(Context context, int screen_width, int screen_height)
	{
		super(context);
		
		//Set the Renderer
		this.setRenderer(this);		
		//Get display focus(for catching touch events)
		this.requestFocus();
		this.setFocusableInTouchMode(true);
		//Keep the context
		this.context = context;
		
		this.screen_width = screen_width;
		this.screen_height = screen_height;		
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		//Settings
		gl.glEnable(GL10.GL_TEXTURE_2D);					//Enable Texture Mapping
		gl.glShadeModel(GL10.GL_SMOOTH); 					//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 			//Black Background
		gl.glClearDepthf(1.0f); 							//Depth Buffer Setup
	
		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		
		gl.glEnable(GL10.GL_BLEND);							//Enable blending
		gl.glDisable(GL10.GL_DEPTH_TEST);					//Disable depth test
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);		//Set The Blending Function For Translucency		
				
		//Initiate our stars class with the number of stars
		boidController = new Controller(num, screen_width, screen_height);
		//Load the texture for the stars once during Surface creation
		boidController.loadGLTexture(gl, this.context);
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
		
		//
		boidController.updateAll();
		boidController.draw(gl);
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
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity();
		
		this.screen_width = width;
		this.screen_height = height;
		boidController.CalculateBoundingBox(45f, width, height);
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		float x = event.getX();
		float y = event.getY();
		
		float xPercentage = x / screen_width;
		float yPercentage = y / screen_height;
		
		boidController.updateTouch(xPercentage, yPercentage);
		
		//System.out.println("x: " + x + " y: " + y);
		return true;
	}
	
	/*
	public boolean onTouchEvent(MotionEvent event) {
		//
		float x = event.getX();
        float y = event.getY();
        
        //A press on the screen
        if(event.getAction() == MotionEvent.ACTION_UP) {
        	//Define an upper area of 10% to define a lower area
        	int upperArea = this.getHeight() / 10;
        	int lowerArea = this.getHeight() - upperArea;
        	
        	//
        	if(y > lowerArea) {
        		//Change the blend setting if the lower area left has been pressed ( NEW ) 
        		if(x < (this.getWidth() / 2)) {
        			if(blend) {
        				blend = false;
            		} else {
            			blend = true;
            		}
        			
        		//Change the twinkle setting if the lower area right has been pressed 
        		} else {
        			if(twinkle) {
        				twinkle = false;
            		} else {
            			twinkle = true;
            		}	
        		}
        	}
        }
        
        //We handled the event
		return true;
	}
	*/
}
