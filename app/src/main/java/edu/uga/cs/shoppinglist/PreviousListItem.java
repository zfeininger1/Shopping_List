package edu.uga.cs.shoppinglist;

import java.util.List;

public class PreviousListItem {
    private String timestamp;
    private String email;
    private String totalPrice;
    private List<BasketItem> basketItems;

    public PreviousListItem() {

    }
    public PreviousListItem(String timestamp, String email, String totalPrice, List<BasketItem> basketItems) {
        this.timestamp = timestamp;
        this.email = email;
        this.basketItems = basketItems;
        this.totalPrice = totalPrice;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public List<BasketItem> getBasketItems() {
        return basketItems;
    }
    public void setBasketItems(List<BasketItem> basketItems) {
        this.basketItems = basketItems;
    }
    public String getTotalPrice() {
        return  totalPrice;
    }
    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }
}
