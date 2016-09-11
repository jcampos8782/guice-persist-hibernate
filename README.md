# guice-persist-hibernate
If you have as much distaste for XML configuration as I do, then fear no more! Do away with your JPA implementation and persistence.xml and move configuration in to code. Since the configurations are supplied by `Provider` objects, how you obtain the configuration data is up to you (read from file, obtain from consul, whatever your heart desires). 

# Trivial Example
```java

    public void configure() {
        install(new HibernatePersistModule());

        bind(HibernateEntityClassProvider.class).toInstance(new HibernateEntityClassProvider() {
            @Override
            public List<Class<? extends Object>> get() {
                return Arrays.asList(Account.class);
            }
        });

        bind(HibernatePropertyProvider.class).toInstance(new HibernatePropertyProvider() {
            @Override
            public Map<String, String> get() {
                final Map<String, String> properties = new HashMap<>();
                properties.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
                properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                properties.put("hibernate.connection.url", "jdbc:mysql://127.0.0.1/p_user?useSSL=false");
                properties.put("hibernate.connection.username", "root");
                properties.put("hibernate.connection.password", "password");
                return properties;
            }
        });
    }
```
