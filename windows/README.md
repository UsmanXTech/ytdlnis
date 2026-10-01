# YTDLnis Windows Port

This directory contains the Windows desktop implementation being developed alongside the existing Android application.

## Goals

- Reuse the existing yt-dlp request/options and process execution concepts.
- Keep the Android application unchanged.
- Provide Windows-specific runtime, filesystem, process, scheduling, and UI layers.
- Preserve YTDLnis behavior and design as closely as practical.

## Initial architecture

- `engine/`: platform-neutral command/request and process abstractions.
- `runtime/`: Windows paths and bundled executable management.
- `app/`: Windows desktop application entry point and UI.

The first milestone is a minimal Windows engine that can locate yt-dlp and execute a request while reporting progress.
