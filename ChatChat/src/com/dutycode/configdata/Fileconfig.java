package com.dutycode.configdata;

import java.io.File;

/**
 * 文件相关配置变量
 * @author michael
 *
 */
public class Fileconfig {
	
	/**
	 * 保存密码的xml文件名称，存储在SD上
	 */
	public final static String xmlinfoname = "userinfo.xml";
	/**
	 * 保存的xml文件所在文件夹
	 */
	public final static String xmlfolderpath = "chatchat/";
	
	public final static String xmlinfopath = xmlfolderpath + xmlinfoname;
	/**
	 * sd卡根路径
	 */
	public final static String sdrootpath = android.os.Environment.getExternalStorageDirectory() + File.separator;
}
