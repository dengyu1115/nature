package org.nature.common.db.builder.source.table;

import org.nature.common.db.DB;
import org.nature.common.db.builder.model.Mapping;
import org.nature.common.db.builder.util.ModelUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 建表处理source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class CreateSource {

    public Object execute(Class<?> cls, boolean drop) {
        DB db = DB.create(ModelUtil.db(cls));
        String table = ModelUtil.table(cls);
        if (drop) {
            // 标记了重建则先删除表
            db.executeSql(this.drop(table));
        }
        // 创建表
        return db.executeSql(this.create(cls));
    }

    /**
     * 删除表
     * @param table 表
     * @return String
     */
    private String drop(String table) {
        return "drop table if exists " + table;
    }

    /**
     * 创建表
     * @param cls 类
     * @return String
     */
    private String create(Class<?> cls) {
        // id字段
        List<Mapping> ids = ModelUtil.listIdMapping(cls);
        // 其他字段
        List<Mapping> columns = ModelUtil.listNoneIdMapping(cls);
        // 表名
        String table = ModelUtil.table(cls);
        // 拼接sql
        StringBuilder sql = new StringBuilder("create table if not exists " + table + "(");
        // 拼接id字段
        for (Mapping i : ids) {
            sql.append(i.getColumn()).append(" ").append(this.getType(i)).append(",");
        }
        // 拼接其他字段
        for (Mapping i : columns) {
            sql.append(i.getColumn()).append(" ").append(this.getType(i)).append(",");
        }
        // 拼接主键
        sql.append("primary key(")
                .append(ids.stream().map(Mapping::getColumn).collect(Collectors.joining(","))).append("))");
        return sql.toString();
    }

    /**
     * 字段类型
     * @param i mapping对象
     * @return String
     */
    private String getType(Mapping i) {
        Class<?> type = i.getType();
        if (type == String.class) {
            return "text";
        } else {
            return "real";
        }

    }

}
