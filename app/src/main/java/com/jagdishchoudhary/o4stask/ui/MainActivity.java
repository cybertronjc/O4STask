package com.jagdishchoudhary.o4stask.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.jagdishchoudhary.o4stask.helper.GoldenReceiver;
import com.jagdishchoudhary.o4stask.helper.Moon;
import com.jagdishchoudhary.o4stask.database.PlaceDatabase;
import com.jagdishchoudhary.o4stask.model.PlaceEntity;
import com.jagdishchoudhary.o4stask.R;
import com.jagdishchoudhary.o4stask.helper.RisingSettingAlgorithm;
import com.jagdishchoudhary.o4stask.helper.Astro;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 301;
    private boolean mLocationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_CODE = 101;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    List<AutocompletePrediction> predictionList;

    private static final int M_MAX_ENTRIES = 10;

    private static final int AUTOCOMPLETE_REQUEST_CODE = 1001;

    private static final float DEFAULT_ZOOM = 16f;

    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -100), new LatLng(71, 136));

    private FusedLocationProviderClient fusedLocationProviderClient;

    ListView listPlaces;
    List<PlaceEntity> list = new ArrayList<>();

    RisingSettingAlgorithm risingSettingAlgorithm;

    AutoCompleteTextView searchText;
    ImageView gpsImg, previous, next, nearby, saved_places, bookmark_place;
    PlacesClient placesClient;
    Context ctx;
    TextView responseView, sunUp, sunDown, moonUp, moonDown, dateText;
    MaterialSearchBar searchBar;
    Date dt;
    Calendar c;
    LatLng currentLatLng, placeLatLng;
    ProgressBar progressBar;
    ConstraintLayout timeLayout, sunLayout, moonLayout;

    PlaceDatabase placeDatabase ;
    PlaceEntity placeEntity;
    boolean notificationSet = false;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ctx = this.getApplicationContext();

        placeDatabase = PlaceDatabase.getInstance(ctx);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //searchBar = findViewById(R.id.searchBar);

        if (!Places.isInitialized()) {
            String gApiKey = "AIzaSyDDSzJss37KUPKKicoBdCD0SguiazThfZk";
            Places.initialize(ctx, gApiKey);
        }
        placesClient = Places.createClient(ctx);



        searchText = findViewById(R.id.search_text);

        gpsImg = findViewById(R.id.gps_button);

        sp = getSharedPreferences("default", Context.MODE_PRIVATE);

        if (isPlayServicesOk()){
            getLocationPermission();

        }

        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AutocompleteSessionToken token =  AutocompleteSessionToken.newInstance();
                autoComplete();
            }
        });


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init(){

        hideKeyyboard();

        sunUp = findViewById(R.id.text1);
        sunDown = findViewById(R.id.text2);
        moonUp = findViewById(R.id.text4);
        moonDown = findViewById(R.id.text5);
        dateText = findViewById(R.id.date_text);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        nearby = findViewById(R.id.nearby_button);
        progressBar = findViewById(R.id.progressBar);
        timeLayout = findViewById(R.id.timeLayout);
        sunLayout = findViewById(R.id.const_layout2);
        moonLayout = findViewById(R.id.const_layout3);
        saved_places = findViewById(R.id.saved_places);
        bookmark_place = findViewById(R.id.bookmark);


        dt = new Date();
        c = Calendar.getInstance();
        c.setTime(dt);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(dt);

        dateText.setText(formattedDate);

        risingSettingAlgorithm = new RisingSettingAlgorithm();

        //Date c = Calendar.getInstance().getTime();
        //System.out.println("Current time => " + c);



        gpsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Gps button clicked");
                checkGPS();
                placeEntity = null;
                searchText.setText("");
                bookmark_place.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_border_black_24dp));

            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                sunLayout.setVisibility(View.INVISIBLE);
                moonLayout.setVisibility(View.INVISIBLE);
                decreaseDate(currentLatLng);
            }
        });


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                sunLayout.setVisibility(View.INVISIBLE);
                moonLayout.setVisibility(View.INVISIBLE);
                increaseDate(currentLatLng);
            }
        });

        nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//
                getCurrentPlaceLikelihoods();
            }
        });



        bookmark_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (placeEntity != null) {
                    if (placeDatabase.getPlaceDao().getAll().toString().contains(placeEntity.getName())) {
                        Toast.makeText(ctx, "Place already saved", Toast.LENGTH_SHORT).show();
                    } else {
                        new InsertPlace(ctx, placeEntity, placeDatabase).execute();
                        bookmark_place.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_black_24dp));
                    }
                }
                else {
                    Toast.makeText(ctx, "Select place from search bar or nearby places", Toast.LENGTH_SHORT).show();
                }

            }
        });

        saved_places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list = placeDatabase.getPlaceDao().getAll();

                final String[] places = new String[list.size()];
                final Double[] placesLats = new Double[list.size()];
                final String[] placeNames = new String[list.size()];
                final Double[] placesLngs = new Double[list.size()];
                for (int i = 0; i < list.size(); i++){
                    places[i] = list.get(i).getAddress();
                    placesLats[i] = list.get(i).getLat();
                    placesLngs[i] = list.get(i).getLng();
                    placeNames[i] = list.get(i).getName();
                }

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Your saved places")
                        .setItems(placeNames, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // i shows position
                                //gender_edit.setText(genList[i]);
                                Toast.makeText(ctx,places[i], Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.VISIBLE);
                                sunLayout.setVisibility(View.INVISIBLE);
                                moonLayout.setVisibility(View.INVISIBLE);
                                calculateTimeAndUpdateUi(new LatLng(placesLats[i], placesLngs[i]),dt);
                                moveCamera(new LatLng(placesLats[i], placesLngs[i]), DEFAULT_ZOOM, placeNames[i]);
                                placeEntity = new PlaceEntity(0,placeNames[i], places[i], placesLats[i], placesLngs[i]);
                                bookmark_place.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_black_24dp));
                                searchText.setText(placeNames[i]);
                            }
                        })
                        .show();
                builder.create();
            }
        });




    }

    private void increaseDate(LatLng latLng){

        c.add(Calendar.DATE, 1);
        dt = c.getTime();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(dt);

        dateText.setText(formattedDate);

        calculateTimeAndUpdateUi(latLng, dt);

    }

    private void decreaseDate(LatLng latLng){
        c.add(Calendar.DATE, - 1);
        dt = c.getTime();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(dt);

        dateText.setText(formattedDate);
        calculateTimeAndUpdateUi(latLng, dt);
    }

    //autocomplete places api
    private void autoComplete(){
        // Start the autocomplete intent.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields).setCountry("IN")
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

    }


    private void getCurrentPlaceLikelihoods() {
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG);

        // Get the likely places - that is, the businesses and other points of interest that
        // are the best match for the device's current location.
        @SuppressWarnings("MissingPermission") final FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(placeFields).build();
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(this,
                new OnCompleteListener<FindCurrentPlaceResponse>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                        if (task.isSuccessful()) {
                            FindCurrentPlaceResponse response = task.getResult();
                            // Set the count, handling cases where less than 5 entries are returned.
                            int count;
                            if (response.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                                count = response.getPlaceLikelihoods().size();
                            } else {
                                count = M_MAX_ENTRIES;
                            }

                            int i = 0;
                            String[] mLikelyPlaceNames = new String[count];
                            String[] mLikelyPlaceAddresses = new String[count];
                            String[] mLikelyPlaceAttributions = new String[count];
                            LatLng[] mLikelyPlaceLatLngs = new LatLng[count];

                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                Place currPlace = placeLikelihood.getPlace();
                                mLikelyPlaceNames[i] = currPlace.getName();
                                mLikelyPlaceAddresses[i] = currPlace.getAddress();
                                mLikelyPlaceAttributions[i] = (currPlace.getAttributions() == null) ?
                                        null : String.join(" ", currPlace.getAttributions());
                                mLikelyPlaceLatLngs[i] = currPlace.getLatLng();

                                String currLatLng = (mLikelyPlaceLatLngs[i] == null) ?
                                        "" : mLikelyPlaceLatLngs[i].toString();

                                Log.i(TAG, String.format("Place " + currPlace.getName()
                                        + " has likelihood: " + placeLikelihood.getLikelihood()
                                        + " at " + currLatLng));

                                i++;
                                if (i > (count - 1)) {
                                    break;
                                }
                            }


                            // COMMENTED OUT UNTIL WE DEFINE THE METHOD
                            // Populate the ListView
                            fillPlacesList(mLikelyPlaceNames, mLikelyPlaceAddresses, mLikelyPlaceAttributions, mLikelyPlaceLatLngs);
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Too many request are not allowed to places API, please try after some time to see nearby places")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();


                            }
                        }
                    }
                });
    }

    private void fillPlacesList(final String[] mLikelyPlaceNames, final String[] mLikelyPlaceAddresses, String[] mLikelyPlaceAttributions, final LatLng[] mLikelyPlaceLatLngs) {
        // Set up an ArrayAdapter to convert likely places into TextViews to populate the ListView
//        listPlaces.setAdapter(placesAdapter);
//        listPlaces.setOnItemClickListener(listClickedHandler);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select nearby place")
                .setItems(mLikelyPlaceNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // i shows position
                        //gender_edit.setText(genList[i]);
                        Toast.makeText(ctx,mLikelyPlaceNames[i], Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.VISIBLE);
                        sunLayout.setVisibility(View.INVISIBLE);
                        moonLayout.setVisibility(View.INVISIBLE);
                        calculateTimeAndUpdateUi(mLikelyPlaceLatLngs[i],dt);
                        moveCamera(new LatLng(mLikelyPlaceLatLngs[i].latitude, mLikelyPlaceLatLngs[i].longitude), DEFAULT_ZOOM, mLikelyPlaceNames[i]);
                        placeEntity = new PlaceEntity(0,mLikelyPlaceNames[i], mLikelyPlaceAddresses[i], mLikelyPlaceLatLngs[i].latitude, mLikelyPlaceLatLngs[i].longitude);

                        if (placeDatabase.getPlaceDao().getAll().toString().contains(placeEntity.getName())){
                            bookmark_place.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_black_24dp));
                        }


                        searchText.setText(mLikelyPlaceNames[i]);
                    }
                })
                .show();
        builder.create();
    }

    private void getDeviceLocation(){

        try {
            if (mLocationPermissionsGranted){
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()){
                            if (task.getResult() != null) {
                                Log.d(TAG, "Location found " + task.getResult().toString());
                                currentLatLng = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                                moveCamera(new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude()), DEFAULT_ZOOM, "My location");
                                placeEntity = new PlaceEntity(0, "MyLocation", "None", task.getResult().getLatitude(), task.getResult().getLongitude());
                            }
                        }

                    }
                });

            }
        }
        catch (SecurityException e){
            Log.d("TAG", e.toString());
        }
    }
    //moving camera to latlng
    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moving camera: "+ latLng.latitude + ", lng:" + latLng.longitude);

        map.clear();

        map.addPolyline(new PolylineOptions().add(latLng, new LatLng(latLng.latitude - 4, latLng.longitude - 5)).color(Color.YELLOW));
        map.addPolyline(new PolylineOptions().add(latLng, new LatLng(latLng.latitude + 4, latLng.longitude - 5)).color(Color.YELLOW));

        map.addPolyline(new PolylineOptions().add(latLng, new LatLng(latLng.latitude - 4, latLng.longitude + 5)).color(Color.GREEN));
        map.addPolyline(new PolylineOptions().add(latLng, new LatLng(latLng.latitude + 4, latLng.longitude + 5)).color(Color.GREEN));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (!title.equals("My location")){
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            map.addMarker(options);
        }

        calculateTimeAndUpdateUi(latLng, dt);


        hideKeyyboard();


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void golderHourNotification(int hour, int min){

        if (!sp.contains("alarm")){
            //Create alarm manager
            AlarmManager alarmMgr0 = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

//Create pending intent & register it to your alarm notifier class
            Intent intent0 = new Intent(this, GoldenReceiver.class);
            PendingIntent pendingIntent0 = PendingIntent.getBroadcast(this, 0, intent0, 0);

//set timer you want alarm to work (here I have set it to 7.20pm)
            Calendar timeOff9 = Calendar.getInstance();
            timeOff9.set(Calendar.HOUR_OF_DAY, hour);
            timeOff9.set(Calendar.MINUTE, min);
            timeOff9.set(Calendar.SECOND, 0);

            Log.d(TAG, Long.toString(timeOff9.getTimeInMillis()));

//set that timer as a RTC Wakeup to alarm manager object
            alarmMgr0.setExact(AlarmManager.RTC, timeOff9.getTimeInMillis(), pendingIntent0);

            sp.edit().putString("alarm", "true").commit();
        }

    }

    //checking where GPS is enabled or not
    private void checkGPS() {
        if (mLocationPermissionsGranted) {
            // getDeviceLocation();

            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            //check if gps is enabled or not
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

            SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);
            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    getDeviceLocation();
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        ResolvableApiException exception = (ResolvableApiException) e;
                        try {
                            exception.startResolutionForResult(MainActivity.this, 51);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }

                    }
                }
            });

        }
    }

    private void calculateTimeAndUpdateUi(final LatLng latLng, final Date date){


        new Handler().postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                //sun's rising time
                String sunRising = risingSettingAlgorithm.getRisingSettingTime(date, new LatLng(latLng.latitude, latLng.longitude), "Rising");


                //sun's setting time
                String sunSetting = risingSettingAlgorithm.getRisingSettingTime(date, new LatLng(latLng.latitude, latLng.longitude), "Setting");

                double[] moonTime = Moon.riseSet(date, Astro.getTimeZoneOffset(date), latLng.latitude, latLng.longitude);
                //Log.d("Moon", Double.toString(moonTime[0]) + "set" + Double.toString(moonTime[1]));
                // Log.d("Moon", moonTime);
                // Log.d("Offset", risingSettingAlgorithm.getLocalTimeAsString(new BigDecimal(moonTime[1])));

                if ((sunRising != null) || (sunSetting != null)) {

                    progressBar.setVisibility(View.GONE);
                    sunLayout.setVisibility(View.VISIBLE);
                    moonLayout.setVisibility(View.VISIBLE);

                    sunUp.setText(convertClock(sunRising));
                    sunDown.setText(convertClock(sunSetting));
                    moonUp.setText(convertClock(risingSettingAlgorithm.getLocalTimeAsString(new BigDecimal(moonTime[0]))));
                    moonDown.setText(convertClock(risingSettingAlgorithm.getLocalTimeAsString(new BigDecimal(moonTime[1]))));

                    if (!notificationSet){
                        notificationSet = true;
                        //time for golden hour
                        //golderHourNotification(Integer.parseInt(sunSetting.substring(0,2))-1, Integer.parseInt(sunSetting.substring(3)));
                        //time for golden hour
                        golderHourNotification(Integer.parseInt("00:04".substring(0,2)), Integer.parseInt("00:04".substring(3)));

                    }

                }
            }
        }, 1000);



    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        map = googleMap;
        checkGPS();
        init();

    }

    //convert 24hours to 12 hours
    private static String convertClock(String time){

        String timeInClock = "";
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
            final Date dateObj = sdf.parse(time);
            timeInClock = (new SimpleDateFormat("hh:mm aa").format(dateObj));
        } catch (final ParseException e) {
            e.printStackTrace();
            timeInClock = time;
        }

        return timeInClock;
    }

    //database queries
    private static class InsertPlace extends AsyncTask<Void, Void, Boolean> {

        private Context context;
        private PlaceEntity placeEntity;
        private PlaceDatabase placeDatabase;
        private String status = "";

        public InsertPlace(Context context, PlaceEntity placeEntity, PlaceDatabase placeDatabase) {
            this.context = context;
            this.placeEntity = placeEntity;
            this.placeDatabase = placeDatabase;

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            placeDatabase.getPlaceDao().insert(placeEntity);

            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Toast.makeText(context, "Place saved", Toast.LENGTH_SHORT).show();
        }


    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51){
            if (resultCode ==RESULT_OK){
                Log.d("Here", "Location turned on");
                getDeviceLocation();

            }

        }

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && data != null) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", "+ place.toString());
                Toast.makeText(ctx, "Place:"+ place.getName(), Toast.LENGTH_SHORT).show();

                progressBar.setVisibility(View.VISIBLE);
                sunLayout.setVisibility(View.INVISIBLE);
                moonLayout.setVisibility(View.INVISIBLE);
                calculateTimeAndUpdateUi(place.getLatLng(),dt);
                moveCamera(place.getLatLng(), DEFAULT_ZOOM, place.getName());
                placeEntity = new PlaceEntity(0,place.getName(), place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude);

                if (placeDatabase.getPlaceDao().getAll().toString().contains(placeEntity.getName())){
                    bookmark_place.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_black_24dp));
                }


                searchText.setText(place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public static void startAlarmBroadcastReceiver(Context context, long delay) {
        Intent _intent = new Intent(context, GoldenReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, _intent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        // Remove any previous pending intent.
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
    }

    private void initializeMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    //check if google services version is ok
    public boolean isPlayServicesOk(){
        Log.d(TAG, "checking google services");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS){
            //everything working fine
            Log.d(TAG, "services working fine");
            return true;
        }

        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occurred but we can fix it
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            //nothing can be done
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initializeMap();
            }
            else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            }

        }
        else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_CODE:{
                if (grantResults.length > 0 ){
                    for (int i = 0; i < grantResults.length; i++){
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED){

                            mLocationPermissionsGranted = true;
                            //initialize map
                            initializeMap();
//                            onMapReady(map);
//                            checkGPS();
                            return;
                        }
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Grant location permission")
                                    .setMessage("Please allow location permission to see the sunrise and sunset timings at your location")
                                    .setPositiveButton("Ask me again", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            getLocationPermission();
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })

                                    .show();
                            builder.create();
                        }
                        mLocationPermissionsGranted = false;

                    }
                }
            }
        }
    }

    private void hideKeyyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        notificationSet = true;
        //searchText.removeTextChangedListener(new MyTextWatcher(searchText));
    }
}
