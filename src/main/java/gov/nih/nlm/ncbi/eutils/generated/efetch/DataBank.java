//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.02 at 07:25:44 AM BST 
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
    "dataBankName",
    "accessionNumberList"
})
@XmlRootElement(name = "DataBank")
public class DataBank {

    @XmlElement(name = "DataBankName", required = true)
    protected String dataBankName;
    @XmlElement(name = "AccessionNumberList")
    protected AccessionNumberList accessionNumberList;

    /**
     * Gets the value of the dataBankName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataBankName() {
        return dataBankName;
    }

    /**
     * Sets the value of the dataBankName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataBankName(String value) {
        this.dataBankName = value;
    }

    /**
     * Gets the value of the accessionNumberList property.
     * 
     * @return
     *     possible object is
     *     {@link AccessionNumberList }
     *     
     */
    public AccessionNumberList getAccessionNumberList() {
        return accessionNumberList;
    }

    /**
     * Sets the value of the accessionNumberList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccessionNumberList }
     *     
     */
    public void setAccessionNumberList(AccessionNumberList value) {
        this.accessionNumberList = value;
    }

}
