package org.nature.common.db.builder.source.annotated;

import org.nature.common.db.DB;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlBuilder;
import org.nature.common.db.builder.util.TextUtil;

import java.lang.reflect.Method;

/**
 * 单挑数据查询处理source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class QueryOneSource extends BaseAnnotatedSource {

    @Override
    public Object execute(Class<?> cls, String where, Method method, Object... args) {
        return DB.create(ModelUtil.db(cls)).find(this.genSql(cls, where, method, args), ModelUtil.resultMap(cls));
    }

    @Override
    protected SqlBuilder initSqlBuilder(Class<?> cls) {
        return SqlBuilder.build().append("select")
                .append(TextUtil.columns(ModelUtil.listMapping(cls)))
                .append("from").append(ModelUtil.table(cls))
                .append("where");
    }

}
