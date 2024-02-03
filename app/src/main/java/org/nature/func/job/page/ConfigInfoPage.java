package org.nature.func.job.page;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.ioc.holder.JobHolder;
import org.nature.common.page.ListPage;
import org.nature.common.util.*;
import org.nature.common.view.TableView;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.func.job.enums.Status;
import org.nature.func.job.manager.ConfigInfoManager;
import org.nature.func.job.model.ConfigInfo;
import org.nature.func.job.service.JobService;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 任务配置
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/10
 */
@PageView(name = "任务配置", group = "基础", col = 2, row = 1)
@SuppressLint("DefaultLocale")
public class ConfigInfoPage extends ListPage<ConfigInfo> {

    @Injection
    private ConfigInfoManager configInfoManager;

    private Button start, stop, add;
    private LinearLayout editPop;
    private Selector<String> jobSel, statusSel;
    private EditText year, month, day, hour, minute, second;

    private final List<TableView.D<ConfigInfo>> DS = Arrays.asList(
            TableView.row("", C, Arrays.asList(
                    TableView.row("名称", d -> JobHolder.getName(d.getCode()), C, S, Sorter.nullsLast(d -> JobHolder.getName(d.getCode()))),
                    TableView.row("编号", d -> TextUtil.text(d.getCode()), C, C, ConfigInfo::getCode))
            ),
            TableView.row("年", d -> TextUtil.text(d.getYear()), C, C, ConfigInfo::getYear),
            TableView.row("月", d -> TextUtil.text(d.getMonth()), C, C, ConfigInfo::getMonth),
            TableView.row("日", d -> TextUtil.text(d.getDay()), C, C, ConfigInfo::getDay),
            TableView.row("时", d -> TextUtil.text(d.getHour()), C, C, ConfigInfo::getHour),
            TableView.row("分", d -> TextUtil.text(d.getMinute()), C, C, ConfigInfo::getMinute),
            TableView.row("秒", d -> TextUtil.text(d.getSecond()), C, C, ConfigInfo::getSecond),
            TableView.row("状态", d -> TextUtil.text(Status.name(d.getStatus())), C, C, ConfigInfo::getStatus),
            TableView.row("编辑", d -> "+", C, C, this.edit()),
            TableView.row("删除", d -> "-", C, C, this.delete())
    );

    @Override
    protected List<TableView.D<ConfigInfo>> define() {
        return DS;
    }

    @Override
    protected List<ConfigInfo> listData() {
        return configInfoManager.listAll();
    }


    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(start = template.button("启动", 60, 30));
        searchBar.addConditionView(stop = template.button("停止", 60, 30));
        searchBar.addConditionView(add = template.button("+", 30, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        Intent service = new Intent(context, JobService.class);
        start.setOnClickListener(i -> context.startService(service));
        stop.setOnClickListener(i -> context.stopService(service));
        add.setOnClickListener(i -> this.add());
    }

    /**
     * 构建弹窗
     */
    private void makeWindowStructure() {
        editPop = template.linearPage();
        editPop.setGravity(Gravity.CENTER);
        LinearLayout l1 = template.line(300, 30);
        LinearLayout l2 = template.line(300, 30);
        LinearLayout l3 = template.line(300, 30);
        LinearLayout l4 = template.line(300, 30);
        LinearLayout l5 = template.line(300, 30);
        LinearLayout l6 = template.line(300, 30);
        LinearLayout l7 = template.line(300, 30);
        LinearLayout l8 = template.line(300, 30);
        l1.addView(template.textView("任务：", 100, 30));
        l1.addView(jobSel = template.selector(200, 30));
        l2.addView(template.textView("年：", 100, 30));
        l2.addView(year = template.editText(200, 30));
        l3.addView(template.textView("月：", 100, 30));
        l3.addView(month = template.editText(200, 30));
        l4.addView(template.textView("日：", 100, 30));
        l4.addView(day = template.editText(200, 30));
        l5.addView(template.textView("时：", 100, 30));
        l5.addView(hour = template.editText(200, 30));
        l6.addView(template.textView("分：", 100, 30));
        l6.addView(minute = template.editText(200, 30));
        l7.addView(template.textView("秒：", 100, 30));
        l7.addView(second = template.editText(200, 30));
        l8.addView(template.textView("状态：", 100, 30));
        l8.addView(statusSel = template.selector(200, 30));
        jobSel.mapper(JobHolder::getName).init().refreshData(JobHolder.jobs());
        statusSel.mapper(Status::name).init().refreshData(Status.codes());
        editPop.addView(l1);
        editPop.addView(l2);
        editPop.addView(l3);
        editPop.addView(l4);
        editPop.addView(l5);
        editPop.addView(l6);
        editPop.addView(l7);
        editPop.addView(l8);
    }

    /**
     * 添加操作
     */
    private void add() {
        this.makeWindowStructure();
        PopUtil.confirm(context, "新增", editPop, () -> this.doEdit(configInfoManager::save));
    }

    /**
     * 编辑操作
     * @return 操作逻辑
     */
    private Consumer<ConfigInfo> edit() {
        return d -> {
            this.makeWindowStructure();
            this.jobSel.setValue(d.getCode());
            this.year.setText(d.getYear());
            this.month.setText(d.getMonth());
            this.day.setText(d.getDay());
            this.hour.setText(d.getHour());
            this.minute.setText(d.getMinute());
            this.second.setText(d.getSecond());
            this.statusSel.setValue(d.getStatus());
            String name = JobHolder.getName(d.getCode());
            PopUtil.confirm(context, "编辑-" + name, editPop, () -> this.doEdit(configInfoManager::edit));
        };
    }

    /**
     * 删除操作
     * @return 操作逻辑
     */
    private Consumer<ConfigInfo> delete() {
        return d -> {
            String name = JobHolder.getName(d.getCode());
            PopUtil.confirm(context, "删除-" + name, "确认删除吗？", () -> {
                configInfoManager.delete(d);
                this.refreshData();
                PopUtil.alert(context, "删除成功！");
            });
        };
    }

    /**
     * 编辑
     * @param consumer 处理逻辑
     */
    private void doEdit(Consumer<ConfigInfo> consumer) {
        String code = this.jobSel.getValue();

        if (code == null) {
            throw new RuntimeException("请选择任务");
        }
        String status = this.statusSel.getValue();
        if (status.isEmpty()) {
            throw new RuntimeException("请选择状态");
        }
        String year = this.year.getText().toString();
        String month = this.month.getText().toString();
        String day = this.day.getText().toString();
        String hour = this.hour.getText().toString();
        String minute = this.minute.getText().toString();
        String second = this.second.getText().toString();
        ConfigInfo d = new ConfigInfo();
        d.setCode(code);
        d.setStatus(status);
        d.setYear(year);
        d.setMonth(month);
        d.setDay(day);
        d.setHour(hour);
        d.setMinute(minute);
        d.setSecond(second);
        d.setSignature(Md5Util.md5(code, year, month, day, hour, minute, second));
        consumer.accept(d);
        this.refreshData();
        PopUtil.alert(context, "编辑成功！");
    }

}
