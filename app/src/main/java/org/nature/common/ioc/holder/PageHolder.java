package org.nature.common.ioc.holder;

import org.nature.common.ioc.annotation.PageView;
import org.nature.common.model.PageInfo;
import org.nature.common.page.Page;

import java.util.*;

/**
 * 页面实例持有
 * @author nature
 * @version 1.0.0
 * @since 2019/11/21 16:33
 */
public class PageHolder {

    private static final Map<String, List<List<PageInfo>>> CTX = new HashMap<>();

    public static List<List<PageInfo>> get(String name) {
        return CTX.get(name);
    }

    @SuppressWarnings("unchecked")
    public synchronized static void register(Class<?> cls, PageView pageView) {
        String group = pageView.group();
        String name = pageView.name();
        int col = pageView.col();
        int row = pageView.row();
        if (col == 0 || row == 0) {
            return;
        }
        List<List<PageInfo>> list = CTX.computeIfAbsent(group, k -> new ArrayList<>());
        while (list.size() < col) {
            list.add(new ArrayList<>());
        }
        List<PageInfo> pages = list.get(col - 1);
        PageInfo page = new PageInfo();
        page.setName(name);
        page.setCls((Class<? extends Page>) cls);
        page.setOrder(row);
        pages.add(page);
        pages.sort(Comparator.comparing(PageInfo::getOrder));
    }
}
