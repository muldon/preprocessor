package com.ufu.preprocessor.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ufu.preprocessor.repository.CommentsRepository;
import com.ufu.preprocessor.repository.GenericRepository;
import com.ufu.preprocessor.repository.PostsRepository;
import com.ufu.preprocessor.to.Comment;
import com.ufu.preprocessor.to.Post;
import com.ufu.preprocessor.utils.PreProcessorUtils;


@Service
@Transactional
public class PreProcessorService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@Autowired
	protected PostsRepository postsRepository;
	
	@Autowired
	protected CommentsRepository commentsRepository;
	
	@Autowired
	protected GenericRepository genericRepository;
	
	@Autowired
	private PreProcessorUtils utils;
	
	
	@Value("${removeSpecialChars}")
	public Boolean removeSpecialChars; 
	
	
	
	@Transactional(readOnly = true)
	public List<Integer> getQuestionsIdsByFilters(String tagFilter) {
		return genericRepository.getQuestionsIdsByFilters(tagFilter);
	}

	
	
	public void stemStopOld(List<Integer> someIds) throws Exception {
		for (Integer questionId : someIds) {
			Post question = postsRepository.findOne(questionId);
						
			String title = question.getTitle();
			String body = question.getBody();
			if(StringUtils.isBlank(title) || StringUtils.isBlank(body)){  //disconsider these cases
				continue;
			}
				
			String tags = question.getTags().replaceAll("<","");
			tags = tags.replaceAll(">"," ");
						
			
			if(removeSpecialChars){
				String[] titleContent = utils.separaSomentePalavrasNaoSomentePalavras(title,"title");
				question.setTitle(titleContent[0] + " "+ titleContent[1]);
				
				String[] bodyContent = utils.separaSomentePalavrasNaoSomentePalavras(body,"body");
				question.setBody(bodyContent[0] + " "+ bodyContent[1]);
				if (!StringUtils.isBlank(bodyContent[2])) {
					question.setCode(bodyContent[2]);
				}
								
				tags = utils.tagMastering(tags);												
				//question.setTagsSyn(tags);		
			
			}else{
				question.setTitle(utils.tokenizeStopStem(title));
				question.setBody(utils.tokenizeStopStem(body));
				
			}
			question.setTags(tags);														
			postsRepository.save(question);
					
		}
		
	}
	

	public void tagSynCodeGen(List<Integer> someIds) throws Exception {
		for (Integer questionId : someIds) {
			Post question = postsRepository.findOne(questionId);
			String title = question.getTitle();
			String body = question.getBody();
			if(StringUtils.isBlank(title) || StringUtils.isBlank(body)){  //disconsider these cases
				continue;
			}
			
			String tags = question.getTags();
			tags = utils.tagMastering(tags);												
			//question.setTagsSyn(tags);		
			
			String[] bodyContent = utils.separaSomentePalavrasNaoSomentePalavras(body,"body");
			if (!StringUtils.isBlank(bodyContent[2])) {
				question.setCode(bodyContent[2]);
			}
			
			postsRepository.save(question);
		}		
	}



	public List<Post> getDuplicatedQuestions(String tagFilter) {
		return genericRepository.getDuplicatedQuestions(tagFilter);
	}



	public void appendDuplicateToTitles(List<Post> duplicatedQuestions) {
		int count = 0;
		for(Post question:duplicatedQuestions) {
			if(count%50000==0) {
				logger.info("appending duplicate to title of question "+count+ " of "+duplicatedQuestions.size());
			}
			count++;
			question.setTitle(question.getTitle()+ " Duplicate ");
			postsRepository.save(question);
		}
		
	}

	@Transactional(readOnly = true)
	public List<Integer> findAllPostsIds() {
		return postsRepository.findAllPostsIds();
	}

	

	@Transactional(readOnly = true)
	public List<Post> findSomePosts() {
		return postsRepository.findSomePosts();
	}


	@Transactional(readOnly = true)
	public Post findPostByPk(Integer postId) {
		return postsRepository.findOne(postId);
	}



	public void savePost(Post post) {
		postsRepository.save(post);
		
	}


	@Transactional(readOnly = true)
	public List<Post> findPostsById(List<Integer> somePosts) {
		return genericRepository.getPostsByIds(somePosts);
	}


	@Transactional(readOnly = true)
	public List<Integer> findAllPostsIdsByTag(String tag) {
		return genericRepository.findAllPostsIdsByTag(tag);
	}


	@Transactional(readOnly = true)
	public List<Integer> findAllCommentsIds() {
		return commentsRepository.findAllCommentsIds();
	}


	@Transactional(readOnly = true)
	public List<Comment> findCommentsById(List<Integer> someCommentsIds) {
		return genericRepository.getCommentsByIds(someCommentsIds);
	}
	
	
	
	
}
