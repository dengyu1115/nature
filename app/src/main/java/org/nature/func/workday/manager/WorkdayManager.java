package org.nature.func.workday.manager;

import android.annotation.SuppressLint;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nature.common.constant.Const;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.DateUtil;
import org.nature.common.util.HttpUtil;
import org.nature.func.workday.mapper.WorkdayMapper;
import org.nature.func.workday.model.Month;
import org.nature.func.workday.model.Workday;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作日
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/1
 */
@SuppressLint("DefaultLocale")
@Component
public class WorkdayManager {

    private static final String URL_HOLIDAY = "https://tool.bitefu.net/jiari/?d=%s";

    private static final String TYPE_HOLIDAY = "H";

    private static final String TYPE_WORKDAY = "W";

    @Injection
    private WorkdayMapper workdayMapper;

    /**
     * 重载工作日数据
     * @param year 年
     * @return int
     */
    public int reload(String year) {
        workdayMapper.deleteByYear(year);
        return workdayMapper.batchSave(this.getYearWorkDays(year));
    }

    /**
     * 加载工作日数据
     * @param year 年
     * @return int
     */
    public int load(String year) {
        List<Workday> list = workdayMapper.listByYear(year);
        if (!list.isEmpty()) {
            throw new Warn("已存在数据");
        }
        return workdayMapper.batchSave(this.getYearWorkDays(year));
    }

    /**
     * 是否工作日判断
     * @return boolean
     */
    public boolean isWorkday() {
        Workday workday = workdayMapper.findById(DateUtil.today());
        if (workday == null) {
            return false;
        }
        return workday.getType().equals(TYPE_WORKDAY);
    }

    /**
     * 上一工作日
     * @param date 日期
     * @return String
     */
    public String lastWorkday(String date) {
        Workday workday = workdayMapper.findLastWorkday(date);
        if (workday == null) {
            return null;
        }
        return workday.getDate();
    }

    /**
     * 上一工作日
     * @param date 日期
     * @return String
     */
    public String lastWorkday(String date, int n) {
        List<Workday> workdays = workdayMapper.listLast(date, n);
        if (workdays.size() < n) {
            return null;
        }
        return workdays.get(n - 1).getDate();
    }

    /**
     * 最新工作日
     * @param date 日期
     * @return String
     */
    public String latestWorkday(String date) {
        Workday workday = workdayMapper.findLatestWorkday(date);
        if (workday == null) {
            return null;
        }
        return workday.getDate();
    }

    /**
     * 按年查询月份数据
     * @param year 年
     * @return list
     */
    public List<Month> listYearMonths(String year) {
        // 查询数据
        List<Workday> workdays = workdayMapper.listByYear(year);
        // 按月分组
        Map<String, List<Workday>> map = workdays.stream()
                .collect(Collectors.groupingBy(i -> i.getDate().substring(0, 6)));
        List<Month> results = new ArrayList<>();
        // 遍历分组转换为月份对象
        map.keySet().stream().sorted().forEach(i -> {
            List<Workday> list = map.get(i);
            if (list != null) {
                Month month = new Month();
                month.setMonth(i);
                for (Workday w : list) {
                    month.setDateType(w.getDate(), w.getType());
                }
                results.add(month);
            }
        });
        return results;
    }

    /**
     * 获取全年节假日
     * @param year 年
     * @return list
     */
    private List<Workday> getYearWorkDays(String year) {
        // 获取全年节假日
        Set<String> holidays = new TreeSet<>(this.getHolidaysByHttp(year));
        List<Workday> list = new ArrayList<>();
        // 年初第一天
        String day = DateUtil.format(DateUtil.parse(year, "yyyy"), Const.FORMAT_DAY);
        while (true) {
            Workday workDay = new Workday();
            workDay.setDate(day);
            workDay.setType((holidays.contains(day) || this.isWeekend(day)) ? TYPE_HOLIDAY : TYPE_WORKDAY);
            list.add(workDay);
            if (day.endsWith("1231")) {
                // 生成至年底截止
                break;
            }
            day = DateUtil.addDays(day, 1);
        }
        return list;
    }

    /**
     * 从网络获取节假日
     * @param year 年
     * @return list
     */
    private List<String> getHolidaysByHttp(String year) {
        String s = HttpUtil.doGet(String.format(URL_HOLIDAY, year), lines -> lines.collect(Collectors.toList()).get(0));
        JSONObject map = JSON.parseObject(s).getJSONObject(year);
        List<String> holidays = new LinkedList<>();
        for (String date : map.keySet()) {
            holidays.add(String.format("%s%s%s", year, date.substring(0, 2), date.substring(2, 4)));
        }
        return holidays;
    }

    /**
     * 判断是否为周末
     * @param day 日期
     * @return boolean
     */
    private boolean isWeekend(String day) {
        Date date = DateUtil.parse(day, Const.FORMAT_DAY);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == 1 || dayOfWeek == 7;
    }

}
