package entity;

import java.sql.Time;

public class NhiemVu {

    private int maNhiemVu;
    private int maLichLam;     // FK tới LichLamViec
    private Time thoiGian;     // 07:30, 10:00,...
    private String trangThai;  // Hoàn thành / Chưa xong / Chờ xử lý
    private String ghiChu;
    private String nhiemVu;

    public NhiemVu() {}

    public NhiemVu(int maNhiemVu, int maLichLam, String nhiemVu,
                   Time thoiGian, String trangThai, String ghiChu) {
        this.maNhiemVu = maNhiemVu;
        this.maLichLam = maLichLam;
        this.thoiGian = thoiGian;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.nhiemVu = nhiemVu;
    }

    public int getMaNhiemVu() { return maNhiemVu; }
    public void setMaNhiemVu(int maNhiemVu) { this.maNhiemVu = maNhiemVu; }

    public int getMaLichLam() { return maLichLam; }
    public void setMaLichLam(int maLichLam) { this.maLichLam = maLichLam; }


    public Time getThoiGian() { return thoiGian; }
    public void setThoiGian(Time thoiGian) { this.thoiGian = thoiGian; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getNhiemVu() { return nhiemVu; }
    public void setNhiemVu(String nhiemVu) { this.nhiemVu = nhiemVu; }
}