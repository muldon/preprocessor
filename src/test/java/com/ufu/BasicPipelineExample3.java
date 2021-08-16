package com.ufu;

import java.util.List;

import com.ufu.preprocessor.utils.PreProcessorUtils;

import edu.stanford.nlp.simple.Document;


public class BasicPipelineExample3 {

  public static String text = "Joe Smith was born in California. "+
  	  "Study studying studied. stream streaming streamming streammed" +
      "In 2017, he went to Paris, France in the summer. " +
      "His flight left at 3:00pm on July 10th, 2017. " +
      "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
      "He sent a postcard to his sister Jane Smith. " +
      "He is ok. " +
      "Simple, right? Remove removed removing was were is are element at given gave give index, insert it at desired index. Let's see if it works for the second test case."+
      "He is ok to go now. " +
      "After hearing about Joe's trip, Jane decided she might go to France one day.";

  public static void main(String[] args) {
    // set up pipeline properties
    //Properties props = new Properties();
    // set the list of annotators to run
   // props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
    // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
    //props.setProperty("coref.algorithm", "neural");
    // build pipeline
    //StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    // create a document object
    //CoreDocument document = new CoreDocument(text);
    // annnotate the document
   // pipeline.annotate(document);
    // examples

    // 10th token of the document
	  text=  PreProcessorUtils.removeAllPunctuations(text);   
	//text =  removeAllPunctuations(text);  
	  
    Document doc = new Document(text);
    List<String> sentList = doc.sentences().get(0).lemmas();
    text = String.join(" ", sentList);
    System.out.println(text);
    /*for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
        System.out.println(sent.lemmas());
        // ...
    }*/
    
    
    
  }

}
