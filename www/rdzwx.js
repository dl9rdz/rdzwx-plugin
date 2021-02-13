var exec = require("cordova/exec");

var RdzWx = {
    start: function(str, callback) {
        exec(callback, function(err) { callback('error: '+err); }, "RdzWx", "start", [str]);
    },
    stop: function(str, callback) {
            exec(callback, function(err) { callback('error: '+err); }, "RdzWx", "stop", [str]);
    },
    closeconn: function(str, callback) {
        exec(callback, function(err) { callback('error: '+err); }, "RdzWx", "closeconn", [str]);
    },
    showmap: function(str, callback) {
        exec(callback, function(err) { callback('error: '+err); }, "RdzWx", "showmap", [str]);
     },
    wgstoegm: function(lat, lon, callback) {
        exec(callback, function(err) { callback('error: '+err); }, "RdzWx", "wgstoegm", [lat, lon]);
     }

};

module.exports = RdzWx;
