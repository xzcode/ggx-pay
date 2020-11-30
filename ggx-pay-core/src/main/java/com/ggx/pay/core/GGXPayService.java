package com.ggx.pay.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.ggx.pay.core.adapter.GGXPayAdapter;
import com.ggx.pay.core.config.GGXPayConfig;
import com.ggx.pay.core.constant.GGXPayConstant;
import com.ggx.pay.core.exception.GGXPayLogicException;
import com.ggx.pay.core.model.GGXPayCloseRequest;
import com.ggx.pay.core.model.GGXPayCloseResponse;
import com.ggx.pay.core.model.GGXPayNotification;
import com.ggx.pay.core.model.GGXPayPayRequest;
import com.ggx.pay.core.model.GGXPayPayResponse;
import com.ggx.pay.core.model.GGXPayQueryRequest;
import com.ggx.pay.core.model.GGXPayQueryResponse;
import com.ggx.pay.core.model.GGXPayRefundRequest;
import com.ggx.pay.core.model.GGXPayRefundResponse;
import com.ggx.pay.core.platforms.weixin.WxUtil;
import com.ggx.pay.core.util.AmountTransferUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 聚合支付服务类
 * 
 * 
 * @author zai
 * 2017-08-25
 */
public class GGXPayService {
	
	private static final Logger logger = LoggerFactory.getLogger(GGXPayService.class);
	
	private GGXPayConfig gGXPayConfig;
	
	private GGXPayAdapter alipayAdapter;
	
	private GGXPayAdapter wxpayAdapter;
	
	private Gson gson = new GsonBuilder().create();

	public GGXPayService(GGXPayConfig gGXPayConfig) {
		this.gGXPayConfig = gGXPayConfig;
	}
	
	/**
	 * 发起支付
	 *
	 * @param request
	 * @return
	 * 2020-11-30 16:29:45
	 */
	public GGXPayPayResponse pay(GGXPayPayRequest request) {
		
		if (request.getAmount() == 0L) {
			throw new GGXPayLogicException("Order amount must not be 0 !");
		}
		
		GGXPayPayResponse payResponse = null;
		
		switch (request.getPayChannel()) {
		
		case GGXPayConstant.PayChannel.ALIPAY:
			
			payResponse = this.alipayAdapter.pay(request);
			
			break;
		case GGXPayConstant.PayChannel.WEIXIN:
			
			payResponse = this.wxpayAdapter.pay(request);
			
			break;
			
		default:
			
			break;
		}
		
		return payResponse;
		
		
	}
	
	
	/**
	 * 支付订单查询
	 *
	 * @param request
	 * @return
	 * 2020-11-30 16:29:29
	 */
	public GGXPayQueryResponse query(GGXPayQueryRequest request){
		
		if (request.getTradeNo() == null || request.getOutTradeNo() == null ) {
			throw new GGXPayLogicException("Order TradeNo or OutTradeNo must not be null !");
		}
		
		GGXPayQueryResponse queryResponse = null;
		
		switch (request.getPayChannel()) {
		
		case GGXPayConstant.PayChannel.ALIPAY:
			
			queryResponse = this.alipayAdapter.query(request);
			
			break;
		case GGXPayConstant.PayChannel.WEIXIN:
			
			queryResponse = this.wxpayAdapter.query(request);
			
			break;
			
		default:
			
			break;
		}
		
		return queryResponse;
		
	}
	
	/**
	 * 退款请求
	 *
	 * @param request
	 * @return
	 * 2020-11-30 16:29:57
	 */
	public GGXPayRefundResponse refund(GGXPayRefundRequest request){
		
		if (request.getTradeNo() == null && request.getOutTradeNo() == null ) {
			throw new GGXPayLogicException("Order TradeNo or OutTradeNo must not be null !");
		}
		
		GGXPayRefundResponse refundResponse = null;
		
		switch (request.getPayChannel()) {
		
		case GGXPayConstant.PayChannel.ALIPAY:
			
			refundResponse = this.alipayAdapter.refund(request);
			
			break;
		case GGXPayConstant.PayChannel.WEIXIN:
			
			refundResponse = this.wxpayAdapter.refund(request);
			
			break;
			
		default:
			
			break;
		}
		
		return refundResponse;
	}
	
