package com.tawn.tawnht.service;

import com.tawn.tawnht.constant.PredefinedRole;
import com.tawn.tawnht.dto.request.SellerCreationRequest;
import com.tawn.tawnht.dto.response.SellerResponse;
import com.tawn.tawnht.entity.Role;
import com.tawn.tawnht.entity.Seller;
import com.tawn.tawnht.entity.User;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.mapper.SellerMapper;
import com.tawn.tawnht.repository.jpa.RoleRepository;
import com.tawn.tawnht.repository.jpa.SellerRepository;
import com.tawn.tawnht.repository.jpa.UserRepository;
import com.tawn.tawnht.utils.SecurityUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class SellerService {
    SellerRepository sellerRepository;
    EmailService emailService;
    CloudinaryService cloudinaryService;
    UserRepository userRepository;
    SellerMapper sellerMapper;
    RoleRepository roleRepository;
    public void requestVerify(String email,String token){
        String htmlMail = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Xác Thực Tài Khoản Pupduck</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;\">\n" +
                "    <table width=\"100%\" bgcolor=\"#f4f4f4\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "        <tr>\n" +
                "            <td align=\"center\" style=\"padding: 20px 0;\">\n" +
                "                <table bgcolor=\"#ffffff\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"border: 1px solid #e0e0e0; border-radius: 5px;\">\n" +
                "                    <tr>\n" +
                "                        <td align=\"center\" style=\"background-color: #6a0dad; padding: 20px;\">\n" +
                "                            <h1 style=\"color: #ffffff; margin: 0;\">Xác Thực Tài Khoản Pupduck</h1>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 20px;\">\n" +
                "                            <p style=\"color: #333333;\">Chào bạn,</p>\n" +
                "                            <p style=\"color: #333333;\">Cảm ơn bạn đã đăng ký tại <strong>Pupduck</strong>! Để hoàn tất đăng ký, vui lòng xác thực email của bạn bằng cách nhấp vào nút bên dưới:</p>\n" +
                "                            <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin: 20px 0;\">\n" +
                "                                <tr>\n" +
                "                                    <td align=\"center\">\n" +
                "                                        <a href=\"[linkVerify]\"\n" +
                "   style=\"display: inline-block; background: linear-gradient(45deg, #4b0082, #000000); \n" +
                "          padding: 15px 30px; border-radius: 5px; color: #ffffff; text-decoration: none;\n" +
                "          font-size: 16px; font-weight: bold;\">\n" +
                "   Xác Thực Tài Khoản\n" +
                "</a>\n" +
                "                                        <p style=\"color: #666666; font-size: 12px; margin-top: 10px;\">(Link này sẽ hết hạn sau 24 giờ)</p>\n" +
                "                                    </td>\n" +
                "                                </tr>\n" +
                "                            </table>\n" +
                "                            <p style=\"color: #333333;\">Nếu bạn không đăng ký, vui lòng bỏ qua email này hoặc liên hệ đội ngũ hỗ trợ của chúng tôi.</p>\n" +
                "                            <p style=\"text-align: center;\">\n" +
                "                                <img src=\"https://i.imgur.com/2nCt3Sbl.jpg\" alt=\"Con Vịt Dễ Thương\" style=\"width: 150px; height: auto; margin-top: 20px;\">\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td align=\"center\" style=\"padding: 20px; background-color: #f0f0f0; color: #666666;\">\n" +
                "                            <p>© 2025 Pupduck. Tất cả quyền được bảo lưu. | <a href=\"https://pupduck.com\" style=\"color: #6a0dad;\">Truy Cập Pupduck</a></p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
        StringBuilder stringBuilder = new StringBuilder();
        String verifyLink = "http://localhost:8080/seller?token=";
        stringBuilder.append(verifyLink).append(token);
        String htmlBody = htmlMail.replace("[linkVerify]", stringBuilder.toString());
        log.info(stringBuilder.toString());
        emailService.sendEmail(email,"[Pupduck]: Yêu cầu xác thực",htmlBody);
    }
    public SellerResponse createSeller(SellerCreationRequest request)  {
        String email= SecurityUtils.getCurrentUserLogin()
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        String url=request.getStoreLogo();
        HashSet<Role> roles = new HashSet<>();
       roleRepository.findById(PredefinedRole.SELLER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        Seller seller=Seller.builder()
                .createdAt(LocalDateTime.now())
                .isVerified(false)
                .rating(0)
                .storeDescription(request.getStoreDescription())
                .storeLogo(url)
                .verifyToken(createToken())
                .storeName(request.getStoreName())
                .user(user)
                .build();
        seller=sellerRepository.save(seller);
        user.setSeller(seller);
        userRepository.save(user);
        requestVerify(email,seller.getVerifyToken());
        return sellerMapper.toSellerResponse(seller);
    }
    public SellerResponse activeSeller(String verifyToken){
        Seller seller=sellerRepository.findSellerByVerifyToken(verifyToken)
                .orElseThrow(()->new AppException(ErrorCode.VERIFY_FAILED));
        seller.setIsVerified(true);
        sellerRepository.save(seller);
        return sellerMapper.toSellerResponse(seller);
    }
    private String createToken(){
        return UUID.randomUUID().toString();
    }
}
