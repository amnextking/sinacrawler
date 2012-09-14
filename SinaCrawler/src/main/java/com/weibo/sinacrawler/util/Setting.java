package com.weibo.sinacrawler.util;

public class Setting {	
	public static int maxBlogNum = 10000;
	public static String su = "younilunwen@163.com";
	public static String sp = "zhengcx";
	public static String logFilePath = "f:/weibo/";
	public static String basicInfoPath = "f:/weibo/";
	public static String weiboInfoPath = "f:/weibo/";
	public static String virifyImgPath = "f:/weibo/";
	
	public static void load() {
		
		maxBlogNum = 10000;
		su = "younilunwen@163.com";
//		su = "75957893";
		sp = "zhengcx";
		logFilePath = "f:/weibo/";
		basicInfoPath = "f:/weibo/";
		weiboInfoPath = "f:/weibo/";
		virifyImgPath = "f:/weibo/";
		
//		Properties pro;
//	    FileInputStream proReader = null;
//		try{
//			proReader = new FileInputStream("setting.ini");
//			pro = new Properties();
//			pro.load(proReader);
//			maxBlogNum = Integer.parseInt(pro.getProperty("max_weibo_number"));
//			su = pro.getProperty("weibo_user_name");
//			sp = pro.getProperty("weibo_user_pwd");
//			logFilePath = pro.getProperty("log_file_path");
//			basicInfoPath = pro.getProperty("basic_info_file");
//			weiboInfoPath = pro.getProperty("weibo_info_file");
//			virifyImgPath = pro.getProperty("verify_img_path");
//			
//		}catch(Exception e){
//			System.out.println("read conf error: " + e.getMessage() );
//		}
		

	}
}
