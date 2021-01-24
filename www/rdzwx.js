var exec = require("cordova/exec");

var RdzWx = {
    rdzwx: function(str, callback) {
        exec(callback, function(err) { callback('Nothing to do'); }, "RdzWx", "rdzwx", [str]);
    }
};

module.exports = RdzWx;
