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
package com.digi_dmx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.digi_dmx.gen.Attr;

/**
 * The Primary Class. This class implements Context. More to the point, a subset of Context that works for
 * most application like JPA providors and general resource management of lookup/bind. It does not support subcontexts.
 * Unsupported methods will throw {@link java.lang.NoSuchMethodError}. 
 * 
 * @author Eric E Bardes
 */
public class XMLContext implements Context
{
	private static final String DEFAULT_ENCODING = "UTF-8";

	private static final String FILE_EXTENSION = ".xml";

	private final Hashtable<String, String> env = new Hashtable<String, String>();

	private URI uri;
	
	private String scheme;

	/**
	 * 
	 * @param environment
	 * @throws NamingException
	 */
	XMLContext(Hashtable<?, ?> environment) throws NamingException
	{
		for (Entry<?, ?> entry : environment.entrySet())
		{
			addToEnvironment((String) entry.getKey(), entry.getValue());
		}
		
		String e = System.getenv("JNDI_HOME");
		if (e != null)
		{
			addToEnvironment(PROVIDER_URL, e);
		}
	}

	/**
	 * This is a handy method for initializing the global JNDI environment if for some reason, the defaults don't come through.
	 */
	public static void init()
	{
		System.setProperty(INITIAL_CONTEXT_FACTORY, XMLContextFactory.class.getName());
		System.setProperty(PROVIDER_URL, "%{user.home}/jndi");
	}
	
	/**
	 * Add a value to the environment.
	 * @param propName The property Name.
	 * @param propVal The property Value.  This implementation requires that all values be strings.  Values in the form ${user.dir} expand to the corresponding value. 
	 * @return The value as expanded.
	 */
	@Override
	public Object addToEnvironment(String propName, Object propVal) throws NamingException
	{
		String val = (String) propVal;
		val = propertySubstitute(val);
		if (propName.equals(PROVIDER_URL))
		{
			this.uri = URI.create(val);
			this.scheme = uri.getScheme();
		}
		return env.put(propName, val);
	}

	/**
	 * This method provides environment expansion.  Values in the form %{user.dir} expand to the corresponding value. 
	 * 
	 * @param val the Preprocessed text
	 * @return Processed text
	 */
	private String propertySubstitute(String val)
	{
		Pattern p = Pattern.compile("\\%\\{(.*?)\\}");
		Matcher m = p.matcher(val);
		while (m.find())
		{
			String sysProp = m.group(1);
			String property = System.getProperty(sysProp, "");
			if (sysProp.equals("user.home"))
			{
				File f = new File(property);
				property = f.toURI().toString();
			}
			val = m.replaceFirst(property);
			
			m.reset(val);
		}
		return val;
	}

	/**
	 * This methods delegates to {@link #bind(String, Object)}.
	 */
	@Override
	public void bind(Name name, Object obj) throws NamingException
	{
		bind(name.toString(), obj);
	}

	/**
	 * Saves a reference to the given name.  This method technically differs from many implementations
	 * in that it permits <tt>rebind</tt> behaviour.  In fact, {@link #rebind(String, Object)} delegates
	 * to this method.
	 * 
	 * @param name The name of the reference.  This name is used to construct
	 * a location by appending the name to the Provider URL and appending ".xml".  
	 * See {@link #makePath(String)} for full details.
	 * 
	 * @param obj An object to save.  The object <b>must</b> implement Referenecable.
	 * 
	 * @throws NamingException Object does not implement Referenceable or some other problem occurs during processing.
	 */
	@Override
	public void bind(String name, Object obj) throws NamingException
	{
		if (obj instanceof Referenceable)
		{
			Reference ref = ((Referenceable) obj).getReference();

			FileOutputStream fos = null;
			try
			{
				if (scheme == null || scheme.startsWith("file"))
				{
					File f = makePath(name);
					fos = new FileOutputStream(f);
				}
				else
				{
					throw new NamingException("Scheme “" + scheme + "” is not supported by XMLContext.bind()");
				}
				
				saveXML(ref, fos);
			}
			catch (Exception e)
			{
				NamingException ne = new NamingException();
				ne.setRootCause(e);
				throw ne;
			}
			finally
			{
				try
				{
					if (fos != null)
						fos.close();
				}
				catch (IOException e)
				{
					NamingException ne = new NamingException();
					ne.setRootCause(e);
					throw ne;
				}
			}
		}
		else
		{
			throw new NamingException("Class must implement Referenceable");
		}
	}

	/**
	 * An internal method used to save the stream to the target.
	 * 
	 * @param ref
	 * @param fos
	 * @throws JAXBException
	 */
	private void saveXML(Reference ref, FileOutputStream fos) throws JAXBException
	{
		JAXBContext ctx = JAXBContext.newInstance(com.digi_dmx.gen.Context.class);
		Marshaller m = ctx.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.setProperty(Marshaller.JAXB_ENCODING, DEFAULT_ENCODING);
		
		com.digi_dmx.gen.Context save = new com.digi_dmx.gen.Context();
		save.setFactory(ref.getFactoryClassName());
		save.setClazz(ref.getClassName());
		Enumeration<RefAddr> all = ref.getAll();
		while (all.hasMoreElements())
		{
			RefAddr refAddr = all.nextElement();
			Attr attr = new Attr();
			attr.setName(refAddr.getType());
			Object content = refAddr.getContent();
			if (content != null)
			{
				attr.setValue(content.toString());
			}
			
			save.addAttr(attr); // this hurts my soul
		}
		m.marshal(save, fos);
	}

	/**
	 * @param name This name is used to construct a location by appending the name to the Provider URL and appending ".xml".  Directories are created as required.  
	 * @return A file
	 */
	private File makePath(String name)
	{
		File f = new File(new File(uri.getPath()), name + FILE_EXTENSION);
		File path = f.getParentFile();
		if (!path.isDirectory())
			path.mkdirs();
		return f;
	}

	/**
	 * In this implementation, no operation.
	 */
	@Override
	public void close() throws NamingException
	{
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError This method isn't implemented
	 */
	@Override
	public Name composeName(Name name, Name prefix) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError This method isn't implemented
	 */
	@Override
	public String composeName(String name, String prefix) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError This method isn't implemented
	 */
	@Override
	public Context createSubcontext(Name name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError This method isn't implemented
	 */
	@Override
	public Context createSubcontext(String name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError This method isn't implemented
	 */
	@Override
	public void destroySubcontext(Name name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public void destroySubcontext(String name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * Returns the environment.
	 */
	@Override
	public Hashtable<?, ?> getEnvironment() throws NamingException
	{
		return env;
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public String getNameInNamespace() throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public NameParser getNameParser(Name name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public NameParser getNameParser(String name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public NamingEnumeration<NameClassPair> list(String name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public NamingEnumeration<Binding> listBindings(String name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * Delegates to {@link #lookup(String)}.
	 */
	@Override
	public Object lookup(Name name) throws NamingException
	{
		return lookup(name.toString());
	}

	/**
	 * @param name The name of the resource to load.  The name to file translation is provided by {@link #makePath(String)}.
	 * @return The loaded object.
	 * @throws NamingException if the object is not found, or errors occurred while loading or validating.
	 */
	@Override
	public Object lookup(String name) throws NamingException
	{
		InputStream fis = null;
		try
		{
			if (scheme == null || scheme.equalsIgnoreCase("file"))
			{
				File f = makePath(name);
				fis = new FileInputStream(f);
			}
			else if (scheme.startsWith("http"))
			{
				URI loc = uri.resolve(name + FILE_EXTENSION);
				fis = loc.toURL().openStream();
			}
			else
			{
				throw new NamingException("Scheme “" + scheme + "” not supported by XMLContext.lookup()");
			}
			
			Reference ref = parseXML(fis);

			ObjectFactory factory = (ObjectFactory) Class.forName(ref.getFactoryClassName()).newInstance();
			Object instance = factory.getObjectInstance(ref, new CompositeName(name), this, env);
			return instance;
		}
		catch (Throwable e)
		{
			NamingException ne = new NamingException();
			ne.initCause(e);
			throw ne;
		}
		finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException ignore)
				{
				}
			}
		}
	}

	/**
	 * Parse the XML using JAXB.
	 * 
	 * @param fis
	 * @return A Reference suitable for factories. 
	 * @throws JAXBException 
	 */
	private Reference parseXML(InputStream fis) throws JAXBException 
	{
		JAXBContext ctx = JAXBContext.newInstance(com.digi_dmx.gen.Context.class);
		Unmarshaller u = ctx.createUnmarshaller();
		com.digi_dmx.gen.Context unmarshaledObject = (com.digi_dmx.gen.Context) u.unmarshal(fis);
		
		Reference ref = new Reference(unmarshaledObject.getClazz(), unmarshaledObject.getFactory(), null);
		for (Attr a : unmarshaledObject.getAttr())
		{
			String aname = a.getName();
			String value = a.getValue();

			RefAddr addr = new StringRefAddr(aname, value);
			ref.add(addr);
		}
		return ref;
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public Object lookupLink(Name name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * This implementation does not implement this method.
	 * @throws NoSuchMethodError  This method isn't implemented
	 */
	@Override
	public Object lookupLink(String name) throws NamingException
	{
		throw new NoSuchMethodError();
	}

	/**
	 * Delegates to {@link #bind(String, Object)}
	 */
	@Override
	public void rebind(Name name, Object obj) throws NamingException
	{
		bind(name.toString(), obj);
	}

	/**
	 * Delegates to {@link #bind(String, Object)}
	 */
	@Override
	public void rebind(String name, Object obj) throws NamingException
	{
		bind(name, obj);
	}

	@Override
	public Object removeFromEnvironment(String propName) throws NamingException
	{
		return env.remove(propName);
	}

	/**
	 */
	@Override
	public void rename(Name oldName, Name newName) throws NamingException
	{
		rename(oldName.toString(), newName.toString());
	}

	/**
	 */
	@Override
	public void rename(String oldName, String newName) throws NamingException
	{
		File oldFile = makePath(oldName);
		File newFile = makePath(newName);
		
		oldFile.renameTo(newFile);
	}

	/**
	 * This method delegates to {@link #unbind(String)}.
	 */
	@Override
	public void unbind(Name name) throws NamingException
	{
		unbind(name.toString());
	}

	/**
	 * Removes the object.  Removes the object by deleting the saved object from the filesystem.
	 */
	@Override
	public void unbind(String name) throws NamingException
	{
		File file = makePath(name);
		if (file.exists())
		{
			file.delete();
		}
	}
}
