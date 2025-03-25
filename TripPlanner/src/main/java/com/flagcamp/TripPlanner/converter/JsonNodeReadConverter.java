package com.flagcamp.TripPlanner.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@ReadingConverter
@Component
public class JsonNodeReadConverter implements Converter<String, JsonNode> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonNode convert(String source) {
        try {
            return source == null ? null : mapper.readTree(source);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JsonNode from DB string", e);
        }
    }
}