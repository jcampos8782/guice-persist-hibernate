package me.jasoncampos.inject.persist.hibernate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;

import me.jasoncampos.inject.persist.hibernate.module.HibernateKeys;

/**
 * Loads hibernate configuration properties from consul. The {@link #keyPath} must
 * specify the full path to the consul folder containing hibernate configuration properties. For example,
 * {@code "my/app/config/hibernate"}.
 *
 * @author Jason Campos <jcampos8782@gmail.com>
 */
public class HibernatePropertyProviderConsul implements HibernatePropertyProvider {
	private static final Logger logger = LoggerFactory.getLogger(HibernatePropertyProviderConsul.class);

	private final KeyValueClient kvClient;
	private final String keyPath;

	@Inject
	public HibernatePropertyProviderConsul(final KeyValueClient kvClient, @HibernateKeys final String keyPath) {
		this.kvClient = kvClient;
		this.keyPath = keyPath;
	}

	@Override
	public Map<String, String> get() {
		logger.info("Retrieving hibernate configuration from consul");
		final List<Value> values = kvClient.getValues(keyPath);

		final Map<String, String> hibernateProperties = new HashMap<>(values.size());
		for (final Value value : values) {
			final Optional<String> v = value.getValue();

			if (v.isPresent()) {
				// Strip consul path
				final String k = value.getKey();
				final String key = k.substring(k.lastIndexOf('/') + 1);
				hibernateProperties.put(key, new String(Base64.getDecoder().decode(v.get())));
			}
		}

		if (logger.isDebugEnabled()) {
			hibernateProperties.entrySet().stream().forEach(k -> logger.debug(String.format("%s:%s", k.getKey(), k.getValue())));
		}
		return hibernateProperties;
	}
}
