package com.flagcamp.TripPlanner.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@WritingConverter
@Component
public class JsonNodeWriteConverter implements Converter<JsonNode, PGobject> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public PGobject convert(JsonNode source) {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(mapper.writeValueAsString(source));
            return jsonObject;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JsonNode to PGobject", e);
        }
    }
}