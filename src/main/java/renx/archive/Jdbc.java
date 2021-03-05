package renx.archive;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class Jdbc {
	public static Logger logger = Logger.getLogger(Jdbc.class);

	public static void main(String[] args) {
		System.out.println(123123123);
	}

	public static List<Map> queryList(Connection conn, String sql, Object... params) throws Exception {
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(sql);
			return resultSetToList(query(pst, sql, params));
		} catch (Exception e) {
			throw e;
		} finally {
			if (pst != null)
				pst.close();
		}
	}

	public static List<Object> queryThinList(Connection conn, String sql, Object... params) throws Exception {
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(sql);
			return resultSetToThinList(query(pst, sql, params));
		} catch (Exception e) {
			throw e;
		} finally {
			if (pst != null)
				pst.close();
		}
	}

	public static InputStream queryStream(Connection conn, String sql, Object... params) throws Exception {
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(sql);
			ResultSet rs = query(pst, sql, params);
			if (rs.next()) {
				return rs.getBinaryStream(1);
			}
			return null;
		} catch (Exception e) {
			throw e;
		} finally {
			if (pst != null)
				pst.close();
		}
	}

	public static Integer queryInteger(Connection conn, String sql, Object... params) throws Exception {
		return Var.toInteger(queryColumn(conn, sql, params));
	}

	public static String queryString(Connection conn, String sql, Object... params) throws Exception {
		return Var.toString(queryColumn(conn, sql, params));
	}

	public static BigDecimal queryDecimal(Connection conn, String sql, Object... params) throws Exception {
		return Var.toDecimal(queryColumn(conn, sql, params));
	}

	public static Long queryLong(Connection conn, String sql, Object... params) throws Exception {
		return Var.toLong(queryColumn(conn, sql, params));
	}

	public static Float queryFloat(Connection conn, String sql, Object... params) throws Exception {
		return Var.toFloat(queryColumn(conn, sql, params));
	}

	public static Date queryDate(Connection conn, String sql, Object... params) throws Exception {
		return Var.toDate(queryColumn(conn, sql, params));
	}

	public static Object queryColumn(Connection conn, String sql, Object... params) throws Exception {
		Map row = query(conn, sql, params);
		if (row == null)
			return null;
		return row.get(row.keySet().iterator().next());
	}

//	public static Mappquery(Connection conn, String sql, Object... params) throws Exception {
//		logger.debug("in " + RandomStringUtils.randomNumeric(5));
//		PreparedStatement pst = null;
//		try {
//			pst = conn.prepareStatement(sql);
//			List<Map> list = parseResultSetOfList(query(pst, sql, params));
//			if (list == null || list.isEmpty())
//				return null;
//			return list.get(0);
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			if (pst != null)
//				pst.close();
//			logger.debug("out " + RandomStringUtils.randomNumeric(5));
//		}
//	}

	public static Map query(Connection conn, String sql, Object... params) throws Exception {

		Map item = null;
		List<Map> itemList = queryList(conn, sql, params);
		if (itemList != null && itemList.size() > 0) {
			item = itemList.get(0);
		}

		return item;
	}

	public static ResultSet query(PreparedStatement pst, String sql, Object... params) throws SQLException {
		if (params == null)
			params = new Object[] {};
		sql = sql.replaceAll("\\s+", " ");
		sql = sql.replaceAll("\\s,", "");
		sql = sql.replaceAll(",\\s", "");
		sql = sql.replaceAll("\\s'", "");
		sql = sql.replaceAll("'\\s", "");
		logger.debug(sql);
		logger.debug(Arrays.toString(params));
		try {
			for (int i = 0; i < params.length; i++) {
				Object param = params[i];
				if (param instanceof Var) {
					param = ((Var) param).val();
				}
				pst.setObject(i + 1, param);
			}
			long s = System.currentTimeMillis();
			ResultSet rs = pst.executeQuery();
			long e = System.currentTimeMillis();
			logger.debug("takes: " + StringUtils.commaNum((e - s) + "") + "ms");
			return rs;
		} catch (Exception e) {
			throw new SQLException(e.getMessage() + " sql: " + sql, e);
		}
	}

