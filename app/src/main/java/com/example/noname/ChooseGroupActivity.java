package com.example.noname; // Đảm bảo đúng package của bạn

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.noname.database.CategoryDAO;
import com.example.noname.models.Category;
import com.example.noname.R; // Đảm bảo import R.class của bạn

import java.util.List;

public class ChooseGroupActivity extends AppCompatActivity {

    private static final String TAG = "ChooseGroupActivity";
    private static final int ADD_CATEGORY_REQUEST_CODE = 200;

    private CategoryDAO categoryDAO;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group_dynamic);

        Log.d(TAG, "onCreate: Activity started.");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "System back button clicked. Cancelling result.");
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Toolbar back button clicked. Cancelling result.");
            setResult(RESULT_CANCELED);
            finish();
        });

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("LOGGED_IN_USER_ID", -1);
        Log.d(TAG, "Retrieved currentUserId: " + currentUserId);

        if (currentUserId == -1) {
            Log.e(TAG, "Current User ID is -1. Cannot load categories.");
            Toast.makeText(this, "Lỗi: Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        categoryDAO = new CategoryDAO(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called. Loading categories.");
        loadCategoriesAndDisplay();
    }

    private void loadCategoriesAndDisplay() {
        if (currentUserId == -1) {
            return;
        }

        categoryDAO.open();
        List<Category> categories = categoryDAO.getAllCategories(currentUserId);
        categoryDAO.close();
        Log.d(TAG, "Found " + categories.size() + " categories for user ID " + currentUserId);

        LinearLayout categoryContainer = findViewById(R.id.category_container);
        if (categoryContainer == null) {
            Log.e(TAG, "Layout container not found with ID R.id.category_container");
            Toast.makeText(this, "Lỗi: Không tìm thấy container danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        categoryContainer.removeAllViews();
        Log.d(TAG, "Removed all existing views from category container.");

        if (categories.isEmpty()) {
            Toast.makeText(this, "Không có danh mục nào được tìm thấy.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Category list is empty.");
        } else {
            for (Category category : categories) {
                Log.d(TAG, "Processing category: " + category.getName() + " (ID: " + category.getId() + ")");
                setupCategoryItem(categoryContainer, category);
            }
        }

        setupAddMoreCategoryItem(categoryContainer);
    }

    private void setupCategoryItem(LinearLayout parentLayout, Category category) {
        LayoutInflater inflater = getLayoutInflater();
        View itemView = inflater.inflate(R.layout.item_category, parentLayout, false);
        Log.d(TAG, "Inflated new view for category: " + category.getName());

        ImageView iconView = itemView.findViewById(R.id.category_icon);
        TextView nameView = itemView.findViewById(R.id.category_name);

        nameView.setText(category.getName());

        int iconResId = getResources().getIdentifier(category.getIconName(), "drawable", getPackageName());
        if (iconResId != 0) {
            iconView.setImageResource(iconResId);
            iconView.setColorFilter(ContextCompat.getColor(this, R.color.primary_green));
            Log.d(TAG, "Set icon for " + category.getName() + " with resource ID: " + iconResId);
        } else {
            Log.w(TAG, "Icon resource not found for name: " + category.getIconName() + ". Using default icon.");
            iconView.setImageResource(R.drawable.ic_other);
            iconView.setColorFilter(ContextCompat.getColor(this, R.color.light_gray_bg));
        }

        itemView.setOnClickListener(v -> {
            Log.d(TAG, "Category item clicked: " + category.getName() + ", ID: " + category.getId());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_category_id", category.getId());
            resultIntent.putExtra("selected_group_name", category.getName());
            resultIntent.putExtra("selected_group_icon", iconResId);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        parentLayout.addView(itemView);
        Log.d(TAG, "Added new category view to container.");
    }

    private void setupAddMoreCategoryItem(LinearLayout parentLayout) {
        LayoutInflater inflater = getLayoutInflater();
        View itemView = inflater.inflate(R.layout.item_category, parentLayout, false);

        ImageView iconView = itemView.findViewById(R.id.category_icon);
        TextView nameView = itemView.findViewById(R.id.category_name);

        nameView.setText("Thêm danh mục mới");
        iconView.setImageResource(R.drawable.ic_add_circle);
        iconView.setColorFilter(ContextCompat.getColor(this, R.color.accent_yellow));

        Log.d(TAG, "Setting up 'Add new category' item.");

        itemView.setOnClickListener(v -> {
            Log.d(TAG, "Add new category button clicked.");
            // TODO: Điều hướng đến màn hình thêm mới danh mục
            Toast.makeText(this, "Chức năng thêm danh mục đang phát triển.", Toast.LENGTH_SHORT).show();
        });

        parentLayout.addView(itemView);
        Log.d(TAG, "Added 'Add new category' view to container.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Received result. RequestCode: " + requestCode + ", ResultCode: " + resultCode);

        if (requestCode == ADD_CATEGORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Returned from AddCategoryActivity. Reloading categories.");
            loadCategoriesAndDisplay();
        }
    }
}