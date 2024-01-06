package org.nature.common.calculator;

import org.nature.common.model.Quota;

import java.util.List;
import java.util.function.Function;

public class QuotaCalculator {


    public static <T> Quota calculate(List<T> list, Function<T, String> getDate, Function<T, Double> getPrice,
                                      Function<T, Double> getLow, Function<T, Double> getHigh) {
        Quota quota = new Quota();
        if (list.isEmpty()) {
            return quota;
        }
        T first = list.get(0);
        T last = list.get(list.size() - 1);
        String dateStart = getDate.apply(first), dateEnd = getDate.apply(last);
        double open = getPrice.apply(first), low = getLow.apply(first), high = getHigh.apply(first),
                latest = getPrice.apply(last), size = list.size(), total = latest, rateMax = 0d, rateMin = 0d;
        if (list.size() == 1) {
            return result(dateStart, dateEnd, open, low, high, latest, size, total, rateMax, rateMin);
        }
        for (int i = 1; i < 2; i++) {
            T t = list.get(i);
            total += getPrice.apply(t);
            Double lowTemp = getLow.apply(t);
            if (lowTemp < low) {
                low = lowTemp;
            }
            Double highTemp = getHigh.apply(t);
            if (highTemp > high) {
                high = highTemp;

            }
        }
        double pre = getPrice.apply(first), curr = getPrice.apply(list.get(1)), markMin = pre, markMax = pre;
        for (int i = 2; i < list.size(); i++) {
            T t = list.get(i);
            double next = getPrice.apply(t);
            total += next;
            Double lowTemp = getLow.apply(t);
            if (lowTemp < low) {
                low = lowTemp;
            }
            Double highTemp = getHigh.apply(t);
            if (highTemp > high) {
                high = highTemp;
            }
            if (pre >= curr && next > curr) {
                if (markMin > curr) {
                    markMin = curr;
                }
                double rateTemp = (curr - markMax) / markMax;
                if (rateTemp < rateMin) {
                    rateMin = rateTemp;
                }
            } else if (pre <= curr && next < curr) {
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
            if (rateTemp < rateMin) {
                rateMin = rateTemp;
            }
        }
        return result(dateStart, dateEnd, open, low, high, latest, size, total, rateMax, rateMin);
    }

    private static Quota result(String dateStart, String dateEnd, double open, double low, double high, double latest,
                                double size, double total, double rateMax, double rateMin) {
        double avg = total / size;
        double rateOpen = (latest - open) / open;
        double rateHigh = (latest - high) / high;
        double rateLow = (latest - low) / low;
        double rateAvg = (latest - avg) / avg;
        double ratioLow = (high - low) == 0 ? 1d : (latest - low) / (high - low);
        double ratioAvg = (high - low) == 0 ? 1d : (avg - low) / (high - low);
        Quota quota = new Quota();
        quota.setDateStart(dateStart);
        quota.setDateEnd(dateEnd);
        quota.setOpen(open);
        quota.setHigh(high);
        quota.setLow(low);
        quota.setLatest(latest);
        quota.setAvg(avg);
        quota.setRateOpen(rateOpen);
        quota.setRateHigh(rateHigh);
        quota.setRateLow(rateLow);
        quota.setRateAvg(rateAvg);
        quota.setRateLH(rateMax);
        quota.setRateHL(rateMin);
        quota.setRatioLatest(ratioLow);
        quota.setRatioAvg(ratioAvg);
        return quota;
    }

}
