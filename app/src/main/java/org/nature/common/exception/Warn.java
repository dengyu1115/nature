package org.nature.common.exception;

/**
 * 自定义异常
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
public class Warn extends RuntimeException {

    public Warn(String msg) {
        super(msg);
    }

}
