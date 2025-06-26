package com.tawn.tawnht.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttributeResponse {
    Long attributeId;
    String attributeName;
    List<AttributeValueResponse> attributeValue;
}
