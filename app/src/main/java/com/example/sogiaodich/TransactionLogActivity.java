package com.example.sogiaodich;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionLogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private List<Transaction> filteredList;

    private EditText searchInput;
    private Spinner monthFilterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_log);

        recyclerView = findViewById(R.id.transactionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchInput = findViewById(R.id.searchInput);
        monthFilterSpinner = findViewById(R.id.monthFilterSpinner);

        // Tạo dữ liệu giả lập
        transactionList = new ArrayList<>();
        transactionList.add(new Transaction(1, "Ăn sáng", "2025-07-25", 25000, "Ăn uống"));
        transactionList.add(new Transaction(2, "Tiền điện", "2025-06-20", 150000, "Hóa đơn"));
        transactionList.add(new Transaction(3, "Xăng xe", "2025-07-22", 50000, "Đi lại"));
        transactionList.add(new Transaction(4, "Học phí", "2025-05-15", 2000000, "Giáo dục"));

        filteredList = new ArrayList<>(transactionList);
        adapter = new TransactionAdapter(filteredList, this::onTransactionClick);
        recyclerView.setAdapter(adapter);

        setupMonthFilter();
        setupSearch();
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupMonthFilter() {
        List<String> months = Arrays.asList("Tất cả", "01", "02", "03", "04", "05", "06", "07");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthFilterSpinner.setAdapter(spinnerAdapter);

        monthFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterList();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterList() {
        String query = searchInput.getText().toString().toLowerCase();
        String selectedMonth = monthFilterSpinner.getSelectedItem().toString();

        filteredList.clear();
        for (Transaction t : transactionList) {
            boolean matchesQuery = t.getDescription().toLowerCase().contains(query);
            boolean matchesMonth = selectedMonth.equals("Tất cả") || t.getDate().substring(5, 7).equals(selectedMonth);

            if (matchesQuery && matchesMonth) {
                filteredList.add(t);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void onTransactionClick(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Sửa hoặc Xóa giao dịch")
                .setMessage(transaction.getDescription() + "\n" + transaction.getAmount() + " VND")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    transactionList.remove(transaction);
                    filterList();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
