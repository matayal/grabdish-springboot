package com.springboot.inventory.config;

import java.sql.SQLException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jms.AQjmsFactory;


@Configuration
public class OracleAQConfiguration {
    Logger logger = LoggerFactory.getLogger(OracleAQConfiguration.class);
  
    private String password="MayankTayal1234";
    private String url="jdbc:oracle:thin:@grabdishi_high?TNS_ADMIN=C:/Users/HP/Desktop/Wallet_grabdishi";
    
    private static String order = "order1";
	private static String orderTable = "orderTable1";
	private static String inventory = "inventory1";
	private static String inventoryTable = "inventoryTable1";

    @Bean
    public DataSource dataSource() throws SQLException {
        OracleDataSource ds = new OracleDataSource();
        ds.setUser("admin");
        ds.setPassword(password);
        ds.setURL(url);
        logger.info("OracleAQConfiguration-->dataSource success"+ds);
        return ds;
    }

    @Bean
    public QueueConnectionFactory connectionFactory(DataSource dataSource) throws JMSException, SQLException {
        logger.info("OracleAQConfiguration-->AQ factory success");
        return AQjmsFactory.getQueueConnectionFactory(dataSource);
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory conFactory) throws Exception{
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestinationName(inventory);
        jmsTemplate.setSessionTransacted(true);
        jmsTemplate.setConnectionFactory(conFactory);

        logger.info("Jms Configuration-->jms template success");
        return jmsTemplate;
    }
}