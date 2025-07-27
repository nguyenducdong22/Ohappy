package com.example.noname.allwallets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.noname.R;
import com.google.android.material.tabs.TabLayout;

public class TransactionsFragment extends Fragment {

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập cho Tab "THÁNG NÀY" được chọn mặc định
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_transactions);
        tabLayout.getTabAt(1).select(); // Chọn tab thứ 2 (index 1)
    }
}