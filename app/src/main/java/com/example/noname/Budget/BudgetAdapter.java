package com.example.noname.Budget;

import android.view.LayoutInflater; // Lớp này được sử dụng để "inflate" (phóng to) một layout XML (tức là chuyển đổi cấu trúc XML thành các đối tượng View trong Java). Điều này là cần thiết để tạo ra các item View cho RecyclerView từ file layout `item_budget.xml`.
import android.view.View;           // Lớp cơ sở cho mọi thành phần giao diện người dùng (UI) trong Android. Mọi thứ bạn thấy trên màn hình (nút, văn bản, hình ảnh) đều là một loại View.
import android.view.ViewGroup;      // Một lớp con của View, được dùng làm container để chứa và tổ chức các View con khác. Trong RecyclerView, `ViewGroup` này thường là chính `RecyclerView` hoặc một phần tử cha của nó.
import android.widget.ImageView;    // Một loại View dùng để hiển thị hình ảnh, thường là biểu tượng hoặc avatar.
import android.widget.ProgressBar;  // Một loại View hiển thị tiến độ của một hoạt động. Ở đây, nó dùng để trực quan hóa tỷ lệ chi tiêu so với ngân sách.
import android.widget.TextView;     // Một loại View cơ bản dùng để hiển thị văn bản không chỉnh sửa được trên màn hình.

import androidx.annotation.NonNull;     // Một annotation (chú thích) từ thư viện AndroidX, dùng để chỉ ra rằng một tham số của phương thức, biến cục bộ hoặc giá trị trả về của phương thức KHÔNG ĐƯỢC là `null`. Điều này giúp phát hiện sớm các lỗi NullPointerException trong quá trình phát triển.
import androidx.recyclerview.widget.RecyclerView; // Lớp cơ sở của thành phần RecyclerView. RecyclerView là một View cực kỳ linh hoạt và hiệu quả để hiển thị các danh sách lớn hoặc lưới dữ liệu có thể cuộn được, bằng cách tái sử dụng các View item.

import com.example.noname.R; // Lớp `R` tự động được tạo ra bởi Android, chứa các ID số nguyên cho tất cả các tài nguyên trong dự án (như layout files, drawable images, string values, v.v.). Ở đây, nó được dùng để truy cập layout `item_budget` và các ID của các View con bên trong nó.

import java.text.NumberFormat; // Một lớp từ thư viện chuẩn của Java, được sử dụng để định dạng số (như tiền tệ, phần trăm) thành chuỗi văn bản theo các quy tắc ngôn ngữ cụ thể.
import java.util.List;         // Một giao diện từ Java Collections Framework, đại diện cho một tập hợp có thứ tự của các phần tử. Nó được dùng để lưu trữ danh sách các đối tượng `Budget`.
import java.util.Locale;       // Đối tượng `Locale` đại diện cho một khu vực địa lý, chính trị hoặc văn hóa cụ thể. Khi định dạng tiền tệ, việc chỉ định `Locale` (ví dụ: `new Locale("vi", "VN")`) đảm bảo rằng số tiền được hiển thị theo quy ước của quốc gia đó (ví dụ: ký hiệu tiền tệ, dấu phân cách).

/**
 * <p><b>BudgetAdapter: Bộ chuyển đổi Dữ liệu cho RecyclerView</b></p>
 *
 * <p>Lớp {@code BudgetAdapter} là một thành phần thiết yếu trong việc sử dụng {@link RecyclerView}
 * để hiển thị danh sách các mục ngân sách. Nó hoạt động như một "bộ chuyển đổi"
 * giữa danh sách dữ liệu các đối tượng {@link Budget} và các View item riêng lẻ
 * được trình bày trên giao diện người dùng.</p>
 *
 * <p><b>Các Trách Nhiệm Chính của BudgetAdapter:</b></p>
 * <ul>
 * <li><b>Tạo ViewHolder:</b> Khi {@link RecyclerView} cần hiển thị một mục mới
 * mà không có View sẵn có để tái sử dụng, Adapter sẽ tạo ra một đối tượng {@link BudgetViewHolder} mới.
 * ViewHolder này đóng vai trò như một "khung chứa" cho các thành phần UI của một item.</li>
 * <li><b>Gắn Dữ liệu (Binding Data):</b> Adapter chịu trách nhiệm lấy dữ liệu từ một đối tượng
 * {@link Budget} cụ thể và "gắn" (bind) dữ liệu đó vào các thành phần UI (ImageView, TextView, ProgressBar)
 * bên trong một {@link BudgetViewHolder}.</li>
 * <li><b>Quản lý Tái sử dụng View:</b> {@link RecyclerView} tối ưu hiệu suất bằng cách tái sử dụng
 * các View không còn hiển thị trên màn hình. Adapter giúp quản lý quá trình này, đảm bảo
 * các View được làm sạch và điền dữ liệu mới một cách hiệu quả.</li>
 * <li><b>Hiển thị Thông tin Ngân sách:</b> Cụ thể, Adapter này hiển thị tên danh mục,
 * biểu tượng tương ứng, tổng số tiền ngân sách, và số tiền còn lại có thể chi tiêu,
 * cùng với một thanh tiến trình để trực quan hóa mức độ chi tiêu.</li>
 * </ul>
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgetList; // Biến này lưu trữ danh sách các đối tượng Budget mà Adapter có trách nhiệm hiển thị. Đây là nguồn dữ liệu chính cho RecyclerView.
    private NumberFormat currencyFormat; // Đối tượng `NumberFormat` này được sử dụng để định dạng các giá trị số (như số tiền) thành một chuỗi tiền tệ dễ đọc và phù hợp với quy ước địa phương (ví dụ: "100.000 đ" thay vì "100000.0").

    /**
     * <p><b>Constructor cho BudgetAdapter</b></p>
     *
     * <p>Khởi tạo một thể hiện mới của {@code BudgetAdapter}.
     * Constructor này nhận danh sách các đối tượng {@link Budget} ban đầu
     * mà Adapter sẽ hiển thị và thiết lập bộ định dạng tiền tệ.</p>
     *
     * @param budgetList Danh sách ban đầu của các đối tượng {@link Budget} mà Adapter sẽ quản lý và hiển thị.
     */
    public BudgetAdapter(List<Budget> budgetList) {
        this.budgetList = budgetList; // Gán danh sách dữ liệu được truyền vào cho biến `budgetList` của Adapter.
        // --- Khởi tạo bộ định dạng tiền tệ ---
        // Lấy một thể hiện của `NumberFormat` chuyên dùng để định dạng tiền tệ.
        // `new Locale("vi", "VN")` chỉ định rằng chúng ta muốn định dạng theo quy ước của tiếng Việt, Việt Nam.
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // Cài đặt để không hiển thị phần thập phân (ví dụ: không hiển thị ",00") vì đồng Việt Nam thường không dùng số lẻ.
        currencyFormat.setMinimumFractionDigits(0); // Số chữ số thập phân tối thiểu là 0.
        currencyFormat.setMaximumFractionDigits(0); // Số chữ số thập phân tối đa cũng là 0.
    }

    /**
     * <p><b>`setBudgetList(List<Budget> newBudgetList)`: Cập nhật Dữ liệu của Adapter</b></p>
     *
     * <p>Phương thức này được sử dụng để cập nhật danh sách các đối tượng {@link Budget}
     * mà Adapter đang hiển thị. Điều này cực kỳ quan trọng khi dữ liệu ngân sách
     * trong ứng dụng thay đổi (ví dụ: sau khi người dùng thêm một ngân sách mới,
     * xóa một ngân sách hiện có, hoặc cập nhật thông tin của một ngân sách).
     * Sau khi cập nhật danh sách, phương thức sẽ gọi {@link #notifyDataSetChanged()}
     * để thông báo cho {@link RecyclerView} biết rằng dữ liệu đã thay đổi và nó cần
     * làm mới giao diện người dùng của mình.</p>
     *
     * <p><b>Lưu ý về Hiệu suất:</b></p>
     * <p>Trong các ứng dụng lớn hoặc danh sách rất dài, việc sử dụng
     * {@link #notifyDataSetChanged()} có thể không hiệu quả nhất vì nó yêu cầu
     * {@link RecyclerView} vẽ lại tất cả các item. Đối với các thay đổi nhỏ hơn
     * (ví dụ: chỉ thêm một item, xóa một item, hoặc cập nhật một item),
     * bạn có thể tối ưu hiệu suất bằng cách sử dụng các phương thức cụ thể hơn
     * như `notifyItemInserted(position)`, `notifyItemRemoved(position)`,
     * `notifyItemChanged(position)`, hoặc sử dụng `DiffUtil` để tính toán các thay đổi
     * một cách thông minh hơn.</p>
     *
     * @param newBudgetList Danh sách mới của các đối tượng {@link Budget} sẽ thay thế
     * danh sách hiện tại trong Adapter.
     */
    public void setBudgetList(List<Budget> newBudgetList) {
        this.budgetList = newBudgetList; // Cập nhật tham chiếu của Adapter tới danh sách dữ liệu mới.
        notifyDataSetChanged(); // Thông báo cho RecyclerView rằng toàn bộ tập dữ liệu đã thay đổi. RecyclerView sẽ phản ứng bằng cách vẽ lại các item.
    }

    /**
     * <p><b>`onCreateViewHolder`: Tạo ViewHolder Mới</b></p>
     *
     * <p>Phương thức callback này được {@link RecyclerView} gọi khi nó cần một {@link BudgetViewHolder}
     * mới để đại diện cho một item. Điều này xảy ra khi không có View item hiện có nào
     * có thể được tái sử dụng từ màn hình (ví dụ: khi một item mới cuộn vào tầm nhìn).</p>
     *
     * <p><b>Chức năng:</b></p>
     * <p>Nhiệm vụ của phương thức này là tạo ra một View mới bằng cách "inflate" (phóng to)
     * layout XML của một item (tức là `item_budget.xml`) thành một đối tượng View trong Java,
     * và sau đó gói View đó vào một thể hiện của {@link BudgetViewHolder}.</p>
     *
     * @param parent   {@link ViewGroup} mà View mới sẽ được thêm vào (thường là chính {@link RecyclerView} đó).
     * Adapter sử dụng context từ `parent` để inflate layout.
     * @param viewType Loại View của View mới. (Hữu ích khi bạn có nhiều loại item View khác nhau trong cùng một danh sách).
     * @return Một thể hiện mới của {@link BudgetViewHolder} mà sẽ giữ các tham chiếu đến các View con cho item đó.
     */
    @NonNull // Annotation này chỉ ra rằng giá trị trả về sẽ không bao giờ là `null`.
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng `LayoutInflater.from(parent.getContext())` để lấy một LayoutInflater.
        // LayoutInflater này biết cách chuyển đổi các file layout XML thành các đối tượng View Java.
        // `inflate(R.layout.item_budget, parent, false)`:
        // - `R.layout.item_budget`: ID của file layout XML định nghĩa giao diện của một item ngân sách.
        // - `parent`: ViewGroup cha (RecyclerView), cần thiết để tạo LayoutParams đúng.
        // - `false`: Tham số `attachToRoot`. Nếu là `true`, View sẽ được thêm ngay lập tức vào `parent`.
        //            Chúng ta đặt là `false` vì RecyclerView sẽ tự thêm View vào đúng vị trí sau đó.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view); // Trả về một thể hiện mới của ViewHolder, truyền View item vừa được inflate vào.
    }

    /**
     * <p><b>`onBindViewHolder`: Gắn Dữ liệu vào ViewHolder</b></p>
     *
     * <p>Phương thức callback này được {@link RecyclerView} gọi để hiển thị dữ liệu
     * tại một vị trí cụ thể trong danh sách. Đây là nơi Adapter chịu trách nhiệm
     * "gắn" (bind) dữ liệu từ tập dữ liệu của mình ({@code budgetList}) vào
     * các thành phần giao diện người dùng (Views) bên trong một {@link BudgetViewHolder}.</p>
     *
     * <p><b>Chức năng:</b></p>
     * <p>Phương thức này sẽ cập nhật nội dung của {@code holder} (một ViewHolder đã có sẵn,
     * có thể là được tái sử dụng) để phản ánh dữ liệu của item tại vị trí đã cho.
     * Quá trình này rất hiệu quả vì nó chỉ cập nhật nội dung của View hiện có
     * thay vì tạo ra View mới hoàn toàn.</p>
     *
     * @param holder   {@link BudgetViewHolder} cần được cập nhật. Đây là ViewHolder
     * mà chúng ta sẽ điền dữ liệu vào các thành phần UI của nó.
     * @param position Vị trí số nguyên của item trong tập dữ liệu của Adapter.
     * Chỉ số này được dùng để truy cập đối tượng {@link Budget} tương ứng từ {@code budgetList}.
     */
    @Override
    public void onBindViewHolder(@NonNull BudgetAdapter.BudgetViewHolder holder, int position) {
        // Lấy đối tượng Budget cụ thể từ danh sách `budgetList` tại vị trí hiện tại.
        // Đối tượng này chứa tất cả dữ liệu mà chúng ta cần hiển thị cho item này.
        Budget budget = budgetList.get(position);
        // Gọi phương thức `bind` tùy chỉnh của `BudgetViewHolder`.
        // Phương thức này chịu trách nhiệm lấy dữ liệu từ đối tượng `budget`
        // và đặt nó vào các thành phần UI (TextView, ImageView, ProgressBar) bên trong `holder`.
        holder.bind(budget);
    }

    /**
     * <p><b>`getItemCount()`: Trả về Tổng Số Lượng Item</b></p>
     *
     * <p>Phương thức này là một phần bắt buộc của bất kỳ lớp Adapter nào kế thừa
     * từ {@link RecyclerView.Adapter}. Nó được {@link RecyclerView} gọi
     * để xác định tổng số lượng item (mục) có trong tập dữ liệu mà Adapter
     * đang quản lý. {@link RecyclerView} sử dụng con số này để biết
     * bao nhiêu item cần hiển thị và để quản lý hiệu quả việc cuộn và tái sử dụng View.</p>
     *
     * @return Một số nguyên đại diện cho tổng số lượng đối tượng {@link Budget}
     * hiện có trong danh sách dữ liệu ({@code budgetList}) của Adapter.
     */
    @Override
    public int getItemCount() {
        return budgetList.size(); // Trả về số lượng phần tử hiện có trong danh sách `budgetList`.
    }

    /**
     * <p><b>BudgetViewHolder: Khung chứa View cho một Item Ngân sách</b></p>
     *
     * <p>Lớp {@code BudgetViewHolder} là một lớp lồng (nested class) bên trong {@link BudgetAdapter}.
     * Nó kế thừa từ {@link RecyclerView.ViewHolder}. Mục đích chính của ViewHolder là
     * **mô tả một khung nhìn item và lưu trữ các tham chiếu đến tất cả các View con**
     * (như ImageView, TextView, ProgressBar) bên trong layout của một item duy nhất
     * (ví dụ: `item_budget.xml`).</p>
     *
     * <p><b>Tầm quan trọng của ViewHolder:</b></p>
     * <p>Việc sử dụng ViewHolder là một mẫu thiết kế cốt lõi của {@link RecyclerView}
     * để tối ưu hiệu suất. Thay vì phải tìm kiếm (sử dụng `findViewById()`) các View
     * con mỗi khi một item cuộn vào tầm nhìn, ViewHolder lưu trữ các tham chiếu này.
     * Khi {@link RecyclerView} tái sử dụng một View item, Adapter chỉ cần cập nhật
     * dữ liệu trên các View đã được tham chiếu trong ViewHolder, thay vì phải inflate
     * lại layout và tìm kiếm View từ đầu. Điều này giúp giảm đáng kể chi phí hiệu suất
     * và làm cho việc cuộn danh sách trở nên mượt mà hơn, đặc biệt với các danh sách dài.</p>
     */
    public class BudgetViewHolder extends RecyclerView.ViewHolder {
        // --- Khai báo các thành phần UI (Views) có trong mỗi item của danh sách ngân sách ---
        ImageView ivBudgetIcon;      // ImageView để hiển thị biểu tượng của danh mục ngân sách.
        TextView tvBudgetCategory;   // TextView để hiển thị tên của danh mục ngân sách (ví dụ: "Ăn uống").
        TextView tvBudgetRemaining;  // TextView để hiển thị số tiền còn lại có thể chi tiêu trong ngân sách này.
        TextView tvBudgetTotal;      // TextView để hiển thị tổng số tiền đã được đặt ngân sách cho danh mục này.
        ProgressBar progressBar;     // ProgressBar để hiển thị trực quan tỷ lệ phần trăm số tiền đã chi so với tổng ngân sách.

        /**
         * <p><b>Constructor cho BudgetViewHolder</b></p>
         *
         * <p>Khởi tạo một thể hiện mới của {@code BudgetViewHolder}.
         * Trong constructor này, chúng ta sẽ tìm kiếm và lưu trữ các tham chiếu
         * đến các View con bên trong layout của một item ngân sách ({@code itemView}).</p>
         *
         * @param itemView View gốc của layout item (`item_budget.xml`). Đây là View cha chứa tất cả các thành phần UI của một mục trong danh sách.
         */
        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView); // Gọi constructor của lớp cha `RecyclerView.ViewHolder`, truyền `itemView` vào.

            // --- Khởi tạo tất cả các phần tử UI bằng cách tìm chúng theo ID trong layout item ---
            // Sử dụng `findViewById()` trên `itemView` để lấy tham chiếu đến các View con.
            // Điều này chỉ được thực hiện một lần khi ViewHolder được tạo, sau đó các tham chiếu được tái sử dụng.
            ivBudgetIcon = itemView.findViewById(R.id.iv_budget_item_icon);
            tvBudgetCategory = itemView.findViewById(R.id.tv_budget_item_category);
            tvBudgetRemaining = itemView.findViewById(R.id.tv_budget_item_remaining);
            tvBudgetTotal = itemView.findViewById(R.id.tv_budget_item_total);
            progressBar = itemView.findViewById(R.id.progress_budget_item);
        }

        /**
         * <p><b>`bind(Budget budget)`: Gắn Dữ liệu vào Các View của ViewHolder</b></p>
         *
         * <p>Phương thức `bind` này là một phương thức tùy chỉnh trong {@code BudgetViewHolder}.
         * Nó chịu trách nhiệm lấy dữ liệu từ một đối tượng {@link Budget} cụ thể
         * và điền dữ liệu đó vào các thành phần giao diện người dùng (Views)
         * mà ViewHolder này đang giữ tham chiếu. Đây là nơi logic hiển thị dữ liệu
         * cụ thể cho một item ngân sách được đặt.</p>
         *
         * @param budget Đối tượng {@link Budget} chứa tất cả dữ liệu mà chúng ta cần
         * hiển thị cho item ngân sách hiện tại.
         */
        public void bind(Budget budget) {
            // 1. Đặt biểu tượng danh mục:
            // Lấy ID tài nguyên drawable của biểu tượng từ đối tượng Budget và đặt nó vào ImageView.
            ivBudgetIcon.setImageResource(budget.getGroupIconResId());

            // 2. Đặt tên danh mục:
            // Lấy tên danh mục từ đối tượng Budget và đặt nó vào TextView.
            tvBudgetCategory.setText(budget.getGroupName());

            // 3. Hiển thị tổng số tiền ngân sách:
            // Lấy số tiền ngân sách từ đối tượng Budget và định dạng nó thành chuỗi tiền tệ (ví dụ: "100.000 đ").
            tvBudgetTotal.setText(currencyFormat.format(budget.getAmount()));

            // 4. TODO: Triển khai logic THỰC TẾ để tính toán `amountSpent` (số tiền đã chi tiêu).
            // --- Đây là một phần quan trọng CẦN được phát triển thêm trong một ứng dụng thực tế. ---
            // Hiện tại, `amountSpent` chỉ là một giá trị PLACEHOLDER giả định.
            // Để tính toán chính xác số tiền đã chi tiêu cho một ngân sách cụ thể, bạn sẽ cần:
            // a) **Truy vấn bảng `transactions`** trong cơ sở dữ liệu.
            // b) **Lọc các giao dịch** theo các tiêu chí sau:
            //    - Giao dịch phải thuộc về cùng một danh mục với ngân sách này (`budget.getCategoryId()`).
            //    - Ngày giao dịch (`transaction_date`) phải nằm trong khoảng thời gian của ngân sách
            //      (tức là giữa `budget.getStartDate()` và `budget.getEndDate()`).
            //    - Chỉ tính các giao dịch có loại là "Expense" (chi tiêu).
            // c) **Tổng hợp (SUM)** cột `amount` của tất cả các giao dịch phù hợp.
            //    Bạn có thể thêm một phương thức vào `TransactionDAO` (hoặc một DAO mới dành cho báo cáo)
            //    để thực hiện truy vấn này.
            double amountSpent = 50000.0; // Đây là giá trị giả định (placeholder), cần thay thế bằng kết quả tính toán thực tế từ dữ liệu giao dịch.

            // 5. Tính toán và hiển thị số tiền còn lại:
            double remainingAmount = budget.getAmount() - amountSpent; // Số tiền còn lại = Tổng ngân sách - Số tiền đã chi.
            // Định dạng và hiển thị số tiền còn lại trong TextView.
            tvBudgetRemaining.setText(String.format("Còn lại %s", currencyFormat.format(remainingAmount)));

            // 6. Cập nhật ProgressBar dựa trên tiến độ chi tiêu:
            // Tính toán phần trăm chi tiêu so với tổng ngân sách.
            // Ép kiểu thành `int` vì `setProgress` nhận giá trị nguyên.
            int progressPercentage = (int) ((amountSpent / budget.getAmount()) * 100);
            // Đảm bảo phần trăm không vượt quá 100% hoặc nhỏ hơn 0%.
            // Nếu chi tiêu vượt quá ngân sách, vẫn hiển thị tối đa 100%.
            if (progressPercentage > 100) progressPercentage = 100;
            // Đảm bảo giá trị không âm.
            if (progressPercentage < 0) progressPercentage = 0;
            progressBar.setProgress(progressPercentage); // Đặt tiến độ cho ProgressBar.
            // Tùy chọn: Bạn có thể thêm logic để thay đổi màu của ProgressBar dựa trên `progressPercentage`
            // để cảnh báo người dùng khi họ sắp vượt ngân sách (ví dụ: màu vàng khi đạt 75%, màu đỏ khi đạt 90%).
        }
    }
}