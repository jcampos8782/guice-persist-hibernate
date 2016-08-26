package me.jasoncampos.inject.persist.hibernate;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.common.base.Preconditions;
import com.google.inject.persist.UnitOfWork;

/**
 * Hibernate {@code UnitOfWork} implementation which manages a ThreadLocal {@code Session} instance. Invoking
 * {@link #begin()} will open a new {@code Session} if one is not already open. Subsequent invocations of {@link #get()}
 * will return the same {@code Session} instance until {@link #end()} is invoked. <br />
 * <br />
 *
 * This class may also be used as a {@code Provider<Session>} without the {@code UnitOfWork} context. Note that any
 * session not created during a {@link #begin()} invocation *must* be manually closed. Invoking {@link #get()} and then
 * {@link #end()} will *not* close the session created from {@link #get()}.
 *
 * @author Jason Campos <jcmapos8782@gmail.com>
 */
@Singleton
public class HibernateUnitOfWork implements Provider<Session>, UnitOfWork {

	private final ThreadLocal<Session> sessions = new ThreadLocal<>();
	private final Provider<SessionFactory> sessionFactory;

	@Inject
	public HibernateUnitOfWork(final Provider<SessionFactory> sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Session get() {
		// If a unit of work has begun, return that session.
		// Otherwise, assume the session will be manually managed.
		if (isWorking()) {
			return sessions.get();
		}

		return sessionFactory.get().openSession();
	}

	public boolean isWorking() {
		return sessions.get() != null;
	}

	@Override
	public void begin() {
		if (!isWorking()) {
			sessions.set(get());
		}
	}

	@Override
	public void end() {
		Preconditions.checkState(isWorking(), "UnitOfWork.end() invoked with no corresponding UnitOfWork.begin()");
		final Session session = sessions.get();
		try {
			if (session.isOpen()) {
				session.flush();
			}
		} finally {
			if (session.isOpen()) {
				session.close();
			}
			sessions.remove();
		}
	}
}
