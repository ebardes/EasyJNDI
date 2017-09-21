package com.digi_dmx.ds;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Iterator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

class DataSourceWrapper implements Referenceable, DataSource
{
	/**
	 * The time in milliseconds that we check for connection leakage.
	 */
	private static final int CONNECTION_LEAK_CHECK_INTERVAL = 30000;

	/**
	 * The mutex used to make all thread-unsafe operations thread-safe.
	 */
	private static Lock lock = new ReentrantLock();
	
	/**
	 * The original DataSource.
	 */
	private final DataSource ds;
	
	/**
	 * A queue of open, available connections
	 */
	private static Queue<WrappedConnection> available = new ConcurrentLinkedQueue<WrappedConnection>();
	
	/**
	 * A queue of open but in-use connections
	 */
	private static Queue<WrappedConnection> inuse = new ConcurrentLinkedQueue<WrappedConnection>();
	
	/**
	 * A timer that fires periodically to check for leak-timeout, old connections. 
	 */
	private Timer timer;
	
	private long leakTimeout;

	private TimerTask task;

	private long idleTimeout;

	/**
	 * 
	 * @param ds
	 */
	public DataSourceWrapper(DataSource ds)
	{
		this(ds, 0);
	}
	
	/**
	 * 
	 * @param ds
	 * @param timeout
	 */
	public DataSourceWrapper(DataSource ds, int timeout)
	{
		this.ds = ds;
		this.leakTimeout = timeout;
		this.idleTimeout = 200000;
	}

	public void close() throws SQLException
	{
		while (available.size() > 0)
		{
			WrappedConnection w = available.remove();
			w.conn.close();
		}
		
		if (inuse.size() > 0)
		{
			throw new SQLException("Connections inuse");
		}
	}
	
	private void startTimer()
	{
		lock.lock();
		try
		{
			if (task == null)
			{
				timer = new Timer(true);
				task = new TimerTask() { public void run() { onTimer(); } };
				timer.scheduleAtFixedRate(task, CONNECTION_LEAK_CHECK_INTERVAL, CONNECTION_LEAK_CHECK_INTERVAL);
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	private void stopTimer()
	{
		lock.lock();
		try
		{
			if (task != null)
			{
				task.cancel();
				task = null;
				timer.cancel();
			}
		}
		finally
		{
			lock.unlock();
		}
	}
	
	protected void finalize() throws Throwable
	{
		stopTimer();
		timer.cancel();
	}

	protected void onTimer()
	{
		lock.lock();
		try
		{
			if (leakTimeout > 0)
			{
				long threshold = System.currentTimeMillis() - leakTimeout;
				Iterator<WrappedConnection> it = inuse.iterator();
				while (it.hasNext())
				{
					WrappedConnection w = it.next();
					if (w.timestamp < threshold)
					{
						Logger logger = Logger.getAnonymousLogger();
						it.remove();
						try
						{
							w.conn.close();
							logger.log(Level.INFO, "Removing stale connection", w.stackTrace);
						}
						catch (SQLException e)
						{
							logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
						}
					}
				}
			}
			
			if (idleTimeout > 0)
			{
				long threshold = System.currentTimeMillis() - idleTimeout;
				Iterator<WrappedConnection> it = available.iterator();
				while (it.hasNext())
				{
					WrappedConnection w = it.next();
					if (w.timestamp < threshold)
					{
						it.remove();
						try
						{
							w.conn.close();
						}
						catch (SQLException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			
			if (inuse.isEmpty() && available.isEmpty())
			{
				stopTimer();
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public Connection getConnection() throws SQLException
	{
		lock.lock();
		try
		{
			WrappedConnection w;
			if (available.isEmpty())
			{
				Connection conn = ds.getConnection();
				w = new WrappedConnection(this, conn);
				if (inuse.isEmpty())
				{
					startTimer();
				}
			}
			else
			{
				w = available.poll();
			}
			inuse.add(w);
			w.timestamp = System.currentTimeMillis();
			w.stackTrace = new TimeoutException();
			w.stackTrace.fillInStackTrace();
			return w;
		}
		finally
		{
			lock.unlock();
		}
	}

	public Connection getConnection(String username, String password) throws SQLException
	{
		throw new NoSuchMethodError();
	}

	public PrintWriter getLogWriter() throws SQLException
	{
		return ds.getLogWriter();
	}

	public int getLoginTimeout() throws SQLException
	{
		return ds.getLoginTimeout();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return ds.isWrapperFor(iface);
	}

	public void setLogWriter(PrintWriter out) throws SQLException
	{
		ds.setLogWriter(out);
	}

	public void setLoginTimeout(int seconds) throws SQLException
	{
		ds.setLoginTimeout(seconds);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		return ds.unwrap(iface);
	}

	@Override
	public Reference getReference() throws NamingException
	{
		Reference wrapRef = ((Referenceable) ds).getReference();
		Reference ref = new Reference(DataSourceWrapper.class.getCanonicalName(), DataSourceWrapperFactory.class.getCanonicalName(), null);
		
		int n = wrapRef.size();
		for (int i = 0; i < n; i++)
		{
			ref.add(wrapRef.get(i));
		}
		
		/*
		 * Please see DataSourceWrapperFactory.WRAPPER_ITEMS if you change the number of items added to the reference
		 */
		ref.add(new StringRefAddr(DataSourceWrapperFactory.WRAPPER_FACTORY, wrapRef.getFactoryClassName()));
		ref.add(new StringRefAddr(DataSourceWrapperFactory.WRAPPER_OBJECT, wrapRef.getClassName()));
		ref.add(new StringRefAddr(DataSourceWrapperFactory.TIMEOUT, String.valueOf(leakTimeout)));
		return ref;
	}

	public void release(WrappedConnection wrapped)
	{
		lock.lock();
		try
		{
			inuse.remove(wrapped);
			available.add(wrapped);
			wrapped.timestamp = System.currentTimeMillis();
			
			if (inuse.isEmpty() && available.isEmpty())
			{
				stopTimer();
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public long getLeakTimeout()
	{
		return leakTimeout;
	}

	public void setLeakTimeout(long currentTimeout)
	{
		this.leakTimeout = currentTimeout;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{
		return null;
	}
}
