package com.tawn.tawnht.document;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword) // Category ID có thể là Keyword để lọc
    private Long categoryId;

    @Field(type = FieldType.Keyword) // Category Name có thể là Keyword hoặc Text tùy mục đích tìm kiếm
    private String categoryName;

    @Field(type = FieldType.Keyword) // Seller ID
    private Long sellerId;

    @Field(type = FieldType.Keyword) // Slug
    private String slug;

    @Field(type = FieldType.Keyword) // Cover Image URL
    private String coverImage; // Đã đổi tên từ 'image' thành 'coverImage' cho rõ ràng

    @Field(type = FieldType.Integer)
    private Integer purchase;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Date)
    private LocalDate createdAt;

    @Field(type = FieldType.Text) // Meta Title
    private String metaTitle;

    @Field(type = FieldType.Text) // Meta Description
    private String metaDescription;

    @Field(type = FieldType.Text) // Warranty Info
    private String warrantyInfo;

    @Field(type = FieldType.Boolean) // Active status
    private boolean isActive;

    // Để ánh xạ ProductImageResponse, bạn có thể lưu trữ danh sách các URL ảnh hoặc một DTO đơn giản
    // Lưu ý: Elasticsearch không có khái niệm mối quan hệ như JPA.
    // Bạn thường denormalize dữ liệu hoặc chỉ lưu trữ các trường đơn giản.
    // Ở đây, tôi sẽ giả định bạn muốn lưu trữ danh sách các URL ảnh.
    @Field(type = FieldType.Keyword) // Lưu trữ các URL ảnh dưới dạng Keyword
    private List<String> imageUrls; // Danh sách các URL ảnh

    // Đối với ProductVariants và Attributes, việc denormalize vào ES Document có thể phức tạp.
    // Nếu bạn chỉ cần hiển thị thông tin cơ bản của variant (ví dụ: giá thấp nhất, stock tổng)
    // thì có thể thêm các trường tổng hợp.
    // Nếu bạn cần chi tiết từng variant và thuộc tính, bạn sẽ phải denormalize chúng rất nhiều.
    // Ví dụ đơn giản:
    @Field(type = FieldType.Nested, includeInParent = true) // Sử dụng Nested type cho các đối tượng phức tạp
    private List<ProductVariantDocument> variants; // Nested document cho Product Variants

    // Các trường khác nếu cần cho tìm kiếm hoặc hiển thị
}
