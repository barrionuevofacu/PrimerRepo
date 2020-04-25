package com.example.trabajofinalobjetos15_7_2019;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.util.Log;
import android.view.MenuItem;

import com.example.trabajofinalobjetos15_7_2019.DTOs.LocationDTO;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener {

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;

    private final List<Marker> pointsToAddList = new ArrayList<>(); // almacena los markers para luego generar el polygono/linea
    private HashMap<Object, LocationDTO> locationsHash = new HashMap<>(); /// la clave de este hash es el objeto(linea marker o polygono y su valor es el dto que le corresponde)
    private List<Polyline> polylineList = new ArrayList<>();//almacenar todos para setearlos clickeables o no clickeables
    private List<Polygon> polygonList = new ArrayList<>(); //almacenar todos para setearlos clickeables o no clickeables
    private Polygon polygon;
    private Polyline polyline;
    private Marker marker;
    private LocationDTO locationToAdd;
    private String token;
    private Api_Interface api;
    private List<LatLng> refreshList;
    private int activeMethod = 3;
    private int mapType = MAP_TYPE_NORMAL;
    ArrayList<String> stringListArea = new ArrayList<>();
    private boolean savedInstance = false; // para saber si dirijo la camara a donde esta el usuario o no.
    private GoogleMap mMap;
    private Button confirmButton;
    private Button editButton;
    private Button verButton;
    private ImageView gpsWidget;
    private Boolean vieneDePuntosEnArea = false;
    private Boolean trajoUbicaciones = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba_menu);
        gpsWidget = findViewById(R.id.id_gps);
        editButton = findViewById(R.id.editButton);
        confirmButton = findViewById(R.id.ConfirmButton);
        verButton = findViewById(R.id.VerButton);
        gpsWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
        token = getIntent().getStringExtra("Token");
        if (!token.contains("bearer"))
            token = "bearer " + token;

        refreshList = new ArrayList<>();
        stringListArea = getIntent().getStringArrayListExtra("imgs");
        vieneDePuntosEnArea = getIntent().getBooleanExtra("vieneDePointsInArea",false);
        if (vieneDePuntosEnArea){
            if (stringListArea != null && stringListArea.size()>0){
                trajoUbicaciones = true;
                for (String str : stringListArea) {
                    String [] listPoint = str.split(":");
                    String lat = listPoint[listPoint.length-1].split(",")[0];
                    String lng = listPoint[listPoint.length-1].split(",")[1];
                    LatLng nltlng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    refreshList.add(nltlng);
                }
            }
            else{
                trajoUbicaciones = false;
                Toast.makeText(getApplicationContext(), "No se encontraron perros en el area seleccionada", Toast.LENGTH_SHORT).show();
            }
        }
        confirmButton.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.INVISIBLE);
        verButton.setVisibility(View.INVISIBLE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!vieneDePuntosEnArea){
            if (savedInstanceState != null) {
                savedInstance = true;
                mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
                confirmButton.setVisibility(savedInstanceState.getInt("confirmButton"));
                mapType = savedInstanceState.getInt("mapType");
                refreshList = savedInstanceState.getParcelableArrayList("points");
                activeMethod = savedInstanceState.getInt("activeMethod");
            } else {
                String welcome = "Bienvenido/a " + getIntent().getStringExtra("username") + " !";
               Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_SHORT).show();
            }
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            outState.putInt("confirmButton", confirmButton.getVisibility());
            outState.putInt("mapType", mMap.getMapType());
            outState.putInt("activeMethod", activeMethod);

            ArrayList<LatLng> list = new ArrayList<LatLng>();
            for (Marker m : pointsToAddList)
                list.add(m.getPosition());
            outState.putParcelableArrayList("points", list);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(mapType);
        for (LatLng latLng : refreshList) {
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker Title"));
            pointsToAddList.add(marker);
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker clickedMarker) {
                if (pointsToAddList.contains(clickedMarker)) {
                    clickedMarker.remove();
                    pointsToAddList.remove(clickedMarker);
                    return true;
                } else {
                    if (!(confirmButton.getVisibility() == View.VISIBLE)) {
                        if (vieneDePuntosEnArea && trajoUbicaciones){
                            verButton.setVisibility(View.VISIBLE);
                            verButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    marker = clickedMarker;
                                    Intent intent = new Intent(MapActivity.this, LocationActivity.class);
                                    intent.putExtra("type", 0);
                                    intent.putExtra("color", getResources().getColor(R.color.Map_Green));
                                    intent.putExtra("tag", clickedMarker.getTag().toString());
                                    intent.putExtra("isNew", false);
                                    intent.putExtra("imageId", locationsHash.get(marker).getImageId());
                                    intent.putExtra("token", token);
                                    intent.putExtra("soloVista", true);
                                    startActivityForResult(intent, 2);
                                }
                            });
                        }
                        else{
                            editButton.setVisibility(View.VISIBLE);
                            editButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    marker = clickedMarker;
                                    Intent intent = new Intent(MapActivity.this, LocationActivity.class);
                                    intent.putExtra("type", 0);
                                    intent.putExtra("color", getResources().getColor(R.color.Map_Red));
                                    intent.putExtra("tag", clickedMarker.getTag().toString());
                                    intent.putExtra("isNew", false);
                                    intent.putExtra("imageId", locationsHash.get(marker).getImageId());
                                    intent.putExtra("token", token);
                                    intent.putExtra("soloVista", false);
                                    startActivityForResult(intent, 2);
                                }
                            });
                        }
                    }
                        Toast.makeText(getApplicationContext(), clickedMarker.getTag().toString(), Toast.LENGTH_SHORT).show();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(clickedMarker.getPosition(), mMap.getCameraPosition().zoom));
                    return true;
                }
            }
        });
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnPolygonClickListener(this);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                editButton.setVisibility(View.INVISIBLE);
                verButton.setVisibility(View.INVISIBLE);
            }
        });

        getLocationPermission();
        updateLocationUI();

        if (!savedInstance) {
            getDeviceLocation();
        }
        drawFigures();
    }

    @Override
    public void onPolygonClick(final Polygon polygon) {
        editButton.setVisibility(View.VISIBLE);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationActivity(2, polygon.getFillColor(), polygon.getTag().toString(), locationsHash.get(polygon).getImageId(), token, false, 2);
            }
        });
        this.polygon = polygon;
        Toast.makeText(getApplicationContext(), polygon.getTag().toString(), Toast.LENGTH_SHORT).show();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getCentroid(polygon.getPoints()), mMap.getCameraPosition().zoom));
    }

    @Override
    public void onPolylineClick(final Polyline line) {
        editButton.setVisibility(View.VISIBLE);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationActivity(1, line.getColor(), line.getTag().toString(), locationsHash.get(line).getImageId(), token, false, 2);
            }
        });
        polyline = line;
        Toast toast1 = Toast.makeText(getApplicationContext(), polyline.getTag().toString(), Toast.LENGTH_SHORT);
        toast1.show();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getCentroid(polyline.getPoints()), mMap.getCameraPosition().zoom));
    }

    ////////////////////////to get user location////////////////////////

    private void getLocationPermission() {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
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
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = (Location) task.getResult();
                            if (mLastKnownLocation != null)
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    //////////////// Menu Functions///////////////
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (activeMethod == 4){
                polygon.remove();
                confirmButton.setText("Confirm");
            }
            if (confirmButton.getVisibility() == View.VISIBLE) {
                setPolygonsClickable(true);
                setPolylinesClickable(true);
                activeMethod = 3;
                confirmButton.setVisibility(View.INVISIBLE);
                clearPointsList(pointsToAddList);
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        editButton.setVisibility(View.INVISIBLE);
                        verButton.setVisibility(View.INVISIBLE);
                    }
                });
            }
            else super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        editButton.setVisibility(View.INVISIBLE);
        verButton.setVisibility(View.INVISIBLE);
        clearPointsList(pointsToAddList);
        int id = item.getItemId();

        if (id == R.id.Add_Location) {
            addLocation();
        } else if (id == R.id.getlocations){
            getLocationsInArea();
        } else if (id == R.id.search){
            search();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getLocationsInArea(){
        activeMethod = 4;
        Toast.makeText(getApplicationContext(), "Seleccione los vértices del área", Toast.LENGTH_SHORT).show();
        setPolygonsClickable(false);
        setPolylinesClickable(false);
        confirmButton.setVisibility(View.VISIBLE);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                pointsToAddList.add(marker);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pointsToAddList.size() > 2) {
                    PolygonOptions polyOptions = new PolygonOptions().clickable(true);
                    for (Marker marker : pointsToAddList)
                        polyOptions.add(marker.getPosition());
                    polygon = mMap.addPolygon(polyOptions);
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) { }
                    });
                    polygon.setFillColor(getResources().getColor(R.color.Map_Green));
                    polygon.setStrokeColor(getResources().getColor(R.color.Map_Green));
                    polygon.setClickable(false);
                    clearPointsList(pointsToAddList);
                    confirmButton.setText("Ver perros");
                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmButton.setText("Confirmar");
                            confirmButton.setVisibility(View.INVISIBLE);
                            String coordinates = "";
                            for(LatLng coordinate : polygon.getPoints())
                                coordinates+= extractLocationString(coordinate);
                            polygon.remove();
                            pointsToAddList.clear();
                            setPolygonsClickable(true);
                            setPolylinesClickable(true);
                            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    editButton.setVisibility(View.INVISIBLE);
                                    verButton.setVisibility(View.INVISIBLE);
                                }
                            });
                            Intent i = new Intent(MapActivity.this, PointsInAreaActivity.class);
                            i.putExtra("token", token);
                            i.putExtra("coordinates", coordinates);
                            startActivity(i);
                        }
                    });
                } else
                    Toast.makeText(getApplicationContext(), "Seleccione otro vértice del área", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addLocation(){
        activeMethod = 0;
        Toast.makeText(getApplicationContext(), "Marque ubicación del perro encontrado/visto", Toast.LENGTH_SHORT).show();
        setPolygonsClickable(false);
        setPolylinesClickable(false);
        confirmButton.setVisibility(View.VISIBLE);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (pointsToAddList.size() > 0) {
                    pointsToAddList.get(0).remove();
                    pointsToAddList.remove(0);
                }
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker Title"));
                pointsToAddList.add(marker);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pointsToAddList.isEmpty())
                    Toast.makeText(getApplicationContext(), "Marque ubicación del perro encontrado/visto", Toast.LENGTH_SHORT).show();
                else
                    startLocationActivity(0, getResources().getColor(R.color.Map_Red), "Descripción", 0, token, true, 1);
            }
        });
    }
    public void search(){
        activeMethod = 0;
        Toast.makeText(getApplicationContext(), "Please select which location to search a dog", Toast.LENGTH_SHORT).show();
        setPolygonsClickable(false);
        setPolylinesClickable(false);
        confirmButton.setVisibility(View.VISIBLE);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (pointsToAddList.size() > 0) {
                    pointsToAddList.get(0).remove();
                    pointsToAddList.remove(0);
                }
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Marker Title"));
                pointsToAddList.add(marker);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pointsToAddList.isEmpty())
                    Toast.makeText(getApplicationContext(), "Please select location to add", Toast.LENGTH_SHORT).show();
                else
                    startSearchActivity(0, getResources().getColor(R.color.Map_Red), "New Tag", 0, token, true, 11);
            }
        });
    }

    public void addLine() {
        activeMethod = 1;
        Toast.makeText(getApplicationContext(), "Please select points of the line", Toast.LENGTH_SHORT).show();
        setPolygonsClickable(false);
        setPolylinesClickable(false);
        confirmButton.setVisibility(View.VISIBLE);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                pointsToAddList.add(marker);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pointsToAddList.size() > 1)
                    startLocationActivity(1, getResources().getColor(R.color.Map_Orange), "New Tag", 0, token, true, 1);
                else
                    Toast.makeText(getApplicationContext(), "Please select another point for the line", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addPolygon() {
        activeMethod = 2;
        Toast.makeText(getApplicationContext(), "Seleccione los vértices del área", Toast.LENGTH_SHORT).show();
        setPolygonsClickable(false);
        setPolylinesClickable(false);
        confirmButton.setVisibility(View.VISIBLE);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                pointsToAddList.add(marker);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pointsToAddList.size() > 2)
                    startLocationActivity(2, getResources().getColor(R.color.Map_Green), "New Tag", 0, token, true, 1);
                else {
                    Toast.makeText(getApplicationContext(), "Seleccione otro vértice del área", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void changeMapType() {
        switch (mMap.getMapType()) {
            case MAP_TYPE_NORMAL: {
                mMap.setMapType(MAP_TYPE_TERRAIN);
                break;
            }
            case MAP_TYPE_TERRAIN: {
                mMap.setMapType(MAP_TYPE_HYBRID);
                break;
            }
            case MAP_TYPE_HYBRID: {
                mMap.setMapType(MAP_TYPE_SATELLITE);
                break;
            }
            case MAP_TYPE_SATELLITE: {
                mMap.setMapType(MAP_TYPE_NORMAL);
                break;
            }
        }
    }

    public void clearPointsList(List<Marker> pointsList) {
        for (Marker m : pointsList)
            if (m.getTag() == null)
                m.remove();
        pointsList.clear();
    }

    public void setPolygonsClickable(boolean bool) {
        for (Polygon p : polygonList)
            p.setClickable(bool);
    }

    public void setPolylinesClickable(boolean bool) {
        for (Polyline p : polylineList)
            p.setClickable(bool);
    }

    public static LatLng getCentroid(List<LatLng> points) {
        double[] centroid = {0.0, 0.0};
        for (int i = 0; i < points.size(); i++) {
            centroid[0] += points.get(i).latitude;
            centroid[1] += points.get(i).longitude;
        }
        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;
        return new LatLng(centroid[0], centroid[1]);
    }

    public void drawFigures() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getString(R.string.base_Url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(Api_Interface.class);
        Call<List<LocationDTO>> call = null;
        if (!vieneDePuntosEnArea || (vieneDePuntosEnArea && !trajoUbicaciones)) {
            call = api.getLocations(token);
        }
        else{
            if(stringListArea != null)
                call = api.getLocationsById(stringListArea, token);
        }
        if (call != null){
            call.enqueue(new Callback<List<LocationDTO>>() {
                @Override
                public void onResponse(Call<List<LocationDTO>> call, Response<List<LocationDTO>> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Error obtaining user locations", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<LocationDTO> locations = response.body();
                    for (LocationDTO location : locations) {
                        generateLocation(location);
                    }
                    switch (activeMethod) {
                        case 0: {
                            addLocation();
                            break;
                        }
                        case 1: {
                            addLine();
                            break;
                        }
                        case 2: {
                            addPolygon();
                            break;
                        }
                        case 4:{
                            getLocationsInArea();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<LocationDTO>> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Service failure", Toast.LENGTH_SHORT).show();
                    return;
                }
            });
        }
    }

    public void generateLocation(LocationDTO location) {
        switch (location.getType()) {
            case 0: {
                Marker markerToAdd;
                markerToAdd = mMap.addMarker(new MarkerOptions().position(getPoints(location.getCoordinates()).get(0)).title("Marker Title"));
                markerToAdd.setTag(location.getTag());
                if (location.getIsSearch())
                    markerToAdd.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                locationsHash.put(markerToAdd, location);
                break;
            }
            case 1: {
                PolylineOptions polyOptions = new PolylineOptions().clickable(true);
                for (LatLng latLng : getPoints(location.getCoordinates())) {
                    polyOptions.add(latLng);
                }
                polyline = mMap.addPolyline(polyOptions);
                polyline.setColor(location.getColor());
                polyline.setTag(location.getTag());
                locationsHash.put(polyline, location);
                polylineList.add(polyline);
                break;
            }
            case 2: {
                PolygonOptions polyOptions = new PolygonOptions().clickable(true);
                for (LatLng latLng : getPoints(location.getCoordinates()))
                    polyOptions.add(latLng);
                polygon = mMap.addPolygon(polyOptions);
                polygon.setTag(location.getTag());
                polygon.setFillColor(location.getColor());
                polygon.setStrokeColor(location.getColor());
                locationsHash.put(polygon, location);
                polygonList.add(polygon);
                break;
            }
        }
    }

    public List<LatLng> getPoints(String locationsString) {
        List<LatLng> latLngList = new ArrayList<>();
        for (String location : locationsString.split(";")) {
            String lat = location.split(",")[0];
            String lng = location.split(",")[1];
            LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            latLngList.add(latLng);
        }
        return latLngList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            locationToAdd = new LocationDTO();
            locationToAdd.setTag(data.getStringExtra("tag"));
            locationToAdd.setColor(data.getIntExtra("color", getResources().getColor(R.color.Map_Green)));
            locationToAdd.setType(data.getShortExtra("type", (short) 0));
            locationToAdd.setImageId(data.getIntExtra("imageId", 0));

            if (requestCode == 1) {
                for (Marker marker : pointsToAddList) {
                    locationToAdd.setCoordinates(locationToAdd.getCoordinates() + extractLocationString(marker.getPosition()));
                }

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(getResources().getString(R.string.base_Url))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                api = retrofit.create(Api_Interface.class);
                Call<LocationDTO> call = api.addLocation(locationToAdd, token);
                call.enqueue(new Callback<LocationDTO>() {
                    @Override
                    public void onResponse(Call<LocationDTO> call, Response<LocationDTO> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Unable to save location", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        confirmButton.setVisibility(View.INVISIBLE);
                        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                editButton.setVisibility(View.INVISIBLE);
                                verButton.setVisibility(View.INVISIBLE);
                            }
                        });

                        switch (response.body().getType()) {
                            case 0: {
                                marker.setTag(response.body().getTag());
                                locationsHash.put(marker, response.body());
                                break;
                            }
                            case 1: {
                                PolylineOptions polyOptions = new PolylineOptions().clickable(true);
                                for (Marker marker : pointsToAddList) {
                                    polyOptions.add(marker.getPosition());
                                }
                                polyline = mMap.addPolyline(polyOptions);
                                polyline.setColor(response.body().getColor());
                                polyline.setTag(response.body().getTag());
                                locationsHash.put(polyline, response.body());
                                polylineList.add(polyline);
                                break;
                            }
                            case 2: {
                                PolygonOptions polyOptions = new PolygonOptions().clickable(true);
                                for (Marker marker : pointsToAddList) {
                                    polyOptions.add(marker.getPosition());
                                }
                                polygon = mMap.addPolygon(polyOptions);
                                polygon.setTag(response.body().getTag());
                                polygon.setFillColor(response.body().getColor());
                                polygon.setStrokeColor(response.body().getColor());
                                locationsHash.put(polygon, response.body());
                                polygonList.add(polygon);
                                break;
                            }
                        }
                        activeMethod = 3;
                        setPolygonsClickable(true);
                        setPolylinesClickable(true);
                        clearPointsList(pointsToAddList);
                        Toast.makeText(getApplicationContext(), "Location Saved Successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<LocationDTO> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Service failure", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            } else if (requestCode == 11) {
                for (Marker marker : pointsToAddList) {
                    locationToAdd.setCoordinates(locationToAdd.getCoordinates() + extractLocationString(marker.getPosition()));
                }
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(getResources().getString(R.string.base_Url))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                api = retrofit.create(Api_Interface.class);
                Call<List<LocationDTO>> call = api.searchLocation(locationToAdd, token);
                call.enqueue(new Callback<List<LocationDTO>>() {
                    @Override
                    public void onResponse(Call<List<LocationDTO>> call, Response<List<LocationDTO>> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Unable to save location", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        confirmButton.setVisibility(View.INVISIBLE);
                        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                editButton.setVisibility(View.INVISIBLE);
                            }
                        });
                        mMap.clear();
                        if (response.body().size() > 0)
                            for (LocationDTO location : response.body()){
                            generateLocation(location);
                            }
                        else
                            Toast.makeText(getApplicationContext(), "No se encontraron perros en el area seleccionada", Toast.LENGTH_SHORT).show();
                        activeMethod = 3;
                        setPolygonsClickable(true);
                        setPolylinesClickable(true);
                        clearPointsList(pointsToAddList);
                    }

                    @Override
                    public void onFailure(Call<List<LocationDTO>> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Service failure", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
            else {
                if (!data.getBooleanExtra("delete", true)) {
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            editButton.setVisibility(View.INVISIBLE);
                            verButton.setVisibility(View.INVISIBLE);
                        }
                    });
                    switch (data.getShortExtra("type", (short) 0)) {
                        case 0: {
                            locationToAdd.setCoordinates(extractLocationString(marker.getPosition()));
                            if (locationToAdd.getImageId() == 0)
                                locationToAdd.setImageId(locationsHash.get(marker).getImageId());
                            locationToAdd.setId(locationsHash.get(marker).getId());
                            marker.setTag(data.getStringExtra("tag"));
                            locationsHash.get(marker).setImageId(locationToAdd.getImageId());
                            break;
                        }
                        case 1: {
                            for (LatLng coordinate : polyline.getPoints()) {
                                locationToAdd.setCoordinates(locationToAdd.getCoordinates()+ extractLocationString(coordinate));
                            }
                            locationToAdd.setId(locationsHash.get(polyline).getId());
                            editButton.setVisibility(View.INVISIBLE);
                            verButton.setVisibility(View.INVISIBLE);
                            polyline.setColor(data.getIntExtra("color", getResources().getColor(R.color.Map_Green)));
                            polyline.setTag(data.getStringExtra("tag"));
                            if (locationToAdd.getImageId() == 0)
                                locationToAdd.setImageId(locationsHash.get(polyline).getImageId());
                            locationsHash.get(polyline).setImageId(locationToAdd.getImageId());
                            break;
                        }
                        case 2: {
                            for (LatLng coordinate : polygon.getPoints()) {
                                locationToAdd.setCoordinates(locationToAdd.getCoordinates()+ extractLocationString(coordinate));
                            }
                            locationToAdd.setId(locationsHash.get(polygon).getId());
                            polygon.setTag(data.getStringExtra("tag"));
                            polygon.setFillColor(data.getIntExtra("color", getResources().getColor(R.color.Map_Green)));
                            polygon.setStrokeColor(data.getIntExtra("color", getResources().getColor(R.color.Map_Green)));
                            if (locationToAdd.getImageId() == 0)
                                locationToAdd.setImageId(locationsHash.get(polygon).getImageId());
                            locationsHash.get(polygon).setImageId(locationToAdd.getImageId());
                        }
                    }
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(getResources().getString(R.string.base_Url))
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    api = retrofit.create(Api_Interface.class);
                    Call<String> call = api.updateLocation(locationToAdd.getId(), token, locationToAdd);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (!response.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Unable to update location", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Service failure", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });
                } else {
                    switch (data.getShortExtra("type", (short) 0)) {
                        case 0: {
                            locationToAdd.setId(locationsHash.get(marker).getId());
                            locationsHash.remove(marker);
                            marker.remove();
                            break;
                        }
                        case 1: {
                            locationToAdd.setId(locationsHash.get(polyline).getId());
                            locationsHash.remove(polyline);
                            polyline.remove();
                            break;
                        }
                        case 2: {
                            locationToAdd.setId(locationsHash.get(polygon).getId());
                            locationsHash.remove(polygon);
                            polygon.remove();
                            break;
                        }
                    }
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(getResources().getString(R.string.base_Url))
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    api = retrofit.create(Api_Interface.class);
                    Call<String> call = api.deleteLocation(locationToAdd.getId(), token);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (!response.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Unable to delete location", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(getApplicationContext(), response.body(), Toast.LENGTH_SHORT).show();
                            editButton.setVisibility(View.INVISIBLE);
                            verButton.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), "Service failure", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });
                }
            }
        }
    }

    public String extractLocationString(LatLng coordinate) {
        String cleanString = coordinate.toString();
        String a = new String();
        String b = new String();
        a = cleanString.split("\\(")[1];
        b = a.split("\\)")[0];
        return b.concat(";");
    }

    private void startLocationActivity(int type, int color, String tag,int imageId, String token, boolean isNew, int requestCode){
        Intent intent = new Intent(MapActivity.this, LocationActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("color", color);
        intent.putExtra("tag", tag);
        intent.putExtra("imageId", imageId);
        intent.putExtra("token", token);
        intent.putExtra("isNew", isNew);
        startActivityForResult(intent, requestCode);
    }

    private void startSearchActivity(int type, int color, String tag,int imageId, String token, boolean isNew, int requestCode){
        Intent intent = new Intent(MapActivity.this, SearchActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("color", color);
        intent.putExtra("tag", tag);
        intent.putExtra("imageId", imageId);
        intent.putExtra("token", token);
        intent.putExtra("isNew", isNew);
        startActivityForResult(intent, requestCode);
    }

}
