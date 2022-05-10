package edu.mcw.rgd.biocyc;

import org.springframework.jdbc.object.MappingSqlQuery;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BioCycQuery extends MappingSqlQuery {

    public BioCycQuery(DataSource ds, String query) {
        super(ds, query);
    }

    protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        BioCycRecord r = new BioCycRecord();
        r.setGeneRatCycId(rs.getString("GENE_RATCYC_ID"));
        r.setGeneRgdId(rs.getInt("GENE_RGD_ID"));
        if( rs.wasNull() ) {
            r.setGeneRgdId(null);
        }
        r.setGeneNcbiId(rs.getString("GENE_NCBI_ID"));
        r.setGeneRatCycPage(rs.getString("GENE_RATCYC_PAGE"));
        r.setUniProtId(rs.getString("UNIPROT_ID"));
        r.setPathwayRatCycId(rs.getString("PATHWAY_RATCYC_ID"));
        r.setPathwayRatCycName(rs.getString("PATHWAY_RATCYC_NAME"));
        r.setPathwayRatCycPage(rs.getString("PATHWAY_RATCYC_PAGE"));
        return r;
    }
}
