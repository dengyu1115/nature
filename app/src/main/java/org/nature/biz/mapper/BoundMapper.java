package org.nature.biz.mapper;


import org.nature.biz.model.Bound;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;

/**
 * 债券
 * @author Nature
 * @version 1.0.0
 * @since 2024/4/4
 */
@TableModel(Bound.class)
public interface BoundMapper extends FindById<Bound, Bound>, ListAll<Bound>, Save<Bound>, Merge<Bound>, DeleteById<Bound> {
}
