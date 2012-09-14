package com.weibo.sinacrawler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;

import com.weibo.sinacrawler.model.PostReturnInfo;
import com.weibo.sinacrawler.sinautil.SinaLogin;
import com.weibo.sinacrawler.util.ParallelUtil;
import com.weibo.sinacrawler.util.Setting;



public class TestMain {
	
	public static ArrayList<Long> sendUserList = new ArrayList<Long>();
	ParallelUtil parallelUtil = new ParallelUtil();

	public static String lunwenContent = "有你论文网由在校博士生与高校教师组成，为您提供原创论文代写代发。有你论文网真诚欢迎您的光临与惠顾！！！";
//		+ "有你论文网由在校博士生与高校教师组成，为您提供原创论文代写代发。有你论文网真诚欢迎您的光临与惠顾！！！";

	public static String dianpuContent = "  http://shop70611321.taobao.com  "
		+ "兄弟姐妹朋友们， 还在为话费高而担忧吗？ 还等什么呢，足不出户，网上充值优惠进行时，全网最低价，欢迎你的光顾！！！";


	public void getSendUserList(int start, int end ){
		try {
			sendUserList = parallelUtil.getSendUser(start, end);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String getAtContent(List<Long> atUserIdList){
		String content;
		StringBuilder atContent = new StringBuilder();
		for(Long userId: atUserIdList){
			atContent.append("@").append(userId).append(" ");
		}
		content = atContent.toString();
		
		return content;
	}
	
	public static int getweiboStrSize(String str){
		int count = 0;
		for(int i = 0; i < str.length(); i++){
			char c = str.charAt(i);
			if(c < '\u00ff'){
				count++;
			}else{
				count += 2;
			}
		}
		int realCount = Math.round((float)count/2);
		return realCount;
	}

	public void sendMessage(Long userId, SinaLogin sinaLogin, String weiboContent) throws Exception{
		String realContent = weiboContent;
		
		if(getweiboStrSize(realContent) < 140){
			if(userId != null){
				String weiboID = sinaLogin.getLatestWeiboID(userId);
				PostReturnInfo postReturnInfo = sinaLogin.comment(weiboID, realContent);
				
				if(postReturnInfo.getReturnCode() == 100000){
					
					System.out.println("send " + userId + " comment successed. ");
					
					//批量回写数据库，设置已发送
					parallelUtil.finishSend(userId);
					
					TimeUnit.SECONDS.sleep(15);
				}else if(postReturnInfo.getReturnMsg().contains("抱歉，根据用户的设置，你无法对此微博进行评论")){
					System.out.println("can not comment user " + userId);
					parallelUtil.deleteUser(userId);
					System.out.println("send " + userId + " error, delete the user. ");
					
				}else if(postReturnInfo.getReturnMsg().contains("微博发的太多啦，休息一会再发啦")){				
					System.out.println("操作过于频繁,等待10分钟");
					TimeUnit.MINUTES.sleep(10);		
					
				}else if(postReturnInfo.getReturnMsg().contains("抱歉，此微博不存在哦，换一个试试吧")){
					parallelUtil.deleteUser(userId);
					System.out.println("send " + userId + " error, delete the user. ");
					
				}else if(postReturnInfo.getReturnMsg().contains("不要太贪心哦，该微博已经评论过了")){
					System.out.println("user： " + userId + postReturnInfo.getReturnMsg());
				
				}else{
					System.out.println("mail error message:" + postReturnInfo.getReturnMsg());
					parallelUtil.deleteUser(userId);
					System.out.println("send " + userId + " error, delete the user. ");
	
				}
			}
		}
	}

	public void sendMessage(Long userId, List<Long> atUserIdList, SinaLogin sinaLogin, String weiboContent) throws Exception{
		
		String atContent = getAtContent(atUserIdList);
		String realContent = atContent + weiboContent;
		
		if(getweiboStrSize(realContent) < 140){
			
			if(userId != null){
					
				String weiboID = sinaLogin.getLatestWeiboID(userId);
				PostReturnInfo postReturnInfo = sinaLogin.comment(weiboID, realContent);
				
				if(postReturnInfo.getReturnCode() == 100000){
					
					System.out.println("send " + userId + " " + atContent + " comment successed. ");
					
					//批量回写数据库，设置已发送
					atUserIdList.add(userId);
					parallelUtil.finishSend(atUserIdList);
					
					TimeUnit.SECONDS.sleep(10);
				}else if(postReturnInfo.getReturnMsg().contains("抱歉，根据用户的设置，你无法对此微博进行评论")){
					System.out.println("can not comment user " + userId);
					parallelUtil.deleteUser(userId);
					System.out.println("send " + userId + " error, delete the user. ");
					
				}else if(postReturnInfo.getReturnMsg().contains("微博发的太多啦，休息一会再发啦")){				
					System.out.println("操作过于频繁,等待10分钟");
					TimeUnit.MINUTES.sleep(10);		
					
				}else if(postReturnInfo.getReturnMsg().contains("抱歉，此微博不存在哦，换一个试试吧")){
					System.out.println("抱歉，此微博不存在， 用户: " + userId);
					parallelUtil.deleteUser(userId);
					System.out.println("send " + userId + " error, delete the user. ");
					
				}else if(postReturnInfo.getReturnMsg().contains("不要太贪心哦，该微博已经评论过了")){
					System.out.println("user： " + userId + postReturnInfo.getReturnMsg());
				
				}else{
					System.out.println("mail error message:" + postReturnInfo.getReturnMsg());
					parallelUtil.deleteUser(userId);
					System.out.println("send " + userId + " error, delete the user. ");
	
				}
				TimeUnit.SECONDS.sleep(5);
			}
		}
	}

	public static void main(String[] args) throws Exception{
		TestMain testMain = new TestMain();
		int start =0;
		int step = 100;
		int length = 10;
		
		System.setProperty( "org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog" );
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(9000000); 
		client.getHttpConnectionManager().getParams().setSoTimeout(9000000);
		
		//加载信息
		Setting.load();
		//登录初始化
		SinaLogin sinaLogin = new SinaLogin(client, Setting.su, Setting.sp);
		try{
			sinaLogin.login();
		}catch (Exception e) {

			System.out.println("login failed, try to login with verify image");
			sinaLogin.loginWithVerify();
			System.out.println("login with verify image successed");
		}
		
		int sendNum =0;
		int atSize = 0;
		List<Long> atUserIdList = new ArrayList<Long>();
		for(int index=0; index <length; index++ ){
			
			testMain.getSendUserList( ++start, step );
			
			//自己，检查发送是否成功
			sendUserList.add(0, 2098861353L);
			
			int size = sendUserList.size();
			for(int i= 0; i<size; i++){
				try {
					
					if(atSize < 5 ){
						atUserIdList.add(sendUserList.get(i));
						atSize++;
					}else{
						
						sendNum++;
						System.out.print( sendNum*5 + " > ");
						testMain.sendMessage(atUserIdList.get(0), atUserIdList.subList(1, 5), sinaLogin, lunwenContent);
//						testMain.sendMessage(sendUserList.get(i), sinaLogin, lunwenContent);
						
						atSize = 1;
						atUserIdList.clear();
						atUserIdList.add(sendUserList.get(i));
					}
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
					
				}
			}
			
			start = step;
			
		}
		
	}

}
