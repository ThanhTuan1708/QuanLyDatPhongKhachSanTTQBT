package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class PhieuDatPhong {
    private String maPhieu;
    private LocalDateTime ngayDatPhong;
    private LocalDateTime ngayNhanPhong;
    private LocalDateTime ngayTraPhong;
    private KhachHang khachHang;
    private Phong phong;
    // Thêm trạng thái phiếu đặt phòng nếu cần (hiện tại chưa có trong sơ đồ rõ ràng)
    // private TrangThaiPhieuDat trangThai;

    // Constructors
    public PhieuDatPhong() {
    }

    public PhieuDatPhong(String maPhieu) {
        this.maPhieu = maPhieu;
    }

    public PhieuDatPhong(String maPhieu, LocalDateTime ngayDatPhong, LocalDateTime ngayNhanPhong, LocalDateTime ngayTraPhong, KhachHang khachHang, Phong phong) {
        this.maPhieu = maPhieu;
        this.ngayDatPhong = ngayDatPhong;
        this.ngayNhanPhong = ngayNhanPhong;
        this.ngayTraPhong = ngayTraPhong;
        this.khachHang = khachHang;
        this.phong = phong;
    }

    // Getters and Setters
    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }
    public LocalDateTime getNgayDatPhong() { return ngayDatPhong; }
    public void setNgayDatPhong(LocalDateTime ngayDatPhong) { this.ngayDatPhong = ngayDatPhong; }
    public LocalDateTime getNgayNhanPhong() { return ngayNhanPhong; }
    public void setNgayNhanPhong(LocalDateTime ngayNhanPhong) { this.ngayNhanPhong = ngayNhanPhong; }
    public LocalDateTime getNgayTraPhong() { return ngayTraPhong; }
    public void setNgayTraPhong(LocalDateTime ngayTraPhong) { this.ngayTraPhong = ngayTraPhong; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }

    // equals and hashCode based on maPhieu
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhieuDatPhong that = (PhieuDatPhong) o;
        return Objects.equals(maPhieu, that.maPhieu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhieu);
    }

    @Override
    public String toString() {
        return "PhieuDatPhong{" + "maPhieu='" + maPhieu + '\'' + ", phong=" + phong + ", khachHang=" + khachHang + '}';
    }
}