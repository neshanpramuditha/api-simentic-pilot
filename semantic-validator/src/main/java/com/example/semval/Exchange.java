package com.example.semval;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/** One observed HTTP request/response pair, attributed to an OpenAPI operation. */
public record Exchange(
		int index,
		String operationId,
		String method,
		String url,
		Map<String, String> pathParams,
		JsonNode requestBody,
		int status,
		JsonNode responseBody,
		long durationMillis) {

	public boolean isSuccess() {
		return status >= 200 && status < 300;
	}

	/** Resolves {@code $path.x}, {@code $request.body...} and response-body paths ({@code $}, {@code $.a}). */
	public JsonNode resolve(String reference) {
		if (reference.startsWith("$path.")) {
			String value = pathParams.get(reference.substring("$path.".length()));
			return value == null ? Json.MAPPER.missingNode() : Json.MAPPER.getNodeFactory().textNode(value);
		}
		if (reference.startsWith("$request.body")) {
			return Json.at(requestBody, "$" + reference.substring("$request.body".length()));
		}
		return Json.at(responseBody, reference);
	}

}
