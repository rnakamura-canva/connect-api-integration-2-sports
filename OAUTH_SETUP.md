# Canva OAuth Setup Guide

This guide explains how to configure OAuth authentication for your Canva Connect API integration.

## Prerequisites

1. A Canva account
2. Access to the [Canva Developer Portal](https://www.canva.com/developers)
3. A registered Canva Connect integration

## Step 1: Create a Canva Integration

1. Go to the [Canva Developer Portal](https://www.canva.com/developers)
2. Click "Create an app" or select your existing app
3. Navigate to the "Authentication" section
4. Note your **Client ID** and **Client Secret**

## Step 2: Configure Redirect URI

In your Canva app settings, add the following redirect URI:

```
http://127.0.0.1:8080/oauth/redirect
```

**Important:** The redirect URI in your Canva app settings must exactly match this URL.

## Step 3: Configure Scopes

Enable the following scopes in your Canva app settings (or adjust based on your needs):

- `asset:read` - Read assets
- `asset:write` - Create and upload assets
- `design:content:read` - Read design content
- `design:content:write` - Modify design content
- `design:meta:read` - Read design metadata
- `folder:read` - Read folders
- `folder:write` - Create and modify folders
- `profile:read` - Read user profile

## Step 4: Set Environment Variables

You can configure your credentials using environment variables:

### On macOS/Linux:

```bash
export CANVA_CLIENT_ID="your_client_id_here"
export CANVA_CLIENT_SECRET="your_client_secret_here"
```

### On Windows (PowerShell):

```powershell
$env:CANVA_CLIENT_ID="your_client_id_here"
$env:CANVA_CLIENT_SECRET="your_client_secret_here"
```

### On Windows (Command Prompt):

```cmd
set CANVA_CLIENT_ID=your_client_id_here
set CANVA_CLIENT_SECRET=your_client_secret_here
```

## Step 5: Or Edit application.properties

Alternatively, you can directly edit `src/main/resources/application.properties`:

```properties
# Replace the placeholder values
canva.oauth.client-id=your_actual_client_id
canva.oauth.client-secret=your_actual_client_secret
```

**Warning:** Don't commit your credentials to version control! Add `application.properties` to `.gitignore` if it contains real credentials.

## Step 6: Adjust Scopes (Optional)

If you need different scopes, edit `application.properties`:

```properties
canva.oauth.scopes=asset:read,design:meta:read,profile:read
```

Make sure the scopes match those enabled in your Canva app settings.

## Step 7: Start the Application

```bash
mvn spring-boot:run
```

Or if using an IDE, run the `Application` class.

## Step 8: Test the OAuth Flow

1. Open your browser and go to: `http://127.0.0.1:8080`
2. Click the "Connect to Canva" button
3. You'll be redirected to Canva's authorization page
4. Log in to Canva (if not already logged in)
5. Approve the permissions
6. You'll be redirected back to your application
7. You should see a success page with your access token information

## Troubleshooting

### "Invalid redirect_uri" Error

- Make sure the redirect URI in your Canva app settings exactly matches: `http://127.0.0.1:8080/oauth/redirect`
- Note: `127.0.0.1` is different from `localhost`

### "Invalid client" Error

- Check that your Client ID is correct
- Verify your Client Secret is correct
- Ensure there are no extra spaces or characters

### "Access denied" Error

- Make sure you approved the permissions on the Canva authorization page
- Check that the requested scopes are enabled in your Canva app settings

### "Invalid scope" Error

- Verify that all scopes in `application.properties` are enabled in your Canva app settings
- Scopes must match exactly (case-sensitive)

## OAuth Flow Details

This implementation uses the OAuth 2.0 Authorization Code flow with PKCE (Proof Key for Code Exchange):

1. **User clicks "Connect to Canva"** → Generates PKCE challenge
2. **Redirect to Canva** → User authorizes the app
3. **Canva redirects back** → With authorization code
4. **Token exchange** → Exchange code for access token using PKCE verifier
5. **Store token** → Access token stored in session
6. **API calls** → Use access token to call Canva APIs

## Token Management

- Access tokens are stored in the HTTP session
- Access tokens expire after a certain period (check `expires_in` from token response)
- Refresh tokens are also stored and can be used to get new access tokens
- Logout clears the session and removes tokens

## Security Notes

- Never commit credentials to version control
- Use environment variables for production deployments
- The state parameter protects against CSRF attacks
- PKCE adds additional security for the OAuth flow
- Tokens are stored in the server session (consider more secure storage for production)

## Next Steps

After successful authentication:

1. Your access token is available in the session
2. You can now make API calls to Canva
3. Use the generated API classes in `com.example.demo.canva.api`
4. See `CANVA_API_USAGE.md` for API usage examples
