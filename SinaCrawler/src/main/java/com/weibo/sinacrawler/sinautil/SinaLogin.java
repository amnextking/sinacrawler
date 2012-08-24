package com.weibo.sinacrawler.sinautil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.weibo.sinacrawler.htmlparser.*;
import com.weibo.sinacrawler.model.*;
import com.weibo.sinacrawler.util.ParallelUtil;
import com.weibo.sinacrawler.util.Setting;

public class SinaLogin {
	private String pwd;
	private HttpClient dhc;
	private String su;
	private class MyLogHander extends Formatter { 
		@Override 
		public String format(LogRecord record) { 
			StringBuilder sb = new StringBuilder();       	
			sb.append(getCurrTime());
			sb.append("\t");
			sb.append(record.getLevel());
			sb.append(": ");
			sb.append(record.getMessage());
			sb.append("\n");
			return sb.toString(); 
		} 
	}
	public Logger log; 
	private LogManager logManger;
	private HTMLParser htmlParser;

	private SimpleDateFormat timeStampForm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static Cookie[] cookies1=null;
	
	public SinaLogin(HttpClient dhc,String account,String pwd) throws SecurityException, IOException{
		this.dhc = dhc;
		this.pwd = pwd;
		this.su = this.encodeAccount(account);
		htmlParser = new HTMLParser();		
		logManger = LogManager.getLogManager();
		logManger.reset();
		log = Logger.getLogger(account);
		SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMddHHmmss");
		FileHandler fileHandler = new FileHandler(Setting.logFilePath + "/" + dateForm.format(new Date())+".log");
		fileHandler.setFormatter(new MyLogHander()); 
		log.addHandler(fileHandler);			
	}
	
	private void downloadImg(String url,String fileName) throws IOException{
		URL imgurl=new URL(url);
		File outFile = new File(fileName);
		OutputStream os = new FileOutputStream(outFile);
		InputStream is = imgurl.openStream();
		byte[] buff = new byte[1024];
		while(true) {
			int readed = is.read(buff);
			if(readed == -1) {
				break;
			}
			byte[] temp = new byte[readed];
			System.arraycopy(buff, 0, temp, 0, readed);
			os.write(temp);
		}
		is.close(); 
		os.close();

	}
	public void reconnect(){
		this.dhc = new HttpClient();
		this.dhc.getHttpConnectionManager().getParams().setConnectionTimeout(9000000); 
		this.dhc.getHttpConnectionManager().getParams().setSoTimeout(9000000);		
	}
	public void login() throws HttpException, IOException{
		System.out.println(su);
		String preUrl="http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su="+this.su+"&client=ssologin.js(v1.3.19)";

		GetMethod getMethod = new GetMethod(preUrl);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler()); 
		getMethod.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode = dhc.executeMethod(getMethod);
		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + getMethod.getStatusLine());
		}        

		String prelogin = getMethod.getResponseBodyAsString();
		String servertime = prelogin.substring(prelogin.indexOf("servertime")+12,prelogin.indexOf("servertime")+22);
		String nonce = prelogin.substring(prelogin.indexOf("nonce")+8,prelogin.indexOf("nonce")+14);
		int startt = prelogin.indexOf("pcid");
		prelogin = prelogin.substring(startt+7);
		String pcid = prelogin.substring(0,prelogin.indexOf("\""));

		String pw = new SinaSSOEncoder().encode(this.pwd, servertime, nonce);
		//System.out.println(pcid);
		PostMethod post = new PostMethod("http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.3.19)");		
		post.addParameter("entry", "weibo");
		post.addParameter("gateway", "1");
		post.addParameter("from", "");
		post.addParameter("savestate", "7");
		post.addParameter("useticket", "1");
		post.addParameter("ssosimplelogin", "1");
		post.addParameter("vsnf", "1");
		post.addParameter("vsnval", "");
		post.addParameter("su", this.su);
		post.addParameter("service", "miniblog");
		post.addParameter("servertime", servertime);
		post.addParameter("nonce", nonce);
		post.addParameter("pwencode","wsse");
		post.addParameter("sp", pw);
		post.addParameter("encoding", "utf-8");
		post.addParameter("wvr", "4");
		post.addParameter("url", "http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack");
		post.addParameter("returntype", "META");
		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		dhc.executeMethod(post);

		String ttemp = post.getResponseBodyAsString();
		int start=ttemp.indexOf("location.replace");
		ttemp=ttemp.substring(start);
		String location=ttemp.substring(ttemp.indexOf("location.replace")+18,ttemp.indexOf("')"));
		post.releaseConnection();
		GetMethod getMethod1 = new GetMethod(location);
		getMethod1.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		getMethod1.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode1 = dhc.executeMethod(getMethod1);
		if (statusCode1 != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + getMethod1.getStatusLine());
		}					

		cookies1 = dhc.getState().getCookies();
		dhc.getState().addCookies(cookies1);
		try{
			InetAddress addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress();	
			System.out.println(ip + " login successed");
		} catch (Exception e) {

		}
	}    


	public void loginWithVerify() throws HttpException, IOException, InterruptedException{
		System.out.println(su);
		String preUrl="http://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su="+this.su+"&client=ssologin.js(v1.3.19)";

		GetMethod getMethod = new GetMethod(preUrl);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler()); 
		getMethod.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode = dhc.executeMethod(getMethod);
		if (statusCode != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + getMethod.getStatusLine());
		}        

		String prelogin = getMethod.getResponseBodyAsString();
		String servertime = prelogin.substring(prelogin.indexOf("servertime")+12,prelogin.indexOf("servertime")+22);
		String nonce = prelogin.substring(prelogin.indexOf("nonce")+8,prelogin.indexOf("nonce")+14);
		int startt = prelogin.indexOf("pcid");
		prelogin = prelogin.substring(startt+7);
		String pcid = prelogin.substring(0,prelogin.indexOf("\""));
		//System.out.println(pcid);
		String verifyURL = "http://login.sina.com.cn/cgi/pin.php?r=87545108&s=0&p=" + pcid;
		downloadImg(verifyURL, Setting.virifyImgPath + pcid + ".jpg");
		//System.out.println(verifyURL);
		String door = "";
		L1:
			while(true){
				TimeUnit.SECONDS.sleep(10);
				File imgPath = new File(Setting.virifyImgPath);
				File[] files = imgPath.listFiles();
				for(File file : files){
					String fileName = file.getName();
					String[] ss = fileName.split("\\.");
					if(ss[0].equals(pcid) && ss.length == 3){
						door = ss[2];
						break L1;
					}
				}

			}
		/*System.out.print("please input verify code: ");
		Scanner input = new Scanner(System.in);
		String door = input.next();*/
		//System.out.println(door);
		String pw = new SinaSSOEncoder().encode(this.pwd, servertime, nonce);
		//System.out.println(pcid);
		PostMethod post = new PostMethod("http://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.3.19)");		
		post.addParameter("entry", "weibo");
		post.addParameter("gateway", "1");
		post.addParameter("from", "");
		post.addParameter("savestate", "7");
		post.addParameter("useticket", "1");
		post.addParameter("ssosimplelogin", "1");
		post.addParameter("pcid", pcid);
		post.addParameter("door", door);
		post.addParameter("vsnf", "1");
		post.addParameter("vsnval", "");
		post.addParameter("su", this.su);
		post.addParameter("service", "miniblog");
		post.addParameter("servertime", servertime);
		post.addParameter("nonce", nonce);
		post.addParameter("pwencode","wsse");
		post.addParameter("sp", pw);
		post.addParameter("encoding", "utf-8");
		post.addParameter("wvr", "4");
		post.addParameter("url", "http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack");
		post.addParameter("returntype", "META");
		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		dhc.executeMethod(post);

		String ttemp = post.getResponseBodyAsString();
		//System.out.println(ttemp);
		int start=ttemp.indexOf("location.replace");
		ttemp=ttemp.substring(start);
		String location=ttemp.substring(ttemp.indexOf("location.replace")+18,ttemp.indexOf("')"));
		post.releaseConnection();
		GetMethod getMethod1 = new GetMethod(location);
		getMethod1.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		getMethod1.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode1 = dhc.executeMethod(getMethod1);
		if (statusCode1 != HttpStatus.SC_OK) {
			System.err.println("Method failed: " + getMethod1.getStatusLine());
		}					

		cookies1 = dhc.getState().getCookies();
		dhc.getState().addCookies(cookies1);
	}


	private String encodeAccount(String account){
		return new String(Base64.encodeBase64(URLEncoder.encode(account).getBytes()));
	}
	private String getCurrTime(){
		SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return tempDate.format(new Date());

	}
	public StringBuilder getURLContent(String url){
		while(true){
			GetMethod getMethod2=new GetMethod(url);
			try{				
				StringBuilder cookieInfo = new StringBuilder();
				for(Cookie cookie : cookies1){
					cookieInfo.append(cookie.toExternalForm()).append("; ");
				}
				cookieInfo.deleteCharAt(cookieInfo.length()-1);

				getMethod2.setRequestHeader("Cookie", cookieInfo.toString());	
				getMethod2.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
				getMethod2.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
				int statusCode2 = dhc.executeMethod(getMethod2);
				if (statusCode2 != HttpStatus.SC_OK) {
					throw new Exception("returned " + statusCode2 + " code,failed!");
				}
				//dhc.executeMethod(getMethod2);
				StringBuilder responseBody = new StringBuilder();  				
				BufferedReader br = new BufferedReader(new InputStreamReader(getMethod2.getResponseBodyAsStream(), "UTF-8"));
				String line = "";
				while((line = br.readLine()) != null){
					responseBody.append(line).append("\n");
				}
				//responseBody = new String(responseBody.getBytes("ISO8859-1"),"UTF-8");
				getMethod2.releaseConnection();
				return responseBody;
			} catch(Exception e){
				getMethod2.releaseConnection();
				String wrongMessage = e.getMessage();
				log.severe("get url: \"" + url + "\" failed! reason: " + wrongMessage);
				if(wrongMessage.contains("Circular redirect")){
					return new StringBuilder();
				}
				try{
					TimeUnit.SECONDS.sleep(5);
					InetAddress addr = InetAddress.getLocalHost();
					String ip = addr.getHostAddress();
					System.out.println(ip + " retry!");
					//login();
				} catch (Exception e2) {
					log.severe("relogin failed! reason: " + e2.getMessage());
				}
			}
		}
	}

	public List<String> getFollowerList(Long UserID,boolean getName){
		String url = "http://weibo.com/" +UserID + "/follow";
		ArrayList<String> resultList = new ArrayList<String>();
		StringBuilder fileContent = getURLContent(url);
		//System.out.println(fileContent);
		Pattern followNumPattern = Pattern.compile("\">��ע\\(([0-9]*?)\\)</a>",
				Pattern.CASE_INSENSITIVE);
		Matcher followNumMatcher = followNumPattern.matcher(fileContent);
		Pattern followPattern = Pattern.compile("itmeClick\\\\\" action-data=\\\\\"uid=([0-9]*?)&fnick=(.*?)\\\\\">",
				Pattern.CASE_INSENSITIVE);	
		Pattern followPattern2 = Pattern.compile("<span class=\"name\">.*?uid=\"([0-9]*?)\"\\snamecard=\"true\"\\stitle=\"(.*?)\"",
				Pattern.CASE_INSENSITIVE);	
		int pages = 0;
		if(followNumMatcher.find()){
			//System.out.println(pages);
			Integer fansNum = Integer.parseInt(followNumMatcher.group(1).trim());	
			pages = fansNum / 20 + 1;
		}else {
			return resultList;
		}
		pages = Math.min(pages,50);
		for(int i = 1; i <= pages; i++){
			String pageURL = "http://weibo.com/" + UserID + "/follow?page=" + i;
			StringBuilder pageContent = getURLContent(pageURL);
			Matcher followMatcher = followPattern.matcher(pageContent);
			while(followMatcher.find()){
				String followerID = followMatcher.group(1);
				String followerName = htmlParser.UnicodeToString(followMatcher.group(2));
				if(getName)
					resultList.add(followerName);
				else
					resultList.add(followerID);
			}
			Matcher followMatcher2 = followPattern2.matcher(pageContent);
			while(followMatcher2.find()){
				String followerID = followMatcher2.group(1);
				String followerName = htmlParser.UnicodeToString(followMatcher2.group(2));
				if(getName)
					resultList.add(followerName);
				else
					resultList.add(followerID);
			}
		}
		return resultList;		
	}



	public List<Long> getfansList(Long UserID,int pageNum){
		String url = "http://weibo.com/" +UserID + "/fans";
		ArrayList<Long> resultList = new ArrayList<Long>();
		StringBuilder fileContent = getURLContent(url);
		Pattern fansNumPattern = Pattern.compile("\">��˿\\((.*?)\\)</a>",
				Pattern.CASE_INSENSITIVE);
		Matcher fansNumMatcher = fansNumPattern.matcher(fileContent);
		Pattern fanPattern = Pattern.compile("itmeClick\\\\\" action-data=\\\\\"uid=([0-9]*?)&fnick",
				Pattern.CASE_INSENSITIVE);
		int pages = 0;
		if(fansNumMatcher.find()){
			Integer fansNum = Integer.parseInt(fansNumMatcher.group(1).trim());	
			pages = fansNum / 20 + 1;
		}else {
			return resultList;
		}
		if(pageNum > 0)
			pages = Math.min(pages,pageNum);

		for(int i = 1; i <= pages; i++){
			String pageURL = "http://weibo.com/" + UserID + "/fans?page=" + i;
			StringBuilder pageContent = getURLContent(pageURL);
			Matcher fanMatcher = fanPattern.matcher(pageContent);
			while(fanMatcher.find()){
				Long fanID = Long.parseLong(fanMatcher.group(1));
				resultList.add(fanID);
			}

		}
		return resultList;	

	}

	public UserWeiboInfo getUserInfo(Long userID,Date timestamp) throws ParserException{
		int page = 1;
		UserWeiboInfo userWeiboInfo = new UserWeiboInfo();
		String url = "";
		StringBuilder urlContent = new StringBuilder();
		int retry = 0;
		int lastBlogSize = 0;
		while(userWeiboInfo.getMicroBlogList().size() < Setting.maxBlogNum){
			lastBlogSize = userWeiboInfo.getMicroBlogList().size();
			url = "http://weibo.com/" + userID + "?page=" + page;
			urlContent = getURLContent(url);
			if(page == 1){				
				userWeiboInfo.setArea(htmlParser.getUserArea(urlContent));
				userWeiboInfo.setBlogNum(htmlParser.getBlogNum(urlContent));
				userWeiboInfo.setBriefIntro(htmlParser.getBriefIntro(urlContent));
				userWeiboInfo.setCollege(htmlParser.getUserCollege(urlContent));
				userWeiboInfo.setCompany(htmlParser.getUserCompany(urlContent));
				userWeiboInfo.setFansNum(htmlParser.getFansNum(urlContent));
				userWeiboInfo.setFollowNum(htmlParser.getFollowNum(urlContent));
				userWeiboInfo.setInterest(htmlParser.getUserInterest(urlContent));
				userWeiboInfo.setIsVIP(htmlParser.isVip(urlContent));
				userWeiboInfo.setSex(htmlParser.getUserGender(urlContent));
				userWeiboInfo.setTopics(htmlParser.getUserTopics(urlContent));
				userWeiboInfo.setTags(htmlParser.getUserTags(urlContent));
				userWeiboInfo.setUserID(htmlParser.getUserID(urlContent));
				userWeiboInfo.setUserName(htmlParser.getUserName(urlContent));
				userWeiboInfo.setUpdateTime(timeStampForm.format(new Date()));				
			}
			ArrayList<MircoBlogInfo> blogList = null;
			try{
				blogList = htmlParser.getBlogList(urlContent);
			} catch (Exception e) {
				if(retry < 1){
					retry++;
					continue;
				}								
			}
			retry = 0;
			/*if(blogList.size() < 1){
				break;
			}*/
			userWeiboInfo.getMicroBlogList().addAll(blogList);
			//该页中间15条微博
			url = url + "&pre_page=" + page;
			urlContent = getURLContent(url);			
			try{
				blogList = htmlParser.getBlogList(urlContent);
			} catch (Exception e) {
				if(retry < 1){
					retry++;
					continue;
				}								
			}
			retry = 0;			
			/*if(blogList.size() < 1){
				break;
			}*/
			userWeiboInfo.getMicroBlogList().addAll(blogList);
			//该页最后15条微博
			url = url + "&pagebar=1";
			urlContent = getURLContent(url);
			try{
				blogList = htmlParser.getBlogList(urlContent);
			} catch (Exception e) {
				if(retry < 1){
					retry++;
					continue;
				}								
			}
			retry = 0;
			/*if(blogList.size() < 1){
				break;
			}*/
			userWeiboInfo.getMicroBlogList().addAll(blogList);
			if(lastBlogSize == userWeiboInfo.getMicroBlogList().size()){
				break;
			}
			page++;
		}

		return userWeiboInfo;
	}

	public UserWeiboInfo getUserBasicInfo(Long userID,Date timestamp) throws ParserException{

		UserWeiboInfo userWeiboInfo = new UserWeiboInfo();
		String url = "http://weibo.com/" + userID;
		StringBuilder urlContent = getURLContent(url);	
		try{
			userWeiboInfo.setArea(htmlParser.getUserArea(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setBlogNum(htmlParser.getBlogNum(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setBriefIntro(htmlParser.getBriefIntro(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setCollege(htmlParser.getUserCollege(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setCompany(htmlParser.getUserCompany(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setFansNum(htmlParser.getFansNum(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setFollowNum(htmlParser.getFollowNum(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setInterest(htmlParser.getUserInterest(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setIsVIP(htmlParser.isVip(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setSex(htmlParser.getUserGender(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setTopics(htmlParser.getUserTopics(urlContent));
		} catch (Exception e) {
		}
		try{
			userWeiboInfo.setTags(htmlParser.getUserTags(urlContent));
		} catch (Exception e) {
		}
		userWeiboInfo.setUserID(htmlParser.getUserID(urlContent));
		userWeiboInfo.setUserName(htmlParser.getUserName(urlContent));
		userWeiboInfo.setUpdateTime(timeStampForm.format(new Date()));				

		return userWeiboInfo;
	}

	public void addUserByKeyword(String keyword,String othertype) throws SQLException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException{		
		ParallelUtil parallelUtil = new ParallelUtil();
		String urlKeyword = URLEncoder.encode(keyword, "UTF-8");		
		/*String url = "http://s.weibo.com/weibo/" + urlKeyword + "&page=1";
		String urlContent = getURLContent(url);
		String pageSuffix = htmlParser.getPageLink(urlContent);
		ArrayList<Long> resultList = htmlParser.getkeywordUsers(urlContent);
		if(resultList.size() == 0)
			return;
		for(Long userID : resultList){
			parallelUtil.insertMysql(userID, 1, keyword);
		}*/
		int retry = 0;
		int page = 0;
		while(page <50){
			page++;
			String url = "http://s.weibo.com/weibo/" + urlKeyword + "&page=" + page + othertype;
			System.out.println(url);
			StringBuilder urlContent = getURLContent(url);
			ArrayList<Long> resultList = htmlParser.getkeywordUsers(urlContent);
			System.out.println(resultList.size());
			if(resultList.size() == 0 && retry == 0){
				page--;
				retry = 1;
				TimeUnit.SECONDS.sleep(10);
				continue;
			} else if(resultList.size() == 0 && retry == 1){
				return;				
			} else{
				retry = 0;
			}
			for(Long userID : resultList){
				parallelUtil.insertMysql(userID, 1, keyword);
			}	
			TimeUnit.SECONDS.sleep(10);
		}		
	}

	public void addUserByKeywordArea(String keyword,int provinceCode,int cityCode,String otherType) throws SQLException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException{		
		ParallelUtil parallelUtil = new ParallelUtil();
		String urlKeyword = URLEncoder.encode(keyword, "UTF-8");		
		/*String url = "http://s.weibo.com/weibo/" + urlKeyword + "&page=1";
		String urlContent = getURLContent(url);
		String pageSuffix = htmlParser.getPageLink(urlContent);
		ArrayList<Long> resultList = htmlParser.getkeywordUsers(urlContent);
		if(resultList.size() == 0)
			return;
		for(Long userID : resultList){
			parallelUtil.insertMysql(userID, 1, keyword);
		}*/
		int retry = 0;
		int page = 0;
		while(page <50){
			page++;
			String url = "http://s.weibo.com/weibo/" + urlKeyword + "&page=" + page + "&region=custom:" + provinceCode + ":" + cityCode + otherType;
			System.out.println(url);
			StringBuilder urlContent = getURLContent(url);
			ArrayList<Long> resultList = htmlParser.getkeywordUsers(urlContent);
			System.out.println(resultList.size());
			if(resultList.size() == 0 && retry == 0){
				page--;
				retry = 1;
				TimeUnit.SECONDS.sleep(10);
				continue;
			} else if(resultList.size() == 0 && retry == 1){
				return;				
			} else{
				retry = 0;
			}
			for(Long userID : resultList){
				parallelUtil.insertMysql(userID, 1, keyword);
			}	
			TimeUnit.SECONDS.sleep(10);
		}		
	}

	public void addUserByTag(String tag) throws SQLException, ClassNotFoundException, UnsupportedEncodingException, InterruptedException{		
		ParallelUtil parallelUtil = new ParallelUtil();
		String urlTag = URLEncoder.encode(tag, "UTF-8");	
		int retry = 0;
		int page = 0;

		while(page <50){
			page++;
			String url = "http://s.weibo.com/user/&tag=" + urlTag + "&page=" + page;
			System.out.println(url);
			StringBuilder urlContent = getURLContent(url);
			ArrayList<Long> resultList = htmlParser.getTagUsers(urlContent);
			System.out.println(resultList.size());
			if(resultList.size() == 0 && retry == 0){
				page--;
				retry = 1;
				TimeUnit.SECONDS.sleep(10);
				continue;
			} else if(resultList.size() == 0 && retry == 1){
				break;				
			} else{
				retry = 0;
			}
			for(Long userID : resultList){
				parallelUtil.insertMysql(userID, 2, tag);
			}	
			TimeUnit.SECONDS.sleep(10);
		}
		retry = 0;
		page = 0;
		while(page <50){
			page++;
			String url = "http://s.weibo.com/user/&tag=" + urlTag + "&auth=ord&page=" + page;
			System.out.println(url);
			StringBuilder urlContent = getURLContent(url);
			ArrayList<Long> resultList = htmlParser.getTagUsers(urlContent);
			System.out.println(resultList.size());
			if(resultList.size() == 0 && retry == 0){
				page--;
				retry = 1;
				TimeUnit.SECONDS.sleep(10);
				continue;
			} else if(resultList.size() == 0 && retry == 1){
				break;				
			} else{
				retry = 0;
			}
			for(Long userID : resultList){
				parallelUtil.insertMysql(userID, 2, tag);
			}	
			TimeUnit.SECONDS.sleep(10);
		}
	}

	public void addUserByQun(String qunID) throws SQLException, ClassNotFoundException, HttpException, IOException{
		ParallelUtil parallelUtil = new ParallelUtil();
		String url = "http://q.weibo.com/" + qunID + "/members/all";
		StringBuilder urlContent = getURLContent(url);		
		int pageNum = htmlParser.getQunPagesNum(urlContent);
		ArrayList<Long> userList = htmlParser.getQunUsers(urlContent);
		for(Long userID : userList){
			parallelUtil.insertMysql(userID, 5, qunID);
		}
		System.out.println("��ҳ��: " + pageNum);
		for(int page = 2; page <= pageNum; page++){

			PostMethod post = new PostMethod("http://q.weibo.com/ajax/members/page");
			post.addRequestHeader("X-Requested-With", "XMLHttpRequest");
			StringBuilder cookieInfo = new StringBuilder();
			for(Cookie cookie : cookies1){
				cookieInfo.append(cookie.toExternalForm()).append("; ");
			}
			cookieInfo.deleteCharAt(cookieInfo.length()-1);
			post.addRequestHeader("Cookie", cookieInfo.toString());			
			post.addRequestHeader("Referer", "http://q.weibo.com/" + qunID + "/members/all");
			post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			post.addParameter("page", String.valueOf(page));
			post.addParameter("gid", qunID);
			post.addParameter("tab", "all");
			post.addParameter("_t", "0");
			DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
			post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
			int statusCode2 = dhc.executeMethod(post);
			String ttemp = post.getResponseBodyAsString();
			urlContent = new StringBuilder(ttemp.replaceAll("\\\\", ""));
			userList = htmlParser.getQunUsers(urlContent);
			for(Long userID : userList){
				parallelUtil.insertMysql(userID, 5, qunID);
			}
		}
	}

	public String getLatestWeiboID(Long userID){
		try{
			String url = "http://weibo.com/" + userID;
			StringBuilder urlContent = getURLContent(url);
			ArrayList<MircoBlogInfo> blogList = htmlParser.getBlogList(urlContent);
			if(blogList.size() == 0)
				return "";
			String weiboID = blogList.get(0).getWeiboID();
			return weiboID;
		} catch (Exception e) {
			return "";
		}
	}

	public void changeWeiboVersion() throws HttpException, IOException{
		PostMethod post = new PostMethod("http://account.weibo.com/aj4/person/set_version.php");
		StringBuilder cookieInfo = new StringBuilder();
		for(Cookie cookie : cookies1){
			cookieInfo.append(cookie.toExternalForm()).append("; ");
		}
		cookieInfo.deleteCharAt(cookieInfo.length()-1);
		post.addRequestHeader("Cookie", cookieInfo.toString());			
		post.addRequestHeader("Referer", "http://account.weibo.com/set/version?topnav=1&wvr=3.6");
		post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		post.addParameter("version", "4");
		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		int statusCode2 = dhc.executeMethod(post);
	}

	public void repost() throws HttpException, IOException{


		PostMethod post = new PostMethod("http://weibo.com/aj/mblog/forward?__rnd=24023453334");
		//post.addRequestHeader("Cookie", cookies1[0].toExternalForm()+"; "+cookies1[1].toExternalForm()+"; "+cookies1[2].toExternalForm()+"; "+cookies1[3].toExternalForm()+"; "+cookies1[4].toExternalForm()+"; "+cookies1[5].toExternalForm()+"; "+cookies1[6].toExternalForm()+"; "+cookies1[7].toExternalForm()+"; "+cookies1[8].toExternalForm()+"; "+cookies1[9].toExternalForm()+"; ="+cookies1[10].toExternalForm()+"; ="+cookies1[11].toExternalForm()+"; "+cookies1[12].toExternalForm()+"; "+cookies1[13].toExternalForm()+"; "+cookies1[14].toExternalForm());
		post.addRequestHeader("X-Requested-With", "XMLHttpRequest");

		StringBuilder cookieInfo = new StringBuilder();
		for(Cookie cookie : cookies1){
			cookieInfo.append(cookie.toExternalForm()).append("; ");
		}
		cookieInfo.deleteCharAt(cookieInfo.length()-1);
		post.addRequestHeader("Cookie", cookieInfo.toString());			
		post.addRequestHeader("Referer", "http://weibo.com/");
		post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		post.addParameter("appkey", "");
		post.addParameter("mid", "3443372079492385");
		post.addParameter("style_type", "1");
		post.addParameter("reason", "test3");
		post.addParameter("location", "home");
		post.addParameter("module", "tranlayout");
		post.addParameter("group_source", "group_all");
		post.addParameter("_t", "0");

		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);

		int statusCode2 = dhc.executeMethod(post);
		System.out.println(statusCode2);
		String ttemp = post.getResponseBodyAsString();

	}

	public PostReturnInfo comment(String weiboID, String content) throws HttpException, IOException, InterruptedException{
		while(true){
			PostMethod post = new PostMethod("http://weibo.com/aj/comment/add?__rnd=1336460214231");
			try{
				PostReturnInfo postReturnInfo = new PostReturnInfo();		
				post.addRequestHeader("X-Requested-With", "XMLHttpRequest");


				StringBuilder cookieInfo = new StringBuilder();
				for(Cookie cookie : cookies1){
					cookieInfo.append(cookie.toExternalForm()).append("; ");
				}
				cookieInfo.deleteCharAt(cookieInfo.length()-1);
				//System.out.println(cookieInfo);
				post.addRequestHeader("Cookie", cookieInfo.toString());		
				post.addRequestHeader("Referer", "http://weibo.com/");
				post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				post.addParameter("act", "post");
				post.addParameter("mid", weiboID);
				//post.addParameter("uid", "2794580700");
				post.addParameter("forward", "0");
				post.addParameter("isroot", "0");

				post.addParameter("content", content);
				post.addParameter("location", "home");
				post.addParameter("module", "scommlist");
				post.addParameter("group_source", "group_all");
				post.addParameter("_t", "0");

				DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
				post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);

				int statusCode2 = dhc.executeMethod(post);
				String ttemp = post.getResponseBodyAsString();
				//System.out.println(ttemp);
				Pattern idpattern = Pattern.compile("\"code\":\"([0-9]*?)\"");
				Matcher idmatcher = idpattern.matcher(ttemp);
				if(idmatcher.find()){
					postReturnInfo.setReturnCode(Integer.parseInt(idmatcher.group(1)));
				}
				Pattern msgpattern = Pattern.compile("\"msg\":\"(.*?)\"");
				Matcher msgmatcher = msgpattern.matcher(ttemp);
				if(msgmatcher.find()){
					postReturnInfo.setReturnMsg(htmlParser.UnicodeToString(msgmatcher.group(1)));
				}
				post.releaseConnection();
				return postReturnInfo;
			} catch (Exception e) {
				post.releaseConnection();
				System.out.println(e.getMessage());
				TimeUnit.SECONDS.sleep(5);
			}
		}
	}

	public PostReturnInfo mail(String userName, String content) throws HttpException, IOException, InterruptedException{
		while(true){
			PostMethod post = new PostMethod("http://weibo.com/aj/message/add?__rnd=1336462498132");
			try{
				PostReturnInfo postReturnInfo = new PostReturnInfo();		
				post.addRequestHeader("X-Requested-With", "XMLHttpRequest");

				StringBuilder cookieInfo = new StringBuilder();
				for(Cookie cookie : cookies1){
					cookieInfo.append(cookie.toExternalForm()).append("; ");
				}
				cookieInfo.deleteCharAt(cookieInfo.length()-1);
				post.addRequestHeader("Cookie", cookieInfo.toString());	

				post.addRequestHeader("Referer", "http://weibo.com/");
				post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				post.addParameter("text", content);
				post.addParameter("screen_name", userName);
				//post.addParameter("uid", "2794580700");
				post.addParameter("id", "0");
				post.addParameter("fids", "");
				post.addParameter("touid", "0");
				post.addParameter("style_id", "2");
				post.addParameter("location", "msglist");
				post.addParameter("module", "msglayout");
				post.addParameter("_t", "0");

				DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
				post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);

				int statusCode2 = dhc.executeMethod(post);

				String ttemp = post.getResponseBodyAsString();
				Pattern idpattern = Pattern.compile("\"code\":\"([0-9]*?)\"");
				Matcher idmatcher = idpattern.matcher(ttemp);
				if(idmatcher.find()){
					postReturnInfo.setReturnCode(Integer.parseInt(idmatcher.group(1)));
				}
				Pattern msgpattern = Pattern.compile("\"msg\":\"(.*?)\"");
				Matcher msgmatcher = msgpattern.matcher(ttemp);
				if(msgmatcher.find()){
					postReturnInfo.setReturnMsg(htmlParser.UnicodeToString(msgmatcher.group(1)));
				}
				post.releaseConnection();
				return postReturnInfo;
			} catch (Exception e) {
				post.releaseConnection();
				System.out.println(e.getMessage());
				TimeUnit.SECONDS.sleep(5);
			}
		}
	}

	public PostReturnInfo postBlog(String content,boolean isFace) throws HttpException, IOException{
		PostReturnInfo postReturnInfo = new PostReturnInfo();
		PostMethod post = new PostMethod("http://weibo.com/aj/mblog/add?__rnd=1341207207570");
		post.addRequestHeader("X-Requested-With", "XMLHttpRequest");
		StringBuilder cookieInfo = new StringBuilder();
		for(Cookie cookie : cookies1){
			cookieInfo.append(cookie.toExternalForm()).append("; ");
		}
		cookieInfo.deleteCharAt(cookieInfo.length()-1);
		post.addRequestHeader("Cookie", cookieInfo.toString());	

		post.addRequestHeader("Referer", "http://weibo.com/");
		post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		post.addParameter("text", content);
		post.addParameter("pic_id", "");
		post.addParameter("rank", "");
		String face = "";
		if(isFace)
			face = "face";
		post.addParameter("_surl", face);
		post.addParameter("location", "home");
		post.addParameter("module", "");
		post.addParameter("_t", "0");
		DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
		post.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);

		int statusCode2 = dhc.executeMethod(post);
		String ttemp = post.getResponseBodyAsString();
		Pattern idpattern = Pattern.compile("\"code\":\"([0-9]*?)\"");
		Matcher idmatcher = idpattern.matcher(ttemp);
		if(idmatcher.find()){
			postReturnInfo.setReturnCode(Integer.parseInt(idmatcher.group(1)));
		}
		Pattern msgpattern = Pattern.compile("\"msg\":\"(.*?)\"");
		Matcher msgmatcher = msgpattern.matcher(ttemp);
		if(msgmatcher.find()){
			postReturnInfo.setReturnMsg(htmlParser.UnicodeToString(msgmatcher.group(1)));
		}
		post.releaseConnection();
		return postReturnInfo;
	}

	public ArrayList<FarwardUserInfo> getAllFarwardUserInfo(String farwardURL){
		ArrayList<FarwardUserInfo> result = new ArrayList<FarwardUserInfo>();
		StringBuilder urlContent = getURLContent(farwardURL);
		FarwardUserInfo originalUserInfo = htmlParser.getOriginalFarwardUserInfo(urlContent);
		result.add(originalUserInfo);
		ArrayList<FarwardUserInfo> farwardUserInfoList = htmlParser.getFarwardUserInfo(urlContent);
		result.addAll(farwardUserInfoList);
		int pageNum = htmlParser.getPagesNum(urlContent);
		if(pageNum == 0){
			return result;
		}else{
			for(int i = 2; i <= pageNum; i++){
				String url = farwardURL + "&page=" + i;
				urlContent = getURLContent(url);
				farwardUserInfoList = htmlParser.getFarwardUserInfo(urlContent);
				result.addAll(farwardUserInfoList);
			}
		}
		return result;
	}

	public ArrayList<FarwardUserInfo> getAllCommentUserInfo(String commentURL){
		ArrayList<FarwardUserInfo> result = new ArrayList<FarwardUserInfo>();
		StringBuilder urlContent = getURLContent(commentURL);
		ArrayList<FarwardUserInfo> commentUserInfoList = htmlParser.getCommentUserInfo(urlContent);
		result.addAll(commentUserInfoList);
		int pageNum = htmlParser.getPagesNum(urlContent);
		if(pageNum == 0){
			return result;
		}else{
			for(int i = 2; i <= pageNum; i++){
				String url = commentURL + "&page=" + i;
				urlContent = getURLContent(url);
				commentUserInfoList = htmlParser.getCommentUserInfo(urlContent);
				result.addAll(commentUserInfoList);
			}
		}
		return result;
	}

	public UserWeiboInfo getUserInfoRaw(Long userID,Date timestamp) throws ParserException{
		int page = 1;
		UserWeiboInfo userWeiboInfo = new UserWeiboInfo();
		String url = "";
		StringBuilder urlContent = new StringBuilder();
		int lastBlogSize = 0;
		while(userWeiboInfo.getMicroBlogList().size() < Setting.maxBlogNum){
			lastBlogSize = userWeiboInfo.getMicroBlogList().size();
			url = "http://e.weibo.com/" + userID + "?page=" + page;
			System.out.println(url);
			urlContent = getURLContent(url);
			if(page == 1){				
				//userWeiboInfo.area = htmlParser.getUserArea(urlContent);
				//userWeiboInfo.BlogNum = htmlParser.getBlogNum(urlContent);
				//userWeiboInfo.briefIntro = htmlParser.getBriefIntro(urlContent);
				//userWeiboInfo.college = htmlParser.getUserCollege(urlContent);
				//userWeiboInfo.company = htmlParser.getUserCompany(urlContent);
				//userWeiboInfo.fansNum = htmlParser.getFansNum(urlContent);
				//userWeiboInfo.followNum = htmlParser.getFollowNum(urlContent);
				//userWeiboInfo.interest = htmlParser.getUserInterest(urlContent);
				//userWeiboInfo.isVIP = htmlParser.isVip(urlContent);
				//userWeiboInfo.sex = htmlParser.getUserGender(urlContent);
				//userWeiboInfo.topics = htmlParser.getUserTopics(urlContent);
				//userWeiboInfo.tags = htmlParser.getUserTags(urlContent);
				userWeiboInfo.setUserID(htmlParser.getUserID(urlContent));
				userWeiboInfo.setUserName(htmlParser.getUserName(urlContent));
				//userWeiboInfo.updateTime = timeStampForm.format(new Date());				
			}
			ArrayList<MircoBlogInfo> blogList = new ArrayList<MircoBlogInfo>();
			try{
				blogList = htmlParser.getBlogList(urlContent);

			}catch (Exception e) {

			}
			userWeiboInfo.getMicroBlogList().addAll(blogList);
			//该页中间15条微博
			url = url + "&pre_page=" + page;
			urlContent = getURLContent(url);
			blogList = new ArrayList<MircoBlogInfo>();
			try{
				blogList = htmlParser.getBlogList(urlContent);

			} catch (Exception e) {
			}
			userWeiboInfo.getMicroBlogList().addAll(blogList);
			//该页最后15条微博
			url = url + "&pagebar=1";
			urlContent = getURLContent(url);
			blogList = new ArrayList<MircoBlogInfo>();
			try{
				blogList = htmlParser.getBlogList(urlContent);

			} catch (Exception e) {
			}
			userWeiboInfo.getMicroBlogList().addAll(blogList);
			if(lastBlogSize == userWeiboInfo.getMicroBlogList().size()){
				break;
			}
			page++;
		}

		return userWeiboInfo;
	}

}
