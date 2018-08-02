//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.02 at 01:41:58 AM BST 
//


package gov.nih.nlm.ncbi.eutils.generated.esummary;

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
    "docSumOrERROR"
})
@XmlRootElement(name = "eSummaryResult")
public class ESummaryResult {

    @XmlElements({
        @XmlElement(name = "DocSum", required = true, type = DocSum.class),
        @XmlElement(name = "ERROR", required = true, type = ERROR.class)
    })
    protected List<Object> docSumOrERROR;

    /**
     * Gets the value of the docSumOrERROR property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the docSumOrERROR property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocSumOrERROR().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DocSum }
     * {@link ERROR }
     * 
     * 
     */
    public List<Object> getDocSumOrERROR() {
        if (docSumOrERROR == null) {
            docSumOrERROR = new ArrayList<Object>();
        }
        return this.docSumOrERROR;
    }

}
