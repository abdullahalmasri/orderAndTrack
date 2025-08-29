package org.plasma.common.entities.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.plasma.common.utils.GeneratedIdCreatedAt;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Entity
@Table(name = "orders")
public class Order extends GeneratedIdCreatedAt implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "name_client", nullable = false, length = 100)
    private final String nameOfClient;
    @Column(name = "name_provider", nullable = false, length = 100)
    private final String nameOfProvider;
    @Column(name = "information_order_details", nullable = false, length = 255)
    private final String description;
    @Column(name = "price_total", nullable = false, precision = 10, scale = 2)
    private final BigDecimal price;

    public Order(String nameOfClient, String nameOfProvider, String description, BigDecimal price) {
        super();
        this.nameOfClient = nameOfClient;
        this.nameOfProvider = nameOfProvider;
        this.description = description;
        this.price = price;
    }

    public Order() {
        super();
        this.nameOfClient = null;
        this.nameOfProvider = null;
        this.description = null;
        this.price = null;
    }

    public String getNameOfClient() {
        return nameOfClient;
    }

    public String getNameOfProvider() {
        return nameOfProvider;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Order order = (Order) o;
        return Objects.equals(nameOfClient, order.nameOfClient) && Objects.equals(nameOfProvider, order.nameOfProvider) && Objects.equals(description, order.description) && Objects.equals(price, order.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nameOfClient, nameOfProvider, description, price);
    }

    @Override
    public String toString() {
        return "Order{" +
                "nameOfClient='" + nameOfClient + '\'' +
                ", nameOfProvider='" + nameOfProvider + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}
