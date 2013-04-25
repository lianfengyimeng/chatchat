package com.dutycode.chatchatmain;

import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dutycode.configdata.Fileconfig;
import com.dutycode.service.ChatService;
import com.dutycode.service.ClientConServer;
import com.dutycode.service.FileTransferOperateService;

/**
 * 聊天Activity
 * 
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
	private Button btnSendFile;
	private Button btnSearchHistoryMessage;// 查询历史记录

	// 发送文件Dialog界面控件
	private EditText editSendFilePath;
	private Button btnSelectFile;

	private ProgressDialog mypDialog;;

	private String chatTo;// 聊天对象
	private String filePath;

	private ChatService chatservice;

	private ClientConServer clintconnserver;

	private String messageContent;

	private String[] messageListViewTitle = { "messageFrom", "messageBody",
			"messageTime" };
	private int[] messageListViewRes = { R.id.message_from, R.id.message_body,
			R.id.message_time };

	private Thread mChatThred;

	private Thread mChatListenThread;// 消息监听线程

	// 服务器连接监听线程
	private Thread conncetToServerListenerThread;

	private MessageHandler messageHandler;

	private boolean isConnectOK = true;// 标示当前与服务器的连接状态,默认当前为已连接

	// listview数据，此时为聊天数据，暂时不初始化，在OnCreate中进行初始化，目的是保持simpleadapterdata数据源唯一
	private List<Map<String, Object>> simpleadapterdata;

	private Context context = ChatActivity.this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		// 得到connection对象
		connection = (XMPPConnection) ClientConServer.connection;
		Bundle bundle = getIntent().getExtras();
		// 当前登录用户JID的信息
		String username = connection.getUser();
		// 聊天的对象的JID

		chatTo = bundle.getString("ChatTo");

		edittext_chatMessageContent = (EditText) findViewById(R.id.et_sendmessage);

		btnSendMessage = (Button) findViewById(R.id.btn_send);
		btnSearchHistoryMessage = (Button) findViewById(R.id.right_btn);
		btnSendFile = (Button) findViewById(R.id.btn_chat_send_file);

		String topTitle = username + " Chat With " + chatTo;

		/* 设置聊天界面头部信息 */
		textviewChatWith = (TextView) findViewById(R.id.chatwith);
		textviewChatWith.setText(topTitle);

		listview_chatlist = (ListView) findViewById(R.id.listview_chat);

		// 得到当前线程的Looper实例，由于当前线程是UI线程也可以通过Looper.getMainLooper()得到

		Looper looper = Looper.myLooper();

		// 此处甚至可以不需要设置Looper，因为 Handler默认就使用当前线程的Looper

		messageHandler = new MessageHandler(looper);

		if (bundle.size() == 2) {
			// 从状态栏传递过来的
			chatservice = new ChatService(messageHandler,
					bundle.getString("ChatTo"),
					bundle.getString("ChatThreadId"));
		} else {
			chatservice = new ChatService(messageHandler,
					bundle.getString("ChatTo"));
		}
		// chatservice = new ChatService(messageHandler);

		simpleadapterdata = chatservice.getMessageList();// 初始化

		SimpleAdapter simpleadapter = new SimpleAdapter(ChatActivity.this,
				simpleadapterdata, R.layout.chat_list, messageListViewTitle,
				messageListViewRes);
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
					if (isConnectOK) {
						mChatThred = new Thread(chatRunnable);
						mChatThred.start();

						// 将发送框设置为空
						edittext_chatMessageContent.setText("");
					} else {
						Toast.makeText(
								ChatActivity.this,
								ChatActivity.this
										.getString(R.string.lose_connect_with_server),
								Toast.LENGTH_SHORT).show();
					}

				}
			}
		});

		// 发送文件
		btnSendFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// 初始化mypDialog
				mypDialog = new ProgressDialog(ChatActivity.this);
				sendFileHandler.sendEmptyMessage(0);
			}
		});

		// 查询历史消息按钮点击事件
		btnSearchHistoryMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("chatTo", chatTo);
				Intent intent = new Intent(ChatActivity.this,
						HistoryMessageActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);

				// 保存当前的聊天内容
				chatservice.onlySaveMessageFileOnExit();
			}
		});

		// 启动服务器状态连接监听线程
		conncetToServerListenerThread = new Thread(connectToServeListerRunable);
		conncetToServerListenerThread.start();
		// 消息接入监听
		// mChatListenThread = new Thread(chatListenRunnable);
		// mChatListenThread.start();

	}

	@Override
	public void onBackPressed() {
		// 当点击返回按钮的时候,保存聊天记录
		chatservice.onlySaveMessageFileOnExit();
		super.onBackPressed();
	}

	/**
	 * 发送文件Handler
	 */
	Handler sendFileHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);

			View view = inflater.inflate(R.layout.send_file_layout, null);

			// 初始化控件
			editSendFilePath = (EditText) view
					.findViewById(R.id.send_file_file_path);
			btnSelectFile = (Button) view
					.findViewById(R.id.send_file_btn_select_file);

			AlertDialog dialog = new AlertDialog.Builder(ChatActivity.this)
					.setIcon(R.drawable.chatchat)
					.setTitle(R.string.file_send_file_title)
					.setView(view)
					.setPositiveButton(
							context.getResources().getString(
									R.string.file_btn_send_file_final),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO
									filePath = editSendFilePath.getText()
											.toString().trim();
									new Thread(sendFileRunnable).start();

								}
							})
					.setNegativeButton(
							context.getResources().getString(
									R.string.file_btn_send_file_cancel), null)
					.create();

			dialog.show();
		}

	};

	/**
	 * 服务器连接状态Handler
	 */
	Handler cononcetToServerHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			boolean isConnected = (Boolean) msg.obj;
			isConnectOK = isConnected;
		}

	};

	/**
	 * 服务器连接状态监听器
	 */
	Runnable connectToServeListerRunable = new Runnable() {

		@Override
		public void run() {
			clintconnserver = new ClientConServer(cononcetToServerHandler);
			clintconnserver.listeningConnectToServer();
		}

	};

	/* 聊天线程 */
	Runnable chatRunnable = new Runnable() {

		@Override
		public void run() {
			chatservice.sendMessage(chatTo, messageContent);
			Message message = Message.obtain();
			message.obj = chatservice.getMessageList();

			messageHandler.sendMessage(message);

		}

	};

	/**
	 * 发送文件线程
	 */
	Runnable sendFileRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO 发送文件线程
			// 这里只是用于测试
			filePath = Fileconfig.sdrootpath + "a.jpg";
			FileTransferOperateService filet = new FileTransferOperateService();
			OutgoingFileTransfer transfer = filet.sendFile(chatTo, filePath,
					"Send By ChatChat");
			android.os.Message msg = android.os.Message.obtain();

			// if (transfer.getStatus() == Status.negotiating_transfer){
			// msg.obj =
			// context.getResources().getString(R.string.file_send_request_other_acept);
			// sendFileProgressHandler.sendMessage(msg);
			// }
			// if (transfer.getStatus() == Status.negotiating_transfer ||
			// transfer.getStatus() == Status.in_progress) {
			msg.obj = context.getResources().getString(
					R.string.file_send_in_progress);
			sendFileProgressHandler.sendMessage(msg);
			// }
			while (!transfer.isDone()) {

				// msg.obj =
				// context.getResources().getString(R.string.file_send_in_progress);
				// sendFileProgressHandler.sendMessage(msg);

				if (transfer.getStatus().equals(Status.error)) {
					System.out.println("ERROR!!! " + transfer.getError());
				} else {
					System.out.println(transfer.getStatus());
					System.out.println(transfer.getProgress());

				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (transfer.isDone()) {
				System.out.println("Done!");
				msg.obj = context.getResources().getString(
						R.string.file_send_success);
				sendFileProgressHandler.sendMessage(msg);
			}

		};

		/**
		 * 处理进度条显示Handler
		 */
		Handler sendFileProgressHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String message = (String) msg.obj;

				// 设置进度条风格，风格为圆形，旋转的
				mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				// 设置ProgressDialog 标题
				mypDialog.setTitle("Google");
				// 设置ProgressDialog 提示信息
				mypDialog.setMessage(message);
				// 设置ProgressDialog 标题图标
				mypDialog.setIcon(R.drawable.chatchat);

				// mypDialog.setButton("Google",this);
				// 设置ProgressDialog 的一个Button
				// 设置ProgressDialog 的进度条是否不明确
				mypDialog.setIndeterminate(false);
				// 设置ProgressDialog 是否可以按退回按键取消
				mypDialog.setCancelable(true);
				// 让ProgressDialog显示

				if (message.equals(context.getResources().getString(
						R.string.file_send_in_progress))) {
					mypDialog.show();
				}

				if (message.equals(context.getResources().getString(
						R.string.file_send_success))) {
					Toast.makeText(
							context,
							context.getResources().getString(
									R.string.file_send_success),
							Toast.LENGTH_SHORT).show();
					mypDialog.dismiss();

				}
			}
		};

	};

	/* 消息监听线程 */
	// Runnable chatListenRunnable = new Runnable() {
	//
	// @Override
	// public void run() {
	// /*添加消息监听器，监听接入消息*/
	// chatservice.listenningMessage(chatTo);
	// }
	// };

	class MessageHandler extends Handler {

		public MessageHandler(Looper _lopper) {
			super(_lopper);
		}

		@Override
		public void handleMessage(Message msg) {

			// 更新数据源，用于主线程刷新UI
			simpleadapterdata = (List<Map<String, Object>>) msg.obj;

			// 刷新listView控件
			listview_chatlist.invalidateViews();
			// 使最后一个被选中，目的是使最新的数据显示在界面上
			listview_chatlist.setSelection(listview_chatlist.getBottom());
		}

	}

}
