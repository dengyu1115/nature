package org.nature.biz.mapper;


import org.nature.biz.model.Item;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;

/**
 * 项目
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
@TableModel(Item.class)
public interface ItemMapper extends FindById<Item, Item>, ListAll<Item>, Save<Item>, Merge<Item>, DeleteById<Item> {
}
