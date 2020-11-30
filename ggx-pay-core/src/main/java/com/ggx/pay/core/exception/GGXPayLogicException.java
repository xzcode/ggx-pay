package com.ggx.pay.core.exception;

/**
 * 统一支付异常包装类
 * 
 * 
 * @author zai
 * 2017-08-25
 */
@SuppressWarnings("serial")
public class GGXPayLogicException extends RuntimeException{


	public GGXPayLogicException() {
		super();
	}

	public GGXPayLogicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GGXPayLogicException(String message, Throwable cause) {
		super(message, cause);
	}

	public GGXPayLogicException(String message) {
		super(message);
	}

	public GGXPayLogicException(Throwable cause) {
		super(cause);
	}
	
	@Override
	public synchronized Throwable fillInStackTrace() {
		return super.fillInStackTrace();
	}
}
