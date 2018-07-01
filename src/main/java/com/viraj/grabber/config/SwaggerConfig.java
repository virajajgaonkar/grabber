package com.viraj.grabber.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * Configures Swagger UI for the REST Api Documentation
 * The Swagger UI is avaliable at @see <a href="http://localhost:8080/swagger-ui.html">Swagger UI</a>
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket productApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.viraj.grabber"))
				.paths(PathSelectors.any())
				.build()
				.apiInfo(metaData())
				.useDefaultResponseMessages(false);
	}

	private ApiInfo metaData() {
		ApiInfo apiInfo = new ApiInfo(
				"Grabber Service",
				"Grabber Service with REST Api",
				"1.0",
				"Terms of service",
				new Contact("Viraj Ajgaonkar", "https://github.com/virajajgaonkar/grabber", "virajajgaonkar@gmail.com"),
				"Apache License Version 2.0",
				"https://www.apache.org/licenses/LICENSE-2.0.html",
				Collections.emptyList()
		);
		return apiInfo;
	}
}