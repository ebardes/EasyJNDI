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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;

class DataSourceWrapperFactory implements ObjectFactory
{
	public static final int WRAPPER_ITEMS = 3;
	public static final String TIMEOUT = "$@$@.wrapper.leak.timeout";
	public static final String WRAPPER_FACTORY = "$@$@.wrapper.factory.name";
	public static final String WRAPPER_OBJECT = "$@$@.wrapper.object.name";

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
	{
		Reference ref = (Reference) obj;
		DataSource wrapped;
		
		RefAddr addr;
		addr = (StringRefAddr) ref.get(WRAPPER_OBJECT);
		String objClassName = (String) addr.getContent();
		addr = (StringRefAddr) ref.get(WRAPPER_FACTORY);
		String factClassName = (String) addr.getContent();
		addr = (StringRefAddr) ref.get(TIMEOUT);
		int timeout = Integer.parseInt((String) addr.getContent());

		ObjectFactory innerFactory = (ObjectFactory) Class.forName(factClassName).newInstance();
		
		Reference innerRef = new Reference(objClassName, factClassName, null);
		int n = ref.size() - WRAPPER_ITEMS;
		
		for (int i = 0; i < n; i++)
		{
			innerRef.add(ref.get(i));
		}
		
		wrapped = (DataSource) innerFactory.getObjectInstance(innerRef, name, nameCtx, environment);
		
		DataSourceWrapper wrapper = new DataSourceWrapper(wrapped, timeout);
		return wrapper;
	}

}
