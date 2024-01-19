package lk.avn.irenttechs.model;

public class UserProfile {

    private String ad1;
    private String ad2;
    private String city;
    private String district;
    private String province;
    private String postalCode;

    public UserProfile(String ad1, String ad2, String city, String district, String province, String postalCode) {
        this.ad1 = ad1;
        this.ad2 = ad2;
        this.city = city;
        this.district = district;
        this.province = province;
        this.postalCode = postalCode;
    }

    public String getAd1() {
        return ad1;
    }

    public void setAd1(String ad1) {
        this.ad1 = ad1;
    }

    public String getAd2() {
        return ad2;
    }

    public void setAd2(String ad2) {
        this.ad2 = ad2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
