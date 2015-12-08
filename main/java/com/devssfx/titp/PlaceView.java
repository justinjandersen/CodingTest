package com.devssfx.titp;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PlaceView extends AppCompatActivity {

    private ThisPlace _place = null;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Marker mMarkNew = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_view);

        Bundle bun = this.getIntent().getExtras();
        String placeCode = bun.getString("PlaceCode");

        DBHelper db = new DBHelper(this);
        _place = db.LoadPlaceFromCode(placeCode);
        db.close();

        TextView txv;

        txv = (TextView)findViewById(R.id.PV_txvPlaceName);
        txv.setText(_place.PlaceName);

        String address = "";
        if(_place.IsLocationSet()) {
            if (_place.PlaceAddress != null && _place.PlaceAddress.length() != 0) {
                address = _place.PlaceAddress;
            } else {
                if (_place.PlaceLat != null && _place.PlaceLat.length() != 0) {
                    address = "Location Set";
                }
            }
        }
        if(address.length() > 0) {
            txv = (TextView) findViewById(R.id.PV_txvPlaceAddress);
            txv.setText(address);
        }
        setUpMapIfNeeded();


        setButtons();


        txv = (TextView)findViewById(R.id.PV_txvStatus);
        txv.setText(R.string.Refreshing);
        RefreshFromServer();

    }

    private void setButtons(){
        //Button btn;

        //btn = (Button)findViewById(R.id.PV_btnNavigate);
        //btn.setEnabled(_place.IsLocationSet());

        //btn = (Button)findViewById(R.id.PV_btnLocationClear);
        if(_place.IsAdmin == 1) {
            findViewById(R.id.PV_layLocationSet).setVisibility(View.VISIBLE);
            if(mMarkNew != null){
                findViewById(R.id.PV_btnLocationClear).setEnabled(true);
            }
            findViewById(R.id.PV_btnLocationSet).setEnabled(!_place.IsLocationSet());

        }else{
            findViewById(R.id.PV_layLocationSet).setVisibility(View.GONE);
        }



    }

    private void UpdateLocationToServer(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            DownloadWebpageTask dl = new DownloadWebpageTask();
            dl.Doing = 1;
            dl.UrlParams.add("pc");
            dl.UrlParams.add(_place.PlaceCode);
            dl.UrlParams.add("plat");
            dl.UrlParams.add(_place.PlaceLat+"");
            dl.UrlParams.add("plng");
            dl.UrlParams.add(_place.PlaceLong+"");
            dl.UrlParams.add("mc");
            dl.UrlParams.add(_place.ModCount+"");

            dl.RequestMethod = "POST";
            dl.execute("http://www.showlineup.com/titp/TITP_UpdateLocation");
        } else {
            Toast.makeText(this, R.string.NoConnection, Toast.LENGTH_LONG).show();
        }

    }

    private void setUpMapIfNeeded() {

        //try {
            if (mMap == null) {
                mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.PV_map)).getMap();
                if (mMap != null) {
                    setUpMap();
                }
            }

            if (mMap != null){
                if(_place != null) {
                    if (mMarkNew == null && _place.IsLocationSet()) {
                        if (_place.PlaceLat != null && _place.PlaceLat.length() > 0) {
                            LatLng mLatLongThem = new LatLng(Double.parseDouble(_place.PlaceLat), Double.parseDouble(_place.PlaceLong));
                            mMarkNew = mMap.addMarker(new MarkerOptions().position(mLatLongThem).title(_place.PlaceName));
                            CameraUpdate center = CameraUpdateFactory.newLatLng(mLatLongThem);
                            CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);
                            mMap.moveCamera(center);
                            mMap.moveCamera(zoom);
                        }
                    }
                }
            }
        //}finally
        //{

        //}


    }

    public void PV_btnLocationSet_Click(View view){
        if(mMarkNew == null){ //set location to current location
            if(mMap != null){
                Location loc = mMap.getMyLocation();
                if(loc != null){
                    Double lat = loc.getLatitude();
                    Double lng = loc.getLongitude();
                    _place.PlaceLat = lat.toString();
                    _place.PlaceLong = lng.toString();

                    mMarkNew = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(_place.PlaceName));

                    findViewById(R.id.PV_btnLocationSet).setEnabled(false);
                    findViewById(R.id.PV_btnLocationClear).setEnabled(true);

                    UpdateLocationToDBAndServer();

                }
            }
        }else{
            //new position for the marker, update local and db
            LatLng ll = mMarkNew.getPosition();
            Double lat = ll.latitude;
            Double lng = ll.longitude;
            _place.PlaceLat = lat.toString();
            _place.PlaceLong = lng.toString();

            findViewById(R.id.PV_btnLocationSet).setEnabled(false);
            findViewById(R.id.PV_btnLocationClear).setEnabled(true);

            UpdateLocationToDBAndServer();
        }
    }
    public void PV_btnLocationClear_Click(View view){
        if(mMarkNew != null){
            if(mMap != null){
                mMarkNew.remove();
                mMarkNew = null;

                findViewById(R.id.PV_btnLocationSet).setEnabled(true);
                findViewById(R.id.PV_btnLocationClear).setEnabled(false);

                _place.PlaceLat = "";
                _place.PlaceLong = "";
                _place.PlaceAddress = "";

                UpdateLocationToDBAndServer();

            }
        }




    }

    private void UpdateLocationToDBAndServer(){

        _place.ModCount++;

        DBHelper db = new DBHelper(this);
        //boolean success =
        db.UpdateLocation(_place);
        db.close();

        UpdateLocationToServer();

    }


    private Integer _keepCenter = 0;
    private Boolean _keepCenterIgnore = false;

    private void setUpMap() {

        mMap.setMyLocationEnabled(true);

        if(_place.IsAdmin == 1) {
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {

                    if (mMarkNew != null) {
                        mMarkNew.remove();
                    }
                    mMarkNew = mMap.addMarker(new MarkerOptions().position(latLng).title(_place.PlaceName));

                    findViewById(R.id.PV_btnLocationSet).setEnabled(true);
                    findViewById(R.id.PV_btnLocationClear).setEnabled(true);

                }
            });
        }

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(!_keepCenterIgnore) {
                    if (_keepCenter == 1)
                        _keepCenter = 2;//map moved manually, so don't re-center.
                }
                _keepCenterIgnore = false;
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                _keepCenter = 1;

                if(mMarkNew != null) {
                    Location loc = mMap.getMyLocation();
                    CameraUpdate cu;
                    if(loc != null) { //bound location and marker
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        LatLng ll = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                        builder.include(ll);
                        builder.include(mMarkNew.getPosition());
                        LatLngBounds bounds = builder.build();
                        int padding = 25; // offset from edges of the map in pixels
                        cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        mMap.animateCamera(cu);
                    }else{ //locate onto marker only
                        cu = CameraUpdateFactory.newLatLng(mMarkNew.getPosition());
                    }
                    mMap.animateCamera(cu);

                    return true;
                }else
                    return false;
            }
        });

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
               @Override
               public void onMyLocationChange(Location arg0) {

                    if(_keepCenter < 2) {

                        LatLng llMe = new LatLng(arg0.getLatitude(), arg0.getLongitude());

                        if (mMarkNew == null) {
                            if (mMap.isMyLocationEnabled()) {

                                float zoom = 13;
                                if(_keepCenter == 1)
                                    zoom = mMap.getCameraPosition().zoom;
                                CameraPosition camPos = new CameraPosition.Builder()
                                        .target(llMe).zoom(zoom).build();

                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos), new GoogleMap.CancelableCallback() {
                                    @Override
                                    public void onFinish() {
                                        _keepCenterIgnore = true;
                                        _keepCenter = 1;
                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                });
                            }
                        }else{
                           LatLngBounds.Builder builder = new LatLngBounds.Builder();
                           builder.include(llMe); //mMarkMe.getPosition());
                           builder.include(mMarkNew.getPosition()); //mMarkThem.getPosition());
                           LatLngBounds bounds = builder.build();
                           int padding = 25; // offset from edges of the map in pixels
                           CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                           mMap.animateCamera(cu, new GoogleMap.CancelableCallback(){
                               @Override
                               public void onFinish() {
                                   _keepCenterIgnore = true;
                                   _keepCenter = 1;
                               }
                               @Override
                               public void onCancel() {

                               }
                           });
                        }
                    }//keep center



               }
           }
        );



    }

    private void mapLocationUpdate(){
        //focus on them if available
        if (_place != null) {
            if(_place.IsLocationSet()) {
                if (_place.PlaceLat != null && _place.PlaceLat.length() > 0) {
                    if(mMarkNew != null)
                        mMarkNew.remove();

                    setUpMapIfNeeded();
                }
            }
        }
    }


    private void RefreshFromServer(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            DownloadWebpageTask dl = new DownloadWebpageTask();
            dl.UrlParams.add("pc");
            dl.UrlParams.add(_place.PlaceCode);
            //dl.UrlParams.add("pw");
            //dl.UrlParams.add("1");
            //dl.UrlParams.add("pwt");
            //dl.UrlParams.add("1");
            dl.RequestMethod = "POST";
            dl.execute("http://www.showlineup.com/titp/TITP_PlaceCodeSearch");
        } else {
            Toast.makeText(this, "Check the internet connection and try again.", Toast.LENGTH_LONG).show();
        }

    }


    public void CreateResult(String result, Integer Doing){

        TextView txv;
        if(result.length() > 0) {
            Jjson j = new Jjson(result);

            if (j.getValue("Success").equals("1")) {
                if(Doing == 0) {

                    Integer modCount = Integer.parseInt(j.getValue("ModCount"));

                    if (modCount > _place.ModCount) {

                        _place.PlaceId = j.getValue("PlaceId");
                        _place.PlaceCode = j.getValue("PlaceCode");
                        _place.PlaceName = j.getValue("PlaceName");
                        _place.PlaceDate = Integer.parseInt(j.getValue("PlaceDate"));
                        _place.PlaceAddress = j.getValue("PlaceAddress");
                        _place.PlaceLat = j.getValue("PlaceLat");
                        _place.PlaceLong = j.getValue("PlaceLong");
                        //_place.PlaceUserId = j.getValue("PlaceUerId");
                        _place.CreateDate = Integer.parseInt(j.getValue("CreateDate"));
                        _place.ModCount = modCount;
                        DBHelper db = new DBHelper(this);
                        db.PlaceSaveOnFind(_place);
                        db.close();


                        txv = (TextView) findViewById(R.id.PV_txvPlaceName);
                        txv.setText(_place.PlaceName);

                        String address = "";
                        if (_place.PlaceAddress == null || _place.PlaceAddress.length() == 0) {
                            if (_place.PlaceLat != null && _place.PlaceLat.length() != 0) {
                                address = "Location Set";
                            }
                        } else {
                            address = _place.PlaceAddress;
                        }
                        if (address.length() > 0) {
                            txv = (TextView) findViewById(R.id.PV_txvPlaceAddress);
                            txv.setText(address);
                        }
                        if (mMarkNew != null) { //will be updated in mapLocationUpdate
                            mMarkNew.remove();
                            mMarkNew = null;
                        }
                        mapLocationUpdate();
                        setButtons();

                    }
                    txv = (TextView) findViewById(R.id.PV_txvStatus);
                    txv.setText(R.string.PV_Refreshed);
                }else if(Doing == 1){

                    txv = (TextView) findViewById(R.id.PV_txvPlaceAddress);
                    if(j.getValue("Result").equals("1")){
                        txv.setText(R.string.LocationCleared);
                    }else{
                        txv.setText(R.string.LocationSet);
                    }

                }
            }else{
                txv = (TextView) findViewById(R.id.PV_txvStatus);
                if(Doing == 0)
                    txv.setText(R.string.ProblemRefresh);
                else
                    txv.setText(R.string.ProblemUpdating);
            }
        }else {
            txv = (TextView) findViewById(R.id.PV_txvStatus);
            if(Doing == 0)
                txv.setText(R.string.ProblemRefresh);
            else
                txv.setText(R.string.ProblemUpdating);
        }
    }


    public void PV_btnNavigate_Click(View view){

        if(mMarkNew != null) {

            //dirflg=h - Switches on "Avoid Highways" route finding mode.
            //dirflg=t - Switches on "Avoid Tolls" route finding mode.
            //dirflg=r - Switches on "Public Transit" - only works in some areas.
            //dirflg=w - Switches to walking directions - still in beta.
            //dirflg=d - Switches to driving directions

            String url = "http://maps.google.com/maps?";
            //url = mMarkNew.getPosition().latitude + "," + mMarkNew.getPosition().longitude
            url += "daddr=" + mMarkNew.getPosition().latitude + "," + mMarkNew.getPosition().longitude + "&mode=driving";

            Uri gmmIntentUri = Uri.parse(url);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                //web
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri.parse("http://maps.google.com/maps?"
                        //+ "&daddr=" + mMarkThem.getPosition().latitude + "," + mMarkThem.getPosition().longitude
                        + "saddr=" + mMarkNew.getPosition().latitude + "," + mMarkNew.getPosition().longitude
                );
                startActivity(intent);
            }
        }else{
            Toast.makeText(this.getApplicationContext(), "No location has been set.", Toast.LENGTH_SHORT).show();
        }

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_place_view, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        public ArrayList<String> UrlParams = new ArrayList<String>(); //name then value, repeat
        public String RequestMethod = "GET"; //GET or POST
        public int Doing = 0; //0=refresh, 1=update ll

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            CreateResult(result, Doing);
        }

        private String downloadUrl(String urlAddress) throws IOException {
            String contentAsString = "";

            InputStream is = null;
            try {
                URL url = new URL(urlAddress);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod(RequestMethod);
                conn.setDoInput(true);

                if(UrlParams.size() > 0){
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    for(int i = 0; i < UrlParams.size(); i = i + 2){
                        if(i != 0)
                            writer.write("&");
                        writer.write(UrlParams.get(i) + "=" + URLEncoder.encode(UrlParams.get(i+1),"UTF-8"));
                    }
                    writer.flush();
                    writer.close();
                    os.close();
                }

                // Starts the query
                conn.connect();
                //int response = conn.getResponseCode();
                conn.getResponseCode();

                is = conn.getInputStream();

                contentAsString = readIt(is);

            } finally {
                if (is != null) {
                    is.close();
                }
            }

            return contentAsString;
        }

        public String readIt(InputStream stream) throws IOException {

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return sb.toString();

        }


    }

}
