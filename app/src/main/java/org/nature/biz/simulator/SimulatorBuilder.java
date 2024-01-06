package org.nature.biz.simulator;

import org.nature.biz.model.Kline;
import org.nature.biz.model.Rule;

import java.util.List;

public class SimulatorBuilder {

    public static Simulator instance(Rule rule, List<Kline> list, List<String> dates) {
        String ruleType = rule.getRuleType();
        String date = rule.getDate();
        if ("0".equals(ruleType)) {
            return new GridSimulator(list, date, dates, rule.getBase(), rule.getRatio(), rule.getExpansion());
        }
        if ("1".equals(ruleType)) {
            return new GridSimulator(list, date, dates, rule.getBase(), rule.getRatio(), rule.getExpansion());
        }
        return new CompoundSimulator(list, date, dates, rule.getBase(), rule.getRatio(), rule.getExpansion());
    }

}
