# Canva Scopes Configuration Guide

## The Issue

If you see the error **"Requested scopes are not allowed for this client"**, it means you're requesting OAuth scopes that haven't been enabled in your Canva Developer Portal app.

## How to Fix

### 1. Access Your Canva App Settings

1. Go to [Canva Developer Portal](https://www.canva.com/developers/apps)
2. Sign in with your Canva account
3. Select your app from the list

### 2. Enable Required Scopes

1. In your app settings, find the **"Scopes & permissions"** section
2. Check the boxes for all scopes you need:
   - `profile:read` - Read user profile information
   - `design:meta:read` - Read design metadata (title, ID, thumbnails)
   - `design:content:read` - Read design content details
   - `asset:read` - Read user assets
   - `folder:read` - Read user folders
   - `design:content:write` - Create/modify designs
   - `design:permission:write` - Manage design permissions
   - And others as needed

3. **Save your changes**

### 3. Update application.properties

After enabling scopes in Canva, update your `application.properties`:

```properties
# Use comma-separated format (code automatically converts to space-separated)
canva.oauth.scopes=profile:read,design:meta:read,asset:read
```

### 4. Restart Your Application

Stop and restart your Spring Boot application for the changes to take effect.

## Common Scope Combinations

### Basic Profile Access
```properties
canva.oauth.scopes=profile:read
```

### Read Designs and Assets
```properties
canva.oauth.scopes=profile:read,design:meta:read,asset:read
```

### Full Read Access
```properties
canva.oauth.scopes=profile:read,design:meta:read,design:content:read,asset:read,folder:read
```

### Read and Write Access
```properties
canva.oauth.scopes=profile:read,design:meta:read,design:content:read,design:content:write,asset:read
```

## Important Notes

1. **Portal First**: Always enable scopes in the Canva Developer Portal BEFORE adding them to your application
2. **Match Exactly**: The scopes in your code must match what's enabled in the portal
3. **Case Sensitive**: Scope names are case-sensitive (use lowercase with colons)
4. **Format**: Use comma-separated format in application.properties - the code will convert it properly

## Troubleshooting

### Error persists after enabling scopes?
- Clear your browser cache and cookies
- Try the authorization flow in an incognito/private window
- Wait a few minutes (portal changes may take time to propagate)
- Verify the scope names are spelled correctly

### Which scopes do I need?
Check the [Canva API Documentation](https://www.canva.dev/docs/connect/authentication/#scopes) for:
- List of all available scopes
- What permissions each scope grants
- Which API endpoints require which scopes

## Technical Details

The application automatically converts comma-separated scopes to space-separated format as required by OAuth 2.0 specification:

```
application.properties: profile:read,design:meta:read,asset:read
      ↓
OAuth URL parameter: scope=profile:read%20design:meta:read%20asset:read
```

This happens in `CanvaOAuthService.buildAuthorizationUrl()` at line 69.
