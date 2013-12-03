package org.nerdcode.droidboid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Environment;

public class FileHandler {
	
	String filename;
	
	public static void SaveDroidBoid(String filename, String title, DroidBoidApp appContext)
	{
		System.out.println("DroidBoid Attempting Save to file " + filename + " of setting " + title);
		try 
		{
		    File rootDir = Environment.getExternalStorageDirectory();
		    if (rootDir.canWrite())
		    {
		        File csvFile = new File(rootDir, filename);
		        FileWriter csvFileWriter = new FileWriter(csvFile, true);
		        BufferedWriter out = new BufferedWriter(csvFileWriter);
		        
		        StringBuffer outLine = new StringBuffer();
		        outLine.append(title + ",");
		        //-----Rules------
		        for(int i = 0; i < appContext.getRules().length; i++)
		        {
		        	outLine.append(appContext.getRules()[i] + ",");
		        }
		        
		        //-------Colors-------
		        for(int i = 0; i < appContext.getColors().length; i++)
		        {
		        	outLine.append(appContext.getColors()[i] + ", ");
		        }
		        
		        //------Consts------
		        //        Controller.setConsts(appContext.getCENTER_PULL_FACTOR(), appContext.getTARGET_PULL_FACTOR(), appContext.getBOUNCE_ABSORPTION(), appContext.getVELOCITY_PULL_FACTOR(), appContext.getMIN_DISTANCE(), appContext.getVELOCITY_LIMITER());
		        outLine.append(appContext.getCENTER_PULL_FACTOR() + ", " + appContext.getTARGET_PULL_FACTOR() + ", " + appContext.getBOUNCE_ABSORPTION() + ", " + appContext.getVELOCITY_PULL_FACTOR() + ", " + appContext.getMIN_DISTANCE() + ", " + appContext.getVELOCITY_LIMITER()+",");
		        //------Flock Size------
		        outLine.append(appContext.getFlockSize());
		        
		        //System.out.println("DERPITY: " + appContext.getFlockSize());
		        //-------Cleanup-----
		        outLine.append("\n");
		        System.out.println("DroidBoid:: Writing Out Setting : " + outLine.toString());
		        out.write(outLine.toString());
		        out.flush();
		        out.close();
		        
		    }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param filename 
	 * @param title 
	 */
	public static DroidBoidApp LoadDroidBoid(String filename, int lineNumber, DroidBoidApp appContext)
	{
		try 
		{
		    File rootDir = Environment.getExternalStorageDirectory();
		    if (rootDir.canWrite())
		    {
		        File csvFile = new File(rootDir, filename);
		        FileReader csvFileReader = new FileReader(csvFile);
		        BufferedReader in = new BufferedReader(csvFileReader);
		        
		        String line;
		        int lineCounter = 0;
		        //If you find setting with title we want load it into appContext
		        while((line = in.readLine()) != null)
		        {
		        	//System.out.println("DroidBoid:: Seeing Setting : " + line);
		        	String ruleName = line.substring(0, line.indexOf(','));
		        	//System.out.println("Looking for :" + title + ":");
		        	if(lineCounter == lineNumber)
		        	{
		        		System.out.println("DroidBoid:: Reading In Setting : " + line);
		        		//System.out.println("Found :" + ruleName + ":" + " " + lineCounter);
		        		//System.out.println("Line Reads: " + line);
		        		//int[] ruleValues = line.substring(line.indexOf(','), line.length()).split(',');
		        		String workingLine = line.substring(line.indexOf(','), line.length()); //String from 1st comma to end of line
		        		int numberOfValues = workingLine.split(",").length;
		        		System.out.println("Name: " + ruleName + " Values : " + numberOfValues);
		        		int[] ruleValues = new int[numberOfValues - 1]; //-1 for the name
		        		for(int i = 0; i < numberOfValues; i++)
		        		{
		        			String currentValue = workingLine.split(",")[i].trim();
		        			if(!currentValue.equals(""))
		        			{
		        				//This skips the first entry. All subsequent entries are -1
		        				//System.out.println("DroidBoid Value " + i + " ::" + currentValue + "::");
		        				ruleValues[i - 1] = Integer.parseInt(currentValue);
		        			}
		        		}
		        		//Set Rules from 0 - 4
		        		appContext.setRules(ruleValues[0], ruleValues[1], ruleValues[2], ruleValues[3], ruleValues[4]);
		        		//Set Colors from 5 - 10
		        		int[] colorValues = {ruleValues[5],ruleValues[6],ruleValues[7],ruleValues[8],ruleValues[9],ruleValues[10]};

		        		appContext.setColors(colorValues);
		        		//Set Consts from 11 to 16
		        		appContext.setConsts(ruleValues[11],ruleValues[12],ruleValues[13],ruleValues[14],ruleValues[15],ruleValues[16]);
		        		//Set flock size to last value 17
		        		appContext.setFlockSize(ruleValues[17]);
		        		System.out.println("Setting Loaded");
		        	}
		        	lineCounter++;

		        }
		    }
		    
		}
		catch(Exception e)
		{
			System.out.println("Derp loading file");
			e.printStackTrace();	
		}
		
		return appContext;
	}
	
	
	//Return a string array of all the setting names
	public static ArrayList<String> LoadSettingsNames(String filename)
	{

		System.out.print("DroidBoid: Attempting load of file " + filename);
		//Store the setting names
		ArrayList<String> settingNames = new ArrayList<String>();
		try
		{
			File root = Environment.getExternalStorageDirectory();
			if(root.canRead())
			{
				File csvFile = new File(root, filename);
				FileReader csvFileReader = new FileReader(csvFile);
				BufferedReader in = new BufferedReader(csvFileReader);
				
				//Read in variables one line at a time
				String settingLine;
				System.out.print(" found settings:");
				while((settingLine = in.readLine()) != null)
				{
					//Get the name of the setting
					String[] settings = settingLine.split(",");
					settingNames.add(settings[0]);
					System.out.print(" " + settings[0]);
				}
				System.out.print("\n");
			}
		}
		catch(IOException e)
		{
			System.out.println("Could not read file: "+filename+" - " + e.getMessage());
		}
		System.out.println("DroidBoid Returning " + settingNames.size() + " entries");
		return settingNames;
	}
	

}
