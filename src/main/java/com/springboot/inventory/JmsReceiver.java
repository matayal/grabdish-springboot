package com.springboot.inventory.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Component;

import com.springboot.inventory.dto.InventoryTable;
import com.springboot.inventory.model.Inventory;
import com.springboot.inventory.model.Order;
import com.springboot.inventory.service.SupplierService;
import com.springboot.inventory.util.JsonUtils;

import oracle.jms.AQjmsSession;

@Component

public class JmsReceiver implements SessionAwareMessageListener {

	@Autowired
	JmsTemplate jmsTemplate;

	@Autowired
	SupplierService supplierService;

	Logger logger = LoggerFactory.getLogger(JmsReceiver.class);

	@JmsListener(destination = "order2")
	public void reviewJavaDevMessage(String message, AQjmsSession session) {
		Order order = JsonUtils.read(message, Order.class);

		logger.info("orderId:" + order.getOrderid());
		logger.info("itemId:" + order.getItemid());
		logger.info("Inventory Session"+session);

		String location = evaluateInventory(order, session);
		inventoryEvent(order.getOrderid(), order.getItemid(), location);
		System.out.println("Received Message End process:Session: "+session);
	}

	public void inventoryEvent(String orderId, String itemId, String location) {

		Inventory inventory = new Inventory(orderId, itemId, location, "beer");
		String jsonString = JsonUtils.writeValueAsString(inventory);
		logger.info("Final Inventory msg" + jsonString+"\n");
		jmsTemplate.convertAndSend("inventory2", jsonString);
		logger.info(jmsTemplate.getDefaultDestinationName());

	}

	public String evaluateInventory(Order order, AQjmsSession session) {
		String itemId = order.getItemid();
		supplierService.removeInventory(itemId);
		InventoryTable viewInventory = supplierService.getInventory(itemId);
		logger.info("Evaluate Inventory Session"+session);
		String inventoryLocation = viewInventory != null ? viewInventory.getInventoryLocation(): "inventoryDoesNotExist";

		logger.info("InventoryServiceOrderEventConsumer orderId:" + order.getOrderid());
		logger.info("itemId:" + order.getItemid());
		logger.info("Evaluate Inventory end Session"+session);

		return inventoryLocation;
	}

	@Override
	public void onMessage(Message message, Session session) throws JMSException {
		// TODO Auto-generated method stub
		
	}
}
