var EventEmitter = require('events').EventEmitter;
var fs = require('fs');
var url = require('url');
var util = require('util');

var env = require('./env');
var paramChecker = require('./param-checker');

module.exports.createPluginMgr = function (pluginInfos, logger) {
    return new PluginMgr(pluginInfos, logger);
};


/**
 *  manager class for plugins
 * @class PluginMgr
 * @constructor
 *
 */
function PluginMgr(logger) {
    this.pluginModules = {};
    this.logger = logger;
    EventEmitter.call(this);
}

util.inherits(PluginMgr, EventEmitter);

PluginMgr.prototype.loadAllPlugins = function (pluginInfos) {
    for (var i in pluginInfos) {
        doLoadPluginModule(this, pluginInfos[i].ver, pluginInfos[i].p);
    }
};

PluginMgr.prototype.usePluginAtVersion = function (name, version) {
     var pluginModule = doLoadPluginModule(this, version, p);
     if (pluginModule instanceof Error) {
     } else {
         this.pluginModules[name] = pluginModule;
         this.emit('plugin-upgrade', name, pluginModule);
     }
     return pluginModule;
};

function doLoadPluginModule(self, ver, path) {
    var pluginModule = require(path);
    if (!pluginModule.name ) {
        throw new Error('plugin ' + path +
            ' miss required field name');
    }
    if (!pluginModule.createSDK ) {
        throw new Error('plugin ' + path +
            ' miss required function create');
    }
    if (!pluginModule.cfgDesc ) {
        throw new Error('plugin ' + path +
            ' miss required function cfgDesc');
    }
    var checker = paramChecker.createChecker(pluginModule.cfgDesc);
    var pluginModuleInfo = {
        name: pluginModule.name,
        m: pluginModule,
        path: path,
        version: ver,
        plugin: pluginModule.createSDK(self.logger, checker, env.debug)
    };
    self.pluginModules[pluginModule.name] = pluginModuleInfo;
    self.logger.info({name: pluginModuleInfo.name}, 'load plugin module');
    return pluginModuleInfo;
}

