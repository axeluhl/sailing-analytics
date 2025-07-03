The video below gives an excellent overview of the main functionality of the SAP Race Player which is accessible on a per-race basis via the "Races/Tracking" Tab of a regatta.


<head>
    
  <meta charset="UTF-8" />
  <title>YouTube IFrame API Example</title>
</head>
<body>

  <div id="player"></div>

  <script>
    // 1. Load the IFrame Player API code asynchronously.
    var tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    var firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

    // 2. Create the YouTube player after the API code downloads.
    var player;
    function onYouTubeIframeAPIReady() {
      player = new YT.Player('player', {
        height: '360',
        width: '640',
        videoId: 'A2Z86lYV7CE', // Your YouTube video ID
        events: {
          'onReady': onPlayerReady
        }
      });
    }

    // 3. Autoplay when ready
    function onPlayerReady(event) {
      event.target.playVideo();
    }
  </script>

</body>
</html>