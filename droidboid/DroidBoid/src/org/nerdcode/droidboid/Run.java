package org.nerdcode.droidboid;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class Run extends Activity {
    /** Called when the activity is first created. */
    Main main;
    DroidBoidApp appContext;
    SensorManager sM;
    Sensor accel;
    float accelx = 0f;
    float accely = 0f;
    
    SensorEventListener sensorEventListener = new SensorEventListener()
    {
    	public void onAccuracyChanged(Sensor sensor, int accuracy)
    	{
    		
    	}
    	
    	public void onSensorChanged(SensorEvent event)
    	{
    		float x = event.values[0];
    		float y = event.values[1];
    		updateAccelData(x,y);
    	}
    	
    };
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);									//Fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);				//Fullscreen
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); 	//Fullscreen
        WindowManager w = getWindowManager();											//Needed for Width/Height
        Display d = w.getDefaultDisplay();        										//Needed for Width/Height
        appContext = ((DroidBoidApp)getApplicationContext()); 							//Retreive the application Context. 
        																				//This contains the default values for the sliders       
        main = new Main(this, d.getWidth(), d.getHeight(), appContext.getFlockSize()); 	//Create Main with the parameters required
        setContentView(main);
        
        sM = (SensorManager)getSystemService(SENSOR_SERVICE);
        accel = sM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Controller.setDampingValues(appContext.getRules());								//Sets the static rule values in the Controller to be the values from the AppContext
        Controller.setAccelerometer(accelx, accely);
        Controller.setConsts(appContext.getCENTER_PULL_FACTOR(), appContext.getTARGET_PULL_FACTOR(), appContext.getBOUNCE_ABSORPTION(), appContext.getVELOCITY_PULL_FACTOR(), appContext.getMIN_DISTANCE(), appContext.getVELOCITY_LIMITER());
        Controller.setColors(appContext.getColors());
    }
    
	protected void onResume() {		
		super.onResume();
		main.onResume();
		sM.registerListener(sensorEventListener, accel, SensorManager.SENSOR_DELAY_NORMAL);
	}
    
	protected void onPause() {
		super.onPause();
		main.onPause();
		sM.unregisterListener(sensorEventListener);
	}
	
	public void updateAccelData(float x, float y)
	{
		accelx = x;
		accely = y;
		Controller.setAccelerometer(accelx, accely);
	}	
	
	public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)    
    {
    	Intent i;
    	/*
    	System.out.println("Event ID: " + item.getItemId() + " Title: " + item.getTitle());
    	System.out.println("ID of Colors: " + org.nerdcode.droidboid.R.layout.colors);
    	System.out.println("ID of Options: " + org.nerdcode.droidboid.R.layout.controls);
    	 */
    	if(item.getTitle().equals("Options"))
    	{
    		i = new Intent(Run.this, Controls.class);
    	}
    	else if(item.getTitle().equals("Colors"))
    	{
    		i = new Intent(Run.this, ColorOptions.class);
    	}
    	else if(item.getTitle().equals("Save Load"))
    	{
    		i = new Intent(Run.this, SaveLoad.class);
    	}
    	else
    	{
    		i = null;
    	}
    	if(i != null)
    		startActivity(i);
    	finish();
    	return true;
    }
        
    /*
    public boolean onColorItemSelected(MenuItem item)    
    {
    	Intent i = new Intent(Run.this, ColorOptions.class);
    	startActivity(i);
    	finish();
    	return true;
    }
    */
}