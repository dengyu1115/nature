package org.nature.func.workday.page;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.nature.common.constant.Const;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.PopUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.TableView;
import org.nature.common.view.ViewTemplate;
import org.nature.func.workday.manager.WorkdayManager;
import org.nature.func.workday.mapper.WorkdayMapper;
import org.nature.func.workday.model.Month;
import org.nature.func.workday.model.Workday;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.nature.common.constant.Const.*;

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
    @Injection
    private WorkdayMapper workdayMapper;
    /**
     * 重载、加载最新
     */
    private Button reload, loadLatest;
    /**
     * 年份选择器
     */
    private Selector<String> year, type;
    /**
     * 弹窗
     */
    private LinearLayout page;
    /**
     * 工作日属性输入框
     */
    private EditText date;

    @Override
    protected List<TableView.D<Month>> define() {
        List<TableView.D<Month>> ds = new ArrayList<>();
        ds.add(TableView.row("月份", i -> TextUtil.text(i.getMonth()), C, C, Month::getMonth));
        for (int i = 1; i < 32; i++) {
            String day = String.format("%02d", i);
            Consumer<Month> monthConsumer = d -> {
                String date = d.getMonth() + day;
                this.edit(d.getDateType(date), date);
            };
            ds.add(TableView.row(day, d -> TextUtil.text(d.getDateType(d.getMonth() + day)), C, C, monthConsumer));
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
            Warn.check(() -> StringUtils.isBlank(year), "请选择年份");
            return String.format("加载完成,共%s条", workDayManager.reload(year));
        });
        ClickUtil.onAsyncClick(loadLatest, () -> {
            String year = this.year.getValue();
            Warn.check(() -> StringUtils.isBlank(year), "请选择年份");
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

    private void edit(String type, String day) {
        this.makeWindowStructure();
        this.date.setText(day);
        this.type.setValue(type);
        PopUtil.confirm(context, "编辑-" + day, page, () -> this.doEdit(workdayMapper::merge));
    }

    /**
     * 执行编辑操作
     * @param consumer 处理逻辑
     */
    private void doEdit(Consumer<Workday> consumer) {
        String date = this.date.getText().toString();
        Warn.check(date::isEmpty, "请填写日期");
        String type = this.type.getValue();
        Warn.check(type::isEmpty, "请选择类型");
        Workday workday = new Workday();
        workday.setDate(date);
        workday.setType(type);
        consumer.accept(workday);
        this.refreshData();
        PopUtil.alert(context, "编辑成功！");
    }

    /**
     * 构建弹窗
     */
    private void makeWindowStructure() {
        ViewTemplate t = template;
        page = t.linearPage(Gravity.CENTER,
                t.line(L_W, L_H, t.textView("日期：", L_W_T, L_H), date = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("类型：", L_W_T, L_H), type = t.selector(L_W_C, L_H))
        );
        date.setFocusable(false);
        type.init().mapper(i -> i).refreshData(Arrays.asList("H", "W"));
    }

}
