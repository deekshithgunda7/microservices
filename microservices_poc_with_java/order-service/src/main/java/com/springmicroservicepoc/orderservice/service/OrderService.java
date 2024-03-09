package com.springmicroservicepoc.orderservice.service;

import com.springmicroservicepoc.orderservice.dto.InventoryResponse;
import com.springmicroservicepoc.orderservice.dto.OrderLineItemsDto;
import com.springmicroservicepoc.orderservice.dto.OrderRequest;
import com.springmicroservicepoc.orderservice.model.Order;
import com.springmicroservicepoc.orderservice.model.OrderLineItems;
import com.springmicroservicepoc.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes=order.getOrderLineItemsList()
                .stream().map(OrderLineItems::getSkuCode).toList();

        //call the inventory service to check if product is available or not before we place it
        //We here use Inter process communication using webclient
        InventoryResponse[] inventoryResponseArray=webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder ->uriBuilder.queryParam("skuCode",skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock= Arrays.stream(inventoryResponseArray)
                .allMatch(InventoryResponse::isInStock);

        if(allProductsInStock){
            orderRepository.save(order);
        }else{
            throw new IllegalArgumentException("Product is not in stock please try again later");
        }
        }



        private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
            OrderLineItems orderLineItems = new OrderLineItems();
            orderLineItems.setPrice(orderLineItemsDto.getPrice());
            orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
            orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
            return orderLineItems;
        }
}
