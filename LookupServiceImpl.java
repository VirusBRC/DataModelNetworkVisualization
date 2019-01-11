package com.ngc.brc.services.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.criterion.CriteriaSpecification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ngc.brc.commons.constants.Constants;
import com.ngc.brc.commons.helperobjects.NameValue;
import com.ngc.brc.commons.utils.CastHelper;
import com.ngc.brc.commons.utils.Utils;
import com.ngc.brc.commons.utils.enums.ApplicationType;
import com.ngc.brc.commons.utils.enums.HierarchyType;
import com.ngc.brc.commons.utils.enums.OrganismType;
import com.ngc.brc.commons.web.util.config.EnvironmentConfig;
import com.ngc.brc.dao.cachedData.CachedDataWrapper;
import com.ngc.brc.dao.cachedData.cachedObject.CacheDrugInformation;
import com.ngc.brc.dao.lookup.LookupDao;
import com.ngc.brc.dao.util.CriteriaAlias;
import com.ngc.brc.dao.util.CriteriaAliasFetch;
import com.ngc.brc.dao.util.CriteriaOperators;
import com.ngc.brc.enums.BlastProgram;
import com.ngc.brc.enums.InfluenzaType;
import com.ngc.brc.enums.TicketType;
import com.ngc.brc.model.BlastDatabase;
import com.ngc.brc.model.BlastResultsView;
import com.ngc.brc.model.DataSummaryHostFactor;
import com.ngc.brc.model.Feature;
import com.ngc.brc.model.GenomicSequence;
import com.ngc.brc.model.HomePageHighlight;
import com.ngc.brc.model.IedbAssays;
import com.ngc.brc.model.IedbEpitope;
import com.ngc.brc.model.NucleotideSequence;
import com.ngc.brc.model.Protein;
import com.ngc.brc.model.antiviralDrug.DrugBankMaster;
import com.ngc.brc.model.antiviralDrug.DrugBankTarget;
import com.ngc.brc.model.antiviralDrug.DrugMutation;
import com.ngc.brc.model.antiviralDrug.DrugPdbSpeciesMap;
import com.ngc.brc.model.antiviralDrug.DrugRef;
import com.ngc.brc.model.drug.DrugInformation;
import com.ngc.brc.model.featureVariant.SequenceFeatures;
import com.ngc.brc.model.featureVariant.SequenceVariants;
import com.ngc.brc.model.hostFactor.HfExperiment;
import com.ngc.brc.model.nonpersistent.AntiviralDrugTaxonomyData;
import com.ngc.brc.model.nonpersistent.DataSummaryFlu;
import com.ngc.brc.model.nonpersistent.HfCorrEdgeAll;
import com.ngc.brc.model.nonpersistent.HfCorrGeneInfo;
import com.ngc.brc.model.nonpersistent.HfCorrNodeAll;
import com.ngc.brc.model.nonpersistent.Hierarchy;
import com.ngc.brc.model.nonpersistent.HierarchyStrain;
import com.ngc.brc.model.nonpersistent.ProteinSubtypes;
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
import com.ngc.brc.util.BrcConstants;
import com.ngc.brc.util.common.SFVTUtils;
import com.ngc.brc.util.idstrategy.IdStrategy;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

@Service("lookupServiceTarget")
public class LookupServiceImpl implements LookupService {

	@Resource
	private LookupDao lookupDao;
	@Resource
	private CachedDataWrapper cachedData;

	public List<Object[]> getNetCtlPredictionSummary(Long id) {
		return lookupDao.getNetCtlPredictionSummary(id);
	}

	public List<Object[]> getSnpSummary(Long id) {
		return lookupDao.getSnpSummary(id);
	}

	public Object getIedbCount(Long id) {
		return lookupDao.getIedbCount(id);
	}

	public Object getHostToStrainCount(List ids) {
		return lookupDao.getHostToStrainCount(ids);
	}

	public List getHostToStrainIds(List ids) {
		return lookupDao.getHostToStrainIds(ids);
	}

	public Object getHostToSegmentCount(List ids) {
		return lookupDao.getHostToSegmentCount(ids);
	}

	public List getHostToSegmentIds(List ids, List segments) {
		return lookupDao.getHostToSegmentIds(ids, segments);
	}

	public Object getHostToProteinCount(List ids) {
		return lookupDao.getHostToProteinCount(ids);
	}

	public List getHostToProteinIds(List ids, List proteins) {
		return lookupDao.getHostToProteinIds(ids, proteins);
	}

	public VbrcKnowledgeBase getVbrcKnowledgeBaseByVbrcGeneId(Long vbrcGeneId) {
		List<VbrcKnowledgeBase> vbrcKnowledgeBaseList = lookupDao.getVbrcKnowledgeBaseByVbrcGeneId(vbrcGeneId);
		if (vbrcKnowledgeBaseList == null || vbrcKnowledgeBaseList.size() == 0) {
			return null;
		}
		return (VbrcKnowledgeBase) vbrcKnowledgeBaseList.get(0);
	}

	public List<Object[]> getAccessionsForProteinId(List<Long> ids) {
		return lookupDao.getAccessionsForProteinId(ids);
	}

	public Map<String, List<VbrcEpiDataView>> getEpiDataSets() {
		return lookupDao.getEpiDataSets();
	}

	public List<Object[]> getViprStrainsWithCompleteSequences(Date fromCreateDate, Date endCreateDate) {
		return lookupDao.getViprStrainsWithCompleteSequences(fromCreateDate, endCreateDate);
	}

	public List<VbrcEpiData> getEpiDataSourceType(String srcCvt, String typeCvt) {
		return lookupDao.getEpiDataSourceType(srcCvt, typeCvt);
	}

	public List<Object[]> getEpiDataByCountryYear(String year) {
		return lookupDao.getEpiDataByCountryYear(year);
	}

	public List<VbrcAnnotation> getVbrcAnnotationsByProteinNcbiId(String ncbiId, String order) {
		return lookupDao.getVbrcAnnotationsByProteinNcbiId(ncbiId, order);
	}

	public Protein findProteinByNcbiProteinId(String ncbiProteinId) {
		return lookupDao.getProteinByNcbiProteinId(ncbiProteinId);
	}

	public Protein findProteinBySwissProtId(String swissProtId) {
		return lookupDao.getProteinBySwissProtId(swissProtId);
	}

	public List<BlastResultsView> getBlastResults(String ncbiProteinId) {
		return lookupDao.getBlastResults(ncbiProteinId);
	}

	public boolean doSequenceFeaturesExistForViprDecorator(String decorator) {

		Long count = (Long) lookupDao.countSequenceFeaturesExistForViprDecorator(decorator);
		if (count != null && count.intValue() > 0)
			return true;
		else
			return false;
	}

	public Map<Long, Long> findGenomicSequenceIdsForProteinIds(List<String> ids) {
		Map<Long, Long> map = new HashMap<Long, Long>();
		List<Object[]> proteinIdGeneId = lookupDao.getGenomicSequenceIdsForProteinIds(ids);

		for (Object[] data : proteinIdGeneId) {
			map.put((Long) data[0], (Long) data[1]);
		}

		return map;
	}

	// BRC-10757
	public Set<Long> findUniqueGenomicSequenceIdsForProteinIds(List<String> ids) {
		Set<Long> set = new HashSet<Long>();
		List<Object[]> proteinIdGeneId = lookupDao.getGenomicSequenceIdsForProteinIds(ids);

		for (Object[] data : proteinIdGeneId) {
			set.add((Long) data[1]);
		}

		return set;
	}

	/**
	 * Given a list of sequence IDs, return those sequence IDs that have a
	 * protein associated with them. Used to screen out sequences without
	 * proteins.
	 */
	public List<String> checkSequencesForProteins(List<String> sequenceIds) {

		List<String> returnList = new ArrayList<String>();
		for (Object[] result : lookupDao.getProteinIdsForGenomicSequenceIds(sequenceIds)) {
			returnList.add(result[0].toString());
		}

		return CastHelper.removeDuplicates(returnList);
	}

	/**
	 * Convert protein IDs to segment IDs
	 * 
	 * @param proteinIds
	 * @return
	 */
	public List<String> getSegmentIdsForProteinIds(List<String> proteinIds) {
		// A list of protein IDs is passed in, but we need to return a list
		// of gene IDs
		List<String> segmentIds = new ArrayList<String>(proteinIds.size());

		for (List<String> partialSelectedIds : Utils.separateIntoChunks(proteinIds)) {
			// Get a gene ID for each protein
			Map<Long, Long> proteinSegmentMap = findGenomicSequenceIdsForProteinIds(partialSelectedIds);

			// To preserve the original order of IDs, look up the segment ID
			// for each protein ID passed in.
			for (String id : partialSelectedIds) {
				String nextSegmentId = String.valueOf(proteinSegmentMap.get(Long.parseLong(id)));

				// Only add the segment id to the list if it is not in
				// the list already.
				if (!segmentIds.contains(nextSegmentId)) {
					segmentIds.add(nextSegmentId);
				}
			}
		}
		return segmentIds;
	}

	public Map<Long, Long> findProteinIdsForGenomicSequenceIds(List<String> ids) {
		Map<Long, Long> map = new HashMap<Long, Long>();
		List<Object[]> sequenceIdProteinId = lookupDao.getProteinIdsForGenomicSequenceIds(ids);

		for (Object[] data : sequenceIdProteinId) {
			map.put((Long) data[0], (Long) data[1]);
		}

		return map;
	}

	public int findProteinCountForGenomicSequenceIds(List<String> ids) {
		List<Object[]> sequenceIdProteinId = lookupDao.getProteinIdsForGenomicSequenceIds(ids);
		return sequenceIdProteinId.size();
	}

	public Long findCDSCountForGenomicSequenceIds(List<String> ids) {
		Long count = (Long) lookupDao.getCDSCountForGenomicSequenceIds(ids);
		return count;
	}

	public Map<String, Long> findGenomicSequenceIdsForNcbiGenomeAccession(List<String> ncbiGenomeAccessions, OrganismType ot) {
		Map<String, Long> map = new HashMap<String, Long>();
		List<Object[]> results = lookupDao.getGenomicSequenceIdsForNcbiGenomeAccession(ncbiGenomeAccessions, ot);

		for (Object[] data : results) {
			map.put((String) data[0], (Long) data[1]);
		}

		return map;
	}

	public Map<Long, Long> findGenomicSequenceIdsForFeatureIds(List<String> featureIds) {
		Map<Long, Long> map = new HashMap<Long, Long>();
		List<Object[]> results = lookupDao.getGenomicSequenceIdsForFeatureIds(featureIds);

		for (Object[] data : results) {
			map.put((Long) data[0], (Long) data[1]);
		}

		return map;
	}

	public Map<String, Long> findProteinPrimaryKeysForNcbiProteinIds(List<String> ncbiProteinIds) {
		Map<String, Long> map = new HashMap<String, Long>();
		List<Object[]> results = lookupDao.getProteinPrimaryKeysForNcbiProteinIds(ncbiProteinIds);

		for (Object[] data : results) {
			map.put((String) data[0], (Long) data[1]);
		}

		return map;
	}

	public List findByIds(List<String> idStrings, Class clazz, List<String> eagerProperties, IdStrategy idStrategy,
			String projectionPropertyName, CriteriaAliasFetch aliasFamily, String decorator) {
		return lookupDao.getByIds(idStrings, clazz, eagerProperties, idStrategy, projectionPropertyName, aliasFamily, decorator,
				null);
	}

	public List findByIdsInOrder(Class clazz, List<String> ids) {
		return lookupDao.getByIdsInOrder(ids, clazz);
	}

	public Protein findProteinById(Long proteinId) {
		return lookupDao.getProteinById(proteinId);
	}

	public List<IedbAssays> getIedbAssay(Long proteinId, Long iedbId) {
		return lookupDao.getIedbAssay(proteinId, iedbId);
	}

	public List<Object[]> getSegmentTypeAndSubTypeForSegmentIdList(List<String> segmentIds) {
		return lookupDao.getSegmentTypeAndSubTypeForSegmentIdList(segmentIds);
	}

	public List<IedbEpitope> getIedbSummary(Long id, String sSortBy, String sSortOder) {
		return lookupDao.getIedbSummary(id, sSortBy, sSortOder);
	}

	/**
	 * Returns a map of program name to the list of blastable databases that can
	 * be used with that program for the organism passed in
	 */
	public Map<BlastProgram, List<BlastDatabase>> getBlastableDatabasesPerBlastProgram(String decorator,
			String decoratorOriginal) {

		// organism column of blast database is capitalized
		decorator = StringUtils.capitalize(decorator);

		// Get full list of blast DBs
		List<BlastDatabase> allBlastDbs = null;
		if (!decorator.equalsIgnoreCase("influenza")) {
			List<String> decorators = new ArrayList();
			decorators.add(decorator);
			decorators.add("Vipr");
			allBlastDbs = lookupDao.findDataInByClassColumnName(BlastDatabase.class, "organism", decorators, false, false,
					"databaseName", null, false);
		} else {
			allBlastDbs = lookupDao.findDataByClassColumnName(BlastDatabase.class, "organism", decorator, false, false,
					"databaseName", null, false);
		}
		List<BlastDatabase> refinedBlastDbs = new ArrayList<BlastDatabase>();
		for (BlastDatabase bdb : allBlastDbs) {
			if (decoratorOriginal.equalsIgnoreCase("flavi_dengue")) {
				if (bdb.getDatabaseName().startsWith("Flavi") || bdb.getDatabaseName().startsWith("Dengue"))
					refinedBlastDbs.add(bdb);
			} else if (decoratorOriginal.equalsIgnoreCase("flavi_hcv")) {
				if (bdb.getDatabaseName().startsWith("Flavi") || bdb.getDatabaseName().startsWith("Hepac"))
					refinedBlastDbs.add(bdb);
			} else if (decoratorOriginal.equalsIgnoreCase("flavi_zika")) {
				if (bdb.getDatabaseName().startsWith("Flavi") || bdb.getDatabaseName().startsWith("Zika"))
					refinedBlastDbs.add(bdb);
			} else if (decoratorOriginal.equalsIgnoreCase("filo_ebola")) {
				if (bdb.getDatabaseName().startsWith("Filo") || bdb.getDatabaseName().startsWith("Ebola"))
					refinedBlastDbs.add(bdb);
			} else if (decoratorOriginal.equalsIgnoreCase("picorna_entero")) {
				if (bdb.getDatabaseName().startsWith("Picorna") || bdb.getDatabaseName().startsWith("Entero"))
					refinedBlastDbs.add(bdb);
			} else {
				refinedBlastDbs.add(bdb);
			}

		}
		// Build into map with program name as key
		Map<BlastProgram, List<BlastDatabase>> returnMap = new HashMap<BlastProgram, List<BlastDatabase>>();
		if (refinedBlastDbs != null) {
			for (BlastDatabase bp : refinedBlastDbs) {
				List<BlastDatabase> listForProgram = returnMap.get(BlastProgram.getByName(bp.getProgram()));
				if (listForProgram == null) {
					listForProgram = new ArrayList<BlastDatabase>();
					returnMap.put(BlastProgram.getByName(bp.getProgram()), listForProgram);
				}
				listForProgram.add(bp);
			}
		}
		return returnMap;
	}

	public List<String> getMhcSuperFamilies() {
		return cachedData.getMhcSuperFamilies();
	}

	public List<String> getAssayType(OrganismType ot) {
		return cachedData.getAssayType(ot);
	}

	public List<String> getAlleleClass(OrganismType ot) {
		return cachedData.getAlleleClass(ot);
	}

	public List<String> getAllele(OrganismType ot) {
		return cachedData.getAllele(ot);
	}

	public List<String> getAlleleSpecies(OrganismType ot) {
		return cachedData.getAlleleSpecies(ot);
	}

	public List<String> getAlleleByCriteria(String[] aType, String[] mhcAlleleSrcSpecies, OrganismType ot) {
		return lookupDao.getAlleleByCriteria(aType, mhcAlleleSrcSpecies, ot);
	}

	public Protein findProteinByLocusTag(String locusTag) {
		return lookupDao.getProteinByLocusTag(locusTag);
	}

	// ========================================================================
	public GenomicSequence getGenomicSequenceById(Long id) {
		return lookupDao.getGenomicSequenceById(id);
	}

	public LookupDao getLookupDao() {
		return lookupDao;
	}

	public void setLookupDao(LookupDao lookupDao) {
		this.lookupDao = lookupDao;
	}

	// get drug information by drug id
	public List<DrugInformation> getDrugInformationByDrugId(String drugId) {
		return lookupDao.getDrugInformationByDrugId(drugId);
	}

	// Given a list of IDs, return a map of ID to its decorator
	public Map<String, String> getDecoratorForProteinIds(List<String> ids) {
		List<Object[]> list = lookupDao.getDecoratorForProteinIds(ids);
		// Convert to map and to use actual decorator rather than family name
		Map<String, String> map = new HashMap<String, String>();
		for (Object[] o : list) {
			String id = o[0].toString();
			OrganismType ot = OrganismType.getByFamilyName((String) o[1]);
			if (ot != null) {
				map.put(id, ot.getDecoratorName());
			}
		}
		return map;
	}

	// Given a list of IDs, return a map of ID to its decorator
	public Map<String, String> getDecoratorForGenomicSequenceIds(List<String> ids) {
		List<Object[]> list = lookupDao.getDecoratorForGenomicSequenceIds(ids);
		// Convert to map and to use actual decorator rather than family name
		Map<String, String> map = new HashMap<String, String>();
		for (Object[] o : list) {
			String id = o[0].toString();
			OrganismType ot = OrganismType.getByFamilyName((String) o[1]);
			if (ot != null) {
				map.put(id, ot.getDecoratorName());
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public List findDataByClassColumnName(Class modelClass, String columnName, Object value, boolean filterObsoleteDates,
			boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery) {
		return lookupDao.findDataByClassColumnName(modelClass, columnName, value, filterObsoleteDates, filterDistinct, sortBy,
				sortOrder, cacheQuery);
	}

	@SuppressWarnings("unchecked")
	public List findData(Class modelClass, List<String> retrieveColumnNames, List<CriteriaOperators> operators,
			boolean filterObsoleteDates, boolean filterDistinct, boolean cacheQuery, CriteriaAliasFetch aliasFetchModes) {
		return lookupDao.findData(modelClass, retrieveColumnNames, operators, filterObsoleteDates, filterDistinct, null, null,
				cacheQuery, aliasFetchModes);
	}

	@SuppressWarnings("unchecked")
	public List findData(Class modelClass, List<String> retrieveColumnNames, List<CriteriaOperators> operators,
			boolean filterObsoleteDates, boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery,
			CriteriaAliasFetch aliasFetchModes) {
		return lookupDao.findData(modelClass, retrieveColumnNames, operators, filterObsoleteDates, filterDistinct, sortBy,
				sortOrder, cacheQuery, aliasFetchModes);
	}

	@SuppressWarnings("unchecked")
	public List findDataInByClassColumnName(Class modelClass, String columnName, Collection values, boolean filterObsoleteDates,
			boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery) {

		return lookupDao.findDataInByClassColumnName(modelClass, columnName, values, filterObsoleteDates, filterDistinct, sortBy,
				sortOrder, cacheQuery);
	}

	@SuppressWarnings("unchecked")
	public List findDataInForSingleColumn(Class modelClass, String retrieveColumnName, String columnName, Collection values,
			boolean filterObsoleteDates, boolean filterDistinct, String obsoleteDateColName) {
		return lookupDao.findDataInForSingleColumn(modelClass, retrieveColumnName, columnName, values, filterObsoleteDates,
				filterDistinct, obsoleteDateColName);
	}

	@SuppressWarnings("unchecked")
	public List findDataForInnerSelect(Class modelClass, String retrieveColumnName, Class innerClass, String innerClassColumnName,
			String innerId, boolean filterObsoleteDates, boolean filterDistinct, String innerColumnCriteria,
			String innerColumnValue) {
		return lookupDao.findDataForInnerSelect(modelClass, retrieveColumnName, innerClass, innerClassColumnName, innerId,
				filterObsoleteDates, filterDistinct, innerColumnCriteria, innerColumnValue);
	}

	@SuppressWarnings("unchecked")
	public List findGroupCount(Class modelClass, List<String> groupFields, String sqlGroupExpression,
			List<CriteriaOperators> operators, String sqlOrderExpression, CriteriaAliasFetch aliasFetchModes) {
		return lookupDao.findGroupCount(modelClass, groupFields, sqlGroupExpression, operators, sqlOrderExpression,
				aliasFetchModes);
	}

	@SuppressWarnings("unchecked")
	public Long findCount(Class modelClass, String countField, boolean distinct, List<CriteriaOperators> operators,
			CriteriaAliasFetch aliasFetchModes) {
		return lookupDao.findCount(modelClass, countField, distinct, operators, aliasFetchModes);
	}

	public Protein findProteinByProteinGi(String proteinGi) {
		return lookupDao.findProteinByProteinGi(proteinGi);
	}

	public Object getCount(Class modelClass, String countField, boolean distinct, String modelField, List<String> inValues) {
		return lookupDao.getCount(modelClass, countField, distinct, modelField, inValues);
	}

	public Object getSum(Class modelClass, String sumOfField, String modelField, List<String> inValues) {
		return lookupDao.getSum(modelClass, sumOfField, modelField, inValues);
	}

	public Feature findFeatureById(Long featureId) {
		return lookupDao.getFeatureById(featureId);
	}

	public List<Object[]> getJalviewSegmentFieldsBySegmentIdList(List<String> segmentIds) {
		return lookupDao.getJalviewSegmentFieldsBySegmentIdList(segmentIds);
	}

	public String getCountForMissingAlignSeq(List<String> segmentIds) {
		return lookupDao.getCountForMissingAlignSeq(segmentIds);
	}

	public List<Strain> findStrainByName(String strainName, String familyName) {
		return lookupDao.getStrainByName(strainName, familyName);
	}

	public List<GenomicSequence> findSequenceListByStrainName(String strainName, String familyName, String sortBy,
			String sortOrder) {
		return lookupDao.getSequenceListByStrainName(strainName, familyName, sortBy, sortOrder);
	}

	public List<String> findLocations(OrganismType ot) {
		return cachedData.getLocationsByOrganismType().get(ot);
	}

	public List<Object[]> findInfluenzaLocations() {
		return cachedData.getInfluenzaLocations();
	}

	public List<Object[]> findViprLocations(OrganismType ot) {
		return cachedData.getLocationsWithContientsByOrganismType().get(ot);

	}

	public List<String> findContinents(String familyName) {
		// return lookupDao.getContinents(familyName);
		OrganismType ot = OrganismType.getByFamilyName(familyName);
		return cachedData.getContinentsByOrganismType().get(ot);

	}

	public List<String> findInfluenzaUSStates() {
		return cachedData.getInfluenzaUSStates();
	}

	public List<String> findInfluenzaContinents() {
		return cachedData.getInfluenzaContinents();
	}

	// Clade Locations
	public List<Object[]> findInfluenzaCladeLocations(String cladeType) {
		return cachedData.getInfluenzaCladeLocations().get(cladeType);
	}

	// Clade Continents
	public List<String> findInfluenzaCladeContinents(String cladeType) {
		return cachedData.getInfluenzaCladeContinents().get(cladeType);
	}

	public List<String> findSegments(String familyName) {
		return cachedData.getSegmentsByFamilyName().get(familyName);
	}

	public List<String> findStates() {
		return cachedData.getStates();
	}

	public List<Hierarchy> getHierarchy() {
		return lookupDao.getHierarchy();
	}

	public Collection getChildrenHierarchy(String id) {
		return lookupDao.getChildrenHierarchy(id);
	}

	public Object getHierarchy(String id) {
		return lookupDao.getHierarchy(id);
	}

	public HierarchyStrain getHierarchyStrain(String id) {
		return lookupDao.getHierarchyStrain(id);
	}

	public int getViprStrainsCount(String searchTerm, String decorator) {
		return lookupDao.getViprStrainsCount(searchTerm, decorator);
	}

	public int getViprSpeciesCount(String searchTerm, String decorator) {
		return lookupDao.getViprSpeciesCount(searchTerm, decorator);
	}

	public int getViprSubfamilyCount(String searchTerm, String decorator) {
		return lookupDao.getViprSubfamilyCount(searchTerm, decorator);
	}

	public int getViprGenusCount(String searchTerm, String decorator) {
		return lookupDao.getViprGenusCount(searchTerm, decorator);
	}

	public Collection getViprStrains(String searchTerm, String decorator, Long maxRecord) {
		Collection strains = lookupDao.getViprStrains(searchTerm, decorator, maxRecord);
		Collection result = new ArrayList<Hierarchy>();
		for (Object o : strains) {
			Long sid = (Long) ((Object[]) o)[0];
			result.add(lookupDao.getHierarchyStrain(String.valueOf(sid)));
		}
		return result;
	}

	public Collection getViprSpecies(String searchTerm, String decorator, Long maxRecord) {
		return lookupDao.getViprSpecies(searchTerm, decorator, maxRecord);
	}

	public Collection getViprGenuses(String searchTerm, String decorator, Long maxRecord) {
		return lookupDao.getViprGenuses(searchTerm, decorator, maxRecord);
	}

	public Collection getViprSubfamilys(String searchTerm, String decorator, Long maxRecord) {
		return lookupDao.getViprSubfamilys(searchTerm, decorator, maxRecord);
	}

	public List<Hierarchy> getHierarchy(OrganismType orgType) {
		List<Hierarchy> result = new ArrayList<Hierarchy>();
		Hierarchy hierarchy = null;
		List<Hierarchy> hList = lookupDao.getHierarchy();
		for (Hierarchy h : hList) {
			if (h.getName().equalsIgnoreCase(orgType.getFamilyName())) {
				hierarchy = h.clone();
				if (orgType.getSubFilterType() != null) {
					if (HierarchyType.SPECIES.equals(orgType.getSubFilterType().getHierarchyType())) {
						result.addAll(hierarchy.getDescendants(orgType.getSubFilterType().getHierarchyName(),
								orgType.getSubFilterType().getHierarchyType(), false));
					} else if (HierarchyType.GENUS.equals(orgType.getSubFilterType().getHierarchyType())) {
						result.addAll(hierarchy.getDescendants(orgType.getSubFilterType().getHierarchyName(),
								orgType.getSubFilterType().getHierarchyType(), true));
					}
				} else {
					result.add(hierarchy);
				}
			}
		}
		return result;
	}

	public Date getLastSequenceAdded(OrganismType orgType) {
		Map<OrganismType, Date> map = cachedData.getLastSequenceAddedDateMap();
		return map.get(orgType);
	}

	public Date getLastViprSequenceAdded() {
		Map<OrganismType, Date> map = cachedData.getLastSequenceAddedDateMap();
		Date lastUpdatedDate = null;
		// Find the latest updated date of all Vipr organisms
		for (OrganismType o : OrganismType.values()) {
			if (o.getApplicationType().equals(ApplicationType.VIPR) && o.getSubFilterType() == null) {
				if (map.get(o) != null) {
					if (lastUpdatedDate == null || lastUpdatedDate.before(map.get(o))) {
						lastUpdatedDate = map.get(o);
					}
				}
			}
		}
		if (lastUpdatedDate == null)
			lastUpdatedDate = new Date(0);
		return lastUpdatedDate;
	}

	public Map<String, Object> getInfluenzaDataSummary() {
		return cachedData.getInfluenzaDataSummary();
	}

	public List<DataSummaryFlu> getInfluenzaDetailDataSummary() {
		return cachedData.getInfluenzaDetailDataSummary();
	}

	public List<SurveillanceDataSummaryFlu> getInfluenzaSurveillanceDataSummary() {
		return cachedData.getInfluenzaSurveillanceDataSummary();
	}

	public List<NameValue> getViprAggregateDataSummary() {
		return cachedData.getViprAggregateDataSummary();
	}

	public List<NameValue> getViprDerivedDataSummary() {
		return cachedData.getViprDerivedDataSummary();
	}

	public List<NameValue> getViprDataSummary(OrganismType ot) {
		return cachedData.getViprOrganismTypeDataSummary().get(ot);
	}

	public String getExampleText(String exampleKey) {
		return cachedData.getExampleText(exampleKey);
	}

	/**
	 * Builds example text for ViPR Quick Search
	 */
	public String getViprQuickSearchExampleText(OrganismType ot) {
		// Get protein ncbi Ids (accession.version)
		String proteinNcbiIds = cachedData.getExampleText(ot.getDecoratorName() + "_protein_ncbi_ID");
		List<String> ncbiIdList = new ArrayList<String>();
		if (StringUtils.hasLength(proteinNcbiIds)) {
			ncbiIdList = CastHelper.stringToList(proteinNcbiIds);
		}
		// Get Strain names
		String strainNames = cachedData.getExampleText(ot.getDecoratorName() + "_genome_name");
		List<String> names = new ArrayList<String>();
		if (StringUtils.hasLength(strainNames)) {
			names = CastHelper.stringToList(strainNames);
		}

		// We only want one of each
		String returnString = "";
		if (names.size() > 0) {
			returnString += names.get(0);
		}
		if (ncbiIdList.size() > 0) {
			if (returnString.length() > 0) {
				returnString += ", ";
			}
			returnString += ncbiIdList.get(0);
		}

		if (returnString.length() > 0) {
			returnString += "...";
		}

		return returnString;
	}

	public List<String> findCuratedHosts(OrganismType ot) {
		return cachedData.getCuratedHostsByOrganismType().get(ot);
	}

	public List<String> findMotifTypes(String familyName) {
		return cachedData.getMotifTypesByFamilyName().get(familyName);
	}

	@Override
	public List<VbrcOrthologGroup> getViprOrthologGroups(String decorator) {
		return lookupDao.getViprOrthologGroups(decorator);
	}

	@Override
	public VbrcOrthologGroup getViprOrthologGroup(String familyName, Long orthologGroupId) {
		return lookupDao.getViprOrthologGroup(familyName, orthologGroupId);
	}

	public Protein findMatPeptideById(String id) {
		return lookupDao.getMatPeptideById(id);
	}

	public Date getReleaseDate() {
		return cachedData.getReleaseDate();
	}

	public List<Object[]> getPDBProteinBySF(String studyStrainAccession, SequenceFeatures sequenceFeatures) {
		return lookupDao.getPDBProteinBySF(studyStrainAccession, sequenceFeatures);
	}

	public List<Object[]> getViprSFVTDataByPDB(List proteinIds, String decorator, List<PDBSPMappingSummary> mappingSummary) {
		return lookupDao.getViprSFVTDataByPDB(proteinIds, decorator, mappingSummary);
	}

	public List<String> getOrderedHcvSubtypes() {
		return cachedData.getOrderedHcvSubtypes();
	}

	public List<String> getOrderedWestNileSubtypes() {
		return cachedData.getOrderedWestNileSubtypes();
	}

	public Map<String, List<HomePageHighlight>> getHighlights() {
		return cachedData.getHighlights();
	}

	public CachedDataWrapper getCachedData() {
		return cachedData;
	}

	public void setCachedData(CachedDataWrapper cachedData) {
		this.cachedData = cachedData;
	}

	public List<String> getFOMASubTypes(String sName) {
		return lookupDao.getFOMASubTypes(sName);
	}

	public List<String> getFOMASegments(String sName, String sHost) {
		return lookupDao.getFOMASegments(sName, sHost);
	}

	public List<String> getProteinFOMASegments(String sName, String sHost) {
		return lookupDao.getProteinFOMASegments(sName, sHost);
	}

	public List<String> getFOMAHosts() {
		return lookupDao.getFOMAHosts();
	}

	public List<String> findFOMAProteinName(String sSegment, String subType) {
		return lookupDao.getFOMAProteinName(sSegment, subType);
	}

	public Object getSegmentCountForStrainNameList(List<String> strainNames, String[] displayColumns) {
		return lookupDao.getSegmentCountForStrainNamesList(strainNames, displayColumns);
	}

	public Map<String, String> getCountryRegionMap() {
		return lookupDao.getCountryRegionMap();
	}

	public CacheDrugInformation getCacheDrugInformationPDBData() {
		return cachedData.getCacheDrugInformationPDBData();
	}

	public List<Object[]> getViprFeatureVariantTypeCount(Long proteinId, String family) {
		return lookupDao.getViprFeatureVariantTypeCount(proteinId, family);
	}

	public List findPrimerProbesForSequence(Long sequenceId, String segment, String subType, String orgStrainId) {
		return lookupDao.findPrimerProbesForSequence(sequenceId, segment, subType, orgStrainId);
	}

	public List<String> findGenomicSequenceAccessionsForProteinIds(List<String> ids) {
		return lookupDao.getGenomicSequenceAccessionsForProteinIds(ids);
	}

	public Map<String, List<String>> getSequenceFeatureCategories() {
		return cachedData.getSequenceFeatureCategories();
	}

	public List<String> getSequenceFeatureProteinSymbols(OrganismType ot) {
		return lookupDao.getSequenceFeatureProteinSymbols(ot);
	}

	public List<NameValue> getSequenceFeatureDengueTypes() {
		return lookupDao.getSequenceFeatureDengueTypes();
	}

	public List<Object[]> getViprSFVTByProtein(List<Long> pIds, String proteinName, OrganismType ot) {
		return lookupDao.getViprSFVTByProtein(pIds, proteinName, ot);
	}

	public List getFeatureVariantsForSourceStrain(Long seqFeaturesId, String sourceStrainAndId, Long orgId,
			boolean filterObsoleteDates, String decorator, Long sequenceId) {
		return lookupDao.getFeatureVariantsForSourceStrain(seqFeaturesId, sourceStrainAndId, orgId, filterObsoleteDates,
				decorator, sequenceId);
	}

	// shared by SFVT Details and MSAction
	// this is sorted by Strain Count - reason is that for large number of
	// variant types, we only take a limited set of the data because of timeouts
	public List<Object[]> reterieveVTData(Long id, String organismFamily, String orgStrainId, boolean excludeSequence,
			boolean onlyVT1) {

		// full VT name with count
		List<String> retrieveColumnNames = new ArrayList<String>();
		retrieveColumnNames.add("id");
		// value like: Influenza A_NP_nucleoprotein-chain_1(498)_VT-2
		retrieveColumnNames.add("nonFluSFVTId");

		if (excludeSequence == false)
			retrieveColumnNames.add("variantSequence");

		retrieveColumnNames.add("sequenceFeatureStrainProteinCounts.count");
		retrieveColumnNames.add("variantSequenceLength");
		retrieveColumnNames.add("pvtRules.sfnSeqId");

		List<CriteriaOperators> operators = new ArrayList<CriteriaOperators>();
		operators.add(new CriteriaOperators("sequenceFeatures.id", id, CriteriaOperators.EQUAL));
		operators.add(new CriteriaOperators(BrcConstants.FAMILY_PARTITION, organismFamily, CriteriaOperators.EQUAL));
		String field = "sequenceFeatureStrainProteinCounts." + BrcConstants.FAMILY_PARTITION;
		operators.add(new CriteriaOperators(field, organismFamily, CriteriaOperators.EQUAL));
		operators.add(new CriteriaOperators("obsoleteDate", null, CriteriaOperators.IS_NULL));

		if (onlyVT1 == true)
			operators.add(new CriteriaOperators("nonFluSFVTId", SFVTUtils.VT_1, CriteriaOperators.ENDS_WITH));

		if (StringUtils.hasLength(orgStrainId))
			operators.add(new CriteriaOperators("strain.id", new Long(orgStrainId), CriteriaOperators.EQUAL));

		CriteriaAliasFetch aliasFetchModes = new CriteriaAliasFetch();

		List<CriteriaAlias> aliases = new ArrayList<CriteriaAlias>();
		aliases.add(new CriteriaAlias("sequenceFeatureStrainProteinCounts", "sequenceFeatureStrainProteinCounts"));
		aliases.add(new CriteriaAlias("sfvtPvtMaps", "sfvtPvtMaps", CriteriaSpecification.LEFT_JOIN));
		aliases.add(new CriteriaAlias("sfvtPvtMaps.pvtRules", "pvtRules", CriteriaSpecification.LEFT_JOIN));
		if (StringUtils.hasLength(orgStrainId)) {
			aliases.add(new CriteriaAlias("strainSequenceFeatureVariants", "strainSequenceFeatureVariants"));
			aliases.add(new CriteriaAlias("strainSequenceFeatureVariants.strain", "strain"));
		}

		aliasFetchModes.setAliases(aliases);

		// rule is to subList the list with the count of strains being the
		// item of importance
		String sortByCount = "sequenceFeatureStrainProteinCounts.count";

		List<Object[]> vtList = findData(SequenceVariants.class, retrieveColumnNames, operators, false, excludeSequence,
				sortByCount, Constants.SORT_DECENDING, false, aliasFetchModes);

		return vtList;
	}

	// get the sequences (CLOB Field) for the ids
	public List<Object[]> reterieveGenomeSequencesByIds(List<Long> idList, String organismFamily) {

		// full VT name with count
		// List<String> retrieveColumnNames = new ArrayList<String>();
		// retrieveColumnNames.add("id");
		// retrieveColumnNames.add("sequence");

		List<CriteriaOperators> operators = new ArrayList<CriteriaOperators>();
		operators.add(new CriteriaOperators("id", idList, CriteriaOperators.IN));
		// operators.add(new CriteriaOperators(BrcConstants.FAMILY_PARTITION,
		// organismFamily, CriteriaOperators.EQUAL));

		List<Object[]> idAndSequenceList = findData(NucleotideSequence.class, null, operators, false, false, null, null, false,
				null);

		return idAndSequenceList;
	}

	// get the sequences (CLOB Field) for the Sequence Variant ids
	public List<Object[]> reterieveSequencesByIds(List<Long> idList, String organismFamily) {

		// full VT name with count
		List<String> retrieveColumnNames = new ArrayList<String>();
		retrieveColumnNames.add("id");
		retrieveColumnNames.add("variantSequence");

		List<CriteriaOperators> operators = new ArrayList<CriteriaOperators>();
		operators.add(new CriteriaOperators("id", idList, CriteriaOperators.IN));
		operators.add(new CriteriaOperators(BrcConstants.FAMILY_PARTITION, organismFamily, CriteriaOperators.EQUAL));

		List<Object[]> idAndSequenceList = findData(SequenceVariants.class, retrieveColumnNames, operators, false, false, null,
				null, false, null);

		return idAndSequenceList;
	}

	// get Bioset information by resultMatrixId
	public List<Object[]> getSvBiosetListColumnsByMatrixUserDefId(String resultMatrixId, String biosetIds) {
		return lookupDao.getSvBiosetListColumnsByMatrixUserDefId(resultMatrixId, biosetIds);
	}

	public List<String> findCountriesWithHumanClinicalMetadata(String orgFamily) {
		return lookupDao.findCountriesWithHumanClinicalMetadata(orgFamily);
	}

	public List<String> findHostSpeciesWithHumanClinicalMetadata(String orgFamily) {
		return lookupDao.findHostSpeciesWithHumanClinicalMetadata(orgFamily);
	}

	public List<String> findDataSourcesWithHumanClinicalMetadata(String orgFamily) {
		return lookupDao.findDataSourcesWithHumanClinicalMetadata(orgFamily);
	}

	public List<String> findCollectorsWithHumanClinicalMetadata(String orgFamily) {
		return lookupDao.findCollectorsWithHumanClinicalMetadata(orgFamily);
	}

	public Map<String, List<NameValue>> getCodeConversionLookup() {
		return cachedData.getCodeConversionLookups();
	}

	public List<NameValue> getCodeConversionLookup(String type) {
		return cachedData.getCodeConversionLookups().get(type);
	}

	public List<String> getGenusTypesByDecorator(String decorator) {
		return lookupDao.getGenusTypesByDecorator(decorator);
	}

	public List<Object[]> getSpeciesByGenus(String genus, String decorator) {
		return lookupDao.getSpeciesByGenus(genus, decorator);
	}

	public List<Object[]> getProteinSuggestions(String orgId, String taxonName, String hcvSubType, String decorator,
			String strainName, String segment, String term) {
		return lookupDao.getProteinSuggestions(orgId, taxonName, hcvSubType, decorator, strainName, segment, term);
	}

	public List<String> getProteinGeneSymbols(String decorator, List<Long> genomicSequenceIds) {
		return lookupDao.getProteinGeneSymbols(decorator, genomicSequenceIds);
	}

	public List<String> getAccessionSuggestions(String orgId, String taxonName, String subType, String decorator,
			String strainName, String segment, String protein, String term) {
		return lookupDao.getAccessionSuggestions(orgId, taxonName, subType, decorator, strainName, segment, protein, term);
	}

	/**
	 * This method is utilized by the Download Runner to clear the Hibernate
	 * StatefulPersistenceContext found the solution
	 * (http://stackoverflow.com/questions
	 * /1071631/trying-to-track-down-a-memory-
	 * leak-garbage-collection-problem-in-java) for the memory leak issue on
	 * performing large download.
	 */
	public void clearCache() {
		lookupDao.clearCache();
	}

	public List<Object[]> getPatternProbeCounts(String resultMatrixId, List<Long> biosetInfoIds, String searchKeyword) {
		return lookupDao.getPatternProbeCounts(resultMatrixId, biosetInfoIds, searchKeyword);
	}

	public List<Object[]> getPatternProbeCountsBm(String resultMatrixId, List<Long> biosetInfoIds, String searchKeyword) {
		return lookupDao.getPatternProbeCountsBm(resultMatrixId, biosetInfoIds, searchKeyword);
	}

	public List<String> getSubtypesByProteinSymbol(String vProteinSymbol) {

		List<String> subtypesForProtein = new ArrayList<String>();
		for (ProteinSubtypes proteinSubtypes : cachedData.getProteinSubtypes()) {
			if (proteinSubtypes.getProteinSymbol().equals(vProteinSymbol))
				subtypesForProtein.add(proteinSubtypes.getSubtype());
		}
		if (subtypesForProtein.size() > 0)
			return subtypesForProtein;
		else
			return null;
	}

	public List getOne(Class aClass) {
		return lookupDao.getOne(aClass);
	}

	public List<Object[]> getDengueVirusSubtypes() {
		return cachedData.getDengueVirusSubtypes();
	}

	public List<Object[]> getHCVVirusSubtypes() {
		return cachedData.getHCVVirusSubtypes();
	}

	public List<Object[]> getWestNileVirusSubtypes() {
		return cachedData.getWestNileVirusSubtypes();
	}

	public List<PDBRecord> findIdenticalAASequencePdbRecords(Long proteinId, Long uniqueAASequenceId, Integer size) {
		return lookupDao.findIdenticalAASequencePdbRecords(proteinId, uniqueAASequenceId, size);
	}

	public List<String> getDengueVirusSubtypesForProteinIds(List<Long> proteinIds) {
		return lookupDao.getDengueVirusSubtypesForProteinIds(proteinIds);
	}

	public List<String> getHCVVirusSubtypesForProteinIds(List<Long> proteinIds) {
		return lookupDao.getHCVVirusSubtypesForProteinIds(proteinIds);
	}

	public List<String> getPoxOrthologIdsForProteinIds(List<Long> proteinIds) {
		return lookupDao.getPoxOrthologIdsForProteinIds(proteinIds);
	}

	public List<String> getInfluenzaProteinNamesForProteinIds(List<Long> proteinIds) {

		List<Protein> proteins = lookupDao.findDataInByClassColumnName(Protein.class, "id", proteinIds, true, true, null, null,
				false);
		Set<String> proteinNames = new HashSet<String>();
		for (Protein p : proteins) {
			proteinNames.add(p.getFluProteinAbbrev());
		}
		return CastHelper.collectionToList(proteinNames);
	}

	public List<Long> getBiosetMetadataIdsByKeywords(Map userInputs) {
		return lookupDao.getBiosetMetatdataIds(userInputs);
	}

	public Map searchPanelExperiments(String criteriaPath, Class modelClass, List<String> groupFields, String sqlGroupExpression,
			Map userInputs, CriteriaAliasFetch aliasFetchModes) {
		return lookupDao.searchPanelExperiments(criteriaPath, modelClass, groupFields, sqlGroupExpression, userInputs,
				aliasFetchModes);
	}

	public Map searchPanelHostFactorCount(String criteriaPath, Class modelClass, List<String> groupFields,
			String sqlGroupExpression, Map userInputs, CriteriaAliasFetch aliasFetchModes) {
		return lookupDao.searchPanelHostFactorCount(criteriaPath, modelClass, groupFields, sqlGroupExpression, userInputs,
				aliasFetchModes);
	}

	public List getAssayHosts(OrganismType ot) {
		return cachedData.getAssayHosts(ot);
	}

	public List<Object[]> findExperimentSampleData(Long expId) {
		return lookupDao.findExperimentSampleData(expId);
	}

	public List findViprUSStates(String familyName) {

		return lookupDao.getUSStates(familyName);
	}

	public List<Long> findReferencePositionWithSF(String proteinAccession) {

		return lookupDao.findReferencePositionWithSF(proteinAccession);

	}

	public List<String> findHCVSubtypeList() {
		return cachedData.getHCVSubtypeList();
	}

	public List<String> findHCVIsolationTissue() {
		return cachedData.getHCVIsolationTissue();
	}

	public List<String> findHCVInfectionType() {
		return cachedData.getHCVInfectionType();
	}

	public List<String> findWestNileSubtypeList() {
		return cachedData.getWestNileSubtypeList();
	}

	public List<String> findBovineSubtypeList() {
		return cachedData.getBovineSubtypeList();
	}

	public List<String> findZikaSubtypeList() {
		return cachedData.getZikaSubtypeList();
	}

	public List<String> findMurraySubtypeList() {
		return cachedData.getMurraySubtypeList();
	}

	public List<String> findJapEncephSubtypeList() {
		return cachedData.getJapEncephSubtypeList();
	}

	public List<String> findTickBourneSubtypeList() {
		return cachedData.getTickBourneSubtypeList();
	}

	public List<String> findStLouisSubtypeList() {
		return cachedData.getStLouisSubtypeList();
	}

	public List<String> findYellowFeverSubtypeList() {
		return cachedData.getYellowFeverSubtypeList();
	}

	public List<String> findGenotypeList(OrganismType orgType) {
		return cachedData.getGenotypeList(orgType);
	}

	public List<String> findPicornaSampleSourceList() {
		return cachedData.getPicornaSampleSourceList();
	}

	public List<String> findPicornaInstitutionList() {
		return cachedData.getPicornaInstitutionList();
	}

	public List<String> findPicornaClinicalDiagnosis() {
		return cachedData.getPicornaClinicalDiagnosis();
	}

	public List<String> findPicornaAsthmaSymptonScores() {
		return cachedData.getPicornaAsthmaSymptonScores();
	}

	public List<String> findPicornaColdSymptonScores() {
		return cachedData.getPicornaColdSymptonScores();
	}

	public List<String> findDengueImmuneStatusList() {
		return cachedData.getDengueImmuneStatusList();
	}

	public List<String> findDengueSampleSourceList() {
		return cachedData.getDengueSampleSourceList();
	}

	public List<String> findHealthStatusList() {
		return cachedData.getHealthStatusList();
	}

	public List<String> findTestTypeList() {
		return cachedData.getTestTypeList();
	}

	public List<String> findPassageHistoryList(OrganismType orgType) {
		return cachedData.getPassageHistoryList(orgType);
	}

	public List<String> findSerotypeList(OrganismType orgType) {
		return cachedData.getSerotypeList(orgType);
	}

	public List<String> findDengueDiseaseCourseList() {
		return cachedData.getDengueDiseaseCourseList();
	}

	public List<String> findDengueSampleCohortList() {
		return cachedData.getDengueSampleCohortList();
	}

	public List<String> findDengueWhoSeverityList() {
		return cachedData.getDengueWhoSeverityList();
	}

	public List<String> findGeneSymbolByOrganismType(OrganismType ot) {
		return cachedData.getGeneSymbolByOrganismType(ot);
	}

	public List<DataSummaryHostFactor> getDataSummaryHostFactor() {
		return cachedData.getDataSummaryHostFactor();
	}

	public List<String> findViralTestResultList(OrganismType orgType) {
		return cachedData.getViralTestResultList(orgType);
	}

	public List<String> findVaccineTypeList(OrganismType orgType) {
		return cachedData.getVaccineTypeList(orgType);
	}

	public List<Object> findOnsetHoursList(OrganismType orgType) {
		return cachedData.getOnsetHoursList(orgType);
	}

	public List<String> findTravelCountryList(OrganismType orgType) {
		return cachedData.getTravelCountryList(orgType);
	}

	public List<String> findSpecimenVoucherList(OrganismType orgType) {
		return cachedData.getSpecimenVoucherList(orgType);
	}

	public List<String> findSerovarList(OrganismType orgType) {
		return cachedData.getSerovarList(orgType);
	}

	public List<String> findPathotypeList(OrganismType orgType) {
		return cachedData.getPathotypeList(orgType);
	}

	public List<String> findSubgroupList(OrganismType orgType) {
		return cachedData.getSubgroupList(orgType);
	}

	public List<String> findSubtypeList(OrganismType orgType) {
		return cachedData.getSubtypeList(orgType);
	}

	public List<Object[]> getExpCountsByType() {
		return cachedData.getExpCountsByType();
	}

	public List<String> findCountryDistrictList(OrganismType orgType) {
		return cachedData.getCountryDistrictList(orgType);
	}

	public List<String> findDiseaseOutcomeList(OrganismType orgType) {
		return cachedData.getDiseaseOutcomeList(orgType);
	}

	public List<String> findDrugClass(String orgFamily) {
		return lookupDao.findDrugClass(orgFamily);
	}

	public List<String> findDrugGroup() {
		List<String> tmpList = lookupDao.findDrugGroup();
		List<String> statusList = new ArrayList<String>();
		// tmpList has values like "approved, investigational", need to parse
		// individual values out
		for (String tmp : tmpList) {
			String[] tmpToken = StringUtils.delimitedListToStringArray(tmp, Constants.COMMA, Constants.SPACE);
			for (String token : tmpToken) {
				token = StringUtils.capitalize(token);
				if (!statusList.contains(token)) {
					statusList.add(token);

				}
			}
		}
		Collections.sort(statusList);
		return statusList;
	}

	public List<String> findDrugTarget() {
		return lookupDao.findDrugTarget();
	}

	public AntiviralDrugTaxonomyData getDrugTaxonomyData() {
		AntiviralDrugTaxonomyData adtd = new AntiviralDrugTaxonomyData();

		List<Object[]> drugTaxonomy = lookupDao.getDrugTaxonomy();
		for (Object[] dt : drugTaxonomy) {
			String family = (String) dt[0];
			String subfamily = (String) dt[1];
			String genus = (String) dt[2];
			String species = (String) dt[3];
			if (!StringUtils.hasText(subfamily))
				subfamily = null;

			// get the family list shown on the landing page
			if (!adtd.getFamilyLookupMap().containsKey(family)) {
				adtd.getFamilyLookupMap().put(family, family);
				adtd.getFamilyList().add(family);
			}
			// get FamilySubFamilyMap
			if (subfamily != null && !adtd.getSubfamilyLookupMap().containsKey(subfamily)) {
				adtd.getSubfamilyLookupMap().put(subfamily, subfamily);
				List<String> subfamilyArray = adtd.getFamilySubFamilyMap().get(family);
				if (subfamilyArray == null)
					subfamilyArray = new ArrayList<String>();
				if (!subfamilyArray.contains(subfamily))
					subfamilyArray.add(subfamily);
				adtd.getFamilySubFamilyMap().put(family, subfamilyArray);
			}

			// get SubfamilyGenusMap
			if (!adtd.getGenusLookupMap().containsKey(genus)) {
				adtd.getGenusLookupMap().put(genus, genus);
				String parent = subfamily;
				// some family does not have subfamily, eg Flaviviridae
				if (subfamily == null)
					parent = family;
				List<String> genusArray = adtd.getSubfamilyGenusMap().get(parent);
				if (genusArray == null)
					genusArray = new ArrayList<String>();
				if (!genusArray.contains(genus))
					genusArray.add(genus);
				adtd.getSubfamilyGenusMap().put(parent, genusArray);

			}

			// get GenusSpeciesMap
			if (!adtd.getSpeciesLookupMap().containsKey(species)) {
				adtd.getSpeciesLookupMap().put(species, species);
				List<String> speciesArray = adtd.getGenusSpeciesMap().get(genus);
				if (speciesArray == null)
					speciesArray = new ArrayList<String>();
				if (!speciesArray.contains(species))
					speciesArray.add(species);
				adtd.getGenusSpeciesMap().put(genus, speciesArray);
			}

		}

		// get the subfamily list shown on the landing page
		List<String> subfamilyList = adtd.getSubFamilyList();
		Iterator it = adtd.getFamilySubFamilyMap().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String family = (String) pair.getKey();
			List<String> subfamilyArray = (List<String>) pair.getValue();
			// create a copy so the map value stays intact
			List<String> subfamilyArrayCopy = new ArrayList<String>(subfamilyArray);
			subfamilyArrayCopy.add(0, getHigherLevelDisplayText(family));
			subfamilyList.addAll(subfamilyArrayCopy);
		}

		// get the genus list shown on the landing page
		List<String> genusList = adtd.getGenusList();
		it = adtd.getSubfamilyGenusMap().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String subfamily = (String) pair.getKey();
			List<String> genusArray = (List<String>) pair.getValue();
			// create a copy so the map value stays intact
			List<String> genusArrayCopy = new ArrayList<String>(genusArray);
			genusArrayCopy.add(0, getHigherLevelDisplayText(subfamily));
			genusList.addAll(genusArrayCopy);
		}

		// get the species list shown on the landing page
		List<String> speciesList = adtd.getSpeciesList();
		it = adtd.getGenusSpeciesMap().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String genus = (String) pair.getKey();
			List<String> speciesArray = (List<String>) pair.getValue();
			// create a copy so the map value stays intact
			List<String> speciesArrayCopy = new ArrayList<String>(speciesArray);
			speciesArrayCopy.add(0, getHigherLevelDisplayText(genus));
			speciesList.addAll(speciesArrayCopy);
		}

		return adtd;

	}

