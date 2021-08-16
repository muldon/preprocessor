package com.ufu.preprocessor.repository;

import java.util.List;
import java.util.Map;

import com.ufu.preprocessor.to.Comment;
import com.ufu.preprocessor.to.Post;


public interface GenericRepository {
	
	List<Integer> getQuestionsIdsByFilters(String tagFilter);

	List<Post> getDuplicatedQuestions(String tagFilter);

	List<Post> getPostsByIds(List<Integer> soAnswerIds);

	List<Integer> findAllPostsIdsByTag(String tag);

	List<Comment> getCommentsByIds(List<Integer> someCommentsIds);

	List<Integer> executeQuery(String query);



	
    
}
