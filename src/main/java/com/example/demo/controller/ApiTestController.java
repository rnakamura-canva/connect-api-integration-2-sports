package com.example.demo.controller;

import com.example.demo.canva.api.BrandTemplateApi;
import com.example.demo.canva.api.DesignApi;
import com.example.demo.canva.api.FolderApi;
import com.example.demo.canva.api.UserApi;
import com.example.demo.canva.client.ApiClient;
import com.example.demo.canva.model.GetBrandTemplateDatasetResponse;
import com.example.demo.canva.model.GetBrandTemplateResponse;
import com.example.demo.canva.model.GetDesignResponse;
import com.example.demo.canva.model.GetListDesignResponse;
import com.example.demo.canva.model.ListBrandTemplatesResponse;
import com.example.demo.canva.model.ListFolderItemsResponse;
import com.example.demo.canva.model.MoveFolderItemRequest;
import com.example.demo.canva.model.OwnershipType;
import com.example.demo.canva.model.UserProfileResponse;
import com.example.demo.canva.privateapi.BrandKitApi;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class ApiTestController {

    @Value("${canva.api.base-url:https://api.canva.com/rest}")
    private String baseUrl;

    public ApiTestController() {
    }

    @PostMapping("/profile")
    public ResponseEntity<Map<String, Object>> testGetProfile(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            result.put("error", "Not authenticated");
            result.put("message", "Please connect to Canva first");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // Prepare request details
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "GET");
        requestDetails.put("endpoint", "https://api.canva.com/rest/v1/users/me/profile");
        requestDetails.put("timestamp", Instant.now().toString());
        requestDetails.put("authentication", "Bearer token (from session)");

        result.put("request", requestDetails);

        try {
            // Create API client with session token
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            // Add bearer token as default header instead of using setBearerToken
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            UserApi userApi = new UserApi(apiClient);

            // Make the API call
            long startTime = System.currentTimeMillis();
            UserProfileResponse profile = userApi.getUserProfile();
            long duration = System.currentTimeMillis() - startTime;

            // Prepare response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", 200);
            responseDetails.put("status", "OK");
            responseDetails.put("duration", duration + "ms");
            responseDetails.put("timestamp", Instant.now().toString());

            // Response body - convert the whole response to map for display
            Map<String, Object> body = new HashMap<>();
            if (profile.getProfile() != null) {
                body.put("display_name", profile.getProfile().getDisplayName());
            }
            // Add the whole profile object as JSON-like structure
            body.put("profile", profile);

            responseDetails.put("body", body);
            result.put("response", responseDetails);
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", e.getStatusCode().value());
            responseDetails.put("status", e.getStatusText());
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("errorBody", e.getResponseBodyAsString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("error", e.getClass().getSimpleName());
            responseDetails.put("message", e.getMessage());
            responseDetails.put("timestamp", Instant.now().toString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    @PostMapping("/designs")
    public ResponseEntity<Map<String, Object>> testListDesigns(
            @RequestParam(required = false) String query,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            result.put("error", "Not authenticated");
            result.put("message", "Please connect to Canva first");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // Prepare request details
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "GET");
        requestDetails.put("endpoint", "https://api.canva.com/rest/v1/designs");
        requestDetails.put("timestamp", Instant.now().toString());
        requestDetails.put("authentication", "Bearer token (from session)");
        requestDetails.put("parameters", Map.of(
            "query", query != null ? query : "null (not specified)",
            "continuation", "null (first page)",
            "ownership", "null (all designs)",
            "sortBy", "null (default sort)"
        ));

        result.put("request", requestDetails);

        try {
            // Create API client with session token
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            DesignApi designApi = new DesignApi(apiClient);

            // Make the API call with query parameter
            long startTime = System.currentTimeMillis();
            GetListDesignResponse designs = designApi.listDesigns(query, null, null, null);
            long duration = System.currentTimeMillis() - startTime;

            // Prepare response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", 200);
            responseDetails.put("status", "OK");
            responseDetails.put("duration", duration + "ms");
            responseDetails.put("timestamp", Instant.now().toString());

            // Response body
            Map<String, Object> body = new HashMap<>();
            body.put("items_count", designs.getItems() != null ? designs.getItems().size() : 0);
            body.put("has_more", designs.getContinuation() != null);
            body.put("continuation", designs.getContinuation());
            body.put("items", designs.getItems());

            responseDetails.put("body", body);
            result.put("response", responseDetails);
            result.put("success", true);

            // Extract design info for display (thumbnails, titles, etc.)
            if (designs.getItems() != null) {
                result.put("designs", designs.getItems().stream()
                    .map(design -> {
                        Map<String, Object> designInfo = new HashMap<>();
                        designInfo.put("id", design.getId());
                        designInfo.put("title", design.getTitle() != null ? design.getTitle() : "Untitled");

                        // Get edit URL from design.urls
                        if (design.getUrls() != null) {
                            designInfo.put("url", design.getUrls().getEditUrl());
                        }

                        if (design.getThumbnail() != null) {
                            Map<String, Object> thumbnailInfo = new HashMap<>();
                            thumbnailInfo.put("url", design.getThumbnail().getUrl());
                            thumbnailInfo.put("width", design.getThumbnail().getWidth());
                            thumbnailInfo.put("height", design.getThumbnail().getHeight());
                            designInfo.put("thumbnail", thumbnailInfo);
                        }

                        designInfo.put("created_at", design.getCreatedAt());
                        designInfo.put("updated_at", design.getUpdatedAt());
                        designInfo.put("page_count", design.getPageCount());

                        return designInfo;
                    })
                    .toList()
                );
            }

            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", e.getStatusCode().value());
            responseDetails.put("status", e.getStatusText());
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("errorBody", e.getResponseBodyAsString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("error", e.getClass().getSimpleName());
            responseDetails.put("message", e.getMessage());
            responseDetails.put("timestamp", Instant.now().toString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    @GetMapping("/design/{designId}")
    public ResponseEntity<Map<String, Object>> testGetDesign(@PathVariable String designId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            result.put("error", "Not authenticated");
            result.put("message", "Please connect to Canva first");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // Prepare request details
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "GET");
        requestDetails.put("endpoint", "https://api.canva.com/rest/v1/designs/" + designId);
        requestDetails.put("timestamp", Instant.now().toString());
        requestDetails.put("authentication", "Bearer token (from session)");
        requestDetails.put("parameters", Map.of("designId", designId));

        result.put("request", requestDetails);

        try {
            // Create API client with session token
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            DesignApi designApi = new DesignApi(apiClient);

            // Make the API call
            long startTime = System.currentTimeMillis();
            GetDesignResponse design = designApi.getDesign(designId);
            long duration = System.currentTimeMillis() - startTime;

            // Prepare response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", 200);
            responseDetails.put("status", "OK");
            responseDetails.put("duration", duration + "ms");
            responseDetails.put("timestamp", Instant.now().toString());

            // Response body
            Map<String, Object> body = new HashMap<>();
            body.put("design", design.getDesign());

            responseDetails.put("body", body);
            result.put("response", responseDetails);
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", e.getStatusCode().value());
            responseDetails.put("status", e.getStatusText());
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("errorBody", e.getResponseBodyAsString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("error", e.getClass().getSimpleName());
            responseDetails.put("message", e.getMessage());
            responseDetails.put("timestamp", Instant.now().toString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    @PostMapping("/brand-templates")
    public ResponseEntity<Map<String, Object>> testListBrandTemplates(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String ownership,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            result.put("error", "Not authenticated");
            result.put("message", "Please connect to Canva first");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // Parse ownership parameter
        OwnershipType ownershipType = null;
        if (ownership != null && !ownership.isEmpty() && !ownership.equals("any")) {
            try {
                ownershipType = OwnershipType.fromValue(ownership);
            } catch (IllegalArgumentException e) {
                result.put("error", "Invalid ownership value");
                result.put("message", "Ownership must be 'any', 'owned', or 'shared'");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        }

        // Prepare request details
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "GET");
        requestDetails.put("endpoint", "https://api.canva.com/rest/v1/brand-templates");
        requestDetails.put("timestamp", Instant.now().toString());
        requestDetails.put("authentication", "Bearer token (from session)");
        requestDetails.put("parameters", Map.of(
            "query", query != null ? query : "null (not specified)",
            "continuation", "null (first page)",
            "ownership", ownership != null && !ownership.equals("any") ? ownership : "null (all templates)",
            "sortBy", "null (default sort)",
            "dataset", "null (not specified)"
        ));

        result.put("request", requestDetails);

        try {
            // Create API client with session token
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            BrandTemplateApi brandTemplateApi = new BrandTemplateApi(apiClient);

            // Make the API call with query and ownership parameters
            long startTime = System.currentTimeMillis();
            ListBrandTemplatesResponse templates = brandTemplateApi.listBrandTemplates(query, null, ownershipType, null, null);
            long duration = System.currentTimeMillis() - startTime;

            // Prepare response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", 200);
            responseDetails.put("status", "OK");
            responseDetails.put("duration", duration + "ms");
            responseDetails.put("timestamp", Instant.now().toString());

            // Response body
            Map<String, Object> body = new HashMap<>();
            body.put("items_count", templates.getItems() != null ? templates.getItems().size() : 0);
            body.put("has_more", templates.getContinuation() != null);
            body.put("continuation", templates.getContinuation());
            body.put("items", templates.getItems());

            responseDetails.put("body", body);
            result.put("response", responseDetails);
            result.put("success", true);

            // Extract template info for display (thumbnails, titles, etc.)
            if (templates.getItems() != null) {
                result.put("templates", templates.getItems().stream()
                    .map(template -> {
                        Map<String, Object> templateInfo = new HashMap<>();
                        templateInfo.put("id", template.getId());
                        templateInfo.put("title", template.getTitle() != null ? template.getTitle() : "Untitled");
                        templateInfo.put("view_url", template.getViewUrl());
                        templateInfo.put("create_url", template.getCreateUrl());

                        if (template.getThumbnail() != null) {
                            Map<String, Object> thumbnailInfo = new HashMap<>();
                            thumbnailInfo.put("url", template.getThumbnail().getUrl());
                            thumbnailInfo.put("width", template.getThumbnail().getWidth());
                            thumbnailInfo.put("height", template.getThumbnail().getHeight());
                            templateInfo.put("thumbnail", thumbnailInfo);
                        }

                        templateInfo.put("created_at", template.getCreatedAt());
                        templateInfo.put("updated_at", template.getUpdatedAt());

                        return templateInfo;
                    })
                    .toList()
                );
            }

            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", e.getStatusCode().value());
            responseDetails.put("status", e.getStatusText());
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("errorBody", e.getResponseBodyAsString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("error", e.getClass().getSimpleName());
            responseDetails.put("message", e.getMessage());
            responseDetails.put("timestamp", Instant.now().toString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    @GetMapping("/brand-template/{brandTemplateId}")
    public ResponseEntity<Map<String, Object>> testGetBrandTemplate(@PathVariable String brandTemplateId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            result.put("error", "Not authenticated");
            result.put("message", "Please connect to Canva first");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // Prepare request details
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "GET");
        requestDetails.put("endpoint", "https://api.canva.com/rest/v1/brand-templates/" + brandTemplateId);
        requestDetails.put("timestamp", Instant.now().toString());
        requestDetails.put("authentication", "Bearer token (from session)");
        requestDetails.put("parameters", Map.of("brandTemplateId", brandTemplateId));

        result.put("request", requestDetails);

        try {
            // Create API client with session token
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            BrandTemplateApi brandTemplateApi = new BrandTemplateApi(apiClient);

            // Make the API call
            long startTime = System.currentTimeMillis();
            GetBrandTemplateResponse template = brandTemplateApi.getBrandTemplate(brandTemplateId);
            long duration = System.currentTimeMillis() - startTime;

            // Prepare response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", 200);
            responseDetails.put("status", "OK");
            responseDetails.put("duration", duration + "ms");
            responseDetails.put("timestamp", Instant.now().toString());

            // Response body
            Map<String, Object> body = new HashMap<>();
            body.put("brand_template", template.getBrandTemplate());

            responseDetails.put("body", body);
            result.put("response", responseDetails);
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", e.getStatusCode().value());
            responseDetails.put("status", e.getStatusText());
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("errorBody", e.getResponseBodyAsString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("error", e.getClass().getSimpleName());
            responseDetails.put("message", e.getMessage());
            responseDetails.put("timestamp", Instant.now().toString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    @GetMapping("/brand-template-dataset/{brandTemplateId}")
    public ResponseEntity<Map<String, Object>> testGetBrandTemplateDataset(@PathVariable String brandTemplateId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            result.put("error", "Not authenticated");
            result.put("message", "Please connect to Canva first");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // Prepare request details
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", "GET");
        requestDetails.put("endpoint", "https://api.canva.com/rest/v1/brand-templates/" + brandTemplateId + "/dataset");
        requestDetails.put("timestamp", Instant.now().toString());
        requestDetails.put("authentication", "Bearer token (from session)");
        requestDetails.put("parameters", Map.of("brandTemplateId", brandTemplateId));

        result.put("request", requestDetails);

        try {
            // Create API client with session token
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            // Make the API call directly using RestClient to get raw JSON
            long startTime = System.currentTimeMillis();
            String rawResponse = apiClient.getRestClient()
                .get()
                .uri(baseUrl + "/v1/brand-templates/" + brandTemplateId + "/dataset")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);
            long duration = System.currentTimeMillis() - startTime;

            // Prepare response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", 200);
            responseDetails.put("status", "OK");
            responseDetails.put("duration", duration + "ms");
            responseDetails.put("timestamp", Instant.now().toString());

            // Parse the raw JSON to Map
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = objectMapper.readValue(rawResponse, Map.class);

            responseDetails.put("body", bodyMap);
            result.put("response", responseDetails);
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            e.printStackTrace();
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", e.getStatusCode().value());
            responseDetails.put("status", e.getStatusText());
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("errorBody", e.getResponseBodyAsString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            e.printStackTrace();
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("error", e.getClass().getSimpleName());
            responseDetails.put("message", e.getMessage());
            responseDetails.put("timestamp", Instant.now().toString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    @PostMapping("/brand-kits")
    public ResponseEntity<Map<String, Object>> testListBrandKits(
            @RequestParam(required = false) Integer limit,
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
            com.example.demo.canva.privateclient.ApiClient apiClient = new com.example.demo.canva.privateclient.ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            // Create the API instance
            BrandKitApi brandKitApi = new BrandKitApi(apiClient);

            // Store request details
            Map<String, Object> requestDetails = new HashMap<>();
            requestDetails.put("method", "GET");
            requestDetails.put("endpoint", "/v1/internal/brand-kits");
            Map<String, Object> params = new HashMap<>();
            if (limit != null) {
                params.put("limit", limit);
            }
            requestDetails.put("parameters", params);
            requestDetails.put("headers", Map.of("Authorization", "Bearer ***"));
            result.put("request", requestDetails);

            // Make the API call
            long startTime = System.currentTimeMillis();
            com.example.demo.canva.privatemodel.ListBrandKitsResponse brandKitsResponse =
                brandKitApi.listBrandKitsInternal(null, limit);
            long duration = System.currentTimeMillis() - startTime;

            // Prepare response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", 200);
            responseDetails.put("status", "OK");
            responseDetails.put("duration", duration + "ms");
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("body", brandKitsResponse);

            result.put("response", responseDetails);
            result.put("success", true);
            result.put("brandKits", brandKitsResponse.getItems());

            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            e.printStackTrace();
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", e.getStatusCode().value());
            responseDetails.put("status", e.getStatusText());
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("errorBody", e.getResponseBodyAsString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            e.printStackTrace();
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("error", e.getClass().getSimpleName());
            responseDetails.put("message", e.getMessage());
            responseDetails.put("timestamp", Instant.now().toString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    @PostMapping("/folders/{folderId}/items")
    public ResponseEntity<Map<String, Object>> testListFolderItems(
            @PathVariable String folderId,
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
            // Store request details
            Map<String, Object> requestDetails = new HashMap<>();
            requestDetails.put("method", "GET");
            requestDetails.put("endpoint", "/v1/folders/" + folderId + "/items");
            requestDetails.put("parameters", Map.of("folderId", folderId));
            requestDetails.put("headers", Map.of("Authorization", "Bearer ***"));
            result.put("request", requestDetails);

            // Make the API call using raw HTTP client to avoid deserialization issues
            long startTime = System.currentTimeMillis();
            org.springframework.web.client.RestClient restClient = org.springframework.web.client.RestClient.create();
            String responseBody = restClient.get()
                    .uri(baseUrl + "/v1/folders/" + folderId + "/items")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);
            long duration = System.currentTimeMillis() - startTime;

            // Parse the JSON manually
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> jsonResponse = objectMapper.readValue(responseBody, Map.class);

            // Prepare response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", 200);
            responseDetails.put("status", "OK");
            responseDetails.put("duration", duration + "ms");
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("body", jsonResponse);

            result.put("response", responseDetails);
            result.put("success", true);
            result.put("items", jsonResponse.get("items"));

            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            e.printStackTrace();
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", e.getStatusCode().value());
            responseDetails.put("status", e.getStatusText());
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("errorBody", e.getResponseBodyAsString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            e.printStackTrace();
            // Prepare error response details
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("error", e.getClass().getSimpleName());
            responseDetails.put("message", e.getMessage());
            responseDetails.put("timestamp", Instant.now().toString());

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }

    @PostMapping("/folders/move")
    public ResponseEntity<Map<String, Object>> testMoveFolder(
            @RequestBody Map<String, String> requestBody,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null || accessToken.isEmpty()) {
            result.put("error", "Not authenticated");
            result.put("message", "Please connect to Canva first");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // Extract parameters from request body
        String itemId = requestBody.get("itemId");
        String toFolderId = requestBody.get("toFolderId");

        if (itemId == null || itemId.isEmpty()) {
            result.put("error", "Missing required parameter: itemId");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        if (toFolderId == null || toFolderId.isEmpty()) {
            result.put("error", "Missing required parameter: toFolderId");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        try {
            // Store request details
            Map<String, Object> requestDetails = new HashMap<>();
            requestDetails.put("method", "POST");
            requestDetails.put("endpoint", "/v1/folders/move");
            requestDetails.put("body", Map.of(
                    "item_id", itemId,
                    "to_folder_id", toFolderId
            ));
            requestDetails.put("headers", Map.of("Authorization", "Bearer ***"));
            result.put("request", requestDetails);

            // Call the Canva API
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);
            FolderApi folderApi = new FolderApi(apiClient);

            MoveFolderItemRequest moveFolderItemRequest = new MoveFolderItemRequest();
            moveFolderItemRequest.setItemId(itemId);
            moveFolderItemRequest.setToFolderId(toFolderId);

            long startTime = System.currentTimeMillis();
            folderApi.moveFolderItem(moveFolderItemRequest);
            long duration = System.currentTimeMillis() - startTime;

            // Prepare response details (204 No Content response)
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", 204);
            responseDetails.put("status", "No Content");
            responseDetails.put("duration", duration + "ms");
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("body", null);

            result.put("response", responseDetails);
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (RestClientResponseException e) {
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", e.getStatusCode().value());
            responseDetails.put("status", e.getStatusText());
            responseDetails.put("duration", "N/A");
            responseDetails.put("timestamp", Instant.now().toString());

            try {
                responseDetails.put("body", new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        e.getResponseBodyAsString(), Map.class));
            } catch (Exception ex) {
                responseDetails.put("body", Map.of("error", e.getResponseBodyAsString()));
            }

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "API call failed with status " + e.getStatusCode().value());

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (Exception e) {
            Map<String, Object> responseDetails = new HashMap<>();
            responseDetails.put("statusCode", null);
            responseDetails.put("status", "ERROR");
            responseDetails.put("duration", "N/A");
            responseDetails.put("timestamp", Instant.now().toString());
            responseDetails.put("body", Map.of("error", e.getMessage()));

            result.put("response", responseDetails);
            result.put("success", false);
            result.put("error", "Unexpected error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }
}
