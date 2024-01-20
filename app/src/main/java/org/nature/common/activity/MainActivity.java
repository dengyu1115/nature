package org.nature.common.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.nature.common.ioc.starter.ComponentStarter;
import org.nature.common.page.BasicPage;
import org.nature.common.page.MainPage;
import org.nature.common.util.NotifyUtil;

import static android.Manifest.permission.*;

/**
 * 应用入口（对其他组件使用单例模式加载）
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
public class MainActivity extends AppCompatActivity {
    /**
     * 请求全局存储权限
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    /**
     * 全局页面对象
     */
    private BasicPage view;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 初始化处理
        super.onCreate(savedInstanceState);
        //  请求全局存储权限
        this.verifyStoragePermissions(this);
        // 启动组件（控制只执行一次）
        if ((this.getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) == 0) {
            // 单例组件加载
            ComponentStarter.getInstance().start(this);
            // 通知工具初始化
            NotifyUtil.init(MainActivity.this);
        }
        // 全局页面初始化
        view = new BasicPage(this);
        this.setContentView(view);
        // 加载首页
        view.show(MainPage.class);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 控制按返回时候让应用后台运行或者执行关闭当前操作页面回上个页面
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            int size = view.viewSize();
            if (size == 1) {
                return this.moveTaskToBack(true);
            } else {
                view.dispose();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void verifyStoragePermissions(Activity activity) {
        // 检测是否有写的权限
        int permission = ActivityCompat.checkSelfPermission(activity, MANAGE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // 没有写的权限，去申请写的权限，会弹出对话框
            String[] permissions = {MANAGE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_EXTERNAL_STORAGE);
        }
    }


}
