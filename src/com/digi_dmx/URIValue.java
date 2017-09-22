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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

/**
 * A utility class to make URIs Referencable and therefore able to be saved.
 * Most methods delegate to the underlying wrapped class.
 * 
 * @author Eric E Bardes
 */
public class URIValue implements Referenceable
{
	private final URI uri;
	
	/**
	 * @param uri A URI to store
	 */
	public URIValue(final URI uri)
	{
		this.uri = uri;
	}
	
	/**
	 * @see java.net.URI#toString()
	 */
	public String toString()
	{
		return uri.toString();
	}
	
	@Override
	public Reference getReference() throws NamingException
	{
		RefAddr addr = new StringRefAddr(URIValueFactory.ATTR_NAME, uri.toString());
		Reference ref = new Reference(URIValue.class.getCanonicalName(), URIValueFactory.class.getCanonicalName(), null);
		ref.add(addr);
		return ref;
	}

	/**
	 * @return the URI encapsulated in this URIValue.
	 * @see java.net.URI
	 */
	public URI getUri()
	{
		return uri;
	}

	/**
	 * @return The port component of this URIValue, or -1 if the port is undefined. 
	 * @see java.net.URI#getPort()
	 */
	public int getPort()
	{
		return uri.getPort();
	}
	
	/**
	 * @return The host name component of this URIValue.
	 * @see java.net.URI#getHost()
	 */
	public String getHost()
	{
		return uri.getHost();
	}
	
	/**
	 * @return The decoded path component of this URIValue, or null if the path is undefined.
	 * @see java.net.URI#getPath()
	 */
	public String getPath()
	{
		return uri.getPath();
	}
	
	/**
	 * @return The path component of this URIValue, or null if the path is undefined.
	 */
	public String getRawPath()
	{
		return uri.getRawPath();
	}

	/**
	 * @return A URL constructed from this URIValue
	 * @throws MalformedURLException from superclass
	 * @throws IllegalArgumentException from superclass
	 * @see java.net.URI#toURL()
	 */
	public URL toURL() throws MalformedURLException, IllegalArgumentException
	{
		return uri.toURL();
	}

	/**
	 * @return The scheme component of this URIValue, or null if the scheme is undefined
	 * @see java.net.URI#getScheme()
	 */
	public String getScheme()
	{
		return uri.getScheme();
	}

	/**
	 * @return The decoded query component of this URIValue, or null if the query is undefined
	 * @see java.net.URI#getQuery()
	 */
	public String getQuery()
	{
		return uri.getQuery();
	}
}
