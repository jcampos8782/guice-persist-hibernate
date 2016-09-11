package me.jasoncampos.inject.persist.hibernate.integrator;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.ClearEvent;
import org.hibernate.event.spi.ClearEventListener;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.DirtyCheckEvent;
import org.hibernate.event.spi.DirtyCheckEventListener;
import org.hibernate.event.spi.EventType;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableSet;

import me.jasoncampos.inject.persist.hibernate.integrator.AutoRegisteringListener.RegistrationType;

@RunWith(MockitoJUnitRunner.class)
public class AutoRegisteringListenerIntegratorTest {

	@Mock
	private SessionFactoryServiceRegistry serviceRegistry;

	@Mock
	private EventListenerRegistry eventListenerRegistry;

	private List<AutoRegisteringListener> listeners;
	private AutoRegisteringListenerIntegrator integrator;

	@Before
	public void beforeEach() {
		when(serviceRegistry.getService(EventListenerRegistry.class)).thenReturn(eventListenerRegistry);

		listeners = new ArrayList<>();
		listeners.add(createListener(ImmutableSet.of(EventType.AUTO_FLUSH, EventType.CLEAR), ImmutableSet.of(RegistrationType.PREPEND)));
		listeners.add(createListener(ImmutableSet.of(EventType.DIRTY_CHECK), ImmutableSet.of(RegistrationType.APPEND, RegistrationType.PREPEND)));
		listeners.add(createListener(ImmutableSet.of(EventType.DELETE), ImmutableSet.of(RegistrationType.REPLACE)));

		integrator = new AutoRegisteringListenerIntegrator(ImmutableSet.copyOf(listeners));
	}

	@Test
	public void integratorRegistersAllListeners() {
		integrator.integrate(null, null, serviceRegistry);

		// Ensure all listeners were registered with their event type and under the correct registration type.
		verify(eventListenerRegistry, times(1)).prependListeners(EventType.AUTO_FLUSH, (AutoFlushEventListener) listeners.get(0));
		verify(eventListenerRegistry, times(1)).prependListeners(EventType.CLEAR, (ClearEventListener) listeners.get(0));
		verify(eventListenerRegistry, times(1)).prependListeners(EventType.DIRTY_CHECK, (DirtyCheckEventListener) listeners.get(1));
		verify(eventListenerRegistry, times(1)).appendListeners(EventType.DIRTY_CHECK, (DirtyCheckEventListener) listeners.get(1));
		verify(eventListenerRegistry, times(1)).setListeners(EventType.DELETE, (DeleteEventListener) listeners.get(2));

		verifyNoMoreInteractions(eventListenerRegistry);
	}

	private AutoRegisteringListener createListener(
			final ImmutableSet<EventType<?>> eventTypes,
			final ImmutableSet<RegistrationType> registrationTypes) {
		return new TestListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public ImmutableSet<EventType<?>> getEventType() {
				return eventTypes;
			}

			@Override
			public ImmutableSet<RegistrationType> getEventRegistrations() {
				return registrationTypes;
			}
		};
	}

	private abstract static class TestListener
			implements
			AutoRegisteringListener,
			AutoFlushEventListener,
			ClearEventListener,
			DirtyCheckEventListener,
			DeleteEventListener {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void onAutoFlush(final AutoFlushEvent event) throws HibernateException {
		}

		@Override
		public void onDelete(final DeleteEvent event) throws HibernateException {
		}

		@Override
		public void onDelete(final DeleteEvent event, @SuppressWarnings("rawtypes") final Set transientEntities) throws HibernateException {
		}

		@Override
		public void onDirtyCheck(final DirtyCheckEvent event) throws HibernateException {
		}

		@Override
		public void onClear(final ClearEvent event) {
		}
	}
}
