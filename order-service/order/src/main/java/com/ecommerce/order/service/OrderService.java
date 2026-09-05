package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.PlaceOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(PlaceOrderRequest request);

    List<OrderResponse> getOrdersForUser(Long userId);

    OrderResponse getOrder(Integer orderId);

    void cancelOrder(Integer orderId);
}
