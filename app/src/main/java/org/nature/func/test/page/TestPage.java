package org.nature.func.test.page;

import android.widget.LinearLayout;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.NotifyUtil;
import org.nature.common.view.Button;
import org.nature.common.view.Input;

/**
 * 测试功能
 * @author Nature
 * @version 1.0.0
 * @since 2024/9/14
 */
@PageView(name = "测试功能", group = "基础", col = 2, row = 2)
public class TestPage extends Page {

    private Button ttsBtn;

    @Override
    protected void makeStructure() {
        page.addView(ttsBtn = template.button("tts", 10, 7));
    }

    @Override
    protected void onShow() {
        ClickUtil.onClick(ttsBtn, () -> {
            Input ttsText = template.textArea(40, 40);
            LinearLayout line = template.line(50, 50, template.text("内容", 5, 7), ttsText);
            template.confirm("请输入要转语音的文本", line, () -> {
                String text = ttsText.getValue();
                NotifyUtil.speak(text);
            });
        });
    }

}
