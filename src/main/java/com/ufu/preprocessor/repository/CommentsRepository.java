package com.ufu.preprocessor.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.ufu.preprocessor.to.Comment;



public interface CommentsRepository extends CrudRepository<Comment, Integer> {

	List<Comment> findByPostId(Integer postId, Sort sort);

	@Query(value="select c.id " 
			+ " from commentsmin c",nativeQuery=true)
	List<Integer> findAllCommentsIds();
    
	

	
}
