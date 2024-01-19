package lk.avn.irenttechs.dto;

public class UserDTO {
    private String email;
    private String password;
    private String message;

    public UserDTO() {
    }

    public UserDTO(String email, String password, String message) {
        this.email = email;
        this.password = password;
        this.message = message;
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
