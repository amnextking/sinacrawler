package com.weibo.sinacrawler.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.weibo.sinacrawler.model.*;


//import redis.clients.jedis.Jedis;


public class SendCdkeyUtil {
	private static String mysqlURL = "jdbc:mysql://10.1.8.208:3306/weibo";
	private static String redisServer = "10.1.8.207";
	private static String mysqlUser = "process";
	private static String mysqlPW = "hivedw@Podkgqgfq_1";

//	private Jedis jedis = new Jedis(redisServer,6379,200000);
	private Connection conn;

	public SendCdkeyUtil() throws SQLException, ClassNotFoundException{		
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(mysqlURL,mysqlUser,mysqlPW);
//		jedis.select(4);
	}

	public String getUnSendedUser() throws SQLException{
		String query = "select User_ID,User_Name from weibo_send_user where Is_Success = 0 and Send_Failed=0 limit 50";
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		while(rs.next()){
			Long userID = Long.parseLong(rs.getString(1));
			String userName = rs.getString(2);
//			if(!isProcessing(String.valueOf(userID))){

				if(userName == null)
					return String.valueOf(userID);
				else
					return userID + "\t" + userName;
//			}
		}
		return null;
	}

//	public String getUnUsedCdkey() throws SQLException{
//		String query = "select cdkey from weibo_send_cdkey where Is_Used = 0 and Is_Forbidden = 0 limit 50";
//		Statement st = conn.createStatement();
//		ResultSet rs = st.executeQuery(query);
//		while(rs.next()){
//			String cdkey = rs.getString(1);
//			if(!isProcessing(cdkey)){
//				return cdkey;
//			}
//		}
//		return null;
//	}
	public void finishSend(Long userID,String cdkey,String sendAccount,String sendType ) throws SQLException{
		String updateUserSql = "update weibo_send_user set Cdkey = '" + cdkey + "',Is_Success = 1,send_account = '" + sendAccount + "',send_type = '" + sendType + "' where user_id = " + userID;				
		Statement st1 = conn.createStatement();
		st1.executeUpdate(updateUserSql);
		
//		String updateCdkeySql = "update weibo_send_cdkey set is_used = 1 where cdkey = '" + cdkey + "'";
//		Statement st2 = conn.createStatement();
//		st2.executeUpdate(updateCdkeySql);
//		while(true){
//			try{				
//				jedis.del(String.valueOf(userID));
//				jedis.del(cdkey);
//				return;
//			}catch(Exception e){
//				//e.printStackTrace();
//				jedis = new Jedis(redisServer,6379,200000);
//				jedis.select(4);
//			}
//		}
	}

	public void sendFailed(Long userID,String cdkey) throws SQLException{
		String updateSql = "update weibo_send_user set Send_Failed = 1 where user_id = " + userID;
		Statement st2 = conn.createStatement();
		st2.executeUpdate(updateSql);
//		while(true){
//			try{				
//				jedis.del(String.valueOf(userID));
//				jedis.del(cdkey);
//				return;
//			}catch(Exception e){
//				//e.printStackTrace();
//				jedis = new Jedis(redisServer,6379,200000);
//				jedis.select(4);
//			}
//		}
	}

	public void cdkeyForbidden(Long userID,String cdkey) throws SQLException{
		String updateSql = "update weibo_send_cdkey set Is_Forbidden = 1 where cdkey = '" + cdkey + "'";
		Statement st2 = conn.createStatement();
		st2.executeUpdate(updateSql);
//		while(true){
//			try{	
//				jedis.del(String.valueOf(userID));
//				jedis.del(cdkey);
//				return;
//			}catch(Exception e){
//				//e.printStackTrace();
//				jedis = new Jedis(redisServer,6379,200000);
//				jedis.select(4);
//			}
//		}
	}


//	private boolean isProcessing(String key){
//		while(true){
//			try{
//				Long result = jedis.setnx(key, "1");
//				if(result.longValue() == 0)
//					return true;
//				else
//					return false;
//			} catch(Exception e){
//				//e.printStackTrace();
//				jedis = new Jedis(redisServer,6379,200000);
//				jedis.select(4);
//			}
//		}
//	}



}
