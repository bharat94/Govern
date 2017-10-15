package govern.ny.hack.edu.govern;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.github.clans.fab.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.OnClick;
import govern.ny.hack.edu.govern.models.GovModel;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private FirebaseAuth mFirebaseauth;
    private FirebaseAuth.AuthStateListener mAuthstateListener;
    private static final int RC_SIGN_IN = 123;
    private UserModel mUserModel;

    private GoogleMap mMap;
    GeoDataClient mGeoDataClient;
    PlaceDetectionClient mPlaceDetectionClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    boolean mLocationPermissionGranted = false;
    static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    Location mLastKnownLocation;
    LatLng mDefaultLocation;
    float DEFAULT_ZOOM = 14.0f;
    List<LatLng> latLngList = new ArrayList<LatLng>();

    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseauth = FirebaseAuth.getInstance();
        mUserModel = new UserModel();


        FloatingActionButton governBtn = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);
        governBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mLastKnownLocation!=null) {
                    Toast.makeText(MainActivity.this, "trying to add new governor at : "+ mLastKnownLocation.getLatitude()+","+
                            mLastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    addAsNewGoverner(new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()));
                }
                else{
                    Toast.makeText(MainActivity.this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                }
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Boilerplate code to get the location from the phone
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Create a reference to the firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();


        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        mAuthstateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mFirebaseauth.getCurrentUser();
                if (user != null) {
                    //user is signed in
                    mUserModel.setmPhoneNumber(user.getPhoneNumber());
                    if (user.getPhotoUrl() != null) {
                        mUserModel.setmPhotourl(user.getPhotoUrl().toString());
                    }
                    mUserModel.setmUid(user.getUid());
                    mUserModel.setmUserName(user.getDisplayName());
                } else {
                    //user is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sign_out_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "User Signed In!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseauth.removeAuthStateListener(mAuthstateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseauth.addAuthStateListener(mAuthstateListener);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        mDefaultLocation = new LatLng(-34, 151);


        getLocationPermission();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        mMap.getUiSettings().setMyLocationButtonEnabled(true);

    }



    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                getDeviceLocation();
                //getGovernorList();
            } else {
                mMap.setMyLocationEnabled(false);
                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void getDeviceLocation() {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            LatLng currentLatLng = new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude());
                            zoomInAddMarker(mMap, currentLatLng);
                            getGovernorList();
                        } else {
                            Log.d("Map Activity", "Current location is null. Using defaults.");
                            Log.e("Map Activity", "Exception: %s", task.getException());
                            zoomInAddMarker(mMap, mDefaultLocation);
                            //mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void zoomInAddMarker(GoogleMap map, LatLng latLng){
        map.addMarker(new MarkerOptions().position(latLng)
                .title("Your Position"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }


    private void getGovernorList(){
        // TODO HTTP get request here
        // call back should call populateGovernorList with the new list here
        if(this.mLastKnownLocation!=null)
            populateGovernorList(new ArrayList<LatLng>());
    }

    private void populateGovernorList(List<LatLng> fetchedList){
        // Using a mock list of governor lat longs
        // start Mock
        fetchedList = new ArrayList<LatLng>();
        fetchedList.add(new LatLng(1, 1));
        fetchedList.add(new LatLng(2, 2));
        fetchedList.add(new LatLng(3, 3));
        fetchedList.add(new LatLng(4, 4));
        fetchedList.add(new LatLng(5, 5));
        fetchedList.add(new LatLng(6, 6));
        fetchedList.add(new LatLng(7, 7));
        // end Mock

        this.latLngList = fetchedList;

        updateMapUI();
    }


    private void updateMapUI(){
        for(LatLng latLng : this.latLngList){
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(this.mLastKnownLocation.getLatitude()+ latLng.latitude,
                            this.mLastKnownLocation.getLongitude()+ latLng.longitude))
                    .radius(10000)
                    .strokeColor(Color.argb(180, 0, 0, 255))
                    .fillColor(Color.argb(70, 0, 0, 255)));
        }
    }


    private void addAsNewGoverner(LatLng latLng){
        addNewGoverner(mUserModel.getmUid(), latLng);
    }

    private void addNewGoverner(String userID, LatLng latLng){
        GovModel gm = new GovModel();
        gm.setIssues(new ArrayList<String>());
        gm.setLatitude(latLng.latitude);
        gm.setLongitude(latLng.longitude);
        gm.setOwnerID(userID);
        mDatabase.child("GovernanceLocations").child(latToBat(latLng)).push().setValue(gm);
    }

    private String latToBat(LatLng latLng){
        if(latLng == null)
            return "";
        String s1[] = (latLng.latitude+".").split(".");
        String s2[] = (latLng.longitude+".").split(".");
        String res = "BAT";
        for(String s : s1)
            res+=s+"BAT";
        for(String s : s2)
            res+=s+"BAT";
        return res;
    }

}
