
var map;

function random() {
    return Math.floor(Math.random()*484887498739872)
}

/**
 * General functionality
 **/
var loader_whitebg = '<div class="loader"><img src="' + _base_url() + 'static/images/ajax-loader.gif"/></div>';

/**
 * format("i can speak {language} since i was {age}",{language:'javascript',age:10});
 */
var format = function (str, col) {
    col = typeof col === 'object' ? col : Array.prototype.slice.call(arguments, 1);

    return str.replace(/\{([^}]+)\}/gm, function () {
        return col[arguments[1]];
    });
};

/**
 * Renders the given template and invokes the callback with response
 */
function template(templatename, parameters, callback) {
    $.get(_base_url() + '@@/' + templatename, parameters, function(data) {
        callback(data);
    });
}

/**
 * Calls the given function. Must return JSON
 */
function GET(name, parameters, callback) {
    $.get(_base_url() + '++/' + name, parameters, function(data) {
        if (callback)
            callback(data);
    })
}

function displayLoader(element_desc) {
    $(element_desc).html(loader_whitebg);
}

$(document).ready(function() {
    $.ajaxSetup({cache:false});
});
