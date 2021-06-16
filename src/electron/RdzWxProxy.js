var net = global.require('net');

var rdzhost = "rdzsonde.local";
var rdzport = 14570;

var client = null;
function runService(success) {
  console.log("Connecting");
  client = new net.Socket();
  client.connect(rdzport, rdzhost, function() {
    console.log("Connected");
    success('{ "msgtype": "ttgostatus", "state": "online", "ip": "'+client.remoteAddress+'" }', { keepCallback: true } );
  });
  client.on('data', function(data) {
    console.log("Received: "+data);
    success(data, { keepCallback: true });
  });
  client.on("close", function() {
    // keep on trying
    console.log("Connection closed");
    success('{ "msgtype": "ttgostatus", "state": "offline", "ip": "" }', { keepCallback: true } );
    client = null;
    runService(success);
  });
}

exports.start = function(success, fail, args) {
  console.error("Started");
  runService(success);
  console.error("Running");
}

exports.stop = function(success, fail, args) {
  console.stopped("Started");
  success(true);
}

exports.closeconn = function(success, fail, args) {
  success(true);
}

exports.showmap = function(success, fail, args) {
  success(true);
}

exports.wgstoegm = function(success, fail, args) {
  success(true);
}

cordova.commandProxy.add('RdzWx', exports);
