package com.dutycode.chatchatmain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.dutycode.serverconn.ClientConServer;

public class MainActivity extends Activity {
	public static String userloginname;

	// 分组信息
	private List<Object> groupArr;
	// 组员信息
	private List<Object> childArr_S;// 中间变量，用于转换List为List<List<Object>>
	private List<List<Object>> childArr;

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
				text = createView(childArr.get(groupPosition)
						.get(childPosition).toString());
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
				text = createView(groupArr.get(groupPosition).toString());
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

		private TextView createView(String content) {
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, 38);
			TextView text = new TextView(MainActivity.this);
			text.setLayoutParams(layoutParams);
			text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			text.setPadding(40, 0, 0, 0);
			text.setTextSize(20);
			text.setText(content);
			return text;
		}

	}

}