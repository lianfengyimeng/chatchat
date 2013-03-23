package com.dutycode.serverconn;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.content.Context;

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
	private static String SERVER_ADDR = "192.168.1.107";
	private Context context;
	
	public ClientConServer(Context _context){
		this.context = _context;
	}
		
	public boolean login(String _username, String _password){
		ConnectionConfiguration config = new ConnectionConfiguration(SERVER_ADDR,PORT);
		/* 是否启用安全验证 */
		config.setSASLAuthenticationEnabled(false);
		/*是否启用调试模式*/
//		config.setDebuggerEnabled(true);
		
		/*创建Connection链接*/
		XMPPConnection connection = new XMPPConnection(config);
		try{
			connection.connect();
			connection.login(_username, _password);
			
			return true;
		}catch (XMPPException e){
			e.printStackTrace();
		}
		
		return false;
		
	}
}
