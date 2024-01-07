package org.nature.common.db.builder.source.annotated;

import org.nature.common.db.DB;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlBuilder;

import java.lang.reflect.Method;

/**
 * 删除处理source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class DeleteSource extends BaseAnnotatedSource {
    @Override
    public Object execute(Class<?> cls, String where, Method method, Object... args) {
        return DB.create(ModelUtil.db(cls)).executeUpdate(this.genSql(cls, where, method, args));
    }

    @Override
    protected SqlBuilder initSqlBuilder(Class<?> cls) {
        return SqlBuilder.build().append("delete")
                .append("from").append(ModelUtil.table(cls))
                .append("where");
    }

}
