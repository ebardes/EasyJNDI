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

import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

/**
 * A utility class to make file paths Referencable and therefore able to be saved.
 * @author eric
 */
public class FileLocation implements Referenceable
{
	private final String path;

	/**
	 * @param path
	 */
	public FileLocation(final String path)
	{
		this.path = path;
	}

	/**
	 * @return The path to the file.
	 */
	public final String getPath()
	{
		return path;
	}
	
	/**
	 * @return a {@link File} object
	 */
	public final File getFile()
	{
		return new File(path);
	}
	
	@Override
	public Reference getReference() throws NamingException
	{
		RefAddr addr = new StringRefAddr(FileLocationFactory.ATTR_NAME, path);
		Reference ref = new Reference(FileLocation.class.getCanonicalName(), FileLocationFactory.class.getCanonicalName(), null);
		ref.add(addr);
		return ref;
	}
}
