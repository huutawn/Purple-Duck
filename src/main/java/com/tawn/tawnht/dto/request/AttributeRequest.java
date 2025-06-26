package com.tawn.tawnht.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
