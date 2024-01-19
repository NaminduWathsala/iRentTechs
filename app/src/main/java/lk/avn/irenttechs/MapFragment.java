package lk.avn.irenttechs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.List;

import lk.avn.irenttechs.custom.CustomLoading;
import lk.avn.irenttechs.model.SaveLocation;
import lk.avn.irenttechs.service.DirectionApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = MapFragment.class.getName();

    private GoogleMap mMap;
    private Location currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private static View fragmentView;
    private Marker marker_current, marker_pin;
    private static String warning_name ,email;
    private Polyline polyline;
    private SearchView mapSearch;
    private static CustomLoading customLoading;
    private static FirebaseFirestore fireStore;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);
        fragmentView = fragment;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mapSearch = fragment.findViewById(R.id.mapSearch);
        SharedPreferences preferences = getActivity().getSharedPreferences("AuthActivity", getContext().MODE_PRIVATE);
        email = preferences.getString("EMAIL", null);
        customLoading = new CustomLoading(getActivity());
        fireStore = FirebaseFirestore.getInstance();

        if (getActivity() instanceof HomeActivity) {
            HomeActivity ha = (HomeActivity) getActivity();
            ha.findViewById(R.id.bottomBar).setVisibility(View.GONE);
            TextView t_name = ha.findViewById(R.id.toolbar_name);
            t_name.setText("MAP");
            MaterialToolbar toolbar = ha.findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(null);
            ImageButton backbtn = ha.findViewById(R.id.back_btn);
            backbtn.setVisibility(View.VISIBLE);
            backbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layouts,new ProfileFragment());
                        transaction.commit();                    }
                }
            });
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(MapFragment.this);
        }

        mapSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = mapSearch.getQuery().toString();
                List<Address> addressList = null;

                if (location != null) {
                    Geocoder geocoder = new Geocoder(getContext());

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermission()) {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    }
                }
            });
        } else {
            requestLocationPermissions();
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        MapStyleOptions styleOptions = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_design);
        mMap.setMapStyle(styleOptions);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng end) {
                Log.d(TAG, "Latitude: " + end.latitude + ", Longitude: " + end.longitude);

                warning_name = "Do you wnt to Continue";
                new MapFragment.WarningDialog(end.latitude, end.longitude,getActivity().getSupportFragmentManager()).show(getActivity().getSupportFragmentManager(), "Error");

                if (marker_pin == null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(end);
                    marker_pin = mMap.addMarker(markerOptions);
                } else {
                    marker_pin.setPosition(end);
                }



//                LatLng start = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                getDirection(start, end);
            }
        });

        if (checkPermission()) {
//            getLastLocation();
        } else {
            requestLocationPermissions();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Snackbar.make(fragmentView.findViewById(R.id.fraimId), "Location permission denied", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }

    public static void addLocationData(Double latitude, Double longitude, FragmentManager fragmentManager){
        SaveLocation saveLocation = new SaveLocation(email,latitude,longitude);
        customLoading.show();
        DocumentReference locationDocument = fireStore.collection("Location").document(email);
        locationDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    fireStore.collection("Location").document(email).set(saveLocation).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            customLoading.dismiss();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.frame_layouts,new ProfileFragment());
                            transaction.commit();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            customLoading.dismiss();
                            Log.e(TAG, "Error response");

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                customLoading.dismiss();
                    Log.e(TAG, "Error response");

            }
        });
    }

    public static class WarningDialog extends DialogFragment {
        private Double longitude;
        private Double latitude;
        private FragmentManager fragmentManager;

        public WarningDialog(Double latitude, Double longitude,FragmentManager fragmentManager) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.fragmentManager = fragmentManager;

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.warning_message, null);


            TextView messageTextView = customView.findViewById(R.id.warning_dialog_message);
            messageTextView.setText(warning_name);

            Button ContinueButton = customView.findViewById(R.id.warning_dialog_ok_button);
            ContinueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addLocationData(latitude, longitude,fragmentManager);
                    dismiss();
                }
            });

            ContinueButton.setText("Continue");

            Button cancelButton = customView.findViewById(R.id.warning_dialog_cancel_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            cancelButton.setText("No");

            builder.setView(customView);
            return builder.create();
        }
    }

}