package entity;

import java.util.Objects;

// Lớp này đã được tạo ở bước trước
public class ChiTietHoaDon_Phong {
    private HoaDon hoaDon;
    private PhieuDatPhong phieuDatPhong;
    private double donGiaLucDat; // Thêm từ sơ đồ
    private double thanhTien;

    // Constructors
    public ChiTietHoaDon_Phong(){} // Cần constructor rỗng cho DAO

    public ChiTietHoaDon_Phong(HoaDon hoaDon, PhieuDatPhong phieuDatPhong, double donGiaLucDat, double thanhTien) {
        super();
        this.hoaDon = hoaDon;
        this.phieuDatPhong = phieuDatPhong;
        this.donGiaLucDat = donGiaLucDat;
        this.thanhTien = thanhTien;
    }

    // Getters and Setters
    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }
    public PhieuDatPhong getPhieuDatPhong() { return phieuDatPhong; }
    public void setPhieuDatPhong(PhieuDatPhong phieuDatPhong) { this.phieuDatPhong = phieuDatPhong; }
    public double getDonGiaLucDat() { return donGiaLucDat; }
    public void setDonGiaLucDat(double donGiaLucDat) { this.donGiaLucDat = donGiaLucDat; }
    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    // equals and hashCode dựa trên khóa chính (maHD + maPhieu)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietHoaDon_Phong that = (ChiTietHoaDon_Phong) o;
        return Objects.equals(hoaDon, that.hoaDon) && Objects.equals(phieuDatPhong, that.phieuDatPhong);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hoaDon, phieuDatPhong);
    }
}