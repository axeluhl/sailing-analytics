
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
function loadLeaderboard(races, sortby, competitors, direction, colmode) {
    showLoader();
    $.getJSON('/++/moderatorLiveData', 
                {races:races, sortby:sortby, competitors:competitors, direction:direction,colmode:colmode}, 
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
        loadLeaderboard(global_race, global_sortkey, global_competitors, global_direction, global_colmode);
}

function toggleListener() {
    $('#refresh-button').toggleClass('refresh');
    if (!$('#refresh-button').hasClass('refresh')) {
        $('#refresh-button').css('background-image', 'url(/moderator-static/freeze.png)');
        listener_paused = true;
    } else {
        listener_paused = false;
        $('#refresh-button').css('background-image', 'url(/moderator-static/refresh-icon.png)');
    }
}

function displayRaceAndLeg(raceindex, legs) {
    // disabled upon request from Marcus Baur
    //$('.pause-btn').html('<div style="padding: 8px 4px 5px 15px; color: white; font-size: 23px;font-weight: bold;">R'+raceindex+' : '+legs+'</div>');
}

function showLoader() {
    $('.pause-btn').html(loader_image);
}

function hideLoader() {
    $('.pause-btn').html('');
}

function showOverallWrap(caller) {
    element = $('#overall-wrap');
    element.fadeToggle('fast', function() {
        $('#main-interface').toggle();
        if (!element.is(':visible')) {
            caller.css('background-image', 'url(/moderator-static/arrow-right.png)');
        } else {
            caller.css('background-image', 'url(/moderator-static/arrow-left.png)');
        }
    });
}

function switchRaceBlock(block) {
    loadLeaderboard(block, param, global_competitors, global_direction, global_colmode);
}

function sortBy(param, element) {
    $('.sort-asc, .sort-desc').css('background-image', 'url(/moderator-static/sort-none.png)');

    element.toggleClass('sort-asc').toggleClass('sort-desc');

    if (element.hasClass('sort-asc')) {
        global_direction = 'asc';
        element.css('background-image', 'url(/moderator-static/sort-arrow-active-bottom.png)')
    } else {
        global_direction = 'desc';
        element.css('background-image', 'url(/moderator-static/sort-arrow-active-top.png)')
    }

    global_sortkey = param;
    loadLeaderboard(global_race, param, global_competitors, global_direction, global_colmode);
}

function yieldValue(element, newvalue, ignore_zeros, legvalue) {
    if (element.html() != newvalue && newvalue != '' && newvalue != 'None') {
        if (ignore_zeros == true && (newvalue == '0' || newvalue == 0))
            return;

        // special handling for minute shown
        if (isNaN(newvalue) && newvalue.indexOf(':') == -1)
            return;

        element.html(newvalue);
    }
}

/**
 * Puts data into the right context for the leaderboard
 */
function displayLeaderboard(data) {
    rowid = 1;

    displayRaceAndLeg(data[0].current_race, data[0].current_legs);

    for (cpos in data) {
        competitor = data[cpos];

        /* always change current rank in view */
        $('#clipping-'+rowid+'-1 span').html(competitor.current_rank);

        /* insert some data in overall view */
        $('#competitor-global-rank-'+rowid).html(competitor.global_rank);
        $('#competitor-global-nationality-'+rowid).html(competitor.nationality);
        $('#competitor-global-name-'+rowid).html(competitor.name);
        $('#competitor-global-total-'+rowid).html(competitor.total_points);
        $('#competitor-global-net-'+rowid).html(competitor.net_points);

        name_element = $('#clipping-'+rowid+'-3 span');
        if (name_element.html() != competitor.name) {
            /* competitor position has changed - refresh whole line */

            name_element.html(competitor.name);

            $('#clipping-'+rowid+'-2 span').html(competitor.nationality);

            /* now set values independent what has been there before */
            racepos = 1;
            for (racerank in competitor.raceranks) {
                yieldValue($('#race-'+racepos+'-rankrow-'+rowid), competitor.raceranks[racerank], true);

                markpos = 1;
                for (markrank in competitor.markranks[racepos-1]) {
                    yieldValue($('#race-'+racepos+'-mark-'+markpos+'-row-'+rowid), competitor.markranks[racepos-1][markrank], true);

                    legpos = 1;
                    for (legvalue in competitor.legvalues[racepos-1][markpos-1]) {
                        if (legvalue == 0) {
                            yieldValue($('#race-'+racepos+'-mark-'+markpos+'-legrow-'+rowid), parseFloat(competitor.legvalues[racepos-1][markpos-1][legvalue]).toFixed(), false);
                        } else {
                            yieldValue($('#race-'+racepos+'-mark-'+markpos+'-valrow-'+rowid+'-pos-'+(legpos-1)+' span'), competitor.legvalues[racepos-1][markpos-1][legvalue], false);
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
                yieldValue($('#race-'+racepos+'-rankrow-'+rowid), competitor.raceranks[racerank], true);

                markpos = 1;
                for (markrank in competitor.markranks[racepos-1]) {
                    yieldValue($('#race-'+racepos+'-mark-'+markpos+'-row-'+rowid), competitor.markranks[racepos-1][markrank], true);

                    legpos = 1;
                    for (legvalue in competitor.legvalues[racepos-1][markpos-1]) {
                        if (legvalue == 0) {
                            yieldValue($('#race-'+racepos+'-mark-'+markpos+'-legrow-'+rowid), parseFloat(competitor.legvalues[racepos-1][markpos-1][legvalue]).toFixed(), false);
                        } else {
                            yieldValue($('#race-'+racepos+'-mark-'+markpos+'-valrow-'+rowid+'-pos-'+(legpos-1)+' span'), competitor.legvalues[racepos-1][markpos-1][legvalue], false, legvalue);
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

  loadLeaderboard(global_race, global_sortkey, global_competitors, global_direction, global_colmode);
  window.setInterval('liveRefresh()', 5000);
});
