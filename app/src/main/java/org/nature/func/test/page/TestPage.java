package org.nature.func.test.page;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.NotifyUtil;
import org.nature.common.util.PopUtil;
import org.nature.common.view.ViewTemplate;

/**
 * 测试功能
 * @author Nature
 * @version 1.0.0
 * @since 2024/9/14
 */
@PageView(name = "测试功能", group = "基础", col = 2, row = 2)
public class TestPage extends Page {

    /**
     * view模板
     */
    private ViewTemplate template;

    private Button ttsBtn;

    @Override
    protected void makeStructure(LinearLayout page, Context context) {
        template = ViewTemplate.build(context);
        page.addView(ttsBtn = template.button("tts", 80, 30));
    }

    @Override
    protected void onShow() {
        ClickUtil.onClick(ttsBtn, () -> {
            EditText ttsText = template.areaText(360, 160);
            LinearLayout line = template.line(500, 230, template.textView("内容", 60, 30), ttsText);
            PopUtil.confirm(ttsBtn.getContext(), "请输入要转语音的文本", line, () -> {
                String text = ttsText.getText().toString();
                NotifyUtil.speak(text);
            });
        });
    }

}
