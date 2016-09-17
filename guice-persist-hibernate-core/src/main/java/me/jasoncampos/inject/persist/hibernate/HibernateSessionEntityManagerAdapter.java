package me.jasoncampos.inject.persist.hibernate;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import org.hibernate.Session;

/**
 * Simple adapter which uses an injected {@code Provider<Session>} to return the provided {@code Session} objects as an
 * {@code EntityManager}.
 *
 * @author Jason Campos <jcampos8782@gmail.com>
 */
public class HibernateSessionEntityManagerAdapter implements Provider<EntityManager> {

	private final Provider<Session> sessionProvider;

	@Inject
	public HibernateSessionEntityManagerAdapter(final Provider<Session> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}

	@Override
	public EntityManager get() {
		return sessionProvider.get();
	}
}
