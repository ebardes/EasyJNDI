/*
The MIT License

Copyright (c) 2017 Eric E Bardes

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.digi_dmx.ds;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * The purpose of this class is to provide an adapter for JDBC DataSources that don't implement
 * {@link javax.naming.Referenceable}. A notable example is the Mysql and MariaDB drivers.
 * Most methods delegate to {@link org.apache.commons.dbcp2.BasicDataSource}
 * 
 * @author eric
 */
public class DSAdapter implements DataSource, Referenceable
{
	private String driver;

	private String url;

	private String username;

	private String password;
	
	private BasicDataSource ds;

	/**
	 * @param driver The driver class name to be used. For example: <tt>org.mariadb.jdbc.Driver</tt>
	 */
	public void setDriver(String driver)
	{
		this.driver = driver;
	}

	/**
	 * @param url The url to used in the format required by the Driver. For example <tt>jdbc:mariadb://mariadb.example.com/database</tt>
	 */
	public void setUrl(String url)
	{
		this.url = url;
	}

	/**
	 * @param username The User Name.
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * @param password The password.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException
	{
		return ds.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException
	{
		ds.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException
	{
		ds.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException
	{
		return ds.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{
		return getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		return ds.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return ds.isWrapperFor(iface);
	}

	@Override
	public Reference getReference() throws NamingException
	{
		Reference r = new Reference(DSAdapter.class.getName(), DSFactory.class.getName(), null);
		r.add(new StringRefAddr("url", url));
		r.add(new StringRefAddr("username", username));
		r.add(new StringRefAddr("password", password));
		r.add(new StringRefAddr("driver", driver));
		return r;
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return ds.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException
	{
		return ds.getConnection(username, password);
	}

	void open()
	{
		ds = new BasicDataSource();
		ds.setDriverClassName(driver);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setUrl(url);
	}

}
