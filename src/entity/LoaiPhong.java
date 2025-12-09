package entity;

// Enum dựa trên sơ đồ lớp/ERD
public enum LoaiPhong {
    TIEU_CHUAN("Tiêu chuẩn"),
    DELUXE("Deluxe"),
    VIEW_BIEN("View biển"),
    GIA_DINH("Gia đình"),
    TONG_THONG("Tổng thống");

    private final String tenLoai;

    LoaiPhong(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    @Override
    public String toString() {
        return tenLoai;
    }
}