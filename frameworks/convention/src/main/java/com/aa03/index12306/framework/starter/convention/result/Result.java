package com.aa03.index12306.framework.starter.convention.result;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 全局返回对象
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private final long serialVersionUID = 1;

    /**
     * 正确返回码
     */
    public final static String SUCCESS_CODE = "0";

    /**
     * 返回码
     */
    private String code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 请求ID
     */
    private String requestId;

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}
