The video below gives an excellent overview of the main functionality of the SAP Race Player which is accessible on a per-race basis via the "Races/Tracking" Tab of a regatta.




 <div id="player"></div>

  <script>
    console.log('Loading YouTube IFrame API');

    var tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    var firstScriptTag = document.getElementsByTagName('script')[0];
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

    var player;
    function onYouTubeIframeAPIReady() {
      console.log('YouTube API Ready, creating player...');
      player = new YT.Player('player', {
        height: '360',
        width: '640',
        videoId: 'A2Z86lYV7CE',
        playerVars: {
          autoplay: 1,
          mute: 1
        },
        events: {
          'onReady': function(event) {
            console.log('Player ready, starting video');
            event.target.playVideo();
          },
          'onError': function(e) {
            console.error('Player error:', e.data);
          }
        }
      });
    }
  </script>
   

