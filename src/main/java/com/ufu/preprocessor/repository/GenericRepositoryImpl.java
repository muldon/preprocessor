package com.ufu.preprocessor.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ufu.preprocessor.to.Comment;
import com.ufu.preprocessor.to.Post;
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
	public List<Post> getDuplicatedQuestions(String tagFilter) {
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
		
		
		Query q = em.createNativeQuery(query, Post.class);
		
		List<Post> post = q.getResultList();
				
		logger.info("Post em getQuestionsByFilters: "+post.size());
		
		return post;
	}

	

	@Override
	public List<Post> getPostsByIds(List<Integer> soAnswerIds) {
		String idsIn = " ";
		for(Integer soId: soAnswerIds) {
			idsIn+= soId+ ",";
		}
		idsIn+= "#end";
		idsIn = idsIn.replace(",#end", "");
		
		String sql = " select * "
				+ " from postsmin po"  
				+ " where po.id in ("+idsIn+")";
		
			
		Query q = em.createNativeQuery(sql, Post.class);
		List<Post> posts = (List<Post>) q.getResultList();
		//logger.info("getPostsByIds: "+posts.size());
		return posts;
	}




	@Override
	public List<Integer> findAllPostsIdsByTag(String tag) {
		String sqlQuestions = "select p.id from postsmin p where p.tags like '%"+tag+"%' "; 
		Query q = em.createNativeQuery(sqlQuestions);
		Set<Integer> postsIdsQuestions = new HashSet(q.getResultList());
				
		logger.info("\n\n\nfindAllPostsIdsByTag for tag: "+tag+" - "+postsIdsQuestions.size());
		
		
		String sqlAnswers = "select p.id from postsmin p, postsmin p2 where p.parentid = p2.id and p2.tags like '%"+tag+"%'";  
		q = em.createNativeQuery(sqlAnswers);
		Set<Integer> postsIdsAnswers = new HashSet(q.getResultList());
				
		logger.info("\nfind Answers for questions of tag: "+tag+" - "+postsIdsAnswers.size());
		
		List<Integer> allIds = new ArrayList(postsIdsQuestions);
		allIds.addAll(postsIdsAnswers);
		logger.info("\nTotal of ids: "+allIds.size());
		
		return allIds;
	}




	@Override
	public List<Comment> getCommentsByIds(List<Integer> someCommentsIds) {
		String idsIn = " ";
		for(Integer soId: someCommentsIds) {
			idsIn+= soId+ ",";
		}
		idsIn+= "#end";
		idsIn = idsIn.replace(",#end", "");
		
		String sql = " select * "
				+ " from commentsmin c"  
				+ " where c.id in ("+idsIn+")";
		
			
		Query q = em.createNativeQuery(sql, Comment.class);
		List<Comment> comments = (List<Comment>) q.getResultList();
		logger.info("getCommentsByIds: "+comments.size());
		return comments;
	}


	@Override
	public List<Integer> executeQuery(String query) {
		Query q = em.createNativeQuery(query);
		List<Integer> ids = (List<Integer>) q.getResultList();
		logger.info("ids: "+ids.size());
		return ids;
	}


	
}
