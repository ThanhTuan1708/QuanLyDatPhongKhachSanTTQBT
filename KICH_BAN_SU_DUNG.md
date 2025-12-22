# KỊCH BẢN SỬ DỤNG ỨNG DỤNG QUẢN LÝ ĐẶT PHÒNG KHÁCH SẠN

## 1. Tổng quan ứng dụng

Ứng dụng Quản lý Đặt phòng Khách sạn hỗ trợ 2 loại người dùng:
- **Nhân viên Lễ tân**: Thực hiện các nghiệp vụ hàng ngày như đặt phòng trước vặt trực tiếp, check-in/check-out, quản lý dịch vụ
- **Nhân viên Quản lý**: Quản lý tổng thể khách sạn, xem thống kê, quản lý nhân viên

---

## 2. Đăng nhập hệ thống

### Mô tả
Người dùng đăng nhập vào hệ thống với tài khoản được cấp

### Các bước thực hiện
1. Khởi động ứng dụng
2. Tại màn hình đăng nhập, nhập **Tên đăng nhập** 
3. Nhập **Mật khẩu**
4. Nhấn nút **Đăng nhập**

### Kết quả mong đợi
- Nếu thông tin hợp lệ: Chuyển đến giao diện phù hợp với vai trò (Lễ tân hoặc Quản lý)
- Nếu thông tin không hợp lệ: Hiển thị thông báo lỗi

---Xem dashboard rồi giới thiệu từng chức năng

## 3. Đặt phòng mới (Nhân viên Lễ tân)

### Mô tả
Nhân viên lễ tân tạo phiếu đặt phòng cho khách hàng

### Điều kiện tiên quyết
- Đăng nhập thành công với tài khoản Lễ tân

### Các bước thực hiện
1. Từ menu bên trái, chọn **"Đặt phòng"**
2. Nhập thông tin tìm kiếm hoặc lọc phòng trống theo:
   - Ngày nhận phòng
   - Ngày trả phòng
   - Loại phòng
3. Chọn phòng trống phù hợp từ danh sách
4. Nhập thông tin khách hàng:
   - Họ tên
   - Số điện thoại
   - Email 
   - Chọn dịch vụ
   - Chọn mã khuyến mãi
5. Xác nhận thông tin đặt phòng
6. Nhấn nút **Xác nhận đặt phòng**

### Kết quả mong đợi
- Phiếu đặt phòng được tạo thành công
- Trạng thái phòng chuyển sang "Đã đặt"
- Thông tin khách hàng được lưu vào hệ thống

---
## 4. Check-in cho khách (Nhân viên Lễ tân)

### Mô tả
Xử lý check-in khi khách đến nhận phòng

### Điều kiện tiên quyết
- Khách hàng đã có phiếu đặt phòng hoặc đặt phòng trực tiếp

### Các bước thực hiện
1. Từ menu bên trái, chọn **"Check-in/Check-out"**
2. Tìm kiếm phiếu đặt phòng theo:
   - Số điện thoại
   - Tên khách hàng
3. Chọn phiếu đặt phòng cần check-in
4. Xác minh thông tin khách hàng
5. Nhấn nút **Check-in**

### Kết quả mong đợi
- Trạng thái phòng chuyển sang "Đang sử dụng"
- Ghi nhận thời gian check-in
- Thông tin được cập nhật vào hệ thống

---

## 6. Check-out và thanh toán (Nhân viên Lễ tân)

### Mô tả
Xử lý check-out và lập hóa đơn thanh toán

### Điều kiện tiên quyết
- Khách đang lưu trú trong phòng

### Các bước thực hiện
1. Từ menu bên trái, chọn **"Check-in/Check-out"**
2. Tìm phòng cần check-out
3. Kiểm tra danh sách dịch vụ đã sử dụng
4. Áp dụng khuyến mãi (nếu có)
5. Xác nhận tổng tiền thanh toán:
   - Tiền phòng
   - Tiền dịch vụ
   - Giảm giá (nếu có)
   - Tổng cộng
6. Chọn phương thức thanh toán
7. Nhấn **Thanh toán & Check-out**
8. In hóa đơn (nếu cần)

### Kết quả mong đợi
- Hóa đơn được tạo và lưu vào hệ thống
- Trạng thái phòng chuyển về "Trống" (sau khi dọn phòng)
- In biên lai cho khách

---
## 5. Đặt phòng trực tiếp cho cho khách (Giống dặt truoc nhung ko can check in )

--- Đăng xuất

## 8. Xem Dashboard và Thống kê (Nhân viên Quản lý)

### Mô tả
Xem tổng quan hoạt động khách sạn và các báo cáo thống kê

### Điều kiện tiên quyết
- Đăng nhập với tài khoản Quản lý

### Các bước thực hiện

#### Xem Dashboard:
1. Sau khi đăng nhập, Dashboard tự động hiển thị
2. Xem các thông tin:
   - Tổng doanh thu
   - Số lượng đặt phòng hôm nay
   - Tỷ lệ lấp đầy phòng
   - ADR (Giá bình quân/phòng)
   - Biểu đồ xu hướng doanh thu
   - Phân bổ loại phòng

#### Xem Thống kê chi tiết:
1. Chọn **"Thống kê"** từ menu
2. Lọc theo khoảng thời gian
3. Xem các báo cáo:
   - Doanh thu theo tháng
   - Số lượng đặt phòng
   - Dịch vụ phổ biến
   - Khách hàng thường xuyên

---


