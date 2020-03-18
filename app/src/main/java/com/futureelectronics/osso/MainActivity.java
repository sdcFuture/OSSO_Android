package com.futureelectronics.osso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import io.reactivex.plugins.RxJavaPlugins;

import android.os.Bundle;

import com.futureelectronics.osso.bluetooth.BleController;

public class MainActivity extends AppCompatActivity {

    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxJavaPlugins.setErrorHandler(e -> {AppLog.d("MainAcitivity", "RxJava exception: "+e);});
        BleController.init(this);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
        }

        navController = Navigation.findNavController(this, R.id.my_nav_host_fragment);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        setSupportActionBar(findViewById(R.id.toolbar));
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        if (!LocationPermission.checkLocationPermissionGranted(this)) {
            LocationPermission.requestLocationPermission(this);
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    public void setActionBarTitle(String title)
    {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        if (!LocationPermission.isRequestLocationPermissionGranted(requestCode, permissions, grantResults)) {
            finish();
        }
    }
}
