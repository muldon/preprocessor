package com.ufu.preprocessor.repository;

import java.util.List;
import java.util.Map;

import com.ufu.preprocessor.to.Posts;


public interface GenericRepository {
	
	List<Integer> getQuestionsIdsByFilters(String tagFilter);

	List<Posts> getDuplicatedQuestions(String tagFilter);



	
    
}
