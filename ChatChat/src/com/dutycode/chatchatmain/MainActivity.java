package com.dutycode.chatchatmain;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	public static String userloginname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("michael", userloginname);
		super.onCreate(savedInstanceState);
	}
	
	
}
