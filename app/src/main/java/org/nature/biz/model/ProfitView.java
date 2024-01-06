package org.nature.biz.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

@AllArgsConstructor
@Getter
@Setter
public class ProfitView extends BaseModel {
    private String title;
    private String title1;
    private String value1;
    private String title2;
    private String value2;
    private String title3;
    private String value3;
}
