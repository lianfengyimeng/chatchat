package com.dutycode.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import android.os.Handler;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.dutycode.bean.MessageBean;
import com.dutycode.chatchatmain.ChatActivity;
import com.dutycode.chatchatmain.MainActivity;
import com.dutycode.chatchatmain.R;
import com.dutycode.configdata.Fileconfig;
import com.dutycode.configdata.MessageConfig;
import com.dutycode.tool.AndroidTools;


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
	//消息监听器
	private NewMessageListener messagelistener = new NewMessageListener();
	
	//消息Handler，用于和主线程UI进行交互，刷新UI数据
	private Handler messageListenHandler;
	
	//chatmanger用于处理当前的聊天
	private ChatManager chatmanger = connection.getChatManager();
	
	private Chat chat ;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
	
	SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy-MM-dd");
	
	
	public ChatService(){}
	
	public ChatService(Handler _handler){
		this.messageListenHandler = _handler;
	}
	
	/**
	 * 发送消息（简单消息，不包括附加内容）
	 * @param _userJID 消息接收人的账号
	 * @param _message 发送的消息内容
	 */
	public void sendMessage(String _userJID, String _message){
		String chatThreadId = _userJID;
		
		//判断ChatThread是否存在
		if (chatmanger.getThreadChat(chatThreadId) != null){
			chat = chatmanger.getThreadChat(chatThreadId);
		}else {
			chat = chatmanger.createChat(_userJID, chatThreadId, null);
		}
		
		MessageBean messageBean = new MessageBean();
		
		try {
			chat.sendMessage(_message);
			
			//记录消息内容，保存到消息的List中
			messageBean.setMessageBody(_message);
			messageBean.setMessageFrom(MainActivity.userloginname + "  ");
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

		String chatThread = _userJID;

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
			saveMessage(messageList);
			Log.i("listSizeIn:", "" + messageList.size());
			//将messageList置为空
			messageList.removeAll(messageList);
			messageList.add(_message);
			Log.i("listSizeIn2:", "" + messageList.size());
		}else {
			messageList.add(_message);
			Log.i("listsize", "" + messageList.size());
		}
		
	}
	
	/**
	 * 将消息保存到内存卡文件当中
	 * @param _messageList 
	 */
	private void saveMessage(List<MessageBean> _messageList){
		
		//聊天的记录文件名
		String messageLogName = sdfdate.format(new Date()) + ".chatchatfile";
		//聊天记录文件所在位置的文件夹，这里格式是这样的:chatchat/messagelog/UserName/：其中UserName为当前登录用户名
		String messageFolder = MessageConfig.MESSAGE_LOG_PATH + MainActivity.userloginname +"/"+chat.getThreadID() +"/";
		//聊天记录完整路径
		String messageFinalLogPath = Fileconfig.sdrootpath + messageFolder + messageLogName;
		
		//检测是否存在SD卡，如果不存在,不能保存聊天记录
		if (AndroidTools.isHasSD()){
			//存在SD卡，执行存储任务
			if (!AndroidTools.isFileExists(messageFinalLogPath)){
				//文件不存在，创建文件，并添加内容
				AndroidTools.createFileOnSD(messageFolder, messageLogName);
			}
			//保存消息记录
			saveMessageToFile(messageList, messageFinalLogPath);
		}
		
	}
	
	
	/**
	 * 文件保存方法，将消息保存到文件当中
	 * @param _messageList 消息列表
	 * @param _filepath 文件路径
	 */
	private void saveMessageToFile(List<MessageBean> _messageList, String _filepath){
		StringBuffer messagesb = new StringBuffer();
		
		
		messagesb.append("---------MessageSaved at ：" + sdf.format(new Date()) + "---------\n");
		//将消息内容转换成String类型用于保存到文件中
		for (int i = 0; i < _messageList.size(); i++){
			messagesb.append(messageList.get(i).getMessageFrom() + "\n");
			messagesb.append(messageList.get(i).getMessageTime() + "\n");
			messagesb.append(messageList.get(i).getMessageBody() + "\n");
			messagesb.append("\n");
		}
		try {
			FileOutputStream fout = new FileOutputStream(_filepath,true);
			byte[] bytes = messagesb.toString().getBytes();
			
			fout.write(bytes);
			fout.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 仅用于在点击返回按钮的时候进行检查是否需要存文件
	 */
	public void onlySaveMessageFileOnExit(){
		if (messageList.size() < MessageConfig.MESSAGE_MAX_LENGTH){
			
			//聊天的记录文件名
			String messageLogName = sdfdate.format(new Date())  + ".chatchatfile";
			//聊天记录文件所在位置的文件夹，这里格式是这样的:chatchat/messagelog/UserName/：其中UserName为当前登录用户名
			String messageFolder = MessageConfig.MESSAGE_LOG_PATH + MainActivity.userloginname +"/" + chat.getThreadID() + "/";
			//聊天记录完整路径
			String messageFinalLogPath = Fileconfig.sdrootpath + messageFolder + messageLogName;
			
			saveMessageToFile(messageList, messageFinalLogPath);
		}
	}

	/**
	 * 得到聊天内容的List
	 * @return
	 */
	public List<Map<String,Object>> getMessageList(){
		
		Map<String,Object> messagemap = new HashMap<String, Object>();
		
		int listsize = messageList.size();
		if (listsize>0){
			messagemap.put("messageBody", messageList.get(listsize-1).getMessageBody());
			messagemap.put("messageFrom", messageList.get(listsize-1).getMessageFrom() + "  ");
			messagemap.put("messageTime", messageList.get(listsize-1).getMessageTime());
			
			list.add(messagemap);
		}
		return list;
	}

	
	/**
	 * 放置内容到Adapter中,重载
	 */
	private void setAdapterList(){
		
		/*更新UI主线程，刷新聊天列表*/
		
		android.os.Message message = android.os.Message.obtain();
		message.obj = this.getMessageList();
		messageListenHandler.sendMessage(message);
		
	}
	
	/**
	 * 监听消息(内部类)
	 * @author michael
	 *
	 */
	class NewMessageListener implements MessageListener{
		
		public NewMessageListener(){}
		
		//消息bean
		
		@Override
		public void processMessage(Chat chat, Message message) {
			MessageBean messageBean = new MessageBean();
			messageBean.setMessageBody(message.getBody());
			messageBean.setMessageFrom(message.getFrom());
			messageBean.setMessageTime("("+sdf.format(new Date())+")");
			
			logMessage(messageBean);
			setAdapterList();
		}
		
		
	}

}



