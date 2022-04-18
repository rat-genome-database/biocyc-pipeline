package edu.mcw.rgd.biocyc;

import edu.mcw.rgd.datamodel.Dumpable;
import edu.mcw.rgd.process.Dumper;

public class BioCycRecord implements Dumpable {

    private String geneRatCycId;
    private Integer geneRgdId;
    private String geneNcbiId;
    private String uniProtId;
    private String pathwayRatCycId;
    private String pathwayRatCycName;
    private String pathwayRatCycPage;
    private String geneRatCycPage;

    public String getGeneRatCycId() {
        return geneRatCycId;
    }

    public void setGeneRatCycId(String geneRatCycId) {
        this.geneRatCycId = geneRatCycId;
    }

    public Integer getGeneRgdId() {
        return geneRgdId;
    }

    public void setGeneRgdId(Integer geneRgdId) {
        this.geneRgdId = geneRgdId;
    }

    public String getGeneNcbiId() {
        return geneNcbiId;
    }

    public void setGeneNcbiId(String geneNcbiId) {
        this.geneNcbiId = geneNcbiId;
    }

    public String getUniProtId() {
        return uniProtId;
    }

    public void setUniProtId(String uniProtId) {
        this.uniProtId = uniProtId;
    }

    public String getPathwayRatCycId() {
        return pathwayRatCycId;
    }

    public void setPathwayRatCycId(String pathwayRatCycId) {
        this.pathwayRatCycId = pathwayRatCycId;
    }

    public String getPathwayRatCycName() {
        return pathwayRatCycName;
    }

    public void setPathwayRatCycName(String pathwayRatCycName) {
        this.pathwayRatCycName = pathwayRatCycName;
    }

    public String getPathwayRatCycPage() {
        return pathwayRatCycPage;
    }

    public void setPathwayRatCycPage(String pathwayRatCycPage) {
        this.pathwayRatCycPage = pathwayRatCycPage;
    }

    public String getGeneRatCycPage() {
        return geneRatCycPage;
    }

    public void setGeneRatCycPage(String geneRatCycPage) {
        this.geneRatCycPage = geneRatCycPage;
    }

    public String dump(String delimiter) {
        return (new Dumper(delimiter, true, true))
                .put("GENE_RATCYC_ID", this.geneRatCycId)
                .put("GENE_RGD_ID", this.geneRgdId)
                .put("GENE_NCBI_ID", this.geneNcbiId)
                .put("UNIPROT_ID", this.uniProtId)
                .put("GENE_RATCYC_PAGE", this.geneRatCycPage)
                .put("PATHWAY_RATCYC_ID", this.pathwayRatCycId)
                .put("PATHWAY_RATCYC_NAME", this.pathwayRatCycName)
                .put("PATHWAY_RATCYC_PAGE", this.pathwayRatCycPage)
        .dump();
    }

}
