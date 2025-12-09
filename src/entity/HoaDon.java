package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HoaDon {
    private String maHoaDon;
    private LocalDateTime ngayLap;
    private double vat;
    private String hinhThucThanhToan; // Có trong sơ đồ lớp/ERD
    private double tongTien; // Có thể tính toán được, nhưng lưu lại cho tiện
    private KhachHang khachHang;
    private NhanVien nhanVien; // Thêm từ lần sửa code trước
    private KhuyenMai khuyenMai; // Có thể null

    // Quan hệ 1-Nhiều
    private List<ChiTietHoaDon_Phong> dsChiTietPhong;
    private List<ChiTietHoaDon_DichVu> dsChiTietDichVu;

    // Constructors
    public HoaDon() {
        dsChiTietPhong = new ArrayList<>();
        dsChiTietDichVu = new ArrayList<>();
    }

    public HoaDon(String maHoaDon) {
        this(); // Gọi constructor mặc định để khởi tạo List
        this.maHoaDon = maHoaDon;
    }

    // Constructor đầy đủ (có thể lược bớt List)
    public HoaDon(String maHoaDon, LocalDateTime ngayLap, double vat, String hinhThucThanhToan, double tongTien, KhachHang khachHang, NhanVien nhanVien, KhuyenMai khuyenMai) {
        this(); // Gọi constructor mặc định
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.vat = vat;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.tongTien = tongTien;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.khuyenMai = khuyenMai;
    }

    // Getters and Setters
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
    public double getVat() { return vat; }
    public void setVat(double vat) { this.vat = vat; }
    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String hinhThucThanhToan) { this.hinhThucThanhToan = hinhThucThanhToan; }
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }
    public List<ChiTietHoaDon_Phong> getDsChiTietPhong() { return dsChiTietPhong; }
    public void setDsChiTietPhong(List<ChiTietHoaDon_Phong> dsChiTietPhong) { this.dsChiTietPhong = dsChiTietPhong; }
    public List<ChiTietHoaDon_DichVu> getDsChiTietDichVu() { return dsChiTietDichVu; }
    public void setDsChiTietDichVu(List<ChiTietHoaDon_DichVu> dsChiTietDichVu) { this.dsChiTietDichVu = dsChiTietDichVu; }

    // equals and hashCode based on maHoaDon
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoaDon hoaDon = (HoaDon) o;
        return Objects.equals(maHoaDon, hoaDon.maHoaDon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maHoaDon);
    }

    @Override
    public String toString() {
        return "HoaDon{" + "maHoaDon='" + maHoaDon + '\'' + ", khachHang=" + khachHang + ", ngayLap=" + ngayLap + '}';
    }
}