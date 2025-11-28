package com.example.demo.config;

import com.example.demo.canva.client.ApiClient;
import com.example.demo.canva.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CanvaApiConfig {

    @Value("${canva.api.base-url:https://api.canva.com/rest}")
    private String baseUrl;

    @Bean
    public ApiClient canvaApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);

        // Note: Authentication is set dynamically per-request using session tokens
        // See ApiTestController for example of how to add bearer token per request

        return apiClient;
    }

    @Bean
    public UserApi userApi(ApiClient apiClient) {
        return new UserApi(apiClient);
    }

    @Bean
    public DesignApi designApi(ApiClient apiClient) {
        return new DesignApi(apiClient);
    }

    @Bean
    public AssetApi assetApi(ApiClient apiClient) {
        return new AssetApi(apiClient);
    }

    @Bean
    public FolderApi folderApi(ApiClient apiClient) {
        return new FolderApi(apiClient);
    }

    @Bean
    public ExportApi exportApi(ApiClient apiClient) {
        return new ExportApi(apiClient);
    }

    @Bean
    public BrandTemplateApi brandTemplateApi(ApiClient apiClient) {
        return new BrandTemplateApi(apiClient);
    }

    @Bean
    public AutofillApi autofillApi(ApiClient apiClient) {
        return new AutofillApi(apiClient);
    }

    @Bean
    public CommentApi commentApi(ApiClient apiClient) {
        return new CommentApi(apiClient);
    }

    @Bean
    public DesignImportApi designImportApi(ApiClient apiClient) {
        return new DesignImportApi(apiClient);
    }

    @Bean
    public OauthApi oauthApi(ApiClient apiClient) {
        return new OauthApi(apiClient);
    }
}
