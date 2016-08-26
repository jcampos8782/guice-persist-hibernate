package me.jasoncampos.inject.persist.hibernate;

import java.util.List;

import com.google.inject.Provider;

/**
 * Provides the list of annotated {@code @Entity} classes to register with
 * Hibernate.
 *
 * @author Jason Campos <jcmapos8782@gmail.com>
 */
public interface HibernateEntityClassProvider extends Provider<List<Class<? extends Object>>> {
}
