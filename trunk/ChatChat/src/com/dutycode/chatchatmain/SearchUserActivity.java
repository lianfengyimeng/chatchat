package com.dutycode.chatchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.dutycode.bean.UserBean;
import com.dutycode.service.UserOperateService;

/**
 * 查询用户界面<br/>
 * 包括：<br/>
 * 1、查询用户<br/>
 * 2、添加用户为好友<br/>
 * @author michael
 *
 */
public class SearchUserActivity extends Activity{

	private EditText edittextSearchUserName;
	private Button btnSearchUser;
	private ListView listviewUserList;
	
	private String searchUsername;
	
	private UserOperateService useroperateservice;
	
	private final Context context = SearchUserActivity.this;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_user);
		
		//初始化控件
		edittextSearchUserName = (EditText)findViewById(R.id.search_user_username);
		btnSearchUser = (Button)findViewById(R.id.btn_search_user);
		listviewUserList = (ListView)findViewById(R.id.listview_search_user_list);
		
		//初始化业务类
		useroperateservice = new UserOperateService();
		//点击查找按钮
		btnSearchUser.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				searchUsername = edittextSearchUserName.getText().toString().trim();
				
				if ("".equals(searchUsername)){
					Toast.makeText(context, context.getResources().getString(R.string.search_user_error_empty_username), 
							Toast.LENGTH_SHORT).show();
				}else {
					new Thread(searchUserRunnable).start();
				}
			}
		});
		
		
		
	}
	
	
	/**
	 * 搜索用户线程
	 */
	Runnable searchUserRunnable = new Runnable(){

		@Override
		public void run() {
			List<UserBean> userList = new ArrayList<UserBean>();
			android.os.Message msg = android.os.Message.obtain();
			try {
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				
				userList = useroperateservice.searchUser(searchUsername);
				//将数据处理成Map类型
				for (int i = 0; i < userList.size(); i++){
					Map<String,Object> map = new HashMap<String, Object>();
					map.put("username", userList.get(i).getUserName());
					map.put("userjid", userList.get(i).getUserJID());
					map.put("useremail", userList.get(i).getEmail());
					list.add(map);
					
				}
				msg.obj = list;
			} catch (XMPPException e) {
				e.printStackTrace();
				msg.obj = null;
			}
			//更新主线程UI
			userSearchResHandler.sendMessage(msg);
		}
		
	};
	
	/**
	 * 用户搜索结果处理Handler
	 */
	Handler userSearchResHandler = new Handler (){

		@Override
		public void handleMessage(Message msg) {
			if (null == msg){
				Toast.makeText(context, context.getString(R.string.search_user_server_error_tip),
						Toast.LENGTH_SHORT).show();
			}else {
				List<Map<String,Object>> userlist = (List<Map<String,Object>>)msg.obj; 
				//将用户信息渲染到ListView
				
				SimpleAdapter adapter = new SimpleAdapter(context, 
						userlist, R.layout.search_user_listview_layout, 
						new String[]{"username","userjid","useremail"}, 
						new int[] {R.id.search_user_res_username, 
									R.id.search_user_res_userejid,
									R.id.search_user_res_useremail});
			
				listviewUserList.setAdapter(adapter);
				
				listviewUserList.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						// TODO 出现的用户列表，长按点击的事件，进行添加好友操作
						return false;
					}
					
				});
			}
		}
		
	};
	
}
