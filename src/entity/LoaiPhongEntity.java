package entity;

import java.util.Objects;

// Lớp này giờ chỉ dùng như một Entity đơn giản
// thay vì Enum vì có thể có nhiều loại hơn sau này
public class LoaiPhongEntity { // Đổi tên để tránh trùng Enum
    private int maLoaiPhong;
    private String tenLoaiPhong;

    // Constructors
    public LoaiPhongEntity() {}

    public LoaiPhongEntity(int maLoaiPhong, String tenLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoaiPhong = tenLoaiPhong;
    }

    // Getters and Setters
    public int getMaLoaiPhong() { return maLoaiPhong;}
    public void setMaLoaiPhong(int maLoaiPhong) { this.maLoaiPhong = maLoaiPhong; }
    public String getTenLoaiPhong() { return tenLoaiPhong; }
    public void setTenLoaiPhong(String tenLoaiPhong) { this.tenLoaiPhong = tenLoaiPhong; }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoaiPhongEntity that = (LoaiPhongEntity) o;
        return maLoaiPhong == that.maLoaiPhong;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maLoaiPhong);
    }

    @Override
    public String toString() { return tenLoaiPhong; }
}