package com.example.mun.ruok.Fragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mun.ruok.DTO.HeartDTO;
import com.example.mun.ruok.House;
import com.example.mun.ruok.Activity.MainActivity;
import com.example.mun.ruok.R;
import com.example.mun.ruok.Service.SensorService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.mun.ruok.Service.SensorService.sConnData;
import static com.example.mun.ruok.Service.SensorService.sUserData;

public class HistoryFragment extends Fragment implements OnMapReadyCallback {

    private String TAG = "HistoryFragment";

    private ViewGroup rootView;

    private LineChart mChart;
    private TextView dateText;

    private GoogleMap map;
    private MapView mapView = null;
    private Button datebtn;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private ClusterManager<House> mClusterManager;

    private ArrayList<LatLng> marker = new ArrayList<>();

    private static final int CONNECTING_PERMISSION_CODE = 2;

    private String date_of_history;
    private String str[];

    private int count;
    private String account;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);

        if(sUserData.getUserType()) {
            account = sUserData.getUserEmailID();
        } else {
            if (sConnData.getConnectingCode() == CONNECTING_PERMISSION_CODE) {
                account = sConnData.getConnectionWith();
            } else {
                account = SensorService.sAccount;
            }
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);

        final Calendar cal = Calendar.getInstance();

        mChart = (LineChart) view.findViewById(R.id.historychart);
        dateText = (TextView) view.findViewById(R.id.dateText);

        datebtn = (Button) view.findViewById(R.id.btn_date);
        datebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.UserActContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, final int month, int dayOfMonth) {
                        final List<Entry> entries = new ArrayList<>();
                        final ArrayList<String> labels = new ArrayList<String>();

                        date_of_history = String.format("%d-%d-%d",year,month+1,dayOfMonth);
                        databaseReference.child("History").child("RUOK-" + account).child(date_of_history).orderByChild("TS").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    count = 0;

                                    map.clear();        // 맵 상에 있는 모든 마커 삭제
                                    marker.clear();     // 이전에 불러온 마커 위치 삭제

                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        HeartDTO heartDTO = snapshot.getValue(HeartDTO.class);

                                        str = heartDTO.getTimeStamp().split(" ");

                                        ShowMyLocaion(heartDTO.getLatitude(), heartDTO.getLonitude(), map, str[1], heartDTO.getHeartRate());

                                        marker.add(new LatLng(heartDTO.getLatitude(), heartDTO.getLonitude()));
                                        entries.add(new Entry(count, heartDTO.getHeartRate()));
                                        labels.add(str[1]);
                                        count++;
                                    }

                                    map.moveCamera(CameraUpdateFactory.newLatLng(marker.get(count - 1)));
                                    map.animateCamera(CameraUpdateFactory.zoomTo(17));

                                    LineDataSet lineDataSet = createSet(Color.parseColor("#FFFF7A87"), "Heart-Rate", entries);
                                    LineData lineData = new LineData(lineDataSet);

                                    chart_setting(labels);

                                    mChart.setData(lineData);

                                    mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                        @Override
                                        public void onValueSelected(Entry e, Highlight h) {
                                            int x = (int) e.getX();

                                            map.moveCamera(CameraUpdateFactory.newLatLng(marker.get(x)));
                                            map.animateCamera(CameraUpdateFactory.zoomTo(17));

                                            Toast.makeText(MainActivity.UserActContext, "심박수 : " + String.valueOf((int) entries.get(x).getY()), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onNothingSelected() {

                                        }
                                    });

                                    mChart.invalidate();
                                    mChart.setVisibleXRangeMaximum(10);                           // chart에서 최대 X좌표기준으로 몇개의 데이터를 보여줄지 설정함
                                    mChart.moveViewToX(count);                     // 가장 최근에 추가한 데이터의 위치로 chart를 이동함

                                    dateText.setText(date_of_history);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        //Toast.makeText(MainActivity.UserActContext,"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.show();
            }
        });

        mapView = (MapView) view.findViewById(R.id.historymapView);
        mapView.getMapAsync(this);

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
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

        mClusterManager = new ClusterManager<>(MainActivity.UserActContext, map);
    }

    private void chart_setting(final ArrayList<String> labels) {
        // 차트의 아래 Axis
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // xAxis의 위치는 아래쪽
        // xAxis.setTextSize(10f); // xAxis에 표출되는 텍스트의 크기는 10f
        xAxis.setDrawGridLines(false); // xAxis의 그리드 라인을 없앰
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (labels.size() > (int) value) {
                    return labels.get((int) value);
                } else return null;
            }
        });

        // 차트의 왼쪽 Axis
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false); // leftAxis의 그리드 라인을 없앰

        // 차트의 오른쪽 Axis
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false); // rightAxis를 비활성화 함
    }

    private LineDataSet createSet(int setColor, String dataName, List<Entry> entries) {
        LineDataSet set = new LineDataSet(entries, dataName);            // 데이터셋의 이름을 "Dynamic Data"로 설정(기본 데이터는 null)
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

    private void ShowMyLocaion(Double lat, Double lon, GoogleMap googleMap, String title, int HeartRate) {
        try {
            LatLng nowLocation = new LatLng(lat, lon);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(nowLocation);
            markerOptions.title(title);

            //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(getResources().getIdentifier(cloudimage,"drawable","com.example.khseob0715.sanfirst"));

            //markerOptions.icon(icon);

            //googleMap.clear();

            // Get back the mutable Circle
            //googleMap.addCircle(circleOptions);

            googleMap.addMarker(markerOptions);

            if(HeartRate < sUserData.getMaxHeartRate() && HeartRate > sUserData.getMinHeartRate()) {
                mClusterManager.addItem(new House(nowLocation, title));
            }
            //googleMap.moveCamera(CameraUpdateFactory.newLatLng(nowLocation));
            //googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        }   catch (IllegalStateException e)   {
        }
    }
}
