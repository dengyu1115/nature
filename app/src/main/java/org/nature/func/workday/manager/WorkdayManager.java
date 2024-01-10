package org.nature.func.workday.manager;

import android.annotation.SuppressLint;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.nature.common.constant.Const;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.HttpUtil;
import org.nature.func.workday.mapper.WorkdayMapper;
import org.nature.func.workday.model.Month;
import org.nature.func.workday.model.Workday;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressLint("DefaultLocale")
@Component
public class WorkdayManager {

    private static final String FORMAT_TIME = "HH:mm:ss";

    private static final String URL_HOLIDAY = "https://tool.bitefu.net/jiari/?d=%s";

    private static final String START_TIME = "09:25:00";

    private static final String END_TIME = "15:05:00";

    private static final String TYPE_HOLIDAY = "1";

    private static final String TYPE_WORKDAY = "0";

    @Injection
    private WorkdayMapper workdayMapper;

    public int reload(String year) {
        workdayMapper.deleteByYear(year);
        return workdayMapper.batchSave(this.getYearWorkDays(year));
    }

    public int load(String year) {
        List<Workday> list = workdayMapper.listByYear(year);
        if (!list.isEmpty()) {
            throw new Warn("已存在数据");
        }
        return workdayMapper.batchSave(this.getYearWorkDays(year));
    }

    private List<Workday> getYearWorkDays(String year) {
        Map<String, Workday> workDays = this.initYearDays(year);    // 获取全年工作日
        List<String> holidays = this.getHolidaysFromNet(year);      // 获取全年节假日
        List<String> weekends = this.initWeekends(year);            // 获取全年周末
        Set<String> days = new TreeSet<>(holidays);
        days.addAll(weekends);
        return workDays.entrySet().parallelStream().map(entry -> {  // 假日标记
            if (days.contains(entry.getKey())) entry.getValue().setType(TYPE_HOLIDAY);
            return entry.getValue();
        }).sorted(Comparator.comparing(Workday::getDate)).collect(Collectors.toList());
    }

    private List<String> getHolidaysFromNet(String year) {
        String s = HttpUtil.doGet(String.format(URL_HOLIDAY, year), lines -> lines.collect(Collectors.toList()).get(0));
        JSONObject map = JSON.parseObject(s).getJSONObject(year);
        List<String> holidays = new LinkedList<>();
        for (String date : map.keySet()) {
            holidays.add(String.format("%s%s%s", year, date.substring(0, 2), date.substring(2, 4)));
        }
        return holidays;
    }

    private Map<String, Workday> initYearDays(String year) {
        Map<String, Workday> workDays = new HashMap<>();
        try {
            Date date = DateUtils.parseDate(year, "yyyy");  // 年初第一天
            int i = 0;
            while (true) {
                Workday workDay = new Workday();
                String day = DateFormatUtils.format(DateUtils.addDays(date, i++), Const.FORMAT_DAY);
                workDay.setDate(day);
                workDay.setType(TYPE_WORKDAY);
                workDays.put(day, workDay);
                if (day.endsWith("1231")) break;   // 生成至年底截止
            }
        } catch (ParseException e) {// ignore
        }
        return workDays;
    }

    private List<String> initWeekends(String year) {
        List<String> days = new ArrayList<>();
        try {
            Date date = DateUtils.parseDate(year, "yyyy");
            int i = 0;
            Date nextYear = DateUtils.addYears(date, 1);
            while (true) {
                Date d = DateUtils.addDays(date, i++);
                Calendar calendar = DateUtils.toCalendar(d);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                if (d.after(nextYear)) break; // 第二次处理当年第一天说明已经跨年
                if (dayOfWeek == 1 || dayOfWeek == 7) days.add(DateFormatUtils.format(d, Const.FORMAT_DAY));
            }
        } catch (ParseException e) {// ignore
        }
        return days;
    }

    public String getToday() {
        return DateFormatUtils.format(new Date(), Const.FORMAT_DAY);
    }

    public String getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return DateFormatUtils.format(calendar.getTime(), Const.FORMAT_DAY);
    }

    public String getNowTime() {
        return DateFormatUtils.format(new Date(), FORMAT_TIME);
    }

    public List<Month> listYearMonths(String year) {
        List<Workday> workdays = workdayMapper.listByYear(year);
        Map<String, List<Workday>> map = workdays.stream()
                .collect(Collectors.groupingBy(i -> i.getDate().substring(0, 6)));
        List<Month> results = new ArrayList<>();
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

}