//	public static String toLikePart(String columnValue) {
//		return new StringBuilder("%").append(columnValue).append("%").toString();
//	}

	public static Integer insert(Connection conn, String sql, Object... params) throws Exception {
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			update(pst, sql, params);
			ResultSet rs = pst.getGeneratedKeys();
			if (rs.next())
				return rs.getInt(1);
			else
				return null;
		} catch (Exception e) {
			throw e;
		} finally {
			if (pst != null)
				pst.close();
		}
	}

	public static int update(Connection conn, String sql, Object... params) throws Exception {
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(sql);
			return update(pst, sql, params);
		} catch (Exception e) {
			throw e;
		} finally {
			if (pst != null)
				pst.close();

		}
	}

	public static int update(PreparedStatement pst, String sql, Object... params) throws Exception {
		if (params == null)
			params = new Object[] {};
		sql = sql.replaceAll("\\s+", " ");
		sql = sql.replaceAll("\\s,", "");
		sql = sql.replaceAll(",\\s", "");
		sql = sql.replaceAll("\\s'", "");
		sql = sql.replaceAll("'\\s", "");
		logger.debug(sql);
		logger.debug(Arrays.toString(params));
		int cnt = 0;
		try {
			for (int i = 0; i < params.length; i++) {
				Object param = params[i];
				if (param instanceof Var) {
					param = ((Var) param).val();
					pst.setObject(i + 1, param);
				} else if (param instanceof InputStream) {
					pst.setBinaryStream(i + 1, (InputStream) param);
				} else {
					pst.setObject(i + 1, param);
				}
			}
			long s = System.currentTimeMillis();
			cnt = pst.executeUpdate();
			long e = System.currentTimeMillis();
			logger.debug("takes: " + StringUtils.commaNum((e - s) + "") + "ms");
		} catch (Exception e) {
			throw new Exception(e.getMessage() + " sql: " + sql, e);
		}
		logger.debug("affected: " + cnt);
		return cnt;
	}

	public static Integer returnGeneratedKey(PreparedStatement pst) throws SQLException {
		ResultSet rs = pst.getGeneratedKeys();
		if (rs.next()) {
			int id = rs.getInt(1);
			rs.close();
			return id;
		} else
			return null;
	}

	public static List<Integer> returnGeneratedKeys(PreparedStatement pst) throws SQLException {
		ResultSet rs = pst.getGeneratedKeys();
		List<Integer> keys = new ArrayList();
		while (rs.next()) {
			int key = rs.getInt(1);
			keys.add(key);
		}
		rs.close();
		return keys;
	}

	public static int[] batch(Connection conn, String sql, Object... paramBatches) throws Exception {
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(sql);
			return batch(pst, sql, paramBatches);
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			if (pst != null)
				pst.close();
		}
	}

	public static int[] batch(PreparedStatement pst, String sql, Object... paramBatches) throws Exception {
		if (paramBatches == null)
			paramBatches = new Object[] {};
		logger.debug(sql);
		int[] cnts = new int[] {};
		for (Object param : paramBatches) {
			logger.debug(param);
			if (param instanceof List) {
				for (int i = 0; i < ((List) param).size(); i++) {
					pst.setObject(i + 1, ((List) param).get(i));
				}
			} else if (param instanceof Object[]) {
				for (int i = 0; i < ((Object[]) param).length; i++) {
					pst.setObject(i + 1, ((Object[]) param)[i]);
				}
			} else if (param instanceof Object) {
				pst.setObject(1, param);
			} else {
				pst.setObject(1, param);
			}
			pst.addBatch();
		}
		long s = System.currentTimeMillis();
		cnts = pst.executeBatch();
		long e = System.currentTimeMillis();
		logger.debug("takes: " + StringUtils.commaNum((e - s) + "") + "ms");
		logger.debug("affected : " + Arrays.toString(cnts));
		return cnts;
	}

	public static List<Map> resultSetToList(ResultSet rs) throws SQLException {
		List<Map> rows = new ArrayList();
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCnt = metaData.getColumnCount();
		while (rs.next()) {
			Map<String, Object> row = new LinkedHashMap();
			for (int i = 1; i <= columnCnt; i++) {
				Object value = rs.getObject(i);
				row.put(metaData.getColumnLabel(i), value);
			}
			rows.add(row);
			if (rows.size() <= 10)
				logger.debug(row);
		}
		logger.debug("affected : " + rows.size());
		return rows;
	}

	public static List<Map> resultSetToList(ResultSet rs, String[] excludeColumns) throws SQLException {
		List<String> excludeColumnList = Arrays.asList(excludeColumns);
		List<Map> rows = new ArrayList();
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCnt = metaData.getColumnCount();
		while (rs.next()) {
			Map<String, Object> row = new LinkedHashMap();
			for (int i = 1; i <= columnCnt; i++) {
				String column = metaData.getColumnLabel(i);
				if (excludeColumnList.contains(column))
					continue;
				Object value = rs.getObject(i);
				row.put(metaData.getColumnLabel(i), value);
			}
			rows.add(row);
			if (rows.size() <= 10)
				logger.debug(row);
		}
		logger.debug("affected : " + rows.size());
		return rows;
	}

	public static List<List<Object>> resultSetToValueLists(ResultSet rs) throws SQLException {
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCnt = metaData.getColumnCount();

		List<List<Object>> valueLists = new ArrayList<List<Object>>();
		while (rs.next()) {
			List<Object> valueList = new ArrayList<Object>();
			for (int i = 1; i <= columnCnt; i++) {
				Object value = rs.getObject(i);
				valueList.add(value);
			}
			valueLists.add(valueList);
			if (valueLists.size() <= 10)
				logger.debug(valueList);
		}
		logger.debug("affected : " + valueLists.size());
		return valueLists;
	}

	public static List<Object> resultSetToThinList(ResultSet rs) throws SQLException {
		List<Object> rows = new ArrayList();
		ResultSetMetaData metaData = rs.getMetaData();
		while (rs.next()) {
			Object value = rs.getObject(1);
			rows.add(value);
			if (rows.size() <= 10)
				logger.debug(new StringBuilder("[").append(value).append("]").toString());
		}
		logger.debug("affected : " + rows.size());
		return rows;
	}

	public static Map resultSetToMap(ResultSet rs) throws SQLException {
		List<Map> rows = resultSetToList(rs);
		if (rows.size() > 0)
			return rows.get(0);
		else
			return null;
	}

	public static Object resultSetToColumn(ResultSet rs) throws SQLException {
		List<Map> rows = resultSetToList(rs);
		if (rows.size() > 0)
			return rows.get(0).get(rows.get(0).keySet().iterator().next());
		else
			return null;
	}

	public static Integer parseResultSetOfOneInteger(ResultSet rs) throws SQLException {
		Object value = resultSetToColumn(rs);
		if (value == null)
			return null;
		else {
			return new Integer(value.toString());
		}
	}
