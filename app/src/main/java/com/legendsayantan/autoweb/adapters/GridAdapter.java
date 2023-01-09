package com.legendsayantan.autoweb.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.legendsayantan.autoweb.R;
import com.legendsayantan.autoweb.interfaces.AutomationData;

/**
 * @author legendsayantan
 */
public class GridAdapter extends ArrayAdapter<AutomationData> {
    Activity activity;
    public GridAdapter(@androidx.annotation.NonNull Activity activity, int resource, @androidx.annotation.NonNull java.util.List<AutomationData> objects) {
        super(activity, resource, objects);
        this.activity = activity;
    }
    @SuppressLint("MissingInflatedId")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View returnView = inflater.inflate(R.layout.grid_item,  parent, false);
        TextView textView = returnView.findViewById(R.id.textView);
        CardView cardView = returnView.findViewById(R.id.cardView);
        switch (getItem(position).color){
            case -1:
                //set grey
                cardView.setCardBackgroundColor(Color.parseColor("#808080"));
                break;
            case 0:
                //set dark red
                cardView.setCardBackgroundColor(Color.parseColor("#8B0000"));
                break;
            case 1:
                //set dark green
                cardView.setCardBackgroundColor(Color.parseColor("#006400"));
                break;
            case 2:
                //set dark yellow
                cardView.setCardBackgroundColor(Color.parseColor("#8B8000"));
                break;
            case 3:
                //set darker blue
                cardView.setCardBackgroundColor(Color.parseColor("#000070"));
                break;

        }
        textView.setText(getItem(position).getName());
        return returnView;
    }

}

