package com.tawn.tawnht.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    VERIFY_FAILED(1009,"xác thực thất bại",HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(1010,"không tìm thấy danh mục",HttpStatus.NOT_FOUND),
    SELLER_NOT_FOUND(1011,"không tìm thấy nhà bán",HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(1012,"không tìm thấy sản phẩm",HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_NOT_FOUND(1013,"không tìm thấy sản phẩm",HttpStatus.NOT_FOUND),
    OUT_OF_STOCK(1014,"số lượng hàng không đủ",HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_ACTIVE(1015,"hàng khóa",HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_FOUND(1015,"không tìm thấy địa chỉ",HttpStatus.BAD_REQUEST),
    CART_NOT_FOUND(1016,"kh",HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1017,"KHÔNG TÌM THẤY ĐƠN HÀNG",HttpStatus.NOT_FOUND),
    ORDER_CANNOT_BE_DELETED(1018,"đơn hàng hiện tại không thể hủy",HttpStatus.BAD_REQUEST),
    ORDER_ITEMS_EMPTY(1019,"giỏ hàng rỗng",HttpStatus.BAD_REQUEST)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
