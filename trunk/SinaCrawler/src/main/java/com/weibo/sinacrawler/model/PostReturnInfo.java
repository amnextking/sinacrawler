package com.weibo.sinacrawler.model;

public class PostReturnInfo {
	private int returnCode;
	private String returnMsg;
	public PostReturnInfo(){
		returnCode = 0;
		returnMsg = "";
	}
	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	public String getReturnMsg() {
		return returnMsg;
	}
	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}
	
}
