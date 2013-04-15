package com.dutycode.chatchatmain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	private  String[] messageListViewTitle = {"messageFrom","messageBody","messageTime"};
	private int[] messageListViewRes = {R.id.message_from, R.id.message_body, R.id.message_time};
	
	private Thread mChatThred;
	
	private Thread mChatListenThread;//消息监听线程
	
	private MessageHandler messageHandler;
	
	//listview数据，此时为聊天数据，暂时不初始化，在OnCreate中进行初始化，目的是保持simpleadapterdata数据源唯一
	private List<Map<String,Object>> simpleadapterdata;
	
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

		
		//得到当前线程的Looper实例，由于当前线程是UI线程也可以通过Looper.getMainLooper()得到   
		  
        Looper looper = Looper.myLooper();   
  
        //此处甚至可以不需要设置Looper，因为 Handler默认就使用当前线程的Looper   
  
        messageHandler = new MessageHandler(looper);   

        chatservice = new ChatService(messageHandler);
        
        simpleadapterdata = chatservice.getMessageList();//初始化
        
		SimpleAdapter simpleadapter =  new SimpleAdapter(ChatActivity.this, simpleadapterdata, R.layout.chat_list,
				messageListViewTitle, messageListViewRes);
		listview_chatlist.setAdapter(simpleadapter);
		
		simpleadapter.notifyDataSetChanged();
		
		

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
	
					// 将发送框设置为空
					edittext_chatMessageContent.setText("");
				}
			}
		});
		
		//消息接入监听
		mChatListenThread = new Thread(chatListenRunnable);
		mChatListenThread.start();

	}
	
	
	@Override
	public void onBackPressed() {
		// 当点击返回按钮的时候,保存聊天记录
		chatservice.onlySaveMessageFileOnExit();
		super.onBackPressed();
	}






	/*聊天线程*/
	Runnable chatRunnable = new Runnable(){

		@Override
		public void run() {
			chatservice.sendMessage(chatTo, messageContent);
			Message message = Message.obtain();
			message.obj = chatservice.getMessageList();
			
			messageHandler.sendMessage(message);
			
		}
		
	};
	
	
	/*消息监听线程*/
	Runnable chatListenRunnable = new Runnable() {
		
		@Override
		public void run() {
			/*添加消息监听器，监听接入消息*/
			chatservice.listenningMessage(chatTo);
		}
	};
	
	
	class MessageHandler extends Handler{
		
		public MessageHandler(Looper _lopper){
			super(_lopper);
		}

		@Override
		public void handleMessage(Message msg) {
			
			//更新数据源，用于主线程刷新UI
			simpleadapterdata = (List<Map<String,Object>>)msg.obj;

			//刷新listView控件
			listview_chatlist.invalidateViews();
			//使最后一个被选中，目的是使最新的数据显示在界面上
			listview_chatlist.setSelection(listview_chatlist.getBottom());
		}
		
		
	}
	
	

}
