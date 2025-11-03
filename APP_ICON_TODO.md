# App Icon Integration Notes

## Icon Details
- **File**: Chain_App_Icon.png
- **Resolution**: 1080 x 1080 pixels
- **Format**: PNG, 8-bit RGB
- **Size**: 151 KB

## TODO: Generate Proper Android Icon Assets

The icon needs to be converted to proper Android launcher icons. You should:

1. **Use Android Studio's Image Asset Studio**:
   - Right-click `res` folder → New → Image Asset
   - Select "Launcher Icons (Adaptive and Legacy)"
   - Choose the Chain_App_Icon.png as source
   - Generate all required densities

2. **Or use an online tool**:
   - [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/)
   - Upload Chain_App_Icon.png
   - Download generated mipmap folders
   - Replace the placeholder icons in `app/src/main/res/mipmap-*/`

## Required Icon Sizes

The tool will generate:
- mipmap-mdpi (48x48)
- mipmap-hdpi (72x72)
- mipmap-xhdpi (96x96)
- mipmap-xxhdpi (144x144)
- mipmap-xxxhdpi (192x192)
- Adaptive icon (foreground + background)

Currently using placeholder icons - replace after build verification.
