package com.smartpantry.manager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartpantry.manager.data.DatabaseHelper;
import com.smartpantry.manager.data.Recipe;
import com.smartpantry.manager.data.RecipeIngredient;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // recipe id comes from SuggestionsActivity
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = DatabaseHelper.getInstance(this).getRecipe(recipeId);
        if (recipe == null) {
            finish();
            return;
        }

        toolbar.setTitle(recipe.getName());
        TextView ingredients = findViewById(R.id.textIngredients);
        TextView steps = findViewById(R.id.textSteps);
        ingredients.setText(formatIngredients(recipe));
        steps.setText(recipe.getSteps());
    }

    private String formatIngredients(Recipe recipe) {
        StringBuilder builder = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append("• ").append(ingredient.getLabel());
        }
        return builder.toString();
    }
}
