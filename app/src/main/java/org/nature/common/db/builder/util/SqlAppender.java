package org.nature.common.db.builder.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nature.common.db.builder.model.Mapping;

import java.util.List;

/**
 * sql拼接器
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class SqlAppender {

    /**
     * 查询语句拼接
     * @param cls 类
     * @return SqlBuilder
     */
    public static SqlBuilder selectBuilder(Class<?> cls) {
        return SqlBuilder.build().append("select")
                .append(TextUtil.columns(ModelUtil.listMapping(cls)))
                .append("from").append(ModelUtil.table(cls))
                .append("where");
    }

    /**
     * 删除语句拼接
     * @param cls 类
     * @return SqlBuilder
     */
    public static SqlBuilder deleteBuilder(Class<?> cls) {
        return SqlBuilder.build().append("delete")
                .append("from").append(ModelUtil.table(cls))
                .append("where");
    }

    /**
     * 保存语句拼接
     * @param cls 类
     * @param o   数据
     * @return SqlBuilder
     */
    public static SqlBuilder saveBuilder(Class<?> cls, JSONObject o) {
        return singleBuilder(cls, o, "insert");
    }

    /**
     * 并入语句拼接
     * @param cls 类
     * @param o   数据
     * @return SqlBuilder
     */
    public static SqlBuilder mergeBuilder(Class<?> cls, JSONObject o) {
        return singleBuilder(cls, o, "replace");
    }

    /**
     * 批量保存语句拼接
     * @param cls  类
     * @param list 数据集
     * @return SqlBuilder
     */
    public static SqlBuilder batchSaveBuilder(Class<?> cls, List<?> list) {
        return batchBuilder(cls, list, "insert");
    }

    /**
     * 批量并入语句拼接
     * @param cls  类
     * @param list 数据集
     * @return SqlBuilder
     */
    public static SqlBuilder batchMergeBuilder(Class<?> cls, List<?> list) {
        return batchBuilder(cls, list, "replace");
    }

    /**
     * 拼接id条件子句
     * @param builder    builder
     * @param o          数据
     * @param idMappings id映射数据集
     */
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

    /**
     * 拼接id条件子句
     * @param builder    builder
     * @param ids        id集合
     * @param idMappings id映射数据集
     */
    public static void idsCondition(SqlBuilder builder, List<?> ids, List<Mapping> idMappings) {
        if (idMappings.size() == 1) {
            // 单个ID字段的数据直接转List
            Mapping mapping = idMappings.get(0);
            builder.append(mapping.getColumn()).append("in").append("(");
            for (int i = 0; i < ids.size(); i++) {
                Object id = ids.get(i);
                builder.appendArg(id);
                builder.append("?");
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

    /**
     * 批量拼接
     * @param cls  类
     * @param list 数据集合
     * @param type 类型
     * @return SqlBuilder
     */
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

    /**
     * 单挑拼接
     * @param cls  类
     * @param o    数据
     * @param type 类型
     * @return SqlBuilder
     */
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
