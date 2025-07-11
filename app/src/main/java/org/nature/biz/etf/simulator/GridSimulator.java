package org.nature.biz.etf.simulator;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.nature.biz.common.model.Kline;
import org.nature.biz.etf.model.Hold;
import org.nature.biz.etf.model.Profit;
import org.nature.common.exception.Warn;
import org.nature.common.util.DateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 网格交易模拟器
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/11
 */
public class GridSimulator implements Simulator {
    /**
     * 小数位数
     */
    public static final int SCALE = 3;
    /**
     * 小数位数
     */
    public static final int SCALE_PROFIT = SCALE + 1;
    /**
     * 百
     */
    public static final BigDecimal HUNDRED = new BigDecimal("100");
    /**
     * 金额基准
     */
    private final BigDecimal amountBase;
    /**
     * 买比例
     */
    private final BigDecimal percentBuy;
    /**
     * 卖比例
     */
    private final BigDecimal percentSell;
    /**
     * 扩增比例
     */
    private final BigDecimal expansion;
    /**
     * K线数据
     */
    private final List<Kline> list;
    /**
     * 持仓数据
     */
    private final SortedSet<Hold> holds;
    /**
     * 临时持仓数据
     */
    private final SortedSet<Hold> holdsTemp;
    /**
     * 持仓数据
     */
    @Getter
    private final List<Hold> holdList;
    /**
     * 收益数据
     */
    private final List<Profit> profits;
    /**
     * 利润（已卖出）
     */
    private BigDecimal profitSold;
    /**
     * 已支付金额（总额）
     */
    private BigDecimal paidTotal;
    /**
     * 已支付金额（剩余）
     */
    private BigDecimal paidLeft;
    /**
     * 已支付金额（最大）
     */
    private BigDecimal paidMax;
    /**
     * 已回收金额
     */
    private BigDecimal returned;
    /**
     * 总份额
     */
    private BigDecimal shareTotal;
    /**
     * 上一价格
     */
    private BigDecimal last;
    /**
     * 最高价、最低价、收益率
     */
    private BigDecimal max, min, profitRatio;
    /**
     * 买次数、卖次数、当天第几次操作
     */
    private int timesBuy, timesSell, level;
    /**
     * 当前处理K线数据
     */
    private Kline curr;
    /**
     * 日期数据
     */
    private List<String> dates;
    /**
     * 当前日期
     */
    private String date, start, dateStart, dateEnd;

    public GridSimulator(List<Kline> list, String date, List<String> dates,
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
        this.returned = BigDecimal.ZERO;
        this.shareTotal = BigDecimal.ZERO;
        this.amountBase = amountBase;
        this.percentBuy = BigDecimal.ONE.subtract(percent);
        this.percentSell = BigDecimal.ONE.add(percent);
        this.expansion = expansion;
        this.profits = new ArrayList<>();
    }

    /**
     * 模拟
     */
    public void calc() {
        // 数据为空
        if (list.isEmpty()) {
            return;
        }
        // 开始日期
        if (dateStart == null) {
            dateStart = list.get(0).getDate();
        }
        // 结束日期
        if (dateEnd == null) {
            dateEnd = list.get(list.size() - 1).getDate();
        }
        // 开始日期
        start = dateStart;
        // 日期数据
        dates = dates.stream().filter(i -> start.compareTo(i) <= 0).collect(Collectors.toList());
        if (dates.isEmpty()) {
            return;
        }
        // 当前日期
        this.date = dates.get(0);
        // 遍历处理
        this.list.forEach(i -> {
            // 记录收益数据
            this.recordProfit(i);
            // 当前处理数据
            this.curr = i;
            this.level = 0;
            // 在设定的日期区间才处理
            if (dateStart.compareTo(i.getDate()) <= 0 && dateEnd.compareTo(i.getDate()) >= 0) {
                // 买操作
                this.buy();
                // 卖操作
                this.sell();
                // 合并持仓数据，在既有买入又有卖出的情况下，新买的得重置价格
                this.mergeHolds();
                // 计算上次处理的价格
                this.calcLast();
            }
            // 记录最高价、最低价、收益率
            this.recordMaxMin();
        });
        // 日期为null了说明模拟结束
        if (date == null) {
            return;
        }
        // 模拟过程中没有记录的收益数据则做个汇总计算
        if (profits.isEmpty()) {
            profits.add(this.calcProfit());
        } else {
            Profit profit = profits.get(profits.size() - 1);
            if (profit.getDate().compareTo(date) < 0) {
                profits.add(this.calcProfit());
            }
        }
    }

    /**
     * 获取最新操作持仓数据
     * @return 最新操作持仓数据
     */
    public List<Hold> latestHandle() {
        // 最新日期
        String date = this.curr.getDate();
        // 取最新日期的操作数据
        return holdList.stream().filter(i -> date.equals(i.getDateBuy()) || date.equals(i.getDateSell()))
                .collect(Collectors.toList());
    }

