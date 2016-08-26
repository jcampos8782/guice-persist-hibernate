package me.jasoncampos.inject.persist.hibernate;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.persist.Transactional;

/**
 * THIS IS A COPY/PASTE ADAPTATION OF THE JPA TX INTERCEPTOR PACKAGED WITH GUICE-PERSIST.
 * Minor changes were made to use Hibernate's UnitOfWork implementation (literally a one line change).
 * Note: Would be nice to interface this to work with any UnitOfWork implementation or some adapter interface
 * which provides the isWorking() method.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Jason Campos (jcampos8782@gmail.com)
 */
public class HibernateTransactionInterceptor implements MethodInterceptor {

	@Inject
	HibernateUnitOfWork unitOfWork = null;

	@Transactional
	private static class Internal {
	}

	// Tracks if the unit of work was begun implicitly by this transaction.
	private final ThreadLocal<Boolean> didWeStartWork = new ThreadLocal<>();

	@Override
	public Object invoke(final MethodInvocation methodInvocation) throws Throwable {

		// Should we start a unit of work?
		if (!unitOfWork.isWorking()) {
			unitOfWork.begin();
			didWeStartWork.set(true);
		}

		final Transactional transactional = readTransactionMetadata(methodInvocation);
		final EntityManager em = unitOfWork.get();

		// Allow 'joining' of transactions if there is an enclosing
		// @Transactional method.
		if (em.getTransaction().isActive()) {
			return methodInvocation.proceed();
		}

		final EntityTransaction txn = em.getTransaction();
		txn.begin();

		Object result;
		try {
			result = methodInvocation.proceed();

		} catch (final Exception e) {
			// commit transaction only if rollback didnt occur
			if (rollbackIfNecessary(transactional, e, txn)) {
				txn.commit();
			}

			// propagate whatever exception is thrown anyway
			throw e;
		} finally {
			// Close the em if necessary (guarded so this code doesn't run
			// unless catch fired).
			if (null != didWeStartWork.get() && !txn.isActive()) {
				didWeStartWork.remove();
				unitOfWork.end();
			}
		}

		// everything was normal so commit the txn (do not move into try block
		// above as it
		// interferes with the advised method's throwing semantics)
		try {
			txn.commit();
		} finally {
			// close the em if necessary
			if (null != didWeStartWork.get()) {
				didWeStartWork.remove();
				unitOfWork.end();
			}
		}

		// or return result
		return result;
	}

	// TODO(dhanji): Cache this method's results.
	private Transactional readTransactionMetadata(final MethodInvocation methodInvocation) {
		Transactional transactional;
		final Method method = methodInvocation.getMethod();
		final Class<?> targetClass = methodInvocation.getThis().getClass();

		transactional = method.getAnnotation(Transactional.class);
		if (null == transactional) {
			// If none on method, try the class.
			transactional = targetClass.getAnnotation(Transactional.class);
		}
		if (null == transactional) {
			// If there is no transactional annotation present, use the default
			transactional = Internal.class.getAnnotation(Transactional.class);
		}

		return transactional;
	}

	/**
	 * Returns True if rollback DID NOT HAPPEN (i.e. if commit should continue).
	 *
	 * @param transactional
	 *            The metadata annotaiton of the method
	 * @param e
	 *            The exception to test for rollback
	 * @param txn
	 *            A JPA Transaction to issue rollbacks on
	 */
	private boolean rollbackIfNecessary(final Transactional transactional, final Exception e,
			final EntityTransaction txn) {
		boolean commit = true;

		// check rollback clauses
		for (final Class<? extends Exception> rollBackOn : transactional.rollbackOn()) {

			// if one matched, try to perform a rollback
			if (rollBackOn.isInstance(e)) {
				commit = false;

				// check ignore clauses (supercedes rollback clause)
				for (final Class<? extends Exception> exceptOn : transactional.ignore()) {
					// An exception to the rollback clause was found, DON'T
					// rollback
					// (i.e. commit and throw anyway)
					if (exceptOn.isInstance(e)) {
						commit = true;
						break;
					}
				}

				// rollback only if nothing matched the ignore check
				if (!commit) {
					txn.rollback();
				}
				// otherwise continue to commit

				break;
			}
		}

		return commit;
	}
}
