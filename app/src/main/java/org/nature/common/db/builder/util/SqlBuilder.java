package org.nature.common.db.builder.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * sql构建
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class SqlBuilder {
    /**
     * 参数集合
     */
    private final List<String> params;
    /**
     * sql语句拼接
     */
    private final StringBuilder sqlBuilder;

    /**
     * 实例化
     */
    private SqlBuilder() {
        params = new ArrayList<>();
        sqlBuilder = new StringBuilder();
    }

    /**
     * 实例化
     * @return SqlBuilder
     */
    public static SqlBuilder build() {
        return new SqlBuilder();
    }

    /**
     * 参数集合
     * @return String[]
     */
    public String[] args() {
        return params.toArray(new String[0]);
    }

    /**
     * sql
     * @return String
     */
    public String sql() {
        return this.delBlanks(sqlBuilder.toString().trim());
    }

    /**
     * 拼接
     * @param sql     sql
     * @param objects 参数
     * @return SqlBuilder
     */
    public SqlBuilder append(String sql, Object... objects) {
        sqlBuilder.append(" ").append(sql);
        return this.appendArg(objects);
    }

    /**
     * 拼接参数
     * @param objects 参数集合
     * @return SqlBuilder
     */
    public SqlBuilder appendArg(Object... objects) {
        if (objects == null) {
            params.add(null);
        } else {
            for (Object object : objects) {
                if (object == null) {
                    params.add(null);
                } else if (object instanceof String) {
                    params.add((String) object);
                } else if (object instanceof Double || object instanceof Integer) {
                    params.add(object.toString());
                } else if (object instanceof BigDecimal) {
                    params.add(((BigDecimal) object).toPlainString());
                } else {
                    throw new RuntimeException(String.format("%s 数据类型暂不支持", object.getClass()));
                }
            }
        }
        return this;
    }

    /**
     * foreach 拼接
     * @param col 集合
     * @param s   开始符号
     * @param e   结束符号
     * @param sep 分隔符号
     * @param c   拼接逻辑
     * @return SqlBuilder
     */
    public <T> SqlBuilder foreach(Collection<T> col, String s, String e, String sep, BiConsumer<T, SqlBuilder> c) {
        if (col != null && !col.isEmpty()) {
            if (s != null && !s.isEmpty()) this.append(s);
            int index = 0;
            for (T t : col) {
                if (++index > 1) sqlBuilder.append(sep);
                c.accept(t, this);
            }
            if (e != null && !e.isEmpty()) this.append(e);
        }
        return this;
    }

    /**
     * 删除字符串中的空格，回车，换行符（节省空间）
     * @param s 字符串
     * @return String
     */
    private String delBlanks(CharSequence s) {
        int c = 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == ' ' || ch == '\t' || ch == '\n') c++;
            else c = 0;
            if (c <= 1) builder.append(ch);
        }
        return builder.toString();
    }

}
