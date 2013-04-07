package com.dutycode.chatchatmain;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.dutycode.configdata.Fileconfig;
import com.dutycode.configdata.UserXmlParseConst;
import com.dutycode.serverconn.ClientConServer;
import com.dutycode.tool.AndroidTools;
import com.dutycode.tool.Tools;
import com.dutycode.tool.XMLHelper;

/**
 * 登录界面
 */
public class LoginActivity extends Activity {
	private EditText edit_username;
	private EditText edit_password;
	private EditText edit_serverip;
	private CheckBox checkbox_remberpsw;
	
	private Button btn_login;
	
	private Context context = LoginActivity.this;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		/*初始化控件*/
		edit_username = (EditText)findViewById(R.id.login_username);
		edit_password = (EditText)findViewById(R.id.login_password);
		edit_serverip = (EditText)findViewById(R.id.login_serverip);
		
		checkbox_remberpsw = (CheckBox)findViewById(R.id.checkBox_remberpsw);
		
		btn_login = (Button)findViewById(R.id.login_sign_in_button);

		/*防止UI冲突*/
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
			.detectDiskReads()
			.detectDiskWrites()
			.detectNetwork()   // or .detectAll() for all detectable problems
			.penaltyLog()
			.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
			.detectLeakedSqlLiteObjects() //探测SQLite数据库操作
			.penaltyLog() //打印logcat
			.penaltyDeath()
			.build());
		
		//检测是否存在SD卡，存在SD卡的情况下进行判断文件是否存在
		if (AndroidTools.isHasSD()){
			//检测是否存在文件，不存在，则创建xml文件
			if (AndroidTools.isFileExists(Fileconfig.xmlinfopath)){
				//存在xml,读取内容，放置到表单中国
				
				String xmlpath = Fileconfig.sdrootpath + Fileconfig.xmlinfopath;
				edit_username.setText(XMLHelper.readXMLByNodeName(UserXmlParseConst.USERNAME, xmlpath));
				edit_password.setText(XMLHelper.readXMLByNodeName(UserXmlParseConst.PASSWORD, xmlpath));
				edit_serverip.setText(XMLHelper.readXMLByNodeName(UserXmlParseConst.SERVER_IP, xmlpath));
				checkbox_remberpsw.setChecked(true);
				
			}
		}else {
			//没有内存卡，不需要执行操作
		}
		
		
		
		btn_login.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//获取用户的登录信息，连接服务器，获取登录状态
				String username = edit_username.getText().toString().trim();
				String password = edit_password.getText().toString().trim();
				String serverIp = edit_serverip.getText().toString().trim();
				int serverPort = 5222;//服务器端口号，这里默认为5222
				if ("".equals(username) || "".equals(password)){
					Toast.makeText(LoginActivity.this, context.getString(R.string.login_emptyname_or_emptypwd) , Toast.LENGTH_SHORT).show();
				}else if ("".equals(serverIp)){
					Toast.makeText(LoginActivity.this, context.getString(R.string.login_empty_serverip) , Toast.LENGTH_SHORT).show();
				}else if (!Tools.isCorrectIp(serverIp)){
					Toast.makeText(LoginActivity.this, context.getString(R.string.login_error_serverip) , Toast.LENGTH_SHORT).show();
				}else {
					ClientConServer ccs = new ClientConServer(LoginActivity.this);
					boolean loginStatus = ccs.login(username, password, serverIp, serverPort);
					if (loginStatus){
						Toast.makeText(LoginActivity.this, context.getString(R.string.login_successful) , Toast.LENGTH_SHORT).show();
						
						/*跳转到主界面	 */
						Intent intent = new Intent(LoginActivity.this, MainActivity.class);
						MainActivity.userloginname = username;//将用户的帐号放置到静态变量中
						
						/*判断是否记住密码，如果记住密码，则将信息写入xml中*/
						if (checkbox_remberpsw.isChecked()){
							Map<String,Object> map = new HashMap<String,Object>();
							map.put("username", username);
							map.put("password", password);
							map.put("serverip", serverIp);
							if (!AndroidTools.isFileExists(Fileconfig.xmlinfopath))
								AndroidTools.createFileOnSD(Fileconfig.xmlfolderpath ,Fileconfig.xmlinfoname);
							XMLHelper.createXML(map, Fileconfig.sdrootpath + Fileconfig.xmlinfopath);
							
						}else {
							//如果不点选保存密码，删除之前存在的xml文件
							if (AndroidTools.isFileExists(Fileconfig.xmlinfopath)){
								AndroidTools.deleteFileOnSD(Fileconfig.xmlinfopath);
							}
						}
						/*将登陆的Activity销毁*/
						LoginActivity.this.finish();
						
						/*跳转到MainActivity*/
						startActivity(intent);
						
						
					}else {
						Toast.makeText(LoginActivity.this, context.getString(R.string.login_fail) , Toast.LENGTH_SHORT).show();
					}
				}
				
			}
		});
		
		
		//设置checkbox监听事件，如果选中，则将信息写入xml中，如果未选中，则删除xml文件
		checkbox_remberpsw.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				//如果点选记住密码,检测是否存在SD卡，如果不存在，提示，则不能进行写文件操作
				if (isChecked){
					if (!AndroidTools.isHasSD()){
						Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.error_nosdcard), 
								Toast.LENGTH_SHORT).show();
					}
				}
				
			}
		});
		
	}
	
	
}
