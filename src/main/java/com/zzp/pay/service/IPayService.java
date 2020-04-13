package com.zzp.pay.service;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import com.zzp.pay.pojo.PayInfo;

import java.math.BigDecimal;

public interface IPayService {

    /**
     * 创建/发起支付
     */
    PayResponse create(String orderId, BigDecimal amout, BestPayTypeEnum bestPayTypeEnum);

    /**
     * 处理异步通知
     */
    String asyncNotify(String notifyData);

    /**
     * 通过订单号查询支付记录
     * @param orderId
     * @return
     */
    PayInfo queryByOrderId(String orderId);
}
