# Canva Sports - Connect API Integration (Java)

A Spring Boot application demonstrating integration with the Canva Connect API. This sample application showcases how to use Canva's APIs to create designs with autofill functionality, featuring a soccer team management interface.

## Overview

Canva Sports is a web application that allows users to:
- Connect to their Canva account using OAuth 2.0
- Manage soccer team rosters
- Create customized soccer formation designs using Canva Brand Templates
- Autofill team data into Canva designs
- Test various Canva API endpoints

## Features

### OAuth Authentication
- Secure OAuth 2.0 integration with Canva
- Session-based authentication
- User profile display with display name
- Connect/Disconnect functionality

### Soccer Team Management
- **Team Page**: Create and edit soccer team rosters with 11 players
  - Goalkeeper, Defenders, Midfielders, Forwards
  - Session-based data persistence
- **Formations Page**: Visualize team formations on an interactive soccer field
  - Visual soccer field with player positions
  - Multiple formation templates (4-3-3, 4-4-2, 3-5-2)
  - One-click design creation using Canva's Autofill API

### API Testing
- Interactive API testing interface
- Test Canva Connect API endpoints:
  - User Profile (`/v1/users/me/profile`)
  - List Designs (`/v1/designs`)
  - Get Design by ID
  - List Brand Templates (`/v1/brand-templates`)
  - Get Brand Template Dataset
  - List Folder Items
  - Move Folder Items
- Detailed request/response inspection

## Requirements

- **Java 21** (or later)
- **Maven 3.6+**
- **Canva Developer Account** with Connect API access
- **Brand Templates** configured in Canva with appropriate datasets

## Setup

### 1. Canva App Configuration

1. Create a Canva app at https://www.canva.com/developers/apps
2. Configure OAuth settings:
   - **Redirect URI**: `http://localhost:8080/oauth/redirect`
   - **Scopes**:
     - `design:content:read`
     - `design:content:write`
     - `asset:read`
     - `asset:write`
     - `brandtemplate:content:read`
     - `profile:read`
3. Note your **Client ID** and **Client Secret**

### 2. Environment Configuration

Create a `.env` file in the project root or set environment variables:

```bash
CANVA_CLIENT_ID=your_client_id_here
CANVA_CLIENT_SECRET=your_client_secret_here
CANVA_REDIRECT_URI=http://localhost:8080/oauth/redirect
```

Or configure in `src/main/resources/application.properties`:

```properties
canva.oauth.client-id=${CANVA_CLIENT_ID}
canva.oauth.client-secret=${CANVA_CLIENT_SECRET}
canva.oauth.redirect-uri=${CANVA_REDIRECT_URI}
canva.oauth.authorize-url=https://www.canva.com/api/oauth/authorize
canva.oauth.token-url=https://api.canva.com/rest/v1/oauth/token
canva.api.base-url=https://api.canva.com/rest
```

### 3. Brand Template Setup

To use the soccer formations feature:

1. Create a Brand Template in Canva
2. Add dataset fields matching the team data:
   - `teamName`
   - `goalkeeper`
   - `centerBack1`, `centerBack2`
   - `leftBack`, `rightBack`
   - `midfielder1`, `midfielder2`, `midfielder3`
   - `leftWing`, `rightWing`, `striker`
3. Note the Brand Template ID

## Build and Run

### Using Maven Wrapper (Recommended)

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

### Using Installed Maven

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start at **http://localhost:8080**

## Usage

### 1. Connect to Canva

1. Navigate to http://localhost:8080
2. Click "Connect to Canva" in the top right corner
3. Authorize the application in Canva
4. You'll be redirected back with your profile displayed

### 2. Manage Your Team

1. Go to **Soccer > Team** in the left menu
2. Enter player names for each position
3. Click "Save Team" to store in session

### 3. Create Designs

1. Go to **Soccer > Formations**
2. Choose a formation (4-3-3, 4-4-2, or 3-5-2)
3. Select or enter a Brand Template ID
4. Click "Create Design with Autofill"
5. The application will:
   - Fetch the Brand Template dataset
   - Map your team data to the template fields
   - Create a design using Canva's Autofill API
   - Open the design in Canva

### 4. Test API Endpoints

1. Go to **API Testing** in the left menu
2. Select an API endpoint to test
3. Enter required parameters
4. Click "Send Request"
5. View detailed request/response information

