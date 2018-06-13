package com.example.zengt9949.mymapsapp;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.util.RangeValueIterator;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zengt9949.mymapsapp.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

//Todo 1:  Create Google maps project, MyMapsApp, select at least API17, Google Maps activity
//      Check out google_maps_api.xml.  Need API key, follow link in google_maps_api.xml
//      Select Create a Project, select Create API key, Restrict key to Android apps,
//      copy/paste the key into google_maps_api.xml file.
//      Run app.  Should see map with Sydney highlighted.  If not:
//      May need to update Google play services, will need wifi connectivity.
//


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;
    private boolean gotMyLocationOneTime;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private double latitude, longitude;
    private double previousLatitude, previousLongitude;
    private boolean notTrackingMyLocation;
    private int trackMarkerDropCounter = 0;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;  //updates in msec
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    private String[] results;
    private LatLng searchQ;


    //Tygan
    private double lastLocationX = 0.0;
    private double lastLocationY = 0.0;
    private double currentLocationX = 0.0;
    private double currentLocationY = 0.0;
    private double distanceX;
    private double distanceY;
    private double distanceX2;
    private double distanceY2;
    private double slope;
    //Tygan



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

//Todo 2:  onCreate implements onMapReadyCallback via getMapAsync call, we need to implement onMapReady.
//      Assignment#1:  Change the location of the initial marker to your place of birth and display
//                     the message "Born Here" if tap the marker and move camera toawards the marker.
//                     Check out api's LatLng, GoogleMap.addMarker, GoogleMap.animateCamera.
//      Assignment#2:  Add method call that drops a dot at your current location.
//                     Check out api setMyLocatonEnabled, add permission check.
//                     Device should ask user to allow location access permission.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

// Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Assignment #1:  Place of birth marker
        LatLng milwaukee = new LatLng(43.0389, -87.9065);  //Milwaukee, WI
        mMap.addMarker(new MarkerOptions().position(milwaukee).title("Born Here!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(milwaukee));

        //Assignment #2:  dot at my location
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("MyMaps", "Failed Permission check 1");
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
//        }
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("MyMaps", "Failed Permission check 2");
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
//        }
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
//            mMap.setMyLocationEnabled(true);


        //From #4 create locationSearch
        locationSearch = (EditText) findViewById(R.id.editText_addr);

// Todo 5a:  Write method getLocation to place a marker at current location
        //Place a marker at current location then disable tracking (to save power)
        //See below for getLocation notes

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMaps", "Failed Permission check 1");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMaps", "Failed Permission check 2");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            gotMyLocationOneTime = false;
            getLocation();
        }


    }


//Todo 3:  Add View button and method to switch between satellite and map views
//      Add linearLayout to activity_maps.xml file - see xml code.
//      Add button called View and bind it to changeView method.
//      Write method changeView to handle the button tap and change the map.
//      See GoogleMap methods (students search on own -- getMapType, setMapType.)

    public void changeView(View v) {
        Log.d("MyMaps", "changing view");

        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }


