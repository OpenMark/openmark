
package om.getOucuInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetOucuInfoResult" type="{http://tempuri.org//GetOucuInfo}OucuInfo" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getOucuInfoResult"
})
@XmlRootElement(name = "GetOucuInfoResponse")
public class GetOucuInfoResponse {

    @XmlElement(name = "GetOucuInfoResult")
    protected OucuInfo getOucuInfoResult;

    /**
     * Gets the value of the getOucuInfoResult property.
     * 
     * @return
     *     possible object is
     *     {@link OucuInfo }
     *     
     */
    public OucuInfo getGetOucuInfoResult() {
        return getOucuInfoResult;
    }

    /**
     * Sets the value of the getOucuInfoResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link OucuInfo }
     *     
     */
    public void setGetOucuInfoResult(OucuInfo value) {
        this.getOucuInfoResult = value;
    }

}
