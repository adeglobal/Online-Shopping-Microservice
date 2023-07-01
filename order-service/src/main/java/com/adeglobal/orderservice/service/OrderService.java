package com.adeglobal.orderservice.service;


import com.adeglobal.orderservice.dto.InventoryResponse;
import com.adeglobal.orderservice.dto.OrderLineItemsDto;
import com.adeglobal.orderservice.dto.OrderRequest;
import com.adeglobal.orderservice.model.Order;
import com.adeglobal.orderservice.model.OrderLineItems;
import com.adeglobal.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.lang.String;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
      public void placeOrder (OrderRequest orderRequest){
          Order order = new Order();
          order.setOrderNumber(UUID.randomUUID().toString());

         List<OrderLineItems> orderLineItems =  orderRequest.getOrderLineItemsDtoList()
                  .stream()
                  .map(this::mapToDto)
                  .toList();

        order.setOrderlineItemsList(orderLineItems);


        List<String> skuCodes = order.getOrderlineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        // Call Inventory Service, and place order if product is in stock
          InventoryResponse[] inventoryResponsesArray = webClient.get()
                  .uri("http://localhost:8082/api/inventory",
                          uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                  .retrieve()
                  .bodyToMono(InventoryResponse[].class)
                  .block();

          boolean allProductsInStock = Arrays.stream(inventoryResponsesArray)
                  .allMatch(InventoryResponse::isInStock);


          if(allProductsInStock){
              orderRepository.save(order);
          }else {
              throw new IllegalArgumentException("Product is not in stock , please try again later");
          }
      }
      private  OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
          OrderLineItems orderLineItems = new OrderLineItems();
          orderLineItems.setPrice(orderLineItemsDto.getPrice());
          orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
          orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
          return orderLineItems;

      }
}
