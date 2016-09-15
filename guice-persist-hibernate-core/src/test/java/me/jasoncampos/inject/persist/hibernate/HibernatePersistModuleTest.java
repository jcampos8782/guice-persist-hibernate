package me.jasoncampos.inject.persist.hibernate;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

public class HibernatePersistModuleTest {

	private Injector injector;

	@Before
	public void beforeEach() {
		injector = Guice.createInjector(new HibernatePersistModule(TestEntityProvider.class, TestPropertyProvider.class));
	}

	@Test(expected = CreationException.class)
	public void failsToIntializeWithoutRequiredProviders() {
		Guice.createInjector(new HibernatePersistModule());
	}

	@Test
	public void initializationSucceedsUsingProviderConstructor() {
		Guice.createInjector(new HibernatePersistModule(TestEntityProvider.class, TestPropertyProvider.class));
	}

	@Test
	public void intializationSucceedsUsingAdditionalModule() {
		Guice.createInjector(new HibernatePersistModule(), new AbstractModule() {
			@Override
			protected void configure() {
				bind(HibernateEntityClassProvider.class).to(TestEntityProvider.class);
				bind(HibernatePropertyProvider.class).to(TestPropertyProvider.class);
			}
		});
	}

	@Test
	public void testPersistServiceAndSessionFactoryProviderAreSingleton() {
		final HibernatePersistService persistService = (HibernatePersistService) injector.getInstance(PersistService.class);
		final HibernatePersistService hibernatePersistService = injector.getInstance(HibernatePersistService.class);
		;
		assertEquals(persistService, hibernatePersistService);
	}

	@Test
	public void testUnitOfWorkAndSessionProviderAreSingleton() {
		final HibernateUnitOfWork unitOfWork = (HibernateUnitOfWork) injector.getInstance(UnitOfWork.class);
		final HibernateUnitOfWork hibernateUnitOfWork = injector.getInstance(HibernateUnitOfWork.class);
		assertEquals(unitOfWork, hibernateUnitOfWork);
	}

	@Entity
	private static class TestEntity {
		@Id
		long id;
	}

	private static class TestEntityProvider implements HibernateEntityClassProvider {
		@Override
		public List<Class<? extends Object>> get() {
			return Arrays.asList(TestEntity.class);
		}
	}

	private static class TestPropertyProvider implements HibernatePropertyProvider {
		@Override
		public Map<String, String> get() {
			return Collections.emptyMap();
		}
	}
}
