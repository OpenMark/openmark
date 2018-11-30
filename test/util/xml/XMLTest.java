/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package util.xml;

import java.io.*;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JUnit test cases for util.xml.XML.
 */
public class XMLTest extends TestCase
{
	private static final String PROLOG="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
	private Document d;
	private Element eTadpole,eTadpole2,eTadpole3,eTadpole4;
	private String sSample=
		"<frog>" +
		"<tadpole>47</tadpole>"+
		"<tadpole2></tadpole2>"+
		"<tadpole3>   \n   </tadpole3>"+
		"<tadpole4>&amp;&#x2222;</tadpole4>"+
		"</frog>";
	private String sSampleCanonical=
		"<frog><tadpole>47</tadpole><tadpole2/><tadpole3>   " + System.lineSeparator() +
		"   </tadpole3><tadpole4>&amp;\u2222</tadpole4></frog>";

	File f;

	@Override
	protected void setUp() throws Exception
	{
		d=XML.parse(sSample);
		Element eRoot=d.getDocumentElement();
		eTadpole=XML.getChild(eRoot,"tadpole");
		eTadpole2=XML.getChild(eRoot,"tadpole2");
		eTadpole3=XML.getChild(eRoot,"tadpole3");
		eTadpole4=XML.getChild(eRoot,"tadpole4");

		File fFolder=new File("c:/temp");
		if(!fFolder.exists()) fFolder.mkdirs();
		f=new File(fFolder,"test.xml");
		FileOutputStream fos=new FileOutputStream(f);
		fos.write(sSample.getBytes("UTF-8"));
		fos.close();
	}

	@Override
	protected void tearDown() throws Exception
	{
		f.delete();
	}

	/**
	 * Test of the getVersion method.
	 */
	public void testGetVersion()
	{
		assertTrue(XML.getVersion().startsWith("XML library: using"));
	}

	/**
	 * Test of the cloneDocument method.
	 * @throws Exception
	 */
	public void testCloneDocument() throws Exception
	{
		// Clone document
		Document dNew=XML.clone(d);

		// Same?
		assertEquals(XML.saveString(d),XML.saveString(dNew));

		// Changes in one don't affect the other?
		XML.createChild(d.getDocumentElement(),"arse");
		assertFalse(XML.saveString(d).equals(XML.saveString(dNew)));
	}

	/**
	 * Test of the getTextNode method.
	 *
	 */
	public void testGetTextNode()
	{
		assertEquals(XML.getText(eTadpole),"47"); // String
		assertEquals(XML.getText(eTadpole2),""); // Empty
		assertEquals(XML.getText(eTadpole3),"   \n   "); // Whitespace
		assertEquals(XML.getText(eTadpole4),"&\u2222"); // Entities
	}

	/**
	 * Test of the parseByteArray method.
	 * @throws Exception
	 */
	public void testParseByteArray() throws Exception
	{
		assertEquals(
				XML.saveString(XML.parse(sSample.getBytes("UTF-8"))),
				XML.saveString(d));
	}

	/**
	 * Test of the parseInputStream method.
	 * @throws Exception
	 */
	public void testParseInputStream() throws Exception
	{
		assertEquals(
				XML.saveString(XML.parse(new ByteArrayInputStream(sSample.getBytes("UTF-8")))),
				XML.saveString(d));
	}

	/**
	 * Test of the parseFile method.
	 * @throws Exception
	 */
	public void testParseFile() throws Exception
	{
		assertEquals(XML.saveString(d),XML.saveString(XML.parse(f)));
	}

	/**
	 * Test of the pestParseInputSource.
	 * @throws Exception
	 */
	public void testParseInputSource()
	{
		// Skip this test as it has been tested enough with the
		// other methods!
	}

