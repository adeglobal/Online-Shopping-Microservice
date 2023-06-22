package com.adeglobal.orderservice.service;


import com.adeglobal.orderservice.dto.OrderLineItemsDto;
import com.adeglobal.orderservice.dto.OrderRequest;
import com.adeglobal.orderservice.model.Order;
import com.adeglobal.orderservice.model.OrderLineItems;
import com.adeglobal.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
      public  void placeOrder (OrderRequest orderRequest){
          Order order = new Order();
          order.setOrderNumber(UUID.randomUUID().toString());

         List<OrderLineItems> orderLineItemsDtoList =  orderRequest.getOrderLineItemsDtoList()
                  .stream()
                  .map(this::mapToDto)
                  .toList();
        order.setOrderlineItemList(orderLineItemsDtoList);
        orderRepository.save(order);
      }
      private  OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
          OrderLineItems orderLineItems = new OrderLineItems();
          orderLineItems.setPrice(orderLineItemsDto.getPrice());
          orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
          orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
          return orderLineItems;

      }
}
