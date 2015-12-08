package com.devssfx.titp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;



import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onResume(){
        super.onResume();


        //refresh list
        DBHelper db = new DBHelper(this);
        ArrayList<ThisPlace> placeList = db.LoadPlaceList();
        db.close();

        AdapterPlaceList adaptPlaceList = new AdapterPlaceList(this, R.layout.main_place_list, placeList);
        ListView lv = (ListView) findViewById(R.id.M_lsvPlace);
        lv.setAdapter(adaptPlaceList);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                view.setSelected(true);

                TextView tvCode = (TextView) view.findViewById(R.id.M_PL_Code);

                Intent intent = new Intent(getBaseContext(), PlaceView.class);
                Bundle bun = new Bundle();
                bun.putString("PlaceCode", tvCode.getText().toString());
                intent.putExtras(bun);
                startActivity(intent);
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                TextView tvCode = (TextView) view.findViewById(R.id.M_PL_Code);
                Intent intent = new Intent(getBaseContext(), CreatePlace.class);
                Bundle bun = new Bundle();
                String pc = tvCode.getText().toString();

                bun.putString("PlaceCode", tvCode.getText().toString());
                intent.putExtras(bun);
                startActivity(intent);

                return false;
            }
        });

    }

    public void btnCreate_Click(View view){

        Intent intent = new Intent(this, CreatePlace.class);
        startActivity(intent);

    }
    public void btnFind_Click(View view){
        Intent intent = new Intent(this, FindPlace.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.MA_menu_about) {
            Intent intent = new Intent(getBaseContext(), About.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
