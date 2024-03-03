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
import org.nature.common.util.Md5Util;
import org.nature.common.util.PopUtil;
import org.nature.common.util.Sorter;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.TableView;
import org.nature.common.view.ViewTemplate;
import org.nature.func.job.enums.Status;
import org.nature.func.job.manager.ConfigInfoManager;
import org.nature.func.job.model.ConfigInfo;
import org.nature.func.job.service.JobService;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.nature.common.constant.Const.*;

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

    private final List<TableView.D<ConfigInfo>> ds = Arrays.asList(
            TableView.row("名称", d -> JobHolder.getName(d.getCode()), C, S, Sorter.nullsLast(d -> JobHolder.getName(d.getCode()))),
            TableView.row("编号", d -> TextUtil.text(d.getCode()), C, S, ConfigInfo::getCode),
            TableView.row("状态", d -> TextUtil.text(Status.name(d.getStatus())), C, C, ConfigInfo::getStatus),
            TableView.row("编辑", d -> "+", C, C, this.edit()),
            TableView.row("删除", d -> "-", C, C, this.delete()),
            TableView.row("年", d -> TextUtil.text(d.getYear()), C, C, ConfigInfo::getYear),
            TableView.row("月", d -> TextUtil.text(d.getMonth()), C, C, ConfigInfo::getMonth),
            TableView.row("日", d -> TextUtil.text(d.getDay()), C, C, ConfigInfo::getDay),
            TableView.row("时", d -> TextUtil.text(d.getHour()), C, C, ConfigInfo::getHour),
            TableView.row("分", d -> TextUtil.text(d.getMinute()), C, C, ConfigInfo::getMinute),
            TableView.row("秒", d -> TextUtil.text(d.getSecond()), C, C, ConfigInfo::getSecond)
    );

    @Override
    protected List<TableView.D<ConfigInfo>> define() {
        return ds;
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

    @Override
    protected int getTotalColumns() {
        return 7;
    }

    @Override
    protected int getFixedColumns() {
        return 3;
    }

    /**
     * 构建弹窗
     */
    private void makeWindowStructure() {
        ViewTemplate t = template;
        editPop = t.linearPage(Gravity.CENTER,
                t.line(L_W, L_H, t.textView("任务：", L_W_T, L_H), jobSel = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("年：", L_W_T, L_H), year = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("月：", L_W_T, L_H), month = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("日：", L_W_T, L_H), day = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("时：", L_W_T, L_H), hour = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("分：", L_W_T, L_H), minute = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("秒：", L_W_T, L_H), second = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("状态：", L_W_T, L_H), statusSel = t.selector(L_W_C, L_H))
        );
        jobSel.mapper(JobHolder::getName).init().refreshData(JobHolder.jobs());
        statusSel.mapper(Status::name).init().refreshData(Status.codes());
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
