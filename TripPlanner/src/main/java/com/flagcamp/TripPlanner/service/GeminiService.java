package com.flagcamp.TripPlanner.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagcamp.TripPlanner.model.TripSearchBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;
import java.io.File;
import java.util.Optional;

@Service
public class GeminiService {
    // Access to APIKey and Uri
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUri;

    private final WebClient webClient;


    public GeminiService(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    public JsonNode getTripDetail(TripSearchBody body) {
        String answer = getAnswer(body);
        return jsonProcess(answer);

    }

    private JsonNode jsonProcess(String jsonBody) {

        try {
            // Create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Read JSON file into JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonBody);

            Optional<JsonNode> textNode = Optional.ofNullable(
                    rootNode.path("candidates")
                            .path(0)
                            .path("content")
                            .path("parts")
                            .path(0)
                            .path("text")
            );



            String newString = textNode.get().asText().replace("```json","").replace("```","");

            JsonNode trip = objectMapper.readTree(newString);

            return trip;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private String getAnswer(TripSearchBody body){
        // Construct request payload
        String question = String.format("Generate Trip Plan for Location: %s, %s from %s to %s, with preference as %s. " +
                "Give me Place Details with place image url, geo coordinates, ratings and reviews, time to travel each of the location with daily itinerary plan to visit in JSON format. " +
                "Start the text directly with json without any explanation" +
                "Sample Json as " +
                "{\n" +
                "    \"daily_itinerary\": {\n" +
                "      \"Date 1\": [\n" +
                "        {\n" +
                "          \"place_name\": \"Place Name\",\n" +
                "          \"address\": \"Address Detail\",\n" +
                "          \"latitude\": 00.00000,\n" +
                "          \"longitude\": 00.00000,\n" +
                "          \"image_url\": \"url\",\n" +
                "          \"rating\": 0.0,\n" +
                "          \"travel_time_to_next_location\": \"00 min\",\n" +
                "          \"suggest_time_to_spend\": \"00 min\",\n" +
                "          \"review\": \"Review 1\",\n" +
                "        },\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}", body.city(), body.country(), body.startDate(), body.endDate(), body.preferences());

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[] {
                                Map.of("text", question)
                        })
                }
        );

        // Make API Call
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return webClient.post()
                        .uri(geminiApiUri + geminiApiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception e) {
                if (attempt == 2) {
                    throw new RuntimeException("Failed to get response after retrying", e);
                }
                // Optionally log retry
                System.out.println("Retrying getAnswer... attempt #" + (attempt + 1));
            }
        }

        return null;

    }

}
