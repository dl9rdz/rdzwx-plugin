var exec = require("cordova/exec");

var RdzWx = {
    start: function(str, callback) {
        exec(callback, function(err) { callback('error: '+err); }, "RdzWx", "start", [str]);
    },
    closeconn: function(str, callback) {
        exec(callback, function(err) { callback('error: '+err); }, "RdzWx", "closeconn", [str]);
    }
};

module.exports = RdzWx;
