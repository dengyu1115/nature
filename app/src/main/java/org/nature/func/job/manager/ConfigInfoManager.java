package org.nature.func.job.manager;

import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.func.job.mapper.ConfigInfoMapper;
import org.nature.func.job.model.ConfigInfo;

import java.util.List;

/**
 * 任务配置信息
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/3
 */
@Component
public class ConfigInfoManager {

    @Injection
    private ConfigInfoMapper configInfoMapper;

    /**
     * 保存
     * @param d 数据
     */
    public void save(ConfigInfo d) {
        ConfigInfo exists = configInfoMapper.findById(d);
        if (exists != null) {
            throw new Warn("数据已存在");
        }
        configInfoMapper.save(d);
    }

    /**
     * 编辑
     * @param d 保存
     */
    public void edit(ConfigInfo d) {
        // 更新数据
        configInfoMapper.merge(d);
    }

    /**
     * 删除
     * @param d 数据
     */
    public void delete(ConfigInfo d) {
        configInfoMapper.deleteById(d);
    }

    /**
     * 查询全部
     * @return list
     */
    public List<ConfigInfo> listAll() {
        return configInfoMapper.listAll();
    }
}
