package org.nature.common.db.builder.source.functional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nature.common.db.DB;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.builder.util.SqlAppender;

/**
 * 并入处理source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class MergeSource implements FunctionalSource {

    @Override
    public Object execute(Class<?> cls, Object... args) {
        JSONObject o = (JSONObject) JSON.toJSON(args[0]);
        DB db = DB.create(ModelUtil.db(cls));
        return db.executeUpdate(SqlAppender.mergeBuilder(cls, o));
    }

}
