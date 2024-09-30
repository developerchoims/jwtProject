package com.ms.hoopi.order.service.serviceImpl;

import com.ms.hoopi.common.util.CommonUtil;
import com.ms.hoopi.constants.Constants;
import com.ms.hoopi.model.entity.*;
import com.ms.hoopi.order.model.dto.OrderRequestDto;
import com.ms.hoopi.order.model.dto.PaymentRequestDto;
import com.ms.hoopi.order.service.OrderService;
import com.ms.hoopi.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepoisitory orderDetailRepoisitory;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final PaymentRepository paymentRepository;
    private final CommonUtil commonUtil;
    private final RestTemplate restTemplate;

    @Override
    public ResponseEntity<String> addOrder(OrderRequestDto orderRequestDto) {
        try{
            PaymentRequestDto paymentRequestDto = orderRequestDto.getPaymentRequestDto();
            String paymentCode = paymentRequestDto.getPaymentCode();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "PortOne " + System.getenv("PORTONE_API_SECRET"));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = "https://api.portone.io/payments/" + paymentCode;
            ResponseEntity<Map> paymentResponse = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (!paymentResponse.getStatusCode().is2xxSuccessful()) {
                throw new Exception("Failed to retrieve payment info");
            }
            // cartCode로 cart정보 가져오기
            Cart cart = cartRepository.findByCartCode(orderRequestDto.getCartCode())
                    .orElseThrow(() -> new EntityNotFoundException(Constants.NONE_CART));

            //반복문
            Long payAmount = 0L;
            for(String productCode : orderRequestDto.getProductCode()){
                // cartDetail 정보를 통해 order, orderDetail 정보 새로 만들기
                CartDetail cartDetail = cartDetailRepository.findByCartCodeAndProductCode(orderRequestDto.getCartCode(), productCode)
                        .orElseThrow(() -> new EntityNotFoundException(Constants.NONE_CART_PRODUCT));
                // order 정보 추가
                Order order = Order.builder()
                        .orderCode(commonUtil.createCode())
                        .code(cart.getCode())
                        .orderDate(LocalDateTime.now())
                        .status(Order.Status.결제완료)
                        .build();
                Order savedOrder = orderRepository.save(order);
                // orderDetail 정보 추가
                OrderDetail orderDetail = OrderDetail.builder()
                        .orderCode(savedOrder)
                        .productCode(cartDetail.getProductCode())
                        .quantity(cartDetail.getQuantity())
                        .orderAmount(cartDetail.getCartAmount())
                        .totalPrice(cartDetail.getCartAmount())
                        .build();
                orderDetailRepoisitory.save(orderDetail);
                payAmount += cartDetail.getCartAmount();

                // cartDetail 정보 삭제
                cartDetailRepository.deleteByCartCodeAndProductCode(orderRequestDto.getCartCode(), productCode);
                // payment 정보 추가
                addPayment(order, orderRequestDto.getPaymentRequestDto());
            }
            Map<String, Object> payment = paymentResponse.getBody();

            if (payAmount.toString().equals(payment.get("amount"))) {
                switch ((String) payment.get("status")) {
                    case "VIRTUAL_ACCOUNT_ISSUED":
                        return ResponseEntity.badRequest().body(Constants.ORDER_FAIL_VIRTUAL_ACCOUNT);
                    case "PAID":
                        // cart status Y로 변경하기
                        changeCartStatus(cart);
                        return ResponseEntity.ok(Constants.ORDER_SUCCESS);
                    default:
                        throw new IllegalStateException(Constants.ORDER_FAIL);
                }
            } else {
                throw new IllegalStateException(Constants.ORDER_FAIL_DO_NOT_MATCH);
            }

        } catch (Exception e) {
            log.error(Constants.ORDER_FAIL, e);
            return ResponseEntity.badRequest().body(Constants.ORDER_FAIL);
        }

    }

    // payment 정보 저장하기
    private void addPayment(Order order, PaymentRequestDto paymentRequestDto){
        Payment payment = Payment.builder()
                .orderCode(order)
                .code(order.getCode())
                .paymentCode(paymentRequestDto.getPaymentCode())
                .paymentAmount(paymentRequestDto.getPaymentAmount())
                .method(paymentRequestDto.getMethod())
                .paymentDate(LocalDateTime.now())
                .bank(paymentRequestDto.getBank())
                .status(Payment.Status.결제완료)
                .build();
        paymentRepository.save(payment);
    }

    // cart status Y로 변경하기
    private void changeCartStatus(Cart cart) {
        boolean flag = cartDetailRepository.findAllByCartCode(cart).isEmpty();
        if(flag){
            Cart changedCart = Cart.builder()
                    .cartCode(cart.getCartCode())
                    .code(cart.getCode())
                    .status("Y")
                    .build();
            cartRepository.save(changedCart);
        }
    }
}