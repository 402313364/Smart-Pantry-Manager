package com.smartpantry.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartpantry.manager.data.DatabaseHelper;
import com.smartpantry.manager.data.PantryItem;
import com.smartpantry.manager.data.Recipe;
import com.smartpantry.manager.data.RecipeMatcher;

import java.util.List;

public class SuggestionsActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    private DatabaseHelper database;
    private RecipeAdapter readyAdapter;
    private RecipeAdapter almostAdapter;
    private RecipeAdapter allAdapter;
    private TextView emptyView;
    private TextView summaryView;
    private RecyclerView readyList;
    private View almostSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        database = DatabaseHelper.getInstance(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        emptyView = findViewById(R.id.textEmpty);
        summaryView = findViewById(R.id.textSummary);
        readyList = findViewById(R.id.readyList);

        readyAdapter = new RecipeAdapter(this);
        readyList.setLayoutManager(new LinearLayoutManager(this));
        readyList.setAdapter(readyAdapter);

        almostSection = findViewById(R.id.almostSection);
        RecyclerView almostList = findViewById(R.id.almostList);
        almostAdapter = new RecipeAdapter(this);
        almostList.setLayoutManager(new LinearLayoutManager(this));
        almostList.setAdapter(almostAdapter);

        RecyclerView allList = findViewById(R.id.allList);
        allAdapter = new RecipeAdapter(this);
        allList.setLayoutManager(new LinearLayoutManager(this));
        allList.setAdapter(allAdapter);
    }

    // check the pantry again whenever this screen is shown
    @Override
    protected void onResume() {
        super.onResume();
        List<PantryItem> pantry = database.getPantryItems();
        List<Recipe> recipes = database.getRecipes();
        // top list is strict matching only
        List<Recipe> suggested = RecipeMatcher.strictMatches(recipes, pantry);
        List<Recipe> almost = RecipeMatcher.almostThere(recipes, pantry);

        readyAdapter.setItems(suggested, pantry);
        almostAdapter.setItems(almost, pantry);
        allAdapter.setItems(recipes, pantry);

        boolean empty = suggested.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        readyList.setVisibility(empty ? View.GONE : View.VISIBLE);
        summaryView.setVisibility(empty ? View.GONE : View.VISIBLE);
        almostSection.setVisibility(almost.isEmpty() ? View.GONE : View.VISIBLE);
        if (!empty) {
            summaryView.setText(getResources().getQuantityString(
                    R.plurals.recipe_match_count, suggested.size(), suggested.size()));
        }
    }

    @Override
    public void onOpen(Recipe recipe) {
        // pass the recipe id to the detail screen
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
