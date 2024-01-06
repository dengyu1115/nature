package org.nature.biz.simulator;

import org.apache.commons.lang3.StringUtils;
import org.nature.biz.model.Hold;
import org.nature.biz.model.Kline;
import org.nature.biz.model.Profit;
import org.nature.common.exception.Warn;
import org.nature.common.util.CommonUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.CEILING;

public class GridExSimulator implements Simulator {

    public static final int SCALE = 3;
    public static final int SCALE_PROFIT = SCALE + 1;
    public static final BigDecimal HUNDRED = new BigDecimal("100");

    private final BigDecimal amountBase;
    private final BigDecimal percentBuy;
    private final BigDecimal percentSell;
    private final BigDecimal expansion;
    private final List<Kline> list;
    private final SortedSet<Hold> holds;
    private final SortedSet<Hold> holdsTemp;
    private final List<Hold> holdList;
    private final List<Profit> profits;

    private BigDecimal profitSold;
    private BigDecimal paidTotal;
    private BigDecimal paidLeft;
    private BigDecimal paidMax;
    private BigDecimal markLeft;
    private BigDecimal markMax;
    private BigDecimal returned;
    private BigDecimal shareTotal;
    private BigDecimal last;
    private BigDecimal max, min, profitRatio;

    private int timesBuy, timesSell, level;

    private Kline curr;
    private List<String> dates;
    private String date, start, dateStart, dateEnd;

    public GridExSimulator(List<Kline> list, String date, List<String> dates,
                           BigDecimal amountBase, BigDecimal percent, BigDecimal expansion) {
        if (dates == null) {
            throw new Warn("日期数据为null");
        }
        dates = dates.stream().filter(StringUtils::isNotBlank).distinct().sorted().collect(Collectors.toList());
        if (dates.isEmpty()) {
            throw new Warn("日期数据为空");
        }
        this.list = list;
        this.dateStart = date;
        this.dateEnd = dates.get(dates.size() - 1);
        this.dates = dates;
        this.holds = new TreeSet<>(Comparator.comparing(Hold::getMark));
        this.holdsTemp = new TreeSet<>(Comparator.comparing(Hold::getMark));
        this.holdList = new ArrayList<>();
        this.profitSold = BigDecimal.ZERO;
        this.paidTotal = BigDecimal.ZERO;
        this.paidLeft = BigDecimal.ZERO;
        this.paidMax = BigDecimal.ZERO;
        this.markLeft = BigDecimal.ZERO;
        this.markMax = BigDecimal.ZERO;
        this.returned = BigDecimal.ZERO;
        this.shareTotal = BigDecimal.ZERO;
        this.amountBase = amountBase;
        this.percentBuy = BigDecimal.ONE.subtract(percent);
        this.percentSell = BigDecimal.ONE.add(percent);
        this.expansion = expansion;
        this.profits = new ArrayList<>();
    }

    public void calc() {
        if (list.isEmpty()) {
            return;
        }
        if (dateStart == null) {
            dateStart = list.get(0).getDate();
        }
        if (dateEnd == null) {
            dateEnd = list.get(list.size() - 1).getDate();
        }
        start = dateStart;
        dates = dates.stream().filter(i -> start.compareTo(i) <= 0).collect(Collectors.toList());
        if (dates.isEmpty()) {
            return;
        }
        this.date = dates.get(0);
        this.list.forEach(i -> {
            this.recordProfit(i);
            this.curr = i;
            this.level = 0;
            if (dateStart.compareTo(i.getDate()) <= 0 && dateEnd.compareTo(i.getDate()) >= 0) {
                this.buy();
                this.sell();
                this.mergeHolds();
                this.calcLast();
            }
            this.recordMaxMin();
        });
        if (date == null) {
            return;
        }
        if (profits.isEmpty()) {
            profits.add(this.calcProfit());
        } else {
            Profit profit = profits.get(profits.size() - 1);
            if (profit.getDate().compareTo(date) < 0) {
                profits.add(this.calcProfit());
            }
        }
    }

    public List<Hold> getHoldList() {
        return this.holdList;
    }

    public List<Hold> latestHandle() {
        String date = this.curr.getDate();
        return holdList.stream().filter(i -> date.equals(i.getDateBuy()) || date.equals(i.getDateSell()))
                .collect(Collectors.toList());
    }

