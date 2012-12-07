
package om.getOucuInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OucuInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OucuInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="OucuExists" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Ids" type="{http://tempuri.org//GetOucuInfo}UserIds" minOccurs="0"/>
 *         &lt;element name="DataStatus" type="{http://tempuri.org//GetOucuInfo}UserDataStatus"/>
 *         &lt;element name="UserData" type="{http://tempuri.org//GetOucuInfo}UserData" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OucuInfo", propOrder = {
    "oucuExists",
    "ids",
    "dataStatus",
    "userData"
})
public class OucuInfo {

    @XmlElement(name = "OucuExists")
    protected boolean oucuExists;
    @XmlElement(name = "Ids")
    protected UserIds ids;
    @XmlElement(name = "DataStatus", required = true)
    protected UserDataStatus dataStatus;
    @XmlElement(name = "UserData")
    protected UserData userData;

    /**
     * Gets the value of the oucuExists property.
     * 
     */
    public boolean isOucuExists() {
        return oucuExists;
    }

    /**
     * Sets the value of the oucuExists property.
     * 
     */
    public void setOucuExists(boolean value) {
        this.oucuExists = value;
    }

    /**
     * Gets the value of the ids property.
     * 
     * @return
     *     possible object is
     *     {@link UserIds }
     *     
     */
    public UserIds getIds() {
        return ids;
    }

    /**
     * Sets the value of the ids property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserIds }
     *     
     */
    public void setIds(UserIds value) {
        this.ids = value;
    }

    /**
     * Gets the value of the dataStatus property.
     * 
     * @return
     *     possible object is
     *     {@link UserDataStatus }
     *     
     */
    public UserDataStatus getDataStatus() {
        return dataStatus;
    }

    /**
     * Sets the value of the dataStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserDataStatus }
     *     
     */
    public void setDataStatus(UserDataStatus value) {
        this.dataStatus = value;
    }

    /**
     * Gets the value of the userData property.
     * 
     * @return
     *     possible object is
     *     {@link UserData }
     *     
     */
    public UserData getUserData() {
        return userData;
    }

    /**
     * Sets the value of the userData property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserData }
     *     
     */
    public void setUserData(UserData value) {
        this.userData = value;
    }

}
