package com.weibo.sinacrawler.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SendContentUtil {
	private static String mysqlURL = "jdbc:mysql://10.1.8.208:3306/weibo";
	private static String mysqlUser = "process";
	private static String mysqlPW = "hivedw@Podkgqgfq_1";
	
	private Connection conn;

	public SendContentUtil() throws SQLException, ClassNotFoundException{		
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(mysqlURL, mysqlUser, mysqlPW);
	}
	
//	public List<String> getUnSendedUser() throws SQLException{
//		String query = "select User_ID,User_Name from weibo_send_user where Is_Success = 0 and Send_Failed=0 limit 50";
//		Statement st = conn.createStatement();
//		ResultSet rs = st.executeQuery(query);
//		while(rs.next()){
//			Long userID = Long.parseLong(rs.getString(1));
//			String userName = rs.getString(2);
//
//				if(userName == null)
//					return String.valueOf(userID); 
//				else
//					return userID + "\t" + userName;
//		}
//		return null;
//	}
}
