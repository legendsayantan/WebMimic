package com.legendsayantan.autoweb.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legendsayantan.autoweb.R;
import com.legendsayantan.autoweb.adapters.GridAdapter;
import com.legendsayantan.autoweb.interfaces.AutomationData;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<AutomationData> data ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        data = new ArrayList<>();
        GridView gridView = findViewById(R.id.grid);
        try {
            data = getList();
        }catch (Exception e){
            e.printStackTrace();
        }
        data.add(new AutomationData("+\nRecord new", 1));
        gridView.setAdapter(new GridAdapter(this,R.layout.grid_item,data));
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if(position==data.size()-1){
                Intent intent = new Intent(MainActivity.this,TrainerActivity.class);
                startActivity(intent);
            }else{
                Intent intent = new Intent(MainActivity.this,ExecutorActivity.class);
                intent.putExtra("data",position);
                startActivity(intent);
            }
        });
        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            if(position!=data.size()-1){
                data.remove(position);
                data.remove(data.size()-1);
                try {
                    saveList(data);
                    MainActivity.this.onResume();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        super.onResume();
    }

    public ArrayList<AutomationData> getList() throws JsonProcessingException {
        String data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("actions","[]");
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(data, new TypeReference<ArrayList<AutomationData>>(){});
    }
    public void saveList(ArrayList<AutomationData> list) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(list);
        System.out.println(data);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("actions", data).apply();
    }
}