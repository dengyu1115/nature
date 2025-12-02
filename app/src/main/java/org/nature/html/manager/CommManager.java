package org.nature.html.manager;

import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.holder.JobHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommManager {

    public Object handle(String name, String param) {
        switch (name) {
            case "jobs":
                return this.jobs(param);
            case "":
                return this.latest(param);
        }
        throw new Warn("调用方法不支持：" + name);
    }

    private Object latest(String param) {
        return null;
    }

    private Object jobs(String param) {
        return JobHolder.jobs().stream().map(i -> {
            Map<String, String> map = new HashMap<>();
            map.put("code", i);
            map.put("name", JobHolder.getName(i));
            return map;
        }).collect(Collectors.toList());
    }

}
