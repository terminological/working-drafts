//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.02 at 05:04:14 AM BST 
//


package gov.nih.nlm.ncbi.eutils.generated.efetch;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "grantID",
    "acronym",
    "agency",
    "country"
})
@XmlRootElement(name = "Grant")
public class Grant {

    @XmlElement(name = "GrantID")
    protected String grantID;
    @XmlElement(name = "Acronym")
    protected String acronym;
    @XmlElement(name = "Agency", required = true)
    protected String agency;
    @XmlElement(name = "Country", required = true)
    protected String country;

    /**
     * Gets the value of the grantID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGrantID() {
        return grantID;
    }

    /**
     * Sets the value of the grantID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGrantID(String value) {
        this.grantID = value;
    }

    /**
     * Gets the value of the acronym property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcronym() {
        return acronym;
    }

    /**
     * Sets the value of the acronym property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcronym(String value) {
        this.acronym = value;
    }

    /**
     * Gets the value of the agency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAgency() {
        return agency;
    }

    /**
     * Sets the value of the agency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAgency(String value) {
        this.agency = value;
    }

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

}
