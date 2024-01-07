package org.nature.common.db.builder.source.functional;

import org.nature.common.db.DB;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlAppender;
import org.nature.common.db.builder.util.SqlBuilder;

/**
 * 根据id删除处理source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class DeleteByIdSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        return DB.create(ModelUtil.db(cls)).executeUpdate(this.genSql(cls, args[0]));
    }

    private SqlBuilder genSql(Class<?> cls, Object o) {
        SqlBuilder builder = SqlAppender.deleteBuilder(cls);
        SqlAppender.idCondition(builder, o, ModelUtil.listIdMapping(cls));
        return builder;
    }

}
