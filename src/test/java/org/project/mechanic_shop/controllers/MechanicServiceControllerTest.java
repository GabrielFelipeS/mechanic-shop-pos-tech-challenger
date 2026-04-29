package org.project.mechanic_shop.controllers;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.project.mechanic_shop.config.ObjectMapperConfig;
import org.project.mechanic_shop.config.SecurityConfig;
import org.project.mechanic_shop.mappers.MechanicServiceMapperImpl;
import org.project.mechanic_shop.models.MechanicService;
import org.project.mechanic_shop.services.MechanicServiceService;
import org.project.mechanic_shop.utils.MechanicServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO FAZER TESTES PARA VALIDAR O @VALID
@WebMvcTest(MechanicServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SecurityConfig.class, MechanicServiceMapperImpl.class,  ObjectMapperConfig.class})
class MechanicServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MechanicServiceService service;

    @Nested
    class FindById {
        @Test
        void shouldFindUserByExternalId() throws Exception {
            var externalId = UUID.randomUUID();
            var mechanic = MechanicServiceHelper.generateMechanicService();

            when(service.findByExternalId(externalId)).thenReturn(mechanic);

            mockMvc.perform(get(String.format("/api/mechanic-services/%s", externalId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.name").value("Troca de óleo"));
        }

        @Test
        void shouldNotFoundUserByExternalId() throws Exception {
            var externalId = UUID.randomUUID();

            when(service.findByExternalId(externalId)).thenThrow(EntityNotFoundException.class);

            mockMvc.perform(get(String.format("/api/mechanic-services/%s", externalId)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Create {
        @Test
        void shouldBeCreateUser() throws Exception {
            var mechanic = MechanicServiceHelper.generateMechanicService();

            var dto = MechanicServiceHelper.generateMechanicServiceShortDto();

            when(service.create(any(MechanicService.class))).thenReturn(mechanic);

            mockMvc.perform(
                    post("/api/mechanic-services/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value( HttpStatus.CREATED.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data").value(mechanic.getExternalId()));
        }

        @Test
        void shouldThrowIllegalArgumentException() throws Exception {
            var dto = MechanicServiceHelper.generateMechanicServiceShortDto();

            when(service.create(any(MechanicService.class))).thenThrow(IllegalArgumentException.class);

            mockMvc.perform(
                            post("/api/mechanic-services/create")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(dto))
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value( HttpStatus.CONFLICT.value()))
            ;
        }
    }

    @Nested
    class Update {
        @Test
        void shouldBeAbleUpdateUser() throws Exception {
            var externalId = UUID.randomUUID();
            var user = MechanicServiceHelper.generateMechanicService();

            var userDto = MechanicServiceHelper.generateMechanicServiceShortDto();

            when(service.update(eq(externalId), any(MechanicService.class))).thenReturn(user);

            mockMvc.perform(
                            put("/api/mechanic-services/{id}", externalId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(userDto))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value( HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"));

            verify(service).update(eq(externalId), any(MechanicService.class));
        }

        @Test
        void shouldThrowIllegalArgumentException() throws Exception {
            var externalId = UUID.randomUUID();

            var userDto = MechanicServiceHelper.generateMechanicServiceShortDto();

            when(service.update(eq(externalId), any(MechanicService.class))).thenThrow(IllegalArgumentException.class);

            mockMvc.perform(
                            put("/api/mechanic-services/{id}", externalId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(userDto))
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value( HttpStatus.CONFLICT.value()));

        }
    }

    @Nested
    class Search {
        @Test
        void shouldSearchWithPageable() throws Exception {
            var mechanic = MechanicServiceHelper.generateMechanicService();
            var listMechanic = List.of(mechanic);
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

            Page<MechanicService> page = new PageImpl<>(
                    listMechanic,
                    PageRequest.of(0, 2),
                    1
            );

            when(service.search(
                    eq(mechanic.getName()),
                    any(Pageable.class)
            )).thenReturn(page);

            mockMvc.perform(
                            get("/api/mechanic-services/search")
                                    .param("name", mechanic.getName())
                                    .param("page", "0")
                                    .param("size", "10")
                                    .param("sort", "name,asc")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value( HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].name").value(mechanic.getName()))
            ;

            verify(service).search(
                    eq(mechanic.getName()),
                    captor.capture()
            );

            Pageable pageableCaptured = captor.getValue();

            assertThat(pageableCaptured.getPageNumber()).isZero();
            assertThat(pageableCaptured.getPageSize()).isEqualTo(10);
            assertThat(pageableCaptured.getSort().getOrderFor("name").isAscending()).isTrue();
        }

        @Test
        void shouldSearchWithPageableDefault() throws Exception {
            var mechanic = MechanicServiceHelper.generateMechanicService();
            var listMechanic = List.of(mechanic);
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

            Page<MechanicService> page = new PageImpl<>(
                    listMechanic,
                    PageRequest.of(0, 2),
                    1
            );

            when(service.search(
                    eq(mechanic.getName()),
                    any(Pageable.class)
            )).thenReturn(page);

            mockMvc.perform(
                            get("/api/mechanic-services/search")
                                    .param("name", mechanic.getName())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value( HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].name").value(mechanic.getName()))
            ;

            verify(service).search(
                    eq(mechanic.getName()),
                    captor.capture()
            );

            Pageable pageableCaptured = captor.getValue();
            pageableCaptured.getSort().forEach(System.out::println);
            assertThat(pageableCaptured.getPageNumber()).isZero();
            assertThat(pageableCaptured.getPageSize()).isEqualTo(10);
            assertThat(pageableCaptured.getSort().getOrderFor("createdAt").isDescending()).isTrue();
        }
    }
}