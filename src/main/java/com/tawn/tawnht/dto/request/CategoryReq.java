package com.tawn.tawnht.dto.request;

import com.tawn.tawnht.entity.Order;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryReq {
    String name;
    String url;
}
