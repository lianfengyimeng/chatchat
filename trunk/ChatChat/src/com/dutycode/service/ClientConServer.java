package com.dutycode.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * 连接openfire服务器类
 * 负责连接Openfire
 * @author Michael.Zhang
 * time: 2013-3-22 下午3:32:36
 * 
 */
public class ClientConServer {
	

	private Context context;
	
	boolean isOnline = false;//标志，用来标示用户是否在线，初始为不在线
	
	private Handler isConnectServerhandler ;//Handler，主要用于软件联网情况，
	
	public static XMPPConnection connection ; //声明XMPPconnection对象，将在login方法中进行初始化
	
	/**
	 * 构造方法
	 * @param _context
	 */
	public ClientConServer(Context _context){
		this.context = _context;
	}
	
	/**
	 * 构造函数，用于初始化处理与UI主线程之间的交互Handler
	 * @param _handler
	 */
	public ClientConServer(Handler _handler){
		this.isConnectServerhandler = _handler;
	}

	public ClientConServer(){}
	
	
	/**
	 * 获取连接
	 * @param _serverIp 服务器IP地址
	 * @param _serverPort 服务器端口
	 * @return
	 */
	private XMPPConnection getConnection(String _serverIp, int _serverPort){
		ConnectionConfiguration config = new ConnectionConfiguration(_serverIp, _serverPort);
		/* 是否启用安全验证 */
		config.setSASLAuthenticationEnabled(false);
		/*是否启用调试模式*/
//		config.setDebuggerEnabled(true);
		
		/*创建Connection链接*/
		XMPPConnection connection = new XMPPConnection(config);
		return connection;
	}
	
	/**
	 * 登陆
	 * @param _username 用户名
	 * @param _password 密码
	 * @param _serverIp 服务器IP地址
	 * @param _serverPort 服务器端口号 
	 * @return
	 */
	public boolean login(String _username, String _password, String _serverIp, int _serverPort){
		
		//初始化connection对象
		connection = getConnection(_serverIp, _serverPort);
		
		try{
			connection.connect();
			connection.login(_username, _password);
			
			return true;
		}catch (XMPPException e){
			e.printStackTrace();
		}
		
		return false;
		
	}
	

	
	/**
	 * 退出登陆，即断开与服务器的链接
	 * @return 退出结果
	 */
	public boolean logoff(){
		
		if (connection.isConnected()){
			connection.disconnect();
			return true;
		}else {
			return false;//用户没有登录，无须断开
		}
		
	}
	
	/**
	 * 得到好友列表
	 * @return 好友列表
	 */
	public Map<String,List<Object>> getUserList(){
		
		Roster roster = connection.getRoster();
		Collection<RosterGroup> entriesGroup = roster.getGroups(); 
		
		
		Map<String,List<Object>>  map = new HashMap<String,List<Object>>();
		List<Object> listGroup = new ArrayList<Object>();
		List<Object> listGroupMember = new ArrayList<Object>();
		for(RosterGroup group: entriesGroup){  
            Collection<RosterEntry> entries = group.getEntries();  
            listGroup.add(group.getName());
            List<Object> groupMemb = new ArrayList<Object>();
            for (RosterEntry entry : entries) {
            	groupMemb.add(entry.getName());
            }
            listGroupMember.add(groupMemb);
        }
		
		map.put("groupName", listGroup);
		map.put("groupMember", listGroupMember);
		
		return map;
	}
	
	/**
	 * 查询某用户是否在线
	 * @param _username
	 * @return 用户在线，返回true， 不在线，返回false
	 */
	public boolean isSomeOneOnline(String _username){
		Roster roster = connection.getRoster();

		String userJID = this.getUserJIDByName(_username);
		roster.addRosterListener(new RosterListener() {
			
			@Override
			public void presenceChanged(Presence presence) {
				isOnline = presence.isAvailable();
			}
			
			@Override
			public void entriesUpdated(Collection<String> arg0) {
				
			}
			
			@Override
			public void entriesDeleted(Collection<String> arg0) {
				
			}
			
			@Override
			public void entriesAdded(Collection<String> arg0) {
				
			}
		});
		Presence presence = roster.getPresence(userJID);
		isOnline = presence.isAvailable();
		return isOnline;
	}
	
	/**
	 * 得到用户在线状态，包括以下几种：<br/>
	 * 	1 ： available ：Available (the default) 在线<br/>
	 * 	2： away Away. 离开<br/>
	 * 	3： chat Chat，可以聊天<br/>
	 * 	4： dnd ：Do not disturb，请勿打扰<br/>
	 * 	5： xa ： Away for an extended period of time.暂时离开<br/>
	 * @param _username 用户名
	 * @return <b> available </b> 在线
	 * 	<b> away</b>  离开
	 * 	<b> chat </b> 可以聊天
	 * 	<b> dnd </b> 请勿打扰
	 * 	<b> xa </b>暂时离开
	 */
	public Presence.Mode getMode(String _username){
		Roster roster = connection.getRoster();
		String userJID = this.getUserJIDByName(_username);
		Presence presence = roster.getPresence(userJID);
		
		return presence.getMode();
	}
	
	/**
	 * 根据用户名查询用户的JID信息，如b@michael-pc
	 * @param _useremail 用户邮箱
	 * @return 返回用户的JID
	 */
	public String getUserJIDByEmail(String _useremail){
		Roster roster = connection.getRoster();
		RosterEntry rosterentry = roster.getEntry(_useremail);
		String userJID = rosterentry.getUser();
		return userJID;
	}
	
	/**
	 * 根据用户名获得用户的JID <br/>
	 * 另见：{@link #getUserEmail(String)} {@link #getUserJIDByEmail(String)}
	 * @param _username 用户名
	 * @return 如果存在，返回用户JID，如果不存在，返回null
	 */
	public String getUserJIDByName(String _username){
		String useremail = getUserEmail(_username);
		String userJID = null;
		if ( useremail != null){
			//用户存在
			userJID = getUserJIDByEmail(useremail);
		}
		return userJID;
	}
	/**
	 * 根据用户名获得用户的邮箱
	 * @param _username 用户名
	 * @return 如果存在用户，返回用户邮箱，如果不存在用户，返回null
	 */
	public String getUserEmail(String _username){
		Roster roster = connection.getRoster();
		Collection<RosterEntry> rosterentrys = roster.getEntries();
		
		String useremail = null;
		for (RosterEntry rosterentry: rosterentrys){
			if (_username.equals(rosterentry.getName())){
				useremail = rosterentry.getUser();
				break;
			}
		}
		
		return useremail;
		
	}
	
	
	
	public void listeningConnectToServer(){
		connection.addConnectionListener(new ConnectionListener() {
			
			@Override
			public void reconnectionSuccessful() {
				// TODO Auto-generated method stub
				android.os.Message msg = android.os.Message.obtain();
				msg.obj = true;
				isConnectServerhandler.sendMessage(msg);
			}
			
			@Override
			public void reconnectionFailed(Exception arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void reconnectingIn(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void connectionClosedOnError(Exception e) {
				// TODO Auto-generated method stub
				android.os.Message msg = android.os.Message.obtain();
				msg.obj = false;
				isConnectServerhandler.sendMessage(msg);
				
			}
			
			@Override
			public void connectionClosed() {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
	 
}
