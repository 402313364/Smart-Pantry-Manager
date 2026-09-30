package com.smartpantry.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpantry.manager.data.DatabaseHelper;
import com.smartpantry.manager.data.PantryItem;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper database;
    private PantryAdapter adapter;
    private TextView emptyView;

    // onCreate only runs once when the screen is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = DatabaseHelper.getInstance(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.pantry_title);
        }

        emptyView = findViewById(R.id.textEmpty);
        RecyclerView list = findViewById(R.id.pantryList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        list.setAdapter(adapter);

        FloatingActionButton addButton = findViewById(R.id.buttonAdd);
        addButton.setOnClickListener(v -> openForm(-1));
    }

    // onResume runs every time we come back to this screen (e.g. after add/edit)
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pantry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_suggestions) {
            startActivity(new Intent(this, SuggestionsActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEdit(PantryItem item) {
        openForm(item.getId());
    }

    @Override
    public void onDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    database.deleteItem(item.getId());
                    loadPantry();
                })
                .show();
    }

    // pass the pantry id to the form. -1 means we are adding a new item
    private void openForm(long pantryId) {
        Intent intent = new Intent(this, IngredientFormActivity.class);
        intent.putExtra(IngredientFormActivity.EXTRA_PANTRY_ID, pantryId);
        startActivity(intent);
    }

    private void loadPantry() {
        List<PantryItem> items = database.getPantryItems();
        boolean alertsOn = new AppSettings(this).expiryAlertsEnabled();
        adapter.setItems(items, alertsOn);
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
