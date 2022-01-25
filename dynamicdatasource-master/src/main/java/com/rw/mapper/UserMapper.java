package com.rw.mapper;

import com.rw.domain.User;

public interface UserMapper {

	public User getById(int id);
	
	public void deleteById(int id);
	
}
