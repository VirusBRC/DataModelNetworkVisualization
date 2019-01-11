package com.ngc.brc.dao.lookup;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.ProjectionList;

import com.ngc.brc.commons.helperobjects.NameValue;
import com.ngc.brc.commons.utils.enums.OrganismType;
import com.ngc.brc.dao.util.CriteriaAliasFetch;
import com.ngc.brc.dao.util.CriteriaOperators;
import com.ngc.brc.enums.InfluenzaType;
import com.ngc.brc.model.BlastResultsView;
import com.ngc.brc.model.Feature;
import com.ngc.brc.model.GenomicSequence;
import com.ngc.brc.model.IedbAssays;
import com.ngc.brc.model.IedbEpitope;
import com.ngc.brc.model.Protein;
import com.ngc.brc.model.antiviralDrug.DrugBankMaster;
import com.ngc.brc.model.antiviralDrug.DrugBankTarget;
import com.ngc.brc.model.antiviralDrug.DrugMutation;
import com.ngc.brc.model.antiviralDrug.DrugPdbSpeciesMap;
import com.ngc.brc.model.antiviralDrug.DrugRef;
import com.ngc.brc.model.drug.DrugInformation;
import com.ngc.brc.model.featureVariant.SequenceFeatures;
import com.ngc.brc.model.hostFactor.HfExperiment;
import com.ngc.brc.model.nonpersistent.HfCorrEdgeAll;
import com.ngc.brc.model.nonpersistent.HfCorrGeneInfo;
import com.ngc.brc.model.nonpersistent.HfCorrNodeAll;
import com.ngc.brc.model.nonpersistent.Hierarchy;
import com.ngc.brc.model.nonpersistent.HierarchyStrain;
import com.ngc.brc.model.nonpersistent.rowobjects.PDBSPMappingSummary;
import com.ngc.brc.model.orfeome.PlasmidFeature;
import com.ngc.brc.model.orfeome.PlasmidPublication;
import com.ngc.brc.model.orfeome.PlasmidSummary;
import com.ngc.brc.model.orfeome.PlasmidVector;
import com.ngc.brc.model.proteinRecords.PDBRecord;
import com.ngc.brc.model.strain.Strain;
import com.ngc.brc.model.supportedPrograms.SpProject;
import com.ngc.brc.model.virus.ortholog.VbrcAnnotation;
import com.ngc.brc.model.virus.ortholog.VbrcEpiData;
import com.ngc.brc.model.virus.ortholog.VbrcEpiDataView;
import com.ngc.brc.model.virus.ortholog.VbrcKnowledgeBase;
import com.ngc.brc.model.virus.ortholog.VbrcOrthologGroup;
import com.ngc.brc.util.idstrategy.IdStrategy;

public interface LookupDao {

	public List<Object[]> getNetCtlPredictionSummary(Long proteinId);

	public List<Object[]> getSnpSummary(Long geneId);

	public Object getIedbCount(Long id);

	public Object getHostToStrainCount(List ids);

	public List getHostToStrainIds(List ids);

	public Object getHostToSegmentCount(List ids);

	public List getHostToSegmentIds(List ids, List segments);

	public Object getHostToProteinCount(List ids);

	public List getHostToProteinIds(List ids, List proteins);

	public List<Object[]> getAccessionsForProteinId(List<Long> ids);

	public Object countSequenceFeaturesExistForViprDecorator(String decorator);

	public List getDecoratorForProteinIds(List<String> ids);

	public List getDecoratorForGenomicSequenceIds(List<String> ids);

	public List<VbrcAnnotation> getVbrcAnnotationsByProteinNcbiId(String ncbiId, String order);

	public List<Object[]> getGenomicSequenceIdsForProteinIds(List<String> ids);

	public List<Object[]> getProteinIdsForGenomicSequenceIds(List<String> ids);

	public Object getCDSCountForGenomicSequenceIds(List<String> ids);

	public List<Object[]> getGenomicSequenceIdsForNcbiGenomeAccession(List<String> ncbiGenomeAccessions, OrganismType ot);

	public List<Object[]> getGenomicSequenceIdsForFeatureIds(List<String> ids);

	public List<Object[]> getProteinPrimaryKeysForNcbiProteinIds(List<String> ids);

	public Protein getProteinByNcbiProteinId(String ncbiProteinId);

	public Protein getProteinBySwissProtId(String swissProtId);

	public List getByIds(List<String> idStrings, Class clazz, List<String> eagerProperties, IdStrategy idStrategy,
			String projectionPropertyName, CriteriaAliasFetch aliasFamily, String decorator, ProjectionList projections);

	public List<Object[]> getViprStrainsWithCompleteSequences(Date fromCreateDate, Date endCreateDate);

	public List getByIdsInOrder(List<String> ids, Class clazz);

	public Protein getProteinById(Long proteinId);

	public Protein getMatPeptideById(String id);

	public List<VbrcKnowledgeBase> getVbrcKnowledgeBaseByVbrcGeneId(Long vbrcGeneId);

	public Map<String, List<VbrcEpiDataView>> getEpiDataSets();

	public List<VbrcEpiData> getEpiDataSourceType(String srcCvt, String typeCvt);

	public List<Object[]> getEpiDataByCountryYear(String year);

	public List<IedbAssays> getIedbAssay(Long proteinId, Long iedbId);

	public List<IedbEpitope> getIedbSummary(Long proteinId, String sSortBy, String sSortOrder);

	public List<BlastResultsView> getBlastResults(String ncbiProteinId);

	public GenomicSequence getGenomicSequenceById(Long genomicSequenceId);

	public List<String> getAlleleByCriteria(String[] assayCategories, String[] mhcAlleleSrcSpecies, OrganismType ot);

	public List<DrugInformation> getDrugInformationByDrugId(String drugId);

	public Map<OrganismType, List<String>> getLocations();

	public List<String> getContinents(String familyName);

	public List<String> getLocations(OrganismType ot);

	public List findDataByClassColumnName(Class modelClass, String columnName, Object value, boolean filterObsoleteDates,
			boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery);

	public List findDataInByClassColumnName(Class modelClass, String columnName, Collection values, boolean filterObsoleteDates,
			boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery);

	public List findData(Class modelClass, List<String> retrieveColumnNames, List<CriteriaOperators> operators,
			boolean filterObsoleteDates, boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery,
			CriteriaAliasFetch aliasFetchModes);

	public List findDataInForSingleColumn(Class modelClass, String retrieveColumnName, String columnName, Collection values,
			boolean filterObsoleteDates, boolean filterDistinct, String obsoleteDateColName);

	public List findDataForInnerSelect(Class modelClass, String retrieveColumnName, Class innerClass, String innerClassColumnName,
			String innerId, boolean filterObsoleteDates, boolean filterDistinct, String innerColumnCriteria,
			String innerColumnValue);

	public List findGroupCount(Class modelClass, List<String> groupFields, String sqlGroupExpression,
			List<CriteriaOperators> operators, String sqlOrderExpression, CriteriaAliasFetch aliasFetchModes);

	public Long findCount(Class modelClass, String countField, boolean distinct, List<CriteriaOperators> operators,
			CriteriaAliasFetch aliasFetchModes);

	public List<Object[]> getSegmentTypeAndSubTypeForSegmentIdList(List<String> segmentIds);

	public Protein getProteinByLocusTag(String locusTag);

	public Protein findProteinByProteinGi(String proteinGi);

	public Feature getFeatureById(Long featureId);

	public List<String> getNcbiProteinIdsForNcbiAccessions(List<String> ncbiAccessions, String[] proteinOptions,
			String[] proteinNames);

	public List<String> getNcbiAccessionsForNcbiProteinIds(List<String> ncbiProteinIds, String[] segments);

	public Object getCount(Class modelClass, String countField, boolean distinct, String modelField, List inValues);

	public Object getSum(Class modelClass, String sumOfField, String modelField, List<String> inValues);

	public List<Object[]> getJalviewSegmentFieldsBySegmentIdList(List<String> segmentIds);

	public String getCountForMissingAlignSeq(List<String> segmentIds);

	public List<Strain> getStrainByName(String strainName, String familyName);

	public List<GenomicSequence> getSequenceListByStrainName(String strainName, String familyName, String sortBy,
			String sortOrder);

	public List<Hierarchy> getHierarchy();

	public Collection getChildrenHierarchy(String id);

	public Object getHierarchy(String id);

	public HierarchyStrain getHierarchyStrain(String id);

	public int getViprStrainsCount(String searchTerm, String decorator);

	public int getViprSpeciesCount(String searchTerm, String decorator);

	public int getViprSubfamilyCount(String searchTerm, String decorator);

	public int getViprGenusCount(String searchTerm, String decorator);

	public Collection getViprStrains(String searchTerm, String decorator, Long maxRecord);

	public Collection getViprSpecies(String searchTerm, String decorator, Long maxRecord);

	public Collection getViprGenuses(String searchTerm, String decorator, Long maxRecord);

	public Collection getViprSubfamilys(String searchTerm, String decorator, Long maxRecord);

	public List<VbrcOrthologGroup> getViprOrthologGroups(String decorator);

	public VbrcOrthologGroup getViprOrthologGroup(String familyName, Long orthologGroupId);

	/**
	 * Return the PDB and ProteinID pair in the object[] by filtering with the
	 * input sequence Features's reference sequence genome accession and start
	 * and end position
	 * 
	 * @param studyStrainAccession
	 * @param sequenceFetures
	 * @return
	 */
	public List<Object[]> getPDBProteinBySF(String studyStrainAccession, SequenceFeatures sequenceFetures);

	public List<Object[]> getViprSFVTDataByPDB(List proteinIds, String decorator, List<PDBSPMappingSummary> mappingSummary);

	public List<String> getFOMASubTypes(String host);

	public List<String> getFOMASegments(String subType, String host);

	public List<String> getProteinFOMASegments(String subType, String host);

	public List<String> getFOMAHosts();

	public List<String> getFOMAProteinName(String segmentNumber, String subType);

	public Object getSegmentCountForStrainIdList(List<String> strainIds, String[] displayColumns);

	public Object getSegmentCountForStrainNamesList(List<String> strainNames, String[] displayColumns);

	public Map<String, String> getCountryRegionMap();

	public List<Object[]> getViprFeatureVariantTypeCount(Long proteinId, String family);

	public List findPrimerProbesForSequence(Long sequenceId, String segment, String subType, String orgStrainId);

	public List getGenomicSequenceAccessionsForProteinIds(List<String> proteinIds);

	public List<String> getSequenceFeatureProteinSymbols(OrganismType ot);

	public List<NameValue> getSequenceFeatureDengueTypes();

	public Object[] getTextIdxColNameAndTableName(String cateogry);

	public List<Object[]> getViprSFVTByProtein(List<Long> pIds, String proteinName, OrganismType ot);

	public List getFeatureVariantsForSourceStrain(Long seqFeaturesId, String sourceStrainAndId, Long orgId,
			boolean filterObsoleteDates, String decorator, Long sequenceId);

	/**
	 * Host Factor v2.0 method to retrieve the bioset header information from
	 * database in the experiment details page
	 * 
	 * @param resultMatrixId
	 *            - the bioset result matrix id/name
	 * @param biosetIds
	 *            - the database generated unique id for each bioset metadata
	 *            record
	 * @return - a list of header objects for virus, time post infection, viral
	 *         dose and strain/line values
	 */
	public List<Object[]> getSvBiosetListColumnsByMatrixUserDefId(String resultMatrixId, String biosetIds);

	public List<String> findCountriesWithHumanClinicalMetadata(String orgFamily);

	public List<String> findHostSpeciesWithHumanClinicalMetadata(String orgFamily);

	public List<String> findDataSourcesWithHumanClinicalMetadata(String orgFamily);

	public List<String> findCollectorsWithHumanClinicalMetadata(String orgFamily);

	/**
	 * This method is utilized by the Download Runner to clear the Hibernate
	 * StatefulPersistenceContext found the solution
	 * (http://stackoverflow.com/questions
	 * /1071631/trying-to-track-down-a-memory-
	 * leak-garbage-collection-problem-in-java) for the memory leak issue on
	 * performing large download.
	 */
	public void clearCache();

	public List<String> getGenusTypesByDecorator(String decorator);

	public List<Object[]> getSpeciesByGenus(String genus, String decorator);

	public List<Object[]> getPatternProbeCounts(String resultMatrixId, List<Long> biosetInfoIds, String searchKeyword);

	/**
	 * Retrieve a list of probe counts for the given criteria based on the
	 * biosetMetadata
	 * 
	 * @param resultMatrixId
	 * @param biosetInfoIds
	 * @param searchKeyword
	 * @return
	 */
	public List<Object[]> getPatternProbeCountsBm(String resultMatrixId, List<Long> biosetInfoIds, String searchKeyword);

	public List<Object[]> getProteinSuggestions(String orgId, String taxonName, String hcvSubtype, String decorator,
			String strainName, String segment, String term);

	public List<String> getProteinGeneSymbols(String decorator, List<Long> genomicSequenceIds);

	public List<String> getAccessionSuggestions(String orgId, String taxonName, String subType, String decorator,
			String strainName, String segment, String protein, String term);

	public List getOne(Class aClass);

	public List<PDBRecord> findIdenticalAASequencePdbRecords(Long proteinId, Long uniqueAASequenceId, Integer size);

	public List<String> getDengueVirusSubtypesForProteinIds(List<Long> proteinIds);

	public List<String> getHCVVirusSubtypesForProteinIds(List<Long> proteinIds);

	public List<String> getPoxOrthologIdsForProteinIds(List<Long> proteinIds);

	public List<String> getInfluenzaProteinSymbolsForProteinIds(List<Long> proteinIds);

	public List<IedbAssays> getIedbAssaysByEpitopeIds(List<Long> iedbIds);

	public List<Long> getBiosetMetatdataIds(Map userInputs);

	public Map searchPanelExperiments(String criteriaPath, Class modelClass, List<String> groupFields, String sqlGroupExpression,
			Map userInputs, CriteriaAliasFetch aliasFetchModes);

	public Map searchPanelHostFactorCount(String criteriaPath, Class modelClass, List<String> groupFields,
			String sqlGroupExpression, Map userInputs, CriteriaAliasFetch aliasFetchModes);

	public List<Object[]> findExperimentSampleData(Long expId);

	public List<Object[]> getLocationsWithContients(String familyName);

	public List<String> getUSStates(String familyName);

	public List<Object[]> findCountsForIEDB(Set<Long> iedbSet);

	public List<Long> findReferencePositionWithSF(String proteinAccession);

	public List<String> getNcbiAccessionsIds(List<String> ncbiAccessions, String[] proteinOptions, String[] proteinNames,
			String[] segments);

	public List<String> findDrugClass(String orgFamily);

	public List<String> findDrugGroup();

	public List<String> findDrugTarget();

	public List<Object[]> getDrugTaxonomy();

	public PlasmidSummary findPlasmidById(String plasmidId);

	public PlasmidSummary findPlasmidSeq(String plasmidId);

	public List<PlasmidPublication> findPubById(String plasmidId);

	public List<PlasmidFeature> findFeaById(String plasmidId);

	public List<PlasmidFeature> findPlasmidAnnotationById(String plasmidId);

	public PlasmidVector findVectorById(String plasmidvector);

	public DrugBankMaster findAntiviralDrugById(Long drugSeqId);

	public DrugBankMaster findAntiviralDrugByHetId(String hetId);

	public List<String> findDrugPDBIdsByHetId(String hetId);

	public DrugBankTarget findAntiviralDrugTargetById(Long targetSeqId);

	public List<String> findDrugPDBSpecies(String hetId);

	public List<DrugPdbSpeciesMap> findDrugPdbBySpecies(String hetId, String species);

	public DrugPdbSpeciesMap findDrugPdbSpeciesMap(String hetId, String ncbiReference, String pdbId);

	public List<Object[]> findDrugPdbBindingSite(String hetId, String ncbiReference, String pdbId);

	public DrugRef findDrugReference(String ncbiReference);

	public String findDrugBindingPdbFamily(String pdbId);

	public List<String> findDrugMutationSpecies(Long drugSeqId);

	public List<DrugMutation> findDrugMutations(Long drugSeqId, String species);

	public SequenceFeatures getResistanceRiskAnnotationSequenceFeature(Long sfnSeqId);

	public String getResistanceRiskAnnotationVTNumber(Long sfnSeqId, String variance);

	public SequenceFeatures getDrugBindingSiteSequenceFeature(String refAcc, String pdbId);

	public List<Object[]> findExperimentBiosetData(Long expId);

	/**
	 * This method retrieves a list of distinct proteinName and
	 * proteinGeneSymbol from the SequenceFeature table
	 * 
	 * @param virusType
	 *            - the InfluenzaType which the list will be for
	 * @return a list of proteinName and proteinGeneSymbol in an Array
	 */
	public List<Object[]> getInfluenzaSfvtProteinListByVirusType(InfluenzaType virusType);

	public List<String> getVirusTypeByStrainName(Strain virusStrain);

	public List<Long> getExperimentIdsByNames(String[] experiementNames);

	public List<String> convertIds(List<String> ids, List<String> selectedSegments, String convertTo, String family);

	public List<Long> getHpiExperimentIds(List<Long> experiementIds);

	public List<String> getHpiExperimentNames(List<Long> experiementIds);

	public List<Long> getHpiExperimentIdByName(String expName);

	public List<Object[]> getHpiResultByColumnAndIds(String column, List<String> ids);

	public HfExperiment getHfExperimentById(Long id);

	// BRC-11509
	public List<String> getModuleColorsByExperimentId(long experiementId);

	public List<String> getGeneIdsbyModuleId(Long experimentId, String moduleId);

	public List<String> findModuleColors(long experiementId);

	public List<HfCorrGeneInfo> findModuleGenes(long experimentId, String moduleId);

	public List<Object[]> getModuleMetaData(Long expId);

	public List<BigDecimal> getPrimarykeys(long experimentId, String moduleId, List<String> geneId);

	public List<Object[]> getModuleGeneSignificance(long expSeqId, String moduleColor, String metaDataDimension);

	public List<Object[]> findModuleMetaData(Long expId);

	public List<HfCorrGeneInfo> findModuleGeneSignificance(long expSeqId, String moduleColor, String metaDataDimension);

	public List<Object[]> getExperimentGeneListForDownload(Long expId);

	public List<String> findMetadataCategories(long experimentId);

	public boolean isRNASeqExperiment(long expSeqId);

	List<Object[]> findMetaDataByCategory(long expSeqId);

	public List<HfCorrGeneInfo> findGenesData(long expSeqId, String moduleColor, List<String> geneId, String moduleCategory);

	public List<HfCorrGeneInfo> findGenesDatabyExprmtId(long expSeqId);

	public List<SpProject> getSystemBiologyProjectDetails();

	public List<HfExperiment> GetAllHFExperiments();

	public List<HfCorrGeneInfo> findModuleGeneDataByEntrezGeneID(List<String> entrezGeneId, String expId, Map entrezIds);

	List<HfCorrEdgeAll> getEdgedata(String expId, String moduleId);

	Map<String, String> getNodedata(String expId, String moduleId);

	List<HfCorrNodeAll> getNodelist(String expId, String moduleId);

	public int getTotalNode(String expId, String moduleId);

	public int getTotalEdge(String expId, String moduleId);

}
