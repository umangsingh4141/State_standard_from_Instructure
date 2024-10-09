package com.example.demo.service;

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
public class Getoutcomes_id {

    private static final Logger logger = LoggerFactory.getLogger(Getoutcomes_id.class);

    @Value("${canvas.web.service.api.url}")
    private String apiUrl;

    @Value("${canvas.web.service.access.token}")
    private String accessToken;

    private final WebClient webClient;

    public Getoutcomes_id(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> geturls() {
        logger.info("Requesting accounts from URL: {}", apiUrl);

        return webClient.get()
            .uri(apiUrl + "/courses/1341513/outcome_groups/1614130/outcomes") // Adjust this as necessary
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> {
                try {
                    // Extract and save the URLs to the file
                    String extractedFilePath = "C:\\Users\\umang.singh\\Downloads\\demo\\Outcomes_Urls.txt";
                    extractUrlsToFile(response, extractedFilePath);
                } catch (IOException e) {
                    logger.error("Failed to process the response: ", e);
                }
            })
            .doOnError(error -> logger.error("Request failed: ", error))
            .onErrorResume(error -> Mono.just("Error: " + error.getMessage()));
    }

    private void extractUrlsToFile(String jsonResponse, String filePath) throws IOException {
        // Parse the JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Prepare to collect URLs
        StringBuilder sb = new StringBuilder();

        // Iterate over the JSON array
        if (rootNode.isArray()) {
            for (JsonNode item : rootNode) {
                // Extract relevant fields
                String mainUrl = item.path("url").asText();
                String subgroupsUrl = item.path("outcome_group").path("subgroups_url").asText();
                String outcomesUrl = item.path("outcome_group").path("outcomes_url").asText();
                String outcomeUrl = item.path("outcome").path("url").asText();

                // Append extracted data with field names
                sb.append("Main URL: ").append(mainUrl).append(System.lineSeparator());
                sb.append("Subgroups URL: ").append(subgroupsUrl).append(System.lineSeparator());
                sb.append("Outcomes URL: ").append(outcomesUrl).append(System.lineSeparator());
                sb.append("State_Standard_URL: ").append(outcomeUrl).append(System.lineSeparator());
                sb.append(System.lineSeparator()); // Separate entries for readability
            }

            // Write the extracted URLs to the file
            Files.write(Paths.get(filePath), sb.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            logger.info("Extracted URLs successfully saved to file at: {}", filePath);
        } else {
            logger.warn("The response is not a valid JSON array.");
        }
    }

    }

