package org.nature.common.util;

import java.io.File;
import java.io.IOException;

/**
 * 文件操作工具类
 * @author nature
 * @version 1.0.0
 * @since 2019/11/21 16:36
 */
public class FileUtil {

    /**
     * 创建一个原本不存在的文件
     * @param file file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void createIfNotExists(File file) {
        if (file.exists()) {
            return;
        }
        try {
            File parent = file.getParentFile();
            assert parent != null;
            if (!parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
