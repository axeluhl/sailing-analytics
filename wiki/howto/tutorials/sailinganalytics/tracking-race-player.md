The video below gives an excellent overview of the main functionality of the SAP Race Player which is accessible on a per-race basis via the "Races/Tracking" Tab of a regatta.

<div id="player"></div>

<script>
  // Load YouTube IFrame API script
  var tag = document.createElement('script');
  tag.src = "https://www.youtube.com/iframe_api";
  var firstScriptTag = document.getElementsByTagName('script')[0];
  firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

  // Create YouTube player when API ready
  var player;
  function onYouTubeIframeAPIReady() {
    player = new YT.Player('player', {
      height: '360',
      width: '640',
      videoId: 'A2Z86lYV7CE',
      events: {
        'onReady': onPlayerReady,
      }
    });
  }

  function onPlayerReady(event) {
    // Autoplay video
    event.target.playVideo();
  }
</script>




