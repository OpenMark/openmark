
package om.getOucuInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UserIds complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserIds">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Oucu" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="StudentId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="StaffId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TutorId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="VisitorId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SelfRegisteredUserId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CorporateContactId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserIds", propOrder = {
    "oucu",
    "studentId",
    "staffId",
    "tutorId",
    "visitorId",
    "selfRegisteredUserId",
    "corporateContactId"
})
public class UserIds {

    @XmlElement(name = "Oucu")
    protected String oucu;
    @XmlElement(name = "StudentId")
    protected String studentId;
    @XmlElement(name = "StaffId")
    protected String staffId;
    @XmlElement(name = "TutorId")
    protected String tutorId;
    @XmlElement(name = "VisitorId")
    protected String visitorId;
    @XmlElement(name = "SelfRegisteredUserId")
    protected String selfRegisteredUserId;
    @XmlElement(name = "CorporateContactId")
    protected String corporateContactId;

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

    /**
     * Gets the value of the studentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Sets the value of the studentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStudentId(String value) {
        this.studentId = value;
    }

    /**
     * Gets the value of the staffId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStaffId() {
        return staffId;
    }

    /**
     * Sets the value of the staffId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStaffId(String value) {
        this.staffId = value;
    }

    /**
     * Gets the value of the tutorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTutorId() {
        return tutorId;
    }

    /**
     * Sets the value of the tutorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTutorId(String value) {
        this.tutorId = value;
    }

    /**
     * Gets the value of the visitorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVisitorId() {
        return visitorId;
    }

    /**
     * Sets the value of the visitorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVisitorId(String value) {
        this.visitorId = value;
    }

    /**
     * Gets the value of the selfRegisteredUserId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSelfRegisteredUserId() {
        return selfRegisteredUserId;
    }

    /**
     * Sets the value of the selfRegisteredUserId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSelfRegisteredUserId(String value) {
        this.selfRegisteredUserId = value;
    }

    /**
     * Gets the value of the corporateContactId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCorporateContactId() {
        return corporateContactId;
    }

    /**
     * Sets the value of the corporateContactId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCorporateContactId(String value) {
        this.corporateContactId = value;
    }

}
