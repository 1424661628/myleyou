package com.leyou.order.enums;

/**
 * Created by lvmen on 2019/9/18
 */
public enum PayStateEnum {

    NOT_PAY(0), SUCCESS(1), FAIL(2);

    int value;

    PayStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}