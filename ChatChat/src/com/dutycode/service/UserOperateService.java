package com.dutycode.service;

import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

/**
 * 用户操作服务类
 * 包括:<br/>
 * 注册用户<br/>
 * 添加好友<br/>
 * 修改用户密码<br/>
 * @author michael
 *
 */
public class UserOperateService {
	//这里不需要关注connection是否初始化，交由注册时点击按钮时初始化
	private XMPPConnection conncetion = ClientConServer.connection;
	
	private AccountManager accountmanger = conncetion.getAccountManager();
	
	private Roster roster = conncetion.getRoster();
	
	/**
	 * 注册新用户
	 * @param _username 用户名
	 * @param _password 密码
	 * @param attributes 附加值，比如邮箱等
	 * @return 注册是否成功
	 */
	public boolean regAccount(String _username, String _password, Map<String,String> attributes){
		boolean regmsg = false;//注册消息返回信息，用于显示给用户的提示
		
		//这里有点疑惑，这里使用AccountManger中的createAccount方法和使用Registration的区别是什么
		try {
			accountmanger.createAccount(_username, _password, attributes);
			regmsg = true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		
		return regmsg;
	}
	
	/**
	 * 修改密码
	 * @param _newpassword 新密码
	 * @return 修改成功 true， 失败false
	 */
	public boolean changePassword(String _newpassword){
		boolean isChangeOK = false;
		try {
			accountmanger.changePassword(_newpassword);
			isChangeOK = true;
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isChangeOK;
	}
	
	/**
	 * 设置好友请求方式<br/>
	 * 1、全部允许 accept_all<br/>
	 * 2、手动处理 manual<br/>
	 * 3、全部拒绝 reject_all<br/>
	 * @param _mode
	 */
	public void setSubScriptionMode(Roster.SubscriptionMode _mode){
		
		roster.setSubscriptionMode(_mode);
	}
}