	private String getHigherLevelDisplayText(String name) {
		String retValue = "";
		if (StringUtils.hasText(name))
			retValue = "===" + name + "===";
		return retValue;
	}

	@Override
	public PlasmidSummary findPlasmidById(String plasmidId) {
		return lookupDao.findPlasmidById(plasmidId);
	}

	public List<PlasmidPublication> findPubById(String plasmidId) {
		return lookupDao.findPubById(plasmidId);
	}

	public List<PlasmidFeature> findFeaById(String plasmidId) {
		return lookupDao.findFeaById(plasmidId);
	}

	public List<PlasmidFeature> findPlasmidAnnotationById(String plasmidId) {
		return lookupDao.findPlasmidAnnotationById(plasmidId);
	}

	public PlasmidVector findVectorById(String plasmidvector) {
		return lookupDao.findVectorById(plasmidvector);
	}

	public PlasmidSummary findPlasmidSeq(String plasmidId) {
		return lookupDao.findPlasmidSeq(plasmidId);
	}

	public DrugBankMaster findAntiviralDrugById(Long drugSeqId) {
		return lookupDao.findAntiviralDrugById(drugSeqId);
	}

	public DrugBankMaster findAntiviralDrugByHetId(String hetId) {
		return lookupDao.findAntiviralDrugByHetId(hetId);
	}

	public List<String> findDrugPDBIdsByHetId(String hetId) {
		return lookupDao.findDrugPDBIdsByHetId(hetId);
	}

	public DrugBankTarget findAntiviralDrugTargetById(Long targetSeqId) {
		return lookupDao.findAntiviralDrugTargetById(targetSeqId);
	}

	public List<String> findDrugPDBSpecies(String hetId) {
		return lookupDao.findDrugPDBSpecies(hetId);
	}

	public Map getDrugReferenceToPDBMap(String hetId, String species) {
		List<DrugPdbSpeciesMap> list = lookupDao.findDrugPdbBySpecies(hetId, species);
		if (list != null) {
			Map<String, ArrayList<String>> referenceToPdbMap = new LinkedHashMap<String, ArrayList<String>>();
			for (DrugPdbSpeciesMap dpsm : list) {
				String ncbiRef = dpsm.getNcbiReference();
				String pdbId = dpsm.getPdbId();
				ArrayList<String> pdbList = referenceToPdbMap.get(ncbiRef);
				if (pdbList == null)
					pdbList = new ArrayList<String>();
				if (!pdbList.contains(pdbId))
					pdbList.add(pdbId);
				referenceToPdbMap.put(ncbiRef, pdbList);

			}

			return referenceToPdbMap;
		}
		return null;
	}

	public DrugPdbSpeciesMap findDrugPdbSpeciesMap(String hetId, String ncbiReference, String pdbId) {
		return lookupDao.findDrugPdbSpeciesMap(hetId, ncbiReference, pdbId);
	}

	public List<Object[]> findDrugPdbBindingSite(String hetId, String ncbiReference, String pdbId) {
		return lookupDao.findDrugPdbBindingSite(hetId, ncbiReference, pdbId);
	}

	public DrugRef findDrugReference(String ncbiReference) {
		return lookupDao.findDrugReference(ncbiReference);
	}

	public String findDrugBindingPdbFamily(String pdbId) {
		return lookupDao.findDrugBindingPdbFamily(pdbId);
	}

	public List<String> findDrugMutationSpecies(Long drugSeqId) {
		return lookupDao.findDrugMutationSpecies(drugSeqId);

	}

	public List<DrugMutation> findDrugMutations(Long drugSeqId, String species) {
		return lookupDao.findDrugMutations(drugSeqId, species);
	}

	public SequenceFeatures getResistanceRiskAnnotationSequenceFeature(Long sfnSeqId) {
		return lookupDao.getResistanceRiskAnnotationSequenceFeature(sfnSeqId);
	}

	public String getResistanceRiskAnnotationVTNumber(Long sfnSeqId, String variance) {
		return lookupDao.getResistanceRiskAnnotationVTNumber(sfnSeqId, variance);
	}

