package com.zzp.pay.service.impl;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.zzp.pay.PayApplicationTests;
import org.junit.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class PayServiceTest extends PayApplicationTests {

    @Autowired
    private PayServiceImpl payServiceImpl;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void create() {
      payServiceImpl.create("1234554674653453", BigDecimal.valueOf(0.01), BestPayTypeEnum.WXPAY_NATIVE);
    }

    @Test
    public void sendMqMsg(){
        amqpTemplate.convertAndSend("payNotify","hello");
    }

}