var exec = require("cordova/exec");

var Rdz = {
    rdztest: function(str, callback) {
        exec(callback, function(err) { callback('Nothing to do'); }, "rdzwx", "rdzwx", [str]);
    }
};

module.exports = Rdz;
