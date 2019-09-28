package com.example.rideal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.Session;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.UberRidesApi;
import com.uber.sdk.rides.client.error.ApiError;
import com.uber.sdk.rides.client.error.ErrorParser;
import com.uber.sdk.rides.client.model.Product;
import com.uber.sdk.rides.client.model.ProductsResponse;
import com.uber.sdk.rides.client.model.RideEstimate;
import com.uber.sdk.rides.client.model.RideRequestParameters;
import com.uber.sdk.rides.client.model.UserProfile;
import com.uber.sdk.rides.client.services.RidesService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    LoginCallback loginCallback = new LoginCallback() {
        @Override
        public void onLoginCancel() {
            // User canceled login
        }
        @Override
        public void onLoginError(@NonNull AuthenticationError error) {
            // Error occurred during login
        }
        @Override
        public void onLoginSuccess(@NonNull AccessToken accessToken) {
            // Successful login!  The AccessToken will have already been saved.
        }
        @Override
        public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {

        }
    };
    AccessTokenManager accessTokenManager;
    LoginManager loginManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        loginManager.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("_uWysVxxI1pPuuOsaQSJZbgdk4VAzAzK")
                //.setServerToken("lVnrfjZENfFgaE9U2_I6YO85ss68_lvx973Oq2c5")
                //.setRedirectUri("http://localhost")
                //.setEnvironment(SessionConfiguration.Environment.SANDBOX)
                //.setScopes(Arrays.asList(Scope.PROFILE, Scope.REQUEST))
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                .setEnvironment(SessionConfiguration.Environment.PRODUCTION)
                .build();

        UberSdk.initialize(config);

        TextView hio = (TextView) findViewById(R.id.hello);
        hio.setText("UBER LINKED YAAAAAAAA");

        requestPermission();

        accessTokenManager = new AccessTokenManager(this);
        loginManager = new LoginManager(accessTokenManager, loginCallback);
        loginManager.setRedirectForAuthorizationCode(true);
        loginManager.login(this);

        try {
            Session session = loginManager.getSession();
            RidesService service = UberRidesApi.with(session).build().createService();
            Response<ProductsResponse> response = service.getProducts(37.79f, -122.39f).execute();
            List<Product> products = (List<Product>) response.body();
            String productId = products.get(0).getProductId();
            RideRequestParameters rideRequestParameters = new RideRequestParameters.Builder().setPickupCoordinates(37.77f, -122.41f)
                    .setProductId(productId)
                    .setDropoffCoordinates(37.49f, -122.41f)
                    .build();
            RideEstimate rideEstimate = service.estimateRide(rideRequestParameters).execute().body();
            int pickupEstimate = rideEstimate.getPickupEstimate();
            RideEstimate.Trip trip = rideEstimate.getTrip();
            float distEstimate = trip.getDistanceEstimate();
            float durationEstimate = trip.getDurationEstimate();
            RideEstimate.Price price = rideEstimate.getPrice();
            String fareId = price.getFareId();

            hio.setText("Fare: " + fareId);
        } catch (IOException e){

        }
    }

    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    requestPermission();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
