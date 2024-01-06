package org.nature.common.db.proxy;

import org.nature.common.db.annotation.Delete;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.QueryOne;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.builder.source.annotated.DeleteSource;
import org.nature.common.db.builder.source.annotated.QueryListSource;
import org.nature.common.db.builder.source.annotated.QueryOneSource;
import org.nature.common.db.builder.source.definition.FunctionalSource;
import org.nature.common.db.builder.source.functional.*;
import org.nature.common.db.builder.source.table.CreateSource;
import org.nature.common.db.builder.util.ModelUtil;
import org.nature.common.db.function.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class MapperProxy {

    private static final Map<Class<?>, FunctionalSource> CONTEXT = new HashMap<>();
    private static final CreateSource CREATE_SOURCE = new CreateSource();
    private static final QueryOneSource QUERY_ONE_SOURCE = new QueryOneSource();
    private static final QueryListSource QUERY_LIST_SOURCE = new QueryListSource();
    private static final DeleteSource DELETE_SOURCE = new DeleteSource();

    static {
        CONTEXT.put(BatchMerge.class, new BatchMergeSource());
        CONTEXT.put(BatchSave.class, new BatchSaveSource());
        CONTEXT.put(DeleteAll.class, new DeleteAllSource());
        CONTEXT.put(DeleteById.class, new DeleteByIdSource());
        CONTEXT.put(DeleteByIds.class, new DeleteByIdsSource());
        CONTEXT.put(FindById.class, new FindByIdSource());
        CONTEXT.put(ListAll.class, new ListAllSource());
        CONTEXT.put(ListByIds.class, new ListByIdsSource());
        CONTEXT.put(Merge.class, new MergeSource());
        CONTEXT.put(Save.class, new SaveSource());
        CONTEXT.put(Update.class, new UpdateSource());
    }

    @SuppressWarnings("unchecked")
    public static <T> T instant(Class<T> mapper) {
        ClassLoader classLoader = MapperProxy.class.getClassLoader();
        TableModel tableModel = mapper.getAnnotation(TableModel.class);
        if (tableModel == null) {
            throw new RuntimeException(String.format("class %s should be marked with TableModel", mapper));
        }
        Class<?> cls = tableModel.value();
        CREATE_SOURCE.execute(cls, ModelUtil.getModel(cls).recreate());
        return (T) Proxy.newProxyInstance(classLoader, new Class<?>[]{mapper}, MapperProxy.getInvocationHandler(cls));
    }

    private static InvocationHandler getInvocationHandler(Class<?> cls) {
        return (proxy, method, args) -> {
            Class<?> dCls = method.getDeclaringClass();
            FunctionalSource source = CONTEXT.get(dCls);
            if (source != null) {
                return source.execute(cls, args);
            }
            QueryOne queryOne = method.getAnnotation(QueryOne.class);
            if (queryOne != null) {
                return QUERY_ONE_SOURCE.execute(cls, queryOne.where(), method, args);
            }
            QueryList queryList = method.getAnnotation(QueryList.class);
            if (queryList != null) {
                return QUERY_LIST_SOURCE.execute(cls, queryList.where(), method, args);
            }
            Delete delete = method.getAnnotation(Delete.class);
            if (delete != null) {
                return DELETE_SOURCE.execute(cls, delete.where(), method, args);
            }
            throw new RuntimeException(String.format("method not supported:%s", method));
        };
    }


}
