package com.dutycode.chatchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dutycode.service.ChatService;
import com.dutycode.service.ClientConServer;
import com.dutycode.service.NotificationService;

public class MainActivity extends Activity {
	public static String userloginname;

	// 分组信息
	private List<Object> groupArr;

	private List<Object> childArr_S;// 中间变量，用于转换List为List<List<Object>>
	// 组员信息
	private List<List<Object>> childArr;

	private boolean isExit; // 标示是否退出程序

	private boolean isConnectOK = true;// 标示当前与服务器的连接状态,默认当前为已连接
	
	// ExpandListView控件，用户存放用户列表
	private ExpandableListView ex_listview_friendlist;

	// 状态栏提示管理器
	private NotificationManager notificationmanger;
	
	//整体消息监听
	private Thread totalMessageListnerThread ;
	//服务器连接监听线程
	private Thread conncetToServerListenerThread;
	
	private ClientConServer clintconnserver ;
	
	
	private Map<String,Object> chatThreadMap = new HashMap<String, Object>();
	private ExpandListViewFriendListAdapter expandlistviewfriendlistadapter =  new ExpandListViewFriendListAdapter();
	private NotificationService  notificationservice = new NotificationService();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.expandlistview_friendlist);

		// 获取用户列表，放置到list中
		Map<String, List<Object>> map = new ClientConServer().getUserList();
		groupArr = new ArrayList<Object>();
		childArr_S = new ArrayList<Object>();
		childArr = new ArrayList<List<Object>>();
		groupArr = map.get("groupName");
		childArr_S = map.get("groupMember");

		
		// 将list对象转换成List<List<Object>>，用于分组查询子节点
		for (int i = 0; i < childArr_S.size(); i++) {
			List<Object> list = (List<Object>) childArr_S.get(i);
			childArr.add(list);
		}

		ex_listview_friendlist = (ExpandableListView) findViewById(R.id.expandableListView_FriendList);
		ex_listview_friendlist
				.setAdapter(expandlistviewfriendlistadapter);
		expandlistviewfriendlistadapter.notifyDataSetChanged();

		// 注册notificationmanger
		notificationmanger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// 添加点击用户事件。点击用户进入到发送消息界面
		ex_listview_friendlist
				.setOnChildClickListener(new OnChildClickListener() {

					@Override
					public boolean onChildClick(ExpandableListView parent,
							View v, int groupPosition, int childPosition,
							long id) {
						if (isConnectOK){
							String username = childArr.get(groupPosition)
									.get(childPosition).toString();
							String userJID = new ClientConServer()
									.getUserJIDByName(username);

							
							// 用于传递参数到下一个Activity
							Bundle bundle = new Bundle();
							bundle.putString("ChatTo", userJID);
							
							//检测是否已经存在ChatThread
							if (chatThreadMap.containsKey(userJID)){
								bundle.putString("ChatThreadId", chatThreadMap.get(userJID).toString());
							}
							
							Intent intent = new Intent(MainActivity.this,
									ChatActivity.class);
							intent.putExtras(bundle);
							startActivity(intent);
						}else {
							Toast.makeText(MainActivity.this, 
									MainActivity.this.getString(R.string.lose_connect_with_server), Toast.LENGTH_SHORT)
									.show();
						}
						
						return false;
					}
				});

		//启动服务器状态连接监听线程
		conncetToServerListenerThread = new Thread(connectToServeListerRunable);
		conncetToServerListenerThread.start();
		
		//启动监听消息线程
		totalMessageListnerThread = new Thread(totalMessageListenerRunnable);
		totalMessageListnerThread.start();

		
	}
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * 退出程序方法
	 */
	private void exit() {
		if (!isExit) {
			isExit = true;
			Toast.makeText(
					getApplicationContext(),
					MainActivity.this.getResources().getString(
							R.string.exit_program_tip), Toast.LENGTH_SHORT)
					.show();
			mHandler.sendEmptyMessageDelayed(0, 2000);
		} else {

			// 将登陆状态改为退出登陆
			new ClientConServer().logoff();
			// 返回主界面
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			System.exit(0);
		}
	}

	/**
	 * 处理退出消息，如果2000ms之后没有再次点击返回，将isExit置为false
	 */
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			isExit = false;
		}

	};
	
	/**
	 * 服务器连接状态Handler
	 */
	Handler cononcetToServerHandler = new Handler (){

		@Override
		public void handleMessage(Message msg) {
		
			super.handleMessage(msg);
			boolean isConnected = (Boolean)msg.obj;
			isConnectOK = isConnected;
		}
		
	};


	
	
	Handler unReadMessageHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 得到传递来的消息，更新状态栏消息给出提示
			// 消息来源，消息发送者
			Map<String,Object> map = (Map<String,Object>)msg.obj;
			
			// 更新状态栏，给出提示
			notificationservice.setNotification(map, MainActivity.this, notificationmanger);
			
			//已经创建了chatThread
			if (!chatThreadMap.containsKey(map.get("chatThreadId").toString())){
				chatThreadMap.put(map.get("chatTo").toString(), map.get("chatThreadId").toString());
			}
		}
	};

	/**
	 * 服务器连接状态监听器
	 */
	Runnable connectToServeListerRunable = new Runnable(){

		@Override
		public void run() {
			clintconnserver = new ClientConServer(cononcetToServerHandler);
			clintconnserver.listeningConnectToServer();
		}
		
	};
	/**
	 * 整体的消息监听
	 */
	Runnable totalMessageListenerRunnable = new Runnable (){

		@Override
		public void run() {
			
			/* 添加消息监听 */
			new ChatService(MainActivity.this,unReadMessageHandler).listenningMessage();
			
		}
		
	};

	private class ExpandListViewFriendListAdapter extends
			BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return childArr.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int arg0, int arg1) {
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView text = null;
			if (convertView != null) {
				text = (TextView) convertView;
				text.setText(childArr.get(groupPosition).get(childPosition)
						.toString());
			} else {
				text = createChildView(childArr.get(groupPosition)
						.get(childPosition).toString());
			}

			Drawable img_online, img_offline;

			Resources res = getResources();

			img_online = res.getDrawable(R.drawable.online);
			img_offline = res.getDrawable(R.drawable.offline);

			// 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示

			img_online.setBounds(0, 0, img_online.getMinimumWidth(),
					img_online.getMinimumHeight());
			img_offline.setBounds(0, 0, img_offline.getMinimumWidth(),
					img_offline.getMinimumHeight());
		
			// 判断是否在线
			if (new ClientConServer().isSomeOneOnline(childArr
					.get(groupPosition).get(childPosition).toString())) {

				text.setCompoundDrawables(img_online, null, null, null);
			} else {
				text.setCompoundDrawables(img_offline, null, null, null);
			}
			return text;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return childArr.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groupArr.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return groupArr.size();
		}

		@Override
		public long getGroupId(int arg0) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView text = null;
			if (convertView != null) {
				text = (TextView) convertView;
				text.setText(groupArr.get(groupPosition).toString());
			} else {
				text = createGroupView(groupArr.get(groupPosition).toString());
			}
			return text;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			// 返回true保证子列表能够被点击，若返回false，则不能被点击
			return true;
		}

		/**
		 * 子列表视图
		 * 
		 * @param _content
		 *            子列表单元名,这里为用户的账号名称
		 * @return
		 */
		private TextView createChildView(String content) {
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 80);
			TextView text = new TextView(MainActivity.this);
			text.setLayoutParams(layoutParams);
			text.setGravity(Gravity.TOP | Gravity.LEFT);
			text.setPadding(40, 0, 0, 0);
			text.setTextSize(20);
			text.setText(content);
			return text;
		}

		/**
		 * 组视图
		 * 
		 * @param content
		 * @return
		 */
		private TextView createGroupView(String content) {
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 60);
			TextView text = new TextView(MainActivity.this);
			text.setLayoutParams(layoutParams);
			text.setGravity(Gravity.TOP | Gravity.LEFT);
			text.setPadding(50, 0, 0, 5);
			text.setTextSize(20);
			text.setText(content);
			return text;
		}

	}

}
