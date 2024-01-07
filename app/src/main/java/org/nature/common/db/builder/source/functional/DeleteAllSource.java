package org.nature.common.db.builder.source.functional;

import org.nature.common.db.DB;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlBuilder;

/**
 * 全量删除处理source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class DeleteAllSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        return DB.create(ModelUtil.db(cls)).executeUpdate(this.genSql(cls));
    }

    /**
     * 生成删除sql语句
     * @param cls 类
     * @return SqlBuilder
     */
    private SqlBuilder genSql(Class<?> cls) {
        return SqlBuilder.build().append("delete from").append(ModelUtil.table(cls));
    }

}