//
//	public static String buildSql_phraseListByOr(String partSql, int count) {
//		if (count == 0)
//			return "";
//		String sqlPart = " ";
//		for (int i = 0; i < count; i++) {
//			if (i == 0)
//				sqlPart += partSql;
//			else
//				sqlPart += " or " + partSql;
//		}
//		sqlPart += " ";
//		return sqlPart;
//	}
//
//	public static String buildSql_paramsplit(String... params) {
//		if (params == null || params.length == 0)
//			return "";
//		String sqlPart = "  ";
//		for (int i = 0; i < params.length; i++) {
//			if (i == 0)
//				sqlPart += "'" + params[i] + "'";
//			else
//				sqlPart += " , '" + params[i] + "'";
//		}
//		sqlPart += " ";
//		return sqlPart;
//	}
//
//	public static String buildSql_placeholderList(int count) {
//		if (count == 0)
//			return "";
//		String sqlPart = "";
//		for (int i = 0; i < count; i++) {
//			if (i == 0)
//				sqlPart += " ?";
//			else
//				sqlPart += ",?";
//		}
//		sqlPart += " ";
//		return sqlPart;
//	}

//	public static String buildSql_orderBy(String[] sorts, String[] orders, String[] sortPool, String[] sortColumnPool,
//			String baseSort, String baseOrder) {
//		StringBuilder sqlB = new StringBuilder(" order by ");
//		if (sorts == null || sorts.length == 0)
//			sorts = new String[] {};
//		if (orders == null || sorts.length == 0) {
//			orders = new String[sorts.length];
//			for (int i = 0; i < orders.length; i++) {
//				orders[i] = "desc";
//			}
//		}
//
//		List<String> sortListPool = sortPool == null ? new ArrayList<String>()
//				: new ArrayList<String>(Arrays.asList(sortPool));
//		List<String> sortColumnListPool = sortColumnPool == null ? new ArrayList<String>()
//				: new ArrayList<String>(Arrays.asList(sortColumnPool));
//		for (int i = 0; i < sorts.length; i++) {
//			if (sorts[i] == null || sorts[i].trim().isEmpty())
//				continue;
//			int n = sortListPool.indexOf(sorts[i]);
//			if (n > -1)
//				sqlB.append(sortColumnListPool.get(n));
//			else
//				throw ModuleBreak.failure("排序字段有误");
//			sqlB.append(" ");
//			if (orders[i].equals("desc"))
//				sqlB.append("desc");
//			else if (orders[i].equals("asc"))
//				sqlB.append("asc");
//			else
//				throw ModuleBreak.failure("排序顺序有误");
//			sqlB.append(sorts.length == 0 ? "" : ",");
//		}
//		sqlB.append(baseSort).append(" ").append(baseOrder);
//		return sqlB.toString();
//	}

//	public static List addparams(List params, String insSplit) {
//		if (insSplit == null)
//			return params;
//		String[] ss = insSplit.split(",");
//		params.addAll(Arrays.asList(ss));
//		return params;
//	}

//	public static String buildConditional(String andOr, String column, String columnEq, String insSplit) {
//		if (insSplit == null)
//			return "";
//		return buildConditional(andOr, column, columnEq, insSplit.split(","));
//	}
//
//	public static String buildConditional(String andOr, String column, String columnEq, String... ins) {
//		String sqlPart = " ";
//
//		if (ins == null || ins.length == 0)
//			return "";
//		columnEq = (columnEq == null || columnEq.trim().isEmpty()) ? "1" : columnEq;
//		if (!columnEq.equals("1") && !columnEq.equals("0"))
//			return "";
//
//		andOr = (andOr == null || andOr.trim().isEmpty()) ? "and" : andOr;
//		if (!andOr.equalsIgnoreCase("and") && !andOr.equalsIgnoreCase("or"))
//			return "";
//		if (ins.length == 1) {
//			sqlPart = andOr + " " + column + (columnEq.equals("1") ? " = " : " != ") + " ? ";
//		} else {
//			sqlPart = andOr + " " + column + (columnEq.equals("1") ? " in " : " not in ") + " (";
//			for (int i = 0; i < ins.length; i++) {
//				if (i == 0)
//					sqlPart += "?";
//				else
//					sqlPart += " , ?";
//			}
//			sqlPart = sqlPart + " )";
//		}
//
//		return sqlPart;
//	}
}