package edu.mcw.rgd.biocyc;

import edu.mcw.rgd.dao.AbstractDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author mtutaj
 * @since 4/18/2022
 * <p>
 * wrapper to handle all DAO code
 */
public class Dao {

    private AbstractDAO dao = new AbstractDAO();

    Logger logInserted = LogManager.getLogger("inserted");

    public String getConnectionInfo() {
        return dao.getConnectionInfo();
    }

    public int deleteAllRows() throws Exception {
        String sql = "DELETE FROM BIOCYC";
        return dao.update(sql);
    }

    public void insertRecord(BioCycRecord r) throws Exception {

        logInserted.debug(r.dump("|"));

        String sql = "INSERT INTO biocyc (gene_ratcyc_id,gene_rgd_id,gene_ncbi_id,uniprot_id,gene_ratcyc_page,"
                +"pathway_ratcyc_id,pathway_ratcyc_name,pathway_ratcyc_page) VALUES(?,?,?,?,?,?,?,?)";

        dao.update(sql, r.getGeneRatCycId(), r.getGeneRgdId(), r.getGeneNcbiId(), r.getUniProtId(), r.getGeneRatCycPage(),
                r.getPathwayRatCycId(), r.getPathwayRatCycName(), r.getGeneRatCycPage());
    }
}
