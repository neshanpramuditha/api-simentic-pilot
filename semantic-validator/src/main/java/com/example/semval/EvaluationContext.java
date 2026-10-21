package com.example.semval;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * What rules may use besides the exchange itself: the OpenAPI index, an HTTP client for read-backs
 * (active mode only) and a JDBC connection for database rules (only when configured).
 */
public final class EvaluationContext {

	private final OpenApiIndex openApi;
	private final String baseUrl;
	private final HttpClient http;
	private final Connection database;

	public EvaluationContext(OpenApiIndex openApi, String baseUrl, Connection database) {
		this.openApi = openApi;
		this.baseUrl = baseUrl;
		this.http = baseUrl == null ? null : HttpClient.newHttpClient();
		this.database = database;
	}

	public boolean isActive() {
		return http != null;
	}

	public boolean hasDatabase() {
		return database != null;
	}

	public OpenApiIndex openApi() {
		return openApi;
	}

	/** Sends a GET for {@code operationId} with the given path parameters and returns {status, body}. */
	public ObjectNode read(String operationId, Map<String, String> pathParams) {
		OpenApiIndex.Operation op = openApi.require(operationId);
		String url = baseUrl + op.expand(pathParams);
		try {
			HttpResponse<String> response = http.send(HttpRequest.newBuilder(URI.create(url))
					.header("Accept", "application/json").GET().build(), HttpResponse.BodyHandlers.ofString());
			ObjectNode result = Json.MAPPER.createObjectNode();
			result.put("operationId", operationId);
			result.put("url", op.expand(pathParams));
			result.put("status", response.statusCode());
			result.set("body", Json.parseOrNull(response.body()));
			return result;
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("read-back failed: " + url, e);
		}
	}

	/** Runs a parameterised query and returns the first row as an object keyed by column label, or null if none. */
	public ObjectNode queryRow(String sql, List<String> params) {
		try (PreparedStatement statement = database.prepareStatement(sql)) {
			for (int i = 0; i < params.size(); i++) {
				statement.setString(i + 1, params.get(i));
			}
			try (ResultSet rs = statement.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				ResultSetMetaData meta = rs.getMetaData();
				ObjectNode row = Json.MAPPER.createObjectNode();
				for (int c = 1; c <= meta.getColumnCount(); c++) {
					Object value = rs.getObject(c);
					row.put(meta.getColumnLabel(c).toLowerCase(), value == null ? null : value.toString());
				}
				return row;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("database query failed: " + sql, e);
		}
	}

	public JsonNode jsonNull() {
		return Json.MAPPER.nullNode();
	}

}
