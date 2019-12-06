package e.kja.mygooglemaptest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private final static int MY_PERMISSIONS_REQ_LOC = 100;

    private GoogleMap mGoogleMap; //멤버 선언
    private LocationManager locManager; //위치 관리자
    private Location lastLocation; //앱에서 최종으로 수신한 위치 저장


    private Marker centerMarker; //기준점 위치 관련 멤버 변수
    private Marker clickMarker;

    private MarkerOptions centerMarkOptins;
    private MarkerOptions clickMarkOptins;

    private PolylineOptions lineOptions; //선그리기 관련 멤버 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //LocationManager준비
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //구글맵 준비
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map); //map 객체 준비
        mapFragment.getMapAsync(mapReadyCallBack); //map정보 가져오기(Callback호출)

        // 마커 옵션 생성 및 아이콘 지정
        centerMarkOptins = new MarkerOptions();
        clickMarkOptins = new MarkerOptions();

        lineOptions = new PolylineOptions();
        // 선그리기 옵션 지정
        lineOptions.color(Color.RED);
        lineOptions.width(5);

        // Permission 확인 후 마지막 위치 정보 확인
        checkPermission();

        lastLocation = locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);


    }
    OnMapReadyCallback mapReadyCallBack = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap; //map정보가져오기 (완료시 멤버에 저장)
            LatLng currentLoc;
            if (lastLocation == null){
                currentLoc = new LatLng(37.606320, 127.041808);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17));
                centerMarkOptins.position(currentLoc);
                centerMarker.showInfoWindow();
            }
            else{
                locationListener.onLocationChanged(lastLocation);
                //위도 경도를 저장할 수 있는 객체에 위치 선정

            }

            // mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15)); //지정한 위치로 이동 후 17의 배율로 확대
            //mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            //최종 위치로 이동 후 마커 표시

            final Geocoder geocoder = new Geocoder(MainActivity.this);
            //지도 관련 이벤트 구현
            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {

                    clickMarkOptins.position(latLng);
                    clickMarker = mGoogleMap.addMarker(clickMarkOptins);

                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        Address address = addresses.get(0);
                        clickMarkOptins.title(address.getAddressLine(0).toString());
                        centerMarker.showInfoWindow();
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }
            });



        }
    };
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQ_LOC);

                return;
            }
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button: //퍼미션 체크 후 위치 요청
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            100);
                    return;
                }
                locManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 5000, 0, locationListener);
                break;
            case R.id.button2: //위치 요청 중지
                locManager.removeUpdates(locationListener);
                break;
        }
    }

    //Location Listener
    LocationListener locationListener = new LocationListener() { //위치 정보를 수신할 때마다 해당 위치로 지도의 중심 변경
        @Override
        public void onLocationChanged(Location location) {
            //현재 수신 위치로 지도의 위치 이동 후
            LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17));
            mGoogleMap.clear();
            //마커 표시
            centerMarkOptins.position(currentLoc);
            centerMarker = mGoogleMap.addMarker(centerMarkOptins);
            centerMarker.showInfoWindow();


            //현재 위치를 기준으로 선그리기 수행
            lineOptions.add(currentLoc);
            mGoogleMap.addPolyline(lineOptions);


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}

