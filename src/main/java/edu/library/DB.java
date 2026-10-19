package edu.library;

import edu.homework.jdbc.util.JDBCTool;
import java.sql.*;
import java.util.*;

public final class DB {
  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static Connection open() throws SQLException {
    return DriverManager.getConnection(
        env(
            "LIBRARY_DB_URL",
            "jdbc:mysql://127.0.0.1:3306/LibraryWeb2026?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"),
        env("LIBRARY_DB_USER", "root"),
        env("LIBRARY_DB_PASSWORD", ""));
  }

  static String env(String k, String d) {
    String v = System.getenv(k);
    return v == null ? d : v;
  }

  public static List<Map<String, Object>> rows(Connection c, String sql, Object... args)
      throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql)) {
      bind(p, args);
      try (ResultSet r = p.executeQuery()) {
        List<Map<String, Object>> out = new ArrayList<>();
        var m = r.getMetaData();
        while (r.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          for (int i = 1; i <= m.getColumnCount(); i++) {
            Object v = r.getObject(i);
            row.put(
                m.getColumnLabel(i),
                v instanceof java.time.temporal.TemporalAccessor || v instanceof java.util.Date
                    ? v.toString()
                    : v);
          }
          out.add(row);
        }
        return out;
      }
    }
  }

  public static int exec(Connection c, String sql, Object... a) throws SQLException {
    try (var p = c.prepareStatement(sql)) {
      bind(p, a);
      return p.executeUpdate();
    }
  }

  static void bind(PreparedStatement p, Object[] a) throws SQLException {
    for (int i = 0; i < a.length; i++) p.setObject(i + 1, a[i]);
  }

  public static <T> List<T> entities(Connection c, String sql, Class<T> t, Object... a)
      throws SQLException {
    try (var p = c.prepareStatement(sql)) {
      bind(p, a);
      try (var r = p.executeQuery()) {
        return JDBCTool.resultSetToList(r, t);
      }
    }
  }

  public static String id() {
    return UUID.randomUUID().toString();
  }
}
