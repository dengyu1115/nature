package org.nature.common.exception;

import lombok.Getter;

@Getter
public class Warn extends RuntimeException {

    private String msg;

    public Warn(String msg) {
        super(msg);
    }

}
