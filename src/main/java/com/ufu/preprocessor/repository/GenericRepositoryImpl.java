package com.ufu.preprocessor.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ufu.preprocessor.to.Posts;
import com.ufu.preprocessor.utils.PreProcessorUtils;

@Repository
public class GenericRepositoryImpl implements GenericRepository {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;

	

	
	public List<Integer> getQuestionsIdsByFilters(String tagFilter) {
		String query = "select p.id from "
				+ " posts p "
				+ " where p.posttypeid = 1 ";
		
		query += PreProcessorUtils.getQueryComplementByTag(tagFilter);
				
		query+= " order by p.id ";
		
		Query q = em.createNativeQuery(query);
		
		List<Integer> questionsIds = q.getResultList();
		
		return questionsIds;
	}




	@Override
	public List<Posts> getDuplicatedQuestions(String tagFilter) {
		logger.info("getDuplicatedQuestions: "+tagFilter);
		
		String query = " select * from posts  p " + 
				" WHERE p.posttypeId = 1 ";
		
		query += PreProcessorUtils.getQueryComplementByTag(tagFilter);
		
		query+=	" and p.id in " + 
				" ( select distinct(pl.postid)" + 
				"   from postlinks pl where pl.linktypeid = 3" + 
				 " union " + 
				"  select distinct(pl.relatedpostid)" + 
				"  from postlinks pl where pl.linktypeid = 3 )";
		
		
		Query q = em.createNativeQuery(query, Posts.class);
		
		List<Posts> posts = q.getResultList();
				
		logger.info("Posts em getQuestionsByFilters: "+posts.size());
		
		return posts;
	}

	
	
	
}
