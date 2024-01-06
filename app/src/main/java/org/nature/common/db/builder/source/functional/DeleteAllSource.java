package org.nature.common.db.builder.source.functional;

import org.nature.common.db.DB;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlBuilder;

public class DeleteAllSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        return DB.create(ModelUtil.db(cls)).executeUpdate(this.genSql(cls));
    }

    private SqlBuilder genSql(Class<?> cls) {
        return SqlBuilder.build().append("delete from").append(ModelUtil.table(cls));
    }

}
