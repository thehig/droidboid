package org.nerdcode.droidboid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class ColorOptions extends Activity {

	DroidBoidApp appContext;

	Spinner rSpinner;
	Spinner gSpinner;
	Spinner bSpinner;
	
	EditText rEditText;
	EditText gEditText;
	EditText bEditText;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(org.nerdcode.droidboid.R.layout.colors);
		
		appContext = ((DroidBoidApp)getApplicationContext());		


		rSpinner = (Spinner) findViewById(R.id.redSpinner);
		gSpinner = (Spinner) findViewById(R.id.greenSpinner);
		bSpinner = (Spinner) findViewById(R.id.blueSpinner);
		
		rEditText = (EditText) findViewById(R.id.redEditText);
		gEditText = (EditText) findViewById(R.id.greenEditText);
		bEditText = (EditText) findViewById(R.id.blueEditText);		

		ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
				R.array.Colors, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		rSpinner.setAdapter(adapter);
		gSpinner.setAdapter(adapter);
		bSpinner.setAdapter(adapter);

		rSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	if(position == 0)
		    		rEditText.setEnabled(true);
		    	else
		    		rEditText.setEnabled(false);
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		    }

		});
		
		
		gSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	if(position == 0)
		    		gEditText.setEnabled(true);
		    	else
		    		gEditText.setEnabled(false);
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		    }

		});
		
		
		bSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	if(position == 0)
		    		bEditText.setEnabled(true);
		    	else
		    		bEditText.setEnabled(false);
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		    }

		});
		
		GetDetails();
	}
	
	private void GetDetails()
	{
		rSpinner.setSelection(appContext.getColors()[0]);
		gSpinner.setSelection(appContext.getColors()[1]);
		bSpinner.setSelection(appContext.getColors()[2]);
		
		
		rEditText.setText("" + appContext.getColors()[3]);
		gEditText.setText("" + appContext.getColors()[4]);
		bEditText.setText("" + appContext.getColors()[5]);
	}
	
	public void UpdateClicked(View view)
	{
		/*
		//Set the application context variables
		appContext.setFlockSize(flockslider.getProgress());
		appContext.setRules(rule1slider.getProgress(), rule2slider.getProgress(), rule3slider.getProgress(), rule4slider.getProgress(), rule6slider.getProgress());
		appContext.setConsts(centerPullSlider.getProgress(), targetPullSlider.getProgress(), bounceAbsorbtionSlider.getProgress(), velocityPullSlider.getProgress(), minDistanceSlider.getProgress(), velocityLimiterSlider.getProgress());
		//Begin the new intent
		Intent i = new Intent(Controls.this, Run.class);
    	startActivity(i);
    	finish();
    	*/
		int red = 0;
		int green = 0;
		int blue = 0;
		try
		{
			//System.out.println("R: " + rEditText.getText().toString());
			red = Integer.parseInt(rEditText.getText().toString());
			if(red < 0) red = 0;
			if(red > 255) red = 255;
			//System.out.println("G: " + gEditText.getText().toString());
			green = Integer.parseInt(gEditText.getText().toString());
			if(green < 0) green = 0;
			if(green > 255) green = 255;
			//System.out.println("B: " + bEditText.getText().toString());
			blue = Integer.parseInt(bEditText.getText().toString());
			if(blue < 0) blue = 0;
			if(blue > 255) blue = 255;
		}
		catch(Exception e)
		{}
		//System.out.println("R: " + red + " G: " + green + " B: " + blue);
		int[] spinnerValues = {rSpinner.getSelectedItemPosition(), gSpinner.getSelectedItemPosition(), bSpinner.getSelectedItemPosition(), red, green, blue};
		
		appContext.setColors(spinnerValues);
		//appContext.setRules(rule1slider.getProgress(), rule2slider.getProgress(), rule3slider.getProgress(), rule4slider.getProgress(), rule6slider.getProgress());
		//appContext.setConsts(centerPullSlider.getProgress(), targetPullSlider.getProgress(), bounceAbsorbtionSlider.getProgress(), velocityPullSlider.getProgress(), minDistanceSlider.getProgress(), velocityLimiterSlider.getProgress());
		//Begin the new intent
		Intent i = new Intent(ColorOptions.this, Run.class);
    	startActivity(i);
    	finish();
	}
}
