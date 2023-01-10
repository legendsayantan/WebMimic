package com.legendsayantan.autoweb.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.card.MaterialCardView;
import com.legendsayantan.autoweb.R;
import com.legendsayantan.autoweb.interfaces.AutomationData;
import com.legendsayantan.autoweb.workers.ColorParser;

/**
 * @author legendsayantan
 */
public class GridAdapter extends ArrayAdapter<AutomationData> {
    Activity activity;
    public GridAdapter(@androidx.annotation.NonNull Activity activity, int resource, @androidx.annotation.NonNull java.util.List<AutomationData> objects) {
        super(activity, resource, objects);
        this.activity = activity;
    }
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View returnView = inflater.inflate(R.layout.grid_item,  parent, false);
        LinearLayout container = returnView.findViewById(R.id.linearLayout);
        TextView textView = returnView.findViewById(R.id.textView);
        MaterialCardView cardView = returnView.findViewById(R.id.cardView);
        textView.setTextColor(ColorParser.getSecondary(activity));
        cardView.setStrokeWidth(3);
        cardView.setCardBackgroundColor(ColorParser.getPrimary(activity));
        switch (getItem(position).color){
            case -1:
                //set grey
                cardView.setStrokeColor(ColorParser.getSecondary(activity));
                break;
            case 0:
                //set glassy red
                cardView.setStrokeColor(Color.parseColor("#FF0000"));
                break;
            case 1:
                //set glassy green
                cardView.setStrokeColor(Color.parseColor("#00FF00"));
                break;
            case 2:
                //set glassy yellow
                cardView.setStrokeColor(Color.parseColor("#FFFF00"));
                break;
            case 3:
                //set glassy blue
                cardView.setStrokeColor(Color.parseColor("#0000FF"));
                break;

        }
        textView.setText(getItem(position).getName());
        cardView.setAlpha(0);
        cardView.animate().alpha(1).setDuration(500).setStartDelay(position * 100L).start();
        returnView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    cardView.setCardBackgroundColor(cardView.getStrokeColor());
                    textView.setTextColor(ColorParser.getPrimary(activity));
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    cardView.setCardBackgroundColor(ColorParser.getPrimary(activity));
                    textView.setTextColor(ColorParser.getSecondary(activity));
                }
                return returnView.callOnClick();
            }
        });
        returnView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    cardView.setCardBackgroundColor(ColorParser.getPrimary(activity));
                    textView.setTextColor(ColorParser.getSecondary(activity));
                }
            }
        });
        return returnView;
    }

}

