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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.*;

/** XML static utility methods */
public abstract class XML
{
	/** Cached transformer factory */
	private static TransformerFactory tf=null;

	/** Synchronization objects */
	private static Object oTransformSynch=new Object();

	/** @return A TransformerFactory ready for use */
	static TransformerFactory getTransformerFactory()
	{
		if(tf==null)
			tf=TransformerFactory.newInstance();
		return tf;
	}

	/** @return Object that should be synchronized on while using the Transformer */
	static Object getTransformSynch() { return oTransformSynch; }

	/** Cached parsers (because parsers aren't thread-safe) */
	private static LinkedList<MyDOMParser> lAvailableParsers=new LinkedList<MyDOMParser>();

	/** Maximum number of parsers to cache */
	private final static int MAXCACHEDPARSERS=10;


	/** Cached documentbuilderfactory object (if not using Xercse) */
	private static DocumentBuilderFactory dbf=null;

	/** Cached DocumentImpl class (if using Xerces) */
	private static Class<?> cDocumentImpl=null;

	/** Cached DOMParser class (if using Xerces) */
	private static Class<?> cDOMParser=null;

	/** Cached setFeature method (if using Xerces) */
	private static Method mSetFeature=null;
	/** Cached parse method (if using Xerces) */
	private static Method mParse=null;
	/** Cached getDocument method (if using Xerces) */
	private static Method mGetDocument=null;

	/** True if using Xerces for parsing */
	private static boolean bUsingXerces;

	/** True if using JAX standard for parsing */
	private static boolean bUsingJAX;

	/* Initialise static members and determine whether to use Xerces or JAX */
	private static void initStatics()
	{
		if(bUsingXerces||bUsingJAX) return;

		try
		{
			cDocumentImpl=Class.forName("org.apache.xerces.dom.DocumentImpl");
			cDOMParser=Class.forName("org.apache.xerces.parsers.DOMParser");
			mSetFeature=cDOMParser.getMethod("setFeature",String.class,boolean.class);
			mParse=cDOMParser.getMethod("parse",InputSource.class);
			mGetDocument=cDOMParser.getMethod("getDocument");
			bUsingXerces=true;
		}
		catch(Exception e) // ClassNotFoundException, NoSuchMethodException
		{
			try
			{
				dbf=DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
			}
			catch(Exception e2)
			{
				e2.printStackTrace();
				throw new Error("Unexpected error creating JAX document builder");
			}
			bUsingJAX=true;
		}
	}

	/** @return Description of XML parsing method in use */
	public static String getVersion()
	{
		try
		{
			initStatics();
			if(bUsingXerces)
				return "XML library: using Xerces";
			else
				return "XML library: using JAX";
		}
		catch(Error e)
		{
			return "XML library: error - "+e.getMessage();
		}
	}

	/** JAX parser wrapper */
	static class MyJAXParser implements MyDOMParser
	{
		/** Document builder */
		private DocumentBuilder db;

		/** Last-parsed document */
		private Document d;

		/**
		 * Constructor.
		 */
		MyJAXParser()
		{
			try
			{
				db=dbf.newDocumentBuilder();
			}
			catch(ParserConfigurationException e)
			{
				throw new Error("Error creating DocumentBuilder");
			}
		}

		public void parse(InputSource is) throws SAXException,IOException
		{
			d=db.parse(is);
		}

		public Document getDocument() throws SAXException
		{
			return d;
		}
	}

	/** Xerces parser wrapper */
	static class MyXercesParser implements MyDOMParser
	{
		private Object o;

		/**
		 * Constructor.
		 */
		MyXercesParser()
		{
			try
			{
				o=cDOMParser.newInstance();
				mSetFeature.invoke(o,new Object[]{
					"http://apache.org/xml/features/dom/defer-node-expansion",Boolean.FALSE});
			}
			catch(Exception e)
			{
				throw new Error("Error setting Xerces feature",e);
			}
		}

		public void parse(InputSource is) throws SAXException,IOException
		{
			try
			{
				mParse.invoke(o,new Object[]{is});
			}
			catch(Throwable t)
			{
				if(t instanceof InvocationTargetException)
				{
					t=((InvocationTargetException)t).getTargetException();
				}
				if(t instanceof SAXException)
					throw (SAXException)t;
				else if(t instanceof IOException)
					throw (IOException)t;
				else
					throw new Error("Error invoking parse on Xerces",t);
			}
		}

		public Document getDocument() throws SAXException
		{
			try
			{
			  return(Document)mGetDocument.invoke(o,new Object[]{});
			}
			catch(Exception e)
			{
				if(e instanceof SAXException)
					throw (SAXException)e;
				else
					throw new Error("Error invoking getDocument on Xerces",e);
			}
		}
	}

	/**
	 * Interface to generalise between Xerces and JAX parsing. Works same way
	 * as Xerces DOMParser.
	 */
	interface MyDOMParser
	{
		/**
		 * @param is Input source used for parsing
		 * @throws SAXException
		 * @throws IOException
		 */
		void parse(InputSource is) throws SAXException,IOException;

		/**
		 * @return Document object that has been parsed
		 * @throws SAXException
		 */
		Document getDocument() throws SAXException;
	}

	/** @return A Xerces parser ready for use by this thread */
	private static MyDOMParser getParser()
	{
		synchronized(lAvailableParsers)
		{
			if(!lAvailableParsers.isEmpty())
			{
				return lAvailableParsers.removeFirst();
			}

			initStatics();
			if(bUsingXerces)
				return new MyXercesParser();
			else
				return new MyJAXParser();
		}
	}

	/**
	 * Call to release a Xerces parser when thread has finished with it.
	 * @param dp Parser no longer needed by thread
	 */
	private static void releaseParser(MyDOMParser dp)
	{
		synchronized(lAvailableParsers)
		{
			// If there's already plenty of parsers cached, throw it away
			if(lAvailableParsers.size()<MAXCACHEDPARSERS)
			{
				lAvailableParsers.addLast(dp);
			}
		}
	}


	/**
	 * Clones an XML document.
	 * @param dInput Input document
	 * @return Clone of document
	 */
	static public Document clone(Document dInput)
	{
		Document dOutput=createDocument();
		dOutput.appendChild(
			dOutput.importNode(dInput.getDocumentElement(),true));
		return dOutput;
	}

	/**
	 * Recursively obtains all text from all children of an XML node, except
	 * element children.
	 * @param n Node
	 * @return Text (empty string if none)
	 */
	static public String getText(Node n)
	{
		if(n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE)
		{
			return n.getNodeValue();
		}

		StringBuffer sbOutput=new StringBuffer();

		for(Node nChild=n.getFirstChild();nChild!=null;nChild=nChild.getNextSibling())
		{
			if(!(nChild instanceof Element))
				sbOutput.append(getText(nChild));
		}

		return sbOutput.toString();
	}

	/**
	 * Parses XML from a byte array and returns the document.
	 * @param abData Byte array containing an XML file
	 * @return DOM document
	 * @throws IOException
	 */
	public static Document parse(byte[] abData) throws IOException
	{
		return parse(new ByteArrayInputStream(abData),null);
	}

	/**
	 * Parses XML from a byte array and returns the document.
	 * @param abData Byte array containing an XML file
	 * @param fRelative During parsing, relative references (e.g. to DTDs) are
	 *   resolved as if the input stream was from this specified file (which
	 *   need not actually exist). Set null to use current directory.
	 * @return DOM document
	 * @throws IOException
	 */
	public static Document parse(byte[] abData,File fRelative) throws IOException
	{
		return parse(new ByteArrayInputStream(abData),fRelative);
	}

	/**
	 * Parses XML from an input stream and returns the document. The XML must not
	 * contain any references to other files e.g. DTDs.
	 * @param is Input stream (note that as far as I know this is not buffered
	 *   internally so for performance you might wish to buffer it beforehand if
	 *   it comes from a file or something slow)
	 * @return DOM document
	 * @throws IOException
	 */
	public static Document parse(InputStream is) throws IOException
	{
		return parse(is,null);
	}

	/**
	 * Parses XML from an input stream and returns the document.
	 * @param is Input stream (note that as far as I know this is not buffered
	 *   internally so for performance you might wish to buffer it beforehand if
	 *   it comes from a file or something slow)
	 * @param fRelative During parsing, relative references (e.g. to DTDs) are
	 *   resolved as if the input stream was from this specified file (which
	 *   need not actually exist). Set null to use current directory.
	 * @return DOM document
	 * @throws IOException
	 */
	public static Document parse(InputStream is,File fRelative) throws IOException
	{
		try
		{
			// Yes there is a reason why this isn't a NullPointerException, basically
			// some existing code relies on this exception being catchable.
			if(is==null) throw new XMLException("May not call with is=null");

			// null = current directory
			if(fRelative==null) fRelative=new File("!!dummy.xml");

			// According to the documentation, this should use an appropriate
			// algorithm to detect character encoding, i.e. default should be UTF-8
			// unless specified by <?xml?>. I do not entirely trust this to be the
			// case.
			InputSource isWithPath=new InputSource(is);
			try
			{
				isWithPath.setSystemId(fRelative.toURI().toString());
			}
			catch(SecurityException se)
			{
				//This fails for applets
			}

			MyDOMParser dp=getParser();
			dp.parse(isWithPath);
			Document d=dp.getDocument();
			releaseParser(dp);
			return d;
		}
		catch (SAXParseException spe)
		{
			// Error generated by the parser
			throw new XMLException("XML file is corrupt. "+
				"(Parsing error on line "+spe.getLineNumber()+": "+spe.getMessage()+")");
		}
		catch (SAXException sxe)
		{
			// Error generated by the parser
			throw new XMLException("System error. ("+sxe.toString()+")");
		}
	}

	/**
	 * Parses XML from an input reader and returns the document.
	 * <p>
	 * This is not ideal unless you are certain of character encoding, as the
	 * reader has predetermined the character encoding to be used, regardless of
	 * any information in the XML declaration.
	 * @param r Input reader (note that as far as I know this is not buffered
	 *   internally so for performance you might wish to buffer it beforehand if
	 *   it comes from a file or something slow)
	 * @param fRelative During parsing, relative references (e.g. to DTDs) are
	 *   resolved as if the input stream was from this specified file (which
	 *   need not actually exist). Set null to use current directory.
	 * @return DOM document
	 * @throws IOException
	 */
	public static Document parse(Reader r,File fRelative) throws IOException
	{
		try
		{
			// null = current directory
			if(fRelative==null) fRelative=new File("!!dummy.xml");

			InputSource isWithPath=new InputSource(r);
			try
			{
				isWithPath.setSystemId(fRelative.toURI().toString());
			}
			catch(SecurityException se)
			{
				// Happens in applets
			}

			MyDOMParser dp=getParser();
			dp.parse(isWithPath);
			Document d=dp.getDocument();
			releaseParser(dp);
			return d;
		}
		catch (SAXParseException spe)
		{
			// Error generated by the parser
			throw new XMLException("XML file is corrupt. "+
				"(Parsing error on line "+spe.getLineNumber()+": "+spe.getMessage()+")");
		}
		catch (SAXException sxe)
		{
			// Error generated by the parser
			throw new XMLException("System error. ("+sxe.toString()+")");
		}
	}

	/**
	 * Parses XML from a file and returns the document.
	 * @param f File to parse
	 * @return DOM document
	 * @throws IOException
	 */
	public static Document parse(File f) throws IOException
	{
		FileInputStream fis=new FileInputStream(f);
		try
		{
			Document dReturn=parse(new BufferedInputStream(fis),f);
			return dReturn;
		}
		finally
		{
			fis.close();
		}
	}

	/**
	 * Parse an InputSource
	 * @param is InputSource to parse
	 * @return DOM tree
	 * @throws IOException Any parsing or I/O error
	 */
	public static Document parse(InputSource is) throws IOException
	{
		try
		{
			MyDOMParser dp=getParser();
			dp.parse(is);
			Document d=dp.getDocument();
			releaseParser(dp);
			return d;
		}
		catch (SAXException e)
		{
			throw new XMLException("Error parsing document",e);
		}
	}

	/**
	 * Parses XML from a string and returns the document.
	 * @param s String to parse
	 * @param fRelative During parsing, relative references (e.g. to DTDs) are
	 *   resolved as if the input stream was from this specified file (which
	 *   need not actually exist). Set null to use current directory.
	 * @return DOM document
	 * @throws IOException Any parsing or I/O error
	 */
	public static Document parse(String s,File fRelative) throws IOException
	{
		return parse(new StringReader(s),fRelative);
	}

	/**
	 * Parses XML from a string and returns the document.
	 * @param s String to parse
	 * @return DOM document
	 * @throws XMLException Any parsing or I/O error
	 */
	public static Document parse(String s) throws XMLException
	{
		try
		{
			return parse(new StringReader(s),null);
		}
		catch(XMLException xe)
		{
			throw xe;
		}
		catch(IOException ioe)
		{
			// Can't happen! We're reading a string, what kind of I/O could go wrong?
			throw new XMLException(ioe);
		}
	}



	/**
	 * Creates new empty document
	 * @return New DOM document
	 */
	public static Document createDocument()
	{
		initStatics();
		if(bUsingXerces)
	  {
			try
			{
				return (Document)cDocumentImpl.newInstance();
			}
			catch(InstantiationException e)
			{
				throw new Error(e);
			}
			catch(IllegalAccessException e)
			{
				throw new Error(e);
			}
	  }
		else // dbf
		{
			try
			{
				return dbf.newDocumentBuilder().newDocument();
			}
			catch(ParserConfigurationException e)
			{
				throw new Error(e);
			}
		}
	}

	/**
	 * Serialises an XML document to the given stream.
	 * @param d Document
	 * @param os Output stream (data will be written in UTF-8 format)
	 * @param bIncludeXMLDeclaration If true, includes XML declaration
	 * @throws IOException In the event of I/O or transformation error
	 */
	public static void save(Document d,OutputStream os,
		boolean bIncludeXMLDeclaration) throws IOException
	{
		save(d,
			new StreamResult(new OutputStreamWriter(os,"UTF-8")),
			bIncludeXMLDeclaration);
	}

	private static void save(Document d,Result r,boolean bIncludeXMLDeclaration)
		throws IOException
	{
		try
		{
			synchronized(oTransformSynch)
			{
				Transformer tCopy=getTransformerFactory().newTransformer();
				tCopy.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
				if(!bIncludeXMLDeclaration)
					tCopy.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
				tCopy.transform(
					new DOMSource(d),
					r);
			}
		}
		catch(TransformerException tce)
		{
			throw new XMLException("Failed to save document: "+tce);
		}
	}

	/**
	 * Serialises an XML document to the given writer.
	 * @param d Document
	 * @param w Output writer
	 * @param bIncludeXMLDeclaration If true, includes XML declaration
	 * @throws IOException In the event of I/O or transformation error
	 */
	public static void save(Document d,Writer w,
		boolean bIncludeXMLDeclaration) throws IOException
	{
		save(d,
			new StreamResult(w),
			bIncludeXMLDeclaration);
	}

	/**
	 * Saves an XML document, including XML declaration.
	 * @param d Document
	 * @param os Output stream (data will be written in UTF-8 format)
	 * @throws IOException In the event of I/O or transformation error
	 */
	public static void save(Document d,OutputStream os) throws IOException
	{
		save(d,os,true);
	}

	/**
	 * Saves an XML document to file, including XML declaration.
	 * @param d Document to save
	 * @param f Target file
	 * @throws IOException In the event of I/O or transformation error
	 */
	public static void save(Document d,File f) throws IOException
	{
		FileOutputStream fos=new FileOutputStream(f);
		BufferedOutputStream bos=new BufferedOutputStream(fos);
		save(d,bos);
		bos.flush();
		fos.close();
	}

	/**
	 * Saves an XML document to a byte array.
	 * @param d Document to save
	 * @param bIncludeXMLDeclaration True if XML declaration should be included.
	 * @throws IOException In the event of I/O or transformation error
	 * @return Byte data for document
	 */
	public static byte[] saveBytes(Document d,boolean bIncludeXMLDeclaration) throws IOException
	{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		save(d,baos,bIncludeXMLDeclaration);
		return baos.toByteArray();
	}

	/**
	 * Saves an XML document to a byte array, including XML declaration.
	 * @param d Document to save
	 * @throws IOException In the event of I/O or transformation error
	 * @return Byte data for document
	 */
	public static byte[] saveBytes(Document d) throws IOException
	{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		save(d,baos,true);
		return baos.toByteArray();
	}

	/**
	 * Saves an XML document to a string.
	 * @param d Document to save
	 * @param bIncludeXMLDeclaration True if XML declaration should be included.
	 * @throws IOException In the event of I/O or transformation error
	 * @return String of document
	 */
	public static String saveString(Document d,boolean bIncludeXMLDeclaration) throws IOException
	{
		StringWriter sw=new StringWriter();
		save(d,sw,bIncludeXMLDeclaration);
		return sw.toString();
	}

	/**
	 * Saves an XML document to a byte array, including XML declaration.
	 * @param d Document to save
	 * @throws IOException In the event of I/O or transformation error
	 * @return Byte data for document
	 */
	public static String saveString(Document d) throws IOException
	{
		StringWriter sw=new StringWriter();
		save(d,sw,true);
		return sw.toString();
	}

	/**
	 * Saves an XML element to file, omitting XML declaration.
	 * <p>
	 * This element and all subelements are saved, but
	 * anything else (elements above this in the tree, or sibling elements)
	 * is not.
	 * @param e Element to save
	 * @param f File to save it to
	 * @throws IOException In the event of I/O or transformation error
	 */
	public static void save(Element e,File f) throws IOException
	{
		FileOutputStream fos=new FileOutputStream(f);
		BufferedOutputStream bos=new BufferedOutputStream(fos);
		save(e,bos);
		bos.flush();
		fos.close();
	}

	/**
	 * Serialises an XML element to an output stream, omitting XML declaration.
	 * <p>
	 * This element and all subelements are saved, but
	 * anything else (elements above this in the tree, or sibling elements)
	 * is not.
	 * @param e Element to save
	 * @param os Output stream to save it to
	 * @throws IOException In the event of I/O or transformation error
	 */
	public static void save(Element e,OutputStream os) throws IOException
	{
		save(e,os,true);
	}


	/**
	 * Serialises an XML element to an output stream.
 	 * <p>
	 * This element and all subelements are saved, but
	 * anything else (elements above this in the tree, or sibling elements)
	 * is not.
	 * @param e Element to save
	 * @param os Output stream to save it to
	 * @param bIncludeXMLDeclaration If true, include XML declaration
	 * @throws IOException In the event of I/O or transformation error
	 */
	public static void save(Element e,OutputStream os,
		boolean bIncludeXMLDeclaration) throws IOException
	{
		// If it's all there is in the document, save time by not copying it
		if(e.getOwnerDocument().getDocumentElement()==e)
		{
			save(e.getOwnerDocument(),os,bIncludeXMLDeclaration);
		}
		else
		{
			// Copy to new document because we can only save whole doc
			Document d=createDocument();
			d.appendChild(d.importNode(e,true));
			save(d,os,bIncludeXMLDeclaration);
		}
	}

	/**
	 * Serialises an XML element to an output writer.
	 * <p>
	 * This element and all subelements are saved, but
	 * anything else (elements above this in the tree, or sibling elements)
	 * is not.
	 * @param e Element to save
	 * @param w Output writer to save it to
	 * @param bIncludeXMLDeclaration If true, include XML declaration
	 * @throws IOException In the event of I/O or transformation error
	 */
	public static void save(Element e,Writer w,
		boolean bIncludeXMLDeclaration) throws IOException
	{
		// If it's all there is in the document, save time by not copying it
		if(e.getOwnerDocument().getDocumentElement()==e)
		{
			save(e.getOwnerDocument(),w,bIncludeXMLDeclaration);
		}
		else
		{
			// Copy to new document because we can only save whole doc
			Document d=createDocument();
			d.appendChild(d.importNode(e,true));
			save(d,w,bIncludeXMLDeclaration);
		}
	}

	/**
	 * Saves an XML element to a byte array, omitting XML declaration.
	 * <p>
	 * This element and all subelements are saved, but
	 * anything else (elements above this in the tree, or sibling elements)
	 * is not.
	 * @param e Element to save
	 * @throws IOException In the event of I/O or transformation error
	 * @return Byte data for element
	 */
	public static byte[] saveBytes(Element e) throws IOException
	{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		save(e,baos,false);
		return baos.toByteArray();
	}

	/**
	 * Saves an XML element to a byte array.
	 * <p>
	 * This element and all subelements are saved, but
	 * anything else (elements above this in the tree, or sibling elements)
	 * is not.
	 * @param e Element to save
	 * @param bIncludeXMLDeclaration True if XML declaration should be included.
	 * @throws IOException In the event of I/O or transformation error
	 * @return Byte data for element
	 */
	public static byte[] saveBytes(Element e,boolean bIncludeXMLDeclaration) throws IOException
	{
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		save(e,baos,bIncludeXMLDeclaration);
		return baos.toByteArray();
	}

	/**
	 * Saves an XML element to a string, omitting XML declaration.
	 * <p>
	 * This element and all subelements are saved, but
	 * anything else (elements above this in the tree, or sibling elements)
	 * is not.
	 * @param e Element to save
	 * @throws IOException In the event of I/O or transformation error
	 * @return String of element
	 */
	public static String saveString(Element e) throws IOException
	{
		StringWriter sw=new StringWriter();
		save(e,sw,false);
		return sw.toString();
	}

	/**
	 * Saves an XML element to a string.
	 * <p>
	 * This element and all subelements are saved, but
	 * anything else (elements above this in the tree, or sibling elements)
	 * is not.
	 * @param e Element to save
	 * @param bIncludeXMLDeclaration True if XML declaration should be included.
	 * @throws IOException In the event of I/O or transformation error
	 * @return String of element
	 */
	public static String saveString(Element e,boolean bIncludeXMLDeclaration) throws IOException
	{
		StringWriter sw=new StringWriter();
		save(e,sw,bIncludeXMLDeclaration);
		return sw.toString();
	}


	/**
	 * Returns child element with given name.
	 * <p>
	 * If there is more than one child with that name, the first is returned.
	 * @param nParent Parent node
	 * @param sName Tag name
	 * @throws XMLException If there is no element with that name
	 * @return Child element with that name
	 */
	public static Element getChild(Node nParent,String sName) throws XMLException
	{
		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element) n;
				if(e.getTagName().equals(sName)) return e;
			}
		}
		throw new XMLException("Element <"+sName+"> not found");
	}

	/**
	 * Return a child element with a given name, creating it if necessary.
	 * If there is more than one child with that name, the first is returned.
	 * @param nParent Parent node
	 * @param sName Tag name
	 * @return Child element with that name
	 */
	public static Element getOrCreateChild(Node nParent, String sName)
	{
		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element) n;
				if(e.getTagName().equals(sName)) return e;
			}
		}
		// Not there, create one
		return createChild(nParent, sName);
	}

	/**
	 * Returns value of attribute with given name.
	 * @param e Element Element whose attribute you're checking
	 * @param sName Name of attribute
	 * @return Attribute value (will not be null)
	 * @throws XMLException If the attribute wasn't specified
	 */
	public static String getRequiredAttribute(Element e,String sName) throws XMLException
	{
		if(!e.hasAttribute(sName))
			throw new XMLException("Element <"+e.getTagName()+">: required attribute "+sName+" not found");
		return e.getAttribute(sName);
	}

	/**
	 * Check whether child element with given name exists.
	 * @param nParent Parent node
	 * @param sName Tag name
	 * @return True if child of that name exists, false if there are none
	 */
	public static boolean hasChild(Node nParent,String sName)
	{
		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element) n;
				if(e.getTagName().equals(sName)) return true;
			}
		}
		return false;
	}

	/**
	 * Returns all (direct) child elements with given name.
	 * @param nParent Parent node
	 * @param sName Tag name
	 * @return Array of all child elements with the given name (empty if none)
	 */
	public static Element[] getChildren(Node nParent,String sName)
	{
		List<Element> lElements=new LinkedList<Element>();

		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element) n;
				if(e.getTagName().equals(sName)) lElements.add(e);
			}
		}

		return lElements.toArray(new Element[0]);
	}

	/**
	 * Returns the first (in document order) child element with given name, even if it is a child
	 * of other elements.
	 * @param nParent Parent node
	 * @param sName Tag name
	 * @return the element, (null if none)
	 */
	public static Element getNestedChild(Node nParent,String sName)
	{
		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element) n;
				if(e.getTagName().equals(sName)) return e;
				Element eRecurse = getNestedChild(n,sName);
				if (eRecurse != null) return eRecurse;
			}
		}

		return null;
	}

	/**
	 * Returns all child elements with given name, even if they are children
	 * of other elements.
	 * @param nParent Parent node
	 * @param sName Tag name
	 * @return Array of all child elements with the given name (empty if none)
	 */
	public static Element[] getNestedChildren(Node nParent,String sName)
	{
		List<Element> lElements=new LinkedList<Element>();

		getNestedChildren(lElements,nParent,sName);

		return lElements.toArray(new Element[0]);
	}

	/**
	 * Adds all child elements with given name to the list.
	 * @param lElements List of elements
	 * @param nParent Parent node
	 * @param sName Tag name
	 */
	private static void getNestedChildren(List<Element> lElements,Node nParent,String sName)
	{
		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element e=(Element) n;
				if(e.getTagName().equals(sName)) lElements.add(e);
				getNestedChildren(lElements,e,sName);
			}
		}
	}

	/**
	 * Returns the text inside each (direct) child element with given name.
	 * @param nParent Parent node
	 * @param sName Tag name
	 * @return Array of strings with the text from each element (or a zero element array if none).
	 */
	public static String[] getTextFromChildren(Node nParent,String sName)
	{
		Element[] ae=getChildren(nParent,sName);
		String[] as=new String[ae.length];
		for (int iChild= 0; iChild < as.length; iChild++)
		{
			as[iChild]=XML.getText(ae[iChild]);
		}
		return as;
	}

	/**
	 * Returns the value of the specified attribute of each (direct) child element with given name.
	 * @param nParent Parent node
	 * @param sName Tag name
	 * @param sAttribute the name of the attribure.
	 * @return Array of strings with the text from each element
	 * @throws XMLException if any of the children is missing this attribute.
	 */
	public static String[] getAttributeFromChildren(Node nParent,String sName,String sAttribute) throws XMLException
	{
		Element[] ae=getChildren(nParent,sName);
		String[] as=new String[ae.length];
		for (int iChild= 0; iChild < as.length; iChild++)
		{
			as[iChild]=XML.getRequiredAttribute(ae[iChild], sAttribute);
		}
		return as;
	}

	/**
	 * Returns all (direct) child elements.
	 * @param nParent Parent node
	 * @return Array of all child elements (empty if none)
	 */
	public static Element[] getChildren(Node nParent)
	{
		List<Element> lElements=new LinkedList<Element>();

		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				lElements.add((Element)n);
			}
		}

		return lElements.toArray(new Element[0]);
	}

	/**
	 * Creates, adds, and returns child element with given name.
	 * @param nParent Parent node
	 * @param sName Tag name
	 * @return Newly-created element
	 */
	public static Element createChild(Node nParent,String sName)
	{
		Element eNew;
		if(nParent instanceof Document)
		  eNew=((Document)nParent).createElement(sName);
		else
		  eNew=nParent.getOwnerDocument().createElement(sName);
		nParent.appendChild(eNew);
		return eNew;
	}

	/**
	 * Gets text from inside child element.
	 * @param n Parent node
	 * @param sChild Tag name of child element
	 * @return Text, empty string if none.
	 * @throws XMLException If child element does not exist
	 */
	public static String getText(Node n,String sChild) throws XMLException
	{
		return getText(getChild(n,sChild));
	}

	/**
	 * Gets integer from inside named child element.
	 * @param n Parent node
	 * @param sChild Tag name of child element
	 * @return Integer value
	 * @throws XMLException If the element doesn't exist or doesn't contain a
	 *   valid integer.
	 */
	public static int getInt(Node n,String sChild) throws XMLException
	{
		try
		{
			return Integer.parseInt(getText(n,sChild));
		}
		catch(NumberFormatException nfe)
		{
			throw new XMLException("Not a valid integer");
		}
	}

	/**
	 * Gets double value from inside named child element.
	 * @param n Parent node
	 * @param sChild Tag name of child element
	 * @return Integer value
	 * @throws XMLException If the element doesn't exist or doesn't contain a
	 *   valid double.
	 */
	public static double getDouble(Node n,String sChild) throws XMLException
	{
		try
		{
			return Double.parseDouble(getText(n,sChild));
		}
		catch(NumberFormatException nfe)
		{
			throw new XMLException("Not a valid double");
		}
	}

	/**
	 * Adds a comment to the parent.
	 * @param nParent Parent node
	 * @param sComment Comment text
	 */
	public static void createComment(Node nParent,String sComment)
	{
		nParent.appendChild(
			nParent.getOwnerDocument().createComment(sComment));
	}

	/**
	 * Creates child of given name including string specified.
	 * @param n Parent node
	 * @param sChild Tag name of new child
	 * @param sValue String to place inside new child
	 */
	public static void createText(Node n,String sChild,String sValue)
	{
		createText(createChild(n,sChild),sValue);
	}

	/**
	 * Creates child of given name including int specified.
	 * @param n Parent node
	 * @param sChild Tag name of new child
	 * @param iValue Number to place inside new child
	 */
	public static void createInt(Node n,String sChild,int iValue)
	{
		createText(n,sChild,iValue+"");
	}

	/**
	 * Creates child of given name including double specified.
	 * @param n Parent node
	 * @param sChild Tag name of new child
	 * @param dValue Number to place inside new child
	 */
	public static void createDouble(Node n,String sChild,double dValue)
	{
		createText(n,sChild,dValue+"");
	}

	/**
	 * Creates a Text node and adds it to the given node.
	 * @param n Node that will contain text
	 * @param sText Value of text (null = empty string)
	 */
	public static void createText(Node n,String sText)
	{
		if(sText==null) sText="";
		Text tNew=n.getOwnerDocument().createTextNode(sText);
		n.appendChild(tNew);
	}

	/**
	 * Sets a child element's text to the given value, replacing any existing text.
	 * @param n Parent node
	 * @param sChild Child element's tag name (must exist!)
	 * @param sText Text for child node
	 * @throws XMLException If the child node doesn't exist.
	 */
	public static void setText(Node n,String sChild,String sText) throws XMLException
	{
		setText(getChild(n,sChild),sText);
	}

	/**
	 * Sets the contents of this node to the given text string, discarding any
	 * existing contents.
	 * @param n Node
	 * @param sText Text value to place inside
	 */
	public static void setText(Node n,String sText)
	{
		// Clear existing children
		removeChildren(n);

		// Set text
		createText(n,sText);
	}

	/**
	 * Removes all children from given node.
	 * @param n Soon-to-be childless node
	 */
	public static void removeChildren(Node n)
	{
		// Clear existing children
		NodeList nl=n.getChildNodes();
		for(int i=nl.getLength()-1;i>=0;i--)
			n.removeChild(nl.item(i));
	}

	/**
	 * Removes node from DOM tree
	 * @param n Soon to be parentless node
	 */
	public static void remove(Node n)
	{
		if(n.getParentNode()!=null)
			n.getParentNode().removeChild(n);
	}

	/**
	 * Escapes a string using XML entities where necessary (use this when writing
	 * XML 'by hand' rather than using save methods).
	 * @param sSource String that may contain triangle brackets and quotes
	 * @return String that will contain XML entities instead, and has illegal
	 *   control characters stripped
	 */
	public static String escape(String sSource)
	{
		StringBuffer sbOutput=new StringBuffer();
		for(int i=0;i<sSource.length();i++)
		{
			char c=sSource.charAt(i);

			// Skip control characters that are forbidden by XML
			if(c<32)
			{
				if(c!=9 && c!=10 && c!=13) continue;
			}

			switch(c)
			{
				case '&' : sbOutput.append("&amp;"); break;
				case '<' : sbOutput.append("&lt;"); break;
				case '>' : sbOutput.append("&gt;"); break;
				case '\"' : sbOutput.append("&quot;"); break;
				case '\'' : sbOutput.append("&apos;"); break;
				default: sbOutput.append(c); break;
			}
		}

		return sbOutput.toString();
	}

	/**
	 * Replaces tokens safely in a string.
	 * @param sValue String to check
	 * @param sBorder Border string e.g. %%; must be made of characters that don't
	 *   need escaping in regexps
	 * @param mReplace Replacement map
	 * @return The new or unchanged string
	 */
	public static String replaceTokens(String sValue,String sBorder,Map mReplace)
	{
		String sNew=replaceStringTokensInternal(sValue,sBorder,mReplace,true);
		if(sNew==null)
			return sValue;
		else
			return sNew;
	}

	/**
	 * Replaces tokens safely in a string.
	 * @param sInput String to check
	 * @param sBorder Border string e.g. %%; must be made of characters that don't
	 *   need escaping in regexps
	 * @param sToken Token to replace (do not include %% at either end)
	 * @param sValue Value for token
	 * @return The new or unchanged string
	 */
	public static String replaceToken(String sInput,String sBorder,String sToken,String sValue)
	{
		Map<String,String> mReplace=new HashMap<String,String>();
		mReplace.put(sToken,sValue);
		return replaceTokens(sInput,sBorder,mReplace);
	}

	/**
	 * Replaces tokens in a string.
	 * @param sValue String to check
	 * @param sBorder Border string e.g. %%; must be made of characters that don't
	 *   need escaping in regexps
	 * @param mReplace Replacement map (containing strings only)
	 * @param bFixBlanks If true, changes %%%% to %% - this should be done only
	 *   after last changes so don't do it if replaceNodeTokens will be called.
	 * @return null if there was no change to the string; otherwise, the new
	 *   string
	 */
	private static String replaceStringTokensInternal(String sValue,String sBorder,Map mReplace,boolean bFixBlanks)
	{
		// Get value and look for tokens. If there aren't any, bail now.
		if(mReplace==null || sValue.indexOf(sBorder)==-1) return null;
		Pattern pTokens=Pattern.compile(sBorder+"(.*?)"+sBorder,Pattern.DOTALL);

		StringBuffer sbResult=new StringBuffer();
		Matcher m=pTokens.matcher(sValue);
		while(m.find())
		{
			String sKey=m.group(1);
			if(sKey.equals(""))
				m.appendReplacement(sbResult,sBorder);
			else
			{
				String sMatch=(String)mReplace.get(sKey);
				if(sMatch==null)
					m.appendReplacement(sbResult,sBorder+sKey+sBorder);
				else
				{
					sMatch=sMatch.replaceAll("\\\\","\\\\\\\\").replaceAll("\\$","\\\\\\$");
					m.appendReplacement(sbResult,sMatch);
				}
			}
		}
		m.appendTail(sbResult);

		return sbResult.toString();
	}

	private static void replaceNodeTokens(Text t,String sBorder,Map mReplace)
	{
		// Get value and look for tokens. If there aren't any, bail now.
		String sValue=t.getData();
		if(mReplace==null || sValue.indexOf(sBorder)==-1) return;

		StringBuffer sbBefore=new StringBuffer();

		Pattern pTokens=Pattern.compile(sBorder+"(.*?)"+sBorder,Pattern.DOTALL);
		Matcher m=pTokens.matcher(sValue);
		while(m.find())
		{
			String sKey=m.group(1);
			if(sKey.equals(""))
				m.appendReplacement(sbBefore,sBorder);
			else
			{
				Node nMatch=(Node)mReplace.get(sKey);
				if(nMatch==null)
					m.appendReplacement(sbBefore,sBorder+sKey+sBorder);
				else
				{
					// OK, found a token. Get everything before it into the buffer...
					m.appendReplacement(sbBefore,"");
					// ...create a new text node with the 'before' text...
					Text tBefore=t.getOwnerDocument().createTextNode(sbBefore.toString());
					t.getParentNode().insertBefore(tBefore,t);
					sbBefore.setLength(0);
					// ...add the new node...
					t.getParentNode().insertBefore(
						t.getOwnerDocument().importNode(nMatch,true),t);
				}
			}
		}
		m.appendTail(sbBefore);
		t.setData(sbBefore.toString());
	}

	/**
	 * Replace tokens in the form %%token-ID%%, anywhere they occur in text
	 * or attribute values. %%%% becomes a literal %%. Anything not matched
	 * as a token is left alone.
	 * @param n Node to begin with
	 * @param mReplace Map of token-ID to replacement text
	 */
	public static void replaceTokens(Node n, Map<String,? extends Object> mReplace)
	{
		replaceTokens(n,"%%",mReplace);
	}

	/**
	 * Replace tokens in the form %%token-ID%%, anywhere they occur in text
	 * or attribute values. %%%% becomes a literal %%. Anything not matched
	 * as a token is left alone. (You can set the border string to anything, not
	 * just %%.)
	 * @param n Node to begin with
	 * @param sBorder Border string e.g. %%; must be made of characters that don't
	 *   need escaping in regexps
	 * @param mReplace Map of token-ID to replacement text (String) or XML content
	 *   (Node)
	 */
	public static void replaceTokens(Node n, String sBorder,Map<String,? extends Object> mReplace)
	{
		// Separate map into two depending on type
		Map<String,String> mStrings=new HashMap<String,String>();
		Map<String,Node> mNodes=new HashMap<String,Node>();
		Integer cnt=0;
		for(Map.Entry<String,? extends Object> me : mReplace.entrySet())
		{
			cnt++;
			if(me.getValue()==null)
			{
				mStrings.put(me.getKey(),"");
			}
			else if(me.getValue() instanceof String)
			{
				mStrings.put(me.getKey(),(String)me.getValue());
			}
			else if(me.getValue() instanceof Node)
			{
				mNodes.put(me.getKey(),(Node)me.getValue());
			}
			else 
			{
				throw new IllegalArgumentException("replaceTokens map may contain only Strings and Nodes "+cnt.toString());
			}
		}

		replaceTokens(n,sBorder,mStrings,mNodes);
	}

	/**
	 * Replace tokens in the form %%token-ID%%, anywhere they occur in text
	 * or attribute values. %%%% becomes a literal %%. Anything not matched
	 * as a token is left alone. (You can set the border string to anything, not
	 * just %%.)
	 * @param n Node to begin with
	 * @param sBorder Border string e.g. %%; must be made of characters that don't
	 *   need escaping in regexps
	 * @param mStringReplace Map of token-ID to replacement text (String)
	 * @param mNodeReplace Map of token-ID to replacement XML content (Node)
	 */
	private static void replaceTokens(Node n,String sBorder,Map mStringReplace,Map mNodeReplace)
	{
		if(n instanceof Text)
		{
			Text t=(Text)n;
			// Only do 'final' replaces if nodereplace is false
			String sNew=replaceStringTokensInternal(t.getData(),sBorder,mStringReplace,mNodeReplace==null);
			if(sNew!=null) t.setData(sNew);
			replaceNodeTokens(t,sBorder,mNodeReplace);
		}
		else if(n instanceof Element)
		{
			// Handle all attributes
			Element e=(Element)n;
			NamedNodeMap nnm=e.getAttributes();
			for(int i=0;i<nnm.getLength();i++)
			{
				String sNew=replaceStringTokensInternal(nnm.item(i).getNodeValue(),sBorder,mStringReplace,true);
				if(sNew!=null) nnm.item(i).setNodeValue(sNew);
			}

			// Coalesce text children
			NodeList nl=e.getChildNodes();
			for(int i=0;i<nl.getLength()-1;i++)
			{
				Node nHere=nl.item(i),nNext=nl.item(i+1);
				if( (nHere instanceof Text) && (nNext instanceof Text) )
				{
					nHere.setNodeValue(nHere.getNodeValue()+nNext.getNodeValue());
					e.removeChild(nNext);
					// Consider this node again next time
					i--;
				}
			}

			// Recurse to children
			for(int i=0;i<nl.getLength();i++)
			{
				replaceTokens(nl.item(i),sBorder,mStringReplace,mNodeReplace);
			}
		}
		else if(n instanceof Document)
		{
			replaceTokens(((Document)n).getDocumentElement(),sBorder,mStringReplace,mNodeReplace);
		}
	}

	/**
	 * Finds an element with the given attribute/value pair.
	 * @param d document
	 * @param sAttribute Desired attribute name
	 * @param sValue Desired attribute value
	 * @return First element matching that specification
	 * @throws XMLException If there is no element with that attribute (note:
	 *   if you don't want this behaviour, try the other find method which
	 *   only returns null)
	 */
	public static Element find(Document d,String sAttribute,String sValue)
	  throws XMLException
	{
		Element e=find(d.getDocumentElement(),sAttribute,sValue);
		if(e!=null) return e;
		throw new XMLException("Failed to find element with "+sAttribute+"="+sValue);
	}

	/**
	 * Recursively finds element with the given attribute/value pair.
	 * @param eParent Parent to search within (will also be checked)
	 * @param sAttribute Desired attribute name
	 * @param sValue Desired attribute value
	 * @return First element matching that specification, or null if none
	 */
	public static Element find(Element eParent,String sAttribute,String sValue)
	{
		// If this matches, return it
		if(sValue.equals(eParent.getAttribute(sAttribute)))
			return eParent;

		// Try all children
		for(Node n=eParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element eResult=find((Element)n,sAttribute,sValue);
				if(eResult!=null) return eResult;
			}
		}

		// Didn't find it within this subtree
		return null;
	}

	/**
	 * Recursively finds all elements with the given attribute/value pair.
	 * @param dParent Parent to search within
	 * @param sAttribute Desired attribute name
	 * @param sValue Desired attribute value
	 * @return All elements matching that request, or empty array if none
	 */
	public static Element[] findAll(Document dParent,String sAttribute,String sValue)
	{
		return findAll(dParent.getDocumentElement(),sAttribute,sValue);
	}

	/**
	 * Recursively finds all elements with the given attribute/value pair.
	 * @param eParent Parent to search within (will also be checked)
	 * @param sAttribute Desired attribute name
	 * @param sValue Desired attribute value
	 * @return All elements matching that request, or empty array if none
	 */
	public static Element[] findAll(Element eParent,String sAttribute,String sValue)
	{
		List<Element> lResult=new LinkedList<Element>();
		findAllInternal(eParent,sAttribute,sValue,lResult);
		return lResult.toArray(new Element[0]);
	}

	/**
	 * Recursively finds all elements with the given attribute/value pair.
	 * @param eParent Parent to search within (will also be checked)
	 * @param sAttribute Desired attribute name
	 * @param sValue Desired attribute value
	 * @param lResult List to which matching elements are added
	 */
	private static void findAllInternal(Element eParent,String sAttribute,String sValue,List<Element> lResult)
	{
		// If this matches, return it
		if(sValue.equals(eParent.getAttribute(sAttribute)))
			lResult.add(eParent);

		// Try all children
		for(Node n=eParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				findAllInternal((Element)n,sAttribute,sValue,lResult);
			}
		}
	}


	/**
	 * Non-recursively test whetere there is an element from among the children of the specified
	 * parent, with the given attribute/value pair.
	 * @param nParent Parent to search within
	 * @param sTag Required tag name (null if you don't care)
	 * @param sAttribute Desired attribute name
	 * @param sValue Desired attribute value
	 * @return First element matching that specification
	 */
	public static boolean hasChildWithAttribute(Node nParent,String sTag,String sAttribute,String sValue)
	{
		// Try all children
		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element eChild=(Element)n;
				if(sTag!=null && !sTag.equals(eChild.getTagName())) continue;
				if(sValue.equals(eChild.getAttribute(sAttribute)))
					return true;
			}
		}
		return false;
	}

	/**
	 * Non-recursively finds element from among the children of the specified
	 * parent, with the given attribute/value pair.
	 * @param nParent Parent to search within
	 * @param sTag Required tag name (null if you don't care)
	 * @param sAttribute Desired attribute name
	 * @param sValue Desired attribute value
	 * @return First element matching that specification
	 * @throws XMLException If element can't be found
	 */
	public static Element getChildWithAttribute(Node nParent,String sTag,String sAttribute,String sValue)
		throws XMLException
	{
		// Try all children
		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element eChild=(Element)n;
				if(sTag!=null && !sTag.equals(eChild.getTagName())) continue;
				if(sValue.equals(eChild.getAttribute(sAttribute)))
					return eChild;
			}
		}

		// Didn't find it within this subtree
		throw new XMLException("Failed to find element with "+sAttribute+"="+sValue);
	}

	/**
	 * Non-recursively finds all elements from among the children of the specified
	 * parent, with the given attribute/value pair.
	 * (same as above, but returns an array)
	 * @param nParent Parent to search within
	 * @param sTag Required tag name (null if you don't care)
	 * @param sAttribute Desired attribute name
	 * @param sValue Desired attribute value
	 * @return Array of elements matching that description
	 */
	public static Element[] getChildrenWithAttribute(Node nParent,String sTag,String sAttribute,String sValue)
	{
		List<Element> list = new LinkedList<Element>();
		// Try all children
		for(Node n=nParent.getFirstChild();n!=null;n=n.getNextSibling())
		{
			if(n instanceof Element)
			{
				Element eChild=(Element)n;
				if(sTag!=null && !sTag.equals(eChild.getTagName())) continue;
				if(sValue.equals(eChild.getAttribute(sAttribute)))
				{
				    list.add(eChild);
				}
			}
		}

		return list.toArray(new Element[0]);
	}



	/**
	 * (Recursively) import all child nodes of an element into another element.
	 * @param eTarget The element to import them into
	 * @param eSource The element to import them from
	 */
	public static void importChildren(Element eTarget, Element eSource)
	{
		for(Node n=eSource.getFirstChild();n!=null;n=n.getNextSibling())
		{
			eTarget.appendChild(eTarget.getOwnerDocument().importNode(n,true));
		}
	}

	/**
	 * Remove all the children of node eSourceParent and append them (in the same order) to eTargetParent.
	 * @param eSourceParent
	 * @param eTargetParent
	 */
	public static void moveChildren(Node eSourceParent, Element eTargetParent) {
		for (Node n=eSourceParent.getFirstChild();n!=null;)
		{
			Node next=n.getNextSibling();
			eTargetParent.appendChild(n);
			n = next;
		}
	}

	/**
	 * Converts a DOM nodelist (which must be of elements, unless you enjoy
	 * ClassCastExceptions) to an array.
	 * @param nl Node list
	 * @return Array of same nodes
	 */
	public static Element[] getElementArray(NodeList nl)
	{
		Element[] ae=new Element[nl.getLength()];
		for(int i=0;i<ae.length;i++)
			ae[i]=(Element)nl.item(i);
		return ae;
	}
}
