package com.devssfx.titp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

public class CreatePlace extends AppCompatActivity {

    private ThisPlace _place = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_place);

        Button btn;
        TextView txt;

        Bundle bun = this.getIntent().getExtras();
        if (bun != null) {
            String placeCode = bun.getString("PlaceCode");

            DBHelper db = new DBHelper(this);
            _place = db.LoadPlaceFromCode(placeCode);
            db.close();

            if(_place != null) {
                txt = (TextView) findViewById(R.id.CP_txtPlaceName);
                txt.setText(_place.PlaceName);
                txt.setEnabled(_place.IsAdmin == 1);

                LinearLayout lay = (LinearLayout) findViewById(R.id.CP_layDelete);
                lay.setVisibility(View.VISIBLE);

                if (_place.IsAdmin == 0) {
                    btn = (Button) findViewById(R.id.CP_btnCreate);
                    btn.setText(R.string.CP_BecomeAdmin);
                }

                txt = (TextView) findViewById(R.id.CP_txvTitle);
                txt.setText(getResources().getString(R.string.CP_TitleEdit));
            }else{
                Toast.makeText(this, "The Place could not be found.", Toast.LENGTH_SHORT).show();
                try {
                    db = new DBHelper(this);
                    db.PlaceDelete(placeCode);
                    db.close();
                }finally {

                }
                //this.finish();
            }
        }

    }


    public void CP_btnCreate_Click(View view){

        if(_place != null && _place.IsAdmin == 0){ //user wants to become admin, validate password
            ValidateEditRequest();
        }else {

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                EditText txtName = (EditText) findViewById(R.id.CP_txtPlaceName);
                EditText txtPassword = (EditText) findViewById(R.id.CP_txtPassword);

                if (txtPassword.getText().length() == 0 || txtName.getText().length() == 0) {
                    Toast.makeText(this, "Enter both Name and Password.", Toast.LENGTH_LONG).show();
                } else {
                    String password, placeName;

                    password = txtPassword.getText().toString();
                    placeName = txtName.getText().toString();

                    DBHelper db = new DBHelper(this);

                    int placeDeviceId = 0;
                    if (_place == null) {
                        placeDeviceId = db.CreatePlace(password, placeName);
                    } else {
                        _place = db.LoadPlaceFromCode(_place.PlaceCode);
                        if (_place != null) {
                            placeDeviceId = _place.PlaceDeviceId;
                            _place.PlaceName = placeName;
                            _place.ModCount = _place.ModCount + 1;
                            db.PlaceSaveOnFind(_place);
                        }
                    }

                    if (placeDeviceId == 0) {
                        StatusUpdate("There was a problem saving the Place. Try again.");
                    } else {

                        DownloadWebpageTask dl = new DownloadWebpageTask();
                        dl.RequestMethod = "POST";
                        dl.UrlParams.add("ldid");
                        dl.UrlParams.add(placeDeviceId + "");
                        dl.UrlParams.add("pname");
                        dl.UrlParams.add(placeName);
                        dl.UrlParams.add("pdate");
                        dl.UrlParams.add("20151010");
                        dl.UrlParams.add("apw");
                        dl.UrlParams.add(password);
                        dl.UrlParams.add("upw");
                        dl.UrlParams.add("user pw");
                        dl.UrlParams.add("ctype");
                        dl.UrlParams.add("1");

                        if (_place != null) {
                            dl.UrlParams.add("pc");
                            dl.UrlParams.add(_place.PlaceCode);
                            dl.UrlParams.add("mc");
                            dl.UrlParams.add(_place.ModCount + "");
                        }

                        dl.execute("http://www.showlineup.com/titp/TITP_SavePlace");
                    }

                    db.close();
                }
            }else{
                Toast.makeText(getBaseContext(), "Make sure there is an internet connection.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void CP_btnDelete_Click(View view){
        if(_place == null){
            Toast.makeText(this, "Create a Place before trying to delete.", Toast.LENGTH_LONG).show(); //shouldn't get here
        }else {
            if(_place.PlaceCode == null || _place.PlaceCode.length() == 0 || _place.PlaceCode.equals("0") ){
                //dodgy, remove it and all 0 codes
                DBHelper db = new DBHelper(this);
                if(_place.PlaceCode == null)
                    db.PlaceDelete(null);
                else
                    db.PlaceDelete(_place.PlaceCode);

                db.close();
                this.finish();
            }else {
                if(_place.IsAdmin == 0){
                    DeleteLocal();
                }else {
                    EditText txtPassword = (EditText) findViewById(R.id.CP_txtPassword);

                    if (txtPassword.getText().length() == 0) {
                        DeleteLocal();
                    } else {
                        String password;

                        password = txtPassword.getText().toString();

                        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isConnected()) {
                            DownloadWebpageTask dl = new DownloadWebpageTask();
                            dl.Doing = 1;
                            dl.RequestMethod = "POST";
                            dl.UrlParams.add("pc");
                            dl.UrlParams.add(_place.PlaceCode);
                            dl.UrlParams.add("apw");
                            dl.UrlParams.add(password);

                            dl.execute("http://www.showlineup.com/titp/TITP_PlaceDelete");

                        } else {
                            Toast.makeText(getBaseContext(), "Make sure there is an internet connection.", Toast.LENGTH_LONG).show();
                        }

                    }
                }
            }
        }
    }

    private void DeleteLocal(){
        int success = DeletePlaceFromLocalDB(_place.PlaceCode);
        if(success == 1){
            Toast.makeText(CreatePlace.this, "Place deleted from your device.", Toast.LENGTH_SHORT).show();
            this.finish();
        }else{
            StatusUpdate("There was a problem deleting the Place. Close the screen and try again.");
        }
    }

    public void ValidateEditRequest() {
        EditText txtPassword = (EditText) findViewById(R.id.CP_txtPassword);
        String password = txtPassword.getText().toString();

        if (password.length() == 0) {
            StatusUpdate("Password is required.");
        } else {

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                DownloadWebpageTask dl = new DownloadWebpageTask();
                dl.RequestMethod = "POST";
                dl.UrlParams.add("pc");
                dl.UrlParams.add(_place.PlaceCode);
                dl.UrlParams.add("apw");
                dl.UrlParams.add(password);
                dl.Doing = 2;
                dl.execute("http://www.showlineup.com/titp/TITP_PlaceEnableEdit");
            } else {
                Toast.makeText(getBaseContext(), "Make sure there is an internet connection.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void CreateResult(String result, Integer Doing){

        if(result.length() > 0) {
            Jjson j = new Jjson(result);
            String rtnSuccess = j.getValue("Success");
            if(rtnSuccess.equals("1") || rtnSuccess.equals("3")) {

                if(Doing == 2) { //enable admin
                    if(_place != null && _place.PlaceCode.equals(j.getValue("PlaceCode"))){
                        DBHelper db = new DBHelper(this);
                        if(!db.EnableAdmin(_place.PlaceCode)){
                            StatusUpdate("There was a problem updating the local device. Please try again.");
                        }else{
                            findViewById(R.id.CP_txtPlaceName).setEnabled(true);
                            ((Button)findViewById(R.id.CP_btnCreate)).setText(R.string.SavePlace);
                            _place.IsAdmin = 1;
                            Toast.makeText(this, getResources().getString(R.string.CP_NowAdmin), Toast.LENGTH_LONG ).show();
                        }
                        db.close();
                    }else{
                        StatusUpdate("There was a problem, please try again.");
                    }
                }else {
                    int success = 0;
                    if (Doing == 0) {
                        DBHelper db = new DBHelper(this);
                        success = db.PlaceUpdateCode(
                                j.getValue("LocalId")
                                , j.getValue("PlaceId")
                                , j.getValue("PlaceCode")
                                , Integer.parseInt(j.getValue("ModCount"))
                        );
                        db.close();
                    } else if (Doing == 1) { //delete
                        success = DeletePlaceFromLocalDB(j.getValue("PlaceCode"));
                        if(success == 1)
                            Toast.makeText(CreatePlace.this, "Place deleted from server.", Toast.LENGTH_LONG).show();
                    }

                    if (success == 1) {
                        this.finish();
                    } else {
                        if (Doing == 0) {
                            StatusUpdate("There was a problem saving the new code. Return to the main screen and refresh the list.");
                            try {
                                DBHelper db = new DBHelper(this);
                                db.PlaceDeleteLocalOnly(Integer.parseInt(j.getValue("LocalId")));
                                db.close();
                            }finally {

                            }
                        }
                        else
                            StatusUpdate("There was a problem deleting the Place.");
                    }
                }


            }else if(rtnSuccess.equals("2")){
                if(Doing == 1)
                    StatusUpdate("Invalid Code or Password.");
            }else{ //rtnSuccess == 0
                if(Doing == 0) {
                    StatusUpdate("There was a problem saving the new Place. Return to the main screen and refresh the list.");
                    try {
                        DBHelper db = new DBHelper(this);
                        db.PlaceDeleteLocalOnly(Integer.parseInt(j.getValue("LocalId")));
                        db.close();
                    } finally {

                    }
                }
                else if(Doing == 1)
                    StatusUpdate("There was a problem deleting the Place.");
                else if(Doing == 2)
                    StatusUpdate("Invalid Password.");
                else
                    StatusUpdate("There was a problem.");
            }
        }else{
            if(Doing == 0)
                StatusUpdate("There was a problem saving the new code. Return to the main screen and refresh the list.");
            else
                StatusUpdate("There was a problem deleting the Place.");
        }

    }

    public int DeletePlaceFromLocalDB(String placeCode){
        int success;
        DBHelper db = new DBHelper(this);
        success = db.PlaceDelete(placeCode);
        db.close();

        return success;
    }

    public void StatusUpdate(String status) {
        Toast.makeText(getBaseContext(), status, Toast.LENGTH_SHORT).show();
    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        public ArrayList<String> UrlParams = new ArrayList<String>(); //name then value, repeat
        public String RequestMethod = "GET"; //GET or POST
        public Integer Doing = 0; //0=save, 1=delete, 2=enable admin

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

                if (UrlParams.size() > 0) {
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    for (int i = 0; i < UrlParams.size(); i = i + 2) {
                        if (i != 0)
                            writer.write("&");
                        writer.write(UrlParams.get(i) + "=" + URLEncoder.encode(UrlParams.get(i + 1), "UTF-8"));
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

