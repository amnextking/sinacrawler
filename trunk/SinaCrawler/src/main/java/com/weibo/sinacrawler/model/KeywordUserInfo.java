package com.weibo.sinacrawler.model;

public class KeywordUserInfo {
	private Long userID;
	private String weiboURL;
	private String content;
	public KeywordUserInfo(){
		userID = 0L;
		weiboURL = "";
		content = "";
	}
	public Long getUserID() {
		return userID;
	}
	public void setUserID(Long userID) {
		this.userID = userID;
	}
	public String getWeiboURL() {
		return weiboURL;
	}
	public void setWeiboURL(String weiboURL) {
		this.weiboURL = weiboURL;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
