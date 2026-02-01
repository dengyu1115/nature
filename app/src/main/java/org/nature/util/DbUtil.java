package org.nature.util;

import android.database.Cursor;
import org.nature.db.DB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DbUtil {

    private static final Function<Cursor, Map<String, Object>> MAPPER = cursor -> {
        Map<String, Object> map = new HashMap<>();
        String[] columnNames = cursor.getColumnNames();
        for (String columnName : columnNames) {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex == -1) {
                continue;
            }
            switch (cursor.getType(columnIndex)) {
                case Cursor.FIELD_TYPE_INTEGER:
                    map.put(columnName, cursor.getInt(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    map.put(columnName, cursor.getDouble(columnIndex));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    map.put(columnName, null);
                    break;
                default:
                    map.put(columnName, cursor.getString(columnIndex));
                    break;
            }
        }
        return map;
    };

    public static void refresh() {
        DB.refresh();
    }

    public static List<Map<String, Object>> list(String path, String sql) {
        return DB.create(path).list(sql, new String[0], MAPPER);
    }

    public static Map<String, Object> find(String path, String sql) {
        return DB.create(path).find(sql, new String[0], MAPPER);
    }

    public static int update(String path, String sql) {
        return DB.create(path).executeUpdate(sql, new String[0]);
    }

    public static int ddl(String path, String sql) {
        return DB.create(path).executeSql(sql);
    }

}