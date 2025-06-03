package org.nature.biz.common.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.nature.biz.common.mapper.RecordMapper;
import org.nature.biz.common.model.Record;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;

/**
 * 记录管理器
 * 用于处理记录的读取和写入操作。
 * @author Nature
 * @version 1.0.0
 * @since 2024/7/16
 */
@Component
public class RecordManager {

    @Injection
    private RecordMapper recordMapper;

    /**
     * 获取指定代码和日期的记录内容
     * @param code 记录的标识符
     * @param date 记录的日期
     * @param t    默认返回值，当记录不存在时返回该值
     * @param type 返回值的类型引用
     * @return 返回解析后的记录内容对象
     */
    public <T> T get(String code, String date, T t, TypeReference<T> type) {
        // 创建查询条件
        Record req = new Record();
        req.setCode(code);
        req.setDate(date);
        // 查询记录
        Record record = recordMapper.findById(req);
        if (record == null) {
            return t;
        }
        // 解析并返回记录内容
        return JSON.parseObject(record.getContent(), type);
    }

    /**
     * 保存指定代码和日期的记录内容
     * @param code 记录的标识符
     * @param date 记录的日期
     * @param t    要保存的内容对象
     */
    public <T> void set(String code, String date, T t) {
        // 创建记录对象
        Record record = new Record();
        record.setCode(code);
        record.setDate(date);
        // 将对象转换为JSON字符串并保存
        record.setContent(JSON.toJSONString(t));
        recordMapper.merge(record);
    }

}
