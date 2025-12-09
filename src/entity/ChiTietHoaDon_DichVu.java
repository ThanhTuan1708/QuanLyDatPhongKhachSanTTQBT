package entity;

import java.util.Objects;

// Lớp này đã được tạo ở bước trước
public class ChiTietHoaDon_DichVu {
    private HoaDon hoaDon;
    private DichVu dichVu;
    private int soLuong;
    private double donGia; // Đơn giá tại thời điểm đặt (từ sơ đồ quan hệ là thanhTien, sửa lại)
    private double thanhTien;

    // Constructors
    public ChiTietHoaDon_DichVu() {} // Cần constructor rỗng cho DAO

    public ChiTietHoaDon_DichVu(HoaDon hoaDon, DichVu dichVu, int soLuong, double donGia, double thanhTien) {
        super();
        this.hoaDon = hoaDon;
        this.dichVu = dichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    // Getters and Setters
    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }
    public DichVu getDichVu() { return dichVu; }
    public void setDichVu(DichVu dichVu) { this.dichVu = dichVu; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    // equals and hashCode dựa trên khóa chính (maHD + maDV)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietHoaDon_DichVu that = (ChiTietHoaDon_DichVu) o;
        return Objects.equals(hoaDon, that.hoaDon) && Objects.equals(dichVu, that.dichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hoaDon, dichVu);
    }
}