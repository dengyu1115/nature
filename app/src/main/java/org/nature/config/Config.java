package org.nature.config;

public interface Config {

    /**
     * 服务通道id
     */
    String CHANNEL_ID = "NATURE_CHANNEL";
    /**
     * 服务通道name
     */
    String CHANNEL_NAME = "NATURE服务通道";
    String DB_PATH_JOB = "nature/common.db";
    String DB_PATH_HTML = "nature/html.db";
    String SQL_JOB = "select name,script from job_config where status='1'";
    String SQL_HTML = "select config from page_config where id=";

}
