package com.example.apipilot.defects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apipilot.dto.UpdateOrderStatusRequest;
import com.example.apipilot.entity.Order;
import com.example.apipilot.enums.OrderStatus;
import com.example.apipilot.repository.OrderRepository;
import com.example.apipilot.service.OrderService;

/**
 * INTENTIONAL DEFECT ST-01 (see docs/defect-register.md and docs/experiments/st-01/design.md).
 * <p>
 * When enabled, {@code PATCH /api/orders/{id}/status} accepts the forbidden transition
 * {@code COMPLETED -> CANCELLED} with 200 and saves it. Every other request goes through the
 * unchanged {@link OrderService} logic, so all other disallowed transitions are still rejected.
 * <p>
 * Disabled by default: this bean only exists when
 * {@code pilot.defects.st01-completed-to-cancelled.enabled=true}.
 */
@Service
@Primary
@ConditionalOnProperty(name = "pilot.defects.st01-completed-to-cancelled.enabled", havingValue = "true")
public class St01CompletedToCancelledOrderService extends OrderService {

	private static final Logger log = LoggerFactory.getLogger(St01CompletedToCancelledOrderService.class);

	private final OrderRepository orderRepository;

	public St01CompletedToCancelledOrderService(OrderRepository orderRepository) {
		super(orderRepository);
		this.orderRepository = orderRepository;
		log.warn("DEFECT ST-01 ENABLED: PATCH /api/orders/{id}/status accepts COMPLETED -> CANCELLED");
	}

	@Override
	@Transactional
	public Order updateStatus(Long id, UpdateOrderStatusRequest request) {
		Order order = getOrder(id);
		if (order.getStatus() == OrderStatus.COMPLETED && request.status() == OrderStatus.CANCELLED) {
			order.setStatus(OrderStatus.CANCELLED);
			return orderRepository.save(order);
		}
		return super.updateStatus(id, request);
	}

}
