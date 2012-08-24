package com.weibo.sinacrawler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;

import com.weibo.sinacrawler.htmlparser.ParserException;
import com.weibo.sinacrawler.model.*;
import com.weibo.sinacrawler.sinautil.SinaLogin;
import com.weibo.sinacrawler.util.SendCdkeyUtil;


public class SendMain {
	private static String contentFile = "./send_content.txt";

	public static void main(String[] args) throws SecurityException, IOException, InterruptedException, SQLException, ClassNotFoundException, ParserException {
		String accountName = args[0];
		String accountPwd = args[1];
		
		System.setProperty( "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog" );
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(9000000); 
		client.getHttpConnectionManager().getParams().setSoTimeout(9000000);
		SinaLogin sinaLogin = new SinaLogin(client, accountName, accountPwd);
		try{
			sinaLogin.login();
		}catch (Exception e) {

			System.out.println(accountName + " login failed, try to login with verify image");
			try{
				sinaLogin.loginWithVerify();
			} catch (Exception e2) {
				System.out.println(accountName + " login failed with verify image, exit");
			}
			System.out.println(accountName + "login with verify image successed");
		}
		
		sinaLogin.changeWeiboVersion();
		
		SendCdkeyUtil sendCdkeyUtil = new SendCdkeyUtil();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(contentFile), "UTF-8"));
		String content = reader.readLine();
		String userIDName = null;
		
		while((userIDName = sendCdkeyUtil.getUnSendedUser()) != null){
			String ss[] = userIDName.split("\t");
			Long userID = Long.parseLong(ss[0]);
			String userName = null;
			UserWeiboInfo userWeiboInfo = new UserWeiboInfo();
			if(ss.length == 1){
				userWeiboInfo = sinaLogin.getUserBasicInfo(userID, new Date());
				if(!userID.equals(userWeiboInfo.getUserID())){
					sendCdkeyUtil.sendFailed(userID,"");
					continue;
				}
				userName = userWeiboInfo.getUserName();
			}else{
				userName = ss[1];
			}
			String cdkey = null; //sendCdkeyUtil.getUnUsedCdkey();
			content = content.replace("{cdkey}", cdkey);
			
			PostReturnInfo postReturnInfo = sinaLogin.mail(userName, content);
			if(postReturnInfo.getReturnCode() == 100000){
				sinaLogin.log.info("send " + userID + " with " + cdkey + " successed by mail");
				sendCdkeyUtil.finishSend(userID, cdkey, accountName, "mail");
				continue;
			}else{
				//sinaLogin.log.info("mail return code:" + postReturnInfo.returnCode);
				//sinaLogin.log.info("mail return massage:" + postReturnInfo.returnMsg);
			}
			String weiboID = sinaLogin.getLatestWeiboID(userID);
			sinaLogin.log.info("weibo id: " + weiboID);
			while(true){
				postReturnInfo = sinaLogin.comment(weiboID, content);
				if(postReturnInfo.getReturnCode() == 100000){
					sinaLogin.log.info("send " + userID + " with " + cdkey + " successed by comment");
					sendCdkeyUtil.finishSend(userID, cdkey, accountName, "comment");
					break;
				}else if(postReturnInfo.getReturnMsg().contains("抱歉，根据用户的设置，你无法对此微博进行评论") || postReturnInfo.getReturnMsg().contains("抱歉，此微博不存在哦，换一个试试吧")){
					sinaLogin.log.info(postReturnInfo.getReturnMsg() + ",user id: " + userID);
					sendCdkeyUtil.sendFailed(userID,cdkey);					
					break;
				}else if(postReturnInfo.getReturnMsg().contains("抱歉，此内容违反了")){
					System.out.println(accountName + "存在封禁cdkey: " + cdkey);
					sinaLogin.log.info("封禁user： " + userID + "   封禁cdkey： " + cdkey);
					sendCdkeyUtil.sendFailed(userID,cdkey);
					sendCdkeyUtil.cdkeyForbidden(userID, cdkey);
					break;
				}else if(postReturnInfo.getReturnMsg().contains("微博发的太多啦，休息一会再发啦")){
					sinaLogin.log.info("processing userID: " + userID);
					sinaLogin.log.info(postReturnInfo.getReturnMsg() + ", 休息20分钟！");
					TimeUnit.MINUTES.sleep(20);
				}else {
					sinaLogin.log.info("wrong userID: " + userID);
					sinaLogin.log.info("error code: " + postReturnInfo.getReturnCode());
					sinaLogin.log.info("error msg: " + postReturnInfo.getReturnMsg());
					sinaLogin.log.info("等待1分钟！");
					TimeUnit.MINUTES.sleep(1);
					sendCdkeyUtil.sendFailed(userID,cdkey);
					sendCdkeyUtil.cdkeyForbidden(userID, cdkey);
					break;
				}
			}
		}
	}
}
