package lk.avn.irenttechs.model;

public class Cart {
    private String documentId;
    private String user_email;
    private String product_id;
    private String qty;
    private String datetime;
    private String status;

    public Cart(String documentId, String user_email, String product_id, String qty, String datetime, String status) {
        this.documentId = documentId;
        this.user_email = user_email;
        this.product_id = product_id;
        this.qty = qty;
        this.datetime = datetime;
        this.status = status;
    }

    public Cart(String user_email, String product_id, String qty, String datetime, String status) {
        this.user_email = user_email;
        this.product_id = product_id;
        this.qty = qty;
        this.datetime = datetime;
        this.status = status;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
