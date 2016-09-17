package me.jasoncampos.inject.persist.hibernate;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class PackageScanEntityClassProviderTest {

	@Test
	public void providesOnlyAnnotatedClasses() {
		final PackageScanEntityClassProvider provider = new PackageScanEntityClassProvider("me.jasoncampos.inject.persist.hibernate");
		final List<Class<?>> entities = provider.get();
		assertEquals(1, entities.size());
		assertEquals(TestEntity.class, entities.get(0));
	}
}
