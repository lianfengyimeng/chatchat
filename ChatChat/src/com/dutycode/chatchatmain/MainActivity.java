package com.dutycode.chatchatmain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import com.dutycode.serverconn.ClientConServer;

public class MainActivity extends Activity {
	public static String userloginname;

	//分组信息
	private List<String> groupArr ;
	//组员信息
	private List<List<String>> childArr;
	
	
	//ExpandListView控件，用户存放用户列表
	private ExpandableListView  ex_listview_friendlist;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.id.expandableListView_FriendList);
		Map<String,Object> map = new ClientConServer().getUserList();
		
		groupArr = new ArrayList<String>();
		childArr = new ArrayList<List<String>>();
		
		
	}
	
	
	
}
