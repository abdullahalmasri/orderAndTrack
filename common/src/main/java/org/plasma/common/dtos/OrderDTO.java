package org.plasma.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class OrderDTO {
    private final String nameOfClient;
    private final String nameOfProvider;
    private final String description;
    private final BigDecimal price;
}
