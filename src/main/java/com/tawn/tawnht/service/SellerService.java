package com.tawn.tawnht.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;

import com.tawn.tawnht.constant.PredefinedRole;
import com.tawn.tawnht.dto.request.SellerCreationRequest;
import com.tawn.tawnht.dto.response.SellerProfileResponse;
import com.tawn.tawnht.dto.response.SellerResponse;
import com.tawn.tawnht.entity.Role;
import com.tawn.tawnht.entity.Seller;
import com.tawn.tawnht.entity.User;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.mapper.SellerMapper;
import com.tawn.tawnht.repository.jpa.ProductRepository;
import com.tawn.tawnht.repository.jpa.RoleRepository;
import com.tawn.tawnht.repository.jpa.SellerRepository;
import com.tawn.tawnht.repository.jpa.SubOrderRepository;
import com.tawn.tawnht.repository.jpa.UserRepository;
import com.tawn.tawnht.utils.SecurityUtils;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SellerService {
    SellerRepository sellerRepository;
    EmailService emailService;
    CloudinaryService cloudinaryService;
    UserRepository userRepository;
    SellerMapper sellerMapper;
    RoleRepository roleRepository;
    SubOrderRepository subOrderRepository;
    ProductRepository productRepository;
    EntityManager entityManager;

    public void requestVerify(String email, String token) {
        String htmlMail = "<!DOCTYPE html>\n" + "<html>\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <title>Xác Thực Tài Khoản Pupduck</title>\n"
                + "</head>\n"
                + "<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;\">\n"
                + "    <table width=\"100%\" bgcolor=\"#f4f4f4\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n"
                + "        <tr>\n"
                + "            <td align=\"center\" style=\"padding: 20px 0;\">\n"
                + "                <table bgcolor=\"#ffffff\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"border: 1px solid #e0e0e0; border-radius: 5px;\">\n"
                + "                    <tr>\n"
                + "                        <td align=\"center\" style=\"background-color: #6a0dad; padding: 20px;\">\n"
                + "                            <h1 style=\"color: #ffffff; margin: 0;\">Xác Thực Tài Khoản Pupduck</h1>\n"
                + "                        </td>\n"
                + "                    </tr>\n"
                + "                    <tr>\n"
                + "                        <td style=\"padding: 20px;\">\n"
                + "                            <p style=\"color: #333333;\">Chào bạn,</p>\n"
                + "                            <p style=\"color: #333333;\">Cảm ơn bạn đã đăng ký tại <strong>Pupduck</strong>! Để hoàn tất đăng ký, vui lòng xác thực email của bạn bằng cách nhấp vào nút bên dưới:</p>\n"
                + "                            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin: 20px 0;\">\n"
                + "                                <tr>\n"
                + "                                    <td align=\"center\">\n"
                + "                                        <a href=\"[linkVerify]\"\n"
                + "   style=\"display: inline-block; background: linear-gradient(45deg, #4b0082, #000000); \n"
                + "          padding: 15px 30px; border-radius: 5px; color: #ffffff; text-decoration: none;\n"
                + "          font-size: 16px; font-weight: bold;\">\n"
                + "   Xác Thực Tài Khoản\n"
                + "</a>\n"
                + "                                        <p style=\"color: #666666; font-size: 12px; margin-top: 10px;\">(Link này sẽ hết hạn sau 24 giờ)</p>\n"
                + "                                    </td>\n"
                + "                                </tr>\n"
                + "                            </table>\n"
                + "                            <p style=\"color: #333333;\">Nếu bạn không đăng ký, vui lòng bỏ qua email này hoặc liên hệ đội ngũ hỗ trợ của chúng tôi.</p>\n"
                + "                            <p style=\"text-align: center;\">\n"
                + "                                <img src=\"https://i.imgur.com/2nCt3Sbl.jpg\" alt=\"Con Vịt Dễ Thương\" style=\"width: 150px; height: auto; margin-top: 20px;\">\n"
                + "                            </p>\n"
                + "                        </td>\n"
                + "                    </tr>\n"
                + "                    <tr>\n"
                + "                        <td align=\"center\" style=\"padding: 20px; background-color: #f0f0f0; color: #666666;\">\n"
                + "                            <p>© 2025 Pupduck. Tất cả quyền được bảo lưu. | <a href=\"https://pupduck.com\" style=\"color: #6a0dad;\">Truy Cập Pupduck</a></p>\n"
                + "                        </td>\n"
                + "                    </tr>\n"
                + "                </table>\n"
                + "            </td>\n"
                + "        </tr>\n"
                + "    </table>\n"
                + "</body>\n"
                + "</html>";
        StringBuilder stringBuilder = new StringBuilder();
        String verifyLink = "http://localhost:8080/seller?token=";
        stringBuilder.append(verifyLink).append(token);
        String htmlBody = htmlMail.replace("[linkVerify]", stringBuilder.toString());
        log.info(stringBuilder.toString());
        emailService.sendEmail(email, "[Pupduck]: Yêu cầu xác thực", htmlBody);
    }

    public SellerResponse createSeller(SellerCreationRequest request) {
        String email =
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String url = request.getStoreLogo();
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.SELLER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        Seller seller = Seller.builder()
                .createdAt(LocalDateTime.now())
                .isVerified(false)
                .rating(0)
                .storeDescription(request.getStoreDescription())
                .storeLogo(url)
                .verifyToken(createToken())
                .storeName(request.getStoreName())
                .user(user)
                .build();
        seller = sellerRepository.save(seller);
        user.setSeller(seller);
        userRepository.save(user);
        //        requestVerify(email, seller.getVerifyToken());
        return sellerMapper.toSellerResponse(seller);
    }

    public SellerResponse activeSeller(String verifyToken) {
        Seller seller = sellerRepository
                .findSellerByVerifyToken(verifyToken)
                .orElseThrow(() -> new AppException(ErrorCode.VERIFY_FAILED));
        seller.setIsVerified(true);
        sellerRepository.save(seller);
        return sellerMapper.toSellerResponse(seller);
    }

    public SellerProfileResponse getSellerProfile() {
        // Get current user
        String email =
                SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        User currentUser =
                userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Seller seller = currentUser.getSeller();
        if (seller == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Get order statistics using native query
        String orderStatsQuery = "SELECT " + "COUNT(DISTINCT so.id) as total_orders, "
                + "COUNT(CASE WHEN so.status IN ('pending', 'init', 'processing') THEN 1 END) as pending_orders, "
                + "COUNT(CASE WHEN so.status IN ('completed', 'delivered') THEN 1 END) as completed_orders, "
                + "COALESCE(SUM(CASE WHEN so.status IN ('completed', 'delivered') THEN so.sub_total ELSE 0 END), 0) as total_revenue, "
                + "COALESCE(SUM(CASE WHEN so.status IN ('completed', 'delivered') AND so.created_at >= :monthStart THEN so.sub_total ELSE 0 END), 0) as monthly_revenue "
                + "FROM sub_order so "
                + "WHERE so.seller_id = :sellerId";

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Object[] orderStats = (Object[]) entityManager
                .createNativeQuery(orderStatsQuery)
                .setParameter("sellerId", seller.getId())
                .setParameter("monthStart", monthStart)
                .getSingleResult();

        Long totalOrders = ((Number) orderStats[0]).longValue();
        Long pendingOrders = ((Number) orderStats[1]).longValue();
        Long completedOrders = ((Number) orderStats[2]).longValue();
        BigDecimal totalRevenue = new BigDecimal(orderStats[3].toString());
        BigDecimal monthlyRevenue = new BigDecimal(orderStats[4].toString());

        // Get product statistics
        String productStatsQuery = "SELECT " + "COUNT(*) as total_products, "
                + "COUNT(CASE WHEN p.active = true THEN 1 END) as active_products "
                + "FROM product p "
                + "WHERE p.seller_id = :sellerId";

        Object[] productStats = (Object[]) entityManager
                .createNativeQuery(productStatsQuery)
                .setParameter("sellerId", seller.getId())
                .getSingleResult();

        Long totalProducts = ((Number) productStats[0]).longValue();
        Long activeProducts = ((Number) productStats[1]).longValue();

        // Get customer count
        String customerCountQuery = "SELECT COUNT(DISTINCT o.user_id) " + "FROM orders o "
                + "INNER JOIN sub_order so ON o.id = so.order_id "
                + "WHERE so.seller_id = :sellerId";

        Long totalCustomers = ((Number) entityManager
                        .createNativeQuery(customerCountQuery)
                        .setParameter("sellerId", seller.getId())
                        .getSingleResult())
                .longValue();

        return SellerProfileResponse.builder()
                .sellerId(seller.getId())
                .storeName(seller.getStoreName())
                .storeDescription(seller.getStoreDescription())
                .storeLogo(seller.getStoreLogo())
                .rating(seller.getRating())
                .isVerified(seller.getIsVerified())
                .sellerCreatedAt(seller.getCreatedAt())
                .userId(currentUser.getId())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .email(currentUser.getEmail())
                .picture(currentUser.getPicture())
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .totalCustomers(totalCustomers)
                .unreadNotifications(0) // TODO: Implement notifications
                .build();
    }

    private String createToken() {
        return UUID.randomUUID().toString();
    }
}
