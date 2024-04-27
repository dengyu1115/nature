package org.nature.common.exception;

import java.util.function.Supplier;

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

    /**
     * 抛出警告异常
     * @param reason 原因
     * @param msg    消息
     */
    public static void check(Supplier<Boolean> reason, String msg) {
        if (reason.get()) {
            throw new Warn(msg);
        }
    }

}
