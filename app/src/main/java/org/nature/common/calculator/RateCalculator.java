package org.nature.common.calculator;

import org.nature.common.model.RateResult;

import java.util.List;
import java.util.function.Function;

public class RateCalculator {

    private static final DoCompare GATHER = (a, b) -> a > b;
    private static final DoCompare LESS = (a, b) -> a < b;

    public static <T> RateResult cal(List<T> list, Function<T, Double> func) {
        return new Duplicate<T>(list, func).cal();
    }

    public static <T> double max(List<T> list, Function<T, Double> func) {
        return new Single<T>(list, func, GATHER).cal();
    }

    public static <T> double min(List<T> list, Function<T, Double> func) {
        return new Single<T>(list, func, LESS).cal();
    }

    @FunctionalInterface
    private interface DoCompare {
        boolean apply(double a, double b);
    }

    private static class Single<T> {

        private final List<T> list;

        private final Function<T, Double> func;

        private final DoCompare compare;

        private Single(List<T> list, Function<T, Double> func, DoCompare compare) {
            this.list = list;
            this.func = func;
            this.compare = compare;
        }

        private double cal() {
            if (list.size() < 2) {
                return 0;
            }
            double rate = 0, pre = func.apply(list.get(0)), curr = func.apply(list.get(1)), mark = pre;
            for (int i = 2; i < list.size(); i++) {
                double next = func.apply(list.get(i));
                if ((pre == curr || this.compare.apply(pre, curr))
                        && this.compare.apply(next, curr) && this.compare.apply(mark, curr)) {
                    mark = curr;
                } else if ((pre == curr || this.compare.apply(curr, pre)) && this.compare.apply(curr, next)) {
                    double rateTemp = (curr - mark) / mark;
                    if (this.compare.apply(rateTemp, rate)) {
                        rate = rateTemp;
                    }
                }
                pre = curr;
                curr = next;
            }
            if (this.compare.apply(curr, pre)) {
                double rateTemp = (curr - mark) / mark;
                if (this.compare.apply(rateTemp, rate)) {
                    rate = rateTemp;
                }
            }
            return rate;
        }
    }

    private static class Duplicate<T> {

        private final List<T> list;

        private final Function<T, Double> func;

        private Duplicate(List<T> list, Function<T, Double> func) {
            this.list = list;
            this.func = func;
        }

        private RateResult cal() {
            if (list.size() < 2) {
                return new RateResult(0, 0);
            }
            double pre = func.apply(list.get(0)), curr = func.apply(list.get(1)), rateMax = 0,
                    rateMin = 0, markMin = pre, markMax = pre;
            for (int i = 2; i < list.size(); i++) {
                double next = func.apply(list.get(i));
                if (pre >= curr && next > curr) {
                    if (markMin > curr) {
                        markMin = curr;
                    }
                    double rateTemp = (curr - markMax) / markMax;
                    if (rateTemp < rateMin) {
                        rateMin = rateTemp;
                    }
                } else if (pre <= curr && curr > next) {
                    if (markMax < curr) {
                        markMax = curr;
                    }
                    double rateTemp = (curr - markMin) / markMin;
                    if (rateTemp > rateMax) {
                        rateMax = rateTemp;
                    }
                }
                pre = curr;
                curr = next;
            }
            if (curr > pre) {
                double rateTemp = (curr - markMin) / markMin;
                if (rateTemp > rateMax) {
                    rateMax = rateTemp;
                }
            } else if (curr < pre) {
                double rateTemp = (curr - markMax) / markMax;
                if (rateTemp > rateMin) {
                    rateMin = rateTemp;
                }
            }
            return new RateResult(rateMax, rateMin);
        }
    }
}
