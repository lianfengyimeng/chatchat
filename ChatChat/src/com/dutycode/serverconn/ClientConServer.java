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
 * 负责连接Openfire
 * @author Michael.Zhang
 * time: 2013-3-22 下午3:32:36
 * 
 */
public class ClientConServer {
	

	private Context context;
	
	private static XMPPConnection connection ; //声明XMPPconnection对象，将在login方法中进行初始化
	
	/**
	 * 构造方法
	 * @param _context
	 */
	public ClientConServer(Context _context){
		this.context = _context;
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
		
		Log.i("michael", entriesGroup.size()+"");
		
		Map<String,List<Object>> map = new HashMap<String,List<Object>>();
		List<Object> listGroup = new ArrayList<Object>();
		List<Object> listGroupMember = new ArrayList<Object>();
		for(RosterGroup group: entriesGroup){  
            Collection<RosterEntry> entries = group.getEntries();  
            Log.i("---", group.getName());
            listGroup.add(group.getName());
            List<Object> groupMemb = new ArrayList<Object>();
            for (RosterEntry entry : entries) {
            	groupMemb.add(entry.getName());
                //Presence presence = roster.getPresence(entry.getUser());   
                //Log.i("---", "user: "+entry.getUser());   
                Log.i("---", "name: "+entry.getName());
                //Log.i("---", "tyep: "+entry.getType());   
                //Log.i("---", "status: "+entry.getStatus());   
                Log.i("---", "groups: "+entry.getGroups());   
            }
            listGroupMember.add(groupMemb);
        }
		
		map.put("groupName", listGroup);
		map.put("groupMember", listGroupMember);
		
		return map;
	}
}
