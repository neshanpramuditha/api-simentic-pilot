package com.example.apipilot.defects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.example.apipilot.controller.OrderController;
import com.example.apipilot.entity.Order;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * INTENTIONAL DEFECT SV-01 (see docs/defect-register.md and docs/experiments/sv-01/design.md).
 * <p>
 * When enabled, the response of {@code POST /api/orders} carries {@code total} as a JSON string
 * instead of a number. Nothing is saved differently and no other endpoint is affected.
 * <p>
 * Disabled by default: this bean only exists when
 * {@code pilot.defects.sv01-total-as-string.enabled=true}.
 */
@RestControllerAdvice
@ConditionalOnProperty(name = "pilot.defects.sv01-total-as-string.enabled", havingValue = "true")
public class Sv01TotalAsStringAdvice implements ResponseBodyAdvice<Object> {

	private static final Logger log = LoggerFactory.getLogger(Sv01TotalAsStringAdvice.class);

	private final ObjectMapper objectMapper;

	public Sv01TotalAsStringAdvice(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		log.warn("DEFECT SV-01 ENABLED: POST /api/orders returns total as a JSON string");
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.getContainingClass() == OrderController.class
				&& returnType.getMethod() != null
				&& returnType.getMethod().getName().equals("createOrder");
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		if (!(body instanceof Order order)) {
			return body;
		}
		ObjectNode json = objectMapper.valueToTree(order);
		json.put("total", order.getTotal().toPlainString());
		return json;
	}

}
