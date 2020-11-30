package com.ggx.pay.core.adapter.impl;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ggx.pay.core.adapter.GGXPayAdapter;
import com.ggx.pay.core.constant.GGXPayConstant;
import com.ggx.pay.core.exception.GGXPayLogicException;
import com.ggx.pay.core.model.GGXPayCloseRequest;
import com.ggx.pay.core.model.GGXPayCloseResponse;
import com.ggx.pay.core.model.GGXPayPayRequest;
import com.ggx.pay.core.model.GGXPayPayResponse;
import com.ggx.pay.core.model.GGXPayQueryRequest;
import com.ggx.pay.core.model.GGXPayQueryResponse;
import com.ggx.pay.core.model.GGXPayRefundRequest;
import com.ggx.pay.core.model.GGXPayRefundResponse;
import com.ggx.pay.core.model.wx.WxPayResp;
import com.ggx.pay.core.platforms.weixin.WxUtil;
import com.ggx.pay.core.platforms.weixin.constant.WxpayCodeConstant;
import com.ggx.pay.core.platforms.weixin.service.impl.WxpayService;
import com.google.gson.Gson;

/**
 * 微信支付 适配器
 * 
 * 
 * @author zai
 * 2017-08-30
 */
public class WxpayAdapter implements GGXPayAdapter{
	
	private static final Logger logger = LoggerFactory.getLogger(WxpayAdapter.class);
	
	private Gson gson = new Gson();
	
	private WxpayService wxpayService;
	
	public WxpayAdapter(WxpayService wxpayService) {
		this.wxpayService = wxpayService;
	}

	@Override
	public GGXPayPayResponse pay(GGXPayPayRequest request){
		
		//参数请查看文档: https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_1
		
		try {
			Map<String, String> params = new TreeMap<>();
			
			
			//商品标题描述 String(128)
			params.put("body", request.getSubject());
			
			//商品详情 String(8192)
			params.put("detail", request.getBody());
			
			if (request.getExtraData() != null) {
				//附加参数，回调时候将原样返回
					params.put("attach", URLEncoder.encode(request.getExtraData(),"utf-8"));
			}
			
			
			//商户订单号
			params.put("out_trade_no", request.getOutTradeNo());
			
			//支付类型
			params.put("trade_type", "APP");
			
			//接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。
			params.put("notify_url", request.getNotifyUrl());
			
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			
			//交易起始时间- 需要对过期时间进行转换
			if (request.getTimeStart() != 0L) {
				params.put("time_start", dateFormat.format(new Date(request.getTimeStart())));
			}
			
			//交易过期时间- 需要对过期时间进行转换
			if (request.getTimeExpire() != 0L) {
				params.put("timeout_express", dateFormat.format(new Date(request.getTimeExpire())));
			}
			
			
			//订单总金额，单位为分
			params.put("total_fee", String.valueOf(request.getAmount()));
			
			Map<String, String> resultMap = this.wxpayService.pay(params);
			
			GGXPayPayResponse payResponse = new GGXPayPayResponse();
			
			payResponse.setPayChannel(GGXPayConstant.PayChannel.WEIXIN);
			
			payResponse.setBody((String) resultMap.get("body"));
			
			if (WxpayCodeConstant.SUCCESS.equals(resultMap.get("return_code"))) {
				payResponse.setSuccess(true);
			}else {
				payResponse.setSuccess(false);
				payResponse.setMessage((String) resultMap.get("return_msg"));
				return payResponse;
			}
			
			WxPayResp payResp = new WxPayResp();
			payResp.setNoncestr(UUID.randomUUID().toString().replaceAll("-", ""));
			payResp.setTimestamp(String.valueOf(System.currentTimeMillis()).substring(0, 10));
			payResp.setPrepayid((String) resultMap.get("prepay_id"));
			
			
			
			//参数签名
			Map<String, Object> signMap = new TreeMap<>();
			
			signMap.put("appid", wxpayService.getWeixinConfig().getAppId());			
			signMap.put("partnerid",wxpayService.getWeixinConfig().getMchId() );
			signMap.put("prepayid", payResp.getPrepayid());
			signMap.put("package", "Sign=WXPay");
			signMap.put("noncestr", payResp.getNoncestr());
			signMap.put("timestamp", payResp.getTimestamp());
			//signMap.put("key", wxpayService.getWeixinConfig().getAppSecret());
			
			String paramString = WxUtil.getParamString((TreeMap<String, Object>) signMap);
			
			paramString += "&key=" + wxpayService.getWeixinConfig().getAppSecret();
			
			String signMd5 = WxUtil.signMd5(paramString);
			
			payResp.setSign(signMd5);
			
			payResponse.setWxPayResp(payResp);
			
			//payResponse.setTradeNo(resultMap);
			
			return payResponse;
		
		} catch (Exception e) {
			throw new GGXPayLogicException(e);
		}
	}

