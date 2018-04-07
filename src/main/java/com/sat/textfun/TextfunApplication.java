package com.sat.textfun;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.TypeTokenFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

@SpringBootApplication
public class TextfunApplication {


	private Logger logger = LogManager.getLogger(TextfunApplication.class);

	private static final String[] EMPTY_ARRAY = new String[0];

	Map<String, LongAdder> termCountMap = new TreeMap<>();

	public static void main(String[] args) throws Exception {

		SpringApplication.run(TextfunApplication.class, args);
		TextfunApplication app = new TextfunApplication();
		app.emitTokens();
	}



	private void emitTokens() throws URISyntaxException, IOException {
		logger.info("------------------------------------------------");

		Path path = Paths.get(this.getClass().getClassLoader()
				.getResource("sample-text.txt").toURI());
		Stream<String> lines = Files.lines(path);
		lines.forEach(line -> {
			try {
				processLine(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		lines.close();
		termCountMap.forEach((k,v) ->
			logger.info(k +  "	:	" + v)
		);
		logger.info("------------------------------------------------");
	}


	private void processLine(String line) throws IOException {
		logger.info(line);
		if(line == null)
			return ;

		line = normalize(line);
		TokenStream tokenStream = tokenize(line);
		tokenStream.reset();
		CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);
		while (tokenStream.incrementToken()) {
			String token = cattr.toString();
			tokenFound(token);
		}
		tokenStream.end();
		tokenStream.close();
	}

	private String normalize(String token){
		if(token == null)
			return token;

		token = token.trim();
		token = token.toLowerCase();
		return token;
	}



	private String[] tokenizeByWhitespace(String line){
		if(line  == null)
			return EMPTY_ARRAY;

		return line.split("\\s+");
	}


	private static Set<String> stopTypes = new HashSet<>(); // Ex: <NUM>
	private TokenStream tokenize(String line){
		if(line  == null)
			return null;

		final StandardTokenizer input = new StandardTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY);
		input.setReader(new StringReader(line));
		return new TypeTokenFilter(input, stopTypes);
	}

	public void tokenFound(String token) {
		termCountMap.computeIfAbsent(token, (t) -> new LongAdder()).increment();
	}


}
