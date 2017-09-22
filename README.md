EasyJNDI
==========

The purpose of this project is to create a JNDI framework that uses easy to edit XML files.


### Environment ###

Java 7 or higher. Optional dependency on Apache Common DBCP2.

JNDI uses two system properties to define the JNDI framework and where to locate resources.
Normally there's a property file called `jndi.properties` included in the JAR file which automatically set
`Context.INITIAL_CONTEXT_FACTORY` and `Context.PROVIDER_URL` to `com.digi_dmx.XMLContextFactory` and `%{user.home}/jndi` respectively.
In some rare cases, where there are multiple JNDI providers it may be necessary to set it manually by

```
System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.digi_dmx.XMLContextFactory");
``` 

### How To Use ###

The first requirement is that anything stored must implement the interface `javax.naming.Referenceable`.
Many JDBC DataSources already implement it. Since each JDBC implementation is unique and isn't always clear
what fields are required. I find it best to run a small program like this to generate the first file.  
```
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.postgresql.ds.PGSimpleDataSource;

public class DSSetup
{
	public static void main(String[] args) throws NamingException
	{
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName("postgres.example.com");
		dataSource.setDatabaseName("database");
		dataSource.setUser("user");
		dataSource.setPassword("*****");
		/*
		 * Any additional parameters and tuning
		 */
		dataSource.setConnectTimeout(5);
		// dataSource.setProperty(name, value);
		
		/*
		 * Create a context overriding JNDI defaults
		 */
		Hashtable<String, Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.digi_dmx.XMLContextFactory");
		env.put(Context.PROVIDER_URL, "/settings");
		Context c = new InitialContext(env);
		
		/*
		 * This will create a file called /settings/jdbc/postgres.xml 
		 */
		c.bind("jdbc/postgres", dataSource);
	}
}

```

This is the file it creates.

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<context class="org.postgresql.ds.PGSimpleDataSource" factory="org.postgresql.ds.common.PGObjectFactory">
    <attr name="serverName" value="postgres.example.com"/>
    <attr name="databaseName" value="database"/>
    <attr name="user" value="user"/>
    <attr name="password" value="*****"/>
    <attr name="connectTimeout" value="5"/>
</context>
```

### Helpers ###

There are lots of classes that don't implement Referenceable.

#### URIValue ####
`/EasyJNDI/src/com/digi_dmx/URIValue` encapsulates a java.net.URI object. Useful for saving references to servers and web services.

#### FileLocation #### 
`/EasyJNDI/src/com/digi_dmx/FileLocation` encapsulates a java.io.File object. Useful for saving references file locations.

#### StringValue #### 
`/EasyJNDI/src/com/digi_dmx/StringValue` encapsulates a java.lang.String object. 

#### Database Helper ####

Unfortunately, the database implementations for MariaDB and MySQL DataSources don't implement Referencable.

```
import com.digi_dmx.ds.DSAdapter;

...

		DSAdapter ds = new DSAdapter();
		ds.setDriver(org.mariadb.jdbc.Driver.class.getName());
		ds.setUrl("jdbc:mariadb://localhost/database");
		ds.setUsername("user");
		ds.setPassword("****");

		new InitialContext().bind("jdbc/mariadb", ds);
```

The DataSource functionality is implemented by Apache Commons DBCP sub-project. 