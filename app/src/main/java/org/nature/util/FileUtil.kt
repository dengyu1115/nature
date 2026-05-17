package org.nature.util

import java.io.File
import java.io.IOException

/**
 * 文件操作工具类
 * @author nature
 * @version 1.0.0
 * @since 2019/11/21 16:36
 */
object FileUtil {

    /**
     * 创建一个原本不存在的文件
     * @param file file
     */
    @Suppress("ResultOfMethodCallIgnored")
    fun createIfNotExists(file: File) {
        if (file.exists()) {
            return
        }
        try {
            val parent = file.parentFile
            requireNotNull(parent)
            if (!parent.exists()) {
                parent.mkdirs()
            }
            file.createNewFile()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
