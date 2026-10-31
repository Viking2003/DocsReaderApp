# APK Deployment Guide

This guide explains how to deploy APK files to GitHub for easy distribution to users.

## 📦 Current Setup

### APK Location
The APK is stored in the `apks/` folder which is tracked in Git:
- `apks/app-release.apk` - Production release APK
- `apks/app-debug.apk` - Debug/testing APK (optional)

### Build Scripts
- **Windows**: `build-apk.bat` - Builds and copies APK to apks folder
- **Linux/Mac**: `build-apk.sh` - Builds and copies APK to apks folder

### Usage
```bash
# Windows
build-apk.bat release

# Linux/Mac
./build-apk.sh release
```

## 🚀 Deployment Methods

### Method 1: Direct APK in Repository (Current Setup)

**Pros:**
- Simple and straightforward
- APK is always available in the repository
- No additional setup required
- Users can download directly from the repository

**Cons:**
- Increases repository size (APK files are ~50MB)
- Not ideal for frequent updates
- Each APK version takes up space in Git history

**Instructions:**
1. Build the APK using the build script
2. Commit the changes:
   ```bash
   git add apks/app-release.apk
   git commit -m "Update APK to v1.0"
   git push origin main
   ```
3. Users can download from: `https://github.com/username/repo/raw/main/apks/app-release.apk`

### Method 2: GitHub Releases (Recommended for Production)

**Pros:**
- Professional distribution method
- Automatic versioning and changelog
- APK files don't bloat the repository
- Users can see release notes
- Better for multiple versions

**Cons:**
- Requires additional setup
- Need to manually create releases

**Instructions:**

1. **Build the APK:**
   ```bash
   # Windows
   build-apk.bat release

   # Linux/Mac
   ./build-apk.sh release
   ```

2. **Create a GitHub Release:**
   - Go to your repository on GitHub
   - Click "Releases" → "Create a new release"
   - Tag version: `v1.0`
   - Release title: `Version 1.0 - Initial Release`
   - Description: Add release notes
   - Attach the APK: `apks/app-release.apk`
   - Click "Publish release"

3. **Users can download from:**
   - `https://github.com/username/repo/releases/download/v1.0/app-release.apk`

### Method 3: GitHub Pages with Download Page

**Pros:**
- Custom download page
- Can show multiple APK versions
- Professional appearance
- Can include screenshots and installation instructions

**Cons:**
- Requires GitHub Pages setup
- More complex to maintain

**Instructions:**

1. Enable GitHub Pages in repository settings
2. Create a download page in `docs/download.html`
3. Add links to APK files from releases
4. Example HTML:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Download Docs Reader App</title>
</head>
<body>
    <h1>Download Docs Reader App</h1>
    <p>Latest Version: v1.0</p>
    <a href="https://github.com/username/repo/releases/download/v1.0/app-release.apk">
        <button>Download APK</button>
    </a>
</body>
</html>
```

## 🔄 Update Workflow

### When Updating the App:

1. **Update version in `app/build.gradle.kts`:**
   ```kotlin
   versionCode = 2
   versionName = "1.1"
   ```

2. **Build the new APK:**
   ```bash
   build-apk.bat release
   ```

3. **Commit and push:**
   ```bash
   git add .
   git commit -m "Release v1.1 - New features"
   git push origin main
   ```

4. **Create GitHub Release:**
   - Tag: `v1.1`
   - Title: `Version 1.1`
   - Attach: `apks/app-release.apk`
   - Add release notes

## 📊 Repository Size Management

### Keep Repository Small:
- Delete old APKs from `apks/` folder after creating releases
- Use GitHub Releases for version history
- Consider using Git LFS for large files if needed

### Clean Up Old APKs:
```bash
# Remove old APK from git history (use with caution)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch apks/app-release-v1.0.apk" \
  --prune-empty --tag-name-filter cat -- --all
```

## 🔐 Security Considerations

### Keystore Management:
- Never commit keystore files (already in `.gitignore`)
- Store keystore passwords in environment variables or secure storage
- Use different keystores for debug and release builds

### APK Signing:
- Release APK must be signed with your keystore
- Debug APK uses default debug keystore
- Keep keystore safe - losing it means you can't update the app

## 📝 Best Practices

1. **Version Control:**
   - Always increment `versionCode` and `versionName`
   - Use semantic versioning (MAJOR.MINOR.PATCH)
   - Create tags for each release

2. **Release Notes:**
   - Document new features
   - List bug fixes
   - Mention breaking changes
   - Include installation instructions

3. **Testing:**
   - Test APK on multiple devices before release
   - Verify installation works
   - Check all features work correctly

4. **Distribution:**
   - Consider using GitHub Releases for production
   - Keep direct APK in repo for quick access
   - Update README with latest download link

## 🎯 Quick Start for New Users

### For Users Wanting to Download:

1. **From Repository (Direct):**
   - Go to `apks/` folder in the repository
   - Download `app-release.apk`
   - Install on device

2. **From GitHub Releases:**
   - Go to Releases page
   - Download latest APK
   - Install on device

3. **From README:**
   - Click the download link in README
   - APK will download automatically
   - Install on device

## 📞 Support

For issues with APK deployment, please open an issue in the repository.
