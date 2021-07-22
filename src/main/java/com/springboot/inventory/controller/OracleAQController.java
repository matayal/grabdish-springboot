package com.springboot.inventory.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.inventory.model.Order;
import com.springboot.inventory.service.OracleAQService;

@EnableTransactionManagement
@RestController
@RequestMapping("/inventory")
@CrossOrigin(maxAge = 3600)
public class OracleAQController {
	
	@Autowired
	private OracleAQService oracleAQService;

	@PostConstruct
	@GetMapping(value = "/oracleAQ")
	public Map<String, Object> oracleAQ(/* @RequestBody Order userData */) throws Exception {
		Map<String, Object> response = new HashMap();

		String createQueue = oracleAQService.createQueue();
		String sendMessage = oracleAQService.sendMessage();
		String browseMessage = oracleAQService.browseMessage();

		response.put("ResponseCode", "200");
		response.put("ResponseText", "Success");
		response.put("ResponseText", createQueue +  " "+sendMessage +" " +browseMessage /*+" " + consumeMessage*/);

		return response;
	}

	
}