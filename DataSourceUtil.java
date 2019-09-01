package com.epoint.projectissues.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;

public class DataSourceUtil {

	private static Properties properties = null;
	private static DruidDataSource dataSource = null;
	
	static {
//		读取配置文件
		InputStream inputStream = DataSourceUtil.class.getClassLoader().getResourceAsStream("db.properties");

		if (inputStream != null) {
			
			properties = new Properties();
			
			try {
//				加载配置文件
				properties.load(inputStream);
//				创建数据源
				dataSource = new DruidDataSource();
				
				dataSource.setUsername(properties.getProperty("user"));
				dataSource.setPassword(properties.getProperty("password"));
				dataSource.setDriverClassName(properties.getProperty("driverClass"));
				dataSource.setUrl(properties.getProperty("jdbcUrl"));				
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}

		}

	}
	/**
	 * 获取连接
	 * @return
	 */
	public static Connection getConnInstance() {
		
		try {
			
			return dataSource.getConnection();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}
		
		return null;
	}
	
}