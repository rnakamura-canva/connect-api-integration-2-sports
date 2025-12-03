package com.example.demo.controller;

import com.example.demo.canva.api.AutofillApi;
import com.example.demo.canva.api.BrandTemplateApi;
import com.example.demo.canva.client.ApiClient;
import com.example.demo.canva.model.CreateDesignAutofillJobRequest;
import com.example.demo.canva.model.CreateDesignAutofillJobResponse;
import com.example.demo.canva.model.GetBrandTemplateDatasetResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/soccer")
public class SoccerController {

    @Value("${canva.api.base-url:https://api.canva.com/rest}")
    private String baseUrl;

    @GetMapping("/team")
    public String team(Model model, HttpSession session) {
        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        boolean isAuthenticated = accessToken != null && !accessToken.isEmpty();
        String displayName = (String) session.getAttribute("display_name");

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("displayName", displayName);

        // Retrieve team data from session if exists
        @SuppressWarnings("unchecked")
        Map<String, String> teamData = (Map<String, String>) session.getAttribute("team_data");

        // Pre-populate with default values if no data exists in session
        if (teamData == null) {
            teamData = new HashMap<>();
            teamData.put("teamName", "All stars");
            teamData.put("goalkeeper", "Marcos");
            teamData.put("centerBack1", "Gamarra");
            teamData.put("centerBack2", "Thiago Silva");
            teamData.put("leftBack", "Roberto Carlos");
            teamData.put("rightBack", "Cafu");
            teamData.put("midfielder1", "Casemiro");
            teamData.put("midfielder2", "Dunga");
            teamData.put("midfielder3", "Messi");
            teamData.put("leftWing", "Neymar");
            teamData.put("rightWing", "Cristiano Ronaldo");
            teamData.put("striker", "Ronaldo");
        }

        model.addAttribute("teamData", teamData);

        return "soccer/team";
    }

    @PostMapping("/team")
    public String saveTeam(
            @RequestParam String teamName,
            @RequestParam String goalkeeper,
            @RequestParam String centerBack1,
            @RequestParam String centerBack2,
            @RequestParam String leftBack,
            @RequestParam String rightBack,
            @RequestParam String midfielder1,
            @RequestParam String midfielder2,
            @RequestParam String midfielder3,
            @RequestParam String leftWing,
            @RequestParam String rightWing,
            @RequestParam String striker,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Save team data to session
        Map<String, String> teamData = new HashMap<>();
        teamData.put("teamName", teamName);
        teamData.put("goalkeeper", goalkeeper);
        teamData.put("centerBack1", centerBack1);
        teamData.put("centerBack2", centerBack2);
        teamData.put("leftBack", leftBack);
        teamData.put("rightBack", rightBack);
        teamData.put("midfielder1", midfielder1);
        teamData.put("midfielder2", midfielder2);
        teamData.put("midfielder3", midfielder3);
        teamData.put("leftWing", leftWing);
        teamData.put("rightWing", rightWing);
        teamData.put("striker", striker);

        session.setAttribute("team_data", teamData);

        redirectAttributes.addFlashAttribute("successMessage",
            "Team '" + teamName + "' saved successfully!");

        return "redirect:/soccer/team";
    }

    @GetMapping("/formations")
    public String formations(HttpSession session, Model model) {
        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        boolean isAuthenticated = accessToken != null && !accessToken.isEmpty();
        String displayName = (String) session.getAttribute("display_name");

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("displayName", displayName);

        // Get team data from session
        @SuppressWarnings("unchecked")
        Map<String, String> teamData = (Map<String, String>) session.getAttribute("team_data");
        model.addAttribute("teamData", teamData);
        return "soccer/formations";
    }

    @PostMapping("/create-autofill")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createAutofill(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            result.put("error", "Not authenticated");
            result.put("message", "Please connect to Canva first");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        String brandTemplateId = request.get("brandTemplateId");
        if (brandTemplateId == null || brandTemplateId.isEmpty()) {
            result.put("error", "Missing brand template ID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        // Get team data from session
        @SuppressWarnings("unchecked")
        Map<String, String> teamData = (Map<String, String>) session.getAttribute("team_data");
        if (teamData == null) {
            result.put("error", "No team data found");
            result.put("message", "Please save team data first");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        try {
            // Configure API client
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            // Step 1: Get brand template dataset using raw JSON to avoid deserialization issues
            String rawDatasetResponse = apiClient.getRestClient()
                .get()
                .uri(baseUrl + "/v1/brand-templates/" + brandTemplateId + "/dataset")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);

            // Parse the raw JSON to get dataset field names
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> datasetResponseMap = objectMapper.readValue(rawDatasetResponse, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> dataset = (Map<String, Object>) datasetResponseMap.get("dataset");

            // Step 2: Map team data to dataset fields
            // Create an uppercase lookup map for case-insensitive matching
            Map<String, String> teamDataUppercase = new HashMap<>();
            for (Map.Entry<String, String> entry : teamData.entrySet()) {
                teamDataUppercase.put(entry.getKey().toUpperCase(), entry.getValue());
            }

            Map<String, Object> dataMap = new HashMap<>();

            if (dataset != null) {
                for (Map.Entry<String, Object> entry : dataset.entrySet()) {
                    String fieldName = entry.getKey();

                    // Try to find matching team data (case-insensitive)
                    String teamValue = teamDataUppercase.get(fieldName.toUpperCase());
                    if (teamValue != null) {
                        // Create a raw map for the dataset value
                        Map<String, Object> datasetValue = new HashMap<>();
                        datasetValue.put("type", "text");
                        datasetValue.put("text", teamValue);
                        dataMap.put(fieldName, datasetValue);
                    }
                }
            }

            // Step 3: Create autofill job using raw JSON
            Map<String, Object> autofillRequestBody = new HashMap<>();
            autofillRequestBody.put("brand_template_id", brandTemplateId);
            autofillRequestBody.put("data", dataMap);

            // Make the autofill API call directly with raw JSON
            String autofillResponseRaw = apiClient.getRestClient()
                .post()
                .uri(baseUrl + "/v1/autofills")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(autofillRequestBody))
                .retrieve()
                .body(String.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> autofillResponse = objectMapper.readValue(autofillResponseRaw, Map.class);

            result.put("success", true);
            result.put("job", autofillResponse.get("job"));
            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            result.put("error", "API Error");
            result.put("message", e.getResponseBodyAsString());
            result.put("statusCode", e.getStatusCode().value());
            return ResponseEntity.status(e.getStatusCode()).body(result);
        } catch (Exception e) {
            result.put("error", "Unexpected error");
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/autofill-status/{jobId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAutofillStatus(
            @PathVariable String jobId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            result.put("error", "Not authenticated");
            result.put("message", "Please connect to Canva first");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        try {
            // Configure API client
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            // Get the autofill job status using raw JSON
            String rawJobResponse = apiClient.getRestClient()
                .get()
                .uri(baseUrl + "/v1/autofills/" + jobId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);

            // Parse the raw JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> jobResponse = objectMapper.readValue(rawJobResponse, Map.class);

            result.put("success", true);
            result.put("job", jobResponse.get("job"));
            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            result.put("error", "API Error");
            result.put("message", e.getResponseBodyAsString());
            result.put("statusCode", e.getStatusCode().value());
            return ResponseEntity.status(e.getStatusCode()).body(result);
        } catch (Exception e) {
            result.put("error", "Unexpected error");
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}
