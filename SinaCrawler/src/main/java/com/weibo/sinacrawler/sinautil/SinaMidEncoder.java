package com.weibo.sinacrawler.sinautil;

public class SinaMidEncoder {
	private String str62value(int key){
		String[] str62Keys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b",
	            "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
	            "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H",
	            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
	            "Y", "Z"};
		return str62Keys[key];
	}
	
	private String intTo62(int int10){
		String str62 = "";
		int r = 0;
		while(int10 != 0){
			r= int10 % 62;
			str62 = str62value(r) + str62;
			int10 = int10 / 62;			
		}
		return str62;
	}
	
	public String midToStr(String mid){
		String result = "";
		for(int i = mid.length()-7; i > -7; i=i-7){
			int offset1 = i < 0 ? 0 : i;
			int offset2 = i + 7;
			int num = Integer.parseInt(mid.substring(offset1,offset2));
			String str62 = intTo62(num);
			result = str62 + result;
		}
		return result;
	}
}
