package org.nature.func.job.page;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.ioc.holder.JobHolder;
import org.nature.common.page.ListPage;
import org.nature.common.util.*;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.Table;
import org.nature.common.view.ViewTemplate;
import org.nature.func.job.enums.Status;
import org.nature.func.job.mapper.ConfigInfoMapper;
import org.nature.func.job.model.ConfigInfo;
import org.nature.func.job.service.JobService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private ConfigInfoMapper configInfoMapper;

    private Button start, stop, add;
    private LinearLayout editPop;
    private Selector<String> jobSel, statusSel;
    private EditText year, month, day, hour, minute, second;

    private final List<Table.Header<ConfigInfo>> headers = Arrays.asList(
            Table.header("名称", d -> JobHolder.getName(d.getCode()), C, S, Sorter.nullsLast(d -> JobHolder.getName(d.getCode()))),
            Table.header("编号", d -> TextUtil.text(d.getCode()), C, S, ConfigInfo::getCode),
            Table.header("状态", d -> TextUtil.text(Status.name(d.getStatus())), C, C, ConfigInfo::getStatus),
            Table.header("年", d -> TextUtil.text(d.getYear()), C, C, ConfigInfo::getYear),
            Table.header("月", d -> TextUtil.text(d.getMonth()), C, C, ConfigInfo::getMonth),
            Table.header("日", d -> TextUtil.text(d.getDay()), C, C, ConfigInfo::getDay),
            Table.header("时", d -> TextUtil.text(d.getHour()), C, C, ConfigInfo::getHour),
            Table.header("分", d -> TextUtil.text(d.getMinute()), C, C, ConfigInfo::getMinute),
            Table.header("秒", d -> TextUtil.text(d.getSecond()), C, C, ConfigInfo::getSecond)
    );

    @Override
    protected List<Table.Header<ConfigInfo>> define() {
        return headers;
    }

    @Override
    protected List<ConfigInfo> listData() {
        return configInfoMapper.listAll().stream().sorted(Comparator.comparing(ConfigInfo::getCode))
                .collect(Collectors.toList());
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
        ClickUtil.onClick(start, () -> context.startService(service));
        ClickUtil.onClick(stop, () -> context.stopService(service));
        ClickUtil.onClick(add, this::add);
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
        editPop = t.block(Gravity.CENTER,
                t.line(L_W, L_H, t.textView("任务：", L_W_T, L_H), jobSel = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("年：", L_W_T, L_H), year = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("月：", L_W_T, L_H), month = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("日：", L_W_T, L_H), day = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("时：", L_W_T, L_H), hour = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("分：", L_W_T, L_H), minute = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("秒：", L_W_T, L_H), second = t.input(L_W_C, L_H)),
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
        PopupUtil.confirm(context, "新增", editPop, () -> this.doEdit(this::save));
    }

    /**
     * 编辑操作
     * @param d 数据
     */
    private void edit(ConfigInfo d) {
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
        PopupUtil.confirm(context, "编辑-" + name, editPop, () -> this.doEdit(configInfoMapper::merge));
    }

    /**
     * 删除操作
     * @param d 数据
     */
    private void delete(ConfigInfo d) {
        String name = JobHolder.getName(d.getCode());
        PopupUtil.confirm(context, "删除-" + name, "确认删除吗？", () -> {
            configInfoMapper.deleteById(d);
            this.refreshData();
            PopupUtil.alert(context, "删除成功！");
        });
    }

    /**
     * 编辑
     * @param consumer 处理逻辑
     */
    private void doEdit(Consumer<ConfigInfo> consumer) {
        String code = this.jobSel.getValue();
        Warn.check(() -> code == null, "请选择任务");
        String status = this.statusSel.getValue();
        Warn.check(status::isEmpty, "请选择状态");
        ConfigInfo d = new ConfigInfo();
        d.setCode(code);
        d.setStatus(status);
        d.setYear(TextUtil.getString(this.year));
        d.setMonth(TextUtil.getString(this.month));
        d.setDay(TextUtil.getString(this.day));
        d.setHour(TextUtil.getString(this.hour));
        d.setMinute(TextUtil.getString(this.minute));
        d.setSecond(TextUtil.getString(this.second));
        d.setSignature(Md5Util.md5(code, d.getYear(), d.getMonth(), d.getDay(), d.getHour(), d.getMinute(), d.getSecond()));
        consumer.accept(d);
        this.refreshData();
        PopupUtil.alert(context, "编辑成功！");
    }

    /**
     * 保存
     * @param d 数据
     */
    private void save(ConfigInfo d) {
        ConfigInfo exists = configInfoMapper.findById(d);
        Warn.check(() -> exists != null, "数据已存在");
        configInfoMapper.save(d);
    }

    @Override
    protected Consumer<ConfigInfo> longClick() {
        return i -> PopupUtil.handle(context, i, this::delete, this::edit);
    }

}
