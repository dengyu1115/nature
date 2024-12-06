package org.nature.biz.common.page;

import android.widget.Button;
import android.widget.LinearLayout;
import org.nature.biz.common.manager.KlineManager;
import org.nature.biz.common.model.KInfo;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.PopupUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.Table;

import java.util.Arrays;
import java.util.List;

/**
 * 项目维护
 * @author Nature
 * @version 1.0.0
 * @since 2023/12/29
 */
@PageView(name = "K线项目", group = "基础", col = 1, row = 2)
public class KInfoListPage extends ListPage<KInfo> {

    @Injection
    private KlineManager klineManager;
    /**
     * 加载K线、重新加载K线
     */
    private Button load, reload;
    /**
     * 表头
     */
    private final List<Table.Header<KInfo>> headers = Arrays.asList(
            Table.header("名称", d -> TextUtil.text(d.getName()), C, S, KInfo::getName),
            Table.header("编号", d -> TextUtil.text(d.getCode()), C, C, KInfo::getCode),
            Table.header("类型", d -> TextUtil.text(d.getType()), C, C, KInfo::getType),
            Table.header("明细", d -> "+", C, C, this::detail),
            Table.header("加载", d -> "+", C, C, this::load),
            Table.header("重载", d -> "+", C, C, this::reload)

    );

    @Override
    protected List<Table.Header<KInfo>> headers() {
        return headers;
    }

    @Override
    protected List<KInfo> listData() {
        return klineManager.kItems();
    }

    @Override
    protected void initHeaderViews(LinearLayout condition) {
        condition.addView(load = template.button("加载", 10, 7));
        condition.addView(reload = template.button("重载", 10, 7));
    }

    @Override
    protected void initHeaderBehaviours() {
        ClickUtil.onAsyncClick(load, this::loadAll);
        ClickUtil.onPopConfirm(reload, "K线重载", "确定重新加载全部K线数据？", this::reloadAll);
    }

    @Override
    protected int getTotalColumns() {
        return headers.size();
    }

    @Override
    protected int getFixedColumns() {
        return headers.size();
    }


    private void detail(KInfo info) {
        this.show(KlineListPage.class, info);
    }

    /**
     * 加载全部K线
     * @return 提示信息
     */
    private String loadAll() {
        return "所有K线加载完成，数据量：" + klineManager.loadAll();
    }

    /**
     * 重载全部K线
     * @return 提示信息
     */
    private String reloadAll() {
        return "所有K线重载完成，数据量：" + klineManager.reloadAll();
    }

    private void load(KInfo info) {
        PopupUtil.alert(this.context, "加载完成，数据量：" + klineManager.load(info));
    }

    private void reload(KInfo info) {
        PopupUtil.confirm(this.context, "重载K线", "确定重载吗？",
                () -> PopupUtil.alert(this.context, "重载完成，数据量：" + klineManager.reload(info)));
    }

}
