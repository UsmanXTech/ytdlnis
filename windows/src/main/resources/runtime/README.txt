YTDLnis Windows runtime

Place distributable tools under the application runtime directory:

runtime/ffmpeg/ffmpeg.exe
runtime/ffmpeg/ffprobe.exe
runtime/aria2/aria2c.exe
runtime/node/node.exe
runtime/deno/deno.exe
runtime/quickjs/qjs.exe

yt-dlp is stored separately under:
yt-dlp/yt-dlp.exe

The application can also discover tools from PATH or environment variables.
