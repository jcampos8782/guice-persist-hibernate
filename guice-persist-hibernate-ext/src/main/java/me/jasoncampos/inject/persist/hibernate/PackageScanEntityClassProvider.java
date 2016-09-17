package me.jasoncampos.inject.persist.hibernate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * Recursively locates {@code @Entity} annotated classes in the argument {@link #entityPackage}.
 * 
 * @author Jason Campos <jcampos8782@gmail.com>
 * @see {@link HibernatePersistModule}
 * @see {@link HibernateEntityClassProvider}
 */
public class PackageScanEntityClassProvider implements HibernateEntityClassProvider {

	private final String entityPackage;

	public PackageScanEntityClassProvider(final String entityPackage) {
		this.entityPackage = entityPackage;
	}

	@Override
	public List<Class<? extends Object>> get() {
		final List<Class<? extends Object>> entityClasses = new ArrayList<>();
		try {
			final ClassPath cp = ClassPath.from(this.getClass().getClassLoader());

			for (final ClassInfo classInfo : cp.getTopLevelClassesRecursive(entityPackage)) {
				final Class<?> c = classInfo.load();
				if (c.isAnnotationPresent(Entity.class)) {
					entityClasses.add(c);
				}
			}
		} catch (final IOException e) {
			// Fail fast
			throw new RuntimeException("Failed to load hibernate entity classes.", e);
		}
		return entityClasses;
	}
}
