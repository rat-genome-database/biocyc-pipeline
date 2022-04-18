package edu.mcw.rgd.biocyc;

import edu.mcw.rgd.dao.AbstractDAO;

/**
 * @author mtutaj
 * @since 4/18/2022
 * <p>
 * wrapper to handle all DAO code
 */
public class Dao {

    private AbstractDAO dao = new AbstractDAO();

    public String getConnectionInfo() {
        return dao.getConnectionInfo();
    }

}
