package com.springboot.inventory.service;

public interface OracleAQService {

	String createQueue();

	String sendMessage();

	String browseMessage();
	
}
