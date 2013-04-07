package com.dutycode.chatchatmain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dutycode.serverconn.ClientConServer;
import com.dutycode.tool.Tools;

/**
 * 登录界面
 */
public class LoginActivity extends Activity {
	private EditText edit_username;
	private EditText edit_password;
	private EditText edit_serverip;
	
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
		
	}
	
	
}