	@Override
	public GGXPayQueryResponse query(GGXPayQueryRequest request) {
		
		Map<String, String> params = new TreeMap<>();
		
		//商户订单号
		params.put("out_trade_no", request.getOutTradeNo());
		
		GGXPayQueryResponse queryResponse = new GGXPayQueryResponse();
		
		queryResponse.setPayChannel(GGXPayConstant.PayChannel.WEIXIN);
		
		Map<String, String> resultMap = this.wxpayService.query(params);
		
		if (WxpayCodeConstant.SUCCESS.equals(resultMap.get("return_code"))) {
			queryResponse.setSuccess(true);
		}else {
			queryResponse.setSuccess(false);
			queryResponse.setMessage((String) resultMap.get("return_msg"));
			return queryResponse;
		}
		
		String tradeState = (String) resultMap.get("trade_state");
		
		
		if ("SUCCESS".equals(tradeState)) {
			queryResponse.setTradeStatus(GGXPayConstant.TradeStatus.SUCCESS);
		}else if ("USERPAYING".equals(tradeState)) {
			queryResponse.setTradeStatus(GGXPayConstant.TradeStatus.PAYING);
		}else if ("CLOSED".equals(tradeState)) {
			queryResponse.setTradeStatus(GGXPayConstant.TradeStatus.CLOSED);
		}else if ("REFUND".equals(tradeState) || "REVOKED".equals(tradeState)) {
			queryResponse.setTradeStatus(GGXPayConstant.TradeStatus.FINISHED);
		}else if ("PAYERROR".equals(tradeState)) {
			queryResponse.setTradeStatus(GGXPayConstant.TradeStatus.ERROR);
		}
		

		queryResponse.setTradeNo((String) resultMap.get("transaction_id"));
		
		queryResponse.setOutTradeNo((String) resultMap.get("out_trade_no"));
		
		
		
		return queryResponse;
	}

	@Override
	public GGXPayRefundResponse refund(GGXPayRefundRequest request) {
		
		
		Map<String, String> params = new TreeMap<>();
		
		//商户订单号
		params.put("out_trade_no", request.getOutTradeNo());
		
		params.put("total_fee", String.valueOf(request.getTotalAmount()));
		
		params.put("refund_fee", String.valueOf(request.getRefundAmount()));
		
		params.put("refund_desc", request.getRefundReason());
		
		
		
		
		
		GGXPayRefundResponse refundResponse = new GGXPayRefundResponse();
		
		refundResponse.setPayChannel(GGXPayConstant.PayChannel.WEIXIN);
		
		Map<String, String> resultMap = this.wxpayService.refund(params);
		
		if (WxpayCodeConstant.SUCCESS.equals(resultMap.get("return_code"))) {
			refundResponse.setSuccess(true);
		}else {
			refundResponse.setSuccess(false);
			refundResponse.setMessage((String) resultMap.get("return_msg"));
			return refundResponse;
		}
		
		refundResponse.setTradeNo((String) resultMap.get("transaction_id"));
		
		refundResponse.setOutTradeNo((String) resultMap.get("out_trade_no"));
		
		refundResponse.setRefundFee(Long.valueOf(resultMap.get("refund_fee")));
		
		
		return refundResponse;
	}



	@Override
	public GGXPayCloseResponse close(GGXPayCloseRequest request) {
		
		Map<String, String> params = new TreeMap<>();
		
		//商户订单号
		params.put("out_trade_no", request.getOutTradeNo());
		
		GGXPayCloseResponse closeResponse = new GGXPayCloseResponse();
		
		Map<String, String> resultMap = this.wxpayService.close(params);
		closeResponse.setPayChannel(GGXPayConstant.PayChannel.WEIXIN);
		
		
		
		if (WxpayCodeConstant.SUCCESS.equals(resultMap.get("return_code"))) {
			closeResponse.setSuccess(true);
		}else {
			closeResponse.setSuccess(false);
			closeResponse.setMessage((String) resultMap.get("return_msg"));
			return closeResponse;
		}
		
		return closeResponse;
	}

	
}
