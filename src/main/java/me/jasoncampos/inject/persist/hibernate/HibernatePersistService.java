package me.jasoncampos.inject.persist.hibernate;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.google.inject.persist.PersistService;

/**
 * Hibernate {@link PersistService} implementation which manages a singleton {@code SessionFactory}.
 * 
 * @author Jason Campos <jcmapos8782@gmail.com>
 */
@Singleton
public class HibernatePersistService implements Provider<SessionFactory>, PersistService {

	private SessionFactory sessionFactory;
	private final Configuration configuration;

	@Inject
	public HibernatePersistService(final Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public SessionFactory get() {
		return sessionFactory;
	}

	@Override
	public void start() {
		final ServiceRegistry registry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();

		this.sessionFactory = configuration.buildSessionFactory(registry);
	}

	@Override
	public void stop() {
		sessionFactory.close();
	}
}
