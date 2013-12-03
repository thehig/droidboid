package org.nerdcode.droidboid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class Boid {
	static Random rand = new Random(1337);
	
	//---------------Boid Vars----------
	int myColor;
	float xpos, ypos;										//X and Y positions of the Boid
	float vx, vy;											//The velocity along the x and y axis. Negative and Positive values possible
	public float r, g, b;									//Boids Color
	int TRAIL_SCALE = 3;									//How long the trail is
	
	//---------------Boid Vertices------
	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	/** The buffer holding the texture coordinates */
	private FloatBuffer textureBuffer;
	
	private FloatBuffer tailBuffer;

	/** The initial vertex definition */
	private float vertices[] = {
								-1.0f, -1.0f, 0.0f, 	//Bottom Left
								1.0f, -1.0f, 0.0f, 		//Bottom Right
								-1.0f, 1.0f, 0.0f,	 	//Top Left
								1.0f, 1.0f, 0.0f 		//Top Right
													};
	
	/** The initial texture coordinates (u, v) */	
	private float texture[] = {
								0.0f, 0.0f, 
								1.0f, 0.0f, 
								0.0f, 1.0f, 
								1.0f, 1.0f,
											};
	
	private float tail[] = {0, 0, 0, 0, 0, 0};

	/**
	 * The Boid constructor.
	 * 
	 * Initiate the buffers.
	 */
	public Boid() {
		//
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
				
		//Change these lines for Random starting colors
		r = 0f;//rand.nextFloat();
		g = 0f;//rand.nextFloat();
		b = 0f;//rand.nextFloat();
	}

	/**
	 * The object own drawing function.
	 * Called from the renderer to redraw this instance
	 * with possible changes in values.
	 * 
	 * @param gl - The GL Context
	 */
	public void draw(GL10 gl) {
		updateColor();																	//Update the color
		gl.glScalef(5, 5, 0);															//Set the scale			
		//gl.glFrontFace(GL10.GL_CW);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);									//Enable the vertex, texture and normal state	
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);							//Set texture	
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);							//Point to our buffers
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);						//Texture Buffer
		gl.glColor4f(r, g, b, 1.0f);													//Set the color
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);				//Draw the vertices as triangle strip
		/*
		tail[4] = TRAIL_SCALE * vx;
		tail[5] = TRAIL_SCALE * vy;
	 	//tail = {0,0,0, TRAIL_SCALE*vx, TRAIL_SCALE*vy, 0};
		tailBuffer = makeFloatBuffer(tail);	 	
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, tailBuffer);
		//gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2); 
		*/
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);									//Disable the client state before leaving	
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);							//Texture
	}
	
	/**
	 * Updates the RGB values for the Boid
	 */
    private void updateColor() 
    {
    	/*
    	//Values should be in OPENGL format, ie: a float value between 0 and 1
		float speed = (float) Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));		
		//r = speed;
		//g = speed / 5;
		//b = speed;
		//r = speed;
		g = Math.abs(vx);
		b = Math.abs(vy);
		*/
    	float speed = (float) Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));	
    	if(Controller.colorOptions != null)
    	{
    		
    		/*
    		 * <item>Manual Value</item>
        <item>vx</item>
        <item>vy</item>
        <item>abs(vx)</item>
        <item>abs(vy)</item>
        <item>Speed</item>
    		 */
	    	//Red Options
    		switch(Controller.colorOptions[0])
    		{
    		case 0:	//Manual
    			r = (float) Controller.colorOptions[3] / 255;
    			break;   
    		case 1:	//vx
    			r = vx;
    			break;
    		case 2: //vy
    			r = vy;
    			break;
    		case 3: //abs(vx)
    			r = Math.abs(vx);
    			break;
    		case 4: //abs(vy)
    			r = Math.abs(vy);
    			break;
    		case 5: //speed
    			r = speed;
    			break;    			
    		}
    		
    		//Green Options
    		switch(Controller.colorOptions[1])
    		{
    		case 0:	//Manual
    			g = (float) Controller.colorOptions[4] / 255;
    			break; 
    		case 1:	//vx
    			g = vx;
    			break;
    		case 2: //vy
    			g = vy;
    			break;
    		case 3: //abs(vx)
    			g = Math.abs(vx);
    			break;
    		case 4: //abs(vy)
    			g = Math.abs(vy);
    			break;
    		case 5: //speed
    			g = speed;
    			break; 
    		}
    		
    		//Blue Options
    		switch(Controller.colorOptions[2])
    		{
    		case 0:	//Manual
    			b = (float) Controller.colorOptions[5] / 255;
    			break;  
    		case 1:	//vx
    			b = vx;
    			break;
    		case 2: //vy
    			b = vy;
    			break;
    		case 3: //abs(vx)
    			b = Math.abs(vx);
    			break;
    		case 4: //abs(vy)
    			b = Math.abs(vy);
    			break;
    		case 5: //speed
    			b = speed;
    			break; 
    		}
    	}
    	else
    	{
    		r = 0.5f;
    		g = 0;
    		b = 0;
    	}
	}
    
    public void updateWithVector(Vector2D movementVector)
    {
    	vx += movementVector.x;
    	vy += movementVector.y;
    	xpos += vx;
    	ypos += vy;
    }

    /**
     * Loads the array provided into a FloatBuffer for display
     * @param arr The array of points
     * @return The corresponding FloatBuffer
     */
	protected static FloatBuffer makeFloatBuffer(float[] arr)    
    { 
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4); 
        bb.order(ByteOrder.nativeOrder()); 
        FloatBuffer fb = bb.asFloatBuffer(); 
        fb.put(arr); 
        fb.position(0); 
        return fb; 
    }
	@Override
	public String toString()
	{
		return ""+ xpos + ", " + ypos + " - " + vx + ", " + vy;
	}
} // end class Boid
