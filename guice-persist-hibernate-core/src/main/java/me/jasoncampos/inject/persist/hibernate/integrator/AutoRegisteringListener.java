package me.jasoncampos.inject.persist.hibernate.integrator;

import org.hibernate.event.spi.EventType;

import com.google.common.collect.ImmutableSet;

/**
 * Use with {@link AutoRegisteringListenerIntegrator} to enable dynamic runtime bootstrapping of hibernate event
 * listeners. Listeners listen for specific {@link EventType EventTypes} and must implement specific interfaces in order
 * to listen for those events. For example, a listener which listens for {@code EventType.POST_COMMIT_INSERT} must
 * implement the {@code PostCommitInsertEventListener} interface. Failure to implement the correct interfaces is *not*
 * fatal but will result in the listener not being registered with Hibernate.
 *
 * @author Jason Campos <jcampos8782@gmail.com>
 */
public interface AutoRegisteringListener {

	/**
	 * Listeners can either replace the existing event listeners for a particular {@code EventType} or be
	 * prepended/appended (or both) to the listener chain for the specified {@code EventType}.
	 */
	public enum EventRegistration {
		REPLACE, PREPEND, APPEND
	};

	/**
	 * @return The {@link EventType EventTypes} this listener should subscribe to.
	 */
	ImmutableSet<EventType<?>> getEventType();

	/**
	 * @return A set of instructions on how to register this listener. Note that if both {@code REPLACE} and
	 *         {@code PREPEND/APPEND} are both specified, behavior is non-deterministic.
	 */
	ImmutableSet<EventRegistration> getEventRegistrations();
}
