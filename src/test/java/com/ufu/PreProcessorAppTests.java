package com.ufu;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ufu.preprocessor.PreProcessorApp;
import com.ufu.preprocessor.repository.CommentsRepository;
import com.ufu.preprocessor.repository.GenericRepository;
import com.ufu.preprocessor.repository.PostsRepository;
import com.ufu.preprocessor.repository.UsersRepository;
import com.ufu.preprocessor.service.PreProcessorService;
import com.ufu.preprocessor.to.Comment;
import com.ufu.preprocessor.to.Post;
import com.ufu.preprocessor.to.User;
import com.ufu.preprocessor.utils.PreProcessorUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class PreProcessorAppTests {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	protected PostsRepository postsRepository;
	
	@Autowired
	protected CommentsRepository commentsRepository;
	
	@Autowired
	protected UsersRepository usersRepository;
	
	@Autowired
	protected GenericRepository genericRepository;
	
	@Autowired
	protected PreProcessorService preProcessorService;
	
	@Autowired
	private PreProcessorUtils preProcessorUtils;
	
	@Autowired
	private PreProcessorApp preProcessorApp;
	
	
	
	@Before
	public void setUpOnce() throws Exception {
		preProcessorUtils.initializeConfigs();
	}
	
	
	
	

	//@Test
	public void getPost() {
		Integer postId = 12125311;
		Post post = postsRepository.findOne(postId);
		//System.out.println(post);
		assertNotNull(post);

	}
	
	
	//@Test
	public void getComments() {
		logger.info("Testing getComments....");
		Integer postId = 910522;
		List<Comment> comments = commentsRepository.findByPostId(postId, new Sort(Sort.Direction.ASC, "id"));
		for (Comment comment : comments) {
			//logger.info(comment.getText());
		}
		assertNotNull(comments);

	}
		
	
	//@Test
	public void getAnswers() {
		logger.info("Testing getAnswers....");
		Integer postId = 910374;
		List<Post> answers = postsRepository.findByParentId(postId);
		for (Post answer : answers) {
			//logger.info(answer.getBody());
		}
		assertNotNull(answers);

	}
	
	
	//@Test
	public void getUser() {
		logger.info("Testing getUser....");
		Integer userId = 112532;
		User user = usersRepository.findOne(userId);
		//System.out.println(user);
		assertNotNull(user);
	}
		
	
	
	//@Test
	public void testStemStop() throws Exception {
		logger.info("testStemStop....");
		Integer questionsIds[] = {50634254,50660898,50662955,50663165,50663228,50663324,50663433,5328,50663419,45960410,2216698,1152694};
		Integer answersIds[] = {910522,45960571,2900755,1053475};
		
		for(Integer postId:questionsIds) {
			if(postId.equals(5328)) {
		//		System.err.println();
			}
			
			Post post = postsRepository.findOne(postId);
			preProcessorUtils.processTitle(post);
		//	System.out.println("\n\nOriginal title: "+post.getTitle()+"\nProcessed: "+post.getProcessedTitle());
			//posts.add(post);
		}
		
		for(Integer postId:answersIds) {
			Post post = postsRepository.findOne(postId);
			preProcessorUtils.processBody(post);
			System.out.println("\n\nOriginal body id: "+post.getId()+"\nProcessed: "+post.getProcessedBody());
			System.out.println("\n\nOriginal code id: "+post.getId()+"\nProcessed: "+post.getProcessedCode());
			//posts.add(post);
		}
		
		
		
		/*for(Post post:posts) {
			System.out.println(post.getBody());
		}*/
		
	}
		
	
	//@Test
	public void testCodeCapture() {
		Post post = postsRepository.findOne(5328);
		Document doc = Jsoup.parse(post.getBody());
		Elements elems = doc.select("pre");
		String codes = "";
		for(Element element: elems) {
			codes+=element.text()+"\n\n\n";
		}
		
		//System.out.println(codes);
		
	}
	
	
	//@Test
	public void testRemovePunctuationNumbers() {
		//Post post = postsRepository.findOne(50660117);
		String body = "I have some issues www.aaa.com.br with 666 3 3 4... (printing) my code? I want my result, to be aligned neatly but it isn't like that. It would, be very, grateful if someone could help me with the issue! class() mymethod.call(String[] args, Integer class) . kssdfd ";
		System.out.println(body);
		String result = PreProcessorUtils.removeAllPunctuations(body);
		Post p = postsRepository.findOne(2793153);
		preProcessorUtils.processBody(p);
		System.out.println("\n\nCode: "+p.getCode()+"\n then: "+p.getProcessedCode());
		System.out.println(result);
		assertFalse(result.contains(". "));
		assertFalse(result.contains(" ."));
		assertFalse(result.contains("."));
		assertFalse(result.contains(","));
		assertFalse(result.contains("?"));
		assertFalse(result.contains("!"));
		
		String body2 = "Why do I keep getting a NullPointerException in the Java BasicTableUI$Handler.setValueIsAdjusting?";
		System.out.println(body2);
		String result2 = PreProcessorUtils.removeAllPunctuations(body2);
		System.out.println(result2);
		assertFalse(result2.contains("?"));
		assertFalse(result2.contains("."));
		assertFalse(result2.contains("$"));
		
		Post post = postsRepository.findOne(1053475);
		System.out.println(post.getBody());
		result = PreProcessorUtils.removeAllPunctuations(post.getBody());
		System.out.println(result);
		assertFalse(result.contains(". "));
		assertFalse(result.contains(" ."));
		assertFalse(result.contains(","));
		assertFalse(result.contains("?"));
		assertFalse(result.contains("!"));
		
		
		assertTrue(PreProcessorUtils.isNumeric("12"));
		assertTrue(PreProcessorUtils.isNumeric("12.5"));
		assertFalse(PreProcessorUtils.isNumeric("12,5"));
		assertFalse(PreProcessorUtils.isNumeric("1212121212ss"));
		
	}
	
	
	
	
	//@Test
	public void testBodyClean() {
		Post post = postsRepository.findOne(49575082);
		Document doc = Jsoup.parse(post.getBody());
		doc.select("pre").remove();
		//System.out.println(doc.text());
		
		Post post2 = postsRepository.findOne(49575082);
		Document doc2 = Jsoup.parse(post2.getBody());
		doc2.select("a").remove();
		//System.out.println(doc2.text());
		
		
		Post post3 = postsRepository.findOne(50660198);
		String linkContent = PreProcessorUtils.getHTMLTagContent(PreProcessorUtils.BLOCKQUOTE_PATTERN,post3.getBody());
		//System.out.println(linkContent);
		
		preProcessorUtils.processBody(post);
		System.out.println("\n\nBody: "+post.getBody()+"\n then: "+post.getProcessedBody());
		System.out.println("\n\nCode: "+post.getCode()+"\n then: "+post.getProcessedCode());
		
		preProcessorUtils.processBody(post2);
		System.out.println("\n\nBody: "+post2.getBody()+"\n then: "+post2.getProcessedBody());
		System.out.println("\n\nCode: "+post2.getCode()+"\n then: "+post2.getProcessedCode());
		
		preProcessorUtils.processBody(post3);
		System.out.println("\n\nBody: "+post3.getBody()+"\n then: "+post3.getProcessedBody());
		System.out.println("\n\nCode: "+post3.getCode()+"\n then: "+post3.getProcessedCode());
		
		
	}
	
	//@Test
	public void testBodyPhpPython() {
		Post pythonPost1 = postsRepository.findOne(11573992);
		
		
		Document doc = Jsoup.parse(pythonPost1.getBody());
		doc.select("pre").remove();
		//System.out.println(doc.text());
		
		Post phoPost1 = postsRepository.findOne(10057671);
		Document doc2 = Jsoup.parse(phoPost1.getBody());
		doc2.select("a").remove();
		//System.out.println(doc2.text());
		
		
		Post post3 = postsRepository.findOne(50660198);
		String linkContent = PreProcessorUtils.getHTMLTagContent(PreProcessorUtils.BLOCKQUOTE_PATTERN,post3.getBody());
		//System.out.println(linkContent);
		
		PreProcessorUtils.setTag("python");
		preProcessorUtils.processBody(pythonPost1);
		System.out.println("\n\nBody: "+pythonPost1.getBody()+"\n then: "+pythonPost1.getProcessedBody());
		System.out.println("\n\nCode: "+pythonPost1.getCode()+"\n then: "+pythonPost1.getProcessedCode());
		
		PreProcessorUtils.setTag("php");
		preProcessorUtils.processBody(phoPost1);
		System.out.println("\n\nBody: "+phoPost1.getBody()+"\n then: "+phoPost1.getProcessedBody());
		System.out.println("\n\nCode: "+phoPost1.getCode()+"\n then: "+phoPost1.getProcessedCode());
		
		preProcessorUtils.processBody(post3);
		PreProcessorUtils.setTag("java");
		System.out.println("\n\nBody: "+post3.getBody()+"\n then: "+post3.getProcessedBody());
		System.out.println("\n\nCode: "+post3.getCode()+"\n then: "+post3.getProcessedCode());
		
		
	}
	
	//@Test
	public void processProblematicPosts() {
		List<Integer> allJavaPostsIds = new ArrayList<>();
		allJavaPostsIds.add(24558);
		allJavaPostsIds.add(7781461);
		allJavaPostsIds.add(19011);
		PreProcessorUtils.setTag("java");
		
		for (Integer postId : allJavaPostsIds) {
			Post post = preProcessorService.findPostByPk(postId);
					
			String title = post.getTitle();
			String body = post.getBody();
			if(StringUtils.isBlank(body)){  //disconsider these cases
				continue;
			}
			
			if(post.getPostTypeId()==1) { //1- question
				if(StringUtils.isBlank(title)) {
					continue;
				}
				preProcessorUtils.processTitle(post); //disconsider these cases
			}
			
			preProcessorUtils.processBody(post);
			System.out.println("Body: "+post.getBody());
			System.out.println("Processed body: "+post.getProcessedBody());
			System.out.println("Processed body lemma: "+post.getProcessedBodyLemma());
			
			//postsRepository.save(post);
			try {
				/*if(postId.equals(968222)) {
					//post.setBody(PreProcessorUtils.translateHTMLSimbols(post.getBody()));
					//post.setTitle(PreProcessorUtils.translateHTMLSimbols(post.getTitle()));
					System.out.println(post.getTitle());
					System.out.println(post.getProcessedTitle());
					System.out.println(post.getBody());
					System.out.println(post.getProcessedBody());
					System.out.println(post.getCode());
					
					post.setProcessedTitle("s");
					post.setProcessedBody("s");
					post.setBody("s");
					post.setTitle("s");
					//post.setCode("s");
				}*/
				postsRepository.save(post);
			} catch (Exception e) {
				System.out.println("here... post: "+post.getId());
				throw e;
			}
			
					
		}
		
		
		
	}
	
	
	//@Test
	public void processCodeCamelCase() throws IOException {
		Post post = postsRepository.findOne(9352608);
		String beforeBody = post.getBody();
		String beforeCode = post.getCode();
		
		System.out.println("\n\n\n\n\nProcessed code before: "+post.getProcessedCode());
		
		String wordsArr[] = {"camelCase","CamelCase","camel","Camel"};
		List<String> wordsBefore = Arrays.asList(wordsArr);
		List<String> words = PreProcessorUtils.processCamelCases(wordsBefore);
		System.out.println(words);
		PreProcessorUtils.processCode(post);
		
		System.out.println("Processed code after:  "+post.getProcessedCode());
		
		assertTrue(beforeBody.equals(post.getBody()));
		assertTrue(beforeCode.equals(post.getCode()));
		
		
	}
	
	//@Test
	public void processLemma() throws IOException {
		List<Integer> somePosts = new ArrayList();
		somePosts.add(49575082);
		somePosts.add(11227809);
		somePosts.add(50660198);
		
		preProcessorApp.processPosts(somePosts);
	}
	
	//@Test
	public void processCommentsLemma() {
		String query = "select id from commentsmin where postid in ( select id from postsmin where tags like '%java%') order by id desc limit 100 ";
		List<Integer> ids = genericRepository.executeQuery(query);
		preProcessorApp.processComments(ids);
	}	
	

}
