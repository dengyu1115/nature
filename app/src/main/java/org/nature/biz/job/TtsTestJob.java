package org.nature.biz.job;

import org.nature.common.constant.Const;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.DateUtil;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 语音测试
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "tts_test_job", name = "TTS测试")
public class TtsTestJob implements Job {

    public static final AtomicInteger COUNT = new AtomicInteger(0);

    @Override
    public void exec(Date date) {
        String time = DateUtil.format(date, Const.FORMAT_DATETIME);
        NotifyUtil.speak("测试语音，第" + COUNT.getAndIncrement() + "次提示，时间：" + time);
    }

}
