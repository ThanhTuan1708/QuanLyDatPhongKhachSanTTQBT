package entity;

public enum LoaiNhanVien {
    LE_TAN(1, "Lễ tân"),
    QUAN_LY(2, "Quản lý"),
    KHAC(0, "Khác");

    private final int id;
    private final String label;

    LoaiNhanVien(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    // 🔄 Chuyển mã số (int) từ DB thành enum
    public static LoaiNhanVien fromId(int id) {
        for (LoaiNhanVien loai : values()) {
            if (loai.id == id)
                return loai;
        }
        return KHAC; // Mặc định nếu không khớp
    }
}