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
import org.nature.common.util.CommonUtil;
import org.nature.common.util.PopUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.ExcelView;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.func.job.enums.Status;
import org.nature.func.job.enums.Type;
import org.nature.func.job.manager.ConfigInfoManager;
import org.nature.func.job.model.ConfigInfo;
import org.nature.func.job.service.JobService;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@PageView(name = "任务配置", group = "基础", col = 2, row = 1)
@SuppressLint("DefaultLocale")
public class ConfigInfoPage extends ListPage<ConfigInfo> {

    @Injection
    private ConfigInfoManager configInfoManager;
    private Button start, stop, add;
    private LinearLayout editPop;
    private Selector<String> jobSel, typeSel, statusSel, unitSel;
    private Button startBtn, endBtn;
    private EditText period;

    private final List<ExcelView.D<ConfigInfo>> DS = Arrays.asList(
            ExcelView.row("", C, Arrays.asList(
                    ExcelView.row("名称", d -> JobHolder.getName(d.getCode()), C, S, CommonUtil.nullsLast(d -> JobHolder.getName(d.getCode()))),
                    ExcelView.row("编号", d -> TextUtil.text(d.getCode()), C, C, CommonUtil.nullsLast(ConfigInfo::getCode)))
            ),
            ExcelView.row("类型", d -> Type.name(d.getType()), C, C, CommonUtil.nullsLast(ConfigInfo::getType)),
            ExcelView.row("开始时间", d -> TextUtil.text(d.getStartTime()), C, C, CommonUtil.nullsLast(ConfigInfo::getStartTime)),
            ExcelView.row("结束时间", d -> TextUtil.text(d.getEndTime()), C, C, CommonUtil.nullsLast(ConfigInfo::getEndTime)),
            ExcelView.row("间隔", d -> TextUtil.text(d.getPeriod()), C, C, CommonUtil.nullsLast(ConfigInfo::getPeriod)),
            ExcelView.row("单位", d -> Type.name(d.getUnit()), C, C, CommonUtil.nullsLast(ConfigInfo::getUnit)),
            ExcelView.row("状态", d -> TextUtil.text(d.getStatus()), C, C, CommonUtil.nullsLast(ConfigInfo::getStatus)),
            ExcelView.row("编辑", d -> "+", C, C, this.edit()),
            ExcelView.row("删除", d -> "-", C, C, this.delete())
    );

    @Override
    protected List<ExcelView.D<ConfigInfo>> define() {
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
        l1.addView(template.textView("任务：", 100, 30));
        l1.addView(jobSel = template.selector(200, 30));
        l2.addView(template.textView("类型：", 100, 30));
        l2.addView(typeSel = template.selector(200, 30));
        l3.addView(template.textView("开始时间：", 100, 30));
        l3.addView(startBtn = template.timePiker(200, 30));
        l4.addView(template.textView("开始时间：", 100, 30));
        l4.addView(endBtn = template.timePiker(200, 30));
        l5.addView(template.textView("间隔：", 100, 30));
        l5.addView(period = template.numeric(200, 30));
        l6.addView(template.textView("单位：", 100, 30));
        l6.addView(unitSel = template.selector(200, 30));
        l7.addView(template.textView("状态：", 100, 30));
        l7.addView(statusSel = template.selector(200, 30));
        jobSel.mapper(JobHolder::getName).init().refreshData(JobHolder.jobs());
        unitSel.mapper(Type::name).init();
        typeSel.mapper(Type::name).onChangeRun(() -> unitSel.refreshData(Type.units(typeSel.getValue())))
                .init().refreshData(Type.codes());
        statusSel.mapper(Status::name).init().refreshData(Status.codes());
        editPop.addView(l1);
        editPop.addView(l2);
        editPop.addView(l3);
        editPop.addView(l4);
        editPop.addView(l5);
        editPop.addView(l6);
        editPop.addView(l7);
    }

    private void add() {
        this.makeWindowStructure();
        PopUtil.confirm(context, "新增", editPop, () -> this.doEdit(configInfoManager::save));
    }

    private Consumer<ConfigInfo> edit() {
        return d -> {
            this.makeWindowStructure();
            this.jobSel.setValue(d.getCode());
            this.typeSel.setValue(d.getType());
            String name = JobHolder.getName(d.getCode());
            PopUtil.confirm(context, "编辑-" + name, editPop, () -> this.doEdit(configInfoManager::edit));
        };
    }

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

    private void doEdit(Consumer<ConfigInfo> consumer) {
        String code = this.jobSel.getValue();
        if (code == null) {
            throw new RuntimeException("请选择任务");
        }
        String type = this.typeSel.getValue();
        if (type.isEmpty()) {
            throw new RuntimeException("请选择类型");
        }
        String status = this.statusSel.getValue();
        if (status.isEmpty()) {
            throw new RuntimeException("请选择状态");
        }
        String startTime = this.startBtn.getText().toString();
        String endTime = this.endBtn.getText().toString();
        Integer period = Integer.parseInt(this.period.getText().toString());
        String unit = this.unitSel.getValue();
        ConfigInfo d = new ConfigInfo();
        d.setCode(code);
        d.setType(type);
        d.setStartTime(startTime);
        d.setEndTime(endTime);
        d.setPeriod(period);
        d.setUnit(unit);
        d.setStatus(status);
        consumer.accept(d);
        this.refreshData();
        PopUtil.alert(context, "编辑成功！");
    }

}
