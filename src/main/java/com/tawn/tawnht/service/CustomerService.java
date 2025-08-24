package com.tawn.tawnht.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tawn.tawnht.dto.response.CustomerResponse;
import com.tawn.tawnht.dto.response.PageResponse;
import com.tawn.tawnht.entity.Seller;
import com.tawn.tawnht.entity.User;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.repository.jpa.SellerRepository;
import com.tawn.tawnht.repository.jpa.UserRepository;
import com.tawn.tawnht.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CustomerService {

    UserRepository userRepository;
    SellerRepository sellerRepository;
    EntityManager entityManager;

    public PageResponse<CustomerResponse> getCustomersBySeller(Pageable pageable, String search) {
        // Get current seller
        String email =
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        User currentUser =
                userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Seller seller = currentUser.getSeller();
        if (seller == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Build the base query to find customers who have ordered from this seller
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("SELECT DISTINCT u.id, u.first_name, u.last_name, u.email, u.picture, ")
                .append("COUNT(DISTINCT o.id) as total_orders, ")
                .append("COALESCE(SUM(oi.price * oi.quantity), 0) as total_spent, ")
                .append("COALESCE(AVG(oi.price * oi.quantity), 0) as avg_order_value, ")
                .append("MAX(so.status) as last_order_status, ")
                .append("MAX(so.created_at) as last_order_date, ")
                .append("MIN(so.created_at) as first_order_date, ")
                .append("ua.phone_number as phone, ")
                .append("ua.city as city, ")
                .append("ua.address as address ")
                .append("FROM users u ")
                .append("INNER JOIN orders o ON u.id = o.user_id ")
                .append("INNER JOIN sub_order so ON o.id = so.order_id ")
                .append("INNER JOIN order_item oi ON so.id = oi.sub_order_id ")
                .append("LEFT JOIN user_address ua ON o.user_address_id = ua.id ")
                .append("WHERE so.seller_id = :sellerId ");

        // Add search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            queryBuilder
                    .append("AND (LOWER(u.first_name) LIKE :search ")
                    .append("OR LOWER(u.last_name) LIKE :search ")
                    .append("OR LOWER(u.email) LIKE :search) ");
        }

        queryBuilder
                .append(
                        "GROUP BY u.id, u.first_name, u.last_name, u.email, u.picture, ua.phone_number, ua.city, ua.address ")
                .append("ORDER BY last_order_date DESC ");

        // Create native query
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter("sellerId", seller.getId());

        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        // Get total count
        String countQuery = "SELECT COUNT(DISTINCT u.id) " + "FROM users u "
                + "INNER JOIN orders o ON u.id = o.user_id "
                + "INNER JOIN sub_order so ON o.id = so.order_id "
                + "WHERE so.seller_id = :sellerId ";

        if (search != null && !search.trim().isEmpty()) {
            countQuery += "AND (LOWER(u.first_name) LIKE :search " + "OR LOWER(u.last_name) LIKE :search "
                    + "OR LOWER(u.email) LIKE :search) ";
        }

        Query totalQuery = entityManager.createNativeQuery(countQuery);
        totalQuery.setParameter("sellerId", seller.getId());

        if (search != null && !search.trim().isEmpty()) {
            totalQuery.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        Number totalElements = (Number) totalQuery.getSingleResult();

        // Apply pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        // Convert results to CustomerResponse objects
        List<CustomerResponse> customers =
                results.stream().map(this::mapToCustomerResponse).collect(Collectors.toList());

        Page<CustomerResponse> customerPage = new PageImpl<>(customers, pageable, totalElements.longValue());

        return PageResponse.<CustomerResponse>builder()
                .currentPage(pageable.getPageNumber())
                .totalPages(customerPage.getTotalPages())
                .pageSize(pageable.getPageSize())
                .totalElements(customerPage.getTotalElements())
                .data(customers)
                .build();
    }

    private CustomerResponse mapToCustomerResponse(Object[] row) {
        String userId = (String) row[0];
        String firstName = (String) row[1];
        String lastName = (String) row[2];
        String email = (String) row[3];
        String picture = (String) row[4];
        Long totalOrders = ((Number) row[5]).longValue();
        BigDecimal totalSpent = new BigDecimal(row[6].toString());
        BigDecimal avgOrderValue = totalOrders > 0
                ? totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        String lastOrderStatus = (String) row[8];
        LocalDateTime lastOrderDate = row[9] != null ? ((java.sql.Timestamp) row[9]).toLocalDateTime() : null;
        LocalDateTime firstOrderDate = row[10] != null ? ((java.sql.Timestamp) row[10]).toLocalDateTime() : null;
        String phone = (String) row[11];
        String city = (String) row[12];
        String address = (String) row[13];

        // Determine if customer is active (has orders in last 6 months)
        Boolean isActive = lastOrderDate != null
                && lastOrderDate.isAfter(LocalDateTime.now().minusMonths(6));

        // Determine customer tier based on total spent
        String customerTier;
        if (totalSpent.compareTo(new BigDecimal("10000000")) >= 0) { // 10M VND
            customerTier = "VIP";
        } else if (totalSpent.compareTo(new BigDecimal("1000000")) >= 0) { // 1M VND
            customerTier = "Regular";
        } else {
            customerTier = "New";
        }

        return CustomerResponse.builder()
                .id(userId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .picture(picture)
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .averageOrderValue(avgOrderValue)
                .lastOrderStatus(lastOrderStatus)
                .lastOrderDate(lastOrderDate)
                .firstOrderDate(firstOrderDate)
                .phone(phone)
                .city(city)
                .address(address)
                .isActive(isActive)
                .customerTier(customerTier)
                .build();
    }
}
