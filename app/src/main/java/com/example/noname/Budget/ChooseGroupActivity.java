package com.example.noname.Budget; // Đảm bảo package này đúng

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.noname.R;

public class ChooseGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group); // This links to your XML layout

        // Handle the back button in the top bar
        ImageView btnBack = findViewById(R.id.btn_back_choose_group);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED); // Set result as canceled if user goes back
                finish(); // Close this activity
            }
        });

        // Handle "New Group" click
        LinearLayout layoutNewGroup = findViewById(R.id.layout_new_group);
        layoutNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement logic to create a new group.
                // After creating, you might want to send the new group's info back
                // using sendResultAndFinish() or navigate to a new screen.
                Toast.makeText(ChooseGroupActivity.this, "Tạo nhóm mới (Chưa triển khai)", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle "Ăn uống" (Food) category click
        LinearLayout categoryAnUong = findViewById(R.id.category_an_uong);
        categoryAnUong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Ăn uống", R.drawable.ic_food_circle);
            }
        });

        // Handle "Hoá đơn & Tiện ích" (Bills & Utilities) category click
        LinearLayout categoryHoaDonTienIch = findViewById(R.id.category_hoa_don_tien_ich);
        categoryHoaDonTienIch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Hoá đơn & Tiện ích", R.drawable.ic_bill);
            }
        });

        // TODO: Repeat for all other category LinearLayouts you have in your XML
        // Example for "Thuê nhà":
        LinearLayout categoryThueNha = findViewById(R.id.category_thue_nha);
        categoryThueNha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Thuê nhà", R.drawable.ic_bill);
            }
        });

        LinearLayout categoryHoaDonNuoc = findViewById(R.id.category_hoa_don_nuoc);
        categoryHoaDonNuoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Hoá đơn nước", R.drawable.ic_bill);
            }
        });

        LinearLayout categoryHoaDonDienThoai = findViewById(R.id.category_hoa_don_dien_thoai);
        categoryHoaDonDienThoai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Hoá đơn điện thoại", R.drawable.ic_bill);
            }
        });

        LinearLayout categoryHoaDonDien = findViewById(R.id.category_hoa_don_dien);
        categoryHoaDonDien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Hoá đơn điện", R.drawable.ic_bill);
            }
        });

        LinearLayout categoryHoaDonGas = findViewById(R.id.category_hoa_don_gas);
        categoryHoaDonGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Hoá đơn gas", R.drawable.ic_bill);
            }
        });

        LinearLayout categoryHoaDonTV = findViewById(R.id.category_hoa_don_tv);
        categoryHoaDonTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Hoá đơn TV", R.drawable.ic_bill);
            }
        });

        LinearLayout categoryHoaDonInternet = findViewById(R.id.category_hoa_don_internet);
        categoryHoaDonInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Hoá đơn internet", R.drawable.ic_bill);
            }
        });

        LinearLayout categoryHoaDonTienIchKhac = findViewById(R.id.category_hoa_don_tien_ich_khac);
        categoryHoaDonTienIchKhac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Hoá đơn tiện ích khác", R.drawable.ic_bill);
            }
        });

        LinearLayout categoryMuaSam = findViewById(R.id.category_mua_sam);
        categoryMuaSam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultAndFinish("Mua sắm", R.drawable.ic_shopping_basket);
            }
        });


        // You can also handle clicks for tab_khoan_chi and tab_vay_no if they trigger different group lists
        // TextView tabKhoanChi = findViewById(R.id.tab_khoan_chi);
        // tabKhoanChi.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         // Logic to show "Khoản chi" groups
        //     }
        // });
        // TextView tabVayNo = findViewById(R.id.tab_vay_no);
        // tabVayNo.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         // Logic to show "Vay/Nợ" groups
        //     }
        // });
    }

    /**
     * Helper method to send the selected group data back to the calling activity
     * and finish this activity.
     * @param groupName The name of the selected group.
     * @param iconResId The resource ID of the icon for the selected group.
     */
    private void sendResultAndFinish(String groupName, int iconResId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_group_name", groupName);
        resultIntent.putExtra("selected_group_icon_res_id", iconResId);
        setResult(Activity.RESULT_OK, resultIntent); // Set the result as OK with the data
        finish(); // Close this activity
    }

    // Override onBackPressed to ensure RESULT_CANCELED is sent if user uses system back button
    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED); // Inform the calling activity that selection was canceled
        super.onBackPressed(); // Call the default back button behavior
    }
}