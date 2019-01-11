package com.ngc.brc.services.lookup;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ngc.brc.commons.helperobjects.NameValue;
import com.ngc.brc.commons.utils.enums.OrganismType;
import com.ngc.brc.dao.cachedData.cachedObject.CacheDrugInformation;
import com.ngc.brc.dao.util.CriteriaAliasFetch;
import com.ngc.brc.dao.util.CriteriaOperators;
import com.ngc.brc.enums.BlastProgram;
import com.ngc.brc.enums.InfluenzaType;
import com.ngc.brc.model.BlastDatabase;
import com.ngc.brc.model.BlastResultsView;
import com.ngc.brc.model.DataSummaryHostFactor;
import com.ngc.brc.model.Feature;
import com.ngc.brc.model.GenomicSequence;
import com.ngc.brc.model.HomePageHighlight;
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
import com.ngc.brc.model.nonpersistent.AntiviralDrugTaxonomyData;
import com.ngc.brc.model.nonpersistent.DataSummaryFlu;
import com.ngc.brc.model.nonpersistent.HfCorrEdgeAll;
import com.ngc.brc.model.nonpersistent.HfCorrGeneInfo;
import com.ngc.brc.model.nonpersistent.HfCorrNodeAll;
import com.ngc.brc.model.nonpersistent.Hierarchy;
import com.ngc.brc.model.nonpersistent.HierarchyStrain;
import com.ngc.brc.model.nonpersistent.SurveillanceDataSummaryFlu;
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
import com.sun.jersey.api.client.ClientResponse;

public interface LookupService {

	public List<Object[]> getNetCtlPredictionSummary(Long id);

	public List<Object[]> getSnpSummary(Long id);

	public Object getIedbCount(Long id);

	public Object getHostToStrainCount(List ids);

	public List getHostToStrainIds(List ids);

	public Object getHostToSegmentCount(List ids);

	public List getHostToSegmentIds(List ids, List segments);

	public Object getHostToProteinCount(List ids);

	public List getHostToProteinIds(List ids, List proteins);

	public List<Object[]> getViprStrainsWithCompleteSequences(Date fromCreateDate, Date endCreateDate);

	public List<Object[]> getAccessionsForProteinId(List<Long> ids);

	public List<String> checkSequencesForProteins(List<String> sequenceIds);

	public boolean doSequenceFeaturesExistForViprDecorator(String decorator);

	public List<VbrcAnnotation> getVbrcAnnotationsByProteinNcbiId(String ncbiId, String order);

	public List<BlastResultsView> getBlastResults(String ncbiProteinId);

	public Map<String, String> getDecoratorForProteinIds(List<String> ids);

	public Map<String, String> getDecoratorForGenomicSequenceIds(List<String> ids);

	public Map<Long, Long> findGenomicSequenceIdsForProteinIds(List<String> ids);

	public Set<Long> findUniqueGenomicSequenceIdsForProteinIds(List<String> ids);

	public Map<Long, Long> findProteinIdsForGenomicSequenceIds(List<String> ids);

	public int findProteinCountForGenomicSequenceIds(List<String> ids);

	public Long findCDSCountForGenomicSequenceIds(List<String> ids);

	public Map<String, Long> findGenomicSequenceIdsForNcbiGenomeAccession(List<String> ncbiGenomeAccessions, OrganismType ot);

	public Map<Long, Long> findGenomicSequenceIdsForFeatureIds(List<String> featureIds);

	public List<Object[]> getSegmentTypeAndSubTypeForSegmentIdList(List<String> segmentIds);

	public Map<String, Long> findProteinPrimaryKeysForNcbiProteinIds(List<String> ncbiProteinIds);

	public Protein findProteinByNcbiProteinId(String ncbiProteinId);

	public List findByIds(List<String> idStrings, Class clazz, List<String> eagerProperties, IdStrategy idStrategy,
			String projectionPropertyName, CriteriaAliasFetch aliasFamily, String decorator);