    public List<Hold> nextHandle(int count) {
        List<Hold> holds = holdList.stream().filter(i -> i.getDateSell() == null)
                .sorted(Comparator.comparing(Hold::getDateBuy).reversed().thenComparing(Hold::getMark))
                .collect(Collectors.toList());
        Hold latest = holds.stream().findFirst().orElse(null);
        if (latest == null && last == null) {
            return new ArrayList<>();
        }
        BigDecimal mark = last == null ? latest.getMark() : last;
        List<Hold> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(this.build(mark = mark.multiply(percentBuy).setScale(SCALE, RoundingMode.FLOOR)));
        }
        int i = Math.min(holds.size(), count);
        for (Hold hold : holds.subList(0, i)) {
            hold.setDateSell(CommonUtil.today());
            BigDecimal priceSell = hold.getMark().multiply(percentSell).setScale(SCALE, CEILING);
            hold.setPriceSell(priceSell);
            hold.setShareSell(this.calcShare(priceSell));
            hold.setProfit(hold.getPriceSell().subtract(hold.getPriceBuy()).multiply(hold.getShareBuy()));
            results.add(hold);
        }
        return results;
    }

    public Profit profit() {
        if (profits.isEmpty()) {
            return null;
        }
        return profits.get(profits.size() - 1);
    }

    public List<Profit> profits() {
        return this.profits;
    }

    private void recordProfit(Kline i) {
        if (date == null || i.getDate().compareTo(date) <= 0) {
            return;
        }
        if (date.compareTo(dateStart) < 0) {
            return;
        }
        profits.add(this.calcProfit());
        start = i.getDate();
        int index = dates.indexOf(date);
        if (index < dates.size() - 1) {
            date = dates.get(index + 1);
        } else {
            date = null;
        }
    }

    private Profit calcProfit() {
        BigDecimal profitHold = shareTotal.multiply(curr.getLatest()).subtract(paidLeft);
        Profit profit = new Profit();
        profit.setDate(date);
        profit.setDateStart(start);
        profit.setDateEnd(curr.getDate());
        profit.setTimesBuy(timesBuy);
        profit.setTimesSell(timesSell);
        profit.setPaidTotal(paidTotal);
        profit.setPaidMax(paidMax);
        profit.setPaidLeft(paidLeft);
        profit.setReturned(returned);
        profit.setShareTotal(shareTotal);
        profit.setProfitTotal(profitHold);
        profit.setProfitHold(profitHold);
        profit.setProfitSold(profitSold);
        profit.setProfitRatio(markMax.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : profitSold.divide(markMax, SCALE_PROFIT, RoundingMode.HALF_UP));
        return profit;
    }

    private void buy() {
        this.level = 0;
        while (this.doBuy()) {
            this.level++;
        }
    }

    private void sell() {
        while (true) {
            if (!this.doSell()) {
                break;
            }
        }
    }

    private void mergeHolds() {
        if (holds.isEmpty()) {
            holds.addAll(holdsTemp);
            holdsTemp.clear();
        } else {
            while (!holdsTemp.isEmpty()) {
                Hold hold = holdsTemp.last();
                holdsTemp.remove(hold);
                hold.setMark(holds.first().getMark().multiply(percentBuy).setScale(SCALE, RoundingMode.FLOOR));
                holds.add(hold);
            }
        }
    }

    private boolean doBuy() {
        if (holdsTemp.isEmpty() && this.last == null) {
            return false;
        }
        BigDecimal low = curr.getLow();
        BigDecimal mark = holdsTemp.isEmpty() ? last : holdsTemp.first().getMark();
        BigDecimal target = mark.multiply(percentBuy).setScale(SCALE, RoundingMode.FLOOR);
        if (low.compareTo(target) > 0) {
            return false;
        }
        BigDecimal open = curr.getOpen();
        BigDecimal price = target.compareTo(open) > 0 ? open : target;
        BigDecimal share = this.calcShare(price);
        Hold hold = new Hold();
        hold.setCode(curr.getCode());
        hold.setType(curr.getType());
        hold.setDateBuy(curr.getDate());
        hold.setLevel(level);
        hold.setMark(target);
        hold.setPriceBuy(price);
        hold.setShareBuy(share);
        hold.setReason(holds.isEmpty() ? "empty" : "compare");
        holdsTemp.add(hold);
        holdList.add(hold);
        BigDecimal money = price.multiply(share);
        shareTotal = shareTotal.add(share);
        paidTotal = paidTotal.add(money);
        paidLeft = paidLeft.add(money);
        if (paidLeft.compareTo(paidMax) > 0) {
            paidMax = paidLeft;
        }
        markLeft = markLeft.add(money);
        if (markLeft.compareTo(markMax) > 0) {
            markMax = markLeft;
        }
        timesBuy++;
        return true;
    }

    private boolean doSell() {
        if (holds.isEmpty()) {
            return false;
        }
        Hold first = holds.first();
        BigDecimal mark = first.getMark();
        BigDecimal priceBuy = first.getPriceBuy();
        BigDecimal share = first.getShareBuy();
        BigDecimal target = mark.multiply(percentSell).setScale(SCALE, RoundingMode.CEILING);
        BigDecimal high = curr.getHigh();
        if (target.compareTo(high) > 0) {
            return false;
        }
        BigDecimal open = curr.getOpen();
        BigDecimal priceSell = target.compareTo(open) < 0 ? open : target;
        BigDecimal profit = priceSell.subtract(priceBuy).multiply(share);
        first.setDateSell(curr.getDate());
        BigDecimal shareSell = this.calcShare(priceSell);
        first.setShareSell(shareSell);
        first.setPriceSell(priceSell);
        first.setProfit(profit);
        this.profitSold = this.profitSold.add(profit);
        if (holds.size() == 1) {
            this.last = holds.first().getMark();
        }
        this.holds.remove(first);
        BigDecimal money = priceBuy.multiply(shareSell);
        BigDecimal moneyMark = priceBuy.multiply(share);
        shareTotal = shareTotal.subtract(shareSell);
        paidLeft = paidLeft.subtract(money);
        returned = returned.add(money);
        markLeft = markLeft.subtract(moneyMark);
        timesSell++;
        return true;
    }

    private void calcLast() {
        if (!this.holds.isEmpty()) {
            this.last = holds.first().getMark();
            return;
        }
        BigDecimal latest = curr.getLatest();
        if (this.last == null || latest.compareTo(this.last) > 0) {
            this.last = latest;
        }
    }

    private BigDecimal calcShare(BigDecimal price) {
        BigDecimal base = this.max == null || this.min == null ? BigDecimal.ZERO : this.max.subtract(this.min);
        BigDecimal decimal = this.min == null ? BigDecimal.ZERO : price.subtract(this.min);
        BigDecimal ratio = base.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE :
                decimal.divide(base, SCALE, RoundingMode.HALF_UP);
        if (ratio.compareTo(BigDecimal.ZERO) < 0) {
            ratio = BigDecimal.ZERO;
        }
        BigDecimal expansionRatio = BigDecimal.ONE.add(BigDecimal.ONE.subtract(ratio).multiply(expansion));
        return amountBase.multiply(BigDecimal.ONE.add(profitRatio)).multiply(expansionRatio)
                .divide(price.multiply(HUNDRED), 0, RoundingMode.CEILING).multiply(HUNDRED);
    }

    private void recordMaxMin() {
        BigDecimal latest = curr.getLatest();
        if (this.max == null || this.max.compareTo(latest) < 0) {
            this.max = latest;
        }
        if (this.min == null || this.min.compareTo(latest) > 0) {
            this.min = latest;
        }
        if (markMax.compareTo(BigDecimal.ZERO) == 0) {
            this.profitRatio = BigDecimal.ZERO;
            return;
        }
        BigDecimal ratio = profitSold.divide(markMax, SCALE_PROFIT, RoundingMode.HALF_UP);
        if (ratio.compareTo(profitRatio) > 0) {
            this.profitRatio = ratio;
        }
    }

    private Hold build(BigDecimal price) {
        Hold hold = new Hold();
        hold.setCode(curr.getCode());
        hold.setType(curr.getType());
        hold.setMark(price);
        hold.setPriceBuy(price);
        hold.setDateBuy(CommonUtil.today());
        hold.setShareBuy(this.calcShare(price));
        return hold;
    }

}
