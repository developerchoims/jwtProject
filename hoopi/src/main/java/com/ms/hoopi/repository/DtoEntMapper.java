package com.ms.hoopi.repository;

import com.ms.hoopi.model.dto.ApplyDto;
import com.ms.hoopi.model.dto.CompanyDto;
import com.ms.hoopi.model.dto.JobPostingDto;
import com.ms.hoopi.model.entity.Apply;
import com.ms.hoopi.model.entity.Company;
import com.ms.hoopi.model.entity.JobPosting;
import com.ms.hoopi.model.entity.Users;
import com.ms.hoopi.model.dto.UsersDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DtoEntMapper {
    Users toEntity(UsersDto dto);

    UsersDto toDto(Users entity);

    @Mapping(source = "companyDto", target = "company")
    JobPosting toEntity(JobPostingDto dto);
    @Mapping(source = "companyDto", target = "company")
    List<JobPosting> toEntityList(List<JobPostingDto> dtoList);

    @Mapping(source = "company", target = "companyDto")
    JobPostingDto toDto(JobPosting entity);
    @Mapping(source = "company", target = "companyDto")
    List<JobPostingDto> toDtoList(List<JobPosting> entityList);

    Company toEntity(CompanyDto dto);
    List<Company> toCompanyEntityList(List<CompanyDto> dtoList);

    CompanyDto toDto(Company entity);
    List<CompanyDto> toCompanyDtoList(List<Company> entity);

    Apply toEntity(ApplyDto dto);
    ApplyDto toDto(Apply entity);
}