package com.example.mun.ruok.Service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.mun.ruok.Activity.AlertActivity;
import com.example.mun.ruok.Activity.MainActivity;
import com.example.mun.ruok.DTO.ConnectDTO;
import com.example.mun.ruok.DTO.HeartDTO;
import com.example.mun.ruok.DTO.UserDTO;
import com.example.mun.ruok.Database.FitSQLiteHelper;
import com.example.mun.ruok.Database.HeartSQLiteHelper;
import com.example.mun.ruok.Database.UserSQLiteHelper;
import com.example.mun.ruok.Fragment.Fragment_TabMain;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.location.LocationManager.GPS_PROVIDER;

public class SensorService extends Service {
    private static final String TAG = "SensorService";

    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private static final int DEFAULT_CODE = 0;
    private static final int REQUEST_CONNECTING_CODE = 1;
    private static final int CONNECTING_PERMISSION_CODE = 2;

    private GoogleApiClient mClient = null;
    private boolean authInProgress = false;

    private int hr = 0;

    public static int max_heart_rate = 120;
    public static int min_heart_rate = 50;
    public static boolean conn_state = false;

    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    private OnDataPointListener mListener;
    // [END mListener_variable_reference]

    public static int heart_count = 0;
    public static Thread heartThread;
    public static int CONNECTING_STATE;
    public static String CONNECTING_ACCOUNT;

    public boolean fit_mode = false;
    public static boolean alert = false;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public static String userid, account;
    public static int UserType;

    private HeartDTO heartDTO = new HeartDTO();

    private Double lat;
    private Double lon;

    public static HeartSQLiteHelper HRsqlhelper = new HeartSQLiteHelper();
    private UserSQLiteHelper Usersqlhelper = new UserSQLiteHelper();
    private FitSQLiteHelper Fitsqlhelper = new FitSQLiteHelper();

    private String Today,currentdate;


    public SensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        account = MainActivity.account;
        UserType = MainActivity.UserType;

        checkConnection();  // 계정 간 연결 상태 확인

        if(UserType == 0) { // 사용자인 경우에만
            LoadDataonFirebase(); // 서버에서 데이터 로드
        }

        Log.d(TAG, "onCreate() 호출됨.");
        Log.d(TAG, "유저타입 : " + UserType);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand() 호출됨.");

        if(intent == null) {
            return Service.START_STICKY;
        } else {
            if(UserType == 0) {
                StartLocationService();
                processCommand(intent);
            } else if(UserType == 1) {
                if(CONNECTING_STATE == CONNECTING_PERMISSION_CODE) {
                    databaseReference.child("RealTime").child(CONNECTING_ACCOUNT).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            heartDTO = dataSnapshot.getValue(HeartDTO.class);
                            Fragment_TabMain.heart_rate_value = heartDTO.HR;
                            Fragment_TabMain.heart_time = heartDTO.TS;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void processCommand(Intent intent) {
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                //.addApi(Fitness.HISTORY_API)
                //.addApi(Fitness.RECORDING_API)
                //.addApi(Fitness.BLE_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                //.addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                //.addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                //com.example.mun.ruok.logger.Log.i(TAG, "Connected!!!");
                                //myTextView.append("Connected\r\n");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
                                findFitnessDataSources(); // for senior
                                //new readFitnessData().execute();
                                // buildBle();
                                // subscribe();
                                // cancelSubscription();

                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    android.util.Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    android.util.Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                android.util.Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    /*GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            currentActivity, 0).show();*/
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        android.util.Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;

                                        result.startResolutionForResult(MainActivity.UserActivity,
                                                REQUEST_OAUTH_REQUEST_CODE);
                                    } catch (IntentSender.SendIntentException e) {
                                        android.util.Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();

        mClient.connect();

        startHeartCheckThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterFitnessDataListener();
    }

    /** Finds available data sources and attempts to register on a specific {@link DataType}. */
    private void findFitnessDataSources() {
        // [START find_data_sources]
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                .setDataTypes(DataType.TYPE_HEART_RATE_BPM)
                // Can specify whether data type is raw or derived.

                .setDataSourceTypes(DataSource.TYPE_RAW, DataSource.TYPE_DERIVED)
                //.setDataSourceTypes(DataSource.TYPE_RAW)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        Log.i(TAG, "Result: " + dataSourcesResult.getStatus().toString());
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            Log.i(TAG, "Data source found: " + dataSource.getName());
                            Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());

                            try {
                                //Let's register a listener to receive Activity data!
                                if (dataSource.getDataType().equals(DataType.TYPE_HEART_RATE_BPM) && dataSource.getName().equals("SWR12- heart rate bpm")
                                        && mListener == null) {
                                    Log.i(TAG, "Data source for TYPE_HEART_RATE_BPM found!  Registering.");
                                    registerFitnessDataListener(dataSource,
                                            DataType.TYPE_HEART_RATE_BPM);
                                }
                            }
                            catch (Exception e) {
                            }
                        }
                    }
                });
        // [END find_data_sources]
    }

    /**
     * Registers a listener with the Sensors API for the provided {@link DataSource} and {@link
     * DataType} combo.
     */

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        // [START register_data_listener]
        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Float heartrate = val.asFloat();
                    hr = heartrate.intValue();

                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
                    Log.i(TAG, "Detected DataPoint value: " + val);
                    //Log.i(TAG, "alert: " + alert);
                    conn_state = true;
                }
            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .build(),
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener registered!");
                            //  myTextView.append("Listener registered!\r\n");
                        } else {
                            Log.i(TAG, "Listener not registered.");
                        }
                    }
                });
        // [END register_data_listener]
    }

    private void unregisterFitnessDataListener() {
        if (mListener == null) {
            // This code only activates one listener at a time.  If there's no listener, there's
            // nothing to unregister.
            return;
        }

        // [START unregister_data_listener]
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.SensorsApi.remove(
                mClient,
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener was removed!");
                            conn_state = false;
                        } else {
                            Log.i(TAG, "Listener was not removed.");
                        }
                    }
                });
        // [END unregister_data_listener]
    }

    public void startHeartCheckThread() {
        //작업스레드 생성(매듭 묶는과정)
        SensorService.heartHandler heartRunnable = new SensorService.heartHandler();
        heartThread = new Thread(heartRunnable);
        heartThread.setDaemon(true);
        heartThread.start();
    }

    android.os.Handler receivehearthandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            if(conn_state) {
                setHeartData();

                Fragment_TabMain.heart_rate_value = heartDTO.HR;
                Fragment_TabMain.heart_time = heartDTO.TS;

                final Calendar cal = Calendar.getInstance();

                if(String.format("%d",cal.get(Calendar.DATE)) != Today) {
                    currentdate = String.format("%d-%d-%d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DATE));
                }

                databaseReference.child("RealTime").child("RUOK-" + account).setValue(heartDTO);
                databaseReference.child("History").child("RUOK-" + account).child(currentdate).push().setValue(heartDTO);

                if(!fit_mode) {
                    if(hr > max_heart_rate || hr < min_heart_rate) {
                        heart_count++;
                        if(heart_count > 5  && alert != true) {
                            //heartThread.interrupt();
                            alert = true;
                            Intent intent = new Intent(getApplicationContext(),AlertActivity.class);
                            startActivity(intent);
                        }
                    } else if(heart_count != 0) {
                        heart_count = 0;
                    }
                }
                conn_state = false;
            }
        }
    };

    public class heartHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                Message msg = Message.obtain();
                msg.what = 0;
                if(UserType == 0) {
                    receivehearthandler.sendMessage(msg);
                }
                try {
                    Thread.sleep(1000); // 갱신주기 1초
                } catch (Exception e) {
                }
            }
        }
    }
    public void setHeartData() {
        long now = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        Date date = new Date(now);

        //heartDTO.account = account;
        heartDTO.HR = hr;
        heartDTO.TS = dateFormat.format(date);
        heartDTO.LAT = lat;
        heartDTO.LON = lon;
    }


    private class GPSListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }

    private void StartLocationService() {
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        GPSListener gpsListener = new GPSListener();
        long minTime = 1000;
        float minDistance = 0;
        try {   //GPS 위치 요청
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void checkConnection() {
        databaseReference.child("Connection").child(account).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConnectDTO connectDTO = dataSnapshot.getValue(ConnectDTO.class);

                CONNECTING_STATE = connectDTO.CONNECTING_CODE;
                CONNECTING_ACCOUNT = connectDTO.ConnectionWith;
                //Log.d(TAG, connectDTO.ConnectionWith);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void LoadDataonFirebase() {
        databaseReference.child("Users").child(account).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserDTO userDTO = dataSnapshot.getValue(UserDTO.class);

                try {
                    min_heart_rate = userDTO.min_heart_rate;
                    max_heart_rate = userDTO.max_heart_rate;
                } catch (Exception e) {
                    Log.d(TAG,"데이터 로드 실패");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