## Project Structure

```
src/main/java/com/example/demo/
├── config/
│   ├── CanvaApiConfig.java          # API client configuration
│   └── WebConfig.java                # Web MVC configuration
├── controller/
│   ├── ApiTestController.java        # API testing endpoints
│   ├── OAuthController.java          # OAuth flow handlers
│   ├── PageController.java           # Page route handlers
│   └── SoccerController.java         # Soccer features
├── service/
│   └── CanvaService.java             # Canva API service wrapper
└── DemoApplication.java              # Spring Boot main class

src/main/resources/
├── templates/
│   ├── home.html                     # Landing page
│   ├── test.html                     # API testing interface
│   ├── oauth-success.html            # OAuth success page
│   ├── oauth-error.html              # OAuth error page
│   └── soccer/
│       ├── team.html                 # Team management
│       └── formations.html           # Formation designer
├── static/                           # Static assets
└── application.properties            # Application configuration
```

## API Client Generation

The project uses OpenAPI Generator to create type-safe Java clients for the Canva Connect API:

- **Public API**: Generated from `openapi/canva-public-openapi-spec.yaml`
- **Private API**: Generated from `openapi/canva-private-openapi-spec.yaml` (internal endpoints)

Generated code is placed in `target/generated-sources/openapi/`

To regenerate clients:
```bash
./mvnw clean generate-sources
```

## Dependencies

### Core
- **Spring Boot 3.4.0**
- **Java 21**
- **Thymeleaf** (server-side templating)
- **Spring Web** (REST controllers)

### Code Generation
- **OpenAPI Generator 7.8.0** (Maven plugin)
- **Jackson** (JSON processing)

### Testing
- **JUnit 5** (included in spring-boot-starter-test)
- **Mockito** (included in spring-boot-starter-test)

## Run Tests

```bash
./mvnw test
```

## Development

### Hot Reload

The application uses Spring Boot DevTools for automatic restart during development. Make changes to Java files, and the application will automatically restart.

### Port Configuration

The default port is 8080. To change it, add to `application.properties`:

```properties
server.port=8081
```

### Session Configuration

Sessions are stored in-memory by default. For production, consider using Redis or a database-backed session store.

## Architecture

### Authentication Flow

1. User clicks "Connect to Canva"
2. Application redirects to Canva's OAuth authorize endpoint
3. User authorizes the app in Canva
4. Canva redirects back with an authorization code
5. Application exchanges code for access token
6. Access token is stored in HTTP session
7. Token is used for subsequent API calls

### API Client Pattern

The application creates authenticated API clients per-request:

```java
// Get access token from session
String accessToken = session.getAttribute("access_token");

// Create authenticated client
ApiClient apiClient = new ApiClient();
apiClient.setBasePath("https://api.canva.com/rest");
apiClient.addDefaultHeader("Authorization", "Bearer " + accessToken);

// Use specific API
UserApi userApi = new UserApi(apiClient);
UserProfileResponse profile = userApi.getUserProfile();
```

### Autofill Workflow

1. User configures team data
2. Application fetches Brand Template dataset definition
3. Maps team data to dataset fields (case-insensitive matching)
4. Creates autofill job via `/v1/autofills` endpoint
5. Polls job status until complete
6. Redirects user to the created design in Canva

## Troubleshooting

### Port Already in Use

```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

### OAuth Redirect URI Mismatch

Ensure your Canva app's redirect URI exactly matches:
```
http://localhost:8080/oauth/redirect
```

### API Authentication Errors

1. Verify your Client ID and Client Secret are correct
2. Check that required scopes are configured in your Canva app
3. Ensure access token is being stored in session

### Build Errors

```bash
# Clean and rebuild
./mvnw clean install -U
```

## Contributing

This is a sample application for demonstration purposes. Feel free to fork and customize for your needs.

## License

This project is provided as-is for educational and demonstration purposes.

## Resources

- [Canva Developers Documentation](https://www.canva.com/developers/docs)
- [Canva Connect API Reference](https://www.canva.com/developers/docs/connect-api)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [OpenAPI Generator](https://openapi-generator.tech/)

## Support

For issues with:
- **Canva API**: Contact Canva Developer Support
- **This Sample App**: Create an issue in the repository
