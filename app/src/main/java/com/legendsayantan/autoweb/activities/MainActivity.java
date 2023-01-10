package com.legendsayantan.autoweb.activities;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

import static com.legendsayantan.autoweb.interfaces.AutomationData.optimise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
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
import com.legendsayantan.autoweb.workers.ColorParser;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ArrayList<AutomationData> data;
    AlertDialog dialog = null;
    GridAdapter gridAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.container).setBackgroundColor(ColorParser.getPrimary(this));
        getWindow().setStatusBarColor(ColorParser.getPrimary(this));
        //set action bar color
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(ColorParser.getDark(this)));
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
        data.add(new AutomationData("+\nRecord new", -1));
        gridAdapter = new GridAdapter(this, R.layout.grid_item, data);
        gridView.setAdapter(gridAdapter);
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
            CardView cardView = new CardView(getApplicationContext());
            cardView.setCardBackgroundColor(ColorParser.getPrimary(MainActivity.this));
            cardView.setRadius(50);
            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            if (position != data.size() - 1) {
                TextView title = new TextView(getApplicationContext());
                title.setText("What to do with " + data.get(position).getName() + "?");
                title.setTextSize(25);
                title.setTextColor(ColorParser.getSecondary(MainActivity.this));
                title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                Button delete = new Button(getApplicationContext());
                delete.setText("Delete");
                delete.getBackground().setColorFilter(ColorParser.getSecondary(MainActivity.this),PorterDuff.Mode.MULTIPLY);
                delete.setTextColor(ColorParser.getPrimary(MainActivity.this));
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
                share.getBackground().setColorFilter(ColorParser.getSecondary(MainActivity.this),PorterDuff.Mode.MULTIPLY);
                share.setTextColor(ColorParser.getPrimary(MainActivity.this));
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
                Button change_color = new Button(getApplicationContext());
                change_color.setText("Change color");
                change_color.getBackground().setColorFilter(ColorParser.getSecondary(MainActivity.this),PorterDuff.Mode.MULTIPLY);
                change_color.setTextColor(ColorParser.getPrimary(MainActivity.this));
                change_color.setOnClickListener(v -> {
                    data.get(position).color = (data.get(position).color + 1) % 4;
                    data.remove(data.size() - 1);
                    try {
                        saveList(data);
                    } catch (JsonProcessingException e) {
                        Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                    }
                    onResume();
                });
                layout.addView(title);
                layout.addView(delete);
                layout.addView(share);
                layout.addView(change_color);
            }
            TextView title2 = new TextView(getApplicationContext());
            title2.setText("App Settings");
            title2.setTextSize(25);
            title2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            title2.setTextColor(ColorParser.getSecondary(MainActivity.this));
            Button add = new Button(getApplicationContext());
            add.setText("Import new");
            add.getBackground().setColorFilter(ColorParser.getSecondary(MainActivity.this),PorterDuff.Mode.MULTIPLY);
            add.setTextColor(ColorParser.getPrimary(MainActivity.this));
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
                easter_egg.getBackground().setColorFilter(ColorParser.getSecondary(MainActivity.this),PorterDuff.Mode.MULTIPLY);
                easter_egg.setTextColor(ColorParser.getPrimary(MainActivity.this));
                easter_egg.setOnClickListener(v -> {
                    Toast.makeText(getApplicationContext(), "Congrats! you have successfully killed yourself, now you don't need this app.", Toast.LENGTH_LONG).show();
                    System.exit(0);
                });
                layout.addView(easter_egg);
            }
            CheckBox checkBox = new CheckBox(getApplicationContext());
            checkBox.setText("Enable Dark Mode on websites?");
            checkBox.setTextColor(ColorParser.getSecondary(MainActivity.this));
            checkBox.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("dark", false));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("dark", isChecked).apply();
            });
            CheckBox checkBox2 = new CheckBox(getApplicationContext());
            checkBox2.setText("Enable Auto Play? (long press for info)");
            checkBox2.setTextColor(ColorParser.getSecondary(MainActivity.this));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkBox2.setTooltipText("Automatically plays the actions when the page is loaded");
            }
            checkBox2.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("auto", false));
            checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("auto", isChecked).apply();
            });
            CheckBox checkBox3 = new CheckBox(getApplicationContext());
            checkBox3.setText("Use fallback algorithm? (long press for info)");
            checkBox3.setTextColor(ColorParser.getSecondary(MainActivity.this));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkBox3.setTooltipText("Switch to the old training algorithm if the updated one performs worse.");
            }
            checkBox3.setChecked(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("fallback", false));
            checkBox3.setOnCheckedChangeListener((buttonView, isChecked) -> {
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("fallback", isChecked).apply();
            });
            layout.addView(title2);
            layout.addView(add);
            layout.addView(checkBox);
            layout.addView(checkBox2);
            layout.addView(checkBox3);
            TextView about = new TextView(getApplicationContext());
            about.setText("Made by @legendsayantan");
            about.setTextSize(15);
            about.setPadding(0,50,0,0);
            about.setTextColor(ColorParser.getSecondary(MainActivity.this));
            about.setGravity(Gravity.CENTER);
            layout.addView(about);
            cardView.addView(layout);
            dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(cardView)
                    .create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setOnDismissListener(dialog -> gridAdapter.notifyDataSetChanged());
            dialog.show();
            return true;
        });
        super.onResume();
    }

    public ArrayList<AutomationData> getList() throws JsonProcessingException {
        String data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("actions", "[]");
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