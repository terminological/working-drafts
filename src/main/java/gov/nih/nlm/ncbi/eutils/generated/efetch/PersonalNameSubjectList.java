//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.02 at 08:11:15 AM BST 
//


package gov.nih.nlm.ncbi.eutils.generated.efetch;

import java.util.ArrayList;
import java.util.List;
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
    "personalNameSubject"
})
@XmlRootElement(name = "PersonalNameSubjectList")
public class PersonalNameSubjectList {

    @XmlElement(name = "PersonalNameSubject", required = true)
    protected List<PersonalNameSubject> personalNameSubject;

    /**
     * Gets the value of the personalNameSubject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the personalNameSubject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersonalNameSubject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PersonalNameSubject }
     * 
     * 
     */
    public List<PersonalNameSubject> getPersonalNameSubject() {
        if (personalNameSubject == null) {
            personalNameSubject = new ArrayList<PersonalNameSubject>();
        }
        return this.personalNameSubject;
    }

}
