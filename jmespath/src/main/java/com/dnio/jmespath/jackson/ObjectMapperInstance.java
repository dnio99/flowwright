package com.dnio.jmespath.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ObjectMapperInstance {

	private final static ObjectMapper INSTANCE = new ObjectMapper();

	static {
		SimpleModule validationModule = new SimpleModule();

		INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		INSTANCE.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		INSTANCE.configOverride(ArrayNode.class).setMergeable(false);
		INSTANCE.registerModule(validationModule);
	}

	public static ObjectMapper getInstance() {
		return INSTANCE;
	}
}
