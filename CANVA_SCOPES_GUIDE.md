# Canva OAuth Scopes Configuration Guide

## The "Requested scopes are not allowed" Error

This error occurs when your application requests OAuth scopes that haven't been enabled in your Canva Developer Portal integration settings.

## How to Fix

### Step 1: Go to Canva Developer Portal

1. Visit https://www.canva.com/developers
2. Click on your integration/app
3. Navigate to the **"Authentication"** or **"Scopes"** section

### Step 2: Enable Required Scopes

In the Canva Developer Portal, you need to **enable/check** the scopes you want to use. Common scopes include:

#### Basic Scopes (Start Here)
- ✅ **`profile:read`** - Read user profile information (usually enabled by default)

#### Design Scopes
- **`design:meta:read`** - Read design metadata (title, thumbnail, etc.)
- **`design:content:read`** - Read design content
- **`design:content:write`** - Create and modify designs
- **`design:permission:read`** - Read design permissions
- **`design:permission:write`** - Modify design permissions

#### Asset Scopes
- **`asset:read`** - Read assets (images, videos, etc.)
- **`asset:write`** - Upload and manage assets

#### Folder Scopes
- **`folder:read`** - Read folders
- **`folder:write`** - Create and manage folders

#### Brand Template Scopes
- **`brandtemplate:meta:read`** - Read brand template metadata
- **`brandtemplate:content:read`** - Read brand template content

#### Comment Scopes
- **`comment:read`** - Read comments
- **`comment:write`** - Create and manage comments

### Step 3: Update application.properties

After enabling scopes in the Canva Developer Portal, update your `application.properties` file to match:

```properties
# Example: Enable multiple scopes (comma-separated, no spaces)
canva.oauth.scopes=profile:read,design:meta:read,design:content:read,asset:read,folder:read
```

**IMPORTANT:** The scopes in `application.properties` MUST be a subset of the scopes enabled in your Canva app settings.

### Step 4: Restart Your Application

```bash
mvn spring-boot:run
```

### Step 5: Try Connecting Again

Go to http://127.0.0.1:8080 and click "Connect to Canva"

## Common Scope Combinations

### Minimal (Profile Only)
```properties
canva.oauth.scopes=profile:read
```
✅ Good for: Testing authentication, getting user info

### Read-Only Access
```properties
canva.oauth.scopes=profile:read,design:meta:read,asset:read,folder:read
```
✅ Good for: Viewing designs, assets, and folders

### Full Access
```properties
canva.oauth.scopes=profile:read,design:meta:read,design:content:read,design:content:write,asset:read,asset:write,folder:read,folder:write
```
✅ Good for: Full API integration with create/update capabilities

## Troubleshooting

### Issue: Still getting "scopes not allowed" error

**Solution:**
1. Double-check that ALL scopes in `application.properties` are enabled in Canva Developer Portal
2. Make sure there are no typos in scope names (they're case-sensitive)
3. Remove any spaces in the comma-separated list
4. Save changes in Canva Developer Portal before testing

### Issue: Don't know which scopes to enable

**Solution:**
1. Start with just `profile:read`
2. Add scopes as you need them for specific features
3. Check the [Canva API documentation](https://www.canva.dev/docs/connect/api-reference/) to see which scopes are required for each API endpoint

### Issue: Some scopes are grayed out or unavailable

**Solution:**
- Some scopes may require additional approval from Canva
- Contact Canva support if you need access to restricted scopes
- Make sure your integration type supports the scopes you need

## Scope Format

✅ **Correct:**
```properties
canva.oauth.scopes=profile:read,design:meta:read,asset:read
```

❌ **Incorrect (has spaces):**
```properties
canva.oauth.scopes=profile:read, design:meta:read, asset:read
```

❌ **Incorrect (wrong separator):**
```properties
canva.oauth.scopes=profile:read;design:meta:read;asset:read
```

## Security Note

⚠️ **Only request the scopes you actually need!**

Following the principle of least privilege:
- Don't request write access if you only need read access
- Don't request access to assets if you only work with designs
- Start minimal and add scopes as needed

## Quick Reference: Scope Requirements

| Feature | Required Scope(s) |
|---------|-------------------|
| Get user profile | `profile:read` |
| List designs | `design:meta:read` |
| Get design content | `design:content:read` |
| Create design | `design:content:write` |
| Upload asset | `asset:write` |
| Get asset | `asset:read` |
| List folders | `folder:read` |
| Create folder | `folder:write` |
| Add comments | `comment:write` |

## Next Steps

1. Enable `profile:read` in Canva Developer Portal (if not already enabled)
2. Test the connection with just this scope
3. Once working, enable additional scopes as needed
4. Update `application.properties` to match your enabled scopes
5. Test each new scope to ensure it works

## Need Help?

- Check [Canva Connect Documentation](https://www.canva.dev/docs/connect/)
- Review [API Reference](https://www.canva.dev/docs/connect/api-reference/)
- Contact [Canva Developer Support](https://www.canva.dev/docs/connect/support/)
