package org.leandro.catalogue.integrated.dynamodb.service;


import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.Flowable;
import io.reactivex.internal.operators.single.SingleDetach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.leandro.api.v1.model.Product;
import org.leandro.api.v1.model.ProductType;
import org.leandro.catalogue.Application;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;
import org.leandro.catalogue.integrated.dynamodb.LocalDynamoDbExtension;
import org.leandro.catalogue.service.CatalogueService;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value= MethodOrderer.MethodName.class)
@MicronautTest(application = Application.class)
@ExtendWith(LocalDynamoDbExtension.class)
public class CatalogueServiceTest {

    @Inject
    ApplicationContext applicationContext;

    @Inject
    CatalogueService<CatalogueEntity> catalogueService;

    @Test
    public void testListProducts() {

        List<CatalogueEntity> catalogue =  Flowable.fromPublisher(
                catalogueService.findAll()
        ).blockingSingle();

        assertEquals(0, catalogue.size());

        try {
            CatalogueEntity catalogueEntity = new SingleDetach<>(
                    catalogueService.save(new CatalogueEntity("", "", ""))).blockingGet();
            fail("Should have thrown a constraint violation");
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().size(), 1);
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            violations.forEach( constraintViolation -> {
                assertEquals( constraintViolation.getInvalidValue(), "" );
            });
        }

        final CatalogueEntity entity = new CatalogueEntity("Fred", "Harry","photo-1457914109735-ce8aba3b7a79.jpeg")
                .type(ProductType.UNDEFINED);
        final Product harry = new SingleDetach<>(catalogueService.save(entity)).blockingGet();
        assertNotNull(harry);

        assertEquals(harry.getDescription(), entity.getDescription());
        assertEquals(harry.getTitle(), entity.getTitle());
        assertEquals(harry.getVendor(), entity.getVendor());
        assertEquals(harry.getType(), entity.getType());

        catalogue = Flowable.fromPublisher(catalogueService
                .findAll()).blockingSingle();

        assertEquals(catalogue.size(), 1);
        assertEquals(catalogue.iterator().next().getTitle(), harry.getTitle());
    }

    @Test
    public void testNextFindByVendor() {

        final CatalogueEntity entity = new CatalogueEntity("Fred", "Ron", "photo-1442605527737-ed62b867591f.jpeg")
                .type(ProductType.UNDEFINED);

        final Product ron = new SingleDetach<>(catalogueService.save(entity)).blockingGet();

        assertNotNull(ron);

        assertEquals(1, Flowable.fromPublisher(catalogueService.findByVendorName("Fred")).toList().blockingGet().size());
    }
}
