package org.nature.common.db.builder.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nature.common.db.builder.model.Mapping;

import java.util.List;

public class SqlAppender {

    public static SqlBuilder selectBuilder(Class<?> cls) {
        return SqlBuilder.build().append("select")
                .append(TextUtil.columns(ModelUtil.listMapping(cls)))
                .append("from").append(ModelUtil.table(cls))
                .append("where");
    }

    public static SqlBuilder deleteBuilder(Class<?> cls) {
        return SqlBuilder.build().append("delete")
                .append("from").append(ModelUtil.table(cls))
                .append("where");
    }

    public static SqlBuilder saveBuilder(Class<?> cls, JSONObject o) {
        return singleBuilder(cls, o, "insert");
    }

    public static SqlBuilder mergeBuilder(Class<?> cls, JSONObject o) {
        return singleBuilder(cls, o, "replace");
    }

    public static SqlBuilder batchSaveBuilder(Class<?> cls, List<?> list) {
        return batchBuilder(cls, list, "insert");
    }

    public static SqlBuilder batchMergeBuilder(Class<?> cls, List<?> list) {
        return batchBuilder(cls, list, "replace");
    }

    public static void idCondition(SqlBuilder builder, Object o, List<Mapping> idMappings) {
        if (idMappings.size() == 1) {
            // 单个ID字段的数据直接转List
            Mapping mapping = idMappings.get(0);
            builder.append(mapping.getColumn()).append("=?", o);
        } else {
            // 多个ID字段的数据转List<JSONObject>
            JSONObject id = (JSONObject) JSON.toJSON(o);
            String columns = TextUtil.columns(idMappings);
            String properties = TextUtil.properties(idMappings);
            builder.append("(").append(columns).append(")=(").append(properties).append(")");
            for (Mapping mapping : idMappings) {
                builder.appendArg(ValueUtil.get(id, mapping.getProperty()));
            }
        }
    }

    public static void idsCondition(SqlBuilder builder, List<?> ids, List<Mapping> idMappings) {
        if (idMappings.size() == 1) {
            // 单个ID字段的数据直接转List
            Mapping mapping = idMappings.get(0);
            builder.append(mapping.getColumn()).append("in").append("(");
            for (int i = 0; i < ids.size(); i++) {
                Object id = ids.get(i);
                builder.appendArg(id);
                if (i < ids.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append(")");
        } else {
            // 多个ID字段的数据转List<JSONObject>
            JSONArray list = (JSONArray) JSON.toJSON(ids);
            builder.append("(").append(TextUtil.columns(idMappings)).append(")")
                    .append("in").append("(");
            String properties = TextUtil.properties(idMappings);
            for (int i = 0; i < list.size(); i++) {
                JSONObject id = list.getJSONObject(i);
                builder.append("(").append(properties).append(")");
                for (Mapping mapping : idMappings) {
                    builder.appendArg(ValueUtil.get(id, mapping.getProperty()));
                }
                if (i < list.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append(")");
        }
    }

    private static SqlBuilder batchBuilder(Class<?> cls, List<?> list, String type) {
        JSONArray array = (JSONArray) JSON.toJSON(list);
        List<Mapping> mappings = ModelUtil.listMapping(cls);
        SqlBuilder builder = SqlBuilder.build().append(type).append("into")
                .append(ModelUtil.table(cls)).append("(")
                .append(TextUtil.columns(mappings)).append(") values");
        String properties = "(" + TextUtil.properties(mappings) + ")";
        for (int i = 0; i < array.size(); i++) {
            JSONObject o = array.getJSONObject(i);
            builder.append(properties);
            for (Mapping mapping : mappings) {
                builder.appendArg(ValueUtil.get(o, mapping.getProperty()));
            }
            if (i < array.size() - 1) {
                builder.append(",");
            }
        }
        return builder;
    }

    private static SqlBuilder singleBuilder(Class<?> cls, JSONObject o, String type) {
        List<Mapping> mappings = ModelUtil.listMapping(cls);
        SqlBuilder builder = SqlBuilder.build().append(type).append("into")
                .append(ModelUtil.table(cls)).append("(")
                .append(TextUtil.columns(mappings)).append(") values (")
                .append(TextUtil.properties(mappings)).append(")");
        for (Mapping mapping : mappings) {
            builder.appendArg(ValueUtil.get(o, mapping.getProperty()));
        }
        return builder;
    }

}
