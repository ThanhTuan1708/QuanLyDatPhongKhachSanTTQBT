package entity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;

public class KhuyenMai {
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private double chietKhau;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private int luotSuDung;  // ✅ thêm thuộc tính mới
    private String trangThai;

    // ===== Constructor đầy đủ dùng trong DAO =====
    public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, double chietKhau,
                     Date ngayBatDau, Date ngayKetThuc, int luotSuDung) {
        this.maKhuyenMai = maKhuyenMai;
        this.tenKhuyenMai = tenKhuyenMai;
        this.chietKhau = chietKhau;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.luotSuDung = luotSuDung;
        this.trangThai = tinhTrangThai();
    }

    // ===== Constructor mặc định =====
    public KhuyenMai() {}

    // ===== Getter/Setter =====
    public String getMaKhuyenMai() { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai) { this.maKhuyenMai = maKhuyenMai; }

    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public void setTenKhuyenMai(String tenKhuyenMai) { this.tenKhuyenMai = tenKhuyenMai; }

    public double getChietKhau() { return chietKhau; }
    public void setChietKhau(double chietKhau) { this.chietKhau = chietKhau; }

    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public int getLuotSuDung() { return luotSuDung; }
    public void setLuotSuDung(int luotSuDung) { this.luotSuDung = luotSuDung; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    // ===== Hàm tính trạng thái tự động =====
    public String tinhTrangThai() {
        long now = System.currentTimeMillis();
        if (ngayBatDau != null && ngayKetThuc != null) {
            if (now >= ngayBatDau.getTime() && now <= ngayKetThuc.getTime()) {
                return "Đang hoạt động";
            } else if (now < ngayBatDau.getTime()) {
                return "Chưa bắt đầu";
            } else {
                return "Đã hết hạn";
            }
        }
        return "Không xác định";
    }

    @Override
    public String toString() {
        return String.format("KhuyenMai[%s - %s - %.2f%% - Lượt: %d - %s]",
                maKhuyenMai, tenKhuyenMai, chietKhau, luotSuDung, trangThai);
    }
}