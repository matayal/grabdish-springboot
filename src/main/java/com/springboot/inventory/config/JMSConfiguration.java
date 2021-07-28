package com.springboot.inventory.config;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.springboot.inventory.listener.JmsReceiver;

@Configuration
@EnableJms
public class JMSConfiguration {
    private static final String ORDER_QUEUE = "order2";
    private static final String INVENTORY_QUEUE = "inventory2";
    Logger logger = LoggerFactory.getLogger(JMSConfiguration.class);


    @Autowired
    private JmsReceiver jmsReceiver; 

    @Bean
    public DefaultMessageListenerContainer messageListenerContainer(ConnectionFactory conFactory, DataSource dataSource) throws Exception {
        DefaultMessageListenerContainer dmlc = new DefaultMessageListenerContainer();
        dmlc.setDestinationName(ORDER_QUEUE);
        dmlc.setSessionTransacted(true);
        dmlc.setConnectionFactory(conFactory);

        DataSourceTransactionManager manager = new DataSourceTransactionManager();
        manager.setDataSource(dataSource);
        dmlc.setTransactionManager(manager);
        logger.info("Jms Configuration-->messageListenerContainer part1 success");

       // dmlc.setMessageListener(jmsReceiver);

        logger.info("Jms Configuration-->messageListenerContainer part2 success"+"\n"+ dmlc.getMessageListener() +"\n"+dmlc.getMessageConverter()+"\n"+dmlc.getActiveConsumerCount());

        return dmlc;
    }


}