	/**
	 * Test of the parseString method.
	 * @throws Exception
	 */
	public void testParseString() throws Exception
	{
		try
		{
			XML.parse("hello");
			fail();
		}
		catch(XMLException xe)
		{
			assertTrue(true);
		}

		try
		{
			XML.parse((String)null);
			fail();
		}
		catch(NullPointerException ne)
		{
			assertTrue(true);
		}

		Document dHere=XML.parse(sSample);
		Element eRoot=dHere.getDocumentElement();
		assertEquals(eRoot.getTagName(),"frog");
		assertEquals(XML.getText(eRoot,"tadpole"),"47");
	}

	/**
	 * Test of the saveStringDocumentBoolean method.
	 * @throws Exception
	 */
	public void testSaveStringDocumentBoolean() throws Exception
	{
		assertEquals(sSampleCanonical,XML.saveString(d,false));
		assertEquals(PROLOG+sSampleCanonical,XML.saveString(d,true));
	}

	/**
	 * Test of the saveStringDocument method.
	 * @throws Exception
	 */
	public void testSaveStringDocument() throws Exception
	{
		assertEquals(PROLOG+sSampleCanonical, XML.saveString(d));
	}
/*
	public void testParseByteArrayFile()
	{

	}

	public void testParseInputStreamFile()
	{

	}

	public void testParseReaderFile()
	{

	}

	public void testParseStringFile()
	{

	}

	public void testCreateDocument()
	{

	}

	public void testSaveDocumentOutputStreamBoolean()
	{

	}

	public void testSaveDocumentWriterBoolean()
	{

	}

	public void testSaveDocumentOutputStream()
	{

	}

	public void testSaveDocumentFile()
	{

	}

	public void testSaveBytesDocumentBoolean()
	{

	}

	public void testSaveBytesDocument()
	{

	}


	public void testSaveElementFile()
	{

	}

	public void testSaveElementOutputStream()
	{

	}

	public void testSaveElementOutputStreamBoolean()
	{

	}

	public void testSaveElementWriterBoolean()
	{

	}

	public void testSaveBytesElement()
	{

	}

	public void testSaveBytesElementBoolean()
	{

	}

	public void testSaveStringElement()
	{

	}

	public void testSaveStringElementBoolean()
	{

	}

	public void testGetChild()
	{

	}

	public void testGetOrCreateChild()
	{

	}

	public void testGetRequiredAttribute()
	{

	}

	public void testHasChild()
	{

	}

	public void testGetChildrenNodeString()
	{

	}

	public void testGetNestedChild()
	{

	}

	public void testGetNestedChildren()
	{

	}

	public void testGetTextFromChildren()
	{

	}

	public void testGetAttributeFromChildren()
	{

	}

	public void testGetChildrenNode()
	{

	}

	public void testCreateChild()
	{

	}

	public void testGetTextNodeString()
	{

	}

	public void testGetInt()
	{

	}

	public void testGetDouble()
	{

	}

	public void testCreateComment()
	{

	}

	public void testCreateTextNodeStringString()
	{

	}

	public void testCreateInt()
	{

	}

	public void testCreateDouble()
	{

	}

	public void testCreateTextNodeString()
	{

	}

	public void testSetTextNodeStringString()
	{

	}

	public void testSetTextNodeString()
	{

	}

	public void testRemoveChildren()
	{

	}

	public void testRemove()
	{

	}

	public void testEscape()
	{

	}

	public void testReplaceTokensStringStringMap()
	{

	}

	public void testReplaceToken()
	{

	}

	public void testReplaceTokensNodeMap()
	{

	}

	public void testReplaceTokensNodeStringMap()
	{

	}

	public void testFindDocumentStringString()
	{

	}

	public void testFindElementStringString()
	{

	}

	public void testFindAllDocumentStringString()
	{

	}

	public void testFindAllElementStringString()
	{

	}

	public void testHasChildWithAttribute()
	{

	}

	public void testGetChildWithAttribute()
	{

	}

	public void testGetChildrenWithAttribute()
	{

	}

	public void testImportChildren()
	{

	}

	public void testMoveChildren()
	{

	}

	public void testGetElementArray()
	{

	}
*/
}
