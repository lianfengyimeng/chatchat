package com.dutycode.chatchatmain;

import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.dutycode.serverconn.ClientConServer;

public class MainActivity extends Activity {
	public static String userloginname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("michael", userloginname);
		Map<String,Object> map = new ClientConServer().getUserList();
		
		super.onCreate(savedInstanceState);
		
	}
	
	
}
