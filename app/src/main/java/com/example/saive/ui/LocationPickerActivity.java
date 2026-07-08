package com.example.saive.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saive.R;
import com.example.saive.adapters.LocationSuggestionAdapter;
import com.example.saive.base.BaseActivity;
import com.example.saive.models.LocationSuggestion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends BaseActivity {

    private static final String TAG = "LocationPickerActivity";
    private EditText etSearch;
    private ImageView btnClear;
    private RecyclerView rvSearchResults;
    private LocationSuggestionAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;
    private MapView mMap;
    
    private boolean isMapMoving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration before setting content view
        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_location_picker);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Map
        mMap = findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setMultiTouchControls(true);
        mMap.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        
        // Mặc định di chuyển đến Việt Nam
        GeoPoint vietnam = new GeoPoint(10.762622, 106.660172);
        mMap.getController().setZoom(15.0);
        mMap.getController().setCenter(vietnam);

        mMap.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                isMapMoving = true;
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        });

        // Use a touch listener or simple thread to check when map stops moving
        new Thread(() -> {
            while (!isDestroyed()) {
                try {
                    Thread.sleep(1000);
                    if (isMapMoving) {
                        isMapMoving = false;
                        // Map might have stopped, let's reverse geocode
                        runOnUiThread(() -> {
                            GeoPoint center = (GeoPoint) mMap.getMapCenter();
                            getAddressFromLatLng(center);
                        });
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();

        etSearch = findViewById(R.id.etSearch);
        btnClear = findViewById(R.id.btnClear);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocationSuggestionAdapter();
        rvSearchResults.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            rvSearchResults.setVisibility(View.GONE);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (s.length() > 2) {
                    searchPlaces(s.toString());
                } else {
                    rvSearchResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter.setOnItemClickListener(suggestion -> {
            rvSearchResults.setVisibility(View.GONE);
            // suggestion placeId holds "lat,lon" from our custom logic below
            String[] parts = suggestion.getPlaceId().split(",");
            if (parts.length == 2) {
                try {
                    double lat = Double.parseDouble(parts[0]);
                    double lon = Double.parseDouble(parts[1]);
                    GeoPoint point = new GeoPoint(lat, lon);
                    mMap.getController().animateTo(point);
                    mMap.getController().setZoom(17.0);
                    etSearch.setText(suggestion.getMainText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btnCurrentLocation).setOnClickListener(v -> getCurrentLocation());

        findViewById(R.id.btnConfirmLocation).setOnClickListener(v -> {
            String addressToUse = etSearch.getText().toString().trim();
            if (!addressToUse.isEmpty()) {
                returnLocation(addressToUse);
            } else {
                Toast.makeText(this, R.string.error_select_location, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPlaces(String query) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(LocationPickerActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 5);
                List<LocationSuggestion> suggestions = new ArrayList<>();
                if (addresses != null) {
                    for (int i = 0; i < addresses.size(); i++) {
                        Address addr = addresses.get(i);
                        // Save lat,lon in placeId field
                        String fakePlaceId = addr.getLatitude() + "," + addr.getLongitude();
                        
                        String primary = addr.getFeatureName() != null ? addr.getFeatureName() : addr.getAddressLine(0);
                        String secondary = addr.getAddressLine(0) != null ? addr.getAddressLine(0) : "";
                        
                        if (primary.equals(secondary)) {
                            // avoid duplicate text
                            if (addr.getLocality() != null) {
                                secondary = addr.getLocality();
                            }
                        }
                        
                        suggestions.add(new LocationSuggestion(fakePlaceId, primary, secondary));
                    }
                }
                
                runOnUiThread(() -> {
                    adapter.setSuggestions(suggestions);
                    rvSearchResults.setVisibility(suggestions.isEmpty() ? View.GONE : View.VISIBLE);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getAddressFromLatLng(GeoPoint geoPoint) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(LocationPickerActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addressLine = addresses.get(0).getAddressLine(0);
                    runOnUiThread(() -> etSearch.setText(addressLine));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && mMap != null) {
                GeoPoint current = new GeoPoint(location.getLatitude(), location.getLongitude());
                mMap.getController().animateTo(current);
                mMap.getController().setZoom(17.0);
            }
        });
    }

    private void returnLocation(String address) {
        Intent data = new Intent();
        data.putExtra("selected_address", address);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) mMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMap != null) mMap.onPause();
    }
}
