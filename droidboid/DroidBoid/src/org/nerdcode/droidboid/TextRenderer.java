package org.nerdcode.droidboid;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLUtils;

public class TextRenderer {


	private  int[] textures = new int[1];
	int[] UVarray = new int[4];
	Bitmap bitmap;
	
	int fontHeight = 30;


	public void GenerateText(GL10 gl, String text)
	{
		//System.out.println("Calling Generate Text");	
		// Create an empty, mutable bitmap
		bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(Color.BLACK);
		// Draw the text
		Paint textPaint = new Paint();
		textPaint.setTextSize(fontHeight);
		textPaint.setAntiAlias(true);
		textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		// draw the text centered
		canvas.drawText(text, 0, fontHeight, textPaint);
		
		//Set UVcoordinates for opengl rendertoscreen function
		UVarray[0] = 0;						//Left
		UVarray[1] = bitmap.getHeight(); 	//Top
		UVarray[2] = bitmap.getWidth(); 	//Right
		UVarray[3] = -bitmap.getHeight(); 	//Bottom


		//Generate one texture pointer...
		gl.glGenTextures(1, textures, 0);
		//...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		//Create Nearest Filtered Texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

		//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		//Clean up
		bitmap.recycle();
	}

	public void RenderToScreen(GL10 gl)
	{
		//System.out.println("Trying to render piece of shit");
	
		gl.glColor4f(1.0f,1.0f, 1.0f, 1.0f);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		//Crop the image to UVarray coordinates
		((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, UVarray,0);
		//Draw texture at the following coordinates (x,y,z,height,width)
		((GL11Ext)gl).glDrawTexfOES(0.0f,Main.getScreen_height() - bitmap.getHeight(),0.0f,bitmap.getWidth(),bitmap.getHeight());

	}




}
