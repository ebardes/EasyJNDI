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

import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

/**
 * A utility class to make strings Referencable and therefore able to be saved.
 * @author Eric E Bardes
 */
public class StringValue implements Referenceable
{
	private final String textValue;
	
	/**
	 * @param textValue Something to be stored
	 */
	public StringValue(final String textValue)
	{
		this.textValue = textValue;
	}
	
	@Override
	public String toString()
	{
		return textValue;
	}
	
	@Override
	public Reference getReference() throws NamingException
	{
		RefAddr addr = new StringRefAddr(StringValueFactory.ATTR_NAME, textValue);
		Reference ref = new Reference(StringValueFactory.class.getCanonicalName(), StringValueFactory.class.getCanonicalName(), null);
		ref.add(addr);
		return ref;
	}

	/**
	 * @return the textValue
	 */
	public String getTextValue()
	{
		return textValue;
	}

}
