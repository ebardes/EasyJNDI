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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

class WrappedConnection implements Connection
{
	final Connection conn;
	private final DataSourceWrapper pool;
	long timestamp;
	Throwable stackTrace;

	public WrappedConnection(DataSourceWrapper pool, Connection conn)
	{
		this.pool = pool;
		this.conn = conn;
	}

	public void clearWarnings() throws SQLException
	{
		conn.clearWarnings();
	}

	public void close() throws SQLException
	{
		pool.release(this);
	}

	public void commit() throws SQLException
	{
		conn.commit();
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException
	{
		return conn.createArrayOf(typeName, elements);
	}

	public Blob createBlob() throws SQLException
	{
		return conn.createBlob();
	}

	public Clob createClob() throws SQLException
	{
		return conn.createClob();
	}

	public NClob createNClob() throws SQLException
	{
		return conn.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException
	{
		return conn.createSQLXML();
	}

	public Statement createStatement() throws SQLException
	{
		return conn.createStatement();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{
		return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
	{
		return conn.createStatement(resultSetType, resultSetConcurrency);
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException
	{
		return conn.createStruct(typeName, attributes);
	}

	public boolean getAutoCommit() throws SQLException
	{
		return conn.getAutoCommit();
	}

	public String getCatalog() throws SQLException
	{
		return conn.getCatalog();
	}

	public Properties getClientInfo() throws SQLException
	{
		return conn.getClientInfo();
	}

	public String getClientInfo(String name) throws SQLException
	{
		return conn.getClientInfo(name);
	}

	public int getHoldability() throws SQLException
	{
		return conn.getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException
	{
		return conn.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException
	{
		return conn.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException
	{
		return conn.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException
	{
		return conn.getWarnings();
	}

	public boolean isClosed() throws SQLException
	{
		return conn.isClosed();
	}

	public boolean isReadOnly() throws SQLException
	{
		return conn.isReadOnly();
	}

	public boolean isValid(int timeout) throws SQLException
	{
		return conn.isValid(timeout);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return conn.isWrapperFor(iface);
	}

	public String nativeSQL(String sql) throws SQLException
	{
		return conn.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{
		return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
	{
		return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql) throws SQLException
	{
		return conn.prepareCall(sql);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{
		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
	{
		return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
	{
		return conn.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
	{
		return conn.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
	{
		return conn.prepareStatement(sql, columnNames);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException
	{
		return conn.prepareStatement(sql);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException
	{
		conn.releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException
	{
		conn.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException
	{
		conn.rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException
	{
		conn.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException
	{
		conn.setCatalog(catalog);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException
	{
		conn.setClientInfo(properties);
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException
	{
		conn.setClientInfo(name, value);
	}

	public void setHoldability(int holdability) throws SQLException
	{
		conn.setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException
	{
		conn.setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException
	{
		return conn.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException
	{
		return conn.setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException
	{
		conn.setTransactionIsolation(level);
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException
	{
		conn.setTypeMap(map);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		return conn.unwrap(iface);
	}

	public void setSchema(String schema) throws SQLException
	{
		conn.setSchema(schema);
	}

	public String getSchema() throws SQLException
	{
		return conn.getSchema();
	}

	public void abort(Executor executor) throws SQLException
	{
		conn.abort(executor);
	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
	{
		conn.setNetworkTimeout(executor, milliseconds);
	}

	public int getNetworkTimeout() throws SQLException
	{
		return conn.getNetworkTimeout();
	}
}
