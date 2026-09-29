package com.smartpantry.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.manager.data.PantryItem;
import com.smartpantry.manager.data.Recipe;
import com.smartpantry.manager.data.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;

// adapter for recipe cards - shows what you have vs still need
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.Holder> {

    public interface Listener {
        void onOpen(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final List<PantryItem> pantry = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Recipe> next, List<PantryItem> pantryItems) {
        recipes.clear();
        recipes.addAll(next);
        pantry.clear();
        if (pantryItems != null) {
            pantry.addAll(pantryItems);
        }
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

        List<String> have = RecipeMatcher.haveNames(recipe, pantry);
        List<String> need = RecipeMatcher.needNames(recipe, pantry);

        if (need.isEmpty()) {
            holder.ready.setVisibility(View.VISIBLE);
            holder.need.setVisibility(View.GONE);
        } else {
            holder.ready.setVisibility(View.GONE);
            holder.need.setVisibility(View.VISIBLE);
            holder.need.setText(holder.itemView.getContext()
                    .getString(R.string.need_label, RecipeMatcher.joinNames(need)));
        }

        if (have.isEmpty()) {
            holder.have.setVisibility(View.GONE);
        } else {
            holder.have.setVisibility(View.VISIBLE);
            holder.have.setText(holder.itemView.getContext()
                    .getString(R.string.have_label, RecipeMatcher.joinNames(have)));
        }

        holder.itemView.setOnClickListener(v -> listener.onOpen(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView ready;
        final TextView have;
        final TextView need;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textRecipeName);
            ready = itemView.findViewById(R.id.textReady);
            have = itemView.findViewById(R.id.textHave);
            need = itemView.findViewById(R.id.textNeed);
        }
    }
}
