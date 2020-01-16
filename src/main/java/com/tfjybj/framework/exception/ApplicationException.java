package com.tfjybj.framework.exception;

import com.tfjybj.framework.config.Config;
import com.tfjybj.framework.config.ConfigManager;

import java.text.MessageFormat;

/**
 * ApplicationException异常, 继承自Exception.
 */
@SuppressWarnings("serial")
public class ApplicationException extends BaseException {
    private static Config messageConfig;

    static {
        messageConfig = ConfigManager.getConfig("Message");
    }

    private String messageid;
    private String message;
    private String code;
    private Throwable cause;

    /**
     * 异常构造函数
     */
    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationException(String message, String code) {
        super(message, code);
    }

    public ApplicationException(String message, String code, Throwable cause) {
        super(message, code, cause);
    }

    /**
     * 异常构造函数
     *
     * @param messageid 消息ID
     * @param params    消息参数
     */
    public ApplicationException(String messageid, String[] params) {
        super(MessageFormat.format(messageConfig.getProperty(messageid), params));
        this.messageid = messageid;
    }

    /**
     * 异常构造函数
     *
     * @param messageid 消息ID
     * @param params    消息参数
     * @param cause     异常内容
     */
    public ApplicationException(String messageid, String[] params, Throwable cause) {
        super(MessageFormat.format(messageConfig.getProperty(messageid), params), cause);
        this.messageid = messageid;
        this.cause = cause;
    }
}
