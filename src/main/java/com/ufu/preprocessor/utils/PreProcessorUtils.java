package com.ufu.preprocessor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ufu.preprocessor.repository.GenericRepository;
import com.ufu.preprocessor.to.Comment;
import com.ufu.preprocessor.to.Post;

import edu.stanford.nlp.simple.Sentence;




@Component
public class PreProcessorUtils {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static Map<String, String> sourceToMaster;
	private static CharArraySet stopWords;
	private AttributeFactory factory;
	private static StandardTokenizer standardTokenizer;
	private Boolean configsInitialized = false;
	private static long endTime;
	private static List<String> stopWordsList;
	private static String tag;
	
	@Autowired
	protected GenericRepository genericRepository;
	
	@Value("${STOP_WORDS_FILE_PATH}")
	public String STOP_WORDS_FILE_PATH;
	
	@Value("${processLemmas}")
	public Boolean processLemmas;  
	
	public static final String USER_MENTION_REGEX_EXPRESSION = "@([A-Za-z0-9_.]+)";
	public static final Pattern USER_MENTION_PATTERN = Pattern.compile(USER_MENTION_REGEX_EXPRESSION, Pattern.DOTALL); 
	
	
	public static final String CODE_REGEX_EXPRESSION = "(?sm)<code>(.*?)</code>";
	public static final Pattern CODE_PATTERN = Pattern.compile(CODE_REGEX_EXPRESSION, Pattern.DOTALL); 
	
	public static final String BLOCKQUOTE_EXPRESSION = "(?sm)<blockquote>(.*?)</blockquote>";
	public static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile(BLOCKQUOTE_EXPRESSION, Pattern.DOTALL);
	
	
	//^(https?|http|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]
	public static final String URL_EXPRESSION_OUT = "\\b((https?|ftp):\\/\\/)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[A-Za-z]{2,6}\\b(\\/[-a-zA-Z0-9@:%_\\+.~#?&//=]*)*(?:\\/|\\b)";
	
	
	//public static final String LINK_EXPRESSION_OUT = "(?sm)<a href=(.*?) rel=\"nofollow\">";
	public static final String LINK_EXPRESSION_OUT = "(?sm)<a href=(.*?)</a>";
	public static final Pattern LINK_PATTERN = Pattern.compile(LINK_EXPRESSION_OUT, Pattern.DOTALL);
	
	public static final String ONLY_WORDS_EXPRESSION = "(?<!\\S)\\p{Alpha}+(?!\\S)";
	public static final Pattern ONLY_WORDS_PATTERN = Pattern.compile(ONLY_WORDS_EXPRESSION, Pattern.DOTALL);
	
	public static final String NOT_ONLY_WORDS_EXPRESSION = "(?<!\\S)(?!\\p{Alpha}+(?!\\S))\\S+";
	public static final Pattern NOT_ONLY_WORDS_PATTERN = Pattern.compile(NOT_ONLY_WORDS_EXPRESSION, Pattern.DOTALL);
	
	private static String htmlTags[] = {"<p>","</p>","<pre>","</pre>","<blockquote>","</blockquote>", /*"<a href=\"","\">",*/
			"</a>","<img src=","alt=","<ol>","</ol>","<li>","</li>","<ul>",
			"</ul>","<br>","</br>","<h1>","</h1>","<h2>","</h2>","<strong>","</strong>",
			"<code>","</code>","<em>","</em>","<hr>"};
	
	public static final String javaKeywords[] = { "abstract", "assert", "boolean",
            "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "extends", "false",
            "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native",
            "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "true",
            "try", "void", "volatile", "while" };
	
	public static final String phpKeywords[] = { "__halt_compiler", "abstract", 
			"and", "array", "as", "break", "callable", "case", "catch", "class", 
			"clone", "const", "continue", "declare", "default", "die", "do", "echo", 
			"else", "elseif", "empty", "enddeclare", "endfor", "endforeach", "endif", 
			"endswitch", "endwhile", "eval", "exit", "extends", "final", "for", "foreach",
			"function", "global", "goto", "if", "implements", "include", "include_once",
			"instanceof", "insteadof", "interface", "isset", "list", "namespace", "new", 
			"or", "print", "private", "protected", "public", "require", "require_once",
			"return", "static", "switch", "throw", "trait", "try", "unset", "use", "var",
			"while", "xor" };
	
	public static final String pythonKeywords[] = { "False", "None", "True", "and", 
			"as", "assert", "break", "class", "continue", "def", "del", "elif", "else",
			"except", "finally", "for", "from", "global", "if", "import", "in", "is", 
			"lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", 
			"while", "with", "yield" };
	
	
	public void initializeConfigs() throws Exception {
		if(!configsInitialized){
			configsInitialized = true;
				
			factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
			standardTokenizer = new StandardTokenizer(factory);
			stopWords = EnglishAnalyzer.getDefaultStopSet();
			
			standardTokenizer.close();
			loadTagSynonyms();
			
			stopWordsList = Files.readAllLines(Paths.get(STOP_WORDS_FILE_PATH.trim()));
		}
				
	}
	
	
	
	public String tokenizeStopStem(String input) throws Exception {
		if (StringUtils.isBlank(input)) {
			return "";
		}
		standardTokenizer.setReader(new StringReader(input));
		TokenStream stream = new StopFilter(new LowerCaseFilter(new PorterStemFilter(standardTokenizer)), stopWords);

		CharTermAttribute charTermAttribute = standardTokenizer.addAttribute(CharTermAttribute.class);
		stream.reset();

		StringBuilder sb = new StringBuilder();
		while (stream.incrementToken()) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(charTermAttribute.toString());
		}

		stream.end();
		stream.close();
		
		
		return sb.toString();
	}
	
	
	
	public String tokenizeStop(String input) throws Exception {
		if (StringUtils.isBlank(input)) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		
		
		
		
		return sb.toString();
	}
	
	
	
	public void reportElapsedTime(long initTime, String processName) {
		
		endTime = System.currentTimeMillis();
		String duration = DurationFormatUtils.formatDuration(endTime-initTime, "HH:mm:ss,SSS");
		logger.info("Elapsed time: "+duration+ " of the execution of  "+processName);
		
	}
	
	
	
	
	
	
	public static List<String> getCodeValues(Pattern patter,String str) {
	    final List<String> tagValues = new ArrayList<String>();
	    final Matcher matcher = patter.matcher(str);
	    while (matcher.find()) {
	        tagValues.add(matcher.group(1));
	    }
	    return tagValues;
	}
	

	
	public String[] separaSomentePalavrasNaoSomentePalavras(String content, String parte) throws Exception {
		String[] finalContent = new String[4];
		
		String lower = content.toLowerCase()+ " ";
			
		//volta simbolos de marcacao HTML para estado original 
		String simbolosOriginais = translateHTMLSimbols(lower);		
		
		String textoSemCodigosLinksEBlackquotes = simbolosOriginais.replaceAll(LINK_EXPRESSION_OUT, " ");
		
		//blockquotes
		String blockquoteContent = "";
		List<String> blockquotes = getCodeValues(BLOCKQUOTE_PATTERN, textoSemCodigosLinksEBlackquotes);
		for(String blockquote: blockquotes){
			blockquoteContent+= blockquote+ " ";
		}
		textoSemCodigosLinksEBlackquotes = textoSemCodigosLinksEBlackquotes.replaceAll(BLOCKQUOTE_EXPRESSION, " ");
		
		//codes
		String codeContent = "";
		List<String> codes = getCodeValues(CODE_PATTERN, textoSemCodigosLinksEBlackquotes);
		for(String code: codes){
			codeContent+= code+ " ";
		}
		
		codeContent = codeContent.replaceAll("\n", " ");
		codeContent = retiraHtmlTags(codeContent);
					
		
		//String codeToBeJoinedInBody = retiraSimbolosCodeParaBody(codeContent);
				
		//String bloquesAndLinks = blockquoteContent+ " "+linksContent;
		String bloquesAndLinks = blockquoteContent;
		bloquesAndLinks = bloquesAndLinks.replaceAll("\n", " ");
		bloquesAndLinks = retiraHtmlTags(bloquesAndLinks);
		bloquesAndLinks += " "+codeContent;
		
		
		textoSemCodigosLinksEBlackquotes = textoSemCodigosLinksEBlackquotes.replaceAll(CODE_REGEX_EXPRESSION, " ");
		textoSemCodigosLinksEBlackquotes = retiraHtmlTags(textoSemCodigosLinksEBlackquotes);
		
		//trata antes de pegar somenta as palavras
		
		String separated = retiraSimbolosImportantesNaoCodigo(textoSemCodigosLinksEBlackquotes,parte);
		
		
		String somentePalavras = getOnlyWords(separated);
		/*List<String> palavras = getWords(ONLY_WORDS_PATTERN, separated);
		for(String word: palavras){
			somentePalavras+= word+ " ";
		}*/
		
		
		String naoSomentePalavras = "";
		List<String> naoPalavras = getWords(NOT_ONLY_WORDS_PATTERN, separated);
		for(String word: naoPalavras){
			naoSomentePalavras+= word+ " ";
		}
		naoSomentePalavras+= bloquesAndLinks;
		
		naoSomentePalavras = retiraSimbolosEspeciais(naoSomentePalavras);
		
		String stoppedStemmed = tokenizeStopStem(somentePalavras.trim());
		
		finalContent[0] = stoppedStemmed; //+ " "+somentePalavrasCodeClean;
		finalContent[1] = naoSomentePalavras.trim();
		finalContent[2] = codeContent.trim();
		
		return finalContent;
		

	}
	
	

	public static String getOnlyWords(String separated) {
		String somentePalavras="";
		List<String> palavras = getWords(ONLY_WORDS_PATTERN, separated);
		for(String word: palavras){
			somentePalavras+= word+ " ";
		}
		return somentePalavras;
		
	}



	public String retiraSimbolosImportantesNaoCodigo(String finalContent, String parte) {
		if(parte.equals("title")){
			finalContent = finalContent.replaceAll("\\?", " ");
		}else {
			finalContent = finalContent.replaceAll("\\:", " ");
		}
		return finalContent;
	}
	
	
	
	/**
	 * Retira tags de marcação do body ou title  
	 */
	public static String retiraHtmlTags(String content) {
		if(content==null || content.trim().equals("")){
			return "";
		}
		//boolean startedHtml = false;
		for(String tag: htmlTags){
			content= content.replaceAll(tag," ");
		}
		//content = content.replaceAll("<a href=", " ");
		//content = content.replaceAll("</a>", " ");

		return content;
		
	}
	
	
	
	public static List<String> getWords(Pattern patter,String str) {
	    final List<String> tagValues = new ArrayList<String>();
	    final Matcher matcher = patter.matcher(str);
	    while (matcher.find()) {
	        tagValues.add(matcher.group(0));
	    }
	    return tagValues;
	}
	
	
	
	
	
	public static String retiraSimbolosEspeciais(String finalContent) {
		
		//nao precisam de espaco
		finalContent = finalContent.replaceAll("\\+"," ");
		finalContent = finalContent.replaceAll("\\^", " ");
		finalContent = finalContent.replaceAll(":", " ");
		finalContent = finalContent.replaceAll(";", " ");
		finalContent = finalContent.replaceAll("-", " ");
		finalContent = finalContent.replaceAll("\\+", " ");
		finalContent = finalContent.replaceAll("&", " ");
		finalContent = finalContent.replaceAll("\\*", " ");
		finalContent = finalContent.replaceAll("\\~", " ");
		finalContent = finalContent.replaceAll("\\\\", " ");
		finalContent = finalContent.replaceAll("/", " ");
		//finalContent = finalContent.replaceAll("\\'", "simbaspassimpl");
		finalContent = finalContent.replaceAll("\\`", " ");
		finalContent = finalContent.replaceAll("\"", " ");
		finalContent = finalContent.replaceAll("\\(", " ");
		finalContent = finalContent.replaceAll("\\)", " ");
		finalContent = finalContent.replaceAll("\\[", " ");
		finalContent = finalContent.replaceAll("\\]", " ");
		finalContent = finalContent.replaceAll("\\{", " ");
		finalContent = finalContent.replaceAll("\\}", " ");
		finalContent = finalContent.replaceAll("\\?", " ");
		finalContent = finalContent.replaceAll("\\|", " ");
		finalContent = finalContent.replaceAll("\\%", " ");
		finalContent = finalContent.replaceAll("\\$", " ");
		finalContent = finalContent.replaceAll("\\@", " ");
		finalContent = finalContent.replaceAll("\\<", " ");
		finalContent = finalContent.replaceAll("\\>", " ");
		finalContent = finalContent.replaceAll("\\#", " ");
		finalContent = finalContent.replaceAll("\\=", " ");
		finalContent = finalContent.replaceAll("\\.", " ");
		finalContent = finalContent.replaceAll("\\,", " ");
		finalContent = finalContent.replaceAll("\\_", " ");
		finalContent = finalContent.replaceAll("\\!", " ");
		finalContent = finalContent.replaceAll("\\'", " ");		
		
		finalContent = finalContent.replaceAll("\n", " ");
		
		return finalContent;
	}
	
	
	




	public static String getQueryComplementByTag(String tagFilter) {
		String query="";
		if(tagFilter!=null && !"".equals(tagFilter)) {
			if(tagFilter.equals("java")) {
				query += " and tags like '%java%' and tags not like '%javascript%' "; 
			}else {
				query += " and tags like '%"+tagFilter+"%'";
			}
		}
		return query;
		
	}

	
	public static String tagMastering(String tags) throws Exception {
		if (tags == null) {
			return "";
		}
		StrTokenizer tokenizer = new StrTokenizer(tags);
		Set<String> tagsSet = new HashSet<>();
		for (String token : tokenizer.getTokenArray()) {

			String master = sourceToMaster.get(token);
			if (master == null) {
				master = token;
			}
			tagsSet.add(master);
		}
		String str = StringUtils.join(tagsSet, " ");
		tokenizer = null;
		tagsSet = null;
		 				
		return str;
	}


	public void loadTagSynonyms() throws Exception {
		sourceToMaster = new HashMap<>();

		String csvFile = "/tagSynonyms.csv";
		String line = "";
		String cvsSplitBy = ",";

		InputStream in = getClass().getResourceAsStream(csvFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		try (BufferedReader br = reader) {
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] tags = line.split(cvsSplitBy);
				sourceToMaster.put(tags[0], tags[1]);
			}
		} catch (IOException e) {
			throw e;
		}
	}
		
	
	public static String translateHTMLSimbols(String finalContent) {
		finalContent = finalContent.replaceAll("&amp;","&");
		finalContent = finalContent.replaceAll("&lt;", "<");
		finalContent = finalContent.replaceAll("&gt;", ">");
		finalContent = finalContent.replaceAll("&quot;", "\"");
		finalContent = finalContent.replaceAll("&apos;", "\'"); 
		
		return finalContent;
	}

	
	
	
	
	public static String getHTMLTagContent(Pattern pattern,String body) {
		String extractedContent = "";
		List<String> contents = getCodeValues(pattern, body);
		for(String blockquote: contents){
			extractedContent+= blockquote+ "\n\n";
		}
		return extractedContent;
	}


	public static List<String> processCamelCases(List<String> words) {
		List<String> newWords = new ArrayList<>();
		for(String word:words) {
			String[] parts=null;
			parts = word.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
			if(parts.length>1) {
				newWords.addAll(Arrays.asList(parts));
			}
			
			newWords.add(word);
			
		}
		
		return newWords;
	}

	public static void processCode(Post post) {
		String body = post.getBody();
		
		body = PreProcessorUtils.translateHTMLSimbols(body);
		
		//extract codes
		String code = extractCode(body); 
	
		List<String> words;
		List<String> validWords = new ArrayList<>();
		
		code = code.replaceAll("\u0000", "");
		
		//Only words of code
		String processedCode = "";
		if(!StringUtils.isBlank(code)) {
			processedCode = code;
			processedCode = processedCode.replace("----next----"," ");
			processedCode = PreProcessorUtils.translateHTMLSimbols(processedCode);
			
			processedCode = removeAllPunctuations(processedCode);
			
			words = Arrays.asList(processedCode.split("\\s+"));
			validWords.clear();
			
			words = processCamelCases(words);
			
			//remove stop words or small words
			assembleValidCodeWords(validWords,words,post);
			processedCode = String.join(" ", validWords);
			
			//processedCode = processedCode.toLowerCase();
			processedCode = processedCode.replaceAll("\u0000", "");
		}
		
		post.setProcessedCode(processedCode);
		validWords = null;
		words=null;
		
	}

	
	public static boolean isKeyword(String keyword, Post post) {
		if(tag.equals("java")) {
			return (Arrays.binarySearch(javaKeywords, keyword) >= 0);
		}else if(tag.equals("php")) {
			return (Arrays.binarySearch(phpKeywords, keyword) >= 0);
		}else if(tag.equals("python")) {
			return (Arrays.binarySearch(pythonKeywords, keyword) >= 0);
		}
        return false;
    }


	public static boolean isJavaKeyword(String keyword) {
        return (Arrays.binarySearch(javaKeywords, keyword) >= 0);
    }

	
	public void processComment(Comment comment) {
		String commentText = comment.getText();
		
		commentText = PreProcessorUtils.translateHTMLSimbols(commentText);
		
		commentText = commentText.replaceAll(USER_MENTION_REGEX_EXPRESSION, " ");
		if(commentText.contains("http")) {
			System.out.println();
		}
		commentText = commentText.replaceAll(URL_EXPRESSION_OUT, " ");
		
		commentText = commentText.toLowerCase();
		
		commentText = removeAllPunctuations(commentText);
		
		String[] words = commentText.split("\\s+");
		
		List<String> validWords = new ArrayList<>();
		
		//remove stop words or small words
		assembleValidWords(validWords,words);
		commentText = String.join(" ", validWords);
		commentText = commentText.replaceAll("\u0000", "");
		
		if(processLemmas) {
			edu.stanford.nlp.simple.Document stfDoc = new edu.stanford.nlp.simple.Document(commentText);
			List<String> sentList; 
					
			List<Sentence> sentences = stfDoc.sentences();
			if(!sentences.isEmpty()) {
				sentList= sentences.get(0).lemmas();
			    String textLemma = String.join(" ", sentList);
			    comment.setProcessedTextLemma(textLemma);
			}
			stfDoc=null;
			sentList=null;
			sentences=null;
		}
		validWords = null;
		words = null;
	}
	
	
	

	public void processBody(Post post) {
		String body = post.getBody();
		
		body = PreProcessorUtils.translateHTMLSimbols(body);
		
		//extract codes
		String code = extractCode(body); 
				
		//remove codes, images, links
		Document doc = Jsoup.parse(body);
		doc.select("pre").remove();
		doc.select("a").remove();
		doc.select("img").remove();
		body = doc.text();  
		
		body = body.toLowerCase();
		
		//remove punctuation marks
		//String[] words = body.split("\\p{Punct}+|\\s+");
		body = removeAllPunctuations(body);
		
		String[] words = body.split("\\s+");
		
		List<String> validWords = new ArrayList<>();
		
		//remove stop words or small words
		assembleValidWords(validWords,words);
		body = String.join(" ", validWords);
		body = body.replaceAll("\u0000", "");
		code = code.replaceAll("\u0000", "");
		
		List<String> words2;
		//Only words of code
		String processedCode = "";
		if(!StringUtils.isBlank(code)) {
			processedCode = code;
			processedCode = processedCode.replace("----next----"," ");
			processedCode = PreProcessorUtils.translateHTMLSimbols(processedCode);
			
			processedCode = removeAllPunctuations(processedCode);
			
			words2 = Arrays.asList(processedCode.split("\\s+"));
			validWords.clear();
			
			words2 = processCamelCases(words2);
			
			//remove stop words or small words
			assembleValidCodeWords(validWords,words2,post);
			processedCode = String.join(" ", validWords);
			
			//processedCode = processedCode.toLowerCase();
			processedCode = processedCode.replaceAll("\u0000", "");
		}
		post.setProcessedBody(body);
		
		if(processLemmas) {
			edu.stanford.nlp.simple.Document stfDoc = new edu.stanford.nlp.simple.Document(body);
			List<String> sentList; 
					
			List<Sentence> sentences = stfDoc.sentences();
			if(!sentences.isEmpty()) {
				sentList= sentences.get(0).lemmas();
			    String processedBodyLemma = String.join(" ", sentList);
			    post.setProcessedBodyLemma(processedBodyLemma);
			}
			stfDoc=null;
			sentList=null;
			sentences=null;
		}
		post.setCode(code);
		post.setProcessedCode(processedCode);
		validWords = null;
		doc = null;
		words = null;
		words2 = null;
		
		
	}
	
	/*
	 * Remove especial simbols only if isolated. Remove .:;'" if surrounded by spaces in any side. ?!, from all 
	 */
	public static String removePunctuations(String body) {
		body = body.replaceAll("\\s+\\p{Punct}+\\s"," "); 
		body = body.replaceAll("\\s+\\."," ");
		body = body.replaceAll("\\.(\\s+|$)"," ");
		body = body.replaceAll("\\s+\\:"," ");
		body = body.replaceAll("\\:(\\s+|$)"," ");
		body = body.replaceAll("\"\\s+"," ");
		body = body.replaceAll("\\s+\""," ");
		body = body.replaceAll("\'\\s+"," ");
		body = body.replaceAll("\\s+\'"," ");
		
		body = body.replaceAll("\\s+(\\;|$)"," ");
		body = body.replaceAll("\\;\\s+"," ");
		body = body.replaceAll("\\,"," ");
		body = body.replaceAll("\\?"," ");
		body = body.replaceAll("\\!"," ");
		return body;
	}
	
	public static String removeAllPunctuations(String body) {
		body = body.replaceAll("\\p{Punct}+"," "); 
		body = body.replaceAll("[^\\x20-\\x7e]", " "); //non-UTF-8 chars
		return body;
	}

	
	public static boolean isNumeric(String str)
	{
		return str.matches("[+-]?\\d*(\\.\\d+)?");
	}


	public void processTitle(Post post) {
		String title = post.getTitle();
		title = title.toLowerCase();
		title = PreProcessorUtils.translateHTMLSimbols(title);
		
		//remove punctuation marks
		//title = title.replaceAll("\\?", " ");
		title = removeAllPunctuations(title);
		String[] words = title.split("\\s+");
		List<String> validWords = new ArrayList<>();
		
		//remove stop words or small words or numbers only
		assembleValidWords(validWords,words);
		
		String finalTitle = String.join(" ", validWords);
		finalTitle = finalTitle.replaceAll("\u0000", "");
		post.setProcessedTitle(finalTitle);
		
		if(processLemmas) {
			edu.stanford.nlp.simple.Document stfDoc = new edu.stanford.nlp.simple.Document(finalTitle);
			List<String> sentList; 
			List<Sentence> sentences = stfDoc.sentences();
			if(!sentences.isEmpty()) {
				sentList= sentences.get(0).lemmas();
				 String processedTitleLemma = String.join(" ", sentList);
				 post.setProcessedTitleLemma(processedTitleLemma);
			}
			
		    stfDoc=null;
		    sentences=null;
		    sentList=null;
		}
	    
		validWords = null;
		title = null;
		words = null;
		
	}
	

	private static void assembleValidWords(List<String> validWords, String[] words) {
		for(String word:words) {
			word = word.trim();
			if(!stopWordsList.contains(word) && !(word.length()<2) && !StringUtils.isBlank(word) && !isNumeric(word)) {
				validWords.add(word);
			}
			
		}
	}
	
	private static void assembleValidCodeWords(List<String> validWords, List<String> words, Post post) {
		for(String word:words) {
			word = word.trim();
			word = word.toLowerCase();
			if(!stopWordsList.contains(word) && !(word.length()<3) && !StringUtils.isBlank(word) && !isNumeric(word) && !isKeyword(word,post)) {
				validWords.add(word);
			}
			
		}
	}



	protected static String extractCode(String postHTML) {
		Document doc = Jsoup.parse(postHTML);
		Elements elems = doc.select("pre");
		String codes = "";
		for(Element element: elems) {
			codes+=element.text()+"\n\n----next----\n\n";
		}
		
		elems = null;
		doc= null;
		return codes;
		
		
	}

	
	public static boolean isCamelCase(String token) {
		//return token.matches("([A-Z][a-z0-9]+)+|([A-Z]+[a-z0-9]+)+");
		
		return token.matches("([A-Z][a-z0-9]+)+|([A-Z]+[a-z0-9]+)+|(^[A-Z][a-z0-9]+[A-Z]$)|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+$)|(^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)+[A-Z]$)");
		
		
		
	}



	public static String getTag() {
		return tag;
	}



	public static void setTag(String tag) {
		PreProcessorUtils.tag = tag;
	}


	
}
