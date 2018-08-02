//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.02 at 05:04:14 AM BST 
//


package gov.nih.nlm.ncbi.eutils.generated.efetch;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "startPageOrEndPageOrMedlinePgn"
})
@XmlRootElement(name = "Pagination")
public class Pagination {

    @XmlElements({
        @XmlElement(name = "StartPage", required = true, type = StartPage.class),
        @XmlElement(name = "EndPage", required = true, type = EndPage.class),
        @XmlElement(name = "MedlinePgn", required = true, type = MedlinePgn.class)
    })
    protected List<java.lang.Object> startPageOrEndPageOrMedlinePgn;

    /**
     * Gets the value of the startPageOrEndPageOrMedlinePgn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the startPageOrEndPageOrMedlinePgn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStartPageOrEndPageOrMedlinePgn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StartPage }
     * {@link EndPage }
     * {@link MedlinePgn }
     * 
     * 
     */
    public List<java.lang.Object> getStartPageOrEndPageOrMedlinePgn() {
        if (startPageOrEndPageOrMedlinePgn == null) {
            startPageOrEndPageOrMedlinePgn = new ArrayList<java.lang.Object>();
        }
        return this.startPageOrEndPageOrMedlinePgn;
    }

}
