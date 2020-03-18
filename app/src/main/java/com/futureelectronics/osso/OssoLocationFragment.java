package com.futureelectronics.osso;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futureelectronics.osso.bluetooth.BleController;
import com.futureelectronics.osso.bluetooth.OssoConnection;
import com.futureelectronics.osso.data.Osso;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 */
public class OssoLocationFragment extends Fragment implements OnMapReadyCallback {

    private final static String TAG = OssoLocationFragment.class.getSimpleName();

    private GoogleMap mMap;
    private Marker mOssoMarker;

    private OssoDataViewModel mViewModel;
    private LatLng mOssoLocation;
    private String mOssoPetName;
    private boolean needToAdjustCamera = true;

    public OssoLocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.osso_location_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment)fm.findFragmentById(R.id.map);
        if(mapFragment == null){
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, mapFragment).commit();
        }

        if(mMap == null){
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(getActivity()).get(OssoDataViewModel.class);

        mViewModel.getOsso().observe(this, osso -> {
            Log.d(TAG, "Osso changed!");
            if(osso != null) {
                LatLng location = new LatLng(osso.lastLatitude, osso.lastLongitude);
                if(!locationIsValid(location) || !ossoIsConnected(osso)){
                    needToAdjustCamera = true;
                    return;
                }
                if(!location.equals(mOssoLocation) || !osso.petName.equals(mOssoPetName)){
                    mOssoLocation = location;
                    mOssoPetName = osso.petName;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOssoMarker != null) {
                                mOssoMarker.setPosition(mOssoLocation);
                                mOssoMarker.setTitle(mOssoPetName);
                            } else if (mMap != null) {
                                mOssoMarker = mMap.addMarker(new MarkerOptions().position(mOssoLocation).title(mOssoPetName));
                            }
                            if(needToAdjustCamera && mMap != null){
                                needToAdjustCamera = false;
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mOssoLocation, (float)13.0));
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean locationIsValid(LatLng location){
        if(location == null)
            return false;
        return !(location.longitude == 0 && location.latitude == 0);
    }

    private boolean ossoIsConnected(Osso osso){
        if(osso == null){
            return false;
        }

        OssoConnection connection = BleController.getInstance().getConnection(osso.address);
        if(connection == null || !connection.isConnected()){
            return false;
        }
        return true;
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
        Log.d(TAG, "Map ready!");
        mMap = googleMap;

        if(LocationPermission.checkLocationPermissionGranted(getActivity())) {
            AppLog.d(TAG, "Have location permission!");
            mMap.setMyLocationEnabled(true);
        }
        else{
            AppLog.d(TAG, "Do NOT have location permission!");
        }
        if(locationIsValid(mOssoLocation))
        {
            mOssoMarker = mMap.addMarker(new MarkerOptions().position(mOssoLocation).title(mOssoPetName));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mOssoLocation));
        }
    }

}
