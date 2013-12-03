package org.nerdcode.droidboid;

import android.os.SystemClock;

public class GameTime {
	
	long prevTime;
	
	public void GameTime()
	{
		prevTime = SystemClock.currentThreadTimeMillis();
	}
	
	public double ElapsedGameTime()
	{
		//Get the current time
		long time = SystemClock.currentThreadTimeMillis();
		//Figure out how long it's been since we were last checked
		long timeDiff = time - prevTime;
		//Backup the current time
		prevTime = time;
		//Return the elapsed time since the last check
		return (double)timeDiff;
	}
}
