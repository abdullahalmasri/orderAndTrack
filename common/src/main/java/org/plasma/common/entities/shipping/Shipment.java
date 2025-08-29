//package org.plasma.common.entities;
//
//public class Shipment {
//}


package org.plasma.common.entities.shipping;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import org.plasma.common.utils.GeneratedIdCreatedAt;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "shipments")
public class Shipment extends GeneratedIdCreatedAt implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "order_id", nullable = false)
    private final UUID orderId;
    @Column(name = "address", nullable = false, length = 255)
    private final String address;

    public Shipment(UUID orderId, String address) {
        super();
        this.orderId = orderId;
        this.address = address;
    }

    public Shipment() {
        super();
        this.orderId = null;
        this.address = null;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getAddress() {
        return address;
    }
}