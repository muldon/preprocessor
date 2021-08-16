package com.ufu.preprocessor.repository;

import org.springframework.data.repository.CrudRepository;

import com.ufu.preprocessor.to.User;



public interface UsersRepository extends CrudRepository<User, Integer> {

	
	
}
