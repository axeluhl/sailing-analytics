# Live Audio Streaming

See also [Bug 1522](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=1522). We currently have the ManyPlayers Race Viewer that supports this use case. It is based on Flash and requires several additional server-side components. Adding live audio support to the HTML-based SAP Sailing Analytics could simplify the consumption of live audio streams for certain use cases and could ease server administration considerably.

# Update Oct 13 2014

With commit 11d7a168 we support Youtube live streaming. This can be used as replacement for audio-only streaming.

## Benefits

- Recording and streaming is all provided by Youtube.
- Can be complemented with video and other Youtube gimmicks (live pictures, stills, user notifications, user links)

## Shortcomings

- 4h max. duration per session. Otherwise Youtube's rolling storage starts overwriting old content.
- Audio-only not yet possible. But hiding the video panel should be not too much effort.