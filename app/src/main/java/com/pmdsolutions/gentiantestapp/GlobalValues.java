package com.pmdsolutions.gentiantestapp;

import android.os.Environment;

/**
 * Global values
 * @author Daniel Hosford
 *
 */
public class GlobalValues {
	
	//Version number
	public class Version{
		public static final String BOMNUMBER = "L";
		public static final String FIRMWARE = "7.0";
		public static final String BLUETOOTH = "7.0";
	}

	public static class FilePaths{
		public static final String ERROR_DIRECTORY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pmd-respirasense-logs/";
		public static final String MAINT_DIRECTORY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pmd-respirasense-logs/";
	}


}