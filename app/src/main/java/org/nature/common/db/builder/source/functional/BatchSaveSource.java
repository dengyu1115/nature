package org.nature.common.db.builder.source.functional;

import org.nature.common.db.DB;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlAppender;

import java.util.ArrayList;
import java.util.List;

public class BatchSaveSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        List<?> list = (List<?>) args[0];
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        DB db = DB.create(ModelUtil.db(cls));
        return db.batchExec(list, this.getBatch(cls), l -> db.executeUpdate(SqlAppender.batchSaveBuilder(cls, l)));
    }

    private int getBatch(Class<?> cls) {
        int size = ModelUtil.listIdMapping(cls).size();
        int i = 999 % size;
        return i == 0 ? 999 / size : 999 / size + 1;
    }

}

