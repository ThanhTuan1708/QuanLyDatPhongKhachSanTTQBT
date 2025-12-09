package entity;

import java.sql.Date;
import java.sql.Time;

public class LichLamViec {
    private String maNV;
    private Date ngayLam;
    private String caLam;
    private Time gioBatDau;
    private Time gioKetThuc;
    private String gioCong;
    private String tangCa;
    private String nhiemVu;
    private String thoiGianNV;
    private String trangThai;
    private String ghiChu;
    private String trangThaiNhiemVu;

    public LichLamViec(String maNV, Date ngayLam, String caLam, Time gioBatDau, Time gioKetThuc,
                       String gioCong, String tangCa, String nhiemVu, String thoiGianNV,
                       String trangThai, String ghiChu, String trangThaiNhiemVu) {
        this.maNV = maNV;
        this.ngayLam = ngayLam;
        this.caLam = caLam;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.gioCong = gioCong;
        this.tangCa = tangCa;
        this.nhiemVu = nhiemVu;
        this.thoiGianNV = thoiGianNV;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.trangThaiNhiemVu = trangThaiNhiemVu;
    }

    public LichLamViec() {}

    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }

    public Date getNgayLam() { return ngayLam; }
    public void setNgayLam(Date ngayLam) { this.ngayLam = ngayLam; }

    public String getCaLam() { return caLam; }
    public void setCaLam(String caLam) { this.caLam = caLam; }

    public Time getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(Time gioBatDau) { this.gioBatDau = gioBatDau; }

    public Time getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(Time gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public String getGioCong() { return gioCong; }
    public void setGioCong(String gioCong) { this.gioCong = gioCong; }

    public String getTangCa() { return tangCa; }
    public void setTangCa(String tangCa) { this.tangCa = tangCa; }

    public String getNhiemVu() { return nhiemVu; }
    public void setNhiemVu(String nhiemVu) { this.nhiemVu = nhiemVu; }

    public String getThoiGianNV() { return thoiGianNV; }
    public void setThoiGianNV(String thoiGianNV) { this.thoiGianNV = thoiGianNV; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTrangThaiNhiemVu() {
        return trangThaiNhiemVu;
    }

    public void setTrangThaiNhiemVu(String trangThaiNhiemVu) {
        this.trangThaiNhiemVu = trangThaiNhiemVu;
    }

    @Override
    public String toString() {
        return "LichLamViec{" +
                "maNV='" + maNV + '\'' +
                ", ngayLam=" + ngayLam +
                ", caLam='" + caLam + '\'' +
                ", gioBatDau=" + gioBatDau +
                ", gioKetThuc=" + gioKetThuc +
                ", gioCong='" + gioCong + '\'' +
                ", tangCa='" + tangCa + '\'' +
                ", nhiemVu='" + nhiemVu + '\'' +
                ", thoiGianNV='" + thoiGianNV + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}