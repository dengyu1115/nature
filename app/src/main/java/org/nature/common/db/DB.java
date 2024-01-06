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
        File file = new File(INTERNAL, path);
        FileUtil.createIfNotExists(file);
        readDbs = new LinkedList<>();
        writeDb = SQLiteDatabase.openOrCreateDatabase(file, null);
        int processors = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < processors; i++) {
            readDbs.add(SQLiteDatabase.openOrCreateDatabase(file, null));
        }
    }

    public static DB create(String path) {
        return DB_MAP.computeIfAbsent(path, DB::new);
    }

    public static Integer getInt(Cursor c, String col) {
        int columnIndex = c.getColumnIndex(col);
        if (columnIndex == -1) {
            return null;
        }
        String string = c.getString(columnIndex);
        return string == null ? null : Integer.valueOf(string);
    }

    public static Double getDouble(Cursor c, String col) {
        int columnIndex = c.getColumnIndex(col);
        if (columnIndex == -1) {
            return null;
        }
        String string = c.getString(columnIndex);
        return string == null ? null : Double.valueOf(string);
    }

    public static BigDecimal getDecimal(Cursor c, String col) {
        int columnIndex = c.getColumnIndex(col);
        if (columnIndex == -1) {
            return null;
        }
        String string = c.getString(columnIndex);
        return string == null ? null : new BigDecimal(string);
    }

    public static String getString(Cursor c, String col) {
        int columnIndex = c.getColumnIndex(col);
        if (columnIndex == -1) {
            return null;
        }
        return c.getString(columnIndex);
    }

    public <T> List<T> list(SqlBuilder sqlBuilder, Function<Cursor, T> mapper) {
        return this.list(sqlBuilder.sql(), sqlBuilder.args(), mapper);
    }

    public <T> List<T> list(String sql, String[] args, Function<Cursor, T> mapper) {
        SQLiteDatabase database = this.gainDb();
        try {
            List<T> list = new ArrayList<>();
            try (Cursor cursor = database.rawQuery(sql, args)) {
                while (cursor.moveToNext()) {
                    list.add(mapper.apply(cursor));
                }
            }
            return list;
        } finally {
            this.returnDb(database);
        }
    }

    public <T> T find(SqlBuilder sqlBuilder, Function<Cursor, T> mapper) {
        return this.find(sqlBuilder.sql(), sqlBuilder.args(), mapper);
    }

    public <T> T find(String sql, String[] args, Function<Cursor, T> mapper) {
        SQLiteDatabase database = this.gainDb();
        try {
            try (Cursor cursor = database.rawQuery(sql, args)) {
                if (cursor.getCount() > 1) {
                    throw new RuntimeException("more than one result");
                }
                if (cursor.moveToNext()) {
                    return mapper.apply(cursor);
                }
                return null;
            }
        } finally {
            this.returnDb(database);
        }
    }

    public int executeSql(String sql) {
        try (SQLiteStatement statement = writeDb.compileStatement(sql)) {
            return statement.executeUpdateDelete();
        }
    }

    public int executeUpdate(SqlBuilder sqlBuilder) {
        return executeUpdate(sqlBuilder.sql(), sqlBuilder.args());
    }

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

    public <T> int batchExec(List<T> data, int batchSize, Function<List<T>, Integer> function) {
        boolean nt = !writeDb.inTransaction();
        if (nt) {
            AtomicInteger result = new AtomicInteger();
            this.doInTransaction(() -> result.set(this.doBatch(data, batchSize, function)));
            return result.get();
        } else {
            return this.doBatch(data, batchSize, function);
        }
    }

    private <T> int doBatch(List<T> data, int batchSize, Function<List<T>, Integer> function) {
        int size = data.size();
        int batch = size % batchSize == 0 ? size / batchSize : size / batchSize + 1;
        int updated = 0;
        for (int i = 0; i < batch; i++) {
            List<T> list = data.subList(batchSize * i, i == batch - 1 ? size : batchSize * (i + 1));
            updated += function.apply(list);
        }
        return updated;
    }

    public void doInTransaction(Runnable runnable) {
        writeDb.beginTransaction();
        try {
            runnable.run();
            writeDb.setTransactionSuccessful();
        } finally {
            writeDb.endTransaction();
        }
    }

    private synchronized SQLiteDatabase gainDb() {
        if (readDbs.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return this.gainDb();
        } else {
            SQLiteDatabase database = readDbs.get(0);
            readDbs.remove(database);
            return database;
        }
    }

    private synchronized void returnDb(SQLiteDatabase database) {
        boolean empty = readDbs.isEmpty();
        readDbs.add(database);
        if (empty) {
            this.notify();
        }
    }

}
