package com.ufu.preprocessor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ufu.preprocessor.service.PreProcessorService;
import com.ufu.preprocessor.to.Posts;
import com.ufu.preprocessor.utils.PreProcessorUtils;


@Component
public class PreProcessorApp {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static List<Integer> allPostsQuestionsIds;
	private static SimpleDateFormat dateFormat;
	private List<Posts> duplicatedQuestions;
	
	
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
	
	@Value("${spring.datasource.url}")
	public String base;
	
	
	
	@Autowired
	private PreProcessorUtils dupPredictorUtils;
	
	private long initTime;
	
		
	@Autowired
	private PreProcessorService preProcessService;
	
	

	@PostConstruct
	public void init() throws Exception {
		logger.info("PreProcess for stemming and stop words");
		dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		dupPredictorUtils.initializeConfigs();
		
		logger.info("Variables:"
				+"\n performStemmingStopWords="+performStemmingStopWords
				+"\n removeSpecialChars="+removeSpecialChars
				+"\n appendDuplicateToTitles="+appendDuplicateToTitles
				+"\n maxQuestions="+maxQuestions
				+"\n base="+base
				+ "\n tagFilter: "+tagFilter);
				
		
		initTime = System.currentTimeMillis();
		logger.info("PreProcessing...");
		
		if(performStemmingStopWords) {
			allPostsQuestionsIds  = preProcessService.getQuestionsIdsByFilters(tagFilter);
			logger.info("Number of Questions to perform stemming and remove stop words: "+allPostsQuestionsIds.size());
			performStemStop();
		}
		if(appendDuplicateToTitles) {
			duplicatedQuestions = preProcessService.getDuplicatedQuestions(tagFilter);
			logger.info("Number of duplicated questions to add 'Duplicate' to their titles: "+duplicatedQuestions.size());
			preProcessService.appendDuplicateToTitles(duplicatedQuestions);
			
		}
		/*if(tagsSynCodeGen) {
			if(allPostsQuestionsIds==null) {
				allPostsQuestionsIds  = preProcessService.getQuestionsIdsByFilters(tagFilter);
				logger.info("Number of Questions to add synonyms for tags: "+allPostsQuestionsIds.size());
			}
			performTagSynCodeGen();
		}*/
		
		logger.info("End of PreProcessing...");
		dupPredictorUtils.reportElapsedTime(initTime,"preProcess");
		
		

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
			preProcessService.stemStop(somePostsQuestionsIds);
			remainingSize = remainingSize - maxQuestions;
			init+=maxQuestions;
			end+=maxQuestions;
		}
		somePostsQuestionsIds = allPostsQuestionsIds.subList(init, init+remainingSize);
		//remaining
		preProcessService.stemStop(somePostsQuestionsIds);
				
		
	}




	
	

}
