package com.dutycode.chatchatmain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.dutycode.service.ClientConServer;

public class MainActivity extends Activity {
	public static String userloginname;

	// 分组信息
	private List<Object> groupArr;
	
	private List<Object> childArr_S;// 中间变量，用于转换List为List<List<Object>>
	// 组员信息
	private List<List<Object>> childArr;
	
	private boolean isExit; //标示是否退出程序

	// ExpandListView控件，用户存放用户列表
	private ExpandableListView ex_listview_friendlist;

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
		ex_listview_friendlist.setAdapter(new ExpandListViewFriendListAdapter());

	}
	
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			exit();
			return false;
		}else {
			return super.onKeyDown(keyCode, event);
		}
	}


	/**
	 * 退出程序方法
	 */
	private void exit(){
		if (!isExit){
			isExit = true;
			Toast.makeText(getApplicationContext(), MainActivity.this.getResources().getString(R.string.exit_program_tip), 
					Toast.LENGTH_SHORT).show();
			mHandler.sendEmptyMessageDelayed(0, 2000);
		}else {
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
            // TODO Auto-generated method stub   
            super.handleMessage(msg);  
            isExit = false;  
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
			
			/**
			 * 判断用户在线状态未完成！
			 */
//			System.out.println("Mode : " + new ClientConServer().getMode(childArr.get(groupPosition).get(childPosition)
//					.toString()));
//			//判断是否在线
//			if (new ClientConServer().isSomeOneOnline(childArr.get(groupPosition).get(childPosition)
//					.toString())){
//				
//				text.setBackgroundColor(Color.RED);
//			}else {
//				text.setBackgroundColor(Color.BLUE);
//			}
			return text;
			/*
			 * 下面这段代码会导致列表错乱，具体原因现在还没有找到
			LinearLayout ll = null;
			if (convertView != null) {
				ll = (LinearLayout) convertView;
			} else {
				ll = createChildView(childArr.get(groupPosition)
						.get(childPosition).toString());
			}
			return ll;
			*/
			
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
			return false;
		}

		/**
		 * 子列表视图
		 * @param _content 子列表单元名,这里为用户的账号名称
		 * @return
		 */
		private TextView createChildView(String content) {
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 50);
			TextView text = new TextView(MainActivity.this);
			text.setLayoutParams(layoutParams);
			text.setGravity(Gravity.TOP | Gravity.LEFT);
			text.setPadding(40, 0, 0, 0);
			text.setTextSize(20);
			text.setText(content);
			return text;
		}
		/*private LinearLayout createChildView(String _content) {
			
			LinearLayout ll = new LinearLayout(
                    MainActivity.this);
            ll.setOrientation(0);
            
            ImageView img = new ImageView(MainActivity.this);
            
            img.setPadding(50, 0, 0, 0);
            
          //得到用户在线状态，根据用户在线状态给出相应的图标
			boolean isUserOnline = new ClientConServer().isSomeOneOnline(_content);
			if (isUserOnline){
				img.setImageResource(R.drawable.online);
			}else {
				img.setImageResource(R.drawable.offline);
			}
            
			
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 50);
			TextView text = new TextView(MainActivity.this);
			text.setLayoutParams(layoutParams);
			text.setGravity(Gravity.TOP | Gravity.LEFT);
			text.setPadding(30, 0, 0, 5);
			text.setTextSize(20);
			
			
			text.setText(_content);
			
			
            ll.addView(img);
            ll.addView(text);
			return ll;
		}
		*/
		
		/**
		 * 组视图
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