	/**
	 * 关闭交易请求
	 *
	 * @param request
	 * @return
	 * 2020-11-30 16:30:06
	 */
	public GGXPayCloseResponse close(GGXPayCloseRequest request){
		
		if (request.getOutTradeNo() == null ) {
			throw new GGXPayLogicException(" outTradeNo must not be null !");
		}
		GGXPayCloseResponse closeResponse = null;
		
		switch (request.getPayChannel()) {
		
		case GGXPayConstant.PayChannel.ALIPAY:
			
			closeResponse = this.alipayAdapter.close(request);
			
			break;
		case GGXPayConstant.PayChannel.WEIXIN:
			
			closeResponse = this.wxpayAdapter.close(request);
			
			break;
			
		default:
			
			break;
		}
		
		return closeResponse;
	}
	
	
	
	/**
	 * 支付宝 request 参数转  map 
	 * @param parameterMap
	 * @return 
	 * 
	 * @author zai
	 * 2018-02-06
	 */
	public Map<String, String> alipayParameterMapTransfer(Map<String, String[]> parameterMap) {
		
		Map<String, String> params = new TreeMap<>();

		for (String key : parameterMap.keySet()) {
			
			String[] vals = parameterMap.get(key);

			if (vals != null && vals.length > 0) {
				
				params.put(key, StringUtils.join(vals, ","));
				
			} else {
				
				params.put(key, "");
				
			}
			
		}
		return params;
	}
	
	/**
	 * 获取支付宝 支付-异步回调通知
	 * @param parameterMap
	 * @return
	 * 
	 * @author zai
	 * 2018-02-06
	 */
	public GGXPayNotification alipayGetPayNotification(Map<String, String[]> parameterMap){
		
		if (logger.isDebugEnabled()) {
			logger.debug("Alipay pay notifiction:{}", this.gson.toJson(parameterMap));
		}
		
		GGXPayNotification notification = new GGXPayNotification();
		Map<String, String> params = alipayParameterMapTransfer(parameterMap);
		boolean rsaCheckV1 = this.alipaySignatureRsaCheckV1(params);
		notification.setSignatureVerifySuccess(rsaCheckV1);
		if (!rsaCheckV1) {
			return notification;
		}
		String tradeStatus = params.get("trade_status");
		if ("TRADE_SUCCESS".equals(tradeStatus)) {
			notification.setTradeSuccess(true);
		}
		
		String outTradeNo = params.get("out_trade_no");
		notification.setOutTradeNo(outTradeNo);
		
		String tradeNo = params.get("trade_no");
		notification.setTradeNo(tradeNo);
		
		String totalAmount = params.get("total_amount");
		notification.setAmount(AmountTransferUtil.toCent(new BigDecimal(totalAmount)));
		
		String extraData = params.get("passback_params");
		notification.setExtraData(extraData);
		
		notification.setPayChannel(GGXPayConstant.PayChannel.ALIPAY);
		
		notification.setPlatformDefaultCallback("success");
		
		return notification;
	}
	
	
	public GGXPayNotification weixinGetPayNotification(InputStream inputStream){
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		try{
			return weixinGetPayNotification(new BufferedReader(inputStreamReader));
		} catch (Exception e) {
			throw new GGXPayLogicException("weixinGetPayNotification ERROR!", e);
		}
	}
	
	public GGXPayNotification weixinGetPayNotification(BufferedReader reader){
		StringBuilder sb = new StringBuilder();
		String readLine = null;
		try {
			
			while ((readLine = reader.readLine()) != null) {
				sb.append(readLine);
			}
		
			String body = sb.toString();
			return weixinGetPayNotification(body);
    	
		} catch (Exception e) {
			throw new GGXPayLogicException("WeixinGetPayNotification ERROR!", e);
		}
	}
	
	
	/**
	 * 获取微信 支付-异步回调通知
	 * @param parameterMap
	 * @return
	 * 
	 * @author zai
	 * 2018-02-06
	 */
	public GGXPayNotification weixinGetPayNotification(String xmlBody){
		
		if (logger.isDebugEnabled()) {
			logger.debug("Weixin pay notifiction:{}", xmlBody);
		}
		
		GGXPayNotification notification = new GGXPayNotification();
		
		notification.setPayChannel(GGXPayConstant.PayChannel.WEIXIN);
		
		//微信支付通知默认响应
		notification.setPlatformDefaultCallback("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
		
		
		Map<String, String> params = weixinParameterMapTransfer(xmlBody);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Weixin pay notifiction params:{}", params);
		}
		
		boolean signatureCheck = this.weixinSignatureMd5Check(params);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Weixin pay notifiction signatureCheck:{}", signatureCheck);
		}
		
