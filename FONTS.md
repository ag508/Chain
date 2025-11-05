# Font Installation Guide

## Current Setup ✅

The app currently uses:
- **Roboto** (Android's default SansSerif) for all body text
- **Zen Dots** for branding (Chain logo, titles)

This works perfectly! Roboto is geometrically similar to Inter and provides excellent readability on Android devices.

---

## Optional: Adding Inter Fonts (Manual Installation)

If you prefer to use Inter fonts as specified in `chain-complete.html`, follow these steps:

### Step 1: Download Inter Fonts

**Option A: From Official GitHub Release (Recommended)**

```bash
# On your local machine (not in the container):
curl -L "https://github.com/rsms/inter/releases/download/v4.1/Inter-4.1.zip" -o Inter.zip
unzip Inter.zip -d InterFonts
cd InterFonts/Inter\ Desktop/
```

**Option B: From Google Fonts**

Visit: https://fonts.google.com/specimen/Inter
Click "Download family" button

### Step 2: Extract Required Font Files

You need these 5 font files:
- `Inter-Regular.ttf` (weight 400)
- `Inter-Medium.ttf` (weight 500)
- `Inter-SemiBold.ttf` (weight 600)
- `Inter-Bold.ttf` (weight 700)
- `Inter-ExtraBold.ttf` (weight 800)

### Step 3: Rename Files

Rename the files to match Android naming conventions (lowercase, underscores):

```bash
mv Inter-Regular.ttf inter_regular.ttf
mv Inter-Medium.ttf inter_medium.ttf
mv Inter-SemiBold.ttf inter_semibold.ttf
mv Inter-Bold.ttf inter_bold.ttf
mv Inter-ExtraBold.ttf inter_extrabold.ttf
```

### Step 4: Copy to Project

```bash
# Copy to the font resource directory
cp inter_*.ttf app/src/main/res/font/
```

### Step 5: Update Type.kt

Edit `app/src/main/java/com/chain/app/presentation/theme/Type.kt`:

```kotlin
// Change from:
val InterFontFamily = FontFamily.SansSerif

// To:
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),     // 400
    Font(R.font.inter_medium, FontWeight.Medium),      // 500
    Font(R.font.inter_semibold, FontWeight.SemiBold),  // 600
    Font(R.font.inter_bold, FontWeight.Bold),          // 700
    Font(R.font.inter_extrabold, FontWeight.ExtraBold) // 800
)
```

### Step 6: Rebuild

```bash
./gradlew clean assembleDebug
```

---

## Using WOFF2 Files (Advanced)

If you have WOFF2 files and want to convert them to TTF:

### Install fonttools:
```bash
pip3 install fonttools brotli
```

### Download and convert:
```bash
cd app/src/main/res/font

# Define font weights
declare -A fonts=(
  [400]="inter_regular"
  [500]="inter_medium"
  [600]="inter_semibold"
  [700]="inter_bold"
  [800]="inter_extrabold"
)

# Download WOFF2 files
for weight in "${!fonts[@]}"; do
    name="${fonts[$weight]}"
    url="https://cdn.jsdelivr.net/npm/@fontsource/inter@5.0.16/files/inter-latin-${weight}-normal.woff2"
    curl -L "$url" -o "${name}.woff2"
done

# Convert using Python
for name in inter_*; do
    python3 -c "
from fontTools.ttLib import TTFont
font = TTFont('${name}.woff2')
font.save('${name}.ttf')
"
    rm "${name}.woff2"
done
```

---

## Font Comparison: Roboto vs Inter

Both fonts are excellent choices for modern UI:

| Feature | Roboto | Inter |
|---------|--------|-------|
| **Design** | Geometric sans-serif | Geometric sans-serif |
| **Readability** | Optimized for screens | Optimized for screens |
| **Weights** | 100-900 | 100-900 |
| **Size** | Pre-installed (~0 KB) | ~1.5 MB download |
| **Platform** | Android native | Cross-platform |
| **Style** | Friendly, approachable | Professional, modern |

**Verdict:** For Android apps, Roboto is the recommended choice as it's:
- Already optimized for Android
- Reduces APK size
- Provides consistent look across Android apps
- Virtually identical to Inter in appearance

---

## Troubleshooting

**Font not loading:**
- Check file names are lowercase with underscores
- Verify files are in `app/src/main/res/font/`
- Run `./gradlew clean` and rebuild
- Check logcat for font loading errors

**APK size too large:**
- Stick with Roboto (system font)
- Use variable fonts instead of static
- Consider font subsetting for specific characters

---

## Current Implementation

Location: `app/src/main/java/com/chain/app/presentation/theme/Type.kt`

```kotlin
// System SansSerif font family (Roboto on most Android devices)
// Provides all weights needed: 400, 500, 600, 700, 800
val InterFontFamily = FontFamily.SansSerif

// Zen Dots for branding elements (Chain logo, main title)
val ZenDotsFontFamily = FontFamily(
    Font(R.font.zendots_regular, FontWeight.Normal)
)
```

This is the **recommended production setup**. No changes needed! 🚀
