package me.jasoncampos.inject.persist.hibernate;

import java.util.Map;

import javax.inject.Provider;

/**
 * Provides a {@code Map} of properties used to initialize Hibernate.
 *
 * @author Jason Campos <jcmapos8782@gmail.com>
 */
public interface HibernatePropertyProvider extends Provider<Map<String, String>> {
}
