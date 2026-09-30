package com.smartpantry.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.smartpantry.manager.data.PantryItem;

import java.util.ArrayList;
import java.util.List;

// custom adapter - binds pantry rows from the database to the RecyclerView
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.Holder> {

    public interface Listener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean alertsEnabled;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> next, boolean alertsEnabled) {
        this.alertsEnabled = alertsEnabled;
        items.clear();
        items.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        PantryItem item = items.get(position); // one row from the list
        holder.name.setText(item.getName());
        holder.quantity.setText(item.getQuantityLabel());

        boolean flag = alertsEnabled && item.expiresWithinDays(3);
        holder.card.setCardBackgroundColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                flag ? R.color.pantry_alert : R.color.white));

        if (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) {
            holder.expiry.setVisibility(View.GONE);
        } else {
            holder.expiry.setVisibility(View.VISIBLE);
            int label = flag ? R.string.expiry_soon_label : R.string.expiry_label;
            holder.expiry.setText(holder.itemView.getContext().getString(label, item.getExpiryDate()));
            holder.expiry.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(),
                    flag ? R.color.pantry_danger : R.color.pantry_muted));
        }
        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final ImageButton delete;

        Holder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            name = itemView.findViewById(R.id.textName);
            quantity = itemView.findViewById(R.id.textQuantity);
            expiry = itemView.findViewById(R.id.textExpiry);
            delete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
