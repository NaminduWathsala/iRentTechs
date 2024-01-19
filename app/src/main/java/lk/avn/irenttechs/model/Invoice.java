package lk.avn.irenttechs.model;

import java.util.List;

public class Invoice {
    private String documentId;
    private String user_email;
    private String datetime;
    private String status;
    private List<InvoiceItem> products;
    private String total_price;
    private String checkInDate;
    private String checkOutDate;

    public Invoice() {
    }

    public Invoice(String user_email, String datetime, String status, List<InvoiceItem> products, String total_price, String checkInDate, String checkOutDate) {
        this.user_email = user_email;
        this.datetime = datetime;
        this.status = status;
        this.products = products;
        this.total_price = total_price;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
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

    public List<InvoiceItem> getProducts() {
        return products;
    }

    public void setProducts(List<InvoiceItem> products) {
        this.products = products;
    }

    public String getTotal_price() {
        return total_price;
    }

    public void setTotal_price(String total_price) {
        this.total_price = total_price;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public static class InvoiceItem {
        private String product_id;
        private String qty;

        public InvoiceItem() {
        }

        public InvoiceItem(String product_id, String qty) {
            this.product_id = product_id;
            this.qty = qty;
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
    }
}
