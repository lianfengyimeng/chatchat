package com.dutycode.service;

import java.util.ArrayList;
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


/**
 * 聊天服务类，处理聊天信息
 * @author michael
 *
 */
public class ChatService {

	XMPPConnection connection = ClientConServer.connection;
	
	/**
	 * 发送消息（简单消息，不包括附加内容）
	 * @param _userJID 消息接收人的账号
	 * @param _message 发送的消息内容
	 */
	public void sendMessage(String _userJID, String _message){
		ChatManager chatmanger = connection.getChatManager();
		String chatThreadId = _userJID;
		Chat newChat = chatmanger.createChat(_userJID, chatThreadId, null);
		
		try {
			newChat.sendMessage(_message);
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
		
		NewMessageListener messagelistener = new NewMessageListener();
		chat.addMessageListener(messagelistener);
		
		
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

}


/**
 * 监听消息
 * @author michael
 *
 */
class NewMessageListener implements MessageListener{

	
	@Override
	public void processMessage(Chat chat, Message message) {
		
	}
	

	
	
	
	
}
