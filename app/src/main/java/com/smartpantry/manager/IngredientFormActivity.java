package com.smartpantry.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.smartpantry.manager.data.DatabaseHelper;
import com.smartpantry.manager.data.PantryItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

// pantry id comes in on the intent. -1 means a new item
public class IngredientFormActivity extends AppCompatActivity {

    public static final String EXTRA_PANTRY_ID = "extra_pantry_id";
    private static final String STATE_EXPIRY = "state_expiry";

    private DatabaseHelper database;
    private long pantryId = -1;
    private String expiryIso;

    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private Spinner unitSpinner;
    private TextView expiryValue;
    private TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient);

        database = DatabaseHelper.getInstance(this);
        pantryId = getIntent().getLongExtra(EXTRA_PANTRY_ID, -1);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(pantryId < 0 ? R.string.add_ingredient : R.string.edit_ingredient);
        toolbar.setNavigationOnClickListener(v -> finish());

        nameInput = findViewById(R.id.inputName);
        quantityInput = findViewById(R.id.inputQuantity);
        unitSpinner = findViewById(R.id.spinnerUnit);
        expiryValue = findViewById(R.id.textExpiryValue);
        errorText = findViewById(R.id.textError);

        ArrayAdapter<CharSequence> units = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        units.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(units);

        MaterialButton expiryButton = findViewById(R.id.buttonExpiry);
        MaterialButton clearExpiry = findViewById(R.id.buttonClearExpiry);
        MaterialButton saveButton = findViewById(R.id.buttonSave);

        expiryButton.setOnClickListener(v -> showDatePicker());
        clearExpiry.setOnClickListener(v -> setExpiry(null));
        saveButton.setOnClickListener(v -> save());

        if (savedInstanceState != null) {
            setExpiry(savedInstanceState.getString(STATE_EXPIRY));
        } else if (pantryId >= 0) {
            loadExisting();
        } else {
            setExpiry(null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY, expiryIso);
    }

    private void loadExisting() {
        PantryItem item = database.getPantryItem(pantryId);
        if (item == null) {
            finish();
            return;
        }
        nameInput.setText(item.getName());
        quantityInput.setText(PantryItem.formatQuantity(item.getQuantity()));
        setSpinnerValue(item.getUnit());
        setExpiry(item.getExpiryDate());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.expiry_optional)
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            // the picker returns utc millis, so don't use the phone's timezone or the day can shift
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            String iso = String.format(Locale.US, "%04d-%02d-%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
            setExpiry(iso);
        });
        picker.show(getSupportFragmentManager(), "expiry");
    }

    private void setExpiry(String iso) {
        expiryIso = iso;
        if (iso == null || iso.isEmpty()) {
            expiryValue.setText(R.string.no_expiry);
            return;
        }
        expiryValue.setText(iso);
    }

    private void setSpinnerValue(String unit) {
        for (int i = 0; i < unitSpinner.getCount(); i++) {
            if (unit.equals(unitSpinner.getItemAtPosition(i).toString())) {
                unitSpinner.setSelection(i);
                return;
            }
        }
    }

    private void save() {
        String name = textOf(nameInput);
        String quantityText = textOf(quantityInput);
        if (name.isEmpty()) {
            showError(R.string.error_name);
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException ex) {
            showError(R.string.error_quantity);
            return;
        }
        if (quantity <= 0) {
            showError(R.string.error_quantity);
            return;
        }
        if (expiryIso != null && !expiryIso.isEmpty() && !isIsoDate(expiryIso)) {
            showError(R.string.error_expiry);
            return;
        }

        PantryItem item = new PantryItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit(unitSpinner.getSelectedItem().toString());
        item.setExpiryDate(expiryIso);

        if (pantryId < 0) {
            database.insertItem(item);
        } else {
            item.setId(pantryId);
            database.updateItem(item);
        }
        finish();
    }

    private void showError(int message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private String textOf(TextInputEditText input) {
        if (input.getText() == null) {
            return "";
        }
        return input.getText().toString().trim();
    }

    private boolean isIsoDate(String value) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        try {
            Date parsed = format.parse(value);
            return parsed != null;
        } catch (ParseException ex) {
            return false;
        }
    }
}
