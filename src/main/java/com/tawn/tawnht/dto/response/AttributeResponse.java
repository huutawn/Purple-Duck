package com.tawn.tawnht.dto.response;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
