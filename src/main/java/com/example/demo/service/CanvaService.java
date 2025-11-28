package com.example.demo.service;

import com.example.demo.canva.api.*;
import com.example.demo.canva.client.ApiClient;
import com.example.demo.canva.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
public class CanvaService {

    @Value("${canva.api.base-url:https://api.canva.com/rest}")
    private String baseUrl;

    private final UserApi userApi;
    private final DesignApi designApi;
    private final AssetApi assetApi;
    private final FolderApi folderApi;

    public CanvaService(UserApi userApi, DesignApi designApi, AssetApi assetApi, FolderApi folderApi) {
        this.userApi = userApi;
        this.designApi = designApi;
        this.assetApi = assetApi;
        this.folderApi = folderApi;
    }

    /**
     * Get the access token from the current session
     */
    private String getAccessToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpSession session = attributes.getRequest().getSession(false);
            if (session != null) {
                return (String) session.getAttribute("access_token");
            }
        }
        return null;
    }

    /**
     * Get the current user's profile
     */
    public UserProfileResponse getUserProfile() {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Not authenticated");
            }

            // Create API client with session token
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(baseUrl);
            apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

            UserApi authenticatedUserApi = new UserApi(apiClient);
            return authenticatedUserApi.getUserProfile();
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Failed to get user profile: " + e.getMessage(), e);
        }
    }

    /**
     * Get user capabilities
     */
    public GetUserCapabilitiesResponse getUserCapabilities() {
        try {
            return userApi.getUserCapabilities();
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Failed to get user capabilities: " + e.getMessage(), e);
        }
    }

    /**
     * List designs for the authenticated user
     */
    public GetListDesignResponse listDesigns(String query, String continuation, OwnershipType ownership, SortByType sortBy) {
        try {
            return designApi.listDesigns(query, continuation, ownership, sortBy);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Failed to list designs: " + e.getMessage(), e);
        }
    }

    /**
     * Get a specific design by ID
     */
    public GetDesignResponse getDesign(String designId) {
        try {
            return designApi.getDesign(designId);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Failed to get design: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new design
     */
    public CreateDesignResponse createDesign(CreateDesignRequest request) {
        try {
            return designApi.createDesign(request);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Failed to create design: " + e.getMessage(), e);
        }
    }

    /**
     * Get pages of a design
     */
    public GetDesignPagesResponse getDesignPages(String designId, Integer offset, Integer limit) {
        try {
            return designApi.getDesignPages(designId, offset, limit);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Failed to get design pages: " + e.getMessage(), e);
        }
    }

    /**
     * List items in a folder
     */
    public ListFolderItemsResponse listFolderItems(String folderId, String continuation, List<FolderItemType> itemTypes, FolderItemSortBy sortBy) {
        try {
            return folderApi.listFolderItems(folderId, continuation, itemTypes, sortBy);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Failed to list folder items: " + e.getMessage(), e);
        }
    }

    /**
     * Get asset information
     */
    public GetAssetResponse getAsset(String assetId) {
        try {
            return assetApi.getAsset(assetId);
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Failed to get asset: " + e.getMessage(), e);
        }
    }
}
