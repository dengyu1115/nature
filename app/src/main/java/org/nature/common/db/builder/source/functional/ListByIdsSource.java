package org.nature.common.db.builder.source.functional;

import org.nature.common.db.DB;
import org.nature.common.db.builder.model.Mapping;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlAppender;
import org.nature.common.db.builder.util.SqlBuilder;
import org.nature.common.db.builder.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class ListByIdsSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        List<?> ids = (List<?>) args[0];
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        DB db = DB.create(ModelUtil.db(cls));
        return db.list(this.genSql(cls, ids), ModelUtil.resultMap(cls));
    }

    private SqlBuilder genSql(Class<?> cls, List<?> ids) {
        List<Mapping> idMappings = ModelUtil.listIdMapping(cls);
        List<Mapping> mappings = ModelUtil.listMapping(cls);
        SqlBuilder builder = SqlBuilder.build().append("select")
                .append(TextUtil.columns(mappings))
                .append("from").append(ModelUtil.table(cls))
                .append("where");
        SqlAppender.idsCondition(builder, ids, idMappings);
        return builder;
    }

}