		notification.setSignatureVerifySuccess(signatureCheck);
		
		if (!signatureCheck) {
			if (logger.isDebugEnabled()) {
				logger.debug("Weixin pay notifiction return:{}", gson.toJson(notification));
			}
			return notification;
		}
		String tradeStatus = params.get("result_code");
		if ("SUCCESS".equals(tradeStatus)) {
			notification.setTradeSuccess(true);
		}
		
		String outTradeNo = params.get("out_trade_no");
		notification.setOutTradeNo(outTradeNo);
		
		String tradeNo = params.get("transaction_id");
		notification.setTradeNo(tradeNo);
		
		String totalAmount = params.get("total_fee");
		notification.setAmount(AmountTransferUtil.toCent(new BigDecimal(totalAmount)));
		
		String extraData = params.get("attach");
		notification.setExtraData(extraData);
		
		
		if (logger.isDebugEnabled()) {
			logger.debug("Weixin pay notifiction return:{}", gson.toJson(notification));
		}
		
		return notification;
	}
	
	/**
	 * 支付宝签名RSA2 V1 验证
	 * @param params
	 * @return
	 * 
	 * @author zai
	 * 2018-02-06
	 */
	public boolean alipaySignatureRsaCheckV1(Map<String, String> params){
		try {
			return AlipaySignature.rsaCheckV1(params, this.gGXPayConfig.getAlipayPublicKey() , this.gGXPayConfig.getAlipayCharset(), this.gGXPayConfig.getAlipaySignType());
		} catch (AlipayApiException e) {
			throw new GGXPayLogicException("AlipaySignatureRsaCheckV1 ERROR!", e);
		}
	}
	
	/**
	 * 支付宝签名RSA2 V2 验证
	 * @param params
	 * @return
	 * 
	 * @author zai
	 * 2018-02-06
	 */
	public boolean AlipaySignatureRsaCheckV2 (Map<String, String> params){
		try {
			return AlipaySignature.rsaCheckV2(params, this.gGXPayConfig.getAlipayPublicKey() , this.gGXPayConfig.getAlipayCharset(), "RSA2");
		} catch (AlipayApiException e) {
			throw new GGXPayLogicException("AlipaySignatureRsaCheckV2 ERROR!", e);
		}
	}
	
	
	
	
	/**
	 * 微信 request body 参数转  map 
	 * @param parameterMap
	 * @return 
	 * 
	 * @author zai
	 * 2018-02-06
	 */
	public Map<String, String> weixinParameterMapTransfer(String body) {
		Map<String, String> params = WxUtil.fromXml(body);
		return params;
	}
	
	/**
	 * 检测 微信 返回状态码 是否为 ： 成功
	 * @param params
	 * @return
	 * 
	 * @author zai
	 * 2018-02-06
	 */
	public boolean weixinCheckReturnSuccess (Map<String, String> params){
		return "SUCCESS".equals(params.get("return_code"));
	}
	
	public boolean weixinSignatureMd5Check (Map<String, String> params){
		
		//移除sign用于签名验证
		String sign = params.remove("sign");
		
		String paramString = WxUtil.getParamString2((TreeMap<String, String>) params);
		
		//把sign存回去
		params.put("sign", sign);
		
		paramString += "&key=" + gGXPayConfig.getWxAppSecret();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Weixin pay notifiction weixinSignatureMd5Check, sign:{}, paramString:{}", sign, paramString);
		}
		
		String signMd5 = WxUtil.signMd5(paramString);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Weixin pay notifiction weixinSignatureMd5Check, sign:{}, checkSign:{}", sign, signMd5);
		}
		
		if (sign.equals(signMd5)) {
			return true;
		}
		
		return false;
	}
	

	public GGXPayConfig getDickpayConfig() {
		return gGXPayConfig;
	}


	public void setDickpayConfig(GGXPayConfig gGXPayConfig) {
		this.gGXPayConfig = gGXPayConfig;
	}


	public GGXPayAdapter getAlipayAdapter() {
		return alipayAdapter;
	}


	public void setAlipayAdapter(GGXPayAdapter alipayChargeAdapter) {
		this.alipayAdapter = alipayChargeAdapter;
	}


	public GGXPayAdapter getWeixinAdapter() {
		return wxpayAdapter;
	}


	public void setWxpayAdapter(GGXPayAdapter wxpayChargeAdapter) {
		this.wxpayAdapter = wxpayChargeAdapter;
	}
	
	
	
	
}
