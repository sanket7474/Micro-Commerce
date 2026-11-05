package com.ecommerce.order.service.impl;

import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.CustomerOrder;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.entity.OutboxEvent;
import com.ecommerce.order.repository.CustomerOrderRepository;
import com.ecommerce.order.repository.OutboxEventRepository;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CustomerOrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final RestTemplate restTemplate;

    @Value("${services.product.url}")
    private String productServiceUrl;

    @Value("${services.inventory.url}")
    private String inventoryServiceUrl;

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    /**
     * The order-placement saga. order-service is the orchestrator:
     * price + validate items -> reserve stock -> charge payment ->
     * commit stock (success) or release stock (failure).
     */
    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {

        // 1. Validate items and snapshot price from product-service
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            ProductResponse product = restTemplate.getForObject(
                    productServiceUrl + "/products/" + itemReq.getProductId(),
                    ProductResponse.class
            );

            OrderItem item = new OrderItem();
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(product.getPrice());
            items.add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        // 2. Create the order in PENDING state so we have an order id
        //    to pass to inventory-service for the reservation/ledger
        CustomerOrder order = new CustomerOrder();
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(total);
        items.forEach(order::addItem);
        order = orderRepository.save(order);

        // 3. Reserve stock for every item
        List<OrderItem> reservedSoFar = new ArrayList<>();
        boolean reservationFailed = false;

        for (OrderItem item : items) {
            try {
                restTemplate.postForObject(
                        inventoryServiceUrl + "/inventory/" + item.getProductId() + "/reserve",
                        new StockOperationRequest(order.getId(), item.getQuantity().intValue()),
                        Void.class
                );
                reservedSoFar.add(item);
            } catch (Exception ex) {
                reservationFailed = true;
                break;
            }
        }

        if (reservationFailed) {
            // Compensate: release whatever was reserved before the failure
            for (OrderItem item : reservedSoFar) {
                restTemplate.postForObject(
                        inventoryServiceUrl + "/inventory/" + item.getProductId() + "/release",
                        new StockOperationRequest(order.getId(), item.getQuantity().intValue()),
                        Void.class
                );
            }
            return failOrder(order, "INSUFFICIENT_STOCK");
        }

        // 4. Charge payment
        PaymentDtos.PaymentRequest paymentRequest = new PaymentDtos.PaymentRequest(
                order.getId(),
                "order-" + order.getId(),
                total,
                "CARD"
        );

        PaymentDtos.PaymentResponse paymentResponse = restTemplate.postForObject(
                paymentServiceUrl + "/payments",
                paymentRequest,
                PaymentDtos.PaymentResponse.class
        );

        if (paymentResponse == null || !"SUCCESS".equals(paymentResponse.getStatus())) {
            // Compensate: release all reservations since payment didn't go through
            for (OrderItem item : items) {
                restTemplate.postForObject(
                        inventoryServiceUrl + "/inventory/" + item.getProductId() + "/release",
                        new StockOperationRequest(order.getId(), item.getQuantity().intValue()),
                        Void.class
                );
            }
            return failOrder(order, "PAYMENT_DECLINED");
        }

        // 5. Payment succeeded - commit the reservations permanently
        for (OrderItem item : items) {
            restTemplate.postForObject(
                    inventoryServiceUrl + "/inventory/" + item.getProductId() + "/commit",
                    new StockOperationRequest(order.getId(), item.getQuantity().intValue()),
                    Void.class
            );
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        writeOutboxEvent(order, "OrderConfirmed");

        return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount());
    }

    @Override
    public List<OrderResponse> getOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(o -> new OrderResponse(o.getId(), o.getStatus(), o.getTotalAmount()))
                .toList();
    }

    @Override
    public OrderResponse getOrder(Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId).orElseThrow();
        return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount());
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId).orElseThrow();

        if (order.getStatus() == OrderStatus.PENDING) {
            for (OrderItem item : order.getItems()) {
                restTemplate.postForObject(
                        inventoryServiceUrl + "/inventory/" + item.getProductId() + "/release",
                        new StockOperationRequest(order.getId(), item.getQuantity().intValue()),
                        Void.class
                );
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private OrderResponse failOrder(CustomerOrder order, String reason) {
        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        writeOutboxEvent(order, "OrderFailed");
        return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount());
    }

    private void writeOutboxEvent(CustomerOrder order, String eventType) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateId(String.valueOf(order.getId()));
        event.setEventType(eventType);
        event.setPayload("{\"orderId\":" + order.getId() + ",\"userId\":" + order.getUserId() + "}");
        event.setStatus("PENDING");
        outboxEventRepository.save(event);
    }
}
