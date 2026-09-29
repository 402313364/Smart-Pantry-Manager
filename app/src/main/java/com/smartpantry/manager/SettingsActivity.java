package com.smartpantry.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    private AppSettings settings;
    private boolean spinnerReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = new AppSettings(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationOnClickListener(v -> finish());

        SwitchCompat alertsSwitch = findViewById(R.id.switchExpiryAlerts);
        TextView alertsHelp = findViewById(R.id.textAlertsHelp);
        Spinner unitSpinner = findViewById(R.id.spinnerDefaultUnit);

        alertsSwitch.setChecked(settings.expiryAlertsEnabled());
        alertsHelp.setVisibility(alertsSwitch.isChecked() ? View.VISIBLE : View.GONE);
        alertsSwitch.setOnCheckedChangeListener((button, checked) -> {
            settings.setExpiryAlertsEnabled(checked);
            alertsHelp.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        ArrayAdapter<CharSequence> units = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        units.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(units);
        selectUnit(unitSpinner, settings.getDefaultUnit());

        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // this listener also runs when the spinner is first set up, skip that one
                if (!spinnerReady) {
                    spinnerReady = true;
                    return;
                }
                settings.setDefaultUnit(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void selectUnit(Spinner spinner, String unit) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (unit.equals(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }
}