//Todo 4:  Add search bar to search for a location on the map
//      Add linearLayout to activity_maps.xml file - see xml code
//      Add onSearch method:
//          - create locationSearch EditText, locationManager and myLocation references (top of class)
//            and add the assignment in onCreate
//          - retrieve location from the search box
//          - create a list of addresses (set of strings describing a location in xAL extensible Address Language)
//          - get the location if it exists and if so create a geocoder obj to translate street into lat/lon coord
//          - place marker on map
//      Note - onSearch2 is for using radarsearch (gets multiple markers)
//          - need to add Jsoup into module build.gradle first so don't import wrong classes

    public void onSearch(View v) {
        //Retrieve the location in the search box

        String location = locationSearch.getText().toString();

        //Create a list of Addresses - see Android api
        //Address is a set of Strings describing a location
        //Format is in xAL (eXtensible Address Language)
        List<Address> addressList = null;
        List<Address> addressListZip = null;


        //Use LocationManager for user location since getMyLocation has been deprecated
        //Implement the LocationListener interface to setup location services

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMaps", "onSearch:  location= " + location);
        Log.d("MyMaps", "onSearch:  provider= " + provider);


        LatLng userLocation = null;
        try {
            //Using getLastKnownLocation caused errors when connecting to school wifi
            //though was ok at home so implement the LocationListener interface and
            //use a call to getLocation() instead
            //myLocation = service.getLastKnownLocation(provider);

            //Try to get myLocation, if not then getLocation and wait for myLocation to be updated
            //After myLocation is updated then disable subsequent location updates that
            //are used in trackMyLocation


            //Check the last known location, need to specifically list the provider (network or gps)
            if (locationManager != null) {
                Log.d("MyMaps", "onSearch: locationManager is not null");
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMaps", "onSearch: using NETWORK_PROVIDER userLocation is " + myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMaps", "onSearch: using GPS_PROVIDER userLocation is " + myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else {
                    Log.d("MyMaps", "onSearch: myLocation is null from getLastKnownLocation with Network provider");
                }

            } else {
                Log.d("MyMaps", "onSearch: myLocation is null!!!");

            }


        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMaps", "Exception getLastKnowLocation");
            Toast.makeText(this, "Exception getLastKnownLocation", Toast.LENGTH_SHORT);
        }


        //******

        //Get the location if it exists
        //if (location != null || !location.equals("")) { //This doesn't work!!!
        if (!location.matches("")) {

            Log.d("MyMaps", "onSearch:  location field is populated");

            //Create a Geocoder obj - translates "street addr" into geo (lat/lon) coords
            //Add a second parameter if want to limit the locale
            //Geocoder geocoder = new Geocoder(this);
            Geocoder geocoder = new Geocoder(this, Locale.US);

            Log.d("MyMaps", "onSearch:  created a new Geocoder");

            try {
                //Get list of Addresses based on a descriptive name (e.g. Wash Monument)
                //2nd param is the number of results to return
                //    addressList = geocoder.getFromLocationName(location, 1);
                //To get multiple markers around an area (e.g. 5), use the getFromLocationName to
                //specify the upper and lower lat/lon.  Use an equation to window the user
                //current location with approx radius of 5miles.  Note that 1/60 of a degree is
                //approx 1mile

                //Use LocationManager since getMyLocation has been deprecated
                //addressList = geocoder.getFromLocationName(location, 10000);

                //Try to find around the 91230 (for the Vons on Valley Center Drive)
                //addressListZip = geocoder.getFromLocationName("92130", 1);
                //                double lat = addressListZip.get(0).getLatitude();
                //                double lon = addressListZip.get(0).getLongitude();
                //                Log.d("MyMaps", lat + " " + lon);
                //                addressList = geocoder.getFromLocationName(location, 10,
                //                        lat-2.0/60.0,lon-2.0/60.0,lat+2.0/60.0,lon+2.0/60.0);
                //                Log.d("MyMaps", "yyyyyAddress list size= " + addressList.size());

                addressList = geocoder.getFromLocationName(location, 10000,
                        userLocation.latitude - (5.0 / 60.0),
                        userLocation.longitude - (5.0 / 60.0),
                        userLocation.latitude + (5.0 / 60.0),
                        userLocation.longitude + (5.0 / 60.0));

                Log.d("MyMaps", "onSearch:  created addressList");

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Log.d("MyMaps", "Address list size= " + addressList.size());

                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    //Place the marker on the map
                    //Display the location string
                    //mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    //Display the address
                    //mMap.addMarker(new MarkerOptions().position(latLng).title(address.getAddressLine(i)));
                    //mMap.addMarker(new MarkerOptions().position(latLng).title(address.getPostalCode()));
                    //mMap.addMarker(new MarkerOptions().position(latLng).title(address.getThoroughfare()));
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()
                            + " " + address.getThoroughfare()));

                    Log.d("MyMaps", "onSearch:  added Marker");
                    //Focus the camera to the marker
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                }
            }

        }

    }

    // Todo 5b:  Write method getLocation to place a marker at current location
    //Write method getLocation.   Need this to setup provider used in locationManager.
    //Write locationListenerNetwork, locationListenerGPS anonymous inner classes
    //Make sure have ACCESS_COARSE_LOCATION set in AndroidManifest
    public void getLocation() {
        //Setup up try-catch structure with exception for locationManager
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            // isProviderEnabled returns true if user has enabled gps on phone
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) Log.d("MyMaps", "getLocation: GPS is enabled");

            //-----STUDENTS DO THIS WITH HINTS-----
            // getting network status (cell tower + wifi ap's)
            // isProviderEnabled returns true if user has enabled cellular and wifi (e.g. off if airplane mode is on)
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMaps", "getLocation: Network is enabled");

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Log.d("MyMaps", "getLocation: No provider is enabled!!");
            } else {
                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network Enabled - requesting Location Updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Log.d("MyMaps", "getLocation: failed Network permission check!! Returning");
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                    Log.d("MyMaps", "getLocation: NetworkLoc update request successful");
                    //                   Toast.makeText(this, "Using network", Toast.LENGTH_SHORT);
                }
                if (isGPSEnabled)
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
            }

        } catch (Exception e) {
            Log.d("MyMaps", "Caught exception in getLocation()");
            e.printStackTrace();
        }

    }

    //LocationListener is an anonymous inner class
    //    Location Listener is “inner class” or nested class
    //    Set up as object instantiation but include braces to have it’s own methods
    //    We set this up for the callbacks that are part of the locationManager.requestLocationUpdates call (see the android api)

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "locationListenerNetwork - onLocationChanged  dropping marker");
//            Toast.makeText(MapsActivity.this, "locationListenerNetwork - onLocationChanged drop marker", Toast.LENGTH_SHORT).show();


            dropAmarker(LocationManager.NETWORK_PROVIDER);

            //Check if doing this one time onCreate if so remove updates to both gps and network
            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
                gotMyLocationOneTime = true;
                //Update the previous lat/long so can calc distance
                previousLatitude = latitude;
                previousLongitude = longitude;
            } else {
                //If here then tracking location so relaunch request for network
                //Todo find out why network doesn't continually update like gps does
                if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

                //Calculate the distance (in meters) from the previous marker to this one and keep a running total
//                Location loc1 = new Location("");
//                loc1.setLatitude(previousLatitude);
//                loc1.setLongitude(previousLongitude);

//                Location loc2 = new Location("");
//                loc2.setLatitude(latitude);
//                loc2.setLongitude(longitude);
//                totalDistanceTraveled += loc1.distanceTo(loc2) - DISTANCE_ERROR_ESTIMATE;
//                trackMarkerDropCounter++;
//                Toast.makeText(MapsActivity.this, trackMarkerDropCounter + " Distance Traveled: " +
//                        totalDistanceTraveled + " Accuracy: " + myLocation.getAccuracy(), Toast.LENGTH_SHORT).show();
//                Log.d("MyMaps", "dropAmarker: totalDistanceTraveled: " + totalDistanceTraveled);


            }


        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMaps", "locationListenerNetwork:  onStatusChanged callback");