	public List findByIdsInOrder(Class clazz, List<String> ids);

	public GenomicSequence getGenomicSequenceById(Long id);

	public Protein findProteinById(Long proteinId);

	public List<IedbAssays> getIedbAssay(Long proteinId, Long iedbId);

	public List<IedbEpitope> getIedbSummary(Long id, String sSortBy, String sSortOder);

	public List<String> getSegmentIdsForProteinIds(List<String> proteinIds);

	public VbrcKnowledgeBase getVbrcKnowledgeBaseByVbrcGeneId(Long vbrcGeneId);

	public Map<String, List<VbrcEpiDataView>> getEpiDataSets();

	public List<VbrcEpiData> getEpiDataSourceType(String srcCvt, String typeCvt);

	public List<Object[]> getEpiDataByCountryYear(String year);

	public Protein findProteinByProteinGi(String proteinGi);

	public Map<BlastProgram, List<BlastDatabase>> getBlastableDatabasesPerBlastProgram(String sorg, String decoratorOriginal);

	public List<String> getMhcSuperFamilies();

	public List<String> getAssayType(OrganismType ot);

	public List<String> getAlleleClass(OrganismType ot);

	public List<String> getAllele(OrganismType ot);

	public List<String> getAlleleByCriteria(String[] aType, String[] mhcAlleleSrcSpecies, OrganismType ot);

	public List<String> getAlleleSpecies(OrganismType ot);

	public List<DrugInformation> getDrugInformationByDrugId(String drugId);

	public List<String> getFOMASubTypes(String sName);

	public List<String> getFOMASegments(String sName, String sHost);

	public List<String> getProteinFOMASegments(String sName, String sHost);

	public List<String> getFOMAHosts();

	@SuppressWarnings("unchecked")
	public List findDataByClassColumnName(Class modelClass, String columnName, Object value, boolean filterObsoleteDates,
			boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery);

	@SuppressWarnings("unchecked")
	public List findDataInByClassColumnName(Class modelClass, String columnName, Collection values, boolean filterObsoleteDates,
			boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery);

	@SuppressWarnings("unchecked")
	// leave retrieveColumnName as null if you want a list of model objects
	// returned
	public List findData(Class modelClass, List<String> retrieveColumnNames, List<CriteriaOperators> operators,
			boolean filterObsoleteDates, boolean filterDistinct, boolean cacheQuery, CriteriaAliasFetch aliasFetchModes);

	/**
	 * Get a list of data for the given model class, column names, operators and
	 * other given criteria. leave retrieveColumnName as null if you want a list
	 * of model objects returned
	 * 
	 * @param modelClass
	 *            - class name for the data model
	 * @param retrieveColumnNames
	 *            - a list of column names that will be queried against
	 * @param operators
	 *            - a list of criteria operators
	 * @param filterObsoleteDates
	 *            - a flag to determine to add the obsolete date check
	 * @param filterDistinct
	 *            - a flag to determine on adding the distinct
	 * @param sortBy
	 *            - sorting column name
	 * @param sortOrder
	 *            - sorting order
	 * @param cacheQuery
	 *            - add query cache
	 * @param aliasFetchModes
	 *            - a list of alias fectch modes
	 * @return a list of data for the model class with the given criteria
	 */
	public List findData(Class modelClass, List<String> retrieveColumnNames, List<CriteriaOperators> operators,
			boolean filterObsoleteDates, boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery,
			CriteriaAliasFetch aliasFetchModes);

	@SuppressWarnings("unchecked")
	public List findDataForInnerSelect(Class modelClass, String retrieveColumnName, Class innerClass, String innerClassColumnName,
			String innerId, boolean filterObsoleteDates, boolean filterDistinct, String innerColumnCriteria,
			String innerColumnValue);

	@SuppressWarnings("unchecked")
	public List findDataInForSingleColumn(Class modelClass, String retrieveColumnName, String columnName, Collection values,
			boolean filterObsoleteDates, boolean filterDistinct, String obsoleteDateColumnName);

	@SuppressWarnings("unchecked")
	public List findGroupCount(Class modelClass, List<String> groupFields, String sqlGroupExpression,
			List<CriteriaOperators> operators, String sqlOrderExpression, CriteriaAliasFetch aliasFetchModes);

	@SuppressWarnings("unchecked")
	public Long findCount(Class modelClass, String countField, boolean distinct, List<CriteriaOperators> operators,
			CriteriaAliasFetch aliasFetchModes);

	// for Synteny Viewer
	public Protein findProteinByLocusTag(String locusTag);

	public Feature findFeatureById(Long featureId);

	public Object getCount(Class modelClass, String countField, boolean distinct, String modelField, List<String> inValues);

	public Object getSum(Class modelClass, String sumOfField, String modelField, List<String> inValues);

	public List<Object[]> getJalviewSegmentFieldsBySegmentIdList(List<String> segmentIds);

	public String getCountForMissingAlignSeq(List<String> segmentIds);

	public List<Strain> findStrainByName(String strainName, String familyName);

	public List<GenomicSequence> findSequenceListByStrainName(String strainName, String familyName, String sortBy,
			String sortOrder);

	public List<String> findLocations(OrganismType ot);

	public List<Object[]> findInfluenzaLocations();

	public List<Object[]> findViprLocations(OrganismType ot);

	public List<String> findContinents(String familyName);

	public List<String> findInfluenzaUSStates();

	public List<String> findInfluenzaContinents();

	public List<Object[]> findInfluenzaCladeLocations(String cladeType);

	public List<String> findInfluenzaCladeContinents(String cladeType);

	public List<String> findStates();

	public List<String> findSegments(String familyName);

	public List<Hierarchy> getHierarchy();

	public Collection getChildrenHierarchy(String id);

	public Object getHierarchy(String id);

	public HierarchyStrain getHierarchyStrain(String id);

	public int getViprStrainsCount(String searchTerm, String decorator);

	public Collection getViprStrains(String searchTerm, String decorator, Long maxRecord);

	public int getViprSpeciesCount(String searchTerm, String decorator);

	public Collection getViprSpecies(String searchTerm, String decorator, Long maxRecord);

	public int getViprSubfamilyCount(String searchTerm, String decorator);

	public Collection getViprSubfamilys(String searchTerm, String decorator, Long maxRecord);

	public int getViprGenusCount(String searchTerm, String decorator);

	public Collection getViprGenuses(String searchTerm, String decorator, Long maxRecord);

	public List<Hierarchy> getHierarchy(OrganismType orgType);

	public Map<String, Object> getInfluenzaDataSummary();

	public List<DataSummaryFlu> getInfluenzaDetailDataSummary();

	public List<SurveillanceDataSummaryFlu> getInfluenzaSurveillanceDataSummary();

	public List<NameValue> getViprAggregateDataSummary();

	public List<NameValue> getViprDerivedDataSummary();

	public List<NameValue> getViprDataSummary(OrganismType ot);

	public String getExampleText(String exampleKey);

	public String getViprQuickSearchExampleText(OrganismType ot);

	public List<String> findCuratedHosts(OrganismType ot);

	public List<String> findMotifTypes(String familyName);

	public List<VbrcOrthologGroup> getViprOrthologGroups(String decorator);

	public VbrcOrthologGroup getViprOrthologGroup(String familyName, Long orthologGroupId);

	public Protein findMatPeptideById(String id);

	public Date getReleaseDate();

	public List<Object[]> getPDBProteinBySF(String studyStrainAccession, SequenceFeatures sequenceFeatures);

	public List<Object[]> getViprSFVTDataByPDB(List proteinIds, String decorator, List<PDBSPMappingSummary> mappingSummary);

	public List<String> getOrderedHcvSubtypes();

	public List<String> getOrderedWestNileSubtypes();

	public Date getLastSequenceAdded(OrganismType orgType);

	public Date getLastViprSequenceAdded();

	public Map<String, List<HomePageHighlight>> getHighlights();

	public List<String> findFOMAProteinName(String sSegment, String subType);

	public Object getSegmentCountForStrainNameList(List<String> strainNames, String[] displayColumns);

	public Map<String, String> getCountryRegionMap();

	public CacheDrugInformation getCacheDrugInformationPDBData();

	public List<Object[]> getViprFeatureVariantTypeCount(Long proteinId, String family);

	public List findPrimerProbesForSequence(Long sequenceId, String segment, String subType, String orgStrainId);

	public List<String> findGenomicSequenceAccessionsForProteinIds(List<String> ids);

	public Map<String, List<String>> getSequenceFeatureCategories();

	public List<String> getSequenceFeatureProteinSymbols(OrganismType ot);

	public List<NameValue> getSequenceFeatureDengueTypes();

	public List<Object[]> getViprSFVTByProtein(List<Long> pIds, String proteinName, OrganismType ot);

	public List getFeatureVariantsForSourceStrain(Long seqFeaturesId, String sourceStrainAndId, Long orgId,
			boolean filterObsoleteDates, String decorator, Long sequenceId);

	public List<Object[]> reterieveVTData(Long id, String organismFamily, String orgStrainId, boolean excludeSequence,
			boolean onlyVT1);

	public List reterieveGenomeSequencesByIds(List<Long> idList, String organismFamily);

	public List<Object[]> reterieveSequencesByIds(List<Long> idList, String organismFamily);

	public List<Object[]> getSvBiosetListColumnsByMatrixUserDefId(String resultMatrixId, String biosetIds);

	public List<String> findCountriesWithHumanClinicalMetadata(String orgFamily);

	public List<String> findHostSpeciesWithHumanClinicalMetadata(String orgFamily);

	public List<String> findDataSourcesWithHumanClinicalMetadata(String orgFamily);

	public List<String> findCollectorsWithHumanClinicalMetadata(String orgFamily);

	public Map<String, List<NameValue>> getCodeConversionLookup();

	public List<NameValue> getCodeConversionLookup(String type);

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
	 * Retrieve the Pattern Probe count for a biosetMetadata record
	 * 
	 * @param resultMatrixId
	 *            - the result matrix ID
	 * @param bmSeqIds
	 *            - the biosetMetadata unique ID
	 * @param searchKeyword
	 *            - the search keywords from the user
	 * @return a list of Pattern probe counts
	 */
	public List<Object[]> getPatternProbeCountsBm(String resultMatrixId, List<Long> bmSeqIds, String searchKeyword);

	public List<Object[]> getProteinSuggestions(String orgId, String taxonName, String hcvSubtype, String decorator,
			String strainName, String segment, String term);

	public List<String> getProteinGeneSymbols(String decorator, List<Long> genomicSequenceIds);

	public List<String> getAccessionSuggestions(String orgId, String taxonName, String subType, String decorator,
			String strainName, String segment, String protein, String term);

	public List<String> getSubtypesByProteinSymbol(String vProteinSymbol);

	public List getOne(Class aClass);

	public List getDengueVirusSubtypes();

	public List getHCVVirusSubtypes();

	public List getWestNileVirusSubtypes();

	public List<PDBRecord> findIdenticalAASequencePdbRecords(Long proteinId, Long uniqueAASequenceId, Integer size);

	public List<String> getDengueVirusSubtypesForProteinIds(List<Long> proteinIds);

	public List<String> getHCVVirusSubtypesForProteinIds(List<Long> proteinIds);

	public List<String> getPoxOrthologIdsForProteinIds(List<Long> proteinIds);

	public List<String> getInfluenzaProteinNamesForProteinIds(List<Long> proteinIds);

	public List<Long> getBiosetMetadataIdsByKeywords(Map userInputs);

	public Map searchPanelExperiments(String criteriaPath, Class modelClass, List<String> groupFields, String sqlGroupExpression,
			Map userInputs, CriteriaAliasFetch aliasFetchModes);

	public Map searchPanelHostFactorCount(String criteriaPath, Class modelClass, List<String> groupFields,
			String sqlGroupExpression, Map userInputs, CriteriaAliasFetch aliasFetchModes);

	public List getAssayHosts(OrganismType ot);

	public List<Object[]> findExperimentSampleData(Long expId);

	public List findViprUSStates(String familyName);

	public List<Long> findReferencePositionWithSF(String proteinAccession);

	public List<String> findHCVSubtypeList();

	public List<String> findHCVIsolationTissue();

	public List<String> findHCVInfectionType();

	public List<String> findWestNileSubtypeList();

	public List<String> findBovineSubtypeList();

	public List<String> findZikaSubtypeList();

	public List<String> findMurraySubtypeList();

	public List<String> findJapEncephSubtypeList();

	public List<String> findTickBourneSubtypeList();

	public List<String> findStLouisSubtypeList();

	public List<String> findYellowFeverSubtypeList();

	public List<String> findGenotypeList(OrganismType orgType);

	public List<String> findPicornaSampleSourceList();

	public List<String> findPicornaInstitutionList();

	public List<String> findPicornaClinicalDiagnosis();

	public List<String> findPicornaAsthmaSymptonScores();

	public List<String> findPicornaColdSymptonScores();

	public List<String> findDengueImmuneStatusList();

	public List<String> findDengueSampleSourceList();

	public List<String> findHealthStatusList();

	public List<String> findTestTypeList();

	public List<String> findPassageHistoryList(OrganismType orgType);

	public List<String> findSerotypeList(OrganismType orgType);

	public List<String> findDengueDiseaseCourseList();

	public List<String> findDengueSampleCohortList();

	public List<String> findDengueWhoSeverityList();

	public List<String> findGeneSymbolByOrganismType(OrganismType ot);

	public List<DataSummaryHostFactor> getDataSummaryHostFactor();

	public List<String> findViralTestResultList(OrganismType orgType);

	public List<String> findVaccineTypeList(OrganismType orgType);

	public List<Object> findOnsetHoursList(OrganismType orgType);

	public List<String> findTravelCountryList(OrganismType orgType);

	public List<String> findSpecimenVoucherList(OrganismType orgType);

	public List<String> findSerovarList(OrganismType orgType);

	public List<String> findPathotypeList(OrganismType orgType);

	public List<String> findSubgroupList(OrganismType orgType);

	public List<String> findSubtypeList(OrganismType orgType);

	public List<Object[]> getExpCountsByType();

	public List<String> findCountryDistrictList(OrganismType orgType);

	public List<String> findDiseaseOutcomeList(OrganismType orgType);

	public List<String> findDrugClass(String orgFamily);

	public List<String> findDrugGroup();

	public List<String> findDrugTarget();

	public AntiviralDrugTaxonomyData getDrugTaxonomyData();

	public DrugBankMaster findAntiviralDrugById(Long drugSeqId);

	public PlasmidSummary findPlasmidById(String plasmidId);

	public List<PlasmidPublication> findPubById(String plasmidId);

	public List<PlasmidFeature> findFeaById(String plasmidId);

	public List<PlasmidFeature> findPlasmidAnnotationById(String plasmidId);

	public PlasmidVector findVectorById(String plasmidvector);

	public PlasmidSummary findPlasmidSeq(String plasmidId);

	public DrugBankMaster findAntiviralDrugByHetId(String hetId);

	public List<String> findDrugPDBIdsByHetId(String hetId);

	public DrugBankTarget findAntiviralDrugTargetById(Long targetSeqId);

