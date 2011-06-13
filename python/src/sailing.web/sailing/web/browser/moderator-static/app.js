
var global_sortkey = 'name';
var global_direction = 'desc';
var global_race = '1:3';
var global_competitors = '1:20';

var loader_image = "<img style='margin: 3px 0px 3px 3px' src='/static/images/ajax-loader-greybg.gif'/>";

var listener_paused = false;

/*
 * Loads leaderboard data. Parameters:
 *
 * races:  Slice that indicates which data to load (e.g. "1:3")
 * sortby: Sorting parameters (e.g "1,1,1")
 * competitors: Slice indicating which competitors to show (e.g. "1:20")
 */
function loadLeaderboard(races, sortby, competitors, direction) {
    showLoader();
    $.getJSON('/++/moderatorLiveData', 
                {races:races, sortby:sortby, competitors:competitors, direction:direction}, 
        function(data) {
            displayLeaderboard(data);

            global_race = races;
            global_sortkey = sortby;
            global_competitors = competitors;
            global_direction = direction;

            hideLoader();
        }
    );
}

function liveRefresh() {
    if (listener_paused == false)
        loadLeaderboard(global_race, global_sortkey, global_competitors, global_direction);
}

function toggleListener() {
    $('#refresh-button').toggleClass('refresh');
    if ($('#refresh-button').hasClass('refresh')) {
        $('#refresh-button').css('background-image', 'url(/moderator-static/refresh-icon.png)');
        listener_paused = true;
    } else {
        listener_paused = false;
        $('#refresh-button').css('background-image', 'url(/moderator-static/pause_button.png)');
    }
}

function showLoader() {
    $('.refresh-btn').html(loader_image);
}

function hideLoader() {
    $('.refresh-btn').html('');
}
function sortBy(param, element) {
    element.toggleClass('sort-asc').toggleClass('sort-desc');

    if (element.hasClass('sort-asc'))
        global_direction = 'asc';
    else global_direction = 'desc';

    global_sortkey = param;
    loadLeaderboard(global_race, param, global_competitors, global_direction);
}

function yieldValue(element, newvalue) {
    element.html(newvalue);
}

/**
 * Puts data into the right context for the leaderboard
 */
function displayLeaderboard(data) {
    rowid = 1;
    for (cpos in data) {
        competitor = data[cpos];

        /* always change global rank */
        $('#clipping-'+rowid+'-1 span').html(competitor.global_rank);

        name_element = $('#clipping-'+rowid+'-3 span');
        if (name_element.html() != competitor.name) {
            /* competitor position has changed - refresh whole line */

            name_element.html(competitor.name);
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
            name_element.html(competitor.name);
            $('#clipping-'+rowid+'-2 span').html(competitor.nationality);

            for (racerank in competitor.raceranks) {
                yieldValue($('#race-'+racepos+'-rankrow-'+rowid), competitor.raceranks[racerank]);

                markpos = 1;
                for (markrank in competitor.markranks[racepos-1]) {
                    yieldValue($('#race-'+racepos+'-mark-'+markpos+'-row-'+rowid), competitor.markranks[racepos-1][markrank]);

                    legpos = 1;
                    for (legvalue in competitor.legvalues[racepos-1][markpos-1]) {
                        if (legvalue == 0) {
                            yieldValue($('#race-'+racepos+'-mark-'+markpos+'-legrow-'+rowid), parseFloat(competitor.legvalues[racepos-1][markpos-1][legvalue]).toFixed());
                        } else {
                            yieldValue($('#race-'+racepos+'-mark-'+markpos+'-valrow-'+rowid+'-pos-'+(legpos-1)+' span'), competitor.legvalues[racepos-1][markpos-1][legvalue]);
                        }
                        legpos += 1;
                    }

                    markpos += 1;
                }
            }
        }

        rowid += 1;
    }
}

$(document).ready(function() {
  var wHeight = $(window).height();

  $("#rootwrapper").css("height", wHeight);
  $("#appinterface").css("height", wHeight-131);

  $.ajaxSetup({cache:false});

  loadLeaderboard(global_race, global_sortkey, global_competitors, global_direction);
  window.setInterval('liveRefresh()', 5000);
});
