package lk.avn.irenttechs.model;

public class Products {


    private String documentId;
    private String category;
    private String brand;
    private String name;
    private String description;
    private String qty;
    private String price;
    private String product_image;
    private String datetime;
    private String status;

    public Products() {
    }

    public Products(String documentId) {
        this.documentId = documentId;
    }

    public Products(String category, String brand, String name, String description, String qty, String price, String product_image, String datetime, String status) {
        this.category = category;
        this.brand = brand;
        this.name = name;
        this.description = description;
        this.qty = qty;
        this.price = price;
        this.product_image = product_image;
        this.datetime = datetime;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getProduct_image() {
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
