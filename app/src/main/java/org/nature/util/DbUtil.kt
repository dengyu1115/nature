package org.nature.util

import android.database.Cursor
import org.nature.db.DB
import java.util.function.Function

object DbUtil {

    private val MAPPER = Function<Cursor, Map<String, Any>> { cursor ->
        val map = HashMap<String, Any>()
        val columnNames = cursor.columnNames
        for (columnName in columnNames) {
            val columnIndex = cursor.getColumnIndex(columnName)
            if (columnIndex == -1) {
                continue
            }
            when (cursor.getType(columnIndex)) {
                Cursor.FIELD_TYPE_INTEGER -> map[columnName] = cursor.getInt(columnIndex)
                Cursor.FIELD_TYPE_FLOAT -> map[columnName] = cursor.getDouble(columnIndex)
                Cursor.FIELD_TYPE_NULL -> map[columnName] = null!!
                else -> map[columnName] = cursor.getString(columnIndex)
            }
        }
        map
    }

    @JvmStatic
    fun refresh() {
        DB.refresh()
    }

    @JvmStatic
    fun list(path: String, sql: String): List<Map<String, Any>> {
        return DB.create(path).list(sql, arrayOf(), MAPPER)
    }

    @JvmStatic
    fun find(path: String, sql: String): Map<String, Any>? {
        return DB.create(path).find(sql, arrayOf(), MAPPER)
    }

    @JvmStatic
    fun update(path: String, sql: String): Int {
        return DB.create(path).executeUpdate(sql, arrayOf())
    }

    @JvmStatic
    fun ddl(path: String, sql: String): Int {
        return DB.create(path).executeSql(sql)
    }
}
