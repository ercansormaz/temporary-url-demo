package dev.ercan.poc.temporaryurldemo.config;

import dev.ercan.poc.temporaryurldemo.interceptor.TemporaryUrlInterceptor;
import dev.ercan.poc.temporaryurldemo.service.TemporaryUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

  private final TemporaryUrlService temporaryUrlService;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new TemporaryUrlInterceptor(temporaryUrlService))
        .addPathPatterns(temporaryUrlService.getProtectedBase() + "**");
  }


}
