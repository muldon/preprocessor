package com.ufu;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class PreProcessorInitializer extends SpringBootServletInitializer {

	public static void main(String[] args) {
		new PreProcessorInitializer().configure(new SpringApplicationBuilder(PreProcessorInitializer.class)).run(args).close();;
	}
	
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PreProcessorInitializer.class);
    }
	
	

}
