package org.nature.biz.common.page;

import android.widget.Button;
import org.nature.biz.common.manager.KlineManager;
import org.nature.biz.common.model.KInfo;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.PopUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.TableView;

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
    private final List<TableView.D<KInfo>> ds = Arrays.asList(
            TableView.row("名称", d -> TextUtil.text(d.getName()), C, S, KInfo::getName),
            TableView.row("编号", d -> TextUtil.text(d.getCode()), C, C, KInfo::getCode),
            TableView.row("类型", d -> TextUtil.text(d.getType()), C, C, KInfo::getType),
            TableView.row("明细", d -> "+", C, C, this::detail),
            TableView.row("加载", d -> "+", C, C, this::load),
            TableView.row("重载", d -> "+", C, C, this::reload)

    );

    @Override
    protected List<TableView.D<KInfo>> define() {
        return ds;
    }

    @Override
    protected List<KInfo> listData() {
        return klineManager.kItems();
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(load = template.button("加载", 80, 30));
        searchBar.addConditionView(reload = template.button("重载", 80, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        ClickUtil.onAsyncClick(load, this::loadAll);
        ClickUtil.onPopConfirm(reload, "K线重载", "确定重新加载全部K线数据？", this::reloadAll);
    }

    @Override
    protected int getTotalColumns() {
        return ds.size();
    }

    @Override
    protected int getFixedColumns() {
        return ds.size();
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
        PopUtil.alert(this.context, "加载完成，数据量：" + klineManager.load(info));
    }

    private void reload(KInfo info) {
        PopUtil.confirm(this.context, "重载K线", "确定重载吗？",
                () -> PopUtil.alert(this.context, "重载完成，数据量：" + klineManager.reload(info)));
    }

}
