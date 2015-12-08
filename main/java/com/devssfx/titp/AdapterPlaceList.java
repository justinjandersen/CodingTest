package com.devssfx.titp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AdapterPlaceList extends ArrayAdapter<ThisPlace> {

    public AdapterPlaceList(Context context, int resource, List<ThisPlace> dataList) {
        super(context, resource, dataList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ThisPlace place = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.main_place_list, parent, false);

            TextView tvName = (TextView) convertView.findViewById(R.id.M_PL_Name);
            TextView tvHome = (TextView) convertView.findViewById(R.id.M_PL_Code);
            TextView tvLocation = (TextView) convertView.findViewById(R.id.M_PL_Location);

            tvName.setText(place.PlaceName);
            tvHome.setText(place.PlaceCode);
            if (place.IsLocationSet()) {
                tvLocation.setText(R.string.LocationSet);
            }
        }

        return convertView;
    }
}
