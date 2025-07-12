package com.tawn.tawnht.service;

import com.tawn.tawnht.constant.PredefinedRole;
import com.tawn.tawnht.dto.request.InitialUserRequest;
import com.tawn.tawnht.dto.request.UserCreationRequest;
import com.tawn.tawnht.dto.request.UserUpdateRequest;
import com.tawn.tawnht.dto.request.VerifyUserRequest;
import com.tawn.tawnht.dto.response.UserResponse;
import com.tawn.tawnht.entity.Role;
import com.tawn.tawnht.entity.User;
import com.tawn.tawnht.exception.AppException;
import com.tawn.tawnht.exception.ErrorCode;
import com.tawn.tawnht.mapper.UserMapper;
import com.tawn.tawnht.repository.jpa.RoleRepository;
import com.tawn.tawnht.repository.jpa.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    EmailService emailService;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        user.setTokenVerify(createToken());
        user.setTimeCreateToken(LocalDateTime.now());
        user.setIsVerified(false);

//        requestVerify(user.getEmail(),user.getTokenVerify());

        return userMapper.toUserResponse(userRepository.save(user));
    }
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
        String verifyLink = "http://localhost:8080/users?token=";
        stringBuilder.append(verifyLink).append(token);
        String htmlBody = htmlMail.replace("[linkVerify]", stringBuilder.toString());
        log.info(stringBuilder.toString());
        emailService.sendEmail(email,"[Pupduck]: Yêu cầu xác thực",htmlBody);
    }
    public UserResponse verifyUser(String token){
        User user = userRepository.findByVerifyToken(token)
                .orElseThrow(()->new AppException(ErrorCode.VERIFY_FAILED));
        user.setIsVerified(true);
        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }
    private String createToken(){
        return UUID.randomUUID().toString();
    }


    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByEmail(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
