# YTDLnis Windows Port

This branch contains the Windows desktop implementation developed alongside the Android application.

## Implemented

- Windows runtime directory management
- yt-dlp executable discovery
- FFmpeg/FFprobe/Aria2c tool discovery
- process registry and cancellation
- yt-dlp request construction
- yt-dlp/aria2c progress parsing
- Compose Desktop Windows application shell
- EXE/MSI packaging configuration
- GitHub Actions Windows build workflow

## Runtime

Default location:

%LOCALAPPDATA%\\YTDLnis\\

The runtime can also use tools supplied through environment variables, including YTDLNIS_YTDLP and YTDLNIS_FFMPEG.

## Build locally

gradlew.bat :windows:packageExe
gradlew.bat :windows:packageMsi

Android modules remain isolated from this Windows port.
