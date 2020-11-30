package com.ggx.pay.core.adapter;

import com.ggx.pay.core.model.GGXPayCloseRequest;
import com.ggx.pay.core.model.GGXPayCloseResponse;
import com.ggx.pay.core.model.GGXPayPayRequest;
import com.ggx.pay.core.model.GGXPayPayResponse;
import com.ggx.pay.core.model.GGXPayQueryRequest;
import com.ggx.pay.core.model.GGXPayQueryResponse;
import com.ggx.pay.core.model.GGXPayRefundRequest;
import com.ggx.pay.core.model.GGXPayRefundResponse;

/**
 * 支付渠道适 -支付- 配器 接口
 * 
 * 
 * @author zai
 * 2017-08-25
 */
public interface GGXPayAdapter {
	
	/***
	 * 支付
	 * @param form
	 * @return
	 * @throws Exception
	 * 
	 * @author zai
	 * 2017-11-03
	 */
	GGXPayPayResponse pay(GGXPayPayRequest request);
	
	/**
	 * 查询订单
	 * @param form
	 * @return
	 * @throws Exception
	 * 
	 * @author zai
	 * 2017-11-03
	 */
	GGXPayQueryResponse query(GGXPayQueryRequest request);
	
	/**
	 * 退款
	 * @param form
	 * @return
	 * @throws Exception
	 * 
	 * @author zai
	 * 2017-11-03
	 */
	GGXPayRefundResponse refund(GGXPayRefundRequest request);
	
	/**
	 * 关闭订单
	 * @param form
	 * @return
	 * @throws Exception
	 * 
	 * @author zai
	 * 2017-11-03
	 */
	GGXPayCloseResponse close(GGXPayCloseRequest request);

	
}
