package edu.mcw.rgd.biocyc;

import edu.mcw.rgd.dao.impl.PathwayDAO;
import edu.mcw.rgd.dao.impl.XdbIdDAO;
import edu.mcw.rgd.datamodel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author mtutaj
 * @since 4/18/2022
 * <p>
 * wrapper to handle all DAO code
 */
public class Dao {

    private XdbIdDAO xdao = new XdbIdDAO();
    private PathwayDAO pdao = new PathwayDAO();

    Logger logInserted = LogManager.getLogger("inserted");
    Logger logDeleted = LogManager.getLogger("deleted");
    Logger logNoMatch = LogManager.getLogger("nomatch");
    Logger logMultiMatch = LogManager.getLogger("multimatch");

    public String getConnectionInfo() {
        return xdao.getConnectionInfo();
    }

    public void insertRecord(BioCycRecord r) throws Exception {
        pdao.insertBioCycRecord(r,logInserted);
    }

    public void deleteRecord(BioCycRecord r) throws Exception {
        pdao.deleteBioCycRecord(r,logDeleted);
    }

    public List<BioCycRecord> getAllRecords() throws Exception {
        return pdao.getAllBioCycRecords();
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
