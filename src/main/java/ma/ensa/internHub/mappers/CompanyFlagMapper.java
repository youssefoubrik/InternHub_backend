package ma.ensa.internHub.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ma.ensa.internHub.domain.dto.response.CompanyFlagResponse;
import ma.ensa.internHub.domain.entities.CompanyFlag;

@Mapper(componentModel = "spring")
public interface CompanyFlagMapper {
    @Mapping(target = "companyId", source = "flaggedCompany.id")
    @Mapping(target = "companyEmail", source = "flaggedCompany.email")
    @Mapping(target = "companyName", source = "flaggedCompany.name")
    @Mapping(target = "studentId", source = "flaggedByStudent.id")
    @Mapping(target = "studentEmail", source = "flaggedByStudent.email")
    @Mapping(target = "studentFirstName", source = "flaggedByStudent.firstName")
    @Mapping(target = "studentLastName", source = "flaggedByStudent.lastName")
    @Mapping(target = "reporterCompanyId", source = "flaggedByCompany.id")
    @Mapping(target = "reporterCompanyName", source = "flaggedByCompany.name")

    CompanyFlagResponse toResponse(CompanyFlag company);
}
