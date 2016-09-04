package me.jasoncampos.inject.persist.hibernate;

import javax.inject.Inject;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

/**
 * Module used to bootstrap hibernate using configuration properties provided by Consul. The {@link #keyPath} must
 * specify the full path to the consul folder containing hibernate configuration properties. For example,
 * {@code "my/app/config/hibernate"}.
 *
 * @author Jason Campos <jcampos8782@gmail.com>
 * @see {@link HibernatePersistModule}
 */
public class HibernateConsulConfigurationModule extends PrivateModule {

	private final String keyPath;

	public HibernateConsulConfigurationModule(final String keyPath) {
		this.keyPath = keyPath;
	}

	@Override
	protected void configure() {
		requireBinding(Consul.class);

		bindConstant().annotatedWith(HibernateKeys.class).to(keyPath);
		bind(HibernatePropertyProvider.class).to(HibernatePropertyProviderConsul.class).asEagerSingleton();
		expose(HibernatePropertyProvider.class);
	}

	@Provides
	@Inject
	private KeyValueClient getKeyValueClient(final Consul consul) {
		return consul.keyValueClient();
	}
}
