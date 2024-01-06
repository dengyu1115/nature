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
        for (Mapping mapping : nonIdMappings) {
            builder.appendArg(ValueUtil.get(o, mapping.getProperty()));
        }
        for (Mapping mapping : idMappings) {
            builder.appendArg(ValueUtil.get(o, mapping.getProperty()));
        }
        return builder;
    }

}
