package com.weibo.sinacrawler.model;

public class UserSourceInfo {
	private Long userId;
	private int sourceType;
	private String sourceDesc;
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	public String getSourceDesc() {
		return sourceDesc;
	}
	public void setSourceDesc(String sourceDesc) {
		this.sourceDesc = sourceDesc;
	}
	public UserSourceInfo() {
		super();
	}
	
}
