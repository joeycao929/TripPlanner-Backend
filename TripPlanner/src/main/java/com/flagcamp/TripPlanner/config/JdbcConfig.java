package com.flagcamp.TripPlanner.config;

import com.flagcamp.TripPlanner.converter.JsonNodeReadConverter;
import com.flagcamp.TripPlanner.converter.JsonNodeWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import java.util.List;


@Configuration
public class JdbcConfig {

    @Bean
    public JdbcCustomConversions jdbcCustomConversions(
            JsonNodeWriteConverter writeConverter,
            JsonNodeReadConverter readConverter
    ) {
        return new JdbcCustomConversions(List.of(writeConverter, readConverter));
    }
}
