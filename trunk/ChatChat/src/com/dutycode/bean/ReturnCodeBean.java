package com.dutycode.bean;
/**
 * 这里放置返回代码
 * @author michael
 *
 */
public class ReturnCodeBean {

	/**
	 * 原密码不正确
	 */
	public final static int ERROR_OLD_PSW_ERROR = 0;
	
	/**
	 * 用户名为空
	 */
	public final static int ERROR_EMPTY_USERNAME_OR_USERJID = -1;
	
	/**
	 * 返回值为真
	 */
	public final static int RETURN_TRUE = 1;
	
	/**
	 * 返回值为假
	 */
	public final static int RETURN_FALSE = 2;
	
}
