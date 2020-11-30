package com.ggx.pay.spring.boot.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ggx.pay.core.GGXPayService;
import com.ggx.pay.core.adapter.impl.AlipayAdapter;
import com.ggx.pay.core.adapter.impl.WxpayAdapter;
import com.ggx.pay.core.config.GGXPayConfig;
import com.ggx.pay.core.platforms.alipay.config.AlipayConfig;
import com.ggx.pay.core.platforms.weixin.config.WxpayConfig;
import com.ggx.pay.core.platforms.weixin.service.impl.WxpayService;

/**
 * 
 * xz支付 spring boot自动配置
 * 
 * @author zai
 * 2018-06-12 19:29:46
 */
@Configuration
@ConditionalOnProperty(prefix = GGXPaySpringBootAutoConfig.PROP_PREFIX, name="enabled", havingValue = "true")
@ConditionalOnMissingBean({GGXPayService.class})
public class GGXPaySpringBootAutoConfig {
	
	
	protected static final String PROP_PREFIX = "com.xzcode.pay";
	
	/**
	 * 实例化xz支付服务
	 * @return
	 * 
	 * @author zai
	 * 2018-06-12 19:37:22
	 */
	@Bean
	public GGXPayService gGXPayService() {
		
		GGXPayConfig config = gGXPayConfig();
		
		AlipayConfig alipayConfig = alipayConfig();
		WxpayConfig wxpayConfig = wxpayConfig();
		
		//初始化支付宝支付适配器
		AlipayAdapter alipayAdapter = new AlipayAdapter(alipayConfig);
		
		//初始化微信支付服务
		WxpayService wxpayService = new WxpayService(wxpayConfig);
		
		//初始化微信支付适配器
		WxpayAdapter wxpayAdapter = new WxpayAdapter(wxpayService);
		
		//初始化xz整合支付服务
		GGXPayService gGXPayService = new GGXPayService(config);
		
		gGXPayService.setAlipayAdapter(alipayAdapter);
		gGXPayService.setWxpayAdapter(wxpayAdapter);
		
		return gGXPayService;
	}
	
	/**
	 * 读取支付配置
	 * @return
	 * 
	 * @author zai
	 * 2018-06-12 19:38:14
	 */
	@Bean
	@ConfigurationProperties(prefix = GGXPaySpringBootAutoConfig.PROP_PREFIX)
	public GGXPayConfig gGXPayConfig() {
		return new GGXPayConfig();
	}
	
	/**
	 * 获取支付宝配置
	 * @return
	 * 
	 * @author zai
	 * 2018-06-12 19:35:17
	 */
	@Bean
	public AlipayConfig alipayConfig() {
		GGXPayConfig gGXPayConfig = gGXPayConfig();
		AlipayConfig alipayConfig = new AlipayConfig();
		
		alipayConfig.setServerUrl(gGXPayConfig.getAlipayServerUrl());
		alipayConfig.setAppId(gGXPayConfig.getAlipayAppId());
		alipayConfig.setAlipayPublicKey(gGXPayConfig.getAlipayPublicKey());
		alipayConfig.setAppPrivateKey(gGXPayConfig.getAlipayAppPrivateKey());
		alipayConfig.setCharset(gGXPayConfig.getAlipayCharset());
		alipayConfig.setFormat(gGXPayConfig.getAlipayFormat());
		alipayConfig.setSignType(gGXPayConfig.getAlipaySignType());
		
		alipayConfig.setPayNotifyUrl(gGXPayConfig.getAlipayPayNotifyUrl());
		alipayConfig.setRefundNotifyUrl(gGXPayConfig.getAlipayRefundNotifyUrl());
		
		return alipayConfig;
	}
	
	/**
	 * 获取微信支付配置
	 * @return
	 * 
	 * @author zai
	 * 2018-06-12 19:35:23
	 */
	@Bean
	public WxpayConfig wxpayConfig() {
		
		GGXPayConfig gGXPayConfig = gGXPayConfig();
		WxpayConfig wxpayConfig = new WxpayConfig();
		
		wxpayConfig.setAppId(gGXPayConfig.getWxAppId());
		
		wxpayConfig.setAppSecret(gGXPayConfig.getWxAppSecret());
		
		wxpayConfig.setMchId(gGXPayConfig.getWxMchId());
		
		wxpayConfig.setPayNotifyUrl(gGXPayConfig.getWxPayNotifyUrl());
		
		wxpayConfig.setRefundNotifyUrl(gGXPayConfig.getWxRefundNotifyUrl());
		
		return wxpayConfig;
	}
	
	

}
