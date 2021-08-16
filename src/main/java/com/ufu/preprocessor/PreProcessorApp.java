package com.ufu.preprocessor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ufu.preprocessor.repository.CommentsRepository;
import com.ufu.preprocessor.repository.PostsRepository;
import com.ufu.preprocessor.service.PreProcessorService;
import com.ufu.preprocessor.to.Comment;
import com.ufu.preprocessor.to.Post;
import com.ufu.preprocessor.utils.PreProcessorUtils;


@Component
public class PreProcessorApp {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static List<Integer> allPostsQuestionsIds;
	private static SimpleDateFormat dateFormat;
	private List<Post> duplicatedQuestions;
	
	
	@Value("${performStemmingStopWords}")
	public Boolean performStemmingStopWords; 
	
	@Value("${removeSpecialChars}")
	public Boolean removeSpecialChars; 
		
	
	@Value("${appendDuplicateToTitles}")
	public Boolean appendDuplicateToTitles; 
	
	@Value("${tagsSynCodeGen}")
	public Boolean tagsSynCodeGen; 
	
	
	@Value("${maxQuestions}")
	public Integer maxQuestions;
	
	@Value("${tagFilter}")
	public String tagFilter;  //null for all
	
	@Value("${processLemmas}")
	public Boolean processLemmas;  
	
	
	
	@Value("${spring.datasource.url}")
	public String base;
	
	@Value("${action}")
	public String action;  
	
	@Autowired
	private PreProcessorUtils preProcessorUtils;
	
	private long initTime;
	
		
	@Autowired
	private PreProcessorService preProcessService;
	
	@Autowired
	private PostsRepository postsRepository;  //memory issues with spring transactions when dealing with 3.8M posts
	
	@Autowired
	private CommentsRepository commentsRepository;  //memory issues with spring transactions when dealing with 3.8M posts

	@PostConstruct
	public void init() throws Exception {
		logger.info("Initializing PreProcessor App...");
		dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		preProcessorUtils.initializeConfigs();
		
		logger.info("Variables:"
				+"\n action="+action
				+"\n maxQuestions="+maxQuestions
				+"\n base="+base
				+"\n processLemmas="+processLemmas
				+ "\n tagFilter: "+tagFilter);
				
		
		initTime = System.currentTimeMillis();
		logger.info("initializing...");
		
		switch (action) {
		
		case "preprocessPosts":
			preprocessPosts();
			break;
			
		case "preprocessComments":
			preprocessComments();
			break;	
			
			
		case "preprocesscode":
			preprocessCode();
			break;		
		
		case "performStemmingStopWords":
			performStemmingStopWords();
			break;
			
		case "appendDuplicateToTitles":
			appendDuplicateToTitles();
			break;
		
		default:
			logger.info("End...");
			preProcessorUtils.reportElapsedTime(initTime,"init");
			break;
		}	
			
		
	
		

	}




	private void preprocessCode() throws IOException {
		//load all posts
		
		String tags[] = {"java","php","python"};
		
		for(String tag:tags) {
			List<Integer> allPostsIdsByTag = preProcessService.findAllPostsIdsByTag(tag);
			PreProcessorUtils.setTag(tag);
			List<Integer> somePostsIds=null; 
			int maxQuestions = 1000;
			
			int init=0;
			int end=maxQuestions;
			int remainingSize = allPostsIdsByTag.size();
			logger.info("all questions ids:  "+remainingSize);
			while(remainingSize>maxQuestions){ //this is for memory issues
				somePostsIds = allPostsIdsByTag.subList(init, end);
				//preProcessService.processPosts(somePostsIds);
				processPostsCodes(somePostsIds);
				remainingSize = remainingSize - maxQuestions;
				init+=maxQuestions;
				end+=maxQuestions;
				somePostsIds=null;
				if(init%100000==0) {
					logger.info("Processed "+init);
				}
				
			}
			somePostsIds = allPostsIdsByTag.subList(init, init+remainingSize);
			//remaining
			//preProcessService.processPosts(somePostsIds);
			processPostsCodes(somePostsIds);
			
		}	
		
	}






	private void appendDuplicateToTitles() {
		duplicatedQuestions = preProcessService.getDuplicatedQuestions(tagFilter);
		logger.info("Number of duplicated questions to add 'Duplicate' to their titles: "+duplicatedQuestions.size());
		preProcessService.appendDuplicateToTitles(duplicatedQuestions);
	}




	private void performStemmingStopWords() throws Exception {
		allPostsQuestionsIds  = preProcessService.getQuestionsIdsByFilters(tagFilter);
		logger.info("Number of Questions to perform stemming and remove stop words: "+allPostsQuestionsIds.size());
		performStemStop();
		
	}




	public void performTagSynCodeGen() throws Exception {
		List<Integer> somePostsQuestionsIds = new ArrayList<>(); 
		
		int init=0;
		int end=maxQuestions;
		int remainingSize = allPostsQuestionsIds.size();
		logger.info("all questions ids:  "+remainingSize);
		while(remainingSize>maxQuestions){ //this is for memory issues
			somePostsQuestionsIds = allPostsQuestionsIds.subList(init, end);
			preProcessService.tagSynCodeGen(somePostsQuestionsIds);
			remainingSize = remainingSize - maxQuestions;
			init+=maxQuestions;
			end+=maxQuestions;
		}
		somePostsQuestionsIds = allPostsQuestionsIds.subList(init, init+remainingSize);
		//remaining
		preProcessService.tagSynCodeGen(somePostsQuestionsIds);
		
	}





	private void performStemStop() throws Exception {
		List<Integer> somePostsQuestionsIds = new ArrayList<>(); 
		
		int init=0;
		int end=maxQuestions;
		int remainingSize = allPostsQuestionsIds.size();
		logger.info("all questions ids:  "+remainingSize);
		while(remainingSize>maxQuestions){ //this is for memory issues
			somePostsQuestionsIds = allPostsQuestionsIds.subList(init, end);
			preProcessService.stemStopOld(somePostsQuestionsIds);
			remainingSize = remainingSize - maxQuestions;
			init+=maxQuestions;
			end+=maxQuestions;
		}
		somePostsQuestionsIds = allPostsQuestionsIds.subList(init, init+remainingSize);
		//remaining
		preProcessService.stemStopOld(somePostsQuestionsIds);
				
		
	}

	

	private void preprocessComments() {
		List<Integer> allCommentsIds = preProcessService.findAllCommentsIds();
		List<Integer> someCommentsIds=null; 
		int max = 1000;
	
		int init=0;
		int end=max;
		int remainingSize = allCommentsIds.size();
		logger.info("all comments ids:  "+remainingSize);
		while(remainingSize>max){ //this is for memory issues
			someCommentsIds = allCommentsIds.subList(init, end);
			processComments(someCommentsIds);
			remainingSize = remainingSize - max;
			init+=max;
			end+=max;
			someCommentsIds=null;
			if(init%100000==0) {
				logger.info("Processed "+init);
			}
			
		}
		someCommentsIds = allCommentsIds.subList(init, init+remainingSize);
		//remaining
		//preProcessService.processPosts(somePostsIds);
		processComments(someCommentsIds);
		
	}

	
	private void preprocessPosts() throws IOException {
		String tags[] = {"java","php","python"};
		
		for(String tag:tags) {
			List<Integer> allPostsIdsByTag = preProcessService.findAllPostsIdsByTag(tag);
			PreProcessorUtils.setTag(tag);
			List<Integer> somePostsIds=null; 
			int maxQuestions = 1000;
		
			int init=0;
			int end=maxQuestions;
			int remainingSize = allPostsIdsByTag.size();
			logger.info("all questions ids:  "+remainingSize);
			while(remainingSize>maxQuestions){ //this is for memory issues
				somePostsIds = allPostsIdsByTag.subList(init, end);
				//preProcessService.processPosts(somePostsIds);
				processPosts(somePostsIds);
				remainingSize = remainingSize - maxQuestions;
				init+=maxQuestions;
				end+=maxQuestions;
				somePostsIds=null;
				if(init%100000==0) {
					logger.info("Tag: "+tag+" - Processed "+init);
				}
				
			}
			somePostsIds = allPostsIdsByTag.subList(init, init+remainingSize);
			//remaining
			//preProcessService.processPosts(somePostsIds);
			processPosts(somePostsIds);
		}
	}
	

	public void processComments(List<Integer> someCommentsIds) {
		List<Comment> some = preProcessService.findCommentsById(someCommentsIds);
		for (Comment comment : some) {
					
			String text = comment.getText();
			if(StringUtils.isBlank(text)){  //disconsider these cases
				continue;
			}
			
			preProcessorUtils.processComment(comment);
			try {
				commentsRepository.save(comment);  //no transaction -- memory issues
			} catch (Exception e) {
				System.out.println("here... comment: "+comment.getId());
				throw e;
			}
			
					
		}
		
	}

	public void processPosts(List<Integer> somePosts) {
		
		List<Post> some = preProcessService.findPostsById(somePosts);
		for (Post post : some) {
					
			String title = post.getTitle();
			String body = post.getBody();
			if(StringUtils.isBlank(body)){  //disconsider these cases
				continue;
			}
			
			if(post.getPostTypeId()==1) { //1- question
				if(StringUtils.isBlank(title)) { //disconsider these cases
					continue;
				}
				preProcessorUtils.processTitle(post); 
			}
			
			preProcessorUtils.processBody(post);
			try {
				postsRepository.save(post);  //no transaction -- memory issues
			} catch (Exception e) {
				System.out.println("here... post: "+post.getId());
				throw e;
			}
			
					
		}
	}

	
	

	public void processPostsCodes(List<Integer> somePosts) {
		
		List<Post> some = preProcessService.findPostsById(somePosts);
		for (Post post : some) {
					
			String title = post.getTitle();
			String body = post.getBody();
			if(StringUtils.isBlank(body)){  //disconsider these cases
				continue;
			}
			
			if(post.getPostTypeId()==1) { //1- question
				if(StringUtils.isBlank(title)) { //disconsider these cases
					continue;
				}
			}
			
			PreProcessorUtils.processCode(post);
			try {
				postsRepository.save(post);  //no transaction -- memory issues
			} catch (Exception e) {
				System.out.println("Error here... post: "+post.getId());
				throw e;
			}
					
		}
	}

	

}
