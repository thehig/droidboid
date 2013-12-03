package org.nerdcode.droidboid;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SaveLoad extends ListActivity {

	DroidBoidApp appContext;
	EditText etSettingName;
	Bundle savedInstanceState;

	public void onCreate(Bundle savedInstanceState) {
		//------Boilerplate------
		this.savedInstanceState = savedInstanceState;
		super.onCreate(savedInstanceState);
		setContentView(org.nerdcode.droidboid.R.layout.saveload);
		appContext = (DroidBoidApp)getApplicationContext();
		etSettingName = (EditText)findViewById(R.id.ETSettingName);
		
		
		try
		{
			// --Get File Names--
			ArrayList<String> files = FileHandler.LoadSettingsNames("DroidBoid.cfg");
			/*		
			String[] filesStringArray = new String[files.size()];
			for (int i = 0; i < filesStringArray.length; i++) {
				filesStringArray[i] = files.get(i);
			}
			*/
			setListAdapter(new ArrayAdapter<String>(this, R.layout.listitem, files));
	
			ListView lv = getListView();
			//lv.setAdapter(new ArrayAdapter<String>(this, R.layout.saveload, filesStringArray));
			lv.setTextFilterEnabled(true);
	
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					appContext = FileHandler.LoadDroidBoid("Droidboid.cfg", position, appContext);
					
					Intent i = new Intent(SaveLoad.this, Run.class);
			    	startActivity(i);
			    	finish();
			    	
					// When clicked, show a toast with the TextView text
					//Toast.makeText(getApplicationContext(),	((TextView) view).getText(), Toast.LENGTH_SHORT).show();
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void SaveSetting(View view) {
		FileHandler.SaveDroidBoid("DroidBoid.cfg", etSettingName.getText()
				.toString(), appContext);
		this.onCreate(savedInstanceState);
	}
}
