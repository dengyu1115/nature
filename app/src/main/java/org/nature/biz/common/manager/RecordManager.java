package org.nature.biz.common.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.nature.biz.common.mapper.RecordMapper;
import org.nature.biz.common.model.Record;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;

/**
 * 记录
 * @author Nature
 * @version 1.0.0
 * @since 2024/7/16
 */
@Component
public class RecordManager {

    @Injection
    private RecordMapper recordMapper;

    public <T> T get(String code, String date, T t) {
        Record req = new Record();
        req.setCode(code);
        req.setDate(date);
        Record record = recordMapper.findById(req);
        if (record == null) {
            return t;
        }
        return JSON.parseObject(record.getContent(), new TypeReference<>() {
        });
    }

    public <T> void set(String code, String date, T t) {
        Record record = new Record();
        record.setCode(code);
        record.setDate(date);
        record.setContent(JSON.toJSONString(t));
        recordMapper.merge(record);
    }

}
