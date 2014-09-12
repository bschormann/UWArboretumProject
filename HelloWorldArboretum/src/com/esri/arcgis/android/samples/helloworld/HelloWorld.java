/* Copyright 2012 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgis.android.samples.helloworld;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.esri.android.map.MapOptions;
import com.esri.android.map.MapOptions.MapType;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;

/**
 * The HelloWorld app is the most basic Map app for the ArcGIS Runtime SDK for Android. It shows how to define a MapView
 * in the layout XML of the activity. Within the XML definition of the MapView, MapOptions attributes are used to
 * populate that MapView with a basemap layer showing streets, and also the initial extent and zoom level are set. By
 * default, this map supports basic zooming and panning operations. This sample also demonstrates calling the MapView
 * pause and unpause methods from the Activity onPause and onResume methods, which suspend and resume map rendering
 * threads. A reference to the MapView is set within the onCreate method of the Activity which can be used at the
 * starting point for further coding.
 */

public class HelloWorld extends Activity {
	
	public enum MAP_OPTION {USE_XML_MAP1, USE_XML_MAP2, ADD_FEATURES};
	
	private MAP_OPTION mMapOption = MAP_OPTION.ADD_FEATURES;
	
	private static final String TAG = "HelloWorld";	
    private MapView mMapView;
    
    // Arboretum coordinates: 47.640139, -122.293780

   // Called when the activity is first created.
   @Override
   public void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   Log.i(TAG, "onCreate()");
     
	   if (mMapOption == MAP_OPTION.USE_XML_MAP1) {
		   setContentView(R.layout.main1);
		   // After the content of this Activity is set, the map can be accessed programmatically from the layout.
		   mMapView = (MapView) findViewById(R.id.map);
		   mMapView.addLayer(new ArcGISDynamicMapServiceLayer( 
				   "http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/UWBG/MapServer"));
      	}
      	else if (mMapOption == MAP_OPTION.USE_XML_MAP2) {
      		setContentView(R.layout.main2);
      		// After the content of this Activity is set, the map can be accessed programmatically from the layout.
      		mMapView = (MapView) findViewById(R.id.map);
          
      		mMapView.addLayer(new ArcGISDynamicMapServiceLayer( 
      				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/UWBG/MapServer"));
  
      		// values from UWBG (MapServer)- All Layers and Tables
	      	double XMin = 1277938.4660206884;
	      	double YMin = 232251.79283960164;
	      	double XMax = 1281150.8841365278;
	      	double YMax = 237883.30388626456;
	      	Envelope envelope = new Envelope(XMin, YMin, XMax, YMax);      
	      	mMapView.setExtent(envelope);      
      	}
      	else if (mMapOption == MAP_OPTION.ADD_FEATURES) {    	  	
     		MapOptions mapOptions = new MapOptions(MapType.STREETS, 47.634219, -122.295264, 17);	// used Google Earth
     		mMapView = new MapView(this, mapOptions);
     		// values from UWBG (MapServer)- All Layers and Tables
	      	double XMin = 1277938.4660206884;
	      	double YMin = 232251.79283960164;
	      	double XMax = 1281150.8841365278;
	      	double YMax = 237883.30388626456;
	      	Envelope envelope = new Envelope(XMin, YMin, XMax, YMax);      
	      	mMapView.setExtent(envelope);      

    	  	mMapView.addLayer(new ArcGISDynamicMapServiceLayer( 
    	  			"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/UWBG/MapServer")); 
    	  	mMapView.addLayer(new ArcGISDynamicMapServiceLayer( 
    	  			"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer"));    	  	
     		setContentView(mMapView);

      	}      
   }

   @Override
   protected void onPause() {
	   super.onPause();
	   Log.i(TAG, "onPause()");

	   // Call MapView.pause to suspend map rendering while the activity is paused, which can save battery usage.
	   if (mMapView != null) {
		   mMapView.pause();
	   }
   }

   @Override
   protected void onResume() {
	   super.onResume();
	   Log.i(TAG, "onResume()");
      
	   // Call MapView.unpause to resume map rendering when the activity returns to the foreground.
	   if (mMapView != null) {
		   mMapView.unpause();
	   }
   }
}