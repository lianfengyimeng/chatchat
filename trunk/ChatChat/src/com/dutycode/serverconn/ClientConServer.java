package com.dutycode.serverconn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import com.dutycode.chatchatmain.MainActivity;

import android.content.Context;
import android.util.Log;

/**
 * 连接openfire服务器类
 * @author Michael.Zhang
 * time: 2013-3-22 下午3:32:36
 *
 */
public class ClientConServer {
	
	/**
	 * 端口号
	 */
	private static int PORT = 5222;
	/**
	 * 服务器地址
	 */
	private static String SERVER_ADDR = "192.168.208.40";
//	private static String SERVER_ADDR = "192.168.1.107";
	private Context context;
	
	private static XMPPConnection connection = getConnection();
	
	public ClientConServer(Context _context){
		this.context = _context;
	}
	public ClientConServer(){}
	
	/**
	 * 取得XMPP链接（私有方法）
	 * @return
	 */
	private static XMPPConnection getConnection(){
		ConnectionConfiguration config = new ConnectionConfiguration(SERVER_ADDR,PORT);
		/* 是否启用安全验证 */
		config.setSASLAuthenticationEnabled(false);
		/*是否启用调试模式*/
//		config.setDebuggerEnabled(true);
		
		/*创建Connection链接*/
		XMPPConnection connection = new XMPPConnection(config);
		return connection;
	}
	
	/**
	 * 登录到服务器
	 * @param _username
	 * @param _password
	 * @return
	 */
	public boolean login(String _username, String _password){

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
	public Map<String,Object> getUserList(){
		
		Roster roster = connection.getRoster();
		Collection<RosterGroup> entriesGroup = roster.getGroups(); 
		
		Log.i("michael", entriesGroup.size()+"");
		
		Map<String,Object> map = new HashMap<String,Object>();
		List<Object> listGroup = new ArrayList<Object>();
		List<Object> listGroupMember = new ArrayList<Object>();
		for(RosterGroup group: entriesGroup){  
            Collection<RosterEntry> entries = group.getEntries();  
            Log.i("---", group.getName());
            listGroup.add(group.getName());
            for (RosterEntry entry : entries) {
            	listGroupMember.add(entry.getName());
                //Presence presence = roster.getPresence(entry.getUser());   
                //Log.i("---", "user: "+entry.getUser());   
                Log.i("---", "name: "+entry.getName());
                //Log.i("---", "tyep: "+entry.getType());   
                //Log.i("---", "status: "+entry.getStatus());   
                Log.i("---", "groups: "+entry.getGroups());   
            }  
        }
		
		map.put("groupName", listGroup);
		map.put("groupMember", listGroupMember);
		
		return map;
	}
}
