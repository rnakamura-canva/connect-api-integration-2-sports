# Canva Connect API Integration Guide

This document explains how to use the generated Canva Connect API client classes in your application.

## Overview

The OpenAPI Generator has created Java client classes from the `openapi/spec.yml` file. These classes use **Spring's RestClient** (not OkHttp) for making HTTP requests.

## Generated Structure

### API Classes (in `com.example.demo.canva.api`)
- `UserApi` - User profile and capabilities
- `DesignApi` - Create and manage designs
- `AssetApi` - Manage assets (images, videos, etc.)
- `FolderApi` - Manage folders and list items
- `ExportApi` - Export designs
- `BrandTemplateApi` - Brand template operations
- `AutofillApi` - Autofill operations
- `CommentApi` - Design comments
- `DesignImportApi` - Import designs
- `OauthApi` - OAuth operations
- `ConnectApi` - Connect API utilities
- `ResizeApi` - Resize operations
- `AppApi` - App information

### Model Classes (in `com.example.demo.canva.model`)
All request/response DTOs are generated, including:
- `UserProfileResponse`
- `CreateDesignRequest`
- `CreateDesignResponse`
- `GetListDesignResponse`
- `Asset`
- `DesignSummary`
- And many more...

### Client Infrastructure (in `com.example.demo.canva.client`)
- `ApiClient` - Main client for making API calls (uses Spring RestClient)
- Authentication classes (OAuth, Bearer Token, etc.)
- Utility classes

## Configuration

### 1. Set up your access token

You can set the access token via environment variable:
```bash
export CANVA_ACCESS_TOKEN=your_access_token_here
```

Or set it directly in `application.properties`:
```properties
canva.api.access-token=your_access_token_here
```

### 2. Configuration Class

The `CanvaApiConfig` class is already set up to:
- Create an `ApiClient` bean with the base URL and authentication
- Create beans for all API classes (UserApi, DesignApi, etc.)

## Usage Examples

### Example 1: Get User Profile

```java
@RestController
public class MyController {

    @Autowired
    private UserApi userApi;

    @GetMapping("/profile")
    public UserProfileResponse getProfile() {
        return userApi.getUserProfile();
    }
}
```

### Example 2: List Designs

```java
@Service
public class MyService {

    @Autowired
    private DesignApi designApi;

    public List<DesignSummary> getUserDesigns() {
        GetListDesignResponse response = designApi.listDesigns(
            null,  // continuation token
            10,    // limit
            OwnershipType.OWNED,  // ownership type
            null,  // query
            SortByType.MODIFIED_DESCENDING  // sort by
        );
        return response.getItems();
    }
}
```

### Example 3: Create a New Design

```java
@Service
public class DesignService {

    @Autowired
    private DesignApi designApi;

    public CreateDesignResponse createBlankDesign() {
        CreateDesignRequest request = new CreateDesignRequest();
        request.setDesignType(DesignTypeCreateRequest.PRESENTATION);
        // Or set custom dimensions:
        // request.setWidth(1920);
        // request.setHeight(1080);

        return designApi.createDesign(request);
    }
}
```

### Example 4: Get Asset Details

```java
@Service
public class AssetService {

    @Autowired
    private AssetApi assetApi;

    public GetAssetResponse getAssetInfo(String assetId) {
        return assetApi.getAsset(assetId);
    }
}
```

### Example 5: Using the Service Layer

The included `CanvaService` provides a convenient wrapper:

```java
@RestController
@RequestMapping("/canva")
public class CanvaController {

    @Autowired
    private CanvaService canvaService;

    @GetMapping("/user")
    public UserProfileResponse getUser() {
        return canvaService.getUserProfile();
    }

    @GetMapping("/designs")
    public GetListDesignResponse listDesigns() {
        return canvaService.listDesigns(null, 10, null, null);
    }
}
```

## Authentication

### Bearer Token (OAuth)

The API client is configured to use Bearer Token authentication:

```java
ApiClient apiClient = new ApiClient();
apiClient.setBearerToken("your_access_token");
```

This is already configured in `CanvaApiConfig` using the `canva.api.access-token` property.

### Per-Request Authentication

You can also set authentication per API instance:

```java
UserApi userApi = new UserApi(apiClient);
// Use userApi with the configured authentication
```

## Available Endpoints

The generated APIs provide access to all Canva Connect API endpoints:

### User API
- `getUserProfile()` - Get user profile
- `getUserCapabilities()` - Get user capabilities

### Design API
- `listDesigns()` - List designs
- `getDesign(designId)` - Get design details
- `createDesign(request)` - Create new design
- `getDesignPages(designId)` - Get design pages

### Asset API
- `getAsset(assetId)` - Get asset details
- `uploadAsset()` - Upload new asset

### Folder API
- `getFolderItems(folderId)` - List folder contents
- `createFolder()` - Create new folder

And many more...

## Error Handling

All API methods throw `RestClientResponseException` on errors:

```java
try {
    UserProfileResponse profile = userApi.getUserProfile();
} catch (RestClientResponseException e) {
    System.err.println("API Error: " + e.getStatusCode());
    System.err.println("Response: " + e.getResponseBodyAsString());
}
```

## Testing the API

### Using the included controller

Start the application and test the endpoints:

```bash
# Get user profile
curl http://localhost:8080/api/canva/user/profile

# List designs
curl http://localhost:8080/api/canva/designs?limit=5

# Get specific design
curl http://localhost:8080/api/canva/designs/{designId}
```

### Direct API usage

You can also use the API classes directly without going through the controller:

```java
@SpringBootTest
class CanvaApiTest {

    @Autowired
    private UserApi userApi;

    @Test
    void testGetUserProfile() {
        UserProfileResponse profile = userApi.getUserProfile();
        assertNotNull(profile);
    }
}
```

## Regenerating the Client

If the OpenAPI spec changes, regenerate the client with:

```bash
mvn clean generate-sources
```

This will recreate all API and model classes based on the latest `openapi/spec.yml`.

## Notes

- The client uses **Spring RestClient** (not OkHttp) for HTTP requests
- All generated classes are in packages under `com.example.demo.canva`
- The client is configured as Spring beans and can be autowired
- Bearer token authentication is configured via properties
- All API responses are strongly typed with generated model classes
