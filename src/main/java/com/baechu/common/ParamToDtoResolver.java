package com.baechu.common;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.baechu.common.exception.CustomException;
import com.baechu.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ParamToDtoResolver implements HandlerMethodArgumentResolver {

	private ObjectMapper mapper;

	public ParamToDtoResolver(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	// 해당 파라미터가 Resolver가 필요한 type인지 판단
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterAnnotation(ParamToDto.class) != null;
	}

	// Resolver가 필요하면 실행되는 메서드. 파라미터를 객체로 매핑하는 로직 구현
	@Override
	public Object resolveArgument(
		MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory
	) {
		try {
			HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();
			String decodedQuery = URLDecoder.decode(request.getQueryString(), "UTF-8")
				.replaceAll("\"", "\\" + "\\" + "\"");
			System.out.println(decodedQuery);
			String json = queryToJson(decodedQuery);
			Object obj = mapper.readValue(json, parameter.getParameterType());
			return obj;
		} catch (Exception e) {
			log.warn("Cause : {}, Message : {}" ,e.getCause(), e.getMessage());
			throw new CustomException(ErrorCode.INVALID_PARAMETERS);
		}
	}

	// parameter json으로 변환
	private String queryToJson(String query) {
		String res = "{\"";
		for (int i = 0; i < query.length(); i++) {
			if (query.charAt(i) == '=') {
				res += "\"" + ":" + "\"";
			} else if (query.charAt(i) == '&') {
				res += "\"" + "," + "\"";
			} else {
				res += query.charAt(i);
			}
		}
		res += "\"" + "}";
		return res;
	}
}
