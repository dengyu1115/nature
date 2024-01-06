package org.nature.func.job.manager;

import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.func.job.mapper.ConfigInfoMapper;
import org.nature.func.job.model.ConfigInfo;

import java.util.List;

@Component
public class ConfigInfoManager {

    @Injection
    private ConfigInfoMapper configInfoMapper;

    public void delete(ConfigInfo d) {
        configInfoMapper.deleteById(d);
    }

    public void edit(ConfigInfo d) {
        ConfigInfo exists = configInfoMapper.findById(d);
        if (exists == null) {
            throw new Warn("数据不存在");
        }
        configInfoMapper.merge(d);
    }

    public void save(ConfigInfo d) {
        ConfigInfo exists = configInfoMapper.findById(d);
        if (exists != null) {
            throw new Warn("数据已存在");
        }
        configInfoMapper.save(d);
    }

    public List<ConfigInfo> listAll() {
        return configInfoMapper.listAll();
    }
}