    /**
     * 获取下一操作持仓数据
     * @param count 条数
     * @return 下一操作持仓数据
     */
    public List<Hold> nextHandle(int count) {
        // 未卖出的数据
        List<Hold> holds = holdList.stream().filter(i -> i.getDateSell() == null)
                .sorted(Comparator.comparing(Hold::getDateBuy).reversed().thenComparing(Hold::getMark))
                .collect(Collectors.toList());
        // 最新的那次操作持仓数据
        Hold latest = holds.stream().findFirst().orElse(null);
        // 即没有最新的操作也不存在上次操作数据
        if (latest == null && last == null) {
            return new ArrayList<>();
        }
        // 计算当前的标记价格
        BigDecimal mark = last == null ? latest.getMark() : last;
        List<Hold> results = new ArrayList<>();
        // 要买的数据生成
        for (int i = 0; i < count; i++) {
            results.add(this.build(mark = mark.multiply(percentBuy).setScale(SCALE, RoundingMode.FLOOR)));
        }
        // 要卖的数据生成
        int i = Math.min(holds.size(), count);
        for (Hold hold : holds.subList(0, i)) {
            hold.setDateSell(DateUtil.today());
            hold.setPriceSell(hold.getMark().multiply(percentSell).setScale(SCALE, RoundingMode.CEILING));
            hold.setProfit(hold.getPriceSell().subtract(hold.getPriceBuy()).multiply(hold.getShareBuy()));
            results.add(hold);
        }
        return results;
    }

    /**
     * 获取最新收益数据
     * @return 最新的Profit对象，包含所有统计指标
     */
    public Profit profit() {
        if (profits.isEmpty()) {
            return null;
        }
        return profits.get(profits.size() - 1);
    }

    /**
     * 获取所有收益数据
     * @return 所有Profit对象，包含所有统计指标
     */
    public List<Profit> profits() {
        return this.profits;
    }

    /**
     * 记录收益数据
     * @param i K线对象
     */
    private void recordProfit(Kline i) {
        // 还没开始模拟计算
        if (date == null || i.getDate().compareTo(date) <= 0) {
            return;
        }
        if (date.compareTo(dateStart) < 0) {
            return;
        }
        // 计算收益数据
        profits.add(this.calcProfit());
        start = i.getDate();
        int index = dates.indexOf(date);
        if (index < dates.size() - 1) {
            date = dates.get(index + 1);
        } else {
            date = null;
        }
    }

    /**
     * 计算收益数据
     * @return 收益统计对象
     */
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
        profit.setProfitTotal(profitSold.add(profitHold));
        profit.setProfitHold(profitHold);
        profit.setProfitSold(profitSold);
        profit.setProfitRatio(paidMax.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : profitSold.divide(paidMax, SCALE_PROFIT, RoundingMode.HALF_UP));
        return profit;
    }

    /**
     * 买入操作
     */
    private void buy() {
        this.level = 0;
        while (this.doBuy()) {
            this.level++;
        }
    }

    /**
     * 卖出操作
     */
    private void sell() {
        while (true) {
            if (!this.doSell()) {
                break;
            }
        }
    }

    /**
     * 合并吃餐数据
     */
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

    /**
     * 买操作
     * @return 是否成功
     */
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
        timesBuy++;
        return true;
    }

    /**
     * 卖出操作
     * @return 是否成功
     */
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
        first.setShareSell(first.getShareBuy());
        first.setPriceSell(priceSell);
        first.setProfit(profit);
        this.profitSold = this.profitSold.add(profit);
        if (holds.size() == 1) {
            this.last = holds.first().getMark();
        }
        this.holds.remove(first);
        BigDecimal money = priceBuy.multiply(share);
        shareTotal = shareTotal.subtract(share);
        paidLeft = paidLeft.subtract(money);
        returned = returned.add(money);
        timesSell++;
        return true;
    }

    /**
     * 计算上次操作的持仓对象
     */
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

    /**
     * 计算份额
     * @param price 价格
     * @return 份额
     */
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

    /**
     * 记录最高最低价收益率
     */
    private void recordMaxMin() {
        BigDecimal latest = curr.getLatest();
        if (this.max == null || this.max.compareTo(latest) < 0) {
            this.max = latest;
        }
        if (this.min == null || this.min.compareTo(latest) > 0) {
            this.min = latest;
        }
        if (paidMax.compareTo(BigDecimal.ZERO) == 0) {
            this.profitRatio = BigDecimal.ZERO;
            return;
        }
        BigDecimal ratio = profitSold.divide(paidMax, SCALE_PROFIT, RoundingMode.HALF_UP);
        if (ratio.compareTo(profitRatio) > 0) {
            this.profitRatio = ratio;
        }
    }

    /**
     * 构建持仓数据
     * @param price 价格
     * @return Hold 持仓对象
     */
    private Hold build(BigDecimal price) {
        Hold hold = new Hold();
        hold.setCode(curr.getCode());
        hold.setType(curr.getType());
        hold.setMark(price);
        hold.setPriceBuy(price);
        hold.setDateBuy(DateUtil.today());
        hold.setShareBuy(this.calcShare(price));
        return hold;
    }

}
