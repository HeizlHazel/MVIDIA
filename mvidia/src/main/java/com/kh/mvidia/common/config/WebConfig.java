package com.kh.mvidia.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry){
		registry.addResourceHandler("/uploadFiles/**") // 브라우저에서 접근할 url 패턴
				.addResourceLocations("file:///" + uploadDir); // 실제 DS 경로
	}
	
	// 요청 url : localhost:8083/uploadFiles/파일명.png를 보내도
	// 실제로는    localhost:8083/uploadFiles/가 요청 되는 것이 아니라
	//			  c:/mvidiaFiles/uploadFiles/ 경로로 요청됨!
}
