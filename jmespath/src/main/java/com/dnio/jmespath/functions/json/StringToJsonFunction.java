package com.dnio.jmespath.functions.json;

import com.dnio.jmespath.jackson.ObjectMapperInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.FunctionArgument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class StringToJsonFunction extends BaseFunction {

	public StringToJsonFunction() {
		super(ArgumentConstraints.typeOf(JmesPathType.STRING));
	}

	@Override
	protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> functionArguments) {

		String value = runtime.toString(functionArguments.getFirst().value());
		ObjectMapper objectMapper = ObjectMapperInstance.getInstance();

		try {
			var typeReference = new TypeReference<Map<String, JsonNode>>() {
			};
			var jsonMap = objectMapper.readValue(value, typeReference);

			Map<T, T> result = new HashMap<>();
			for (Map.Entry<String, JsonNode> entry : jsonMap.entrySet()) {
				T key = runtime.createString(entry.getKey());
				T val = adaptJsonNode(runtime, entry.getValue());
				result.put(key, val);
			}

			return runtime.createObject(result);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private <T> T adaptJsonNode(Adapter<T> runtime, JsonNode node) {
		if (node == null || node.isNull()) {
			return runtime.createNull();
		} else if (node.isTextual()) {
			return runtime.createString(node.asText());
		} else if (node.isNumber()) {
			if (node.isLong()) {
				return runtime.createNumber(node.asLong());
			}
			return runtime.createNumber(node.asDouble());
		} else if (node.isBoolean()) {
			return runtime.createBoolean(node.asBoolean());
		} else if (node.isArray()) {
			List<T> adaptedList = StreamSupport.stream(node.spliterator(), false)
							.map(element -> adaptJsonNode(runtime, element))
							.toList();
			return runtime.createArray(adaptedList);
		} else if (node.isObject()) {
			Map<T, T> adaptedMap = new HashMap<>();
			node.fields().forEachRemaining(entry -> {
				T key = runtime.createString(entry.getKey());
				T val = adaptJsonNode(runtime, entry.getValue());
				adaptedMap.put(key, val);
			});
			return runtime.createObject(adaptedMap);
		}

		throw new IllegalArgumentException("Unsupported JsonNode type: " + node.getNodeType());
	}
}
