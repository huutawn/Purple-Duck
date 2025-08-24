package com.tawn.tawnht.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tawn.tawnht.dto.response.NotificationResponse;
import com.tawn.tawnht.dto.response.PageResponse;
import com.tawn.tawnht.entity.Seller;
import com.tawn.tawnht.entity.User;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
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
public class NotificationService {

    UserRepository userRepository;

    public PageResponse<NotificationResponse> getSellerNotifications(Pageable pageable) {
        // Get current seller
        String email =
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        User currentUser =
                userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Seller seller = currentUser.getSeller();
        if (seller == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // For now, return mock notifications - in a real system, you'd query from a notifications table
        List<NotificationResponse> mockNotifications = Arrays.asList(
                NotificationResponse.builder()
                        .id(1L)
                        .title("Đơn hàng mới")
                        .message("Bạn có một đơn hàng mới từ khách hàng")
                        .type("ORDER")
                        .isRead(false)
                        .relatedEntityId("123")
                        .relatedEntityType("ORDER")
                        .createdAt(LocalDateTime.now().minusHours(1))
                        .build(),
                NotificationResponse.builder()
                        .id(2L)
                        .title("Sản phẩm hết hàng")
                        .message("Sản phẩm ABC đã hết hàng trong kho")
                        .type("SYSTEM")
                        .isRead(false)
                        .relatedEntityId("456")
                        .relatedEntityType("PRODUCT")
                        .createdAt(LocalDateTime.now().minusHours(3))
                        .build(),
                NotificationResponse.builder()
                        .id(3L)
                        .title("Thanh toán thành công")
                        .message("Đơn hàng #789 đã được thanh toán thành công")
                        .type("PAYMENT")
                        .isRead(true)
                        .relatedEntityId("789")
                        .relatedEntityType("ORDER")
                        .createdAt(LocalDateTime.now().minusHours(5))
                        .build());

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), mockNotifications.size());
        List<NotificationResponse> pageContent = mockNotifications.subList(start, end);

        Page<NotificationResponse> notificationPage = new PageImpl<>(pageContent, pageable, mockNotifications.size());

        return PageResponse.<NotificationResponse>builder()
                .currentPage(pageable.getPageNumber())
                .totalPages(notificationPage.getTotalPages())
                .pageSize(pageable.getPageSize())
                .totalElements(notificationPage.getTotalElements())
                .data(pageContent)
                .build();
    }

    public Integer getUnreadNotificationCount() {
        // Get current seller
        String email =
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        User currentUser =
                userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Seller seller = currentUser.getSeller();
        if (seller == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // For now, return a mock count - in a real system, you'd query the notifications table
        return 2; // Mock unread count
    }
}
