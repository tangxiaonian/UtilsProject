package com.epoint.projectissues.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	
	/**
	 * 去除字符串后缀
	 * @param sql
	 * @param suffx
	 * @return
	 */
	public static String removeSuffix(String sql,String suffx) {
		
		Pattern pattern = Pattern.compile("("+suffx+"$)",Pattern.UNICODE_CASE);
		
		Matcher matcher = pattern.matcher(sql);
		
		if ( matcher.find() ) {
			
			return matcher.replaceFirst("");
		}
		
		System.out.println( " 去除SQL后缀失败... " );
		
		return null;
	}
	
}
