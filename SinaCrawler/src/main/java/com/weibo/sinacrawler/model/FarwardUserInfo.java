package com.weibo.sinacrawler.model;

public class FarwardUserInfo {
	private Long userID;
	private int farwardNum;
	private String farwardContent;
	private String farwardURL;
	private String weiboID;
	public FarwardUserInfo(){
		userID = 0L;
		farwardNum = 0;
		farwardContent = "";
		farwardURL = "";
		weiboID = "";
	}
	public Long getUserID() {
		return userID;
	}
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	public int getFarwardNum() {
		return farwardNum;
	}
	public void setFarwardNum(int farwardNum) {
		this.farwardNum = farwardNum;
	}
	public String getFarwardContent() {
		return farwardContent;
	}
	public void setFarwardContent(String farwardContent) {
		this.farwardContent = farwardContent;
	}
	public String getFarwardURL() {
		return farwardURL;
	}
	public void setFarwardURL(String farwardURL) {
		this.farwardURL = farwardURL;
	}
	public String getWeiboID() {
		return weiboID;
	}
	public void setWeiboID(String weiboID) {
		this.weiboID = weiboID;
	}
	
}
