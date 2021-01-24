/*
 * Copyright (C) Hansi Reiser <dl9rdz@darc.de>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.dl9rdz

import android.location.LocationManager
import android.location.LocationListener
import android.location.Location
import android.os.Bundle
import android.Manifest

import android.content.Context
import android.os.Handler
import android.util.Log

import org.apache.cordova.CordovaArgs
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaInterface

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.net.InetAddress
import java.net.Socket

import kotlin.concurrent.thread

/* Class with android specific code */

//const val SERVICE_TYPE = "_kisstnc._tcp."  
const val SERVICE_TYPE = "_jsonrdz._tcp."

class GPSHandler {
    private var locationManager : LocationManager? = null
    private var rdzwx: RdzWx? = null

    var permissions: Array<String> = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d(LOG_TAG, ("" + location.longitude + ":" + location.latitude))
	    rdzwx?.updateGps(location.latitude, location.longitude, location.altitude, location.bearing)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun initialize(cordovaPlugin: RdzWx) {
	if(hasPermission(cordovaPlugin.cordova)) {
	    setupLocationManager(cordovaPlugin)
	} else {
	    cordovaPlugin.cordova.requestPermissions(cordovaPlugin, 0, permissions)
	}
	rdzwx = cordovaPlugin
    }

    fun hasPermission(cordova: CordovaInterface): Boolean {
	for (p in permissions) {
	    if(!cordova.hasPermission(p))
		return false;
	}
	return true;
    }
	
    fun setupLocationManager(cordovaPlugin: CordovaPlugin) {
	locationManager = cordovaPlugin.cordova.getActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
    }
}

class MDNSHandler {
    private var nsdManager : NsdManager? = null
    private var rdzwx: RdzWx? = null

    fun initialize(cordovaPlugin: RdzWx) {
	nsdManager = cordovaPlugin.cordova.getActivity().getSystemService(Context.NSD_SERVICE) as NsdManager?
	nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
	rdzwx = cordovaPlugin
    }

    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {
	var resolveListener = object : NsdManager.ResolveListener {
	    override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
		Log.d(LOG_TAG, "Resolve failed: $errorCode")
	    }
	    override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
		Log.d(LOG_TAG, "Resolve suceeded with host ${serviceInfo?.getHost()} and port ${serviceInfo?.port}")
		if (serviceInfo != null) { rdzwx?.runAprs(serviceInfo) }
		else Log.d(LOG_TAG, "service info is null");
		if( rdzwx == null ) {Log.d(LOG_TAG, "test is null")}
	    }
	}

	// Called as soon as service discovery begins.
	override fun onDiscoveryStarted(regType: String) {
	    Log.d(LOG_TAG, "Service discovery started")
	}

	override fun onServiceFound(service: NsdServiceInfo) {
	    // A service was found! Do something with it.
	    Log.d(LOG_TAG, "Service discovery success$service")
	    when {
		// If it is not the right type, complain....
		service.serviceType != SERVICE_TYPE ->
		    Log.d(LOG_TAG, "Unknown Service Type: ${service.serviceType}")
		// else lookup host and port
		else ->
		    nsdManager?.resolveService(service, resolveListener)
	    }
	    Log.d(LOG_TAG, "serviceName: ${service.serviceName}  host: ${service.host}, port: ${service.port}")
	}

	override fun onServiceLost(service: NsdServiceInfo) {
	    // When the network service is no longer available.
	    // Internal bookkeeping code goes here.
	    Log.e(LOG_TAG, "service lost: $service")
	    // TODO: remove from available devices...
	}

	override fun onDiscoveryStopped(serviceType: String) {
	    Log.i(LOG_TAG, "Discovery stopped: $serviceType")
	}

	override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
	    Log.e(LOG_TAG, "Discovery failed: Error code:$errorCode")
	    nsdManager?.stopServiceDiscovery(this)
	}

	override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
	    Log.e(LOG_TAG, "Discovery failed: Error code:$errorCode")
	    nsdManager?.stopServiceDiscovery(this)
	}
    }
}

