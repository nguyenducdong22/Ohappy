package com.example.sogiaodich;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    public interface OnItemClickListener {
        void onClick(Transaction transaction);
    }

    private List<Transaction> transactionList;
    private OnItemClickListener listener;

    public TransactionAdapter(List<Transaction> list, OnItemClickListener listener) {
        this.transactionList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction t = transactionList.get(position);
        holder.txtDescription.setText(t.getDescription());
        holder.txtDate.setText(t.getDate());
        holder.txtAmount.setText(String.format("%.0f VND", t.getAmount()));
        holder.txtCategory.setText(t.getCategory());

        holder.itemView.setOnClickListener(v -> listener.onClick(t));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView txtDescription, txtDate, txtAmount, txtCategory;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtCategory = itemView.findViewById(R.id.txtCategory);
        }
    }
}
