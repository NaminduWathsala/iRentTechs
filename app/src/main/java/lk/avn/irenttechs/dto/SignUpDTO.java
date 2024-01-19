package lk.avn.irenttechs.dto;

public class SignUpDTO {
    private String name;
    private String mobile;
    private String email;
    private String password;
    private String message;

    public SignUpDTO() {
    }

    public SignUpDTO(String name, String mobile, String email, String password, String message) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.password = password;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
