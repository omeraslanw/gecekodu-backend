package com.example.gecekodubackend.business.concretes;

import com.example.gecekodubackend.business.abstracts.WorkshopService;
import com.example.gecekodubackend.business.constants.EventMessages;
import com.example.gecekodubackend.business.constants.WorkshopMessages;
import com.example.gecekodubackend.core.utilities.results.*;
import com.example.gecekodubackend.dataAccess.abstracts.WorkshopDao;
import com.example.gecekodubackend.entity.concretes.Workshop;
import com.example.gecekodubackend.entity.dtos.workshop.CreateWorkshopDto;
import com.example.gecekodubackend.entity.dtos.workshop.GetWorkshopDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WorkshopManager implements WorkshopService {

    private final WorkshopDao workshopDao;

    public WorkshopManager(WorkshopDao workshopDao) {
        super();
        this.workshopDao = workshopDao;
    }

    @Override
    public DataResult<List<GetWorkshopDto>> getAllWorkshops() {
        List<Workshop> result = workshopDao.findAll();

        if (result.isEmpty()) {
            return new ErrorDataResult<>(WorkshopMessages.workshopsNotFound);
        }

        var workshopDtoList = new GetWorkshopDto().buildListGetWorkshopDto(result);

        return new SuccessDataResult<>(workshopDtoList, WorkshopMessages.workshopsBroughtSuccessfully);
    }

    @Override
    public DataResult<GetWorkshopDto> getWorkshopById(int id) {
        var workshop = workshopDao.findById(id);

        if (workshop.isEmpty()) {
            return new ErrorDataResult<>(WorkshopMessages.workshopNotFound);
        }

        GetWorkshopDto workshopDto = new GetWorkshopDto();
        BeanUtils.copyProperties(workshop.get(), workshopDto);

        return new SuccessDataResult<>(workshopDto, WorkshopMessages.workshopBroughtSuccessfully);
    }

    @Override
    public Result addWorkshop(CreateWorkshopDto createWorkshopDto) {
        if (workshopDao.existsByWorkshopName(createWorkshopDto.getWorkshopName())) {
            return new ErrorResult(WorkshopMessages.workshopAlreadyExists);
        }

        if (createWorkshopDto.getWorkshopName().isEmpty() || createWorkshopDto.getDate() == null || createWorkshopDto.getDescription().isEmpty()) {
            return new ErrorResult(WorkshopMessages.workshopCouldNotBeAdded);
        }

        Workshop workshop = Workshop.builder()
                .workshopName(createWorkshopDto.getWorkshopName())
                .date(createWorkshopDto.getDate())
                .description(createWorkshopDto.getDescription())
                .build();

        workshopDao.save(workshop);
        return new SuccessResult(WorkshopMessages.workshopAddedSuccessfully);
    }

    @Override
    public Result updateWorkshop(CreateWorkshopDto createWorkshopDto, int id) {

        var result = checkIfTheWorkshopExists(id);

        if (checkIfTheWorkshopExistsByName(createWorkshopDto.getWorkshopName()).isSuccess()) {
            return new ErrorResult(WorkshopMessages.workshopAlreadyExists);
        }
        
        if (createWorkshopDto.getWorkshopName().isEmpty() || createWorkshopDto.getDate() == null || createWorkshopDto.getDescription().isEmpty()) {
            return new ErrorResult(WorkshopMessages.workshopCouldNotBeUpdated);
        }

        if (!result.isSuccess()) {
            return new ErrorResult(WorkshopMessages.workshopNotFound);
        }

        var workshopToUpdate = workshopDao.findById(id).get();
        workshopToUpdate.setWorkshopName(createWorkshopDto.getWorkshopName());
        workshopToUpdate.setDescription(createWorkshopDto.getDescription());
        workshopToUpdate.setDate(createWorkshopDto.getDate());

        workshopDao.save(workshopToUpdate);
        return new SuccessResult(WorkshopMessages.workshopUpdatedSuccessfully);
    }

    @Override
    public Result deleteWorkshop(int id) {
        var workshop = workshopDao.findById(id);

        if (workshop.isEmpty()) {
            return new ErrorResult(WorkshopMessages.workshopNotFound);
        }

        workshopDao.deleteById(id);
        return new SuccessResult(WorkshopMessages.workshopDeletedSuccessfully);
    }

    @Override
    public DataResult<Workshop> getWorkshopEntityById(int id) {
        var result = workshopDao.findById(id);

        if (result.isEmpty()) {
            return new ErrorDataResult<>(EventMessages.eventNotFound);
        }

        return new SuccessDataResult<>(result.get(), EventMessages.eventBroughtSuccessfully);
    }

    @Override
    public DataResult<GetWorkshopDto> getWorkshopByName(String workshopName) {
        var workshop = workshopDao.findWorkshopByWorkshopName(workshopName);

        if (workshop == null) {
            return new ErrorDataResult<>(WorkshopMessages.workshopNotFound);
        }

        GetWorkshopDto workshopDto = new GetWorkshopDto();
        BeanUtils.copyProperties(workshop, workshopDto);

        return new SuccessDataResult<>(workshopDto, WorkshopMessages.workshopBroughtSuccessfully);
    }

    @Override
    public Result updateWorkshopByEntity(Workshop workshop) {
        var result = checkIfTheWorkshopExists(workshop.getWorkshopId());

        if (!result.isSuccess()) {
            return new ErrorResult(WorkshopMessages.workshopNotFound);
        }

        workshopDao.save(workshop);
        return new SuccessResult(WorkshopMessages.workshopUpdatedSuccessfully);
    }

    public Result checkIfTheWorkshopExists(int id) {
        Optional<Workshop> workshop = workshopDao.findById(id);

        if (workshop.isEmpty()) {
            return new ErrorResult(WorkshopMessages.workshopNotFound);
        }

        return new SuccessResult(WorkshopMessages.workshopAlreadyExists);
    }

    public Result checkIfTheWorkshopExistsByName(String eventName) {
        var workshop = workshopDao.findWorkshopByWorkshopName(eventName);

        if (workshop == null) {
            return new ErrorResult(WorkshopMessages.workshopNotFound);
        }

        return new SuccessResult(WorkshopMessages.workshopAlreadyExists);
    }
}
