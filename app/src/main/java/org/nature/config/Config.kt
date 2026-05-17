package org.nature.config

import android.os.Environment
import java.io.File

object Config {
    val INTERNAL: File = Environment.getExternalStorageDirectory()

    /**
     * 服务通道id
     */
    const val CHANNEL_ID = "NATURE_CHANNEL"

    /**
     * 服务通道name
     */
    const val CHANNEL_NAME = "NATURE服务通道"

    const val DB_PATH_JOB = "nature/common.db"
    const val DB_PATH_HTML = "nature/html.db"
    const val SQL_JOB = "select name,script from job_config where status='1'"
    const val SQL_HTML = "select config from page_config where id="
}
