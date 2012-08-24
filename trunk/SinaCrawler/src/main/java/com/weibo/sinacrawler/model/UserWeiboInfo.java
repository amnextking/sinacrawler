package com.weibo.sinacrawler.model;

import java.util.ArrayList;

public class UserWeiboInfo {
	private Long UserID;
	private String UserName;
	private Boolean isVIP;
	private Character sex;
	private String interest;
	private String area;
	private String company;
	private String college;
	private String briefIntro;
	private String topics;
	private String tags;
	private Integer followNum;
	private Integer fansNum;
	private Integer BlogNum;
	private String updateTime;
	private ArrayList<MircoBlogInfo> microBlogList;
	
	public UserWeiboInfo(){
		UserID = 0L;
		UserName = null;
		isVIP = false;
		sex = null;
		interest = null;
		area = null;
		company = null;
		college = null;
		briefIntro = null;
		topics = null;
		tags = null;
		followNum = 0;
		fansNum = 0;
		BlogNum = 0;
		updateTime = null;
		microBlogList = new ArrayList<MircoBlogInfo>();
	}
	public Long getUserID() {
		return UserID;
	}
	public void setUserID(Long userID) {
		UserID = userID;
	}
	public String getUserName() {
		return UserName;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public Boolean getIsVIP() {
		return isVIP;
	}
	public void setIsVIP(Boolean isVIP) {
		this.isVIP = isVIP;
	}
	public Character getSex() {
		return sex;
	}
	public void setSex(Character sex) {
		this.sex = sex;
	}
	public String getInterest() {
		return interest;
	}
	public void setInterest(String interest) {
		this.interest = interest;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getCollege() {
		return college;
	}
	public void setCollege(String college) {
		this.college = college;
	}
	public String getBriefIntro() {
		return briefIntro;
	}
	public void setBriefIntro(String briefIntro) {
		this.briefIntro = briefIntro;
	}
	public String getTopics() {
		return topics;
	}
	public void setTopics(String topics) {
		this.topics = topics;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public Integer getFollowNum() {
		return followNum;
	}
	public void setFollowNum(Integer followNum) {
		this.followNum = followNum;
	}
	public Integer getFansNum() {
		return fansNum;
	}
	public void setFansNum(Integer fansNum) {
		this.fansNum = fansNum;
	}
	public Integer getBlogNum() {
		return BlogNum;
	}
	public void setBlogNum(Integer blogNum) {
		BlogNum = blogNum;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public ArrayList<MircoBlogInfo> getMicroBlogList() {
		return microBlogList;
	}
	public void setMicroBlogList(ArrayList<MircoBlogInfo> microBlogList) {
		this.microBlogList = microBlogList;
	}
}
