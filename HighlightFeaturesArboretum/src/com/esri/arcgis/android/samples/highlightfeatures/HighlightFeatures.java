/*
 /*
 * Copyright 2010 ESRI
 * All rights reserved under the copyright laws of the United States and applicable international laws, treaties, and conventions.
 * You may freely redistribute and use this sample code, with or without modification, provided you include the original copyright notice and use restrictions.
 * Disclaimer: THE SAMPLE CODE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ESRI OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) SUSTAINED BY YOU OR A THIRD PARTY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT ARISING IN ANY WAY OUT OF THE USE OF THIS SAMPLE CODE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts and Legal Services Department
 * 380 New York Street Redlands, California, 92373
 * USA
 * email: contracts@esri.com 
 */

package com.esri.arcgis.android.samples.highlightfeatures;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.MapOptions.MapType;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.esri.core.tasks.identify.IdentifyResult;
import com.esri.core.tasks.identify.IdentifyTask;

public class HighlightFeatures extends Activity {
	
	private final static String TAG = "HighlightFeatures";

  // ArcGIS Android elements
  MapView mMapView;
  //ArcGISTiledMapServiceLayer mTiledMapServiceLayer;
  ArcGISDynamicMapServiceLayer mTiledMapServiceLayer;
  GraphicsLayer mGraphicsLayer;
  Graphic[] mHighlightGraphics;
  
  ArrayList<IdentifyResult> identifyResults;

  // Android UI elements
  Button mClearButton;
  Button mLayerButton;
  TextView mLabel;
  TextView mIdRes;

  //String mMapURL = "http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/PublicSafety/PublicSafetyBasemap/MapServer";
  String mMapURL = "http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/UWBG/MapServer"; 
  //String mMapURL =  "http://uwbgmaps.cfr.washington.edu/arcgis/rest/services/PublicFeatures/MapServer";    	  	  
  
  final String[] mLayerNames = new String[] { 
		  "Survey", 				"Plants", 					"UWBG_Boundary_Outline", 	"Water", 
		  "Landmarks",				"Irrigation and Utility",	"Benches and Memorial",		"Hardscape and Parking",
		  "Other",					"Roads",					"Trails",					"Plant Masses",	
		  "Native Plant Masses",	"Beds",						"Squares",					"T519737",
		  "T519752",				"T519767",					"T519782",					"T534737",
		  "T534752",				"T534767",					"T534782" };

  final int[] mLayerIndexes = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22}; 

  int mSelectedLayerIndex = -1;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);

    // Initialize ArcGIS Android MapView
    mMapView = (MapView) findViewById(R.id.map);

    // Long Press listener for map view
    mMapView.setOnLongPressListener(new OnLongPressListener() {

      private static final long serialVersionUID = 1L;

      /*
       * Invoked when user does a Long Press on map. This fires an identify
       * query for features covered by user's finder, on selected layer.
       */
      @Override
      public boolean onLongPress(float x, float y) {
        try {
          if (mTiledMapServiceLayer.isInitialized() && mSelectedLayerIndex >= 0) {

            mGraphicsLayer.removeAll();

            // Get the point user clicked on
            Point pointClicked = mMapView.toMapPoint(x, y);

            // Set parameters for identify task
            IdentifyParameters inputParameters = new IdentifyParameters();
            inputParameters.setGeometry(pointClicked);
            inputParameters.setLayers(new int[] { mLayerIndexes[mSelectedLayerIndex] });
            Envelope env = new Envelope();
            mMapView.getExtent().queryEnvelope(env);
            inputParameters.setSpatialReference(mMapView.getSpatialReference());
            inputParameters.setMapExtent(env);
            inputParameters.setDPI(96);
            inputParameters.setMapHeight(mMapView.getHeight());
            inputParameters.setMapWidth(mMapView.getWidth());
            inputParameters.setTolerance(10);


            // Execute identify task
            MyIdentifyTask mIdenitfy = new MyIdentifyTask();
            mIdenitfy.execute(inputParameters);

          } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Please select a layer to identify features from.",
                Toast.LENGTH_SHORT);
            toast.show();
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
        return true;
      }
    });

    // Initialize LayerButton, Clear button, and Label
    mLayerButton = (Button) findViewById(R.id.layerbutton);
    mLayerButton.setEnabled(false);
    mLayerButton.setOnClickListener(new View.OnClickListener() {
      /*
       * This displays an AlertDilaog as defined in onCreateDialog() method.
       * Invocation of show() causes onCreateDialog() to be called internally.
       */
      @Override
      public void onClick(View v) {
        showDialog(0);
      }
    });

    mLabel = (TextView) findViewById(R.id.label);

    mClearButton = (Button) findViewById(R.id.clearbutton);
    mClearButton.setEnabled(false);
    mClearButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mGraphicsLayer.removeAll();
        mClearButton.setEnabled(false);
      }
    });

    /*
     * Retrieve application state after device is flipped. The
     * onRetainNonConfigurationInstance() method allows us to persist states of
     * elements of the application, including ArcGIS Android entitied, and
     * Android UI element. When device is flipped and onCreate() gets invoked,
     * the getLastNonConfigurationInstance() method will provide access to state
     * of above mentioned elements, which can be used to restore the app to its
     * previous state.
     */
    Object[] init = (Object[]) getLastNonConfigurationInstance();
    if (init != null) {
      // Retrieve map view state
      mMapView.restoreState((String) init[0]);

      int index = ((Integer) init[1]).intValue();
      if (index != -1) {
        mLabel.setText(mLayerNames[index]);
        mSelectedLayerIndex = index;
      }
    } else {
      /*
       * Initialize MapView, TiledMapServiceLayer and GraphicsLayer. This block
       * will be executed when app is started the first time.
       */
      //mMapView.setExtent(new Envelope(-85.61828847183895, 38.19242311866144, -85.53589100936443, 38.31361605305102));
    }

		//mTiledMapServiceLayer = new ArcGISTiledMapServiceLayer(mMapURL);
		mTiledMapServiceLayer = new ArcGISDynamicMapServiceLayer(mMapURL);
    	mGraphicsLayer = new GraphicsLayer();

    /*
     * Use TiledMapServiceLayer's OnStatusChangedListener to listen to events
     * such as change of status. This event allows developers to check if layer
     * is indeed initialized and ready for use, and take appropriate action. In
     * this case, we are modifying state of other UI elements if and when the
     * layer is loaded.
     */
    mTiledMapServiceLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(Object arg0, STATUS status) {
        /*
         * Check if layer's new status = INITIALIZED. If it is, initialize UI
         * elements
         */
        if (status.equals(OnStatusChangedListener.STATUS.INITIALIZED)) {
          mLayerButton.setEnabled(true);
        }
      }
    });

    // Add TiledMapServiceLayer and GraphicsLayer to map
    mMapView.addLayer(mTiledMapServiceLayer);
    mMapView.addLayer(mGraphicsLayer);
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    Object[] objs = new Object[2];
    objs[0] = mMapView.retainState();
    objs[1] = Integer.valueOf(mSelectedLayerIndex);
    return objs;
  }

  /**
   * Returns an AlertDialog that includes names of all layers in the map service
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    return new AlertDialog.Builder(HighlightFeatures.this).setTitle("Select a Layer")
        .setItems(mLayerNames, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mLabel.setText(mLayerNames[which] + " selected.");
        
            mSelectedLayerIndex = which;

            Toast toast = Toast.makeText(getApplicationContext(), "Identify features by pressing for 2-3 seconds.",
                Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
          }
        }).create();
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

  private class MyIdentifyTask extends AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

    IdentifyTask mIdentifyTask;

    @Override
    protected IdentifyResult[] doInBackground(IdentifyParameters... params) {
      IdentifyResult[] mResult = null;
      if (params != null && params.length > 0) {
        IdentifyParameters mParams = params[0];
        try {
          mResult = mIdentifyTask.execute(mParams);
        } catch (Exception e) {
          e.printStackTrace();
        }

      }
      return mResult;
    }

    @Override
    protected void onPostExecute(IdentifyResult[] results) {
      if (results != null && results.length > 0) {

        mHighlightGraphics = new Graphic[results.length];
        String msg = results.length + " features identified\n";
        Log.i(TAG, msg);
        Toast toast = Toast.makeText(getApplicationContext(), msg,
            Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();

        // Highlight all features that match with results
        for (int i = 0; i < results.length; i++) {
          Geometry geom = results[i].getGeometry();
          
          Map<String, Object> attr = results[i].getAttributes();
          if (attr != null) {
        	  if (attr.containsKey("Name")) {
        		  String feature = (String)attr.get("Name");
        		  msg = "Feature name: " + feature;
                  Log.i(TAG, msg);                  
        	  }
          }                   
         
          String typeName = geom.getType().name();

          //Random r = new Random();	
          //int color = Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255));
          
          int color = Color.rgb(255, 255, 00);	// red

          // Create appropriate symbol, based on geometry type
          if (typeName.equalsIgnoreCase("point")) {
            SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color, 20, STYLE.SQUARE);
            mHighlightGraphics[i] = new Graphic(geom, sms);
          } else if (typeName.equalsIgnoreCase("polyline")) {
            SimpleLineSymbol sls = new SimpleLineSymbol(color, 5);
            mHighlightGraphics[i] = new Graphic(geom, sls);
          } else if (typeName.equalsIgnoreCase("polygon")) {
            SimpleFillSymbol sfs = new SimpleFillSymbol(color);
            //sfs.setAlpha(75);
            mHighlightGraphics[i] = new Graphic(geom, sfs);
          }

          // set the Graphic's geometry, add it to GraphicLayer and
          // refresh the Graphic Layer
          mGraphicsLayer.addGraphic(mHighlightGraphics[i]);
          mClearButton.setEnabled(true);
        }
      } else {
    	String msg = "No features identified.";
        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
        Log.i(TAG, msg);
      }

    }

    @Override
    protected void onPreExecute() {
      mIdentifyTask = new IdentifyTask(mMapURL);
    }

  }

}
