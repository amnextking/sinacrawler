package com.weibo.sinacrawler.util;

import java.io.FileInputStream;
import java.util.Properties;

public class Setting {	
	public static int maxBlogNum = 10000;
	public static String su = "zhoujin@mail.ustc.edu.cn";
	public static String sp = "abcd1234!";
	public static String logFilePath = "./logs";
	public static String basicInfoPath = "../data/";
	public static String weiboInfoPath = "../data/";
	public static String virifyImgPath = "./verifyImg/";
	
	public static void load() {
		Properties pro;
	    FileInputStream proReader = null;
		try{
			proReader = new FileInputStream("setting.ini");
			pro = new Properties();
			pro.load(proReader);
			maxBlogNum = Integer.parseInt(pro.getProperty("max_weibo_number"));
			su = pro.getProperty("weibo_user_name");
			sp = pro.getProperty("weibo_user_pwd");
			logFilePath = pro.getProperty("log_file_path");
			basicInfoPath = pro.getProperty("basic_info_file");
			weiboInfoPath = pro.getProperty("weibo_info_file");
			virifyImgPath = pro.getProperty("verify_img_path");
			
		}catch(Exception e){
			maxBlogNum = 10000;
			su = "zhoujin@mail.ustc.edu.cn";
			sp = "abcd1234!";
			logFilePath = "./logs";
			basicInfoPath = "../data/";
			weiboInfoPath = "../data/";
			virifyImgPath = "./verifyImg/";
		}		
	}
}
