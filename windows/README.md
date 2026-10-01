# YTDLnis Windows Port

This directory contains the Windows desktop implementation being developed alongside the existing Android application.

## Milestone 1 — Engine foundation

Implemented:
- Windows application-data paths
- yt-dlp executable discovery
- Windows subprocess execution
- yt-dlp request construction
- yt-dlp/aria2c progress parsing

Current executable lookup order:
1. YTDLNIS_YTDLP environment variable
2. %LOCALAPPDATA%\\YTDLnis\\yt-dlp\\yt-dlp.exe
3. yt-dlp.exe on PATH

The Android application remains unchanged.

Next:
- FFmpeg discovery and injection
- cancellation/process registry
- runtime bootstrap/downloads
- shared data models
- Windows desktop UI
