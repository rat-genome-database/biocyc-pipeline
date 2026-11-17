package edu.mcw.rgd.biocyc;

import edu.mcw.rgd.dao.impl.PathwayDAO;
import edu.mcw.rgd.dao.impl.RGDManagementDAO;
import edu.mcw.rgd.dao.impl.XdbIdDAO;
import edu.mcw.rgd.datamodel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Map; // java.util.Map, not edu.mcw.rgd.datamodel.Map

/**
 * @author mtutaj
 * @since 4/18/2022
 * <p>
 * wrapper to handle all DAO code
 */
public class Dao {

    private XdbIdDAO xdao = new XdbIdDAO();
    private PathwayDAO pdao = new PathwayDAO();
    private RGDManagementDAO rdao = new RGDManagementDAO();

    Logger logInserted = LogManager.getLogger("inserted");
    Logger logDeleted = LogManager.getLogger("deleted");

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

    /// rgd ids of all genes (active or not) with the given NCBI gene id
    public List<Integer> getGeneRgdIdsByNcbiId(String accId, int speciesTypeKey) throws Exception {
        List<Integer> rgdIds = new ArrayList<>();
        for( Gene g: xdao.getGenesByXdbId(XdbId.XDB_KEY_NCBI_GENE, accId, speciesTypeKey) ) {
            rgdIds.add(g.getRgdId());
        }
        return rgdIds;
    }

    /// map of rgd id to object status, for all genes of given species
    public Map<Integer, String> getGeneStatuses(int speciesTypeKey) throws Exception {
        Map<Integer, String> statuses = new HashMap<>();
        for( RgdId id: rdao.getRgdIds(RgdId.OBJECT_KEY_GENES, speciesTypeKey) ) {
            statuses.put(id.getRgdId(), id.getObjectStatus());
        }
        return statuses;
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
