package org.nature.common.db.builder.source.functional;

import org.nature.common.db.DB;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlAppender;

import java.util.List;

/**
 * 批量并入处理source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class BatchMergeSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        List<?> list = (List<?>) args[0];
        // 参数为空的情况返回0
        if (list == null || list.isEmpty()) {
            return 0;
        }
        DB db = DB.create(ModelUtil.db(cls));
        // 执行批量并入处理
        return db.batchExec(list, this.getBatch(cls), l -> db.executeUpdate(SqlAppender.batchMergeBuilder(cls, l)));
    }

    /**
     * 获取批量并入处理批次
     * @param cls 类
     * @return int
     */
    private int getBatch(Class<?> cls) {
        int size = ModelUtil.listIdMapping(cls).size();
        int i = 999 % size;
        // 计算批次：最大处理字段数999/字段数（余数等于0，则直接返回size）
        return i == 0 ? 999 / size : 999 / size + 1;
    }

}

