package com.email.email_writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest){
        String prompt = buildpromt(emailRequest);
        Map<String,Object> requestBody = Map.of(
                "contents",new Object[]{
                        Map.of("parts",new Object[]{
                                Map.of("text", prompt),
                        })
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(); 
        
        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
         try{
             ObjectMapper mapper = new ObjectMapper();
             JsonNode rootNode = mapper.readTree(response);
             return rootNode.path("candidate")
                     .get(0)
                     .path("content")
                     .path("parts")
                     .get(0)
                     .path("text")
                     .asText();
         }catch (Exception e){
             return "Error processing request: " + e.getMessage();
         }
    }
//private String extractResponseContent(String response) {
//    try{
//        // Use the ObjectMapper you defined as a field in the class
//        // If you don't have one, this line is fine:
//        ObjectMapper mapper = new ObjectMapper();
//
//        JsonNode rootNode = mapper.readTree(response);
//
//        // First, check if the "candidates" array exists and is not empty
//        JsonNode candidatesNode = rootNode.path("candidates"); // FIX 1: "candidates" (plural)
//
//        if (candidatesNode.isMissingNode() || !candidatesNode.isArray() || candidatesNode.isEmpty()) {
//            // If not, it's an error. Print the error from Gemini to the console.
//            System.err.println("Error or empty response from Gemini: " + rootNode.toPrettyString());
//
//            // Try to find and return the specific error message from the API
//            JsonNode error = rootNode.path("error");
//            if (!error.isMissingNode()) {
//                return "Error from API: " + error.path("message").asText("Unknown error");
//            }
//            return "Error: Received an invalid response from the AI.";
//        }
//
//        // If we are here, "candidates" exists.
//        // FIX 2: Use .path(0) instead of .get(0) because it's safer and won't crash.
//        String text = candidatesNode.path(0)
//                .path("content")
//                .path("parts")
//                .path(0) // FIX 3: Use .path(0) here too
//                .path("text")
//                .asText(); // .asText() is safe and returns "" if not found
//
//        if (text.isEmpty()) {
//            System.err.println("Could not find 'text' field in response: " + rootNode.toPrettyString());
//            return "Error: Could not parse AI response.";
//        }
//
//        return text;
//
//    }catch (Exception e){
//        System.err.println("Failed to parse JSON: " + e.getMessage());
//        return "Error processing response: " + e.getMessage();
//    }
//}

    private String buildpromt(EmailRequest emailRequest){
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email from the following form:\n");
        if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()){
            prompt.append("Use a").append(emailRequest.getTone()).append("tone");
        }
        prompt.append("\nOriginal email\n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }

}
