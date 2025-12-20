package org.nature.common.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

import java.util.List;

/**
 * 上下文工具
 * @author nature
 * @version 1.0.0
 * @since 2025/12/20
 */
public class CtxUtil {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    /**
     * 初始化
     * @param context 上下文
     */
    public static void init(Context context) {
        CtxUtil.context = context;
    }

    /**
     * 获取上下文
     * @return 上下文
     */
    public static Context get() {
        return CtxUtil.context;
    }

    public static void startService(Class<?> clazz) {
        context.startService(new Intent(context, clazz));
    }

    public static void stopService(Class<?> clazz) {
        context.stopService(new Intent(context, clazz));
    }

    public static boolean isServiceRunning(Class<?> clazz) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        List<RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo i : services) {
            if (clazz.getName().equals(i.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
