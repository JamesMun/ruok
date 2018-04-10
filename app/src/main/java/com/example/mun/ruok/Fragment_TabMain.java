package com.example.mun.ruok;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.location.LocationManager.GPS_PROVIDER;

import com.example.mun.ruok.GpsInfo;

/**
 * Created by KJH on 2017-05-15.
 * Fragment Life Style
 * 1. Fragment is added
 * 2. onAttach()                    Fragment가 Activty에 붙을때 호출
 * 3. onCreate()                    Activty에서의 onCreate()와 비슷하나, ui 관련 작업은 할 수 없다.
 * 4. onCreateView()                Layout을 inflater을 하여 View 작업을 하는 곳
 * 5. onActivityCreated()           Activity에서 Fragment를 모두 생성하고난 다음에 호출됨. Activty의 onCreate()에서 setContentView()한 다음과 같다
 * 6. onStart()                     Fragment가 화면에 표시될때 호출, 사용자의 Action과 상호 작용이 불가능함
 * 7. onResume()                    Fragment가 화면에 완전히 그렸으며, 사용자의 Action과 상호 작용이 가능함
 * 8. Fragment is active
 * 9. User navigates backward or fragment is removed/replaced  or Fragment is added to the back stack, then removed/replaced
 * 10. onPause()
 * 11. onStop()                     Fragment가 화면에서 더이상 보여지지 않게됬을때
 * 12. onDestroy()                  View 리소스를 해제할수있도록 호출. backstack을 사용했다면 Fragment를 다시 돌아갈때 onCreateView()가 호출됨
 * 13. onDetached()
 * 14. Fragment is destroyed
 */


/**
 * Google Map CallStack
 * 1. onCreate()
 * 2. onCreateView()
 * 3. onActivityCreated()
 * 4. onStart();
 * 5. onResume();
 * 5-2. onMapReady();
 * 6. onPause();
 * 7. onSaveInstanceState();
 * 8. onMapReady();
 */

public class Fragment_TabMain extends Fragment implements View.OnClickListener, OnMapReadyCallback {

    private ViewGroup rootView;

    private GoogleApiClient mGoogleApiClient;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 15000;

    public static Double lat = 32.882499;
    public static Double lon = -117.234644;

    private int heart_start = 0;
    public static int heart_rate_value = 0, rr_rate_value = 0;

    private BluetoothAdapter mBluetoothAdapter = null; /* Intent request codes*/

    private TextView HeartRateText;

    private GoogleMap map;
    private MapView mapView = null;

    public static Fragment_TabMain TabMainContext;

    private Thread heartThread;

    public Fragment_TabMain() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.activity_tab_main, container, false);
        TabHost host = (TabHost) rootView.findViewById(R.id.host);
        host.setup();

        TabHost.TabSpec spec = host.newTabSpec("tab1");
        spec.setIndicator(null, ResourcesCompat.getDrawable(getResources(), R.drawable.hearttext, null));
        spec.setContent(R.id.tab_content1);
        host.addTab(spec);

        spec = host.newTabSpec("tab2");
        spec.setIndicator(null, ResourcesCompat.getDrawable(getResources(), R.drawable.airtext, null));
        spec.setContent(R.id.tab_content2);
        host.addTab(spec);

        //setHasOptionsMenu(true);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        TabMainContext = this;

        return rootView;

    }

    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        HeartRateText = (TextView) view.findViewById(R.id.HeartDataValue);

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.getMapAsync(this);

        startSubThread();

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.aqicloud1);
        ShowMyLocaion(lat,lon,map);
        StartLocationService();
    }

    private void StartLocationService() {
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        GPSListener gpsListener = new GPSListener();
        long minTime = 3000;
        float minDistance = 0;
        try {   //GPS 위치 요청
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.requestLocationUpdates(GPS_PROVIDER, minTime, minDistance, (android.location.LocationListener) gpsListener);

            // location request with network
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, (android.location.LocationListener) gpsListener);

            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastLocation != null) {
                Double latitude = lastLocation.getLatitude();
                Double longitude = lastLocation.getLongitude();

            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    private class GPSListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            String msg = "Lat:" + lat + " / Lon:" + lon;
            ShowMyLocaion(lat, lon, map);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
            ShowMyLocaion(lat, lon, map);
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }

    private void ShowMyLocaion(Double lat, Double lon, GoogleMap googleMap) {
        try {
            LatLng nowLocation = new LatLng(lat, lon);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(nowLocation);
            markerOptions.title("now location");

            //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(getResources().getIdentifier(cloudimage,"drawable","com.example.khseob0715.sanfirst"));

            //markerOptions.icon(icon);

            googleMap.clear();

            // Get back the mutable Circle
            //googleMap.addCircle(circleOptions);

            googleMap.addMarker(markerOptions);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(nowLocation));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        }   catch (IllegalStateException e)   {
        }
    }

    public void startSubThread() {
        //작업스레드 생성(매듭 묶는과정)
        heartHandler heartRunnable = new heartHandler();
        heartThread = new Thread(heartRunnable);
        heartThread.setDaemon(true);
        heartThread.start();
    }

    android.os.Handler receivehearthandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            heart_start = heart_rate_value;

            HeartRateText.setText(String.valueOf(heart_rate_value));
        }
    };

    public class heartHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                Message msg = Message.obtain();
                msg.what = 0;
                receivehearthandler.sendMessage(msg);
                try {
                    Thread.sleep(1000); // 갱신주기 1초
                } catch (Exception e) {
                }
            }
        }
    }
}
