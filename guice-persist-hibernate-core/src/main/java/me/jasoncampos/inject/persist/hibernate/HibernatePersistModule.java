package me.jasoncampos.inject.persist.hibernate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.integrator.spi.Integrator;

import com.google.inject.Provides;
import com.google.inject.persist.PersistModule;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.UnitOfWork;

/**
 * Adaptation of the {@link PersistModule} specific to using Hibernate via a {@code SessionFactory} as opposed to the
 * JPA {@code EntityManagerFactory}. This allows for runtime binding of configuration properties (using a
 * {@link HibernatePropertyProvider} and programmatic configuration of {@code @Entity} classes (Yay! No XML!).
 * <br />
 * <br />
 * Users of this module *must* either<br />
 * a) Provide {@link HibernateEntityClassProvider} and {@link HibernatePropertyProvider} classes to the
 * constructor<br />
 * OR<br/>
 * b) Provider bindings for those classes via an installed Guice module
 *
 * @author Jason Campos <jcampos8782@gmail.com>
 */
public class HibernatePersistModule extends PersistModule {

	private final Class<? extends HibernateEntityClassProvider> entityClassProvider;
	private final Class<? extends HibernatePropertyProvider> hibernatePropertyProvider;
	private final Set<Integrator> integrators = new HashSet<>();

	/**
	 * Instantiates module without specifying the {@link #hibernatePropertyProvider} or {@link #entityClassProvider}.
	 * These bindings *must* be set in order for this module to function properly.
	 */
	public HibernatePersistModule() {
		this.entityClassProvider = null;
		this.hibernatePropertyProvider = null;
	}

	/**
	 * Instantiates module with the specified entity class and property providers. No additional bindings required for
	 * this module to function properly.
	 *
	 * @param entityClassProvider
	 * @param hibernatePropertyProvider
	 */
	public HibernatePersistModule(
			final Class<? extends HibernateEntityClassProvider> entityClassProvider,
			final Class<? extends HibernatePropertyProvider> hibernatePropertyProvider) {

		this.entityClassProvider = entityClassProvider;
		this.hibernatePropertyProvider = hibernatePropertyProvider;
	}

	@Override
	protected void configurePersistence() {
		requireBinding(HibernateEntityClassProvider.class);
		requireBinding(HibernatePropertyProvider.class);

		if (entityClassProvider != null) {
			bind(HibernateEntityClassProvider.class).to(entityClassProvider);
		}

		if (hibernatePropertyProvider != null) {
			bind(HibernatePropertyProvider.class).to(hibernatePropertyProvider);
		}

		// Resolve PersistService, Provider<SessionFactory>, and HibernatePersistService
		// to the same object.
		bind(HibernatePersistService.class).in(Singleton.class);
		bind(PersistService.class).to(HibernatePersistService.class);
		bind(SessionFactory.class).toProvider(HibernatePersistService.class);

		// Resolve UnitOfWork, Provider<Session>,and HibernateUnitOfWork
		// to the same object.
		bind(HibernateUnitOfWork.class).in(Singleton.class);
		bind(UnitOfWork.class).to(HibernateUnitOfWork.class);
		bind(Session.class).toProvider(HibernateUnitOfWork.class);

		// Since Session implements EntityManager, bind EntityManager as well in case the user would rather use JPA
		// classes
		bind(EntityManager.class).toProvider(HibernateSessionEntityManagerAdapter.class);
	}

	public void addIntegrator(final Integrator integrator) {
		integrators.add(integrator);
	}

	public void addIntegrators(final Collection<Integrator> integrators) {
		this.integrators.addAll(integrators);
	}

	@Override
	protected MethodInterceptor getTransactionInterceptor() {
		final MethodInterceptor txInterceptor = new HibernateTransactionInterceptor();
		requestInjection(txInterceptor);
		return txInterceptor;
	}

	@Inject
	@Provides
	private Configuration getHibernateConfiguration(
			final HibernateEntityClassProvider entityClassProvider,
			final HibernatePropertyProvider hibernatePropertyProvider) {
		final Configuration configuration = new Configuration();
		hibernatePropertyProvider.get().forEach((key, value) -> configuration.setProperty(key, value));
		entityClassProvider.get().forEach(entityClass -> configuration.addAnnotatedClass(entityClass));
		return configuration;
	}

	@Provides
	@Singleton
	private BootstrapServiceRegistry getBootstrapServiceRegistry() {
		final BootstrapServiceRegistryBuilder builder = new BootstrapServiceRegistryBuilder();

		for (final Integrator integrator : integrators) {
			builder.applyIntegrator(integrator);
		}

		return builder.build();
	}
}
