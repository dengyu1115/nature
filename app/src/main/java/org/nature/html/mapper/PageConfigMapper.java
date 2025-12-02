package org.nature.html.mapper;


import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.FindById;
import org.nature.common.db.function.Merge;
import org.nature.html.model.PageConfig;

/**
 * 页面配置
 * @author Nature
 * @version 1.0.0
 * @since 2025/11/06
 */
@TableModel(PageConfig.class)
public interface PageConfigMapper extends Merge<PageConfig>, FindById<PageConfig, String> {

}
