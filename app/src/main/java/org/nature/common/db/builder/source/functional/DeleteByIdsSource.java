package org.nature.common.db.builder.source.functional;

import org.nature.common.db.DB;
import org.nature.common.db.builder.model.Mapping;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlAppender;
import org.nature.common.db.builder.util.SqlBuilder;

import java.util.List;

/**
 *  根据id集合批量删除数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class DeleteByIdsSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        List<?> ids = (List<?>) args[0];
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return DB.create(ModelUtil.db(cls)).executeUpdate(this.genSql(cls, ids));
    }

    private SqlBuilder genSql(Class<?> cls, List<?> ids) {
        List<Mapping> idMappings = ModelUtil.listIdMapping(cls);
        SqlBuilder builder = SqlBuilder.build().append("delete from")
                .append(ModelUtil.table(cls))
                .append("where");
        SqlAppender.idsCondition(builder, ids, idMappings);
        return builder;
    }

}