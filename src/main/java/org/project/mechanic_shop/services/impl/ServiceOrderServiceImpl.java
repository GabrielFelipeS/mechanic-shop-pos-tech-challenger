//package org.project.mechanic_shop.services.impl;
//
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.project.mechanic_shop.dto.service_order_dto.ServiceOrderManDto;
//import org.project.mechanic_shop.models.ServiceOrder;
//import org.project.mechanic_shop.models.ServiceOrderLabor;
//import org.project.mechanic_shop.models.ServiceOrderPart;
//import org.project.mechanic_shop.models.Vehicle;
//import org.project.mechanic_shop.models.enums.ServiceOrderStatusEnum;
//import org.project.mechanic_shop.repositories.*;
//import org.project.mechanic_shop.services.ServiceOrderService;
//import org.springframework.data.domain.Example;
//import org.springframework.data.domain.ExampleMatcher;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ServiceOrderServiceImpl implements ServiceOrderService {
//
//    private final ServiceOrderRepository repository;
//    private final PartRepository partRepository;
//    private final MechanicServiceRepository laborRepository;
//    private final VehicleRepository vehicleRepository;
//    private final UserRepository userRepository;
//
//    @Override
//    @Transactional
//    public ServiceOrder create(ServiceOrderManDto dto) {
//        log.info("Starting creation of Service Order for vehicle: {}", dto.vehicleExternalId());
//
//        ServiceOrder so = new ServiceOrder();
//
//        var vehicle = vehicleRepository.findByExternalId(dto.vehicleExternalId())
//                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
//        so.setVehicle(vehicle);
//        so.setCustomerComplaint(dto.customerComplaint());
//        so.setOdometerReading(dto.odometerReading());
//
//        if (dto.mechanicExternalId() != null) {
//            var mechanic = userRepository.findByExternalId(dto.mechanicExternalId())
//                    .orElseThrow(() -> new EntityNotFoundException("Mechanic not found"));
//            so.setResponsibleMechanic(mechanic);
//        }
//
//        BigDecimal totalParts = BigDecimal.ZERO;
//        for (var partDto : dto.parts()) {
//            var part = partRepository.findByExternalId(partDto.partExternalId())
//                    .orElseThrow(() -> new EntityNotFoundException("Part not found: " + partDto.partExternalId()));
//
//            ServiceOrderPart soPart = new ServiceOrderPart();
//            soPart.setPart(part);
//            soPart.setQuantity(partDto.quantity());
//            soPart.setUnitPrice(part.getSalePrice());
//            soPart.setTotalPrice(part.getSalePrice().multiply(BigDecimal.valueOf(partDto.quantity())));
//
//            so.addPart(soPart);
//            totalParts = totalParts.add(soPart.getTotalPrice());
//        }
//
//        BigDecimal totalLabor = BigDecimal.ZERO;
//        for (var laborDto : dto.labors()) {
//            var labor = laborRepository.findByExternalId(laborDto.mechanicServiceExternalId())
//                    .orElseThrow(() -> new EntityNotFoundException("Labor service not found"));
//
//            ServiceOrderLabor soLabor = new ServiceOrderLabor();
//            soLabor.setMechanicService(labor);
//            soLabor.setQuantity(laborDto.quantity());
//            soLabor.setUnitPrice(labor.getPrice());
//            soLabor.setTotalPrice(labor.getPrice().multiply(BigDecimal.valueOf(laborDto.quantity())));
//
//            so.addLabor(soLabor);
//            totalLabor = totalLabor.add(soLabor.getTotalPrice());
//        }
//
//        so.setTotalAmount(totalParts.add(totalLabor));
//        so.setStatus(ServiceOrderStatusEnum.DRAFT);
//
//        return repository.save(so);
//    }
//
//
//    @Override
//    @Transactional(readOnly = true)
//    public ServiceOrder findByExternalId(UUID externalId) {
//        return repository.findByExternalId(externalId)
//                .orElseThrow(() -> new EntityNotFoundException("Service Order not found"));
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public Page<ServiceOrder> search(String licensePlate, ServiceOrderStatusEnum status, Pageable pageable) {
//        log.info("Searching OS with filters - Plate: {}, Status: {}", licensePlate, status);
//
//
//        var so = new ServiceOrder();
//        so.setStatus(status);
//        if (licensePlate != null) {
//            var vehicle = new Vehicle();
//            vehicle.setLicensePlate(licensePlate);
//            so.setVehicle(vehicle);
//        }
//
//        ExampleMatcher matcher = ExampleMatcher.matching()
//                .withIgnorePaths("id", "externalId", "totalAmount", "createdAt", "lastUpdatedAt")
//                .withIgnoreNullValues();
//
//        return repository.findAll(Example.of(so, matcher), pageable);
//    }
//
//    @Override
//    @Transactional
//    public ServiceOrder updateStatus(UUID id, ServiceOrderStatusEnum newStatus) {
//        var serviceOrder = repository.findByExternalId(id);
//
//        var so  =  serviceOrder.get();
//        log.info("Updating OS {} status from {} to {}", id, so.getStatus(), newStatus);
//
//        if (so.getStatus() == ServiceOrderStatusEnum.REJECTED || so.getStatus() == ServiceOrderStatusEnum.COMPLETED) {
//            throw new IllegalStateException("Cannot change status of a finalized or rejected Service Order.");
//        }
//
//        if (newStatus == ServiceOrderStatusEnum.APPROVED) {
//            so.setApprovalDate(LocalDateTime.now());
//        } else if (newStatus == ServiceOrderStatusEnum.COMPLETED) {
//            so.setCompletionDate(LocalDateTime.now());
//        }
//
//        so.setStatus(newStatus);
//        return repository.save(so);
//    }
//}
