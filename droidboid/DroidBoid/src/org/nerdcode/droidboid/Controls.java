package org.nerdcode.droidboid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class Controls extends Activity{
	
	DroidBoidApp appContext;
	
	//----------Rules---------
	SeekBar rule1slider;
	SeekBar rule2slider;
	SeekBar rule3slider;
	SeekBar rule4slider;
	SeekBar rule6slider;
	SeekBar flockslider;
	
	TextView r1TextView;
	TextView r2TextView;
	TextView r3TextView;
	TextView r4TextView;
	TextView r6TextView;
	TextView flockTextView;
	
	//--------Consts----------
	SeekBar centerPullSlider;
	SeekBar targetPullSlider;
	SeekBar bounceAbsorbtionSlider;
	SeekBar velocityPullSlider;
	SeekBar minDistanceSlider;
	SeekBar velocityLimiterSlider;
	
	TextView centerPullTextView;
	TextView targetPullTextView;
	TextView bounceAbsorbtionTextView;
	TextView velocityPullTextView;
	TextView minDistanceTextView;
	TextView velocityLimiterTextView;
	
	MySeekListener listener;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(org.nerdcode.droidboid.R.layout.controls);
		
		listener = new MySeekListener();
		
		appContext = ((DroidBoidApp)getApplicationContext());		
		rule1slider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.ControlRule1Slider);
		rule2slider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.ControlRule2Slider);
		rule3slider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.ControlRule3Slider);
		rule4slider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.ControlRule4Slider);
		rule6slider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.ControlRule6Slider);
		flockslider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.ControlFlockSlider);
		
		centerPullSlider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.centerPullSlider);
		targetPullSlider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.targetPullSlider);
		bounceAbsorbtionSlider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.bounceAbsorbtionSlider);
		velocityPullSlider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.velocityPullSlider);
		minDistanceSlider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.minDistanceSlider);
		velocityLimiterSlider = (SeekBar)findViewById(org.nerdcode.droidboid.R.id.velocityLimiterSlider);
		
		r1TextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.rule1Text);
		r2TextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.rule2Text);
		r3TextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.rule3Text);
		r4TextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.rule4Text);
		r6TextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.rule6Text);
		flockTextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.flockText);
		
		centerPullTextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.centerPullText);
		targetPullTextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.targetPullText);
		bounceAbsorbtionTextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.bounceAbsorbtionText);
		velocityPullTextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.velocityPullText);
		minDistanceTextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.minDistanceText);
		velocityLimiterTextView = (TextView)findViewById(org.nerdcode.droidboid.R.id.velocityLimiterText);
		
		rule1slider.setOnSeekBarChangeListener(listener);
		rule2slider.setOnSeekBarChangeListener(listener);
		rule3slider.setOnSeekBarChangeListener(listener);
		rule4slider.setOnSeekBarChangeListener(listener);
		rule6slider.setOnSeekBarChangeListener(listener);
		flockslider.setOnSeekBarChangeListener(listener);
		
		centerPullSlider.setOnSeekBarChangeListener(listener);
		targetPullSlider.setOnSeekBarChangeListener(listener);
		bounceAbsorbtionSlider.setOnSeekBarChangeListener(listener);
		velocityPullSlider.setOnSeekBarChangeListener(listener);
		minDistanceSlider.setOnSeekBarChangeListener(listener);
		velocityLimiterSlider.setOnSeekBarChangeListener(listener);
		
		
		GetCurrentValues();
	}
	
	/**
	 * Set the current slider positions to be the values stored in the application context
	 */
	public void GetCurrentValues()
	{
		rule1slider.setProgress((int)appContext.getRule1());
		rule2slider.setProgress((int)appContext.getRule2());
		rule3slider.setProgress((int)appContext.getRule3());
		rule4slider.setProgress((int)appContext.getRule4());
		rule6slider.setProgress((int)appContext.getRule6());
		flockslider.setProgress((int)appContext.getFlockSize());
		
		centerPullSlider.setProgress((int)appContext.getCENTER_PULL_FACTOR());
		targetPullSlider.setProgress((int)appContext.getTARGET_PULL_FACTOR());
		bounceAbsorbtionSlider.setProgress((int)appContext.getBOUNCE_ABSORPTION());
		velocityPullSlider.setProgress((int)appContext.getVELOCITY_PULL_FACTOR());
		minDistanceSlider.setProgress((int)appContext.getMIN_DISTANCE());
		velocityLimiterSlider.setProgress((int)appContext.getVELOCITY_LIMITER());
		
		r1TextView.setText("" + appContext.getRule1());
		r2TextView.setText("" + appContext.getRule2());
		r3TextView.setText("" + appContext.getRule3());
		r4TextView.setText("" + appContext.getRule4());	
		r6TextView.setText("" + appContext.getRule6());	
		
		centerPullTextView.setText("" + appContext.getCENTER_PULL_FACTOR());
		targetPullTextView.setText("" + appContext.getTARGET_PULL_FACTOR());
		bounceAbsorbtionTextView.setText("" + appContext.getBOUNCE_ABSORPTION());
		velocityPullTextView.setText("" + appContext.getVELOCITY_PULL_FACTOR());	
		minDistanceTextView.setText("" + appContext.getMIN_DISTANCE());	
		velocityLimiterTextView.setText("" + appContext.getVELOCITY_LIMITER());	
		
		flockTextView.setText("" + appContext.getFlockSize());
	}
	
	/**
	 * Update the application Context with the new slider values
	 * @param view
	 */
	public void UpdateClicked(View view)
	{
		//Set the application context variables
		appContext.setFlockSize(flockslider.getProgress());
		appContext.setRules(rule1slider.getProgress(), rule2slider.getProgress(), rule3slider.getProgress(), rule4slider.getProgress(), rule6slider.getProgress());
		appContext.setConsts(centerPullSlider.getProgress(), targetPullSlider.getProgress(), bounceAbsorbtionSlider.getProgress(), velocityPullSlider.getProgress(), minDistanceSlider.getProgress(), velocityLimiterSlider.getProgress());
		//Begin the new intent
		Intent i = new Intent(Controls.this, Run.class);
    	startActivity(i);
    	finish();
	}
	
	class MySeekListener implements SeekBar.OnSeekBarChangeListener
	{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(fromUser)
			{
				if(seekBar == rule1slider)
					r1TextView.setText("" + seekBar.getProgress());
				else if(seekBar == rule2slider)
					r2TextView.setText("" + seekBar.getProgress());
				else if(seekBar == rule3slider)
					r3TextView.setText("" + seekBar.getProgress());
				else if(seekBar == rule4slider)
					r4TextView.setText("" + seekBar.getProgress());
				else if(seekBar == rule6slider)
					r6TextView.setText("" + seekBar.getProgress());
				else if(seekBar == flockslider)
					flockTextView.setText("" + seekBar.getProgress());
				else if(seekBar == centerPullSlider)
					centerPullTextView.setText("" + seekBar.getProgress());
				else if(seekBar == targetPullSlider)
					targetPullTextView.setText("" + seekBar.getProgress());
				else if(seekBar == bounceAbsorbtionSlider)
					bounceAbsorbtionTextView.setText("" + seekBar.getProgress());
				else if(seekBar == velocityPullSlider)
					velocityPullTextView.setText("" + seekBar.getProgress());
				else if(seekBar == minDistanceSlider)
					minDistanceTextView.setText("" + seekBar.getProgress());
				else if(seekBar == velocityLimiterSlider)
					velocityLimiterTextView.setText("" + seekBar.getProgress());
				
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {			
		}		
	}
}
