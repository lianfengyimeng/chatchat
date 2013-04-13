package com.dutycode.chatchatmain;

import org.jivesoftware.smack.XMPPConnection;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dutycode.service.ChatService;
import com.dutycode.service.ClientConServer;

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
	private EditText edittext_chatMessageContent;// 聊天内容
	private Button btnSendMessage;

	private String chatTo;// 聊天对象

	private ChatService chatservice;

	private String messageContent;
	
	private Thread mChatThred;
	
	private Thread mChatListenThread;//消息监听线程
	
	private MessageHandler messageHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		// 得到connection对象
		connection = ClientConServer.connection;
		Bundle bundle = getIntent().getExtras();
		// 当前登录用户JID的信息
		String username = connection.getUser();
		// 聊天的对象的JID
		chatTo = bundle.getString("userJID");

		edittext_chatMessageContent = (EditText) findViewById(R.id.et_sendmessage);

		btnSendMessage = (Button) findViewById(R.id.btn_send);

		String topTitle = username + " Chat With " + chatTo;

		/* 设置聊天界面头部信息 */
		textviewChatWith = (TextView) findViewById(R.id.chatwith);
		textviewChatWith.setText(topTitle);

		listview_chatlist = (ListView) findViewById(R.id.listview_chat);

		chatservice = new ChatService();
		
		//得到当前线程的Looper实例，由于当前线程是UI线程也可以通过Looper.getMainLooper()得到   
		  
        Looper looper = Looper.myLooper();   
  
        //此处甚至可以不需要设置Looper，因为 Handler默认就使用当前线程的Looper   
  
        messageHandler = new MessageHandler(looper);   


		btnSendMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				messageContent = edittext_chatMessageContent.getText()
						.toString();
				if ("".equals(messageContent)) {
					Toast.makeText(
							ChatActivity.this,
							ChatActivity.this.getResources().getString(
									R.string.messagee_eror_null),
							Toast.LENGTH_SHORT).show();
				} else {
					
					mChatThred = new Thread(chatRunnable);
					mChatThred.start();
					
					mChatListenThread = new Thread(chatListenRunnable);
					mChatListenThread.start();
					// 将发送框设置为空
					edittext_chatMessageContent.setText("");
					

					
					
				}
			}
		});

	}
	
	Runnable chatRunnable = new Runnable(){

		@Override
		public void run() {
			chatservice.sendMessage(chatTo, messageContent);
			SimpleAdapter simpleadapter = chatservice
												.getMessageListAdapter(ChatActivity.this);
			Message message = Message.obtain();
			message.obj = simpleadapter;
			
			messageHandler.sendMessage(message);
			
		}
		
	};
	
	
	Runnable chatListenRunnable = new Runnable() {
		
		@Override
		public void run() {
			chatservice.listenningMessage(chatTo);
			
			SimpleAdapter simpleadapter = chatservice
					.getMessageListAdapter(ChatActivity.this);
			Message message = Message.obtain();
			message.obj = simpleadapter;
			
			messageHandler.sendMessage(message);
			
			
		}
	};
	
	
	class MessageHandler extends Handler{
		
		public MessageHandler(Looper _lopper){
			super(_lopper);
		}

		@Override
		public void handleMessage(Message msg) {
			
			//更新UI
			listview_chatlist.setAdapter((SimpleAdapter)msg.obj);
			
		}
		
		
	}

}
