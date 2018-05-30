package com.example.mun.ruok.Fragment;

import android.graphics.Color;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.mun.ruok.Activity.MainActivity;
import com.example.mun.ruok.R;
import com.example.mun.ruok.Service.SensorService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lylc.widget.circularprogressbar.CircularProgressBar;

import static com.example.mun.ruok.Service.SensorService.sHeartDTO;


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

    private Double lat = 35.140665;
    private Double lon = 126.9285385;

    private int heart_start = 0;

    private LineChart mChart;
    private ImageView Heart;
    private Animation animation;

    private BluetoothAdapter mBluetoothAdapter = null; /* Intent request codes*/

    public static TextView HeartRateText,HeartTimeText;

    public static GoogleMap map;
    private MapView mapView = null;

    public static Fragment_TabMain TabMainContext;

    private Thread heartThread, thread;

    public static CircularProgressBar heart_seekbar;

    MainActivity mainclass = new MainActivity();

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
        spec.setIndicator(null, ResourcesCompat.getDrawable(getResources(), R.drawable.heartrate, null));
        spec.setContent(R.id.tab_content1);
        host.addTab(spec);

        spec = host.newTabSpec("tab2");
        spec.setIndicator(null, ResourcesCompat.getDrawable(getResources(), R.drawable.graph, null));
        spec.setContent(R.id.tab_content2);
        host.addTab(spec);

        TabMainContext = this;

        return rootView;

    }

    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {

        HeartRateText = (TextView) view.findViewById(R.id.HeartDataValue);
        mChart = (LineChart) view.findViewById(R.id.chart);
        heart_seekbar = (CircularProgressBar) view.findViewById(R.id.heartseekbar);

        Heart = (ImageView) view.findViewById(R.id.smallheart);

        animation = AnimationUtils.loadAnimation(getContext(), R.anim.wave);
        animation.setRepeatCount(Animation.INFINITE);
        Heart.startAnimation(animation);

        chart_setting();

        LineData data = new LineData();
        mChart.setData(data); // LineData를 셋팅함

        feedMultiple();

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
    }

    public static void ShowMyLocaion(Double lat, Double lon, GoogleMap googleMap) {
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

    // aqi seekbar
    public void seekani(CircularProgressBar item, int startval, int endval) {
        item.animateProgressTo(startval, endval, new CircularProgressBar.ProgressAnimationListener() {
            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationProgress(int progress) {
            }

            @Override
            public void onAnimationFinish() {
            }
        });

    }

    private void chart_setting() {
        // 차트의 아래 Axis
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // xAxis의 위치는 아래쪽
        // xAxis.setTextSize(10f); // xAxis에 표출되는 텍스트의 크기는 10f
        xAxis.setDrawGridLines(false); // xAxis의 그리드 라인을 없앰

        // 차트의 왼쪽 Axis
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false); // leftAxis의 그리드 라인을 없앰

        // 차트의 오른쪽 Axis
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false); // rightAxis를 비활성화 함
    }

    private void feedMultiple() {
        if (thread != null)
            thread.interrupt(); // 살아있는 쓰레드에 인터럽트를 검

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addEntry(); // addEntry를 실행하게 함
            }
        };

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    mainclass.runOnUiThread(runnable); // UI 쓰레드에서 위에서 생성한 runnable를 실행함
                    try {
                        Thread.sleep(500); // 0.5초간 쉼
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    private void addEntry() {
        LineData data = mChart.getData();

        LineDataSet set1 = (LineDataSet) data.getDataSetByIndex(0);

        if (set1 == null) {
            // creation of null
            set1 = createSet(Color.parseColor("#FFFF7A87"), "Heart-Rate");
            data.addDataSet(set1);
        }

        data.addEntry(new Entry(set1.getEntryCount(), sHeartDTO.getHeartRate()), 0);

        data.notifyDataChanged();                                      // data의 값 변동을 감지함

        mChart.notifyDataSetChanged();                                // chart의 값 변동을 감지함
        mChart.setVisibleXRangeMaximum(10);                           // chart에서 최대 X좌표기준으로 몇개의 데이터를 보여줄지 설정함
        mChart.moveViewToX(data.getEntryCount());                     // 가장 최근에 추가한 데이터의 위치로 chart를 이동함
    }

    private LineDataSet createSet(int setColor, String dataName) {
        LineDataSet set = new LineDataSet(null, dataName);            // 데이터셋의 이름을 "Dynamic Data"로 설정(기본 데이터는 null)
        set.setAxisDependency(YAxis.AxisDependency.LEFT);              // Axis를 YAxis의 LEFT를 기본으로 설정
        set.setColor(setColor);                                        // 데이터의 라인색을 HoloBlue로 설정
        set.setCircleColor(setColor);                                  // 데이터의 점을 WHITE로 설정 // 65536
        set.setLineWidth(2f);                                          // 라인의 두께를 2f로 설정
        set.setCircleRadius(4f);                                       // 데이터 점의 반지름을 4f로 설정
        set.setFillAlpha(65);                                          // 투명도 채우기를 65로 설정
        set.setFillColor(ColorTemplate.getHoloBlue());                 // 채우기 색을 HoloBlue로 설정
        set.setHighLightColor(Color.rgb(244, 117, 117));               // 하이라이트 컬러(선택시 색)을 rgb(244, 117, 117)로 설정
        set.setDrawValues(false);                                     // 각 데이터의 값을 텍스트로 나타내지 않게함(false)
        return set;                                                   // 이렇게 생성한 set을 반환
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
            seekani(heart_seekbar, Integer.parseInt(String.valueOf(Math.round(heart_start * 0.7))), Integer.parseInt(String.valueOf(Math.round(sHeartDTO.getHeartRate() * 0.7))));

            //animation.setDuration((20 - sHeartDTO.getHeartRate() / 5) * 80);

            heart_start = sHeartDTO.getHeartRate();

            HeartRateText.setText(String.valueOf(sHeartDTO.getHeartRate()));
            if(sHeartDTO.hasLocation()) {
                ShowMyLocaion(sHeartDTO.getLatitude(), sHeartDTO.getLongitude(), map);
            }
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
