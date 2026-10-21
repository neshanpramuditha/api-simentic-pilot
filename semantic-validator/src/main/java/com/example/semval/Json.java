package com.example.semval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;

/** JSON helpers and a deliberately small path syntax: {@code $}, {@code $.a}, {@code $.a.b}. */
public final class Json {

	public static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private Json() {
	}

	public static JsonNode read(Path file) throws IOException {
		return MAPPER.readTree(Files.readString(file));
	}

	public static JsonNode parseOrNull(String text) {
		if (text == null || text.isBlank()) {
			return NullNode.getInstance();
		}
		try {
			return MAPPER.readTree(text);
		} catch (IOException e) {
			return MAPPER.getNodeFactory().textNode(text);
		}
	}

	/** Resolves {@code $}, {@code $.a.b} against {@code root}; returns MissingNode when a segment is absent. */
	public static JsonNode at(JsonNode root, String path) {
		if (root == null) {
			return MissingNode.getInstance();
		}
		if (path.equals("$")) {
			return root;
		}
		if (!path.startsWith("$.")) {
			throw new IllegalArgumentException("unsupported path: " + path);
		}
		JsonNode node = root;
		for (String segment : path.substring(2).split("\\.")) {
			node = node.path(segment);
		}
		return node;
	}

	public static boolean isAbsentOrNull(JsonNode node) {
		return node == null || node.isMissingNode() || node.isNull();
	}

	public static String text(JsonNode node) {
		if (isAbsentOrNull(node)) {
			return null;
		}
		return node.isValueNode() ? node.asText() : node.toString();
	}

}
