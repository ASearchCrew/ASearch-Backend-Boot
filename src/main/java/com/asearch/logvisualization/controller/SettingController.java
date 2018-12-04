package com.asearch.logvisualization.controller;

import java.io.OutputStream;
import java.net.Socket;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@Api(description = "filebeat 설정", tags = {"set"})
@RequestMapping(value = "/api/v1/set", produces = "application/json")
public class SettingController {
	
	private static final int PORT = 8080;
	
	@RequestMapping(value = "/setting", method = RequestMethod.POST)
	public void setPath(@RequestParam(value = "path", required = true) String path) {
		try {
			Socket socket = new Socket("192.168.157.128", PORT);
			
			OutputStream stream = socket.getOutputStream();
			stream.write(path.getBytes());
			socket.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
