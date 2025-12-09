package entity;

import java.util.Objects;

// Lớp này giờ chỉ dùng như một Entity đơn giản
// thay vì Enum vì có thể có nhiều trạng thái hơn sau này
public class TrangThaiPhongEntity { // Đổi tên để tránh trùng Enum
    private int maTrangThai;
    private String tenTrangThai;

    // Constructors
    public TrangThaiPhongEntity() {}

    public TrangThaiPhongEntity(int maTrangThai, String tenTrangThai) {
        this.maTrangThai = maTrangThai;
        this.tenTrangThai = tenTrangThai;
    }

    // Getters and Setters
    public int getMaTrangThai() { return maTrangThai; }
    public void setMaTrangThai(int maTrangThai) { this.maTrangThai = maTrangThai; }
    public String getTenTrangThai() { return tenTrangThai; }
    public void setTenTrangThai(String tenTrangThai) { this.tenTrangThai = tenTrangThai; }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrangThaiPhongEntity that = (TrangThaiPhongEntity) o;
        return maTrangThai == that.maTrangThai;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maTrangThai);
    }

    @Override
    public String toString() { return tenTrangThai; }

}