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
/**
 * StartReturn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package om.axis.qengine;

public class StartReturn  implements java.io.Serializable {
    private java.lang.String CSS;
    private java.lang.String XHTML;
    private java.lang.String progressInfo;
    private java.lang.String questionSession;
    private om.axis.qengine.Resource[] resources;

    public StartReturn() {
    }

    public StartReturn(
           java.lang.String CSS,
           java.lang.String XHTML,
           java.lang.String progressInfo,
           java.lang.String questionSession,
           om.axis.qengine.Resource[] resources) {
           this.CSS = CSS;
           this.XHTML = XHTML;
           this.progressInfo = progressInfo;
           this.questionSession = questionSession;
           this.resources = resources;
    }


    /**
     * Gets the CSS value for this StartReturn.
     * 
     * @return CSS
     */
    public java.lang.String getCSS() {
        return CSS;
    }


    /**
     * Sets the CSS value for this StartReturn.
     * 
     * @param CSS
     */
    public void setCSS(java.lang.String CSS) {
        this.CSS = CSS;
    }


    /**
     * Gets the XHTML value for this StartReturn.
     * 
     * @return XHTML
     */
    public java.lang.String getXHTML() {
        return XHTML;
    }


    /**
     * Sets the XHTML value for this StartReturn.
     * 
     * @param XHTML
     */
    public void setXHTML(java.lang.String XHTML) {
        this.XHTML = XHTML;
    }


    /**
     * Gets the progressInfo value for this StartReturn.
     * 
     * @return progressInfo
     */
    public java.lang.String getProgressInfo() {
        return progressInfo;
    }


    /**
     * Sets the progressInfo value for this StartReturn.
     * 
     * @param progressInfo
     */
    public void setProgressInfo(java.lang.String progressInfo) {
        this.progressInfo = progressInfo;
    }


    /**
     * Gets the questionSession value for this StartReturn.
     * 
     * @return questionSession
     */
    public java.lang.String getQuestionSession() {
        return questionSession;
    }


    /**
     * Sets the questionSession value for this StartReturn.
     * 
     * @param questionSession
     */
    public void setQuestionSession(java.lang.String questionSession) {
        this.questionSession = questionSession;
    }


    /**
     * Gets the resources value for this StartReturn.
     * 
     * @return resources
     */
    public om.axis.qengine.Resource[] getResources() {
        return resources;
    }


    /**
     * Sets the resources value for this StartReturn.
     * 
     * @param resources
     */
    public void setResources(om.axis.qengine.Resource[] resources) {
        this.resources = resources;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof StartReturn)) return false;
        StartReturn other = (StartReturn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.CSS==null && other.getCSS()==null) || 
             (this.CSS!=null &&
              this.CSS.equals(other.getCSS()))) &&
            ((this.XHTML==null && other.getXHTML()==null) || 
             (this.XHTML!=null &&
              this.XHTML.equals(other.getXHTML()))) &&
            ((this.progressInfo==null && other.getProgressInfo()==null) || 
             (this.progressInfo!=null &&
              this.progressInfo.equals(other.getProgressInfo()))) &&
            ((this.questionSession==null && other.getQuestionSession()==null) || 
             (this.questionSession!=null &&
              this.questionSession.equals(other.getQuestionSession()))) &&
            ((this.resources==null && other.getResources()==null) || 
             (this.resources!=null &&
              java.util.Arrays.equals(this.resources, other.getResources())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getCSS() != null) {
            _hashCode += getCSS().hashCode();
        }
        if (getXHTML() != null) {
            _hashCode += getXHTML().hashCode();
        }
        if (getProgressInfo() != null) {
            _hashCode += getProgressInfo().hashCode();
        }
        if (getQuestionSession() != null) {
            _hashCode += getQuestionSession().hashCode();
        }
        if (getResources() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getResources());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getResources(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(StartReturn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://om.open.ac.uk/", "StartReturn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CSS");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CSS"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("XHTML");
        elemField.setXmlName(new javax.xml.namespace.QName("", "XHTML"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("progressInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("", "progressInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("questionSession");
        elemField.setXmlName(new javax.xml.namespace.QName("", "questionSession"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resources");
        elemField.setXmlName(new javax.xml.namespace.QName("", "resources"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://om.open.ac.uk/", "Resource"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
