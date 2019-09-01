package com.epoint.projectissues.utils;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * 2019.8.23
 * 
 * @author tang
 *
 */
public class BeanUtils {
	
	/**
	 * 参数自动封装
	 * @param request
	 * @param entityClass
	 * @return
	 */
	public static <T>  Object convertBean(HttpServletRequest request,Class<T> entityClass) {

//		获取所有类的字段
		Field[] fields = entityClass.getDeclaredFields();
		
		Object instance = null;
		
		try {
									
			instance = entityClass.newInstance();
			
//			获取所有字段的值
			for (Field field : fields) {
//				设置可访问权限
				field.setAccessible(true);
//				获取属性名
				String fieldName = field.getName();
//				属性类型
				String typeName = field.getGenericType().getTypeName();						
//				获取 request 值
				String parameter = request.getParameter(fieldName);								
				
				field.set(instance, convertField(typeName,parameter));
			}
									
		} catch (Exception e) {
			
			e.printStackTrace();
		}		
				
		return instance;		
	}
	
	/**
	 * 根据类型转换值
	 * @param type
	 * @param value
	 * @return
	 */
	public static Object convertField(String type,String value) {
		
		if ("java.lang.String".equals(type)) {
			
			if (value != null && !value.equals("")) {
				return value;
			}
			
		}else if ("java.lang.Integer".equals(type)) {
			
			if (value != null && !value.equals("")) {				
				return Integer.valueOf(value);				
			}
			return 0;
						
		}else if ("java.lang.Double".equals(type)) {
			
			if (value != null && !value.equals("")) {				
				return Double.valueOf(value);				
			}
			return 0;
						
		}else if ("java.util.Date".equals(type)) {
			
			if (value != null ) {				
//				处理时间
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");			
				try {
//					解析时间
					return dateFormat.parse(value);
					
				} catch (ParseException e) {							
					
					System.out.println( "日期解析錯誤....." );
					
					return new Date();
				}
				
			}
//			默认为当前时间
			return new Date();												
			
		}else if ("java.lang.Float".equals(type)) {
			
			if (value != null && !value.equals("")) {
				
				return Float.valueOf(value);
				
			}
			return 0;
		}
		
		return null;		
	}
	
}
