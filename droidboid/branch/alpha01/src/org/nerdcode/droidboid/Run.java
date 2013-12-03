package org.nerdcode.droidboid;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class Run extends Activity {
    /** Called when the activity is first created. */
    Main main;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager w = getWindowManager();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        Display d = w.getDefaultDisplay();        
        
        main = new Main(this, d.getWidth(), d.getHeight());
        setContentView(main);        
    }
    
	protected void onResume() {
		super.onResume();
		main.onResume();
	}
    
	protected void onPause() {
		super.onPause();
		main.onPause();
	}
}