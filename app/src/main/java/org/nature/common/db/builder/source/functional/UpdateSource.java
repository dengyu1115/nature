package org.nature.common.db.builder.source.functional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nature.common.db.DB;
import org.nature.common.db.builder.model.Mapping;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlBuilder;
import org.nature.common.db.builder.util.TextUtil;
import org.nature.common.db.builder.util.ValueUtil;

import java.util.List;

/**
 * 更新处理source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class UpdateSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        JSONObject o = (JSONObject) JSON.toJSON(args[0]);
        DB db = DB.create(ModelUtil.db(cls));
        return db.executeUpdate(this.genSql(cls, o));
    }

    private SqlBuilder genSql(Class<?> cls, JSONObject o) {
        List<Mapping> nonIdMappings = ModelUtil.listNoneIdMapping(cls);
        List<Mapping> idMappings = ModelUtil.listIdMapping(cls);
        SqlBuilder builder = SqlBuilder.build().append("update")
                .append(ModelUtil.table(cls)).append("set")
                .append(TextUtil.conditions(nonIdMappings)).append("where")
                .append(TextUtil.conditions(idMappings));
        // 拼接set字段部分
        for (Mapping mapping : nonIdMappings) {
            builder.appendArg(ValueUtil.get(o, mapping.getProperty()));
        }
        // 拼接where条件部分（id）
        for (Mapping mapping : idMappings) {
            builder.appendArg(ValueUtil.get(o, mapping.getProperty()));
        }
        return builder;
    }

}
