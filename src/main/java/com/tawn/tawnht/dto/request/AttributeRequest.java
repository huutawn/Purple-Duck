package com.tawn.tawnht.dto.request;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttributeRequest {
    String name;
    String displayName;
    List<AttributeValueRequest> attributeValue;
}
