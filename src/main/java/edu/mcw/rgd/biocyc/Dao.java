package edu.mcw.rgd.biocyc;

import edu.mcw.rgd.dao.impl.XdbIdDAO;
import edu.mcw.rgd.datamodel.Gene;
import edu.mcw.rgd.datamodel.RgdId;
import edu.mcw.rgd.datamodel.SpeciesType;
import edu.mcw.rgd.datamodel.XdbId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.SqlParameter;

import java.sql.Types;
import java.util.List;

/**
 * @author mtutaj
 * @since 4/18/2022
 * <p>
 * wrapper to handle all DAO code
 */
public class Dao {

    private XdbIdDAO xdao = new XdbIdDAO();

    Logger logInserted = LogManager.getLogger("inserted");
    Logger logDeleted = LogManager.getLogger("deleted");
    Logger logNoMatch = LogManager.getLogger("nomatch");
    Logger logMultiMatch = LogManager.getLogger("multimatch");

    public String getConnectionInfo() {
        return xdao.getConnectionInfo();
    }

    public void insertRecord(BioCycRecord r) throws Exception {

        logInserted.debug("BIOCYC "+r.dump("|"));

        String sql = "INSERT INTO biocyc (gene_ratcyc_id,gene_rgd_id,gene_ncbi_id,uniprot_id,gene_ratcyc_page,"
                + "pathway_ratcyc_id,pathway_ratcyc_name,pathway_ratcyc_page) VALUES(?,?,?,?,?,?,?,?)";

        xdao.update(sql, r.getGeneRatCycId(), r.getGeneRgdId(), r.getGeneNcbiId(), r.getUniProtId(), r.getGeneRatCycPage(),
                r.getPathwayRatCycId(), r.getPathwayRatCycName(), r.getPathwayRatCycPage());
    }

    public void deleteRecord(BioCycRecord r) throws Exception {

        logDeleted.debug("BIOCYC "+r.dump("|"));

        if( r.getPathwayRatCycId()==null ) {
            String sql = "DELETE FROM biocyc WHERE gene_ratcyc_id=? AND pathway_ratcyc_id IS NULL";
            xdao.update(sql, r.getGeneRatCycId());
        } else {
            String sql = "DELETE FROM biocyc WHERE gene_ratcyc_id=? AND pathway_ratcyc_id=?";
            xdao.update(sql, r.getGeneRatCycId(), r.getPathwayRatCycId());
        }
    }

    public List<BioCycRecord> getAllRecords() throws Exception {

        String sql = "SELECT * FROM biocyc";
        BioCycQuery q = new BioCycQuery(xdao.getDataSource(), sql);
        List<BioCycRecord> results = q.execute();
        return results;
    }

    public BioCycRecord getRecord(String geneRatCycId, String pathwayRatCycId) throws Exception {

        if( pathwayRatCycId==null ) {
            String sql = "SELECT * FROM biocyc WHERE gene_ratcyc_id=? AND pathway_ratcyc_id IS NULL";
            BioCycQuery q = new BioCycQuery(xdao.getDataSource(), sql);
            q.declareParameter(new SqlParameter(Types.VARCHAR));
            List<BioCycRecord> results = q.execute(geneRatCycId);
            if( results.isEmpty() ) {
                return null;
            }
            if( results.size()>1 ) {
                throw new Exception("unexpected: multiple results for "+geneRatCycId);
            }
            return results.get(0);
        }

        String sql = "SELECT * FROM biocyc WHERE gene_ratcyc_id=? AND pathway_ratcyc_id=?";
        BioCycQuery q = new BioCycQuery(xdao.getDataSource(), sql);
        q.declareParameter(new SqlParameter(Types.VARCHAR));
        q.declareParameter(new SqlParameter(Types.VARCHAR));
        List<BioCycRecord> results = q.execute(geneRatCycId, pathwayRatCycId);
        if( results.isEmpty() ) {
            return null;
        }
        if( results.size()>1 ) {
            throw new Exception("unexpected: multiple results for "+geneRatCycId+", "+pathwayRatCycId);
        }
        return results.get(0);
    }

    public int getGeneRgdIdByNcbiId(String accId, int speciesTypeKey) {
        List<Gene> genesInRgd;
        try {
            genesInRgd = xdao.getGenesByXdbId(XdbId.XDB_KEY_NCBI_GENE, accId, speciesTypeKey);
        } catch( Exception e) {
            throw new RuntimeException(e);
        }

        if (genesInRgd.isEmpty()) {
            logNoMatch.debug("no match by NCBI gene id "+accId);
            return 0;
        }
        if (genesInRgd.size() > 1) {
            logMultiMatch.debug("multiple genes for NCBI gene id"+accId);
        }
        return genesInRgd.get(0).getRgdId();
    }

    public List<XdbId> getGeneBioCycXdbIds(int xdbKey) throws Exception {

        XdbId filter = new XdbId();
        filter.setSrcPipeline("BioCyc");
        filter.setXdbKey(xdbKey);
        return xdao.getXdbIds(filter, SpeciesType.RAT, RgdId.OBJECT_KEY_GENES);
    }

    public int deleteXdbIds( List<XdbId> xdbIds ) throws Exception {
        for( XdbId id: xdbIds ) {
            logDeleted.debug("XDB "+id.dump("|"));
        }
        return xdao.deleteXdbIds(xdbIds);
    }

    public int insertXdbIds( List<XdbId> xdbIds ) throws Exception {
        for( XdbId id: xdbIds ) {
            logInserted.debug("XDB "+id.dump("|"));
        }
        return xdao.insertXdbs(xdbIds);
    }
}
