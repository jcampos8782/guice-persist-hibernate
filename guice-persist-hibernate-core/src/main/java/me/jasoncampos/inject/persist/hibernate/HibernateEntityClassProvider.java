package me.jasoncampos.inject.persist.hibernate;

import java.util.List;

import javax.inject.Provider;

/**
 * Provides the list of annotated {@code @Entity} classes to register with
 * Hibernate.
 *
 * @author Jason Campos <jcampos8782@gmail.com>
 */
public interface HibernateEntityClassProvider extends Provider<List<Class<? extends Object>>> {
}