	public SequenceFeatures getDrugBindingSiteSequenceFeature(String refAcc, String pdbId) {
		return lookupDao.getDrugBindingSiteSequenceFeature(refAcc, pdbId);
	}

	public List<Object[]> findExperimentBiosetData(Long expId) {
		return lookupDao.findExperimentBiosetData(expId);
	}

	public List<Object[]> getInfluenzaSfvtProteinListByVirusType(InfluenzaType virusType) {
		return lookupDao.getInfluenzaSfvtProteinListByVirusType(virusType);
	}

	public List<String> getVirusTypeByStrainName(Strain virusStrain) {
		return lookupDao.getVirusTypeByStrainName(virusStrain);
	}

	public List<Long> getExperimentIdsByNames(String[] experiementNames) {
		return lookupDao.getExperimentIdsByNames(experiementNames);
	}

	public List<String> convertIds(List<String> ids, List<String> selectedSegments, String convertTo, String family) {
		return lookupDao.convertIds(ids, selectedSegments, convertTo, family);
	}

	public List<Long> getHpiExperimentIds(List<Long> experiementIds) {
		return lookupDao.getHpiExperimentIds(experiementIds);
	}

	public List<String> getHpiExperimentNames(List<Long> experiementIds) {
		return lookupDao.getHpiExperimentNames(experiementIds);
	}

