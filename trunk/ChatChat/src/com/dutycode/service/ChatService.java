package com.dutycode.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.dutycode.bean.MessageBean;
import com.dutycode.chatchatmain.MainActivity;
import com.dutycode.chatchatmain.R;
import com.dutycode.configdata.MessageConfig;


/**
 * 聊天服务类，处理聊天信息
 * @author michael
 *
 */
public class ChatService {

	XMPPConnection connection = ClientConServer.connection;
	
	/*用户保存用户的聊天记录*/
	private List<MessageBean> messageList = new ArrayList<MessageBean>();
	
	/*用户放置聊天信息*/
	private List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
	
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
	
	/**
	 * 发送消息（简单消息，不包括附加内容）
	 * @param _userJID 消息接收人的账号
	 * @param _message 发送的消息内容
	 */
	public void sendMessage(String _userJID, String _message){
		ChatManager chatmanger = connection.getChatManager();
		String chatThreadId = _userJID;
		
		Chat newChat = null;
		
		//判断ChatThread是否存在
		if (chatmanger.getThreadChat(chatThreadId) != null){
			newChat = chatmanger.getThreadChat(chatThreadId);
		}else {
			newChat = chatmanger.createChat(_userJID, chatThreadId, null);
		}
		
		MessageBean messageBean = new MessageBean();
		
		try {
			newChat.sendMessage(_message);
			
			//记录消息内容，保存到消息的List中
			messageBean.setMessageBody(_message);
			messageBean.setMessageFrom(MainActivity.userloginname + "  ");
//			messageBean.setMessageTo(_userJID);
			messageBean.setMessageTime(sdf.format(new Date()));
			
			logMessage(messageBean);
			
		}catch(XMPPException e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 监听指定用户JID的消息，适用于单独的聊天
	 * @param _userJID 用户的JID，这里同时为聊天的ThreadID
	 */
	public void listenningMessage(String _userJID){
		ChatManager chatmanger = connection.getChatManager();
		
		Chat chat = chatmanger.getThreadChat(_userJID);

		String chatThread = _userJID;
		NewMessageListener messagelistener = new NewMessageListener();
		if (chatmanger.getThreadChat(chatThread) != null){
			chat = chatmanger.getThreadChat(chatThread);
			chat.addMessageListener(messagelistener);
		}else {
			chat = chatmanger.createChat(_userJID, chatThread, messagelistener);
		}
		
	}
	
	/**
	 * 监听所有的消息
	 */
	public void listenningMessage(){
		ChatManager chatmanger = connection.getChatManager();
		chatmanger.addChatListener(new ChatManagerListener() {
			
			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				if (!createdLocally)
	                chat.addMessageListener(new NewMessageListener());;
			}
		});
	}
	
	/**
	 * 记录消息
	 * @param _message 消息内容
	 */
	private void logMessage(MessageBean _message){
		if (messageList.size() == MessageConfig.MESSAGE_MAX_LENGTH){
			//messageList到达最大长度,将内容保存到文件中
		}else {
			messageList.add(_message);
			Log.i("listsize", "" + messageList.size());
		}
		
		
	
		
	}
	
	/**
	 * 将消息保存到内存卡文件当中
	 * @param _messageList 
	 */
	private void saveMessageToFile(List<Message> _messageList){
		
	}
	
	
	public SimpleAdapter getMessageListAdapter(Context _context){
		
		/*用于放置聊天详细内容*/
		Map<String,Object> map = new HashMap<String, Object>();
		
		int listsize = messageList.size();
		if (listsize>0){
			map.put("messageBody", messageList.get(listsize-1).getMessageBody());
			map.put("messageFrom", messageList.get(listsize-1).getMessageFrom() + "  ");
			map.put("messageTime", messageList.get(listsize-1).getMessageTime());
			
			list.add(map);
		}
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(_context,list,R.layout.chat_list,
				new String[]{"messageFrom","messageBody","messageTime"},
				new int[]{R.id.messageFrom, R.id.messageBody, R.id.messageTime});
		
		
		return simpleAdapter;
	}
	
	/**
	 * 放置内容到Adapter中
	 */
	private void setAdapterList(){
		/*用于放置聊天详细内容*/
		Map<String,Object> map = new HashMap<String, Object>();
		
		
		int listsize = messageList.size();
		if (listsize > 0){
			map.put("messageBody", messageList.get(listsize-1).getMessageBody());
			map.put("messageFrom", messageList.get(listsize-1).getMessageFrom() + "   ");
			map.put("messageTime", messageList.get(listsize-1).getMessageTime());
//			map.put("messageTo", messageList.get(listsize-1).getMessageTo());
			
			list.add(map);
		}
		
		
		
	}
	
	/**
	 * 监听消息(内部类)
	 * @author michael
	 *
	 */
	class NewMessageListener implements MessageListener{

		
		@Override
		public void processMessage(Chat chat, Message message) {
			System.out.println("------:::::::::::::::::::::::::::::::::::::::::;");
			
			MessageBean messageBean = new MessageBean();
			messageBean.setMessageBody(message.getBody());
			messageBean.setMessageFrom(message.getFrom());
//			messageBean.setMessageTo(message.getTo());
			messageBean.setMessageTime("("+sdf.format(new Date())+")");
			
			logMessage(messageBean);
//			setAdapterList();
		}
		
		
	}

}



