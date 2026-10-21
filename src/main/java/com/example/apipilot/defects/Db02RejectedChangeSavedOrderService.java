package com.example.apipilot.defects;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.apipilot.dto.UpdateOrderStatusRequest;
import com.example.apipilot.entity.Order;
import com.example.apipilot.enums.OrderStatus;
import com.example.apipilot.repository.OrderRepository;
import com.example.apipilot.service.OrderService;

/**
 * INTENTIONAL DEFECT DB-02 (see docs/defect-register.md and docs/experiments/db-02/design.md).
 * <p>
 * When enabled, a status change that the unchanged {@link OrderService} rejects with 409 is still written
 * to the database: the response is the same 409 as in clean mode, but the order now has the forbidden status.
 * The accept/reject decision itself is the clean service's.
 * <p>
 * Disabled by default: this bean only exists when {@code pilot.defects.db02-rejected-change-saved.enabled=true}.
 * Do not enable together with another OrderService-replacing defect (ST-01): only one {@code @Primary} may exist.
 */
@Service
@Primary
@ConditionalOnProperty(name = "pilot.defects.db02-rejected-change-saved.enabled", havingValue = "true")
public class Db02RejectedChangeSavedOrderService extends OrderService {

	private static final Logger log = LoggerFactory.getLogger(Db02RejectedChangeSavedOrderService.class);

	private final OrderRepository orderRepository;

	public Db02RejectedChangeSavedOrderService(OrderRepository orderRepository) {
		super(orderRepository);
		this.orderRepository = orderRepository;
		log.warn("DEFECT DB-02 ENABLED: PATCH /api/orders/{id}/status saves rejected (409) status changes");
	}

	@Override
	@Transactional(noRollbackFor = ResponseStatusException.class)
	public Order updateStatus(Long id, UpdateOrderStatusRequest request) {
		try {
			return super.updateStatus(id, request);
		} catch (ResponseStatusException rejected) {
			if (rejected.getStatusCode() == HttpStatus.CONFLICT) {
				Order order = getOrder(id);
				order.setStatus(request.status());
				if (request.status() == OrderStatus.COMPLETED) {
					order.setCompletedAt(LocalDateTime.now());
				}
				orderRepository.save(order);
			}
			throw rejected;
		}
	}

}
