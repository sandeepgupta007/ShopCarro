package com.coviam.shopcarro.order.dto;

import com.coviam.shopcarro.order.details.Details;
import com.coviam.shopcarro.order.details.OrderDetails;
import java.util.List;
import java.util.Objects;

public class OrderDto {

    private String email;
    private List<OrderDetails> details;

    public OrderDto() {
    }

    public OrderDto(String email, List<OrderDetails> details) {
        this.email = email;
        this.details = details;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<OrderDetails> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetails> details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDto orderDto = (OrderDto) o;
        return Objects.equals(email, orderDto.email) &&
                Objects.equals(details, orderDto.details);
    }

    @Override
    public int hashCode() {

        return Objects.hash(email, details);
    }

    @Override
    public String toString() {
        return "OrderDto{" +
                "email='" + email + '\'' +
                ", details=" + details +
                '}';
    }
}
