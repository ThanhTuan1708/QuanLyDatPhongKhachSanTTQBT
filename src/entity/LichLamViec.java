package entity;

import java.sql.Date;
import java.sql.Time;

public class LichLamViec {

    private int maLichLam;
    private String maNV;
    private Date ngayLam;
    private String caLam;
    private Time gioBatDau;
    private Time gioKetThuc;
    private String gioCong;
    private String tangCa;
    private String trangThai;

    public LichLamViec() {}

    public LichLamViec(int maLichLam, String maNV, Date ngayLam, String caLam,
                       Time gioBatDau, Time gioKetThuc,
                       String gioCong, String tangCa, String trangThai) {
        this.maLichLam = maLichLam;
        this.maNV = maNV;
        this.ngayLam = ngayLam;
        this.caLam = caLam;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.gioCong = gioCong;
        this.tangCa = tangCa;
        this.trangThai = trangThai;
    }

    public int getMaLichLam() { return maLichLam; }
    public void setMaLichLam(int maLichLam) { this.maLichLam = maLichLam; }

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

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}