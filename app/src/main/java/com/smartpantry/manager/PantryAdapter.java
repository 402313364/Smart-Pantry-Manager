package com.smartpantry.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.manager.data.PantryItem;

import java.util.ArrayList;
import java.util.List;

/** Binds pantry rows from the database into the list on the main screen. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.Holder> {

    public interface Listener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<PantryItem> next) {
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
        PantryItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.quantity.setText(item.getQuantityLabel());
        if (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) {
            holder.expiry.setVisibility(View.GONE);
        } else {
            holder.expiry.setVisibility(View.VISIBLE);
            holder.expiry.setText(holder.itemView.getContext().getString(R.string.expiry_label, item.getExpiryDate()));
        }
        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final ImageButton delete;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            quantity = itemView.findViewById(R.id.textQuantity);
            expiry = itemView.findViewById(R.id.textExpiry);
            delete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
