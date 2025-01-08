package org.nature.biz.common.page;

import android.widget.LinearLayout;
import org.nature.biz.common.manager.NetManager;
import org.nature.biz.common.model.NInfo;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.Button;
import org.nature.common.view.Table;

import java.util.Arrays;
import java.util.List;

/**
 * 项目维护
 * @author Nature
 * @version 1.0.0
 * @since 2023/12/29
 */
@PageView(name = "净值项目", group = "基础", col = 1, row = 3)
public class NInfoListPage extends ListPage<NInfo> {

    @Injection
    private NetManager netManager;
    /**
     * 表头
     */
    private final List<Table.Header<NInfo>> headers = Arrays.asList(
            Table.header("名称", d -> TextUtil.text(d.getName()), C, S, NInfo::getName),
            Table.header("编号", d -> TextUtil.text(d.getCode()), C, C, NInfo::getCode),
            Table.header("明细", d -> "+", C, C, this::detail),
            Table.header("加载", d -> "+", C, C, this::load),
            Table.header("重载", d -> "+", C, C, this::reload)

    );
    /**
     * 加载K线、重新加载K线
     */
    private Button load, reload;

    @Override
    protected List<Table.Header<NInfo>> headers() {
        return headers;
    }

    @Override
    protected List<NInfo> listData() {
        return netManager.nItems();
    }

    @Override
    protected void initHeaderViews(LinearLayout condition) {
        condition.addView(load = template.button("加载", 10, 7));
        condition.addView(reload = template.button("重载", 10, 7));
    }

    @Override
    protected void initHeaderBehaviours() {
        ClickUtil.onAsyncClick(load, this::loadAll);
        ClickUtil.onClick(reload, () -> template.confirmAsync("净值重载", "确定重新加载全部净值数据？", this::reloadAll));
    }

    @Override
    protected int getTotalColumns() {
        return headers.size();
    }

    @Override
    protected int getFixedColumns() {
        return headers.size();
    }


    private void detail(NInfo info) {
        this.show(NetListPage.class, info);
    }

    /**
     * 加载全部K线
     * @return 提示信息
     */
    private String loadAll() {
        return "所有净值加载完成，数据量：" + netManager.loadAll();
    }

    /**
     * 重载全部K线
     * @return 提示信息
     */
    private String reloadAll() {
        return "所有净值重载完成，数据量：" + netManager.reloadAll();
    }

    private void load(NInfo info) {
        template.alert("加载完成，数据量：" + netManager.load(info.getCode()));
    }

    private void reload(NInfo info) {
        template.confirm("重载净值", "确定重载吗？",
                () -> template.alert("重载完成，数据量：" + netManager.reload(info.getCode())));
    }

}
