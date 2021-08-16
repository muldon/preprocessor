package com.ufu.preprocessor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.ufu.preprocessor.to.Post;



public interface PostsRepository extends CrudRepository<Post, Integer> {

	List<Post> findByParentId(Integer postId);

	
	@Query(value="select * " 
			+ " from postsmin po"  
			+ " where po.body like '%<pre><code>%' "
			+ " order by id desc"
			+ " limit 100",nativeQuery=true)
	List<Post> findSomePosts();

	@Query(value="select po.id " 
			+ " from postsmin po"
			+ " order by id",nativeQuery=true)
	List<Integer> findAllPostsIds();
    
	

	
}
