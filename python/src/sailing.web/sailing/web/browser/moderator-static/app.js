
/*
 * Loads leaderboard data. Parameters:
 *
 * races:  Slice that indicates which data to load (e.g. "0:2")
 * sortby: Sorting parameters (e.g "1,1,1")
 * competitors: Slice indicating which competitors to show (e.g. "0:20")
 */
function loadLeaderboard(races, sortby, competitors) {
    $.getJSON('/++/moderatorLiveData', 
                {races:races, sortby:sortby, competitors:competitors}, 
        function(data) {
            displayLeaderboard(data);
        }
    );
}

/**
 * Puts data into the right context for the leaderboard
 */
function displayLeaderboard(data) {
    rowid = 1;
    for (key in data) {
        competitor = data[key];

        name_element = $('#clipping-'+rowid+'-3 span');
        if (true || name_element.html() == key) {
            /* competitor position has changed - refresh whole line */

            name_element.html(key);
            $('#clipping-'+rowid+'-1 span').html(competitor.global_rank);
            $('#clipping-'+rowid+'-2 span').html(competitor.nationality);

            /* now set values independent what has been there before */
            racepos = 1;
            for (racerank in competitor.raceranks) {
                $('#race-'+racepos+'-rankrow-'+rowid).html(competitor.raceranks[racerank]);

                markpos = 1;
                for (markrank in competitor.markranks[racepos-1]) {
                    $('#race-'+racepos+'-mark-'+markpos+'-row-'+rowid).html(competitor.markranks[racepos-1][markrank]);

                    legpos = 1;
                    for (legvalue in competitor.legvalues[racepos-1][markpos-1]) {
                        if (legvalue == 0) {
                            $('#race-'+racepos+'-mark-'+markpos+'-legrow-'+rowid).html(parseFloat(competitor.legvalues[racepos-1][markpos-1][legvalue]).toFixed());
                        } else {
                            $('#race-'+racepos+'-mark-'+markpos+'-valrow-'+rowid+'-pos-'+(legpos-1)+' span').html(competitor.legvalues[racepos-1][markpos-1][legvalue]);
                        }
                        legpos += 1;
                    }

                    markpos += 1;
                }
            }
        } else {
            /* check if values have changed */
        }

        rowid += 1;
    }
}

$(document).ready(function() {
  var wHeight = $(window).height();

  $("#rootwrapper").css("height", wHeight);
  $("#appinterface").css("height", wHeight-131);

  loadLeaderboard();
});
