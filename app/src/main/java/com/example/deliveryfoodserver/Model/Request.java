package com.example.deliveryfoodserver.Model;

import java.util.List;

public class Request {
    public String address;
    public List<Order> food;
    public String name;
    public String phone;
    public String total;
    public String status;
    public Request(){

    }

    public Request(String address, List<Order> food, String name, String phone, String total) {
        this.address = address;
        this.food = food;
        this.name = name;
        this.phone = phone;
        this.total = total;
        this.status= "0";//0 is default, 1 is shipping, 2 is finish
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Order> getFood() {
        return food;
    }

    public void setFood(List<Order> food) {
        this.food = food;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
