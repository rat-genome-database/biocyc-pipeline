package edu.mcw.rgd.biocyc;

import edu.mcw.rgd.datamodel.SpeciesType;
import edu.mcw.rgd.datamodel.XdbId;
import edu.mcw.rgd.process.FileDownloader;
import edu.mcw.rgd.process.Utils;
import org.apache.commons.collections4.CollectionUtils;
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
    private int bioCycGeneXdbKey;
    private int bioCycPathwayXdbKey;

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

        List<BioCycRecord> incomingRecords = new ArrayList<>();

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

            incomingRecords.add(r);
        }
        in.close();

        log.info("lines read from file: "+incomingRecords.size());

        Collection<BioCycRecord> uniqueIncomingRecords = merge(incomingRecords);
        log.info("unique records after merge: "+uniqueIncomingRecords.size());

        updateBioCycTable(uniqueIncomingRecords);

        generateGeneXdbIds(uniqueIncomingRecords);
        generatePathwayXdbIds(uniqueIncomingRecords);

        log.info("");
        log.info("===    time elapsed: "+ Utils.formatElapsedTime(startTime, System.currentTimeMillis()));
        log.info("");
    }

    void updateBioCycTable(Collection<BioCycRecord> incomingRecords) throws Exception {

        List<BioCycRecord> inRgdRecords = dao.getAllRecords();
        log.info("lines in RGD: "+inRgdRecords.size());

        Collection<BioCycRecord> forInsert = CollectionUtils.subtract(incomingRecords, inRgdRecords);
        Collection<BioCycRecord> forDelete = CollectionUtils.subtract(inRgdRecords, incomingRecords);
        Collection<BioCycRecord> matching = CollectionUtils.intersection(incomingRecords, inRgdRecords);

        if( !forInsert.isEmpty() ) {
            log.info("lines to be inserted: " + forInsert.size());
            for (BioCycRecord r : forInsert) {
                dao.insertRecord(r);
            }
        }

        if( !forDelete.isEmpty() ) {
            log.info("lines to be deleted: " + forDelete.size());
            for (BioCycRecord r : forDelete) {
                dao.deleteRecord(r);
            }
        }

        log.info("lines matching: " + matching.size());
    }

    Collection<BioCycRecord> merge(List<BioCycRecord> incoming) throws Exception {

        // unique record must have unique (gene_id, pathway_id)
        Map<String, BioCycRecord> uniqueMap = new HashMap<>();

        for( BioCycRecord r: incoming ) {
            String key = r.getGeneRatCycId()+"|"+r.getPathwayRatCycId();
            BioCycRecord u = uniqueMap.get(key);
            if( u==null ) {
                uniqueMap.put(key, r);
            } else {
                // merge: gene rgd id
                if( r.getGeneRgdId()!=null ) {
                    if( u.getGeneRgdId()==null ) {
                        u.setGeneRgdId(r.getGeneRgdId());
                        u.setGeneNcbiId(r.getGeneNcbiId());
                        u.setGeneRatCycPage(r.getGeneRatCycPage());
                    } else {
                        int geneRgdId1 = u.getGeneRgdId();
                        int geneRgdId2 = r.getGeneRgdId();
                        if( geneRgdId1!=geneRgdId2 ) {
                            throw new Exception("unexpected");
                        }
                    }
                }

                // merge uniprot ids
                if( r.getUniProtId()!=null ) {
                    if( u.getUniProtId()==null ) {
                        u.setUniProtId(r.getUniProtId());
                    }
                    else if( !u.getUniProtId().contains(r.getUniProtId()) ) {
                        String[] ids = u.getUniProtId().split(",");
                        TreeSet<String> iset = new TreeSet<>(Arrays.asList(ids));
                        iset.add(r.getUniProtId());
                        u.setUniProtId( Utils.concatenate(iset, ","));
                    }
                }
            }
        }

        return uniqueMap.values();
    }

    void generateGeneXdbIds(Collection<BioCycRecord> incoming) throws Exception {

        log.info("genes:");

        List<XdbId> incomigGeneXdbIds = new ArrayList<>();

        incoming.parallelStream().forEach( r -> {

            int geneRgdId = 0;
            if( r.getGeneRgdId()!=null ) {
                geneRgdId = r.getGeneRgdId();
            }
            else {
                geneRgdId = dao.getGeneRgdIdByNcbiId(r.getGeneNcbiId(), SpeciesType.RAT);
            }
            if( geneRgdId==0 ) {
                //log.warn("no matching gene for RGD:"+r.getGeneRgdId()+", NCBI_ID:"+r.getGeneNcbiId());
            } else {

                XdbId xdbId = new XdbId();
                xdbId.setXdbKey(getBioCycGeneXdbKey());
                xdbId.setAccId(r.getGeneRatCycId());
                xdbId.setRgdId(geneRgdId);
                xdbId.setSrcPipeline("BioCyc");
                addToList(xdbId, incomigGeneXdbIds);
            }
        });
        log.info("   xdb ids incoming: "+incomigGeneXdbIds.size());


        List<XdbId> inRgdXdbIds = dao.getGeneBioCycXdbIds(getBioCycGeneXdbKey());

        List<XdbId> xdbIdsForInsert = new ArrayList<>(incomigGeneXdbIds);
        xdbIdsForInsert.removeAll(inRgdXdbIds);

        List<XdbId> xdbIdsForDelete = new ArrayList<>(inRgdXdbIds);
        xdbIdsForDelete.removeAll(incomigGeneXdbIds);

        log.info("   xdb ids for insert: "+xdbIdsForInsert.size());
        log.info("   xdb ids for delete: "+xdbIdsForDelete.size());

        dao.insertXdbIds(xdbIdsForInsert);
        dao.deleteXdbIds(xdbIdsForDelete);
    }

    void generatePathwayXdbIds(Collection<BioCycRecord> incoming) throws Exception {

        log.info("pathways:");

        List<XdbId> incomigPathwayXdbIds = new ArrayList<>();

        incoming.parallelStream().forEach( r -> {

            if( Utils.isStringEmpty(r.getPathwayRatCycId()) ) {
                return;
            }

            int geneRgdId = 0;
            if( r.getGeneRgdId()!=null ) {
                geneRgdId = r.getGeneRgdId();
            }
            else {
                geneRgdId = dao.getGeneRgdIdByNcbiId(r.getGeneNcbiId(), SpeciesType.RAT);
            }
            if( geneRgdId==0 ) {
                //log.warn("no matching gene for RGD:"+r.getGeneRgdId()+", NCBI_ID:"+r.getGeneNcbiId());
            } else {

                XdbId xdbId = new XdbId();
                xdbId.setXdbKey(getBioCycPathwayXdbKey());
                xdbId.setAccId(r.getPathwayRatCycId());
                xdbId.setLinkText(r.getPathwayRatCycId()+" ["+r.getPathwayRatCycName()+"]");
                xdbId.setRgdId(geneRgdId);
                xdbId.setSrcPipeline("BioCyc");
                addToList(xdbId, incomigPathwayXdbIds);
            }
        });
        log.info("   xdb ids incoming: "+incomigPathwayXdbIds.size());


        List<XdbId> inRgdXdbIds = dao.getGeneBioCycXdbIds(getBioCycPathwayXdbKey());

        List<XdbId> xdbIdsForInsert = new ArrayList<>(incomigPathwayXdbIds);
        xdbIdsForInsert.removeAll(inRgdXdbIds);

        List<XdbId> xdbIdsForDelete = new ArrayList<>(inRgdXdbIds);
        xdbIdsForDelete.removeAll(incomigPathwayXdbIds);

        log.info("   xdb ids for insert: "+xdbIdsForInsert.size());
        log.info("   xdb ids for delete: "+xdbIdsForDelete.size());

        dao.insertXdbIds(xdbIdsForInsert);
        dao.deleteXdbIds(xdbIdsForDelete);
    }

    synchronized void addToList(XdbId xdbId, List<XdbId> xdbIds) {
        xdbIds.add(xdbId);
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

    public int getBioCycGeneXdbKey() {
        return bioCycGeneXdbKey;
    }

    public void setBioCycGeneXdbKey(int bioCycGeneXdbKey) {
        this.bioCycGeneXdbKey = bioCycGeneXdbKey;
    }

    public int getBioCycPathwayXdbKey() {
        return bioCycPathwayXdbKey;
    }

    public void setBioCycPathwayXdbKey(int bioCycPathwayXdbKey) {
        this.bioCycPathwayXdbKey = bioCycPathwayXdbKey;
    }
}

