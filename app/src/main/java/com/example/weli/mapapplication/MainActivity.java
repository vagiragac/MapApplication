package com.example.weli.mapapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.weli.mapapplication.POJO.Example;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    Button btnYakin;
    LatLng origin;//location için latlng api yani bir nevi enlem boylam :
    LatLng dest;
    ArrayList<LatLng> MarkerPoints;
    TextView ShowDistanceDuration;
    Polyline line;
    MarkerOptions options = new MarkerOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShowDistanceDuration = (TextView) findViewById(R.id.mesafe_saati);
        btnYakin=(Button)findViewById(R.id.btnYakin); 
        MarkerPoints = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public  LatLng[] yerler = new LatLng[5];

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Benim konumum 37.7387257,29.1022491
        final LatLng myLocatin = new LatLng(37.7387257,29.1022491);
        yerler[0] = new LatLng(37.9249423,29.1209595);//traverten
        yerler[1] = new LatLng(38.0272529,29.2694806);//ağlayan kaya şelalesi
        yerler[2] = new LatLng(37.3879149,29.4978918);//keloğlan mağarası
        yerler[3] = new LatLng(38.0381286,28.9487406);//tripolis antik kenti
        yerler[4] = new LatLng(38.1195734,29.0585033);//güney şelale
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(yerler[0]).title("TRAVERTEN"));
        mMap.addMarker(new MarkerOptions().position(yerler[1]).title("AĞLAYAN KAYA ŞELALESİ"));
        mMap.addMarker(new MarkerOptions().position(yerler[2]).title("KELOĞLAN MAĞARASI"));
        mMap.addMarker(new MarkerOptions().position(yerler[3]).title("TRİPOLİS ANTİK KENTİ"));
        mMap.addMarker(new MarkerOptions().position(yerler[4]).title("GÜNEY ŞELALE"));

        //final LatLng konumum = new LatLng(41, 28);//emülatörde gösterilen kısım
        mMap.addMarker(new MarkerOptions().position(myLocatin).title("Konumum"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocatin));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        mMap.setMyLocationEnabled(true);
        MarkerPoints.add(myLocatin);
        options.position(myLocatin);
        for (int a = 0; a < 5; a++) {
            double x = yerler[a].latitude - myLocatin.latitude;
            double y = yerler[a].longitude - myLocatin.longitude;
        }
            //en yakını bulma butonu 5 noktayı karşılaştırma kendi konumunla
        btnYakin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng minLoc = yerler[0];
                for (int i=1;i<5;i++){
                    if(CalculationByDistance(myLocatin,yerler[i])<CalculationByDistance(myLocatin,minLoc))
                        minLoc=yerler[i];
                }
                mMap.addMarker(new MarkerOptions().position(minLoc).title("en yakın konum"));
                mMap.addMarker(new MarkerOptions().position(minLoc).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                rotaCiz(myLocatin,minLoc);
            }
        });

        // harita için click olayı
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // iki kereden fazla bazılırsa haritadaki işaretlileri temizle
                if (MarkerPoints.size() >1) {
                    mMap.clear();
                    MarkerPoints.clear(); 
                    ShowDistanceDuration.setText("");
                }
                // arraye yeni öge ekleme
                MarkerPoints.add(point);
                // işaret ögeleri için konum ayarlama
                options.position(point);

                if (MarkerPoints.size() == 1) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                } else if (MarkerPoints.size() == 2) {
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
                // işaret ekleme
                mMap.addMarker(options);
                // başlama ve bitiş yerlerini kontrol eder
                if (MarkerPoints.size() >= 2) {
                    origin = MarkerPoints.get(0);
                    dest = MarkerPoints.get(1);
                }
                //yukardan gelen bilidirimle  için natification fonksiyonu

                NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher) //Bunu eklemezsen calismaz patlar.
                        .setContentTitle("My Location")
                        .setContentText("konumum enlem olarak="+myLocatin.latitude + " \n " + "konumum boylam olarak=" + myLocatin.longitude);
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity( getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(0,mBuilder.build());
            }
        });
        Button btnDriving = (Button) findViewById(R.id.btnDriving);
        btnDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                build_retrofit_and_get_response("driving");
            }
        });

        Button btnWalk = (Button) findViewById(R.id.btnWalk);
        btnWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                build_retrofit_and_get_response("walking");
            }
        });
    }
    private void build_retrofit_and_get_response(String type) {
        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getDistanceDuration("metric", origin.latitude + "," +
                origin.longitude, dest.latitude + "," + dest.longitude, type);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit) {

                try {
                    //Remove previous line from map
                    if (line != null) {
                        line.remove();
                    }
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getRoutes().size(); i++) {
                        String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                        String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                        ShowDistanceDuration.setText("Uzaklik:" + distance + ", Sure:" + time);
                        String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                        List<LatLng> list = decodePoly(encodedString);
                        line = mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(20)
                                .color(Color.RED)
                                .geodesic(true)
                        );
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }

    private void rotaCiz(LatLng baslangic,LatLng bitis) {
        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getDistanceDuration("metric", baslangic.latitude + "," +
                baslangic.longitude, bitis.latitude + "," + bitis.longitude, "driving");
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit) {

                try {
                    //Remove previous line from map
                    if (line != null) {
                        line.remove();
                    }
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getRoutes().size(); i++) {
                        String distance = response.body().getRoutes().get(i).getLegs().get(i).getDistance().getText();
                        String time = response.body().getRoutes().get(i).getLegs().get(i).getDuration().getText();
                        ShowDistanceDuration.setText("Uzaklik:" + distance + ", Sure:" + time);
                        String encodedString = response.body().getRoutes().get(0).getOverviewPolyline().getPoints();
                        List<LatLng> list = decodePoly(encodedString);
                        line = mMap.addPolyline(new PolylineOptions()
                                .addAll(list)
                                .width(15)
                                .color(Color.BLUE)
                                .geodesic(true)
                        );
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }
    //konum ayarlarının kontrolü
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }
        return poly;
    }
    //en yakın nokataları bulma algoritması
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }
}
