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
    private RecipeAdapter adapter;
    private TextView emptyView;
    private TextView summaryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        database = DatabaseHelper.getInstance(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        emptyView = findViewById(R.id.textEmpty);
        summaryView = findViewById(R.id.textSummary);
        RecyclerView list = findViewById(R.id.recipeList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        list.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<PantryItem> pantry = database.getPantryItems();
        List<Recipe> recipes = database.getRecipes();
        List<Recipe> suggested = RecipeMatcher.strictMatches(recipes, pantry);
        adapter.setItems(suggested);
        boolean empty = suggested.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        summaryView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) {
            summaryView.setText(getResources().getQuantityString(
                    R.plurals.recipe_match_count, suggested.size(), suggested.size()));
        }
    }

    @Override
    public void onOpen(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
