package com.hms.user.mapper;

import com.hms.user.dto.UserResponseDTO;
import com.hms.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserResponseDTO toResponseDTO(User user);
}
