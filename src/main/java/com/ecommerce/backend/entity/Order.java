package com.ecommerce.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Setter;

@Data
@Entity
@Setter
@Table(name = "orders")
public class Order extends BaseEntity {
    
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems;

  @Column(name = "total_price", nullable = false)
  private BigDecimal totalPrice;

  // Setter for orderItems using the expected name
  public void setOrderItemList(List<OrderItem> orderItems) {
      this.orderItems = orderItems;
  }

  // Setter for totalPrice
  public void setTotalPrice(BigDecimal totalPrice) {
      this.totalPrice = totalPrice;
  }

}
