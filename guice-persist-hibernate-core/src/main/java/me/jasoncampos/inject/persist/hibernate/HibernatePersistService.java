package me.jasoncampos.inject.persist.hibernate;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.persist.PersistService;

/**
 * Hibernate {@link PersistService} implementation which manages a singleton {@code SessionFactory}.
 *
 * @author Jason Campos <jcampos8782@gmail.com>
 */
@Singleton
public class HibernatePersistService implements Provider<SessionFactory>, PersistService {
	private static final Logger logger = LoggerFactory.getLogger(HibernatePersistService.class);

	private volatile SessionFactory sessionFactory;
	private volatile boolean started;
	private final Configuration configuration;
	private final BootstrapServiceRegistry bootstrapServiceRegistry;

	@Inject
	public HibernatePersistService(final BootstrapServiceRegistry bootstrapServiceRegistry, final Configuration configuration) {
		this.configuration = configuration;
		this.bootstrapServiceRegistry = bootstrapServiceRegistry;
	}

	@Override
	public SessionFactory get() {
		if (!started) {
			throw new IllegalStateException("HibernatePersistService has not been started or has been stopped.");
		}
		return sessionFactory;
	}

	@Override
	public void start() {
		logger.info("Starting HibernatePersistService");
		final ServiceRegistry registry = new StandardServiceRegistryBuilder(bootstrapServiceRegistry)
				.applySettings(configuration.getProperties())
				.build();

		this.sessionFactory = configuration.buildSessionFactory(registry);
		logger.info("HibernatePersistServiceStarted");
		started = true;
	}

	@Override
	public void stop() {
		logger.info("Stopping HibernatePersistService");
		sessionFactory.close();
		logger.info("HibernatePersistService stopped");
	}
}
