package org.nature.config;

public interface Config {

    /**
     * 服务通道id
     */
    String CHANNEL_ID = "NATURE_TEST_CHANNEL";
    /**
     * 服务通道name
     */
    String CHANNEL_NAME = "NATURE_TEST服务通道";
    String DB_PATH_JOB = "nature_test/job.db";
    String DB_PATH_HTML = "nature_test/html.db";
    String SQL_JOB = "select from job where status='1'";
    String SQL_HTML = "select config from page_config where id=";

}