	public List<Long> getHpiExperimentIdByName(String expName) {
		return lookupDao.getHpiExperimentIdByName(expName);
	}

	public List<Object[]> getHpiResultByColumnAndIds(String column, List<String> ids) {
		return lookupDao.getHpiResultByColumnAndIds(column, ids);
	}

	public HfExperiment getHfExperimentById(Long id) {
		return lookupDao.getHfExperimentById(id);
	}

	public List<String> getHostAgeList(String familyName) {
		if (cachedData.getPeriodicUpdateCache().getAdvancedOptionLists() == null)
			return null;
		return cachedData.getPeriodicUpdateCache().getAdvancedOptionLists().get(familyName + Constants.ADV_OPTION_HOST_AGE);
	}

	public List<String> getHostGenderList(String familyName) {
		if (cachedData.getPeriodicUpdateCache().getAdvancedOptionLists() == null)
			return null;
		return cachedData.getPeriodicUpdateCache().getAdvancedOptionLists().get(familyName + Constants.ADV_OPTION_HOST_GENDER);
	}

	public List<String> getOrganismDetectionMethodList(String familyName) {
		if (cachedData.getPeriodicUpdateCache().getAdvancedOptionLists() == null)
			return null;
		return cachedData.getPeriodicUpdateCache().getAdvancedOptionLists().get(familyName + Constants.ADV_OPTION_ORG_DET_METHOD);
	}

	public List<String> getSpecimentTypeList(String familyName) {
		if (cachedData.getPeriodicUpdateCache().getAdvancedOptionLists() == null)
			return null;
		return cachedData.getPeriodicUpdateCache().getAdvancedOptionLists().get(familyName + Constants.ADV_OPTION_SPECIMENT_TYPE);
	}

	public List<String> getSourceHealthStatusList(String familyName) {
		if (cachedData.getPeriodicUpdateCache().getAdvancedOptionLists() == null)
			return null;
		return cachedData.getPeriodicUpdateCache().getAdvancedOptionLists()
				.get(familyName + Constants.ADV_OPTION_SRC_HEALTH_STATUS);
	}

	/*
	 * (non-Javadoc) Starts a restful service to POST a form, and get response
	 * 
	 * @see
	 * com.ngc.brc.services.lookup.LookupService#startToolJerseyRest(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	public ClientResponse startSearchJerseyRest(String ticketTypeName, String parameters, String decorator) {

		TicketType ticketType = TicketType.getTicketTypeByType(ticketTypeName);
		String baseUrl = null;
		if (decorator.equalsIgnoreCase("influenza"))
			baseUrl = EnvironmentConfig.getFluWebServerExternalUrl();
		else
			baseUrl = EnvironmentConfig.getViprWebServerExternalUrl();
		String resourceUrl = baseUrl + "/brc" + ticketType.getActionBeanName();
		System.out.println("LookupService.startSearchJerseyRest: resourceUrl=" + resourceUrl);

		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource webResource = client.resource(resourceUrl);
		MultivaluedMap params = new MultivaluedMapImpl();
		// params.add(BeanFields.TICKET_NUMBER, ticketNumber);
		params.add("parameters", parameters);
		// params.add(ToolConstants.INPUT_SEQUENCE, inputSequence);

		// POST a form
		// String response = webResource.post(String.class, params);
		ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, parameters);
		System.out.println("startSearchJerseyRest: response code: " + response.getStatus());
		return response;
	}

	/*
	 * (non-Javadoc) Starts a restful service to POST a form, and get response
	 * 
	 * @see
	 * com.ngc.brc.services.lookup.LookupService#startToolJerseyRest(java.lang.
	 * String, java.lang.String, java.lang.String)
	 */
	public ClientResponse startSearchJerseyRestOtherBrc(String brcName, String ticketTypeName, String parameters,
			String decorator) {

		TicketType ticketType = TicketType.getTicketTypeByType(ticketTypeName);
		String baseUrl = null;
		if (Constants.BRC_EUPATHDB.equalsIgnoreCase(brcName))
			baseUrl = "http://hostdb.org/hostdb/service/hpi/search/experiment/gene-list";
		else if (Constants.BRC_PATRIC.equalsIgnoreCase(brcName))
			baseUrl = "https://www.patricbrc.org/api/hpi/search";
		else if (Constants.BRC_VECTORBASE.equalsIgnoreCase(brcName))
			baseUrl = "";
		String resourceUrl = baseUrl + "";
		System.out.println("startSearchJerseyRestOtherBrc: resourceUrl=" + resourceUrl);

		if (!StringUtils.hasLength(resourceUrl))
			return null;

		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource webResource = client.resource(resourceUrl);
		MultivaluedMap params = new MultivaluedMapImpl();
		// params.add(BeanFields.TICKET_NUMBER, ticketNumber);
		params.add("parameters", parameters);
		// params.add(ToolConstants.INPUT_SEQUENCE, inputSequence);

		// POST a form
		// String response = webResource.post(String.class, params);
		ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, parameters);
		System.out.println("startSearchJerseyRestOtherBrc: response code: " + response.getStatus());
		return response;
	}

	// BRC-11509
	public List<String> getModuleColorsByExperimentId(long experiementId) {
		return lookupDao.getModuleColorsByExperimentId(experiementId);
	}

	public List<String> getGeneIdsbyModuleId(Long experimentId, String moduleId) {
		return lookupDao.getGeneIdsbyModuleId(experimentId, moduleId);
	}

	public List<String> getFindModuleColors(long experiementId) {
		return lookupDao.findModuleColors(experiementId);
	}

	public List<HfCorrGeneInfo> getFindModuleGenes(long experiementId, String moduleId) {
		return lookupDao.findModuleGenes(experiementId, moduleId);
	}

	public List<Object[]> getModuleMetaData(Long expId) {
		return lookupDao.findModuleMetaData(expId);

	}

	public List<Object[]> getExperimentGeneListForDownload(Long expId) {
		return lookupDao.getExperimentGeneListForDownload(expId);
	}

	public Map<String, String> getModuleGeneSignificance(long expSeqId, String metaDataDimension, String moduleColor) {
		Map<String, String> map = new HashMap<String, String>();
		List<Object[]> proteinIdGeneId = lookupDao.getModuleGeneSignificance(expSeqId, moduleColor, metaDataDimension);

		for (Object[] data : proteinIdGeneId) {
			map.put((String) data[0], (String) data[1]);
		}

		return map;
	}

	public List<HfCorrGeneInfo> findModuleGeneSignificance(long expSeqId, String metaDataDimension, String moduleColor) {

		return lookupDao.findModuleGeneSignificance(expSeqId, moduleColor, metaDataDimension);
	}

	public List<String> findMetadataCategories(Long expSeqId) {
		return lookupDao.findMetadataCategories(expSeqId);
	}

	public boolean isRNASeqExperiment(long expSeqId) {
		return lookupDao.isRNASeqExperiment(expSeqId);
	}

	public List<Object[]> findMetaDataByCategory(long expSeqId) {
		return lookupDao.findMetaDataByCategory(expSeqId);

	}

	public List<HfCorrGeneInfo> findGenesData(long expSeqId, String moduleColor, List<String> geneId, String moduleCategory) {

		return lookupDao.findGenesData(expSeqId, moduleColor, geneId, moduleCategory);
	}

	public List<HfCorrGeneInfo> findGenesDatabyExprmtId(long expSeqId) {

		return lookupDao.findGenesDatabyExprmtId(expSeqId);
	}

	@Override
	public List<SpProject> getSystemBiologyProjectDetails() {
		return lookupDao.getSystemBiologyProjectDetails();
	}

	@Override
	public List<HfExperiment> GetAllHFExperiments() {
		return lookupDao.GetAllHFExperiments();
	}

	@Override
	public List<HfCorrGeneInfo> findModuleGeneDataByEntrezGeneID(List<String> entrezGeneId, String expId, Map entrezIds) {
		return lookupDao.findModuleGeneDataByEntrezGeneID(entrezGeneId, expId, entrezIds);
	}

	@Override
	public List<HfCorrEdgeAll> getEdgedata(String expId, String moduleId) {
		return lookupDao.getEdgedata(expId, moduleId);
	}

	@Override
	public Map<String, String> getNodedata(String expId, String moduleId) {
		return lookupDao.getNodedata(expId, moduleId);
	}

	@Override
	public List<HfCorrNodeAll> getNodelist(String expId, String moduleId) {
		return lookupDao.getNodelist(expId, moduleId);
	}

	@Override
	public int getTotalNode(String expId, String moduleId) {
		return lookupDao.getTotalNode(expId, moduleId);
	}

	@Override
	public int getTotalEdge(String expId, String moduleId) {
		return lookupDao.getTotalEdge(expId, moduleId);
	}
}

