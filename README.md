# biocyc-pipeline

Downloads RatCyc data and syncs BioCyc gene, pathway, and pathway image cross-reference
IDs to rat genes in RGD.

## Overview

Retrieves the RGD synchronization file from RatCyc, parses gene-pathway mappings, and
populates:
- The `BIO_CYC` table with unique gene/pathway records
- Gene XDB IDs (BioCyc gene accessions)
- Pathway XDB IDs (BioCyc pathway accessions with link text)
- Pathway image XDB IDs (links to pathway diagram images)

## Input file format

Tab-delimited file with 9 columns:
1. Gene's RatCyc ID
2. Gene's RGD ID
3. Gene's NCBI Gene ID
4. Uniprot ID of gene product
5. Pathway RatCyc ID
6. Pathway name
7. URL to RatCyc pathway page
8. URL to RatCyc gene page
9. URL to pathway diagram image

## Logic

1. **Download** — fetches the rgd-synch.txt file from RatCyc
2. **Parse and merge** — merges records by (gene_id, pathway_id), combining
   multiple UniProt IDs per row
3. **Sync BIO_CYC table** — inserts new records, deletes obsolete ones
4. **Sync gene XDB IDs** — matches genes by RGD ID or NCBI Gene ID, then
   inserts/deletes BioCyc gene accessions in XDB_IDS
5. **Sync pathway XDB IDs** — same pattern for pathway accessions
6. **Sync pathway image XDB IDs** — same pattern for pathway diagram images

## Logging

- `status` — pipeline progress and summary counters

## Build and run

Requires Java 17. Built with Gradle:
```
./gradlew clean assembleDist
```