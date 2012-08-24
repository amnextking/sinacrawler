package com.weibo.sinacrawler.htmlparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.weibo.sinacrawler.model.*;


public class HTMLParser {
	public static String FarwardContentFilter(String str){
		Pattern p1 = Pattern.compile("(<a\\s.*?>)|(<\\\\/a>)|(<img\\ssrc.*?\\\\/>)");
		Matcher m1 = p1.matcher(str); 
		return m1.replaceAll(" ");
	}

	public String htmlTagsFilter(String str){
		Pattern p1 = Pattern.compile("(<.*?>)");
		Matcher m1 = p1.matcher(str); 
		return m1.replaceAll("");
	}

	public Long getUserID(StringBuilder urlContent) throws ParserException{
		Pattern pattern = Pattern.compile("\\$CONFIG\\['oid'\\]\\s=\\s'([0-9]*?)'",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(urlContent);
		if(matcher.find()){
			Long userID = Long.parseLong(matcher.group(1));
			return userID;
		}else{
			throw new ParserException("parse UserID failed!");
		}
	}

	public String getUserName(StringBuilder urlContent) throws ParserException{
		Pattern pattern = Pattern.compile("\\$CONFIG\\['onick'\\]\\s=\\s'(.*?)'");
		Matcher matcher = pattern.matcher(urlContent);
		if(matcher.find()){
			String userName = matcher.group(1);
			return userName;
		}else{
			throw new ParserException("parse UserName failed!");
		}		
	}

	public Boolean isVip(StringBuilder urlContent){
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_hisPersonalInfo\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);

		String subContent = urlContent.substring(beginIndex, endIndex);

		String vipTag = "\\u65b0\\u6d6a\\u4e2a\\u4eba\\u8ba4\\u8bc1";
		if(subContent.contains(vipTag)){
			return true;
		}else{
			return false;
		}

	}

	public Character getUserGender(StringBuilder urlContent) throws ParserException{
		Pattern pattern = Pattern.compile("\\$CONFIG\\['ogender'\\]\\s=\\s'(.)'");
		Matcher matcher = pattern.matcher(urlContent);
		if(matcher.find()){
			Character userName = matcher.group(1).charAt(0);
			return userName;
		}else{
			throw new ParserException("parse UserName failed!");
		}	
	}

	public String getUserInterest(StringBuilder urlContent){
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_litePersonInfo\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);
		String subContent = urlContent.substring(beginIndex, endIndex);

		int beginIndex2 = subContent.indexOf("\\u5174\\u8da3\\uff1a");
		if(beginIndex2 < 0)
			return "";
		int endIndex2 = subContent.indexOf("<\\/dd>", beginIndex2);
		String interestContent = subContent.substring(beginIndex2, endIndex2);
		Pattern pattern = Pattern.compile("<a\\shref=.*?>(.*?)<\\\\/a>");
		Matcher matcher = pattern.matcher(interestContent);
		StringBuilder sb = new StringBuilder();
		while(matcher.find()){
			sb.append(UnicodeToString(matcher.group(1))).append("/"); 
		}
		return sb.toString();
	}

	public String getUserArea(StringBuilder urlContent) throws ParserException{
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_hisPersonalInfo\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);

		String subContent = urlContent.substring(beginIndex, endIndex);
		Pattern pattern = Pattern.compile("<p\\sclass=\\\\\"info\\\\\">.*&nbsp;(.*?)\\\\n");
		Matcher matcher = pattern.matcher(subContent);
		if(matcher.find()){
			String area = UnicodeToString(matcher.group(1));
			return area;
		}else{
			throw new ParserException("parse user area failed!");
		}

	}

	public String getUserCompany(StringBuilder urlContent){
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_hisPersonalInfo\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);

		String subContent = urlContent.substring(beginIndex, endIndex);

		Pattern pattern = Pattern.compile("class=\\\\\"W_vline\\\\\">\\|<\\\\/i>\\\\u516c\\\\u53f8\\\\uff1a.*?title=\\\\\"(.*?)\\\\\">");
		Matcher matcher = pattern.matcher(subContent);
		if(matcher.find()){
			String company = UnicodeToString(matcher.group(1));
			return company;
		}else{
			return "";
		}		
	}

	public String getUserCollege(StringBuilder urlContent){
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_hisPersonalInfo\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);

		String subContent = urlContent.substring(beginIndex, endIndex);

		Pattern pattern = Pattern.compile("class=\\\\\"W_vline\\\\\">\\|<\\\\/i>\\\\u5927\\\\u5b66\\\\uff1a.*?title=\\\\\"(.*?)\\\\\">");
		Matcher matcher = pattern.matcher(subContent);
		if(matcher.find()){
			String college = UnicodeToString(matcher.group(1));
			return college;
		}else{
			return "";
		}		
	}

	public String getBriefIntro(StringBuilder urlContent) throws ParserException{
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_hisPersonalInfo\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);

		String subContent = urlContent.substring(beginIndex, endIndex);

		Pattern pattern = Pattern.compile("class=\\\\\"text\\\\\">\\\\u7b80\\\\u4ecb\\\\uff1a(.*?)<a");
		Matcher matcher = pattern.matcher(subContent);
		if(matcher.find()){
			String briefIntro = UnicodeToString(matcher.group(1));
			return briefIntro;
		}else{
			throw new ParserException("parse user brief introduction failed!");
		}

	}

	public String getUserTopics(StringBuilder urlContent){
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_topic\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);
		String subContent = urlContent.substring(beginIndex, endIndex);
		Pattern pattern = Pattern.compile("<a.*?\">(.*?)<\\\\/a>");
		Matcher matcher = pattern.matcher(subContent);
		StringBuilder sb = new StringBuilder();
		while(matcher.find()){
			sb.append(UnicodeToString(matcher.group(1))).append("/");
		}
		return sb.toString();
	}


	public String getUserTags(StringBuilder urlContent){
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_hisTags\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);
		String subContent = urlContent.substring(beginIndex, endIndex);

		Pattern pattern = Pattern.compile("<span><a.*?\\\\\">(.*?)<\\\\/a>");
		Matcher matcher = pattern.matcher(subContent);

		StringBuilder sb = new StringBuilder();
		while(matcher.find()){
			sb.append(UnicodeToString(matcher.group(1))).append("/");
		}
		return sb.toString();
	}

	public Integer getFollowNum(StringBuilder urlContent) throws ParserException{
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_litePersonInfo\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);
		String subContent = urlContent.substring(beginIndex, endIndex);
		Pattern pattern = Pattern.compile("<strong\\snode-type=\\\\\"follow\\\\\">([0-9]*?)<\\\\/strong>");
		Matcher matcher = pattern.matcher(subContent);

		if(matcher.find()){
			Integer followNum = Integer.parseInt(matcher.group(1));
			return followNum;
		}else{
			throw new ParserException("parse user follow number failed!");
		}		
	}

	public Integer getFansNum(StringBuilder urlContent) throws ParserException{
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_litePersonInfo\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);
		String subContent = urlContent.substring(beginIndex, endIndex);
		Pattern pattern = Pattern.compile("<strong\\snode-type=\\\\\"fans\\\\\">([0-9]*?)<\\\\/strong>");
		Matcher matcher = pattern.matcher(subContent);

		if(matcher.find()){
			Integer fansNum = Integer.parseInt(matcher.group(1));
			return fansNum;
		}else{
			throw new ParserException("parse user fans number failed!");
		}		
	}

	public Integer getBlogNum(StringBuilder urlContent) throws ParserException{
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_litePersonInfo\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);
		String subContent = urlContent.substring(beginIndex, endIndex);
		Pattern pattern = Pattern.compile("<strong\\snode-type=\\\\\"weibo\\\\\">([0-9]*?)<\\\\/strong>");
		Matcher matcher = pattern.matcher(subContent);

		if(matcher.find()){
			Integer blogNum = Integer.parseInt(matcher.group(1));
			return blogNum;
		}else{
			throw new ParserException("parse user fans number failed!");
		}		
	}

	public ArrayList<MircoBlogInfo> getBlogList(StringBuilder urlContent){
		ArrayList<MircoBlogInfo> resultList = new ArrayList<MircoBlogInfo>();
		int beginIndex = urlContent.indexOf("\"pid\":\"pl_content_hisFeed\"");
		int endIndex = urlContent.indexOf("\n", beginIndex);
		String subContent = urlContent.substring(beginIndex, endIndex);
		Pattern feedItemPattern = Pattern.compile("<dl\\saction-type=\\\\\"feed_list_item\\\\\"(.*?)<dd\\sclass=\\\\\"clear\\\\\"><\\\\/dd>");
		Matcher feedItemMatcher = feedItemPattern.matcher(subContent);
		while(feedItemMatcher.find()){
			MircoBlogInfo blogInfo = new MircoBlogInfo();

			String feedItemString = feedItemMatcher.group(1);
			Pattern feedIDPattern = Pattern.compile("mid=\\\\\"([0-9]*?)\\\\\"");
			Matcher feedIDMatcher = feedIDPattern.matcher(feedItemString);
			if(feedIDMatcher.find()){
				blogInfo.setWeiboID(feedIDMatcher.group(1));
			}
			Pattern weiboURLPattern = Pattern.compile("feed_list_item_date.*?href=\\\\\"(.*?)\\\\\"");
			Matcher weiboURLMatcher = weiboURLPattern.matcher(feedItemString);
			if(weiboURLMatcher.find()){

				blogInfo.setWeiboURL(weiboURLMatcher.group(1).replaceAll("\\\\", ""));
			}
			//解析自己发表的内容
			Pattern selfCotentPattern = Pattern.compile("<p\\snode-type=\\\\\"feed_list_content\\\\\"(.*?)\\\\/p>");			
			Matcher selfCotentMatcher = selfCotentPattern.matcher(feedItemString);
			if(selfCotentMatcher.find()){
				String selfContent = selfCotentMatcher.group(1);
				Pattern sentencePattern = Pattern.compile(">(.*?)<");
				Matcher sentenceMatcher = sentencePattern.matcher(selfContent);
				StringBuilder sb = new StringBuilder();
				while(sentenceMatcher.find()){
					String sentence = sentenceMatcher.group(1);
					if(!sentence.contains("@")){
						sb.append(sentence).append(" ");
					}
				}
				blogInfo.setSelfContent(UnicodeToString(sb.toString()));
			}
			//解析转发微博的内容
			Pattern forwardCotentPattern = Pattern.compile("<dt\\snode-type=\\\\\"feed_list_forwardContent\\\\\">(.*?)<\\\\/dt>");			
			Matcher forwardCotentMatcher = forwardCotentPattern.matcher(feedItemString);
			if(forwardCotentMatcher.find()){
				String forwardContent1 = forwardCotentMatcher.group(1);
				Pattern emPattern = Pattern.compile("<em(.*?)\\\\/em>");
				Matcher emMatcher =  emPattern.matcher(forwardContent1);
				if(emMatcher.find()){
					String forwardContent2 = emMatcher.group(1);
					Pattern sentencePattern = Pattern.compile(">(.*?)<");
					Matcher sentenceMatcher = sentencePattern.matcher(forwardContent2);
					StringBuilder sb = new StringBuilder();
					while(sentenceMatcher.find()){
						String sentence = sentenceMatcher.group(1);
						if(!sentence.contains("@") || sentence.contains("@ ")){
							sb.append(sentence).append(" ");
						}
					}
					blogInfo.setForWardContent(UnicodeToString(sb.toString()));
				}
			}

			//解析转发数,评论数与发表时间
			Pattern weiboInfoPattern = Pattern.compile("<p\\sclass=\\\\\"info\\sW_linkb\\sW_textb\\\\\">(.*?)<\\\\/p>");			
			Matcher weiboInfoMatcher = weiboInfoPattern.matcher(feedItemString);
			if(weiboInfoMatcher.find()){
				String weiboInfo = weiboInfoMatcher.group(1);
				
				//转发数
				Pattern forwwardNumPattern = Pattern.compile("\\\\u8f6c\\\\u53d1\\(([0-9]*?)\\)");			
				Matcher forwwardNumMatcher = forwwardNumPattern.matcher(weiboInfo);
				if(forwwardNumMatcher.find()){
					Integer forwardNum = Integer.parseInt(forwwardNumMatcher.group(1));
					blogInfo.setForwardNum(forwardNum);
				}
				
				//评论数
				Pattern commentNumPattern = Pattern.compile("\\\\u8bc4\\\\u8bba\\(([0-9]*?)\\)");			
				Matcher commentNumMatcher = commentNumPattern.matcher(weiboInfo);
				if(commentNumMatcher.find()){
					Integer commentNum = Integer.parseInt(commentNumMatcher.group(1));
					blogInfo.setCommentNum(commentNum);
				}

				//发表时间
				Pattern timePattern = Pattern.compile(".*<a\\stitle=\\\\\"(.*?)\\\\\"\\snode-type=\\\\\"feed_list_item_date");			
				Matcher timeMatcher = timePattern.matcher(weiboInfo);
				if(timeMatcher.find()){
					String postTime = timeMatcher.group(1);
					blogInfo.setTimestamp(postTime);
				}
			}
			resultList.add(blogInfo);						
		}
		return resultList;
	}

	public ArrayList<Long> getkeywordUsers(StringBuilder urlContent){
		ArrayList<Long> resultList = new ArrayList<Long>();
		//System.out.println(urlContent);
		String searchNo = "<div class=\\\"search_noresult\\\">";
		if(urlContent.indexOf(searchNo) >= 0){
			return resultList;
		}
		Pattern feedItemPattern = Pattern.compile("action-type=\\\\\"feed_list_item\\\\\"(.*?)<dd\\sclass=\\\\\"clear\\\\\"><\\\\/dd>",Pattern.DOTALL);
		Matcher feedItemMatcher = feedItemPattern.matcher(urlContent);
		while(feedItemMatcher.find()){
			String feedItemString = feedItemMatcher.group(1);

			Pattern listContentPattern = Pattern.compile("<p\\snode-type=\\\\\"feed_list_content\\\\\">(.*?)<\\\\/p>",Pattern.DOTALL);
			Matcher listContentMatcher = listContentPattern.matcher(feedItemString);
			while(listContentMatcher.find()){
				String listContent = listContentMatcher.group(1);

				Pattern userIDPattern = Pattern.compile("usercard=\\\\\"id=([0-9]*?)\\\\\"",Pattern.DOTALL);
				Matcher userIDMatcher = userIDPattern.matcher(listContent);
				if(userIDMatcher.find()){
					try{
						Long userID = Long.parseLong(userIDMatcher.group(1));
						resultList.add(userID);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}						
		}
		return resultList;
	}

	public ArrayList<Long> getTagUsers(StringBuilder urlContent){
		ArrayList<Long> resultList = new ArrayList<Long>();
		//System.out.println(urlContent);
		String searchNo = "<div class=\"search_no\">";	
		if(urlContent.indexOf(searchNo) >= 0){
			return resultList;
		}
		Pattern userItemPattern = Pattern.compile("<dd\\sclass=\"person_info\">(.*?)</p>",Pattern.DOTALL);
		Matcher userItemMatcher = userItemPattern.matcher(urlContent);
		while(userItemMatcher.find()){
			String userItemString = userItemMatcher.group(1);

			Pattern userIDPattern = Pattern.compile("usercardUid=\"([0-9]*?)\"",Pattern.DOTALL);
			Matcher userIDMatcher = userIDPattern.matcher(userItemString);
			if(userIDMatcher.find()){
				Long userID = Long.parseLong(userIDMatcher.group(1));
				resultList.add(userID);
			}						
		}
		return resultList;
	}

	public ArrayList<Long> getQunUsers(StringBuilder urlContent){
		ArrayList<Long> resultList = new ArrayList<Long>();
		Pattern userItemPattern = Pattern.compile("<a\\saction-type=\"usercard\"\\susercard=\"true\"\\suid=\"([0-9]*?)\"\\stitle=",Pattern.DOTALL);
		Matcher userItemMatcher = userItemPattern.matcher(urlContent);
		while(userItemMatcher.find()){
			Long userID = Long.parseLong(userItemMatcher.group(1));
			resultList.add(userID);
		}
		return resultList;
	}
	public String getPageLink(StringBuilder urlContent){
		String pageLinkSuffix = null;
		Pattern pagesPattern = Pattern.compile("<div\\sclass=\"W_pages W_pages_comment\">(.*?)</div>",Pattern.DOTALL);
		Matcher pagesMatcher = pagesPattern.matcher(urlContent);
		if(pagesMatcher.find()){
			String subContent = pagesMatcher.group(1);
			Pattern pageLinkPattern = Pattern.compile("<a\\shref=\"/weibo/(.*?)&page=");
			Matcher pageLinkMatcher = pageLinkPattern.matcher(subContent);

			if(pageLinkMatcher.find()){
				pageLinkSuffix = pageLinkMatcher.group(1);
			}		
		}
		return pageLinkSuffix;
	}

	public String UnicodeToString(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");
		}
		return str;
	}

	public int getPagesNum(StringBuilder urlContent){
		Pattern pagesNumPattern = Pattern.compile("action-type=\\\\\"feed_list_page\\\\\">([0-9]*?)<\\\\/a>",Pattern.DOTALL);
		Matcher pagesNumMatcher = pagesNumPattern.matcher(urlContent);
		int pagesNum = 0;
		while(pagesNumMatcher.find()){
			pagesNum = Integer.parseInt(pagesNumMatcher.group(1));
		}
		return pagesNum;
	}

	public int getQunPagesNum(StringBuilder urlContent){
		Pattern pagesNumPattern = Pattern.compile("<a\\saction-type=\"pagebar_go\"\\spagebar-params=\"page=([0-9]*?)&gid=",Pattern.DOTALL);
		Matcher pagesNumMatcher = pagesNumPattern.matcher(urlContent);
		int pagesNum = 0;
		while(pagesNumMatcher.find()){
			pagesNum = Integer.parseInt(pagesNumMatcher.group(1));
		}
		return pagesNum;
	}

	public ArrayList<FarwardUserInfo> getFarwardUserInfo(StringBuilder urlContent){
		ArrayList<FarwardUserInfo> result = new ArrayList<FarwardUserInfo>();
		Pattern userInfoPattern = Pattern.compile("class=\\\\\"comment_list\\sW_linecolor\\sclearfix\\\\\"(.*?)<\\\\/dl>",Pattern.DOTALL);
		Matcher userInfoMatcher = userInfoPattern.matcher(urlContent);
		while(userInfoMatcher.find()){
			FarwardUserInfo farwardUserInfo = new FarwardUserInfo();
			String userInfo = userInfoMatcher.group(1);
			Pattern userIDPattern = Pattern.compile("<dd><a.*?usercard=\\\\\"id=([0-9]*?)\\\\\">",Pattern.DOTALL);
			Matcher userIDMatcher = userIDPattern.matcher(userInfo);
			if(userIDMatcher.find()){
				farwardUserInfo.setUserID(Long.parseLong(userIDMatcher.group(1)));				
			}
			Pattern farwardContentPattern = Pattern.compile("<em>(.*?)<\\\\/em>",Pattern.DOTALL);
			Matcher farwardContentMatcher = farwardContentPattern.matcher(userInfo);
			if(farwardContentMatcher.find()){
				String farwardContent = farwardContentMatcher.group(1);
				farwardUserInfo.setFarwardContent(UnicodeToString(FarwardContentFilter(farwardContent)));
			}
			Pattern farwardURLPattern = Pattern.compile("action-data.*?&url=(.*?)&mid=([0-9]*?)&",Pattern.DOTALL);
			Matcher farwardURLMatcher = farwardURLPattern.matcher(userInfo);
			if(farwardURLMatcher.find()){
				farwardUserInfo.setFarwardURL(farwardURLMatcher.group(1).replaceAll("\\\\", ""));	
				farwardUserInfo.setWeiboID(farwardURLMatcher.group(2));
			}
			Pattern farwardNumPattern = Pattern.compile("\\\\\">\\\\u8f6c\\\\u53d1\\(([0-9]*?)\\)<\\\\/a>",Pattern.DOTALL);
			Matcher farwardNumMachter = farwardNumPattern.matcher(userInfo);
			if(farwardNumMachter.find()){
				farwardUserInfo.setFarwardNum(Integer.parseInt(farwardNumMachter.group(1)));
			}
			result.add(farwardUserInfo);
		}
		return result;
	}

	public ArrayList<FarwardUserInfo> getCommentUserInfo(StringBuilder urlContent){
		ArrayList<FarwardUserInfo> result = new ArrayList<FarwardUserInfo>();
		Pattern userInfoPattern = Pattern.compile("class=\\\\\"comment_list\\sW_linecolor\\sclearfix\\\\\"(.*?)<\\\\/dl>",Pattern.DOTALL);
		Matcher userInfoMatcher = userInfoPattern.matcher(urlContent);
		while(userInfoMatcher.find()){

			FarwardUserInfo farwardUserInfo = new FarwardUserInfo();
			String userInfo = userInfoMatcher.group(1);
			Pattern userIDPattern = Pattern.compile("<dd>.*?<a.*?usercard=\\\\\"id=([0-9]*?)\\\\\">",Pattern.DOTALL);
			Matcher userIDMatcher = userIDPattern.matcher(userInfo);
			if(userIDMatcher.find()){
				try{
					farwardUserInfo.setUserID(Long.parseLong(userIDMatcher.group(1)));
				} catch (Exception e) {
					farwardUserInfo.setUserID(-1L);
				}
			}
			Pattern farwardContentPattern = Pattern.compile("<dd>.*?<a.*?<\\\\/a>(.*?)\\\\n\\\\t<span",Pattern.DOTALL);
			Matcher farwardContentMatcher = farwardContentPattern.matcher(userInfo);
			if(farwardContentMatcher.find()){
				String farwardContent = farwardContentMatcher.group(1);
				farwardUserInfo.setFarwardContent(UnicodeToString(FarwardContentFilter(farwardContent)));
			}			
			result.add(farwardUserInfo);
		}
		return result;
	}

	public FarwardUserInfo getOriginalFarwardUserInfo (StringBuilder urlContent){
		FarwardUserInfo farwardUserInfo = new FarwardUserInfo();
		Pattern pattern = Pattern.compile("\\$CONFIG\\['oid'\\]\\s=\\s'([0-9]*?)'",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(urlContent);
		if(matcher.find()){
			farwardUserInfo.setUserID(Long.parseLong(matcher.group(1)));
		}
		Pattern p = Pattern.compile("feed_list\\sclearfix\\sfeed_list_hover\\sW_no_border\\\\\" mid=\\\\\"(.*?)\\\\\".*?<em.*?>(.*?)<\\\\/em>",Pattern.DOTALL);
		Matcher m = p.matcher(urlContent);
		if(m.find()){
			farwardUserInfo.setWeiboID(m.group(1));
			farwardUserInfo.setFarwardContent(UnicodeToString(FarwardContentFilter(m.group(2))));
		}
		Pattern p2 = Pattern.compile("forward_counter.*?\\(([0-9]*?)\\)",Pattern.DOTALL);
		Matcher m2 = p2.matcher(urlContent);
		if(m2.find()){
			farwardUserInfo.setFarwardNum(Integer.parseInt(m2.group(1)));
		}
		return farwardUserInfo;
	}

	public ArrayList<KeywordUserInfo> getkeywordUserInfo(StringBuilder urlContent){
		ArrayList<KeywordUserInfo> resultList = new ArrayList<KeywordUserInfo>();
		//System.out.println(urlContent);
		String searchNo = "<div class=\\\"search_noresult\\\">";
		if(urlContent.indexOf(searchNo) >= 0){
			return resultList;
		}
		Pattern feedItemPattern = Pattern.compile("action-type=\\\\\"feed_list_item\\\\\"(.*?)<dd\\sclass=\\\\\"clear\\\\\"><\\\\/dd>",Pattern.DOTALL);
		Matcher feedItemMatcher = feedItemPattern.matcher(urlContent);
		while(feedItemMatcher.find()){			
			String feedItemString = feedItemMatcher.group(1);

			KeywordUserInfo keywordUserInfo = new KeywordUserInfo();


			Pattern userIDPattern = Pattern.compile("usercard=\\\\\"id=([0-9]*?)\\\\\"",Pattern.DOTALL);
			Matcher userIDMatcher = userIDPattern.matcher(feedItemString);
			if(userIDMatcher.find()){
				try{
					Long userID = Long.parseLong(userIDMatcher.group(1));
					keywordUserInfo.setUserID(userID);
				} catch (Exception e) {
				}
			}
			Pattern weiboContentPattern = Pattern.compile("<em>(.*?)<\\\\/em>",Pattern.DOTALL);
			Matcher weiboContentMatcher = weiboContentPattern.matcher(feedItemString);
			if(weiboContentMatcher.find()){
				keywordUserInfo.setContent(htmlTagsFilter(UnicodeToString(weiboContentMatcher.group(1))));
			}
			Pattern weiboURLPattern = Pattern.compile("<p\\sclass=\\\\\"info\\sW_linkb\\sW_textb\\\\\">.*?<\\\\/span>.*?href=\\\\\"(.*?)\\\\\"",Pattern.DOTALL);
			Matcher weiboURLMatcher = weiboURLPattern.matcher(feedItemString);
			if(weiboURLMatcher.find()){
				keywordUserInfo.setWeiboURL(weiboURLMatcher.group(1).replaceAll("\\\\", ""));
			}
			if(keywordUserInfo.getUserID().longValue() != 0){
				resultList.add(keywordUserInfo);
			}

		}
		return resultList;
	}
}
