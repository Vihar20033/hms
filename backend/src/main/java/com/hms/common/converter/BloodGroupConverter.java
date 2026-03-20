package com.hms.common.converter;

import com.hms.common.enums.BloodGroup;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BloodGroupConverter implements AttributeConverter<BloodGroup, String> {

    @Override
    public String convertToDatabaseColumn(BloodGroup attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getLabel();
    }

    @Override
    public BloodGroup convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (BloodGroup bg : BloodGroup.values()) {
            if (bg.getLabel().equalsIgnoreCase(dbData) || bg.name().equalsIgnoreCase(dbData)) {
                return bg;
            }
        }
        return null; // Or throw exception
    }
}
