package com.flagcamp.TripPlanner.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

@Configuration
@EnableJdbcRepositories(basePackages = "com.flagcamp.TripPlanner.repository")
public class JdbcConfig {

    @Bean
    public JdbcCustomConversions jdbcCustomConversions(ObjectMapper objectMapper) {
        return new JdbcCustomConversions(Arrays.asList(
                new JsonNodeToStringConverter(objectMapper),
                new StringToJsonNodeConverter(objectMapper)
        ));
    }

    @WritingConverter
    private static class JsonNodeToStringConverter implements Converter<JsonNode, PGobject> {
        private final ObjectMapper objectMapper;

        public JsonNodeToStringConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public PGobject convert(JsonNode source) {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            try {
                pgObject.setValue(objectMapper.writeValueAsString(source));
                return pgObject;
            } catch (SQLException e) {
                throw new RuntimeException("Error converting JsonNode to PGobject: SQL error", e);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting JsonNode to PGobject: JSON processing error", e);
            }
        }
    }

    @ReadingConverter
    private static class StringToJsonNodeConverter implements Converter<PGobject, JsonNode> {
        private final ObjectMapper objectMapper;

        public StringToJsonNodeConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public JsonNode convert(PGobject source) {
            if (source.getValue() == null) {
                return null;
            }
            try {
                return objectMapper.readTree(source.getValue());
            } catch (IOException e) {
                throw new RuntimeException("Error converting PGobject to JsonNode", e);
            }
        }
    }
}
