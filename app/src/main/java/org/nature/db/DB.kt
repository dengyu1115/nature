package org.nature.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.nature.config.Config.INTERNAL
import org.nature.util.FileUtil
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

/**
 * DB操作工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
class DB private constructor(path: String) {

    /**
     * 读操作
     */
    private val readDbs: MutableList<SQLiteDatabase>

    /**
     * 写操作
     */
    private val writeDb: SQLiteDatabase

    init {
        // 创建数据库文件
        val file = File(INTERNAL, path)
        FileUtil.createIfNotExists(file)
        readDbs = ArrayList()
        // 创建写实例
        writeDb = SQLiteDatabase.openOrCreateDatabase(file, null)
        val processors = Runtime.getRuntime().availableProcessors()
        for (i in 0 until processors) {
            // 创建与处理核心数相同的读实例
            readDbs.add(SQLiteDatabase.openOrCreateDatabase(file, null))
        }
    }

    /**
     * 查询列表数据
     * @param sql    sql
     * @param args   参数
     * @param mapper 值映射
     * @return T
     */
    fun <T> list(sql: String, args: Array<String>, mapper: Function<Cursor, T>): List<T> {
        // 获取实例
        val database = this.gainDb()
        return try {
            val list: MutableList<T> = ArrayList()
            // 执行查询逻辑
            database.rawQuery(sql, args).use { cursor ->
                while (cursor.moveToNext()) {
                    // 读取结果集
                    list.add(mapper.apply(cursor))
                }
            }
            list
        } finally {
            // 返还实例
            this.returnDb(database)
        }
    }

    /**
     * 查询单条数据
     * @param sql    sql
     * @param args   参数
     * @param mapper 值映射
     * @return T
     */
    fun <T> find(sql: String, args: Array<String>, mapper: Function<Cursor, T>): T? {
        // 获取实例
        val database = this.gainDb()
        return try {
            database.rawQuery(sql, args).use { cursor ->
                if (cursor.count > 1) {
                    throw RuntimeException("more than one result")
                }
                if (cursor.moveToNext()) {
                    // 读取结果集
                    return mapper.apply(cursor)
                }
                null
            }
        } finally {
            // 返还实例
            this.returnDb(database)
        }
    }

    /**
     * 执行SQL
     * @param sql sql
     * @return int
     */
    fun executeSql(sql: String): Int {
        writeDb.compileStatement(sql).use { statement ->
            return statement.executeUpdateDelete()
        }
    }

    /**
     * 执行SQL
     * @param sql  sql
     * @param args args
     * @return int
     */
    fun executeUpdate(sql: String, args: Array<String?>): Int {
        writeDb.compileStatement(sql).use { statement ->
            for (i in args.indices.reversed()) {
                val arg = args[i]
                if (arg == null) {
                    statement.bindNull(i + 1)
                } else {
                    statement.bindString(i + 1, arg)
                }
            }
            return statement.executeUpdateDelete()
        }
    }

    /**
     * 在事务中执行逻辑
     * @param runnable 执行逻辑
     */
    fun doInTransaction(runnable: Runnable) {
        // 开启事务
        writeDb.beginTransaction()
        try {
            // 执行逻辑
            runnable.run()
            // 设置事务成功
            writeDb.setTransactionSuccessful()
        } finally {
            // 结束事务
            writeDb.endTransaction()
        }
    }

    /**
     * 获取可用的数据库连接
     * @return SQLiteDatabase
     */
    @Synchronized
    private fun gainDb(): SQLiteDatabase {
        if (readDbs.isEmpty()) {
            // 如果没有可用的数据库连接，则等待
            try {
                // 等待可用的数据库连接
                (this as Object).wait()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            // 再次尝试获取可用的数据库连接
            return this.gainDb()
        } else {
            // 如果有可用的数据库连接，则返回可用的数据库连接
            val database = readDbs[0]
            // 从列表中移除已获取的数据库连接
            readDbs.remove(database)
            return database
        }
    }

    /**
     * 返还数据库连接
     * @param database 数据库连接
     */
    @Synchronized
    private fun returnDb(database: SQLiteDatabase) {
        val empty = readDbs.isEmpty()
        // 添加可用的数据库连接
        readDbs.add(database)
        if (empty) {
            // 如果有可用的数据库连接，则唤醒等待的线程
            (this as Object).notify()
        }
    }

    companion object {
        /**
         * DB实例map
         */
        private val DB_MAP = ConcurrentHashMap<String, DB>()

        /**
         * 获取String类型值
         * @param c   行
         * @param col 字段
         * @return String
         */
        @JvmStatic
        fun getString(c: Cursor, col: String): String? {
            val i = c.getColumnIndex(col)
            if (i == -1) {
                return null
            }
            return c.getString(i)
        }

        /**
         * 获取值，如果为空则返回默认值
         * @param s    字符串
         * @param func 值转换方法
         * @return T
         */
        @JvmStatic
        private fun <T> getVal(s: String?, func: Function<String, T>): T? {
            return if (s == null) null else func.apply(s)
        }

        /**
         * 创建实例
         * @param path 路径
         * @return DB
         */
        @JvmStatic
        fun create(path: String): DB {
            return DB_MAP.computeIfAbsent(path) { DB(it) }
        }

        /**
         * 刷新DB实例
         */
        @JvmStatic
        fun refresh() {
            DB_MAP.values.forEach { db ->
                db.writeDb.close()
                for (readDb in db.readDbs) {
                    readDb.close()
                }
            }
            DB_MAP.clear()
        }
    }
}
