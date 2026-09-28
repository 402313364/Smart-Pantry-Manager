package com.smartpantry.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.manager.data.Recipe;

import java.util.ArrayList;
import java.util.List;

/** Shows recipe names returned by the strict-matching rule. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.Holder> {

    public interface Listener {
        void onOpen(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Recipe> next) {
        recipes.clear();
        recipes.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        int count = recipe.getIngredients().size();
        holder.detail.setText(holder.itemView.getContext().getResources()
                .getQuantityString(R.plurals.ingredient_count, count, count));
        holder.itemView.setOnClickListener(v -> listener.onOpen(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView detail;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textRecipeName);
            detail = itemView.findViewById(R.id.textRecipeDetail);
        }
    }
}
