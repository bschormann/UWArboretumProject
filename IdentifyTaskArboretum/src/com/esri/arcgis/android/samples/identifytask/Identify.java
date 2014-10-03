/* Copyright 2014 ESRI
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

package com.esri.arcgis.android.samples.identifytask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.android.action.IdentifyResultSpinner;
import com.esri.android.action.IdentifyResultSpinnerAdapter;
import com.esri.android.map.CalloutPopupWindow;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.esri.core.tasks.identify.IdentifyResult;
import com.esri.core.tasks.identify.IdentifyTask;

/**
 * This sample allows the user to identify data based on single tap and view the
 * results in a callout window which has a spinner in its layout. Also the user
 * can select any of the results displayed and view its details. The details are
 * the attribute values.
 * 
 * The output value shown in the spinner is the display field.
 * 
 */

/**
 * Identify Task
 * The purpose of this sample is to show the user how to use the Identify task to query 
 * features on the map and show these results in a callout. The sample adds a tiled map 
 * service as the basemap and a dynamic map service of recent earthquakes. The dynamic map 
 * service is the same as that used for the Identify task.
 * 
 * Features
 * IdentifyTask
 * 		https://developers.arcgis.com/android/api-reference/reference/com/esri/core/tasks/ags/identify/IdentifyTask.html
 * IdentifyResultSpinner
 * 		https://developers.arcgis.com/android/api-reference/reference/com/esri/android/action/IdentifyResultSpinner.html
 * SingleTapListener
 * 		https://developers.arcgis.com/android/api-reference/reference/com/esri/android/map/event/OnSingleTapListener.html
 * 
 * Sample Design
 * The maps layers are added to the map via code in the activities onCreate() method and 
 * the parameters for the Identify task are also created (map service uniform resource 
 * locator [URL] and layers to query). An OnSingleTapListener is set on the map and 
 * within this class, the Identify task is created and invoked via an Executor 
 * (as the Task abstract super class implements the Callable interface), which allows 
 * the task to run in a different thread from the user interface (UI) thread. The geometry 
 * is obtained from the onSingleTap event and is passed to the task. The results from the 
 * Identify task are processed in the onCompletion() method and placed in a map callout.
 * In the callout, a spinner is customized by overriding the IdentifyResultSpinnerAdapter class. 
 * In the new adapter within the getView() method, create a ListView that is populated 
 * with IdentifyResult.
 */

public class Identify extends Activity {
	private final String TAG = "Identify";

	// create ArcGIS objects
	MapView mMapView = null;
	IdentifyParameters mParams = null;
	CalloutPopupWindow mCalloutPopupWindow = null;
	
	String mIdentifyTaskURL = null;

