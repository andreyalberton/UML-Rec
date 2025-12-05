package com.LMJU.projectCW.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;

public class AIAdapter {
    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL_NAME = "mistral:v0.3";

    public String analyzeCode(String sourceCode) {
        UMLPromptGenerator promptGen = new UMLPromptGenerator();

        // 1 Construct the Messages Array
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", promptGen.getSystemPrompt());

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", sourceCode);

        JSONArray messages = new JSONArray();
        messages.put(systemMessage);
        messages.put(userMessage);

        // 2 Build the Payload with format: "json"
        JSONObject payload = new JSONObject();
        payload.put("model", MODEL_NAME);
        payload.put("messages", messages);
        payload.put("stream", false);
        payload.put("format", "json");

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 3 Parse Response
            JSONObject responseJson = new JSONObject(response.body());
            return responseJson.getJSONObject("message").getString("content");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}