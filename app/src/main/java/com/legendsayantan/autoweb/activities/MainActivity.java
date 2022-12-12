package com.legendsayantan.autoweb.activities;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

import static com.legendsayantan.autoweb.interfaces.AutomationData.optimise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legendsayantan.autoweb.R;
import com.legendsayantan.autoweb.adapters.GridAdapter;
import com.legendsayantan.autoweb.interfaces.AutomationData;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<AutomationData> data;
    AlertDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onResume() {
        data = new ArrayList<>();
        GridView gridView = findViewById(R.id.grid);
        try {
            data = getList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.add(new AutomationData("+\nRecord new", 4));
        gridView.setAdapter(new GridAdapter(this, R.layout.grid_item, data));
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == data.size() - 1) {
                Intent intent = new Intent(MainActivity.this, TrainerActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, ExecutorActivity.class);
                intent.putExtra("data", position);
                startActivity(intent);
            }
        });
        gridView.setOnItemLongClickListener((parent, view, position, id) -> {

            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20, 20, 20, 20);
            if (position != data.size() - 1) {
                TextView title = new TextView(getApplicationContext());
                title.setText("What to do with " + data.get(position).getName() + "?");
                title.setTextSize(25);
                Button delete = new Button(getApplicationContext());
                delete.setText("Delete");
                delete.getBackground().setColorFilter(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_secondary_variant), PorterDuff.Mode.MULTIPLY);
                delete.setOnClickListener(v -> {
                    data.remove(position);
                    data.remove(data.size() - 1);
                    try {
                        saveList(data);
                    } catch (JsonProcessingException e) {
                        Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                    }
                    onResume();
                    dialog.dismiss();
                });
                Button share = new Button(getApplicationContext());
                share.setText("Copy to clipboard");
                share.getBackground().setColorFilter(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_secondary_variant), PorterDuff.Mode.MULTIPLY);
                share.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = null;
                    try {
                        clip = ClipData.newPlainText("AutoWeb", AutomationData.toJson(data.get(position)));
                    } catch (JsonProcessingException e) {
                        Toast.makeText(getApplicationContext(), "Error while copying", Toast.LENGTH_SHORT).show();
                    }
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                });
                layout.addView(title);
                layout.addView(delete);
                layout.addView(share);
            }
            TextView title2 = new TextView(getApplicationContext());
            title2.setText("App Settings");
            title2.setTextSize(25);
            Button add = new Button(getApplicationContext());
            add.setText("Import new");
            add.getBackground().setColorFilter(ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_secondary_variant), PorterDuff.Mode.MULTIPLY);
            add.setOnClickListener(v -> {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Import new")
                        .setMessage("Import the automation data from clipboard?")
                        .setPositiveButton("Import", (dialog2, which) -> {
                            try {
                                ArrayList<AutomationData> data2 = getList();
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                String pasteData = "";
                                if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                                    pasteData = item.getText().toString();
                                }
                                data2.add(optimise(AutomationData.fromJson(pasteData)));
                                saveList(data2);
                                onResume();
                            } catch (JsonProcessingException e) {
                                Toast.makeText(getApplicationContext(), "invalid data in clipboard", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
            if (Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID).equals("ac3c9a8a7f2a3912")) {
                Button easter_egg = new Button(getApplicationContext());
                easter_egg.setText("Kill yourself");
                easter_egg.setOnClickListener(v -> {
                    Toast.makeText(getApplicationContext(), "Congrats! you have successfully killed yourself, now you don't need this app.", Toast.LENGTH_LONG).show();
                    System.exit(0);
                });
                layout.addView(easter_egg);
            }
            CheckBox checkBox = new CheckBox(getApplicationContext());
            checkBox.setText("Enable Dark Mode");
            checkBox.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_secondary_variant));
            checkBox.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("dark", false));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("dark", isChecked).apply();
            });
            CheckBox checkBox2 = new CheckBox(getApplicationContext());
            checkBox2.setText("Enable Auto Play");
            checkBox2.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_secondary_variant));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkBox2.setTooltipText("Automatically plays the actions when the page is loaded");
            }
            checkBox2.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("auto", false));
            checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("auto", isChecked).apply();
            });
            layout.addView(title2);
            layout.addView(add);
            layout.addView(checkBox);
            layout.addView(checkBox2);
            TextView about = new TextView(getApplicationContext());
            about.setText("Made by @legendsayantan");
            about.setTextSize(15);
            about.setGravity(Gravity.CENTER);
            layout.addView(about);
            dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(layout)
                    .setPositiveButton("Close", null)
                    .show();
            return true;
        });
        super.onResume();
    }

    public ArrayList<AutomationData> getList() throws JsonProcessingException {
        String data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("actions", "[]");
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(data, new TypeReference<ArrayList<AutomationData>>() {
        });
    }

    public void saveList(ArrayList<AutomationData> list) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(list);
        System.out.println(data);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("actions", data).apply();
    }
}