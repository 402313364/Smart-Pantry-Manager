package com.smartpantry.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.smartpantry.manager.data.DatabaseHelper;
import com.smartpantry.manager.data.PantryItem;
import com.smartpantry.manager.data.Recipe;
import com.smartpantry.manager.data.RecipeIngredient;
import com.smartpantry.manager.data.RecipeMatcher;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private DatabaseHelper database;
    private Recipe recipe;
    private MaterialButton cookButton;
    private TextView cookHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        database = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // recipe id comes from SuggestionsActivity
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        recipe = database.getRecipe(recipeId);
        if (recipe == null) {
            finish();
            return;
        }

        toolbar.setTitle(recipe.getName());
        TextView ingredients = findViewById(R.id.textIngredients);
        TextView steps = findViewById(R.id.textSteps);
        ingredients.setText(formatIngredients(recipe));
        steps.setText(recipe.getSteps());

        cookButton = findViewById(R.id.buttonCook);
        cookHelp = findViewById(R.id.textCookHelp);
        cookButton.setOnClickListener(v -> confirmCook());
        refreshCookButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recipe != null) {
            refreshCookButton();
        }
    }

    private void refreshCookButton() {
        List<PantryItem> pantry = database.getPantryItems();
        boolean ready = RecipeMatcher.canCook(recipe, pantry);
        cookButton.setEnabled(ready);
        cookHelp.setVisibility(ready ? View.GONE : View.VISIBLE);
    }

    private void confirmCook() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cook_confirm_title)
                .setMessage(R.string.cook_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.cook_this, (dialog, which) -> cookNow())
                .show();
    }

    private void cookNow() {
        if (!database.cookRecipe(recipe)) {
            Toast.makeText(this, R.string.cook_not_enough, Toast.LENGTH_LONG).show();
            refreshCookButton();
            return;
        }
        Toast.makeText(this, R.string.cook_done, Toast.LENGTH_LONG).show();
        finish();
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
