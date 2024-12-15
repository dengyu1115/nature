package org.nature.func.job.page;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.Gravity;
import android.widget.LinearLayout;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.ioc.holder.JobHolder;
import org.nature.common.page.ListPage;
import org.nature.common.util.Md5Util;
import org.nature.common.util.Sorter;
import org.nature.common.util.TextUtil;
import org.nature.common.view.*;
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
    @Injection
    private ConfigInfoMapper configInfoMapper;
    private Button start, add;
    private LinearLayout popup;
    private Selector<String> jobSel, statusSel;
    private Input year, month, day, hour, minute, second;
    private boolean running;

    @Override
    protected List<Table.Header<ConfigInfo>> headers() {
        return headers;
    }

    @Override
    protected List<ConfigInfo> listData() {
        return configInfoMapper.listAll().stream().sorted(Comparator.comparing(ConfigInfo::getCode))
                .collect(Collectors.toList());
    }


    @Override
    protected void initHeaderViews(LinearLayout condition) {
        condition.addView(start = template.button("启动", 5, 7));
        condition.addView(add = template.button("+", 3, 7));
    }

    @Override
    protected void initHeaderBehaviours() {
        Intent service = new Intent(context, JobService.class);
        start.onClick(() -> {
            running = !running;
            if (running) {
                context.startService(service);
                start.setText("停止");
                start.setBtnBackground(template.background("success"));
            } else {
                context.stopService(service);
                start.setText("启动");
                start.setBtnBackground(template.background("primary"));
            }
        });
        add.onClick(this::add);
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
        popup = t.block(Gravity.CENTER,
                t.line(L_W, L_H, t.text("任务：", L_W_T, L_H), jobSel = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("年：", L_W_T, L_H), year = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("月：", L_W_T, L_H), month = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("日：", L_W_T, L_H), day = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("时：", L_W_T, L_H), hour = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("分：", L_W_T, L_H), minute = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("秒：", L_W_T, L_H), second = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("状态：", L_W_T, L_H), statusSel = t.selector(L_W_C, L_H))
        );
        jobSel.mapper(JobHolder::getName);
        jobSel.refreshData(JobHolder.jobs());
        statusSel.mapper(Status::name);
        statusSel.refreshData(Status.codes());
    }

    /**
     * 添加操作
     */
    private void add() {
        this.makeWindowStructure();
        template.confirm("新增", popup, () -> this.doEdit(this::save));
    }

    /**
     * 编辑操作
     * @param d 数据
     */
    private void edit(ConfigInfo d) {
        this.makeWindowStructure();
        this.jobSel.setValue(d.getCode());
        this.year.setValue(d.getYear());
        this.month.setValue(d.getMonth());
        this.day.setValue(d.getDay());
        this.hour.setValue(d.getHour());
        this.minute.setValue(d.getMinute());
        this.second.setValue(d.getSecond());
        this.statusSel.setValue(d.getStatus());
        String name = JobHolder.getName(d.getCode());
        template.confirm("编辑-" + name, popup, () -> this.doEdit(configInfoMapper::merge));
    }

    /**
     * 删除操作
     * @param d 数据
     */
    private void delete(ConfigInfo d) {
        String name = JobHolder.getName(d.getCode());
        template.confirm("删除-" + name, "确认删除吗？", () -> {
            configInfoMapper.deleteById(d);
            this.refreshData();
            template.alert("删除成功！");
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
        d.setYear(this.year.getValue());
        d.setMonth(this.month.getValue());
        d.setDay(this.day.getValue());
        d.setHour(this.hour.getValue());
        d.setMinute(this.minute.getValue());
        d.setSecond(this.second.getValue());
        d.setSignature(Md5Util.md5(code, d.getYear(), d.getMonth(), d.getDay(), d.getHour(), d.getMinute(), d.getSecond()));
        consumer.accept(d);
        this.refreshData();
        template.alert("编辑成功！");
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
        return i -> template.handle(i, this::delete, this::edit);
    }

}
