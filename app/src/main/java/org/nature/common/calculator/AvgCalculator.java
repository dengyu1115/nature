/*
package org.nature.common.calculator;

import org.nature.base.model.Avg;
import org.nature.base.model.Net;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class AvgCalculator {

    private static final int WEEK = 5, MONTH = 21, SEASON = 63, YEAR = 252;

    public static void cal(List<Net> list) {
        new Multi(list).cal();
    }

    private static class Multi {

        private final List<Net> list;

        private Multi(List<Net> list) {
            this.list = list;
        }

        private void cal() {
            List<Single> arr = Arrays.asList(
                    new Single(WEEK, (i, v) -> this.setAvg(i, v, Avg::setWeek)),
                    new Single(MONTH, (i, v) -> this.setAvg(i, v, Avg::setMonth)),
                    new Single(SEASON, (i, v) -> this.setAvg(i, v, Avg::setSeason)),
                    new Single(YEAR, (i, v) -> this.setAvg(i, v, Avg::setYear))
            );
            for (Net net : list) {
                for (Single avg : arr) {
                    avg.push(net);
                }
            }
        }

        private void setAvg(Net i, Double v, BiConsumer<Avg, Double> consumer) {
            if (i.getAvg() == null) {
                i.setAvg(new Avg());
            }
            consumer.accept(i.getAvg(), v);
        }
    }

    private static class Single {
        private final int scale, length;
        private final Double[] arr;
        private final BiConsumer<Net, Double> handle;
        private int index;
        private double total;

        private Single(int scale, BiConsumer<Net, Double> handle) {
            this.scale = scale;
            this.arr = new Double[this.length = scale - 1];
            this.handle = handle;
        }

        private void push(Net net) {
            Double latest = net.getNet().getLatest();
            total += latest;
            index++;
            if (index == length) {
                index = 0;
            }
            Double last = arr[index];
            if (last != null) {
                handle.accept(net, total / scale);
                total -= last;
            }
            arr[index] = latest;
        }
    }

}
*/
