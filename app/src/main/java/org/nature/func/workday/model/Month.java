package org.nature.func.workday.model;

import org.nature.common.model.BaseModel;

import java.util.HashMap;
import java.util.Map;

/**
 * 月份
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/1
 */
public class Month extends BaseModel {

    private final Map<String, String> dateTypeMap = new HashMap<>();
    private String month;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setDateType(String date, String type) {
        dateTypeMap.put(date, type);
    }

    public String getDateType(String date) {
        return dateTypeMap.get(date);
    }
}
