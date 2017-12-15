package com.ufu;

import com.ufu.preprocessor.utils.PreProcessorUtils;

public class Tests {
	

	public Tests() throws Exception {
		PreProcessorUtils preProcessorUtils = new PreProcessorUtils();
		preProcessorUtils.loadTagSynonyms();		
		
		String tags = preProcessorUtils.tagMastering("ruby-on-rails ruby c c# gem rubygems");
		System.out.println(tags);
		
		
	}
	
	public static void main(String[] args) throws Exception {
		Tests t = new Tests();
	}
	
}
