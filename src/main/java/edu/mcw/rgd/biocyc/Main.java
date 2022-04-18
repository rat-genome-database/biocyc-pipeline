package edu.mcw.rgd.biocyc;

import edu.mcw.rgd.process.FileDownloader;
import edu.mcw.rgd.process.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author mtutaj
 * @since 3/01/19
 */
public class Main {

    private Dao dao = new Dao();
    private String version;
    private String rgdSynchFile;

    Logger log = LogManager.getLogger("status");

    public static void main(String[] args) throws Exception {

        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        Main manager = (Main) (bf.getBean("main"));

        try {
            manager.run();
        }catch (Exception e) {
            Utils.printStackTrace(e, manager.log);
            throw e;
        }
    }

    public void run() throws Exception {

        long startTime = System.currentTimeMillis();

        String msg = getVersion();
        log.info(msg);

        msg = dao.getConnectionInfo();
        log.info("   "+msg);

        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("   started at "+sdt.format(new Date(startTime)));

        // download RefSeq file
        FileDownloader fd = new FileDownloader();
        fd.setExternalFile(getRgdSynchFile());
        fd.setLocalFile("data/rgd-synch.txt");
        fd.setUseCompression(true);
        fd.setPrependDateStamp(true);
        String localFile = fd.downloadNew();

        log.info("downloaded file "+localFile);

        int deleted = dao.deleteAllRows();
        log.info("deleted rows "+deleted);

        int linesRead = 0;

        BufferedReader in = Utils.openReader(localFile);
        String line;
        while( (line=in.readLine())!=null ) {
            String[] cols = line.split("[\\t]", -1);

            // 1. The gene's RatCyc ID
            // 2. The gene's RGD ID (number, no prefix)
            // 3. The gene's NCBI ID (number, no prefix)
            // 4. The Uniprot ID of a product of the gene
            // 5. The RatCyc ID of a pathway associated with the gene
            // 6. Name of pathway associated with the gene
            // 7. A URL that points to the RatCyc pathway Page for the pathway in column 5
            // 8. A URL that points to the RatCyc gene page for the gene in column 1

            BioCycRecord r = new BioCycRecord();
            r.setGeneRatCycId(cols[0]);
            r.setGeneNcbiId(cols[2]);
            r.setUniProtId(cols[3]);
            r.setPathwayRatCycId(cols[4]);
            r.setPathwayRatCycName(cols[5]);
            r.setPathwayRatCycPage(cols[6]);
            r.setGeneRatCycPage(cols[7]);

            if( !Utils.isStringEmpty(cols[1]) ) {
                r.setGeneRgdId(Integer.parseInt(cols[1]));
            }

            dao.insertRecord(r);

            linesRead++;
        }
        in.close();

        log.info("lines read from file: "+linesRead);

        log.info("");
        log.info("===    time elapsed: "+ Utils.formatElapsedTime(startTime, System.currentTimeMillis()));
        log.info("");
    }


    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getRgdSynchFile() {
        return rgdSynchFile;
    }

    public void setRgdSynchFile(String rgdSynchFile) {
        this.rgdSynchFile = rgdSynchFile;
    }
}

