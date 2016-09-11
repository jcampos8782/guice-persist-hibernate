package me.jasoncampos.inject.persist.hibernate.integrator;

import javax.inject.Inject;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import me.jasoncampos.inject.persist.hibernate.integrator.AutoRegisteringListener.RegistrationType;

/**
 * Hibernate {@link Integrator} which dynamically binds Listeners with the {@code EventType EventTypes} to which they
 * should subscribe. Any existing listener implementation may be decorated with the {@link AutoRegisteringListener}
 * in order to utilize auto-registration.
 *
 * @author Jason Campos <jcampos8782@gmail.com>
 */
public class AutoRegisteringListenerIntegrator implements Integrator {
	private static final Logger logger = LoggerFactory.getLogger(AutoRegisteringListenerIntegrator.class);
	private final ImmutableSet<? extends AutoRegisteringListener> listeners;

	@Inject
	public AutoRegisteringListenerIntegrator(final ImmutableSet<? extends AutoRegisteringListener> listeners) {
		this.listeners = listeners;
	}

	@Override
	public void integrate(
			final Metadata metadata,
			final SessionFactoryImplementor sessionFactory,
			final SessionFactoryServiceRegistry serviceRegistry) {
		final EventListenerRegistry eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);
		for (final AutoRegisteringListener listener : listeners) {
			listener.getEventType().stream().forEach(type -> doRegister(eventListenerRegistry, type, listener));
		}
	}

	@Override
	public void disintegrate(final SessionFactoryImplementor sessionFactory, final SessionFactoryServiceRegistry serviceRegistry) {
		//
	}

	private <T> void doRegister(final EventListenerRegistry registry, final EventType<T> type, final AutoRegisteringListener listener) {
		for (final RegistrationType registrationType : listener.getEventRegistrations()) {
			switch (registrationType) {
			case REPLACE:
				replaceListener(registry, type, listener);
				break;
			case APPEND:
				appendListener(registry, type, listener);
				break;
			case PREPEND:
				prependListener(registry, type, listener);
				break;
			default:
				throw new IllegalArgumentException("Unknown registration type " + registrationType.toString());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void replaceListener(final EventListenerRegistry registry, final EventType<T> type, final AutoRegisteringListener listener) {
		try {
			logger.debug(String.format("Registering listener %s: %s", type.eventName(), listener.getClass()));
			registry.setListeners(type, (T) listener);
		} catch (final ClassCastException e) {
			logFailure(type, listener);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void appendListener(final EventListenerRegistry registry, final EventType<T> type, final AutoRegisteringListener listener) {
		try {
			logger.debug(String.format("Registering listener %s: %s", type.eventName(), listener.getClass()));
			registry.appendListeners(type, (T) listener);
		} catch (final ClassCastException e) {
			logFailure(type, listener);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void prependListener(final EventListenerRegistry registry, final EventType<T> type, final AutoRegisteringListener listener) {
		try {
			logger.debug(String.format("Registering listener %s: %s", type.eventName(), listener.getClass()));
			registry.prependListeners(type, (T) listener);
		} catch (final ClassCastException e) {
			logFailure(type, listener);
		}
	}

	private void logFailure(final EventType<?> type, final AutoRegisteringListener listener) {
		logger.warn(String
				.format("Failed to register event listener %s for event type %s. Listener does not implement required interface %s.",
						listener.getClass(),
						type.eventName(),
						type.baseListenerInterface()));
	}
}
