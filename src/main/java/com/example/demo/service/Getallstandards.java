package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class Getallstandards {

    private static final Logger logger = LoggerFactory.getLogger(Getallstandards.class);

    @Value("${canvas.web.service.api.url}")
    private String apiUrl;

    @Value("${canvas.web.service.access.token}")
    private String accessToken;

    private final WebClient webClient;

    public Getallstandards(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // Function to process State_Standard_URLs
    public Mono<String> processStateStandardUrls() {
        try {
            // Read the file containing URLs
            String filePath = "C:\\Users\\umang.singh\\Downloads\\demo\\Outcomes_Urls.txt";
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // Filter and collect State_Standard_URLs
            List<String> stateStandardUrls = lines.stream()
                .filter(line -> line.startsWith("State_Standard_URL: "))
                .map(line -> line.replace("State_Standard_URL: ", "").trim())
                .map(url -> url.replace("/api/v1", "")) // Remove "/api/v1" from the URLs
                .collect(Collectors.toList());

            // Overwrite file content before starting new writes
            String outputFilePath = "C:\\Users\\umang.singh\\Downloads\\demo\\Getallstandards.txt";
            Files.write(Paths.get(outputFilePath), new byte[0], StandardOpenOption.TRUNCATE_EXISTING); // Clear the file

            // Process each URL (e.g., make API calls)
            for (String url : stateStandardUrls) {
                System.out.println("Processing URL: " + url + "\n");
                makeApiCallForStateStandard(url, outputFilePath);
            }
        } catch (IOException e) {
            logger.error("Failed to process the file: ", e);
        }
        return null;
    }

    // Function to make the API call for each State_Standard_URL and store response
    private void makeApiCallForStateStandard(String stateStandardUrl, String outputFilePath) {
        webClient.get()
            .uri(apiUrl + stateStandardUrl)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> {
                logger.info("Response received for URL {}: {}", stateStandardUrl, response);
                extractAndWriteRatings(stateStandardUrl, response, outputFilePath);  // Extract ratings and write to file
            })
            .doOnError(error -> logger.error("Request failed for URL {}: ", stateStandardUrl, error))
            .subscribe(); // Ensure the call is executed
    }

    // Function to extract the 'ratings' field, print and append it to a file
    private void extractAndWriteRatings(String url, String response, String outputFilePath) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Parse the JSON response
            JsonNode rootNode = objectMapper.readTree(response);

            // Extract the 'ratings' field
            JsonNode ratingsNode = rootNode.path("ratings");

            if (!ratingsNode.isMissingNode()) {
                // Print the URL and extracted ratings
                System.out.println("Extracted data from URL: " + url);
                System.out.println("Ratings: " + ratingsNode.toPrettyString() + "\n");

                // Write the URL and extracted ratings to the file
                String formattedData = "URL: " + url + "\nRatings: " + ratingsNode.toPrettyString() + "\n\n";
                Files.write(Paths.get(outputFilePath), formattedData.getBytes(), StandardOpenOption.APPEND);
                logger.info("Successfully wrote ratings to file for URL: {}", url);
            } else {
                logger.warn("No ratings found for URL: {}", url);
            }
        } catch (IOException e) {
            logger.error("Failed to write ratings to file for URL {}: ", url, e);
        }
    }
}
