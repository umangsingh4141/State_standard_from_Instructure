package com.example.demo.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class Getstate_standards {

    private static final Logger logger = LoggerFactory.getLogger(Getstate_standards.class);

    @Value("${canvas.web.service.api.url}")
    private String apiUrl;

    @Value("${canvas.web.service.access.token}")
    private String accessToken;

    private final WebClient webClient;

    public Getstate_standards(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> getAccounts() {
        logger.info("Requesting accounts from URL: {}", apiUrl);

        return webClient.get()
            .uri(apiUrl + "/outcomes/2001304") // Assuming this is the endpoint for the response you're working with
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> {
                try {
                    // Extract and save the ratings data to the file
                    String extractedFilePath = "C:\\Users\\umang.singh\\Downloads\\demo\\Extract_Standards.txt";
                    extractRatingsToFile(response, extractedFilePath);
                    // Optionally, store the full response in a file
                    //                    String filePath = "C:\\Users\\umang.singh\\Downloads\\demo\\Standards.txt";
                    //                    storeResponseToFile(response, filePath);
                } catch (IOException e) {
                    logger.error("Failed to process the response: ", e);
                }
            })
            .doOnError(error -> logger.error("Request failed: ", error))
            .onErrorResume(error -> Mono.just("Error: " + error.getMessage()));
    }

    private void storeResponseToFile(String response, String filePath) throws IOException {
        Files.write(Paths.get(filePath), response.getBytes());
    }

    private void extractRatingsToFile(String jsonResponse, String filePath) throws IOException {
        // Parse the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Get the ratings array
        JsonNode ratingsNode = rootNode.path("ratings");

        if (ratingsNode.isArray()) {
            // Create a new array node to store the extracted data
            ArrayNode extractedRatingsArray = objectMapper.createArrayNode();

            // Iterate over the ratings array and add each item to the new array node
            for (JsonNode rating : ratingsNode) {
                ObjectNode ratingObject = objectMapper.createObjectNode();
                ratingObject.put("description", rating.path("description").asText());
                ratingObject.put("points", rating.path("points").asDouble());

                extractedRatingsArray.add(ratingObject);
            }

            // Convert the array to a JSON string in the required format
            String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(extractedRatingsArray);

            // Write the JSON string to the file
            Files.write(Paths.get(filePath), jsonOutput.getBytes(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

            logger.info("Extracted ratings successfully saved to file at: {}", filePath);
        } else {
            logger.warn("No ratings found in the response.");
        }
    }
}

