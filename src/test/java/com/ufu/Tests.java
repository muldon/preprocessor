package com.ufu;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ufu.preprocessor.utils.PreProcessorUtils;

public class Tests {
	public static void main(String[] args) throws Exception {
		PreProcessorUtils utils = new PreProcessorUtils();
		utils.initializeConfigs();
		
		String body = "<p>An explicit cast to double isn't necessary.</p>" + 
				"<pre><code>double trans = (double)trackBar1.Value / 5000.0;" + 
				"</code></pre>" + 
				"<p>Identifying the constant as <code>5000.0</code> (or as <code>5000d</code>) is sufficient:</p>" + 
				"<pre><code>double trans = trackBar1.Value / 5000.0;" + 
				"double trans = trackBar1.Value / 5000d;" + 
				"</code></pre>"; 
				
		
		//String[] bodyContent = utils.separaSomentePalavrasNaoSomentePalavras(body,"body");
		//System.out.println(bodyContent[2]);
	
		Document doc = Jsoup.parse(body);
		Elements elems = doc.select("code,pre");
		String codeText = elems.text();
		//System.out.println(codeText);
		
		 
		
	}
}