	public List<String> findDrugPDBSpecies(String hetId);

	public Map getDrugReferenceToPDBMap(String hetId, String species);

	public DrugPdbSpeciesMap findDrugPdbSpeciesMap(String hetId, String ncbiReference, String pdbId);

	public List<Object[]> findDrugPdbBindingSite(String hetId, String ncbiReference, String pdbId);

	public DrugRef findDrugReference(String ncbiReference);

	public String findDrugBindingPdbFamily(String pdbId);

	public List<String> findDrugMutationSpecies(Long drugSeqId);

	public List<DrugMutation> findDrugMutations(Long drugSeqId, String species);

	public SequenceFeatures getResistanceRiskAnnotationSequenceFeature(Long sfnSeqId);

	public String getResistanceRiskAnnotationVTNumber(Long sfnSeqId, String variance);

	public SequenceFeatures getDrugBindingSiteSequenceFeature(String refAcc, String pdbId);

	public List<Object[]> findExperimentBiosetData(Long expSeqId);

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

	public List<Long> getHpiExperimentIds(List<Long> experiementIds);

	public List<String> getHpiExperimentNames(List<Long> experiementIds);

	public List<Long> getHpiExperimentIdByName(String expName);

	public List<Object[]> getHpiResultByColumnAndIds(String column, List<String> ids);

	public HfExperiment getHfExperimentById(Long id);

	public List<String> convertIds(List<String> ids, List<String> selectedSegments, String convertTo, String family);

	public List<String> getHostAgeList(String familyName);

	public List<String> getHostGenderList(String familyName);

	public List<String> getOrganismDetectionMethodList(String familyName);

	public List<String> getSpecimentTypeList(String familyName);

	public List<String> getSourceHealthStatusList(String familyName);

	public ClientResponse startSearchJerseyRest(String ticketNumber, String parameters, String decorator);

	public ClientResponse startSearchJerseyRestOtherBrc(String brcName, String ticketNumber, String parameters, String decorator);

	// BRC-11509

	public List<String> getModuleColorsByExperimentId(long experiementId);

	public List<String> getGeneIdsbyModuleId(Long experimentId, String moduleId);

	public List<String> getFindModuleColors(long experiementId);

	public List<com.ngc.brc.model.nonpersistent.HfCorrGeneInfo> getFindModuleGenes(long experiementId, String moduleId);

	public List<Object[]> getModuleMetaData(Long expId);

	public Map<String, String> getModuleGeneSignificance(long expSeqId, String metaDataDimension, String moduleColor);

	public List<HfCorrGeneInfo> findModuleGeneSignificance(long expSeqId, String metaDataDimension, String moduleColor);

	public List<Object[]> getExperimentGeneListForDownload(Long expId);

	public List<String> findMetadataCategories(Long long1);

	public boolean isRNASeqExperiment(long expSeqId);

	List<Object[]> findMetaDataByCategory(long expSeqId);

	// public List<HfCorrGeneInfo> findModuleGeneSignificance(long expSeqId,
	// String moduleColor, List<String> geneId);

	public List<HfCorrGeneInfo> findGenesData(long expSeqId, String moduleColor, List<String> geneIds, String modCategory);

	public List<HfCorrGeneInfo> findGenesDatabyExprmtId(long longValue);

	public List<SpProject> getSystemBiologyProjectDetails();

	public List<HfExperiment> GetAllHFExperiments();

	List<HfCorrGeneInfo> findModuleGeneDataByEntrezGeneID(List<String> entrezGeneId, String expId, Map object);

	List<HfCorrEdgeAll> getEdgedata(String expId, String moduleId);

	Map<String, String> getNodedata(String expId, String moduleId);

	List<HfCorrNodeAll> getNodelist(String expId, String moduleId);

	public int getTotalNode(String expId, String moduleId);

	public int getTotalEdge(String expId, String moduleId);

}

