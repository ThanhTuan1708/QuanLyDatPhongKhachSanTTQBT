package entity;

import entity.GioiTinh;
import java.time.LocalDate;
import java.util.Date;

public class KhachHang {
    private String maKH;
    private String tenKH;
    private GioiTinh gioiTinh;
    private LocalDate ngaySinh;
    private String CCCD;
    private String soDT;
    private String email;
    private String diaChi;
    private String hangThanhVien;
    private int soLanLuuTru;
    private Date ngayLuuTruGanNhat;
    private double tongChiTieu;
    private double danhGiaTrungBinh;



    public KhachHang() {}

    public KhachHang(String maKH, String tenKH, GioiTinh gioiTinh, LocalDate ngaySinh, String CCCD, String soDT, String email, String diaChi, String hangThanhVien) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.CCCD = CCCD;
        this.soDT = soDT;
        this.email = email;
        this.diaChi = diaChi;
        this.hangThanhVien = hangThanhVien;
    }

    // Getter & Setter
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }

    public GioiTinh getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(GioiTinh gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getCCCD() { return CCCD; }
    public void setCCCD(String CCCD) { this.CCCD = CCCD; }

    public String getSoDT() { return soDT; }
    public void setSoDT(String soDT) { this.soDT = soDT; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHangThanhVien() { return hangThanhVien; }
    public void setHangThanhVien(String hangThanhVien) { this.hangThanhVien =    hangThanhVien; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }


    public int getSoLanLuuTru() { return soLanLuuTru; }
    public void setSoLanLuuTru(int soLanLuuTru) { this.soLanLuuTru = soLanLuuTru; }

    public Date getNgayLuuTruGanNhat() { return ngayLuuTruGanNhat; }
    public void setNgayLuuTruGanNhat(Date val) { this.ngayLuuTruGanNhat = val; }

    public double getTongChiTieu() { return tongChiTieu; }
    public void setTongChiTieu(double tongChiTieu) { this.tongChiTieu = tongChiTieu; }

    public double getDanhGiaTrungBinh() { return danhGiaTrungBinh; }
    public void setDanhGiaTrungBinh(double danhGia) { this.danhGiaTrungBinh = danhGia; }

    @Override
    public String toString() {
        return "KhachHang [maKH=" + maKH + ", tenKH=" + tenKH + ", gioiTinh=" + gioiTinh +
                ", ngaySinh=" + ngaySinh + ", CCCD=" + CCCD + ", soDT=" + soDT +
                ", email=" + email + ", diaChi=" + diaChi + "]";
    }
}
