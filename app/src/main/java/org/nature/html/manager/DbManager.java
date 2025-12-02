package org.nature.html.manager;

import android.database.Cursor;
import org.nature.common.db.DB;
import org.nature.common.ioc.annotation.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class DbManager {

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

    public List<Map<String, Object>> list(String path, String sql) {
        return DB.create(path).list(sql, new String[0], MAPPER);
    }

    public Map<String, Object> find(String path, String sql) {
        return DB.create(path).find(sql, new String[0], MAPPER);
    }

    public int update(String path, String sql) {
        return DB.create(path).executeUpdate(sql, new String[0]);
    }

}