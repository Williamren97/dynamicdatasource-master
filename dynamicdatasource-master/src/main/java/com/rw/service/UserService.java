package com.rw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rw.domain.User;
import com.rw.mapper.UserMapper;


@Service
public class UserService {

	@Autowired
	private UserMapper userMapper;
	
	public User getUser(int id) {
		return this.userMapper.getById(id);
	}
	
	public void delete(int id) {
		this.userMapper.deleteById(id);
	}
	
	@Scheduled(cron = "0,20,40 * * * * ?")
	@ScheduledLock
	public void testLock(){
    		logger.info(ServerTimer.getFull());
	}
}
