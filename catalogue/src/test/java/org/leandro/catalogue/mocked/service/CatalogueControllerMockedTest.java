package org.leandro.catalogue.mocked.service;

import com.mongodb.reactivestreams.client.FindPublisher;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import io.reactivex.*;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.leandro.api.v1.model.Product;
import org.leandro.api.v1.model.ProductType;
import org.leandro.catalogue.Application;
import org.leandro.catalogue.integrated.controller.CatalogueController;
import org.leandro.catalogue.integrated.controller.entity.CatalogueEntity;
import org.leandro.catalogue.service.CatalogueService;
import org.leandro.catalogue.service.CatalogueServiceImpl;
import org.leandro.catalogue.util.FriendlyUrl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Flow;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value=MethodOrderer.Alphanumeric.class)
@MicronautTest(application = Application.class)
public class CatalogueControllerMockedTest {

    @Inject
    CatalogueService<CatalogueEntity> catalogueService;

    @Inject
    CatalogueController catalogueController;

    @MockBean(CatalogueServiceImpl.class)
    CatalogueService<CatalogueEntity> catalogueService() {
        return mock(CatalogueService.class);
    }

    @Test
    public void testListProducts() {

        ArgumentCaptor<Subscriber<CatalogueEntity>> argumentCaptor =
                ArgumentCaptor.forClass(Subscriber.class);

        Publisher<CatalogueEntity> publisherMock = mock(FindPublisher.class);

        Flowable.fromPublisher(publisherMock).toList();

        publisherMock.subscribe(TestSubscriber.create());

        when(catalogueService.findAll()).thenReturn(publisherMock);


        verify(publisherMock).subscribe(argumentCaptor.capture());

        assert(argumentCaptor.getValue() instanceof Subscriber);

        catalogueController
                .list()
                .subscribe();
        //assertEquals(0, catalogue.size());

        try {
            when(catalogueService.save(any(CatalogueEntity.class)))
                    .thenThrow(new ConstraintViolationException(anySet()));

            catalogueController.save(new CatalogueEntity("", "", "")).blockingGet();
            fail("Should have thrown a constraint violation");
        } catch (ConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().size(), 1);
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            violations.forEach( constraintViolation -> {
                assertEquals( constraintViolation.getInvalidValue(), "" );
            });
        }
//
//        final CatalogueEntity entity = new CatalogueEntity("Fred", "Harry","photo-1457914109735-ce8aba3b7a79.jpeg")
//                .type(ProductType.UNDEFINED);
//
//        Publisher<CatalogueEntity> findPublisherMock = mock(FindPublisher.class);
//        Flowable.fromPublisher(findPublisherMock).toList();
//
//        findPublisherMock.subscribe(TestSubscriber.create());
//
//        when(catalogueService.find(anyString())).thenReturn(findPublisherMock);
//
//        SingleSource<CatalogueEntity> singleSourceCatalogue =
//            Single.fromPublisher(findPublisherMock);
//
//
//        when(catalogueService.save(any(CatalogueEntity.class)))
//                .thenReturn(singleSourceCatalogue);
//
//        final Product harry = catalogueController.save(entity).blockingGet();
//
//        assertNotNull(harry);
//
//        assertEquals(harry.getDescription(), entity.getDescription());
//        assertEquals(harry.getTitle(), FriendlyUrl.sanitizeWithDashes(entity.getTitle()));
//        assertEquals(harry.getVendor(), entity.getVendor());
//        assertEquals(harry.getType(), entity.getType());
//
//        when(catalogueService.findAll())
//                .then(invocation -> publisherMock);

//        catalogue = catalogueController
//                .list()
//                .blockingGet();
//        assertEquals(catalogue.size(), 1);
//        assertEquals(catalogue.iterator().next().getTitle(), harry.getTitle());

    }

    @Test
    public void testNextFindByVendor() {

        final CatalogueEntity entity = new CatalogueEntity("Fred", "Ron", "photo-1442605527737-ed62b867591f.jpeg")
                .type(ProductType.UNDEFINED);

//        when(catalogueService.save(any(CatalogueEntity.class)))
//                .then( invocation -> singleSourceCatalogue);
//
//        final Product ron = catalogueController.save(entity).blockingGet();
//
//        assertNotNull(ron);
//
//        when(catalogueService.findByVendorName(anyString()))
//                .then( invocation -> singleSourceCatalogue);
//
//        assertEquals(1, catalogueController.byVendor("Fred").blockingGet().size());
    }
}
