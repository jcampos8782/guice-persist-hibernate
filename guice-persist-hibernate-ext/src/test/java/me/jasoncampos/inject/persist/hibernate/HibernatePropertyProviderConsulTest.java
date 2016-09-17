package me.jasoncampos.inject.persist.hibernate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;

@RunWith(MockitoJUnitRunner.class)
public class HibernatePropertyProviderConsulTest {

	private static final String keyPath = "/path/to/keys/";

	@Mock
	private KeyValueClient kvClient;
	private List<Value> values;
	private HibernatePropertyProviderConsul provider;

	@Before
	public void beforeEach() {
		values = new ArrayList<>();
		values.add(createValue("key1", null));
		values.add(createValue("key2", "value2"));
		values.add(createValue("key3", "value3"));
		when(kvClient.getValues(keyPath)).thenReturn(values);

		provider = new HibernatePropertyProviderConsul(kvClient, keyPath);
	}

	@Test
	public void itReturnsOnlyNonNullValues() {
		assertEquals(2, provider.get().size());
	}

	@Test
	public void itStripsKeyPathFromKeyName() {
		assertEquals("value2", provider.get().get("key2"));
		assertEquals("value3", provider.get().get("key3"));
	}

	private static Value createValue(final String key, final String value) {
		final String encodedString = value != null ? new String(Base64.getEncoder().encode(value.getBytes())) : null;
		return new Value() {
			@Override
			public long getCreateIndex() {
				return 0;
			}

			@Override
			public long getModifyIndex() {
				return 0;
			}

			@Override
			public long getLockIndex() {
				return 0;
			}

			@Override
			public String getKey() {
				return keyPath + key;
			}

			@Override
			public long getFlags() {
				return 0;
			}

			@Override
			public Optional<String> getValue() {
				return Optional.fromNullable(encodedString);
			}

			@Override
			public Optional<String> getSession() {
				return null;
			}
		};
	}
}
