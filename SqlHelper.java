package com.epoint.projectissues.utils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 2019.8.13 封装基于JDBC的SQL工具类
 * 
 * @author tang
 *
 */
public class SqlHelper {

	private static Connection connection = null;
	private static final String PATTERN_STRING = "(@\\w+)";
	private static final String PATTERN_AND_STRING = "(and\\s$)";

	/**
	 * 获取连接
	 * 
	 * @return
	 */
	public static Connection getConnection() {

		if (connection == null) {

			connection = DataSourceUtil.getConnInstance();

		}
		return connection;
	}

	/**
	 * 释放连接
	 */
	public static void releaseConn() {

		if (connection != null) {

			try {

				connection.close();

				connection = null;

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 切除 SQL 语句中带有 "@"符号开始的属性 用 ? 替换
	 * 
	 * @param sql
	 * @return
	 */
	private static String handlerSQL(String sql) {
		
//		==================== 处理sql语句==================
//		正则匹配 @后面的属性名
		Pattern pattern = Pattern.compile(PATTERN_STRING, Pattern.UNICODE_CASE);
		Matcher matcher = pattern.matcher(sql);
//		找出所有@属性名
		while (matcher.find()) {
//			取出每一个匹配的  去掉@表示
			String key = matcher.group(1);
//			替换为?
			sql = sql.replaceFirst(key, "?");
		}
		System.out.println("替换过后的sql:---->" + sql);
		return sql;
	}

	/**
	 * 通过 id 进行删除
	 * 
	 * @param tableName
	 * @param columnName
	 * @param id
	 */
	public static PreparedStatement createDeleteById(String tableName, String columnName, Object id) {

		String sql = "delete from " + tableName + " where " + columnName + " = ? " ;

		Connection connection = SqlHelper.getConnection();

		try {

			PreparedStatement statement = connection.prepareStatement(sql);		
//			设置参数
			statement.setObject(1, id);
			
			System.out.println( "删除的SQL:" + statement );
			
			return statement;

		} catch (SQLException e) {

			e.printStackTrace();

		}
		return null;
	}

	/**
	 * 根据实体类属性进行删除
	 * 
	 * @param entity
	 */
	public static void createDeletePrepareStatement(Object entity) {

	}

	/**
	 * 通过实体构造更新语句
	 * whereProperty 注意:	要和 实体类中的相同
	 * @param entity
	 * @param tableName
	 * @param whereProperty 条件属性
	 **/
	public static PreparedStatement createUpdatePrepareStatement(Object entity, String tableName, String whereProperty) {
		try {
//			获取类的所有值和属性
			Map<String, Object> map = getClassFieldValue(entity);
//			构造基本的update
			StringBuffer stringBuffer = new StringBuffer("update " + tableName + " set ");
//			拼接sql
			map.forEach((key, value) -> {
				String key_ = key;
//				当前字段不是作为条件的字段
				if (value != null && !key_.equals(whereProperty)) {
//						如果当前值是日期型的
					if (value instanceof Date) {
//							转换日期格式添加
						SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
						stringBuffer.append(key_ +"="+"'" + sFormat.format(value) + "', ");
//						当前是字符串
					} else if (value instanceof String) {
						stringBuffer.append(key_ +"="+"'" + value + "', ");
//						数值	
					} else {
						stringBuffer.append(key_ +"="+value + ", ");
					}
				}
			});
			String sql = stringBuffer.toString();
//			消除 逗号
			if (sql.endsWith(", ")) {
				sql = StringUtils.removeSuffix(sql, ", ");
			}
//			构造条件
			stringBuffer.setLength(0);
			stringBuffer.append(sql);
			
//			获取构建条件的 id 的值
			Object key = map.get(whereProperty);
								
//			拼接SQL 语句
			stringBuffer.append(" where  " + whereProperty + " = ?");
			
			System.out.println("生成的更新语句为: " + stringBuffer.toString());
			
			Connection connection = SqlHelper.getConnection();
			
			PreparedStatement statement = connection.prepareStatement(stringBuffer.toString());
	
//			设置条件参数
			statement.setObject(1, key);
			
			System.out.println("设置过参数后的SQL语句: " + statement);
			
//			执行SQL
			return statement;

		} catch (Exception e) {
			
			System.out.println( "SQL 更新失败!" );
			
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * 查询指定表的记录总数
	 * 
	 * @param tableName
	 * @return
	 */
	public static Integer selectCount(String tableName) {

		Connection connection = getConnection();

		try {
//			创建 statement
			Statement statement = connection.createStatement();
//			创建SQL
			String sql = "select count(*) from " + tableName;
//			查询结果
			ResultSet resultSet = statement.executeQuery(sql);

			resultSet.next();
//			返回结果
			return resultSet.getInt(1);

		} catch (SQLException e) {

			e.printStackTrace();
		}
		System.out.println(" 查询总数失败! ");
		return 0;

	}

	/**
	 * 
	 * @param sql    sql语句
	 * @param params 要设置的参数 key:属性名 value:属性值
	 * @return PreparedStatement
	 * @throws Exception eg: select * from user where id = @id/? and username
	 *                   = @username/?
	 */
	public static <T> PreparedStatement createPrepareStatement(String sql, Map<String, Object> params)
			throws Exception {
//==================== 处理sql语句==================
//		正则匹配 @后面的属性名
		Pattern pattern = Pattern.compile(PATTERN_STRING, Pattern.UNICODE_CASE);
		Matcher matcher = pattern.matcher(sql);
//		创造一个map 用来存放 属性对应的值
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
//		找出所有@属性名
		while (matcher.find()) {
//			取出每一个匹配的  去掉@表示
			String key = matcher.group(1);
//			替换为?
			sql = sql.replaceFirst(key, "?");
//			结果存放在hashmap   key: 属性名   value:  对应属性值						
			String key_ = key.substring(1).toLowerCase();

			hashMap.put(key_, params.get(key_));
		}
		System.out.println("替换过后的sql:---->" + sql);
//==================== 设置参数=======================	
		Connection connection = SqlHelper.getConnection();

		PreparedStatement statement = connection.prepareStatement(sql);

//		存放 获取map所有的value转化为list
		List<Object> list = null;
		if (hashMap.size() == 0) {
//			map 若为空，尝试从 初始传入的map中 获取值				
			list = new ArrayList<>(params.values());
		} else {
//			获取map 所有的value 转化为list
			list = new ArrayList<>(hashMap.values());
		}

		setParameters(statement, list);

		System.out.println("设置过参数后的SQL语句:---->" + statement.toString());

		return statement;
	}

	/**
	 * 为 PrepareStatement 设置参数值
	 * 
	 * @param statement
	 * @param hashMap
	 */
	public static void setParameters(PreparedStatement statement, List<Object> list) {

		int parameterIndex = 1;

		for (Object value : list) {
			try {
				statement.setObject(parameterIndex++, value);
			} catch (SQLException e) {
				System.out.println("设置值出现错误....");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据参数数组构造查询语句
	 * 
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static PreparedStatement createPrepareStatement(String sql, Object[] params) throws SQLException {
//		对sql语句进行处理
		sql = SqlHelper.handlerSQL(sql);
//		==================== 设置参数=======================	
		Connection connection = SqlHelper.getConnection();
//		创建statement对象
		PreparedStatement statement = connection.prepareStatement(sql);
//		构造参数列表
		List<Object> list = Arrays.asList(params);
//		设置参数
		setParameters(statement, list);

		System.out.println("设置过参数后的SQL语句:---->" + statement.toString());

		return statement;
	}

	/**
	 * 获取对象字段的属性然后封装为map
	 * 
	 * @param entity
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static Map<String, Object> getClassFieldValue(Object entity)
			throws IllegalArgumentException, IllegalAccessException {
//		获取字节码对象
		Class<? extends Object> clazz = entity.getClass();
//		获取所有类的字段
		Field[] fields = clazz.getDeclaredFields();
//		存放  属性 对应的 值
		Map<String, Object> map = new HashMap<>();
//		获取所有字段的值
		for (Field field : fields) {
//			设置可访问权限
			field.setAccessible(true);
//			获取属性名
			String fieldName = field.getName();
//			获取属性值
			Object fieldValue = field.get(entity);
//			存入map
			map.put(fieldName, fieldValue);
		}

		return map;
	}

	/**
	 * 根据实体类构造查询语句
	 * 
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	private static String createInsertSQL(Object entity) throws Exception {

//		获取字节码对象		
		Class<? extends Object> clazz = entity.getClass();
//		获取类字段的所有信息
		Map<String, Object> map = SqlHelper.getClassFieldValue(entity);
//		获取类名
		String tableName = clazz.getSimpleName();

//		构造value子句
		if (map.size() != 0) {
//			构造sql语句
			StringBuffer stringBuffer = new StringBuffer();
//			构造基本sql原型
			stringBuffer.append("insert into " + tableName + "(");
//			构造一个存放 value 值的 StringBuffer			
			StringBuffer valueBuffer = new StringBuffer();
			valueBuffer.append(" value(");
//			构造其他字段
			map.forEach((key, value) -> {
				if (value != null) {
//					如果当前值是日期型的
					if (value instanceof Date) {
//						转换日期格式添加
						SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
						valueBuffer.append("'" + sFormat.format(value) + "',");
//					当前是字符串
					} else if (value instanceof String) {
						valueBuffer.append("'" + value + "',");
//					数值	
					} else {
						valueBuffer.append(value + ",");
					}
					stringBuffer.append(key + ",");
				}
			});
//			移除逗号
			stringBuffer.deleteCharAt(stringBuffer.length() - 1);
//			移除逗号			
			valueBuffer.deleteCharAt(valueBuffer.length() - 1);
//			追加右边括号
			stringBuffer.append(")");
			valueBuffer.append(")");

			stringBuffer.append(valueBuffer);

			String sql = stringBuffer.toString();

			return sql;
		}

		System.out.println("无法构造查询语句....");

		return null;
	}

	/**
	 * 构造查询语句
	 * 
	 * @param entity
	 * @return
	 */
	public static PreparedStatement createInsertStatement(Object entity) {

		try {
			String sql = createInsertSQL(entity);
			if (sql != null) {
//				创建statement对象
				PreparedStatement statement = SqlHelper.getConnection().prepareStatement(sql);

				System.out.println("构造的SQL语句为:---->" + sql);

				return statement;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 创建查询语句，条件用and连接
	 * 
	 * @param entity
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static String createSQLConditionByAND(Object entity)
			throws IllegalArgumentException, IllegalAccessException {
//		获取字节码对象		
		Class<? extends Object> clazz = entity.getClass();
//		获取类字段的所有信息
		Map<String, Object> map = SqlHelper.getClassFieldValue(entity);
//		获取类名
		String tableName = clazz.getSimpleName();
//		构造sql语句
		StringBuffer stringBuffer = new StringBuffer();
//		构造基本sql原型
		stringBuffer.append("select * from " + tableName);
//		构造where条件
		if (map.size() > 0) {
			stringBuffer.append(" where ");
		}
//		构造其他条件
		map.forEach((key, value) -> {

			if (value != null) {
				stringBuffer.append(key + " = " + value + " and ");
			}
		});
//		去除最后的 and 符号		
		String sql = stringBuffer.toString();
//		判断sql是否已 and 符号结尾
		if (stringBuffer.toString().endsWith("and ")) {

//			利用正则进行匹配
			Pattern compile = Pattern.compile(PATTERN_AND_STRING);

			Matcher matcher = compile.matcher(sql);
//			正则查找
			if (matcher.find()) {
//				查找成功  替换掉最后的 and
				sql = matcher.replaceFirst("");
			}
		}
		return sql;
	}

	/**
	 * 根据实体类构造查询语句 条件and连接
	 * 
	 * @param entity
	 * @return PreparedStatement
	 */
	public static PreparedStatement createQueryStatement(Object entity) throws Exception {

		String sql = SqlHelper.createSQLConditionByAND(entity);
//		创建statement对象
		PreparedStatement statement = SqlHelper.getConnection().prepareStatement(sql);

		System.out.println("构造的SQL语句为:---->" + sql);

		return statement;
	}
	/**
	 * 查询所有
	 * @param tableName
	 * @return
	 */
	public static PreparedStatement createQueryAllStatement(String tableName) {
		
		String sql = "select * from " + tableName;
		
		Connection connection = SqlHelper.getConnection();
		
		try {
			
			PreparedStatement statement = connection.prepareStatement(sql);
			System.out.println( " 查询所有的语句为: " + statement );
			return statement;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
		
	/**
	 * 把结果封装成list集合
	 * 
	 * @param resultSet
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> ConvertList(ResultSet resultSet, Class<T> clazz) throws Exception {

		List<T> list = new ArrayList<>();
//		循环结果集
		while (resultSet.next()) {
			list.add(ResultConvertBean(resultSet, clazz));
		}
		return list;
	}

	/**
	 * 把结果封装成单个bean
	 * 
	 * @param resultSet
	 * @param clazz
	 * @return
	 */
	public static <T> T ConvertBean(ResultSet resultSet, Class<T> clazz) {
		try {
			resultSet.next();
			return ResultConvertBean(resultSet, clazz);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 把结果封装成单个bean
	 * 通过类字段，取数据库中字段对应的值
	 * @param resultSet
	 * @param entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> T ResultConvertBean(ResultSet resultSet, Class<T> clazz) throws Exception {

//		查询不到数据
		if (resultSet.getRow() == 0) {
			System.out.println("查询结果集为null....");
			return null;
		}
//		创建对象
		Object newInstance = clazz.getConstructor().newInstance();
//		获取所有字段
		Field[] fields = clazz.getDeclaredFields();
//		设置字段值	
		for (Field field : fields) {
//			设置可访问
			field.setAccessible(true);
//			属性名
			String fileName = field.getName();
//			属性值
			Object columnValue = null;
			
			try {
//				值映射不到的情况
				columnValue = resultSet.getObject(fileName);
				
			} catch (Exception e) {
				
				System.out.println("这个字段----->" + fileName + "没有映射成功!");
				
			}						
						
//			设置属性值
			field.set(newInstance, columnValue);
		}

		return (T) newInstance;
	}
}