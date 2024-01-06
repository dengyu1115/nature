package org.nature.common.db.builder.source.functional;

import org.nature.common.db.builder.model.Mapping;
import org.nature.common.db.DB;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlBuilder;
import org.nature.common.db.builder.util.TextUtil;

import java.util.List;

public class ListAllSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        return DB.create(ModelUtil.db(cls)).list(this.genSql(cls), ModelUtil.resultMap(cls));
    }

    private SqlBuilder genSql(Class<?> cls) {
        List<Mapping> mappings = ModelUtil.listMapping(cls);
        return SqlBuilder.build().append("select")
                .append(TextUtil.columns(mappings))
                .append("from").append(ModelUtil.table(cls));
    }
}
