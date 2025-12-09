package entity;

import java.time.LocalDate;

public class NhanVien {
    private String maNV;
    private String tenNV;
    private String soDT;
    private String email;
    private String diaChi;
    private String CCCD;
    private LocalDate ngaySinh;
    private GioiTinh gioiTinh;
    private LoaiNhanVien chucVu;
    private String matKhau;

    public NhanVien() {
    }

    public NhanVien(String maNV, String tenNV, String soDT, String email, String diaChi, String cCCD, LocalDate ngaySinh,
                    GioiTinh gioiTinh, LoaiNhanVien chucVu, String matKhau) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.soDT = soDT;
        this.email = email;
        this.diaChi = diaChi;
        this.CCCD = cCCD;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.chucVu = chucVu;
        this.matKhau = matKhau;
    }

    public NhanVien(String maNV, String tenNV, String ngaySinh, GioiTinh gioiTinh,
                    String soDT, String email, String cCCD, LoaiNhanVien chucVu) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.ngaySinh = LocalDate.parse(
                ngaySinh, java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy")
        );
        this.gioiTinh = gioiTinh;
        this.soDT = soDT;
        this.email = email;
        this.CCCD = cCCD;
        this.chucVu = chucVu;
    }

    // Getters and Setters
    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public String getSoDT() {
        return soDT;
    }

    public void setSoDT(String soDT) {
        this.soDT = soDT;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String cCCD) {
        CCCD = cCCD;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public GioiTinh getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(GioiTinh gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public LoaiNhanVien getChucVu() {
        return chucVu;
    }

    public void setChucVu(LoaiNhanVien chucVu) {
        this.chucVu = chucVu;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }



    @Override
    public String toString() {
        return "NhanVien [maNV=" + maNV + ", tenNV=" + tenNV + ", soDT=" + soDT + ", email=" + email + ", diaChi="
                + diaChi + ", CCCD=" + CCCD + ", ngaySinh=" + ngaySinh + ", gioiTinh=" + gioiTinh + ", chucVu=" + chucVu + "]";
    }
}