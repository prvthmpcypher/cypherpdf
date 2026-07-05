<div align="center">

# CypherPDF

A clutter-free PDF reader for Android.
No ads. No accounts. No tracking. No launcher icon.

[![Build APK](https://github.com/prvthmpcypher/cypherpdf/actions/workflows/build-apk.yml/badge.svg)](https://github.com/prvthmpcypher/cypherpdf/actions/workflows/build-apk.yml)
![Platform](https://img.shields.io/badge/platform-Android-3DDC84)
![Min SDK](https://img.shields.io/badge/minSdk-24-blue)
![License](https://img.shields.io/badge/license-GPL--3.0-lightgrey)

</div>

---

## Why this exists

Most "free" PDF apps on the Play Store come with a sign-in wall, a home-screen
icon you never asked for, a permission list longer than the app itself, and a
tracker phoning home in the background.

CypherPDF doesn't do any of that. You open a PDF from another app (file
manager, browser, email, etc.), you read it, you close it. That's the whole
app — it has no launcher icon and doesn't appear in your app drawer.

## Features

- **Continuous scroll** — pages flow top to bottom like a real document
- **Pinch to zoom, with panning** — zoom in and drag around the page freely,
  no need to zoom back out to reposition
- **Tap to focus** — tap anywhere to hide the top bar and read distraction-free
- **Auto light/dark theme** — matches your system theme automatically
- **Jump to page** — tap the page indicator to type in a page number directly
- **Password-protected PDFs** — you're prompted for a password if the file
  needs one
- **Share** — re-share the currently open PDF to another app
- **No internet permission requested** — the app has no network code and
  requests no network access

## What it deliberately doesn't do

- No launcher icon / doesn't appear in your app drawer — it opens only when
  you open a PDF with it
- No accounts, no cloud sync, no analytics, no ads
- No editing, annotation, or form-filling — this is a reader, not an editor

## Installing

Download the latest APK from the
[Actions](https://github.com/prvthmpcypher/cypherpdf/actions/workflows/build-apk.yml)
tab (pick the most recent successful run and grab the `CypherPDF` artifact),
or build it yourself — see below.

## Building from source

Requirements: JDK 17, Android SDK (API 34), Gradle 8.9+.

```bash
git clone https://github.com/prvthmpcypher/cypherpdf.git
cd cypherpdf
gradle assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

CI builds run automatically on every push to `main` via
`.github/workflows/build-apk.yml`.

## Tech stack

- Kotlin, AndroidX (`core-ktx`, `appcompat`, `material`, `recyclerview`)
- [`android-pdf-viewer`](https://github.com/mhiew/AndroidPdfViewer) (mhiew fork)
  for PDF rendering, zoom, and pan
- No Firebase, no Google Play Services, no analytics SDKs

## License

GPL-3.0 — see [LICENSE](LICENSE).

## Contributing

Issues and pull requests are welcome. If you're changing the PDF-rendering
path or the icon/manifest, please test both a debug and a release
(`assembleRelease`) build before opening a PR — the release build type runs
ProGuard/R8, which behaves differently from debug.

## More from Poorvith M P

| Project | What it does |
|---|---|
| [CypherDocs](https://github.com/prvthmpcypher/CypherDocs) | Companion reader for everything except PDFs |
| [AiScrubber](https://github.com/prvthmpcypher/aiscrubber) | Client-side privacy scrubber for AI prompts |
| [PaperHive](https://github.com/prvthmpcypher/paperhive) | Offline-first PDF toolkit for the browser |

## Connect

[![GitHub](https://img.shields.io/badge/GitHub-prvthmpcypher-181717?logo=github)](https://github.com/prvthmpcypher)
[![X](https://img.shields.io/badge/X-@poorvithmp07-000000?logo=x)](https://x.com/poorvithmp07)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-poorvithmp-0A66C2?logo=linkedin)](https://www.linkedin.com/in/poorvithmp)

