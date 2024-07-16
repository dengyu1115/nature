package org.nature.biz.common.mapper;


import org.nature.biz.common.model.Record;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.FindById;
import org.nature.common.db.function.Merge;

/**
 * 记录
 * @author Nature
 * @version 1.0.0
 * @since 2024/7/15
 */
@TableModel(Record.class)
public interface RecordMapper extends Merge<Record>, FindById<Record, Record> {

}
