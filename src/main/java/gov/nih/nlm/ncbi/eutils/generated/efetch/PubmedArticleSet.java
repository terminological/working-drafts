//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.08.02 at 04:44:00 AM BST 
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
    "pubmedArticleOrPubmedBookArticle",
    "deleteCitation"
})
@XmlRootElement(name = "PubmedArticleSet")
public class PubmedArticleSet {

    @XmlElements({
        @XmlElement(name = "PubmedArticle", required = true, type = PubmedArticle.class),
        @XmlElement(name = "PubmedBookArticle", required = true, type = PubmedBookArticle.class)
    })
    protected List<java.lang.Object> pubmedArticleOrPubmedBookArticle;
    @XmlElement(name = "DeleteCitation")
    protected DeleteCitation deleteCitation;

    /**
     * Gets the value of the pubmedArticleOrPubmedBookArticle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pubmedArticleOrPubmedBookArticle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPubmedArticleOrPubmedBookArticle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PubmedArticle }
     * {@link PubmedBookArticle }
     * 
     * 
     */
    public List<java.lang.Object> getPubmedArticleOrPubmedBookArticle() {
        if (pubmedArticleOrPubmedBookArticle == null) {
            pubmedArticleOrPubmedBookArticle = new ArrayList<java.lang.Object>();
        }
        return this.pubmedArticleOrPubmedBookArticle;
    }

    /**
     * Gets the value of the deleteCitation property.
     * 
     * @return
     *     possible object is
     *     {@link DeleteCitation }
     *     
     */
    public DeleteCitation getDeleteCitation() {
        return deleteCitation;
    }

    /**
     * Sets the value of the deleteCitation property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeleteCitation }
     *     
     */
    public void setDeleteCitation(DeleteCitation value) {
        this.deleteCitation = value;
    }

}
