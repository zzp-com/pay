package com.zzp.pay.service.impl;

import com.google.gson.Gson;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.zzp.pay.dao.PayInfoMapper;
import com.zzp.pay.enums.PayPlatformEnum;
import com.zzp.pay.pojo.PayInfo;
import com.zzp.pay.service.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PayServiceImpl implements IPayService {
    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private final static String QUEUE_PAY_NOTITY = "payNotity";


    @Override
    public PayResponse create(String orderId, BigDecimal amout, BestPayTypeEnum payType) {
        //判断是否是这两种支付方式
        if (payType != BestPayTypeEnum.WXPAY_NATIVE
                && payType != BestPayTypeEnum.ALIPAY_PC) {

        }
        //写入数据库
       PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
               PayPlatformEnum.getByPayTypeEnum(payType).getCode(),
               OrderStatusEnum.NOTPAY.name(),amout);
        payInfoMapper.insertSelective(payInfo);
        //设置请求参数
        PayRequest request = new PayRequest();
        request.setOrderName("7604167-最好的支付sdk");
        request.setOrderId(orderId);
        request.setOrderAmount(amout.doubleValue());
        request.setPayTypeEnum(payType);
        //发起支付，并返回信息
        PayResponse response = bestPayService.pay(request);
        log.info("发起支付 response={}", response);
        return response;
    }

    @Override
    public String asyncNotify(String notifyData) {
        //签名校验
        PayResponse response = bestPayService.asyncNotify(notifyData);

        //金额校验（从数据库查订单）
       PayInfo payInfo =  payInfoMapper.selectByOrderNo(Long.parseLong(response.getOrderId()));
        if(payInfo == null){
            //比较严重，可以发出警告：钉钉，短信
            throw new RuntimeException("通过orderNo查询到的结果是null");
        }
        //如果订单支付状态不是已支付
        if(!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS)){
            if(payInfo.getPayAmount().compareTo(BigDecimal.valueOf(response.getOrderAmount())) != 0){
                throw new RuntimeException("异步通知中的金额和数据库中的不一致，orderNo="+response.getOrderId());
            }
            //修改订单支付状态
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(response.getOutTradeNo());
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }
        //TODO 发送MQ消息
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTITY,new Gson().toJson(payInfo));
        log.info("异步通知 response={}", response);
        if (response.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            //告诉微信不要在通知了
            return "<xml>" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        } else if (response.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            return "success";
        }
        throw new RuntimeException("异步通知中错误的支付平台");
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {
        return    payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
