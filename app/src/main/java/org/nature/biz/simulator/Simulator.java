package org.nature.biz.simulator;

import org.nature.biz.model.Hold;
import org.nature.biz.model.Profit;

import java.util.List;

public interface Simulator {

    void calc();

    List<Hold> getHoldList();

    Profit profit();

    List<Profit> profits();

    List<Hold> latestHandle();

    List<Hold> nextHandle(int count);
    
}
