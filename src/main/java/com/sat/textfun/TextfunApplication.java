package com.sat.textfun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TextfunApplication {


	private static Logger logger = LogManager.getLogger(TextfunApplication.class);


	public static void main(String[] args) throws Exception {
		SpringApplication.run(TextfunApplication.class, args);
		double minTokenCount = 2;
		double tokenThreshold = 0.0015d;
		double tfidfThreshold = 0.29d;
		double ratioToTopTermCount = 0.1d;
		//new HVTAnalyzer("mormon-5rj1te.txt").emitTokens(minTokenCount, tokenThreshold, tfidfThreshold, ratioToTopTermCount);
		//new HVTAnalyzer("keto-3adsan.txt").emitTokens(minTokenCount, tokenThreshold, tfidfThreshold, ratioToTopTermCount);
		//new HVTAnalyzer("insomniacs-7pyms4.txt").emitTokens(minTokenCount, tokenThreshold, tfidfThreshold, ratioToTopTermCount);
		//new HVTAnalyzer("pikes-peak-50juq6.txt").emitTokens(minTokenCount, tokenThreshold, tfidfThreshold, ratioToTopTermCount);
		//new HVTAnalyzer("doggo-8adxrl.txt").emitTokens(minTokenCount, tokenThreshold, tfidfThreshold, ratioToTopTermCount);
		//new HVTAnalyzer("algernon-book-8ajn87.txt").emitTokens(minTokenCount, tokenThreshold, tfidfThreshold, ratioToTopTermCount);
		new HVTAnalyzer("tolstoy-8a56ci.txt").emitTokens(minTokenCount, tokenThreshold, tfidfThreshold, ratioToTopTermCount);
	}




}
