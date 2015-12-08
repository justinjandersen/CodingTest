package com.devssfx.titp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

public class FindPlace extends AppCompatActivity {

    private ThisPlace _place = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_place);
    }

    public void btnSearch_Click(View view){

        EditText txtPlaceCode = (EditText)findViewById(R.id.FP_txtPlaceCode);
        String placeCode = txtPlaceCode.getText().toString();
        if(placeCode.length() > 8){ //old codes were just 8 chars. could remove this once everyone has updated
            if(placeCode.indexOf("-") == -1){
                if(placeCode.length() >= 8)
                placeCode = placeCode.substring(0,4) + "-" + placeCode.substring(4,8) + "-" + placeCode.substring(8);
                txtPlaceCode.setText(placeCode);
            }
        }

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //placeCode = Uri.encode(placeCode);

            DownloadWebpageTask dl = new DownloadWebpageTask();
            dl.UrlParams.add("pc");
            dl.UrlParams.add(placeCode);
            dl.UrlParams.add("pw");
            dl.UrlParams.add("");
            dl.UrlParams.add("pwt");
            dl.UrlParams.add("");
            dl.RequestMethod = "POST";
            dl.execute("http://www.showlineup.com/titp/TITP_PlaceCodeSearch");

            //new DownloadWebpageTask().execute("http://www.showlineup.com/titp/TITP_PlaceCodeSearch?pc=" + placeCode + "&pw=1&pwt=1");

        } else {
            Toast.makeText(this, "Check your internet connection and try again.", Toast.LENGTH_LONG).show();
        }
    }

    public void DownloadResult(String result){
        //[{"Success":"1"},{"PlaceId":"79568988-ab5c-47b5-93cb-883fb30be05c","PlaceCode":254917,"PlaceName":"dog\u0027s beach","PlaceDate":20150927,"PlaceAddress":"","PlaceLat":"","PlaceLong":"","PlaceUserId":"acyssfx@gmail.com"}]

        if(result != null && result.length() > 0) {
            Jjson j = new Jjson(result);

            String success = j.getValue("Success");
            if(success.equals("1")) {
                _place = new ThisPlace();
                _place.PlaceId = j.getValue("PlaceId");
                _place.PlaceCode = j.getValue("PlaceCode");
                _place.PlaceName = j.getValue("PlaceName");
                _place.PlaceDate = Integer.parseInt(j.getValue("PlaceDate"));
                _place.PlaceAddress = j.getValue("PlaceAddress");
                _place.PlaceLat = j.getValue("PlaceLat");
                _place.PlaceLong = j.getValue("PlaceLong");
                //_place.PlaceUserId = j.getValue("PlaceUerId");
                _place.CreateDate = Integer.parseInt(j.getValue("CreateDate"));
                _place.ModCount = Integer.parseInt(j.getValue("ModCount"));
                //_place.IsAdmin = Integer.parseInt(j.getValue("IsAdmin"));

                TextView txvPlaceName = (TextView) findViewById(R.id.FP_txvPlaceName);
                txvPlaceName.setText(_place.PlaceName);

                findViewById(R.id.FP_laySave).setVisibility(View.VISIBLE);
                StatusUpdate("Place Found.");
            }
            else{
                StatusUpdate("The Place was not found.");
            }
        }else{
            StatusUpdate("The Place was not found.");
        }
    }

    public void btnSave_Click(View view){
        if(_place == null){
            Toast.makeText(this, "No Place has been found.", Toast.LENGTH_SHORT).show();
        }else {

            DBHelper db = new DBHelper(this);

            int success = db.PlaceSaveOnFind(_place);
            if(success == 1){
                Toast.makeText(this, "Place Saved: " + _place.PlaceName, Toast.LENGTH_LONG).show();
                this.finish();
            }else{
                StatusUpdate("There was a problem saving the new code. Return to the main screen and refresh the list.");
            }

            db.close();



        }
    }

//    public String ParseString(String s){
//        if(s.startsWith("\"")) s = s.substring(1);
//        if (s.endsWith("\"")) s = s.substring(0, s.length()-1);
//        return s.replace("\\u0027", "'");//.replace("\\u002C",",");
//    }
    public void StatusUpdate(String status) {
        //TextView txt = (TextView)findViewById(R.id.FP_txvStatus);
        //txt.setText(status);
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
    }



    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        public ArrayList<String> UrlParams = new ArrayList<String>(); //name then value, repeat
        public String RequestMethod = "GET"; //GET or POST

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            DownloadResult(result);
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
                //int response =
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