	// create UI objects
	static ProgressDialog mProgressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);
		
		Layer layer1 = new ArcGISDynamicMapServiceLayer(	// plants on map service layer 1
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/UWBG/MapServer");

		Layer layer2 = new ArcGISDynamicMapServiceLayer( 	// no plants
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/Basemaps/MapServer");

		Layer layer3 = new ArcGISDynamicMapServiceLayer( 	// plants on map service layer 3
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer");

		Layer layer4 = new ArcGISFeatureLayer(		// plants
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/3",
				ArcGISFeatureLayer.MODE.ONDEMAND);

		Layer layer5 = new ArcGISFeatureLayer( 		// plants
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/UWBG/MapServer/1", 
				ArcGISFeatureLayer.MODE.ONDEMAND);
				
		Layer layer6 = new ArcGISFeatureLayer( 
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PlantswithBGBase/MapServer/0",
				ArcGISFeatureLayer.MODE.ONDEMAND);
		
		Layer layer7 = new ArcGISDynamicMapServiceLayer( 
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PlantswithBGBase/MapServer/0");
		
		Layer layer8 = new ArcGISDynamicMapServiceLayer( 
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/ArbPhotography/MapServer");
		
		mMapView.addLayer(layer3);
		mMapView.addLayer(layer6);
		mIdentifyTaskURL = 
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer";
		
		// set Identify Parameters
		mParams = new IdentifyParameters();
		mParams.setLayers(new int[] { 3 });		// plants on this layer of mIdentifyTaskURL
//		mParams.setTolerance(20);
		mParams.setTolerance(40);
		mParams.setDPI(98);		
		mParams.setLayerMode(IdentifyParameters.ALL_LAYERS);
//		mParams.setLayerMode(IdentifyParameters.VISIBLE_LAYERS);
//		mParams.setLayerMode(IdentifyParameters.TOP_MOST_LAYER);
		Layer[] layerArray = mMapView.getLayers();	// line of code to understand layers
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSingleTap(final float x, final float y) {

				if (!mMapView.isLoaded()) {
					return;
				}

				// Add to Identify Parameters based on tapped location
				Point identifyPoint = mMapView.toMapPoint(x, y);

				mParams.setGeometry(identifyPoint);
				mParams.setSpatialReference(mMapView.getSpatialReference());
				mParams.setMapHeight(mMapView.getHeight());
				mParams.setMapWidth(mMapView.getWidth());

				// add the area of extent to identify parameters
				Envelope env = new Envelope();
				mMapView.getExtent().queryEnvelope(env);
				mParams.setMapExtent(env);

				// execute the identify task off UI thread
				MyIdentifyTask mTask = new MyIdentifyTask(identifyPoint);
				mTask.execute(mParams);
			}

		});

	}

	private ViewGroup createIdentifyContent(final List<IdentifyResult> results) {

		// create a new LinearLayout in application context
		LinearLayout layout = new LinearLayout(this);

		// view height and widthwrap content
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		// default orientation
		layout.setOrientation(LinearLayout.HORIZONTAL);

		// Spinner to hold the results of an identify operation
		IdentifyResultSpinner spinner = new IdentifyResultSpinner(this, results);

		// make view clickable
		spinner.setClickable(false);
		spinner.canScrollHorizontally(BIND_ADJUST_WITH_ACTIVITY);

		// MyIdentifyAdapter creates a bridge between spinner and it's data
		MyIdentifyAdapter adapter = new MyIdentifyAdapter(this, results);
		spinner.setAdapter(adapter);
		spinner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.addView(spinner);

		return layout;
	}

	/**
	 * This class allows the user to customize the string shown in the callout.
	 * By default its the display field name.
	 * 
	 * A spinner adapter defines two different views; one that shows the data in
	 * the spinner itself and one that shows the data in the drop down list when
	 * spinner is pressed.
	 * 
	 */
	public class MyIdentifyAdapter extends IdentifyResultSpinnerAdapter {
		String m_show = null;
		List<IdentifyResult> resultList;
		int currentDataViewed = -1;
		Context m_context;

		public MyIdentifyAdapter(Context context, List<IdentifyResult> results) {
			super(context, results);
			this.resultList = results;
			this.m_context = context;
		}

		// Get a TextView that displays identify results in the callout.
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String LSP = System.getProperty("line.separator");
			StringBuilder outputVal = new StringBuilder();

			// Resource Object to access the Resource fields
			Resources res = getResources();

			// Get Name attribute from identify results
			IdentifyResult curResult = this.resultList.get(position);
			
			outputVal.append("Plant: " + curResult.getValue());
			outputVal.append(LSP);

			// Create a TextView to write identify results
			TextView txtView;
			txtView = new TextView(this.m_context);
			txtView.setText(outputVal);
			txtView.setTextColor(Color.BLACK);
			txtView.setLayoutParams(new ListView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			txtView.setGravity(Gravity.CENTER_VERTICAL);

			return txtView;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

	private class MyIdentifyTask extends
			AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

//				IdentifyTask task = new IdentifyTask(Identify.this.getResources()
//						.getString(R.string.identify_task_url_for_avghouseholdsize));

				IdentifyTask task = new IdentifyTask(mIdentifyTaskURL);
				
				IdentifyResult[] M_Result;

		Point mAnchor;

		MyIdentifyTask(Point anchorPoint) {
			mAnchor = anchorPoint;
		}

		@Override
		protected void onPreExecute() {
			// create dialog while working off UI thread
			mProgressDialog = ProgressDialog.show(Identify.this, "Identify Task",
					"Identify query ...");

		}

		protected IdentifyResult[] doInBackground(IdentifyParameters... params) {
			Log.i(TAG, "doInBackground()");

			// check that you have the identify parameters
			if (params != null && params.length > 0) {
				IdentifyParameters mParams = params[0];

				try {
					// Run IdentifyTask with Identify Parameters

					M_Result = task.execute(mParams);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return M_Result;
		}

		@Override
		protected void onPostExecute(IdentifyResult[] results) {

			// dismiss dialog
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

			ArrayList<IdentifyResult> resultList = new ArrayList<IdentifyResult>();

			IdentifyResult result_1;
			
			Log.i(TAG, "onPostExecute()-length of results: " + results.length);

			for (int index = 0; index < results.length; index++) {

				
				result_1 = results[index];
				resultList.add(result_1);
//				String displayFieldName = result_1.getDisplayFieldName();
//				Map<String, Object> attr = result_1.getAttributes();
//				for (String key : attr.keySet()) {
//					Log.i(TAG, "key: " + key);
//					if (key.equalsIgnoreCase(displayFieldName)) {
//						resultList.add(result_1);
//					}
//				}
				// the layer ID of the layer that contains the feature that was identified
				// the layer name of the layer that contains the feature that was identified
				// a display field (also called an attribute) name for the identified feature
				// a value for the display field of the identified feature
				// a Map of pairs for the identified feature				
			}
			if (mCalloutPopupWindow == null) {
				mCalloutPopupWindow = new CalloutPopupWindow(createIdentifyContent(resultList));
			} else {
				mCalloutPopupWindow.setContent(createIdentifyContent(resultList));
			}
			mCalloutPopupWindow.showCallout(mMapView, mAnchor, 0, 0);
		}
	}
	 private void addLayers(int[] layers) {  // not used - is crap

//		mMapView.addLayer(new ArcGISDynamicMapServiceLayer( //the address for the forest photo, the background
//				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/UWBG/MapServer"));
//	    mMapView.addLayer(new ArcGISDynamicMapServiceLayer(//the address for the street addresses
//	    		"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/Basemaps/MapServer"));
//      mMapView.addLayer(new ArcGISDynamicMapServiceLayer( //the address for the all the layers except basemap 
//        		"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/3"));
//		mMapView.addLayer(new ArcGISFeatureLayer(
//	    		"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/3",ArcGISFeatureLayer.MODE.ONDEMAND));
			
		// AERIAL PHOTOGRAPHY
		mMapView.addLayer(new ArcGISDynamicMapServiceLayer(
				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/Basemaps/MapServer"));

//		// TRANSPORTATION
//		mMapView.addLayer(new ArcGISDynamicMapServiceLayer(
//				"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/TransportationBasemap/MapServer"));
		

		for (int i = 0; i < layers.length; i++) {
			int layer = layers[i];
			switch (layer) {
			case -10: // FOREST PHOTO, BACKGOUND
				mMapView.addLayer(new ArcGISDynamicMapServiceLayer( 
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/UWBG/MapServer"));
				break;
			case 0: // WALKS
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/0",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 1: // GARDENS
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/1",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 2: // Survey
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/2",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 3: // PLANTS
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/3",
						ArcGISFeatureLayer.MODE.ONDEMAND));
				break;
			case 4: // TOPOGRAPHY
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/4",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 5: // BENCHES AND MEMORIALS
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/5",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 6: // LANDMARKS
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/6",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 7: // PARKING AND HARDSCAPE
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/7",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 8: // ROADS
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/8",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 9: // TRAILS
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/9",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 10:// WATER
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/10",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 11: // MASSES
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/11",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 12: // NATIVE PLANT MASSES
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/12",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 13: // BEDS
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/13",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 14: // ARBORETUM BOUNDARY
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/14",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			case 15:// SQUARES
				mMapView.addLayer(new ArcGISFeatureLayer(
						"http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer/15",
						ArcGISFeatureLayer.MODE.SNAPSHOT));
				break;
			default:
				break;
			}
		}
	 }
}