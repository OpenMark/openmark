
package om.getOucuInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetOucuInfoRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetOucuInfoRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Header" type="{http://tempuri.org//GetOucuInfo}RequestHeader" minOccurs="0"/>
 *         &lt;element name="Oucu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetOucuInfoRequest", propOrder = {
    "header",
    "oucu"
})
public class GetOucuInfoRequest {

    @XmlElement(name = "Header")
    protected RequestHeader header;
    @XmlElement(name = "Oucu")
    protected String oucu;

    /**
     * Gets the value of the header property.
     * 
     * @return
     *     possible object is
     *     {@link RequestHeader }
     *     
     */
    public RequestHeader getHeader() {
        return header;
    }

    /**
     * Sets the value of the header property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestHeader }
     *     
     */
    public void setHeader(RequestHeader value) {
        this.header = value;
    }

    /**
     * Gets the value of the oucu property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOucu() {
        return oucu;
    }

    /**
     * Sets the value of the oucu property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOucu(String value) {
        this.oucu = value;
    }

}
