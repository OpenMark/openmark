
package om.getOucuInfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UserDataStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="UserDataStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Partial"/>
 *     &lt;enumeration value="Full"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "UserDataStatus")
@XmlEnum
public enum UserDataStatus {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Partial")
    PARTIAL("Partial"),
    @XmlEnumValue("Full")
    FULL("Full");
    private final String value;

    UserDataStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UserDataStatus fromValue(String v) {
        for (UserDataStatus c: UserDataStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
