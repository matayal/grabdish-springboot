package com.springboot.inventory.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.springboot.inventory.dto.InventoryTable;
import com.springboot.inventory.model.Inventory;
import com.springboot.inventory.model.Order;
import com.springboot.inventory.service.OracleAQService;
import com.springboot.inventory.service.SupplierService;
import com.springboot.inventory.util.JsonUtils;

import oracle.AQ.AQQueueTable;
import oracle.AQ.AQQueueTableProperty;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jms.AQjmsDestination;
import oracle.jms.AQjmsDestinationProperty;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;

@Service
public class OracleAQServiceImpl implements OracleAQService {

	@Autowired
	private SupplierService supplierService;

	@Value("${oracle.username}")
	private String username;

	@Value("${oracle.password}")
	private String password;

	@Value("${oracle.url}")
	private String url;

	private static String deQueueOrder = "orderQueue60";
	private static String deQueueOrderTable = "orderQueueTable60";
	private static String enQueueInventory = "inventoryQueue60";
	private static String enQueueInventoryTable = "inventoryQueueTable60";
	Logger logger = LoggerFactory.getLogger(OracleAQServiceImpl.class);

	public QueueConnection getConnection() {

		QueueConnectionFactory queueFac = null;
		QueueConnection queueCon = null;
		try {
			OracleDataSource ds = new OracleDataSource();
			ds.setUser(username);
			ds.setPassword(password);
			ds.setURL(url);
			queueFac = AQjmsFactory.getQueueConnectionFactory(ds);
			queueCon = queueFac.createQueueConnection(username, password);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return queueCon;
	}

	@Override
	public String createQueue() {

		String status = null;
		AQQueueTableProperty qt_prop;
		AQQueueTable orderQueuetable = null;
		AQQueueTable inventoryQueuetable = null;

		AQjmsDestinationProperty dest_prop;
		Queue orderQueue = null;
		Queue inventoryQueue = null;
		try {
			logger.info("WELCOME TO THE CONNECTION...");
			QueueConnection QCon = getConnection();
			Session session = QCon.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
			dest_prop = new AQjmsDestinationProperty();
			qt_prop = new AQQueueTableProperty("SYS.AQ$_JMS_TEXT_MESSAGE");

			orderQueuetable = ((AQjmsSession) session).createQueueTable(username, deQueueOrderTable, qt_prop);
			inventoryQueuetable = ((AQjmsSession) session).createQueueTable(username,enQueueInventoryTable, qt_prop);

			orderQueue = ((AQjmsSession) session).createQueue(orderQueuetable, deQueueOrder, dest_prop);
			inventoryQueue = ((AQjmsSession) session).createQueue(inventoryQueuetable, enQueueInventory, dest_prop);
			//inventoryQueue = ((AQjmsSession) session).createQueue(orderQueuetable, enQueueInventory, dest_prop);

			((AQjmsDestination) orderQueue).start(session, true, true);
			logger.info("ORDER QUEUE TABLE CREATED" + orderQueuetable);
			logger.info("ORDER QUEUE CREATED" + orderQueue);

			((AQjmsDestination) inventoryQueue).start(session, true, true);
		    logger.info("INVENTORY QUEUE TABLE CREATED" + inventoryQueuetable);
			logger.info("INVENTORY QUEUE CREATED" + inventoryQueue);

			status = "Queue creation successful";
		} catch (Exception e) {
			e.printStackTrace();
			status = "Queue creation failed";
		}
		return status;
	}

	@Override
	public String sendMessage() {

		String status = null;
		try {
			// Order Process
			Order order = new Order("11", "11", "inventoryLocation");
			String jsonOrderString = JsonUtils.writeValueAsString(order);
			String orderProcess = producerConsumer(jsonOrderString, deQueueOrder);
			logger.info("ORDER PROCESS- ENQUEUE/DEQUEU STATUS :" + orderProcess);

			// evaluate inventory
			String inventorylocation = evaluateInventory(order.getItemid());
			Inventory inventory = new Inventory(order.getOrderid(), order.getItemid(), inventorylocation, "beer");
			logger.info("INVENTORY EVALUATION RESULT:" + inventory);

			// inventory process
			String jsonInventoryString = JsonUtils.writeValueAsString(inventory);
			String inventoryProcess = producerConsumer(jsonInventoryString, enQueueInventory);
			logger.info("INVENTORY PROCESS- ENQUEUE/DEQUEU STATUS :" + inventoryProcess);

			if (orderProcess.equalsIgnoreCase("Success") && inventoryProcess.equalsIgnoreCase("Success")) {
				status = "Successfully AQ process completed";
			} else {
				status = "Failed AQ process";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return status;
	}

	public String producerConsumer(String message, String queue) {
		String status = null;
		try {
			QueueConnection QCon = getConnection();
			Session session = QCon.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
			QCon.start();
			Queue producerOrderQueue = ((AQjmsSession) session).getQueue(username, queue);
			MessageProducer producer = session.createProducer(producerOrderQueue);
			TextMessage tMsg = session.createTextMessage(message);
			producer.send(tMsg);
			logger.info("ORDER MESSAGE SENT= " + tMsg.getText());
			producer.close();

			Queue consumerOrderQueue = ((AQjmsSession) session).getQueue(username, queue);
			MessageConsumer consumer = session.createConsumer(consumerOrderQueue);
			TextMessage msg = (TextMessage) consumer.receive();
			logger.info("ORDER MESSAGE RECEIVED " + msg.getText());
			consumer.close();
			QCon.close();
			status = "Success";
		} catch (Exception e) {
			e.printStackTrace();
			status = "Failed";
		}
		return status;
	}

	@Override
	public String browseMessage() {
		Queue queue;
		String status = null;

		try {
			QueueConnection QCon = getConnection();
			Session session = QCon.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);

			QCon.start();
			queue = ((AQjmsSession) session).getQueue(username, enQueueInventory);
			QueueBrowser browser = session.createBrowser(queue);
			Enumeration enu = browser.getEnumeration();
			List list = new ArrayList();
			while (enu.hasMoreElements()) {
				TextMessage message = (TextMessage) enu.nextElement();
				list.add(message.getText());
			}
			for (int i = 0; i < list.size(); i++) {
				System.out.println("Browsed msg " + list.get(i));
			}
			browser.close();
			session.close();
			QCon.close();
			status = "Browse message successful";
		} catch (Exception e) {
			e.printStackTrace();
			status = "Browse message failed";
		}
		return status;
	}

	public String evaluateInventory(String itemId) throws SQLException {
		supplierService.removeInventory(itemId);
		InventoryTable getInventory = supplierService.getInventory(itemId);
		String inventoryLocation = getInventory != null ? getInventory.getInventoryLocation() : "inventoryDoesNotExist";
		logger.info("InventoryServiceOrderEventConsumer orderId:" + getInventory.getItemId());
		return inventoryLocation;
	}
}
