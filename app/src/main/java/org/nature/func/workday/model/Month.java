package org.nature.func.workday.model;

import org.nature.common.model.BaseModel;

import java.util.HashMap;
import java.util.Map;

public class Month extends BaseModel {

    private final Map<String, String> dates = new HashMap<>();
    private String month;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setDateType(String date, String type) {
        dates.put(date, type);
    }

    public String getDateType(String date) {
        return dates.get(date);
    }
}