//            Toast.makeText(MapsActivity.this, "locationListenerNetwork: onStatusChanged callback", Toast.LENGTH_SHORT).show();
        }
    };

    //Separate the LocationListener callbacks with inner class structure
    //use LocationListener from android.location so will get all the methods
    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "locationListenerGPS - onLocationChanged  dropping marker");
//            Toast.makeText(MapsActivity.this, "locationListenerGPS - onLocationChanged drop marker", Toast.LENGTH_SHORT).show();


            //Drop a marker
            dropAmarker(LocationManager.GPS_PROVIDER);

            //Check if doing this one time onCreate if so remove updates to both gps and network
            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
                //Update the previous lat/long so can calc distance
                previousLatitude = latitude;
                previousLongitude = longitude;

            } else {
                //If here then tracking location so remove network updates since gps is
                //more accurate
//                locationManager.removeUpdates(locationListenerNetwork);

                //Calculate the distance (in meters) from the previous marker to this one and keep a running total
//                Location loc1 = new Location("");
//                loc1.setLatitude(previousLatitude);
//                loc1.setLongitude(previousLongitude);
//
//                Location loc2 = new Location("");
//                loc2.setLatitude(latitude);
//                loc2.setLongitude(longitude);
//                totalDistanceTraveled += loc1.distanceTo(loc2) - DISTANCE_ERROR_ESTIMATE;
//                trackMarkerDropCounter++;
//                Toast.makeText(MapsActivity.this, trackMarkerDropCounter + " Distance Traveled: " +
//                        totalDistanceTraveled + " Accuracy: " + myLocation.getAccuracy(), Toast.LENGTH_SHORT).show();
//                Log.d("MyMaps", "dropAmarker: totalDistanceTraveled: " + totalDistanceTraveled);

            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    //Todo:  disable network updates (won't continually call so don't need to manually disable)
                    Log.d("MyMaps", "locationListenerGPS - onStatusChanged AVAIL");
                    Toast.makeText(MapsActivity.this, "locationListenerGPS - onStatusChanged - AVAIL", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    //Todo:  enable network updates
                    Log.d("MyMaps", "locationListenerGPS - onStatusChanged OUTOFSERVICE");
                    Toast.makeText(MapsActivity.this, "locationListenerGPS - onStatusChanged - OUT", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                    //Enable both Network and GPS updates if tracking
                    if (!notTrackingMyLocation){
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "locationListenerGPS - onStatusChanged TEMP UNAVAIL");
                    Toast.makeText(MapsActivity.this, "locationListenerGPS - onStatusChanged - TEMP UNAVAIL", Toast.LENGTH_SHORT).show();

                    //Enable both Network and GPS updates if tracking
                    if (!notTrackingMyLocation){
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }

                    break;
                default:
                    Log.d("MyMaps", "locationListenerGPS - onStatusChanged default");
                    Toast.makeText(MapsActivity.this, "locationListenerGPS - onStatusChanged - default", Toast.LENGTH_SHORT).show();

                    //Enable both Network and GPS updates if tracking
                    if (!notTrackingMyLocation){
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }

                    break;
            }
        }
    };


    public void dropAmarker(String provider) {

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);

            //This is already done in the else clause below - don't need the conditional
            if (myLocation != null) {
                latitude = myLocation.getLatitude();
                longitude = myLocation.getLongitude();
            }
        }

        LatLng userLocation=null;

        if (myLocation == null){
            Toast.makeText(this, "dropAmarker:  myLocation is null, can't show location!!" , Toast.LENGTH_SHORT).show();

        } else {
            userLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
//            Toast.makeText(this, "dropAmarker: Provider: " + provider + " " + myLocation.getLatitude()
//                    + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            Log.d("MyMaps","dropAmarker: Provider: " + provider + " " + myLocation.getLatitude()
                    + " " + myLocation.getLongitude());
            Log.d("MyMaps", "dropAmarker: myLocation accuracy: " + myLocation.getAccuracy());

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
            //mMap.addMarker(new MarkerOptions().position(userLocation).title("I'm here"));

                /*mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("I'm here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))); */

            if (provider == LocationManager.GPS_PROVIDER){
                //Add circles for the marker with 2 outer rings
                mMap.addMarker(new MarkerOptions().position(userLocation).title("X: " + myLocation.getLatitude() + " Y: " + myLocation.getLongitude()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            } else {
                //Add circles for the marker with 2 outer rings
                /*Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.BLUE));

                Circle circleOuterRing1 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(3)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));

                Circle circleOuterRing2 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(5)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));*/
            }

            mMap.animateCamera(update);


        }

        //Tygan
        if (provider == LocationManager.GPS_PROVIDER) {
            lastLocationX = currentLocationX;
            lastLocationY = currentLocationY;
            currentLocationX = myLocation.getLongitude();
            currentLocationY = myLocation.getLatitude();
            Toast.makeText(MapsActivity.this, +myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_LONG).show();
        }


        //Tygan



    }

    public void trackMyLocation (View view) {

        //Kick off the location tracker using getLocation which starts the LocationListeners
        // If notTrackingMyLocation (instance var) then call getLocation
        // else removeUpdates for both Network and GPS LocationListeners
        Log.d("MyMaps", "Tracking now");

        if (notTrackingMyLocation) {
            getLocation();  //updates private var myLocation
            Toast.makeText(this, "trackMyLocation: tracking is ON", Toast.LENGTH_SHORT).show();
            Log.d("MyMaps","trackMyLocation: tracking is ON");
            notTrackingMyLocation = false;
        }else {
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);
            Toast.makeText(this, "trackMyLocation: tracking is OFF", Toast.LENGTH_SHORT).show();
            Log.d("MyMaps","trackMyLocation: tracking is OFF");
            notTrackingMyLocation = true;
        }
    }

    public void clearMarkers(View view){

        //Zero the distance and counter
        trackMarkerDropCounter = 0;
//        totalDistanceTraveled = 0;

        //Clear markers from map
        mMap.clear();
    }


    public IBinder onBind(Intent arg0) {
        return null;
    }


    public void onSearch2(View v) {

        //Tygan
        if (currentLocationX - lastLocationX == 0){
            Toast.makeText(MapsActivity.this,"user hasn't moved",Toast.LENGTH_SHORT).show();
        }
        else {
            slope = (currentLocationY -lastLocationY ) / (currentLocationX - lastLocationX);

        }

        Toast.makeText(MapsActivity.this,"slope is " + slope,Toast.LENGTH_SHORT).show();
        if (slope == 0){
            Toast.makeText(MapsActivity.this,"slope is " + slope,Toast.LENGTH_SHORT).show();
        }
        else{
            slope = -1*(1/slope);
        }

        //Tygan

        //This loop is a hack since the returned string has format inconsistencies which result in errors when parsing
        //Doing it 10x is a hack to get one set of lat/lons for each location that work
        for (int i = 0; i<10;i++){
            Log.d("MyMaps", "Search activated");

            //Get the POI url, eg.  https://maps.googleapis.com/maps/api/place/radarsearch/json?keyword=Starbucks&location=32.959076,-117.189433&radius=9000&key=AIzaSyAO_vWgA5nwAC-KAV_en4p-1GXPYpbg__M
            String siteUrl = "https://maps.googleapis.com/maps/api/place/radarsearch/json?keyword=" +
                    locationSearch.getText().toString() + "&location=" +
                    latitude + "," + longitude + "&radius=9000&key=AIzaSyAO_vWgA5nwAC-KAV_en4p-1GXPYpbg__M";

            Log.d("MyMaps", "POI url: " + siteUrl);
            (new ParseURL()).execute(new String[]{siteUrl});
        }
    }


    private class ParseURL extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String buffer = new String();
            try {
                Log.d("MyMaps", "Connecting to [" + strings[0] + "]");
                Document doc = Jsoup.connect(strings[0]).ignoreContentType(true).get();
                Log.d("MyMaps", "Connected to [" + strings[0] + "]");
                // Get document (HTML page) title

                Element bod = doc.body();
                buffer = ("BOD TEXT  " + bod.text() );
                Log.d("MyMaps", ""+buffer.toString());
                results = new String[201];
                int j = 0;
                while (buffer.indexOf("location")>-1){
                    results[j] = buffer.substring(buffer.indexOf("lat")+7,buffer.indexOf("lng")-3 ) + " " + buffer.substring(buffer.indexOf("lng")+7, buffer.indexOf("lng")+16);
                    buffer = buffer.substring(buffer.indexOf("lng")+20);
                    j++;
                }


                Log.d("MyMaps", ""+buffer.toString());
                for (String str: results){
                    Log.d("MyMaps", ""+str);
                }
                markMaker();



            } catch (Throwable t) {
                Log.d("MyMaps", "ERROR");
                t.printStackTrace();
            }

            return buffer.toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //respText.setText(s);
        }
    }

    ArrayList<LatLng> searchLatLngList = new ArrayList<>();   //Tygan

    public void markMaker(){



        for (String str: results){


            Log.d("MyMaps", "Start Marking");
            searchQ = new LatLng(Double.parseDouble(str.substring(0,str.indexOf(" ")-1)), Double.parseDouble(str.substring(str.indexOf(" ")+1)));
            searchLatLngList.add(searchQ);  //Tygan

            Log.d("MyMaps", ""+searchQ.latitude + " "+searchQ.longitude);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    /*Circle circle = mMap.addCircle(new CircleOptions()
                            .center(searchQ)
                            .radius(1)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.BLUE));

                    Circle circleOuterRing1 = mMap.addCircle(new CircleOptions()
                            .center(searchQ)
                            .radius(3)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT));
*/
                    /*Circle circleOuterRing2 = mMap.addCircle(new CircleOptions()
                            .center(searchQ)
                            .radius(7)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2)
                            .fillColor(Color.TRANSPARENT));*/
                    //mMap.addMarker(new MarkerOptions().position(searchQ).title(locationSearch.getText().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    //Tygan
                    if(lastLocationY<(1*slope*lastLocationX-slope*currentLocationX + currentLocationY)){
                        if(searchQ.latitude>(1*slope*searchQ.longitude-slope*currentLocationX + currentLocationY)){
                            mMap.addMarker(new MarkerOptions().position(searchQ).title(locationSearch.getText().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }
                    }

                    if(lastLocationY>(1*slope*lastLocationX-slope*currentLocationX + currentLocationY)){
                        if(searchQ.latitude<(1*slope*searchQ.longitude-slope*currentLocationX + currentLocationY)){
                            mMap.addMarker(new MarkerOptions().position(searchQ).title(locationSearch.getText().toString()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }
                    }
                    //Tygan

                }
            });
            Log.d("MyMaps", "Stop Marking");
        }







    }


}