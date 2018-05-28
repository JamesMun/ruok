package com.example.mun.ruok.Service;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.mun.ruok.Activity.AlertActivity;
import com.example.mun.ruok.Activity.MainActivity;
import com.example.mun.ruok.DTO.ConnectDTO;
import com.example.mun.ruok.DTO.FitDTO;
import com.example.mun.ruok.DTO.HeartDTO;
import com.example.mun.ruok.DTO.UserDTO;
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
import java.util.Timer;
import java.util.TimerTask;
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

    public static int sHeartRate = 0;

    private HeartDTO heartDTO = new HeartDTO();

    public static UserDTO sUserData;
    public static FitDTO sFitData;
    public static ConnectDTO sConnData;

    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    public static OnDataPointListener mListener = null;
    // [END mListener_variable_reference]

    public static int sHeart_Count = 0;

    public static boolean sAlert = false;
    public static boolean sFit_mode = false;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public static String sAccount;

    public static Double lat, lon;

    private String mCurrentDate;

    private ProgressDialog pd;

    public static Timer timer;
    public static TimerTask timerTask;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand() 호출됨.");

        sAccount = MainActivity.account;
        mListener = null;

        if(intent == null) {
            return Service.START_STICKY;
        } else {
            pd = ProgressDialog.show(MainActivity.UserActContext, "로딩중", "데이터를 로딩중 입니다....",true, true);
            checkConnection(intent);  // 계정 간 연결 상태 확인
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void checkConnection(final Intent intent) {
        databaseReference.child("Connection").child(sAccount).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sConnData = dataSnapshot.getValue(ConnectDTO.class);

                LoadDataOnFirebase(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void LoadDataOnFirebase(final Intent intent) {
        databaseReference.child("Users").child(sAccount).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sUserData = dataSnapshot.getValue(UserDTO.class);

                Log.d(TAG, String.valueOf(sUserData.getUserType()));

                if(sUserData.getUserType()) {
                    try {
                        databaseReference.child("Fitness").child(sAccount).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                sFitData = dataSnapshot.getValue(FitDTO.class);
                                Log.d(TAG,"운동시간 불러오기");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } catch (Exception e) {
                        Log.d(TAG, "데이터 로드 실패");
                    }
                    StartLocationService();
                    processCommand(intent);
                    pd.dismiss();
                } else {
                    if(sConnData.getConnectingCode() == CONNECTING_PERMISSION_CODE) {

                        databaseReference.child("RealTime").child("RUOK-" + sConnData.getConnectionWith()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    heartDTO = dataSnapshot.getValue(HeartDTO.class);
                                    Fragment_TabMain.heart_rate_value = heartDTO.getHeartRate();
                                    Fragment_TabMain.heart_time = heartDTO.getTimeStamp();
                                    Fragment_TabMain.ShowMyLocaion(heartDTO.getLatitude(), heartDTO.getLongitude(), Fragment_TabMain.map);
                                } catch (NullPointerException e) {
                                    Log.d(TAG,"연결 끊김");
                                }
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        pd.dismiss();
                        Toast.makeText(MainActivity.UserActContext,"연결 대상이 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                    sHeartRate = heartrate.intValue();

                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
                    Log.i(TAG, "Detected DataPoint value: " + val);

                    ReceiveHeartData();
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
                        } else {
                            Log.i(TAG, "Listener was not removed.");
                        }
                    }
                });
        // [END unregister_data_listener]
    }

    public void setHeartData() {
        long now = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        Date date = new Date(now);

        heartDTO.setHeartData(sHeartRate, dateFormat.format(date), lat, lon);
    }


    private class GPSListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            Fragment_TabMain.ShowMyLocaion(lat,lon,Fragment_TabMain.map);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
            lat = null;
            lon = null;
            Toast.makeText(MainActivity.UserActContext,"위치 서비스를 켜지 않으면 데이터가 저장되지 않습니다.", Toast.LENGTH_LONG).show();
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

            try {
                Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                Fragment_TabMain.ShowMyLocaion(location.getLatitude(), location.getLongitude(), Fragment_TabMain.map);
            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.UserActContext,"위치 서비스에 문제가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

            manager.requestLocationUpdates(GPS_PROVIDER, minTime, minDistance, (android.location.LocationListener) gpsListener);

            // location request with network
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, (android.location.LocationListener) gpsListener);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void ReceiveHeartData() {
        setHeartData();

        Fragment_TabMain.HeartRateText.setText(String.valueOf(sHeartRate));
        Fragment_TabMain.HeartTimeText.setText(heartDTO.getTimeStamp());
        //Fragment_TabMain.progressBar.setProgress(mHeartRate);

        final Calendar cal = Calendar.getInstance();

        mCurrentDate = String.format("%d-%d-%d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));

        if (heartDTO.hasLocation()) {
            //Fragment_TabMain.ShowMyLocaion(lat,lon,Fragment_TabMain.map);
            databaseReference.child("RealTime").child("RUOK-" + sAccount).setValue(heartDTO);
            databaseReference.child("History").child("RUOK-" + sAccount).child(mCurrentDate).push().setValue(heartDTO);
        }

        if (!sFit_mode) {
            heartChecker(sUserData.getMaxHeartRate(), sUserData.getMinHeartRate());
        } else {
            heartChecker(sFitData.getFitMaxHeartRate(), sFitData.getFitMinHeartRate());
        }
    }

    private void heartChecker(int max, int min) {
        if (sHeartRate > max || sHeartRate < min) {
            sHeart_Count++;
            if (sHeart_Count > 5 && !sAlert) {
                sAlert = true;
                Intent intent = new Intent(getApplicationContext(), AlertActivity.class);
                startActivity(intent);
            }
        } else if (sHeart_Count != 0) {
            sHeart_Count = 0;
        }
    }

    public static void fitStart() {

        int time = sFitData.getFitHour() * 3600000 + sFitData.getFitMinute() * 60000;
        Log.d(TAG,"운동 시간 : " + String.valueOf(time));

        timer = new Timer(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if(!sFit_mode) {
                    sFit_mode = !sFit_mode;
                } else if(sFit_mode) {
                    sFit_mode = !sFit_mode;
                    timer.cancel();
                    Log.d(TAG,"운동 모드 종료");
                }
                Log.d(TAG,"운동 모드 : " + String.valueOf(sFit_mode));
            }

            @Override
            public boolean cancel() {
                return super.cancel();
            }
        };
        timer.schedule(timerTask, 0, time);
    }
}
