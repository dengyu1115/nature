package org.nature.common.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.page.Page;

/**
 * 页面信息
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/9
 */
@Getter
@Setter
public class PageInfo extends BaseModel {
    /**
     * 页面名称
     */
    private String name;
    /**
     * 对应的类
     */
    private Class<? extends Page> cls;
    /**
     * 序号
     */
    private int order;

}
