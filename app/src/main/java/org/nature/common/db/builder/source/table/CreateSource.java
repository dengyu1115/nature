package org.nature.common.db.builder.source.table;

import org.nature.common.db.DB;
import org.nature.common.db.builder.model.Mapping;
import org.nature.common.db.builder.util.ModelUtil;

import java.util.List;
import java.util.stream.Collectors;

public class CreateSource {

    public Object execute(Class<?> cls, boolean drop) {
        DB db = DB.create(ModelUtil.db(cls));
        String table = ModelUtil.table(cls);
        if (drop) {
            db.executeSql(this.drop(table));
        }
        return db.executeSql(this.create(cls));
    }

    private String drop(String table) {
        return "drop table if exists " + table;
    }

    private String create(Class<?> cls) {
        List<Mapping> ids = ModelUtil.listIdMapping(cls);
        List<Mapping> columns = ModelUtil.listNoneIdMapping(cls);
        String table = ModelUtil.table(cls);
        StringBuilder sql = new StringBuilder("create table if not exists " + table + "(");
        for (Mapping i : ids) {
            sql.append(i.getColumn()).append(" ").append(this.getType(i)).append(",");
        }
        for (Mapping i : columns) {
            sql.append(i.getColumn()).append(" ").append(this.getType(i)).append(",");
        }
        sql.append("primary key(")
                .append(ids.stream().map(Mapping::getColumn).collect(Collectors.joining(","))).append("))");
        return sql.toString();
    }

    private String getType(Mapping i) {
        Class<?> type = i.getType();
        if (type == String.class) {
            return "text";
        } else {
            return "real";
        }

    }

}
