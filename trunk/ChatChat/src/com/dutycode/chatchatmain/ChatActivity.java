package com.dutycode.chatchatmain;

import org.jivesoftware.smack.XMPPConnection;

import com.dutycode.service.ChatService;
import com.dutycode.service.ClientConServer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 聊天Activity
 * @author michael
 *
 */
public class ChatActivity extends Activity {

	private XMPPConnection connection;
	
	private TextView textviewChatWith;
	/**
	 * 聊天主界面
	 */
	private ListView listview_chatlist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		//得到connection对象
		connection = ClientConServer.connection;
		Bundle bundle = getIntent().getExtras();
		//当前登录用户JID的信息
		String username = connection.getUser();
		//聊天的对象的JID
		String chatTo = bundle.getString("userJID");
		
		String topTitle = username + " Chat With " + chatTo;
		
		/*设置聊天界面头部信息*/
		textviewChatWith = (TextView)findViewById(R.id.chatwith);
		textviewChatWith.setText(topTitle);
		
		listview_chatlist = (ListView)findViewById(R.id.listview_chat);
		
		new ChatService().sendMessage(chatTo, "I am Online");
		new ChatService().listenningMessage(chatTo);
	}
	
	
	
	
	
	
	
}
