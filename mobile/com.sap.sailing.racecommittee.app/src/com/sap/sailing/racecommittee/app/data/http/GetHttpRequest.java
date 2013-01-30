package com.sap.sailing.racecommittee.app.data.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class GetHttpRequest<T> extends HttpRequest {

	public T get(String url, TypeReference<?> typeref)
			throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		/*mapper = */mapper
				.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
				.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		HttpRequestBase get = new HttpGet(url);
		HttpResponse response = executeRequest(get, "GET request");
		HttpEntity entity = response.getEntity();
		return mapper.readValue(entity.getContent(), typeref);
	}
}
