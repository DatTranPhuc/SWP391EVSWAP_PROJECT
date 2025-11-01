package evswap.swp391to4.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import evswap.swp391to4.entity.VehicleType;

@Converter(autoApply = true)
public class VehicleTypeConverter implements AttributeConverter<VehicleType, String> {

    @Override
    public String convertToDatabaseColumn(VehicleType attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public VehicleType convertToEntityAttribute(String dbData) {
        return VehicleType.fromString(dbData);
    }
}
