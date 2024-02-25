package org.nature.func.workday.page;

import android.annotation.SuppressLint;
import android.widget.Button;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.nature.common.constant.Const;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.TableView;
import org.nature.func.workday.manager.WorkdayManager;
import org.nature.func.workday.model.Month;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 工作日
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/10
 */
@PageView(name = "工作日", group = "基础", col = 1, row = 1)
@SuppressLint("DefaultLocale")
public class WorkdayPage extends ListPage<Month> {

    @Injection
    private WorkdayManager workDayManager;
    /**
     * 重载、加载最新
     */
    private Button reload, loadLatest;
    /**
     * 年份选择器
     */
    private Selector<String> year;

    @Override
    protected List<TableView.D<Month>> define() {
        List<TableView.D<Month>> ds = new ArrayList<>();
        ds.add(TableView.row("月份", i -> TextUtil.text(i.getMonth()), C, C, Month::getMonth));
        for (int i = 1; i < 32; i++) {
            String day = String.format("%02d", i);
            ds.add(TableView.row(day, d -> TextUtil.text(d.getDateType(d.getMonth() + day)), C, C));
        }
        return ds;
    }

    @Override
    protected List<Month> listData() {
        String date = this.year.getValue();
        if (StringUtils.isBlank(date)) {
            return new ArrayList<>();
        }
        return workDayManager.listYearMonths(date.substring(0, 4));
    }


    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(reload = template.button("重新加载", 60, 30));
        searchBar.addConditionView(loadLatest = template.button("加载最新", 60, 30));
        searchBar.addConditionView(year = template.selector(100, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        year.mapper(i -> i).init().refreshData(this.initYears());
        year.setValue(DateFormatUtils.format(new Date(), Const.FORMAT_YEAR));
        ClickUtil.onPopConfirm(reload, "重新加载数据", "确定重新加载吗？", () -> {
            String year = this.year.getValue();
            if (StringUtils.isBlank(year)) {
                throw new RuntimeException("请选择年份");
            }
            return String.format("加载完成,共%s条", workDayManager.reload(year));
        });
        ClickUtil.onAsyncClick(loadLatest, () -> {
            String year = this.year.getValue();
            if (StringUtils.isBlank(year)) {
                throw new RuntimeException("请选择年份");
            }
            return String.format("加载完成,共%s条", workDayManager.load(year));
        });
    }

    @Override
    protected int getTotalColumns() {
        return 14;
    }

    /**
     * 初始化年份
     * @return list
     */
    private List<String> initYears() {
        List<String> years = new ArrayList<>();
        Date now = new Date();
        for (int i = 1; i > -11; i--) {
            Date date = DateUtils.addYears(now, i);
            years.add(DateFormatUtils.format(date, Const.FORMAT_YEAR));
        }
        return years;
    }

}
