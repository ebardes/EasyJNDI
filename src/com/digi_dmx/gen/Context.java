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

package com.digi_dmx.gen;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "attr" })
@XmlRootElement(name = "context")
public class Context
{

	@XmlAttribute(name = "class", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String clazz;

	@XmlAttribute(name = "factory", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	protected String factory;

	protected List<Attr> attr;

	/**
	 * Gets the value of the clazz property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getClazz()
	{
		return clazz;
	}

	/**
	 * Sets the value of the clazz property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setClazz(String value)
	{
		this.clazz = value;
	}

	/**
	 * Gets the value of the factory property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getFactory()
	{
		return factory;
	}

	/**
	 * Sets the value of the factory property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setFactory(String value)
	{
		this.factory = value;
	}

	/**
	 * Gets the value of the attr property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the attr property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAttr().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Attr }
	 * 
	 * @return all the attributes
	 * 
	 */
	public List<Attr> getAttr()
	{
		if (attr == null)
		{
			attr = new ArrayList<Attr>();
		}
		return this.attr;
	}

	/**
	 * @param attr A single attribute that's added to the array.
	 */
	public void addAttr(Attr attr)
	{
		if (this.attr == null)
		{
			this.attr = new ArrayList<Attr>();
		}
		this.attr.add(attr);
	}

}
