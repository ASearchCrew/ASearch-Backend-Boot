package com.asearch.logvisualization.service;

import javax.annotation.Resource;

import com.asearch.logvisualization.dao.MoniteringDao;

public class MoniteringServiceImpl implements MoniteringService{
	
	@Resource(name="MoniteringDao")
	private MoniteringDao moniteringDao;
	
	@Override
	public void getServerStatus() {
		// TODO Auto-generated method stub
		
	}

}
