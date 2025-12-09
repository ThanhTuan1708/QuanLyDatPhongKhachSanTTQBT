package entity;

// Enum cho Giới tính (phổ biến, nên thêm vào)
public enum GioiTinh {
    NAM("Nam"),
    NU("Nữ"),
    KHAC("Khác");

    private final String label;

    GioiTinh(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
