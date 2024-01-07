package org.nature.common.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import org.nature.common.db.builder.util.SqlBuilder;
import org.nature.common.util.FileUtil;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * DB操作工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
public class DB {

    /**
     * 全局路径
     */
    private static final File INTERNAL = Environment.getExternalStorageDirectory();
    /**
     * DB实例map
     */
    private static final Map<String, DB> DB_MAP = new ConcurrentHashMap<>();
    /**
     * 读操作
     */
    private final List<SQLiteDatabase> readDbs;
    /**
     * 写操作
     */
    private final SQLiteDatabase writeDb;

    /**
     * 获取DB实例
     * @param path 路径
     */
    private DB(String path) {
        // 创建数据库文件
        File file = new File(INTERNAL, path);
        FileUtil.createIfNotExists(file);
        readDbs = new LinkedList<>();
        // 创建写实例
        writeDb = SQLiteDatabase.openOrCreateDatabase(file, null);
        int processors = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < processors; i++) {
            // 创建与处理核心数相同的读实例
            readDbs.add(SQLiteDatabase.openOrCreateDatabase(file, null));
        }
    }

    /**
     * 创建实例
     * @param path 路径
     * @return DB
     */
    public static DB create(String path) {
        return DB_MAP.computeIfAbsent(path, DB::new);
    }

    /**
     * 获取int类型值
     * @param c   行
     * @param col 字段
     * @return Integer
     */
    public static Integer getInt(Cursor c, String col) {
        return getVal(getString(c, col), Integer::valueOf);
    }

    /**
     * 湖区double类型值
     * @param c   行
     * @param col 字段
     * @return Double
     */
    public static Double getDouble(Cursor c, String col) {
        return getVal(getString(c, col), Double::valueOf);
    }

    /**
     * 获取BigDecimal类型值
     * @param c   行
     * @param col 字段
     * @return BigDecimal
     */
    public static BigDecimal getDecimal(Cursor c, String col) {
        return getVal(getString(c, col), BigDecimal::new);
    }

    /**
     * 获取String类型值
     * @param c   行
     * @param col 字段
     * @return String
     */
    public static String getString(Cursor c, String col) {
        int i = c.getColumnIndex(col);
        if (i == -1) {
            return null;
        }
        return c.getString(i);
    }

    /**
     * 查询列表数据
     * @param sqlBuilder sqlBuilder
     * @param mapper     值映射
     * @return T
     */
    public <T> List<T> list(SqlBuilder sqlBuilder, Function<Cursor, T> mapper) {
        return this.list(sqlBuilder.sql(), sqlBuilder.args(), mapper);
    }

    /**
     * 查询列表数据
     * @param sql    sql
     * @param args   参数
     * @param mapper 值映射
     * @return T
     */
    public <T> List<T> list(String sql, String[] args, Function<Cursor, T> mapper) {
        // 获取实例
        SQLiteDatabase database = this.gainDb();
        try {
            List<T> list = new ArrayList<>();
            // 执行查询逻辑
            try (Cursor cursor = database.rawQuery(sql, args)) {
                while (cursor.moveToNext()) {
                    // 读取结果集
                    list.add(mapper.apply(cursor));
                }
            }
            return list;
        } finally {
            // 返还实例
            this.returnDb(database);
        }
    }

    /**
     * 查询单条数据
     * @param sqlBuilder sqlBuilder
     * @param mapper     值映射
     * @return T
     */
    public <T> T find(SqlBuilder sqlBuilder, Function<Cursor, T> mapper) {
        return this.find(sqlBuilder.sql(), sqlBuilder.args(), mapper);
    }

    /**
     * 查询单条数据
     * @param sql    sql
     * @param args   参数
     * @param mapper 值映射
     * @return T
     */
    public <T> T find(String sql, String[] args, Function<Cursor, T> mapper) {
        // 获取实例
        SQLiteDatabase database = this.gainDb();
        try {
            try (Cursor cursor = database.rawQuery(sql, args)) {
                if (cursor.getCount() > 1) {
                    throw new RuntimeException("more than one result");
                }
                if (cursor.moveToNext()) {
                    // 读取结果集
                    return mapper.apply(cursor);
                }
                return null;
            }
        } finally {
            // 返还实例
            this.returnDb(database);
        }
    }

    /**
     * 执行SQL
     * @param sql sql
     * @return int
     */
    public int executeSql(String sql) {
        try (SQLiteStatement statement = writeDb.compileStatement(sql)) {
            return statement.executeUpdateDelete();
        }
    }

    /**
     * 执行SQL
     * @param sqlBuilder sqlBuilder
     * @return int
     */
    public int executeUpdate(SqlBuilder sqlBuilder) {
        return executeUpdate(sqlBuilder.sql(), sqlBuilder.args());
    }

    /**
     * 执行SQL
     * @param sql  sql
     * @param args args
     * @return int
     */
    public int executeUpdate(String sql, String[] args) {
        try (SQLiteStatement statement = writeDb.compileStatement(sql)) {
            for (int i = args.length; i > 0; i--) {
                String arg = args[i - 1];
                if (arg == null) {
                    statement.bindNull(i);
                } else {
                    statement.bindString(i, arg);
                }
            }
            return statement.executeUpdateDelete();
        }
    }

    /**
     * 批量执行SQL
     * @param data      数据集
     * @param batchSize 批量执行数量
     * @param function  处理逻辑
     * @return int
     */
    public <T> int batchExec(List<T> data, int batchSize, Function<List<T>, Integer> function) {
        // 判断是否在事务中
        boolean nt = !writeDb.inTransaction();
        if (nt) {
            // 不在事务中开启事务处理
            AtomicInteger result = new AtomicInteger();
            this.doInTransaction(() -> result.set(this.doBatch(data, batchSize, function)));
            return result.get();
        } else {
            // 在事务中直接执行
            return this.doBatch(data, batchSize, function);
        }
    }

    /**
     * 批量执行
     * @param data      数据集
     * @param batchSize 批量执行数量
     * @param function  执行处理逻辑
     * @return int
     */
    private <T> int doBatch(List<T> data, int batchSize, Function<List<T>, Integer> function) {
        int size = data.size();
        // 计算批处理数量
        int batch = size % batchSize == 0 ? size / batchSize : size / batchSize + 1;
        int updated = 0;
        // 循环批处理
        for (int i = 0; i < batch; i++) {
            // 获取批处理数据
            List<T> list = data.subList(batchSize * i, i == batch - 1 ? size : batchSize * (i + 1));
            // 执行处理逻辑
            updated += function.apply(list);
        }
        // 返回更新数量
        return updated;
    }

    /**
     * 在事务中执行逻辑
     * @param runnable 执行逻辑
     */
    public void doInTransaction(Runnable runnable) {
        // 开启事务
        writeDb.beginTransaction();
        try {
            // 执行逻辑
            runnable.run();
            // 设置事务成功
            writeDb.setTransactionSuccessful();
        } finally {
            // 结束事务
            writeDb.endTransaction();
        }
    }

    /**
     * 获取可用的数据库连接
     * @return SQLiteDatabase
     */
    private synchronized SQLiteDatabase gainDb() {
        if (readDbs.isEmpty()) {
            // 如果没有可用的数据库连接，则等待如果没有可用的数据库连接，则等待
            try {
                // 等待可用的数据库连接
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // 再次尝试获取可用的数据库连接
            return this.gainDb();
        } else {
            // 如果有可用的数据库连接，则返回可用的数据库连接
            SQLiteDatabase database = readDbs.get(0);
            // 从列表中移除已获取的数据库连接
            readDbs.remove(database);
            return database;
        }
    }

    /**
     * 返还数据库连接
     * @param database 数据库连接
     */
    private synchronized void returnDb(SQLiteDatabase database) {
        boolean empty = readDbs.isEmpty();
        // 添加可用的数据库连接
        readDbs.add(database);
        if (empty) {
            // 如果有可用的数据库连接，则唤醒等待的线程
            this.notify();
        }
    }

    /**
     * 获取值，如果为空则返回默认值
     * @param s    字符串
     * @param func 值转换方法
     * @return T
     */
    private static <T> T getVal(String s, Function<String, T> func) {
        return s == null ? null : func.apply(s);
    }

}
