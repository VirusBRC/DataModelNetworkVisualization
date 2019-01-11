package com.ngc.brc.dao.lookup;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.ngc.brc.commons.constants.Constants;
import com.ngc.brc.commons.helperobjects.NameValue;
import com.ngc.brc.commons.helperobjects.TableFields;
import com.ngc.brc.commons.utils.CastHelper;
import com.ngc.brc.commons.utils.Utils;
import com.ngc.brc.commons.utils.enums.ApplicationType;
import com.ngc.brc.commons.utils.enums.OrganismType;
import com.ngc.brc.commons.utils.enums.SubFilterType;
import com.ngc.brc.dao.ReadOnlyUtilityDaoImplHibernate;
import com.ngc.brc.dao.cachedData.CachedDataWrapper;
import com.ngc.brc.dao.search.common.featureVariant.SequenceFeatureVariantsSearchDaoImpl;
import com.ngc.brc.dao.search.common.hostFactor.HostFactorBiosetsSearchDao;
import com.ngc.brc.dao.search.common.hostFactor.HostFactorExperimentsSearchDao;
import com.ngc.brc.dao.search.common.hostFactor.HostFactorPatternsSearchDao;
import com.ngc.brc.dao.search.util.KeywordProcessor;
import com.ngc.brc.dao.search.util.SearchCriteriaUtils;
import com.ngc.brc.dao.util.CommonCriteria;
import com.ngc.brc.dao.util.CriteriaAlias;
import com.ngc.brc.dao.util.CriteriaAliasFetch;
import com.ngc.brc.dao.util.CriteriaOperators;
import com.ngc.brc.dao.util.CriteriaRestrictionBuilder;
import com.ngc.brc.dao.util.CriteriaUtils;
import com.ngc.brc.dao.util.DaoUtils;
import com.ngc.brc.enums.HostFactorHostType;
import com.ngc.brc.enums.InfluenzaType;
import com.ngc.brc.enums.SearchDaoMapper;
import com.ngc.brc.model.BlastResultsView;
import com.ngc.brc.model.ContinentCountryGroup;
import com.ngc.brc.model.FOMA;
import com.ngc.brc.model.Feature;
import com.ngc.brc.model.GenomicSequence;
import com.ngc.brc.model.IedbAssays;
import com.ngc.brc.model.IedbEpitope;
import com.ngc.brc.model.Organism;
import com.ngc.brc.model.Protein;
import com.ngc.brc.model.ProteinEpitope;
import com.ngc.brc.model.ProteinFOMA;
import com.ngc.brc.model.SNP;
import com.ngc.brc.model.SwissprotPdbMapping;
import com.ngc.brc.model.WHTextIdxMst;
import com.ngc.brc.model.antiviralDrug.DrugBankMaster;
import com.ngc.brc.model.antiviralDrug.DrugBankTarget;
import com.ngc.brc.model.antiviralDrug.DrugMutation;
import com.ngc.brc.model.antiviralDrug.DrugPdbBindingSiteOnRef;
import com.ngc.brc.model.antiviralDrug.DrugPdbSpeciesMap;
import com.ngc.brc.model.antiviralDrug.DrugRef;
import com.ngc.brc.model.antiviralDrug.DrugVirusLink;
import com.ngc.brc.model.drug.DrugInformation;
import com.ngc.brc.model.featureVariant.SFVTPosLookup;
import com.ngc.brc.model.featureVariant.SequenceFeatureSourceStrainInfo;
import com.ngc.brc.model.featureVariant.SequenceFeatures;
import com.ngc.brc.model.featureVariant.SequenceVariants;
import com.ngc.brc.model.featureVariant.vipr.ProteinSequenceFeatureVariants;
import com.ngc.brc.model.hostFactor.BiosetList;
import com.ngc.brc.model.hostFactor.HfCorrConnectivity;
import com.ngc.brc.model.hostFactor.HfExperiment;
import com.ngc.brc.model.hpi.HfExpPos;
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
import com.ngc.brc.model.primerProbe.PrimerProbeMetaData;
import com.ngc.brc.model.proteinRecords.PDBRecord;
import com.ngc.brc.model.proteinRecords.PDBStructure;
import com.ngc.brc.model.strain.Strain;
import com.ngc.brc.model.supportedPrograms.SpProject;
import com.ngc.brc.model.surveillance.Host;
import com.ngc.brc.model.surveillance.Species;
import com.ngc.brc.model.surveillance.StudySample;
import com.ngc.brc.model.virus.ortholog.VbrcAnnotation;
import com.ngc.brc.model.virus.ortholog.VbrcEpiData;
import com.ngc.brc.model.virus.ortholog.VbrcEpiDataView;
import com.ngc.brc.model.virus.ortholog.VbrcKnowledgeBase;
import com.ngc.brc.model.virus.ortholog.VbrcOrthologGroup;
import com.ngc.brc.util.BrcConstants;
import com.ngc.brc.util.common.HostFactorFormFields;
import com.ngc.brc.util.common.HostFactorModelFields;
import com.ngc.brc.util.common.PlasmidFormFields;
import com.ngc.brc.util.hibernate.FeatureIdentifierSFVTOrder;
import com.ngc.brc.util.hibernate.LowerCaseOrder;
import com.ngc.brc.util.hibernate.NativeSQLOrderNoParens;
import com.ngc.brc.util.idstrategy.IdStrategy;

@Repository("lookupDao")
public class LookupDaoImplHibernate extends ReadOnlyUtilityDaoImplHibernate implements LookupDao {

	@Resource
	private CachedDataWrapper cachedData;

	@SuppressWarnings(value = { "unchecked" })
	public List<Object[]> getNetCtlPredictionSummary(Long proteinId) {
		Session session = super.getSession();
		Criteria criteria = session.createCriteria(ProteinEpitope.class);

		criteria.setProjection(Projections.projectionList().add(Projections.groupProperty("mhcSuperFamily"))
				.add(Projections.count("mhcSuperFamily"), "noPredictions"));
		criteria.add(Restrictions.eq("protein.id", proteinId));
		List<Object[]> result = criteria.list();
		return result;
	}

	public List<Object[]> getSnpSummary(Long geneId) {

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(SNP.class);

		criteria.setProjection(Projections.projectionList().add(Projections.groupProperty("host"))
				.add(Projections.groupProperty("subtype")).add(Projections.groupProperty("segment"))
				.add(Projections.groupProperty("type")).add(Projections.count("type"), "snpCount"));
		criteria.add(Restrictions.eq("gene.id", geneId));
		List<Object[]> result = criteria.list();
		return result;
	}

	public List<Object[]> getAccessionsForProteinId(List<Long> ids) {

		Criteria criteria = super.getSession().createCriteria(Protein.class);
		CriteriaUtils.in(criteria, "id", ids);
		criteria.createAlias("genomicSequence", "genomicSequence");
		ProjectionList pl = Projections.projectionList();
		pl.add(Projections.property("ncbiProteinIdBase"));
		pl.add(Projections.property("genomicSequence.ncbiAccession"));
		criteria.setProjection(pl);
		return criteria.list();
	}

	public List<Object[]> getPDBProteinBySF(String studyStrainAccession, SequenceFeatures sequenceFetures) {
		if (studyStrainAccession == null) {
			return null;
		}

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(SwissprotPdbMapping.class);
		criteria.createAlias("protein", "protein");
		criteria.createAlias("protein.genomicSequence", "genomicSequence");
		criteria.add(Restrictions.isNull("genomicSequence.obsoleteDate"));
		criteria.add(Restrictions.eq("genomicSequence.family", sequenceFetures.getFamily()));

		OrganismType ot = OrganismType.getByFamilyName(sequenceFetures.getFamily());
		if (OrganismType.POX.equals(ot)) {
			// Special treatment for Pox
			criteria.createAlias("protein.whOrthologs", "whOrthologs");
			criteria.createAlias("whOrthologs.whOrthologMaster", "whOrthologMaster");
			criteria.add(Restrictions.eq("whOrthologMaster.id", sequenceFetures.getOrthologId()));
		} else {
			criteria.add(Restrictions.eq("genomicSequence.ncbiAccession", studyStrainAccession));
			if (sequenceFetures.getStartPosition() != null && sequenceFetures.getEndPosition() != null) {
				criteria.add(Restrictions.between("uniprotIndex", Integer.parseInt(sequenceFetures.getStartPosition()),
						Integer.parseInt(sequenceFetures.getEndPosition())));
			} else {
				// Single position case.
				if (sequenceFetures.getStartPosition() != null) {
					criteria.add(Restrictions.eq("uniprotIndex", Integer.parseInt(sequenceFetures.getStartPosition())));
				} else if (sequenceFetures.getEndPosition() != null) {
					criteria.add(Restrictions.eq("uniprotIndex", Integer.parseInt(sequenceFetures.getEndPosition())));
				}
			}
		}
		criteria.add(Restrictions.gt("pdbIndex", 0));

		criteria.setProjection(Projections.distinct(
				Projections.projectionList().add(Projections.property("pdbId")).add(Projections.property("protein.id"))));
		criteria.addOrder(Order.asc("pdbId"));

		List<Object[]> swissProtPDBs = criteria.list();
		return swissProtPDBs;

	}

	public List<Object[]> getViprSFVTDataByPDB(List proteinIds, String decorator, List<PDBSPMappingSummary> mappingSummary) {
		OrganismType ot = OrganismType.getByDecoratorName(decorator);
		String family = ot.getFamilyName();

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(ProteinSequenceFeatureVariants.class);
		criteria.createAlias("protein", "protein");
		criteria.createAlias("sequenceFeatures", "sequenceFeatures");
		criteria.createAlias("sequenceVariants", "sequenceVariants");
		criteria.createAlias("sequenceFeatures.sequencePositions", "sequencePositions");

		CriteriaUtils.in(criteria, "protein.id", proteinIds);
		criteria.add(Restrictions.eq("sequenceFeatures.featureGroup", SequenceFeatures.FEATURE_GROUP_SFVT));
		criteria.add(Restrictions.eq("sequenceFeatures.family", family));

		if (OrganismType.POX.equals(ot)) {
			criteria.createAlias("protein.whOrthologs", "whOrthologs");
			criteria.createAlias("whOrthologs.whOrthologMaster", "whOrthologMaster");
			criteria.add(Restrictions.eqProperty("whOrthologMaster.id", "sequenceFeatures.orthologId"));
		} else {
			if (OrganismType.FLAVI_DENGUE.equals(ot)) {
				criteria.add(Restrictions.ilike("sequenceFeatures.orgSpecies", ot.getSubFilterType().getHierarchyName(),
						MatchMode.START));
			} else if (OrganismType.FLAVI_HCV.equals(ot)) {
				criteria.add(Restrictions.eq("sequenceFeatures.orgGenus", "Hepacivirus"));
			}

			if (mappingSummary != null) {
				int min = Integer.MAX_VALUE;
				int max = Integer.MIN_VALUE;
				for (PDBSPMappingSummary next : mappingSummary) {
					int nextMax = Integer.parseInt(next.getMaxPos());
					int nextMin = Integer.parseInt(next.getMinPos());
					if (nextMax > max) {
						max = nextMax;
					}

					if (nextMin < min) {
						min = nextMin;
					}
				}

				SearchCriteriaUtils.buildPositionRangeQuery(criteria, String.valueOf(min), "sequencePositions.startPosition",
						String.valueOf(max), "sequencePositions.endPosition", null);
			}
		}

		ProjectionList pl = Projections.projectionList();
		pl.add(Projections.property("sequenceFeatures.segmentName"));
		pl.add(Projections.property("sequenceFeatures.category"));
		pl.add(Projections.property("sequenceFeatures.featureIdentifier"));
		pl.add(Projections.property("sequenceVariants.nonFluSFVTId"));
		pl.add(Projections.property("sequenceFeatures.featureName"));
		pl.add(Projections.property("sequenceFeatures.proteinName"));
		pl.add(Projections.property("sequenceFeatures.positionRange"));
		pl.add(Projections.property("sequenceFeatures.id"));
		criteria.setProjection(pl);

		String sortBy = "SFVTFeatureIdentifier(sequenceFeatures.featureIdentifier)";
		String property = sortBy.substring(sortBy.indexOf('(') + 1, sortBy.indexOf(')'));
		criteria.addOrder(new FeatureIdentifierSFVTOrder(property, true, "_SF"));

		return criteria.list();
	}

	/**
	 * Count how many sequence features there are for a ViPR decorator
	 */
	public Object countSequenceFeaturesExistForViprDecorator(String decorator) {
		Criteria criteria = super.getSession().createCriteria(SequenceFeatures.class);
		SearchCriteriaUtils.filterViprSfByDecorator(criteria, decorator);
		criteria.add(Restrictions.eq("featureGroup", SequenceFeatures.FEATURE_GROUP_SFVT));

		criteria.setProjection(Projections.rowCount());
		return criteria.uniqueResult();
	}

	public Object getIedbCount(Long id) {
		Criteria criteria = super.getSession().createCriteria(IedbEpitope.class);
		criteria.createAlias("iedbSwissIndexes", "swissIndex");
		criteria.createAlias("swissIndex.protein", "protein");
		criteria.setProjection(Projections.countDistinct("iedbId"));
		criteria.add(Restrictions.eq("protein.id", id));

		return criteria.uniqueResult();
	}

	public Object getHostToStrainCount(List ids) {
		int count = getHostToStrainIds(ids).size();
		return new Long(count);
	}

	public List getHostToStrainIds(List ids) {
		List strainIds = new ArrayList();
		LinkedHashSet set = new LinkedHashSet();
		for (List<String> chunk : Utils.separateIntoChunks(ids)) {
			List strainIdsChunk = getHostToStrainIdsForEachChunk(chunk);
			set.addAll(strainIdsChunk);
		}
		strainIds.addAll(set);
		return strainIds;
	}

	public List getHostToStrainIdsForEachChunk(List chunk) {
		Criteria criteria = super.getSession().createCriteria(Host.class);
		criteria.createAlias("studySamples", "studySamples");
		criteria.createAlias("studySamples.viruses", "viruses");
		criteria.createAlias("viruses.strain", "strain");
		criteria.add(Restrictions.eq("family", OrganismType.FLU.getFamilyName()));

		criteria.add(Restrictions.in("id", CastHelper.stringListToListOfLongs(chunk)));

		ProjectionList pList = Projections.projectionList();
		pList.add(Projections.property("strain.id"));
		criteria.setProjection(pList);
		return criteria.list();
	}

	public Object getHostToSegmentCount(List ids) {
		int count = getHostToSegmentIds(ids, null).size();
		return new Long(count);
	}

	public List getHostToSegmentIds(List ids, List segment) {
		List segmentIds = new ArrayList();
		LinkedHashSet set = new LinkedHashSet();
		for (List<String> chunk : Utils.separateIntoChunks(ids)) {
			List segmentIdsChunk = getHostToSegmentIdsForEachChunk(chunk, segment);
			set.addAll(segmentIdsChunk);
		}
		segmentIds.addAll(set);
		return segmentIds;
	}

	public List getHostToSegmentIdsForEachChunk(List chunk, List segments) {
		Criteria criteria = super.getSession().createCriteria(Host.class);
		criteria.createAlias("studySamples", "studySamples");
		criteria.createAlias("studySamples.viruses", "viruses");
		criteria.createAlias("viruses.strain", "strain");
		criteria.createAlias("strain.sequences", "sequences");
		criteria.add(Restrictions.isNull("sequences.obsoleteDate"));
		criteria.add(Restrictions.eq("family", OrganismType.FLU.getFamilyName()));

		criteria.add(Restrictions.in("id", CastHelper.stringListToListOfLongs(chunk)));

		if (segments != null && segments.size() > 0) {
			String firstPos = (String) segments.get(0);
			if (!firstPos.equals(Constants.ALL))
				criteria.add(Restrictions.in("sequences.segment", segments));
		}

		ProjectionList pList = Projections.projectionList();
		pList.add(Projections.property("sequences.id"));
		criteria.setProjection(pList);
		return criteria.list();
	}

	public List<Object[]> getViprStrainsWithCompleteSequences(Date fromCreateDate, Date endCreateDate) {

		Criteria criteria = super.getSession().createCriteria(Strain.class);
		criteria.createAlias("sequences", "sequences");
		criteria.add(Restrictions.ne(BrcConstants.FAMILY_PARTITION, OrganismType.FLU.getFamilyName()));
		criteria.add(Restrictions.eq("sequences.completeSequence", "1"));
		criteria.add(Restrictions.between("creationDate", fromCreateDate, endCreateDate));
		criteria.setProjection(Projections
				.distinct(Projections.projectionList().add(Projections.property("name")).add(Projections.property("family"))));

		return criteria.list();
	}

	public Object getHostToProteinCount(List ids) {
		int count = getHostToProteinIds(ids, null).size();
		return new Long(count);
	}

	public List getHostToProteinIds(List ids, List proteins) {
		List proteinIds = new ArrayList();
		LinkedHashSet set = new LinkedHashSet();
		for (List<String> chunk : Utils.separateIntoChunks(ids)) {
			List proteinIdsChunk = getHostToProteinIdsForEachChunk(chunk, proteins);
			set.addAll(proteinIdsChunk);
		}
		proteinIds.addAll(set);
		return proteinIds;
	}

	public List getHostToProteinIdsForEachChunk(List chunk, List proteins) {
		Criteria criteria = super.getSession().createCriteria(Host.class);
		criteria.createAlias("studySamples", "studySamples");
		criteria.createAlias("studySamples.viruses", "viruses");
		criteria.createAlias("viruses.strain", "strain");
		criteria.createAlias("strain.sequences", "sequences");
		criteria.createAlias("sequences.proteins", "proteins");
		criteria.add(Restrictions.isNull("sequences.obsoleteDate"));
		criteria.add(Restrictions.eq("family", OrganismType.FLU.getFamilyName()));

		criteria.add(Restrictions.in("id", CastHelper.stringListToListOfLongs(chunk)));

		if (proteins != null && proteins.size() > 0) {
			String firstPos = (String) proteins.get(0);
			if (!firstPos.equals(Constants.ALL))
				criteria.add(Restrictions.in("proteins.genePrimaryName", proteins));
		}

		ProjectionList pList = Projections.projectionList();
		pList.add(Projections.property("proteins.id"));
		criteria.setProjection(pList);
		return criteria.list();
	}

	public List<VbrcAnnotation> getVbrcAnnotationsByProteinNcbiId(String ncbiId, String order) {

		Criteria criteria = super.getSession().createCriteria(VbrcAnnotation.class);
		criteria.createAlias("proteins", "protein");
		criteria.add(Restrictions.eq("protein.ncbiId", ncbiId));
		criteria.add(Restrictions.isNotNull("genus"));
		return criteria.list();
	}

	public List<VbrcKnowledgeBase> getVbrcKnowledgeBaseByVbrcGeneId(Long vbrcGeneId) {
		Criteria criteria = super.getSession().createCriteria(VbrcKnowledgeBase.class);
		criteria.add(Restrictions.eq("id", vbrcGeneId));
		return criteria.list();
	}

	public Map<String, List<VbrcEpiDataView>> getEpiDataSets() {
		Map<String, List<VbrcEpiDataView>> returnMap = new HashMap<String, List<VbrcEpiDataView>>();
		Session session = super.getSession();
		Criteria criteria = session.createCriteria(VbrcEpiDataView.class);
		criteria.addOrder(Order.asc("id.typeDefinition"));
		List<VbrcEpiDataView> results = criteria.list();

		addSrcCvtToEpiMap(returnMap, criteria.list(), "1304");
		addSrcCvtToEpiMap(returnMap, criteria.list(), "1343");
		addSrcCvtToEpiMap(returnMap, criteria.list(), "1390");
		return returnMap;
	}

	private void addSrcCvtToEpiMap(Map<String, List<VbrcEpiDataView>> map, List<VbrcEpiDataView> results, String srcCvtVal) {
		List srcCvtList = new ArrayList();
		for (VbrcEpiDataView result : results) {
			if (new Long(srcCvtVal).equals(result.getId().getSrcCvt())) {
				srcCvtList.add(result);
			}
		}
		map.put(srcCvtVal, srcCvtList);
	}

	public List<VbrcEpiData> getEpiDataSourceType(String srcCvt, String typeCvt) {
		Session session = super.getSession();
		Criteria criteria = session.createCriteria(VbrcEpiData.class);
		criteria.add(Restrictions.eq("srcCvt", new Long(srcCvt)));
		criteria.add(Restrictions.eq("definitionCvt", new Long(typeCvt)));
		criteria.add(Restrictions.isNotNull("value"));
		criteria.addOrder(Order.asc("countryName"));
		criteria.addOrder(Order.asc("valDate"));
		List<VbrcEpiData> results = criteria.list();
		return results;
	}

	public List<Object[]> getEpiDataByCountryYear(String year) {

		Criteria criteria = super.getSession().createCriteria(VbrcEpiData.class);
		ProjectionList projs = Projections.projectionList();
		projs.add(Projections.count("value"));
		projs.add(Projections.groupProperty("countryName"));

		DateFormat format = new SimpleDateFormat("yyyy");

		try {
			Date date = (Date) format.parse(year);
			criteria.add(Restrictions.eq("valDate", date));
		} catch (Exception e) {
			throw new RuntimeException("Invalid year field.  Cannot proceed.");
		}
		criteria.add(Restrictions.isNotNull("value"));
		criteria.setProjection(projs);

		return criteria.list();
	}

	public Protein getProteinByNcbiProteinId(String ncbiProteinId) {
		Criteria proteinCriteria = super.getSession().createCriteria(Protein.class);

		proteinCriteria.add(Restrictions.eq("ncbiProteinIdBase", ncbiProteinId));

		// Don't include obsolete genes
		Criteria genomicSequenceCriteria = proteinCriteria.createCriteria("genomicSequence");
		genomicSequenceCriteria.add(Restrictions.isNull("obsoleteDate"));

		proteinCriteria.setFetchMode("genomicSequence", FetchMode.JOIN);

		List results = proteinCriteria.list();

		if (results == null || results.size() == 0) {
			return null;
		}

		if (results.size() > 1) {
			throw new RuntimeException("Multiple proteins returned for a ncbiProteinId.  Cannot proceed.");
		}

		return (Protein) results.get(0);
	}

	// test for adding UniProt id
	public Protein getProteinBySwissProtId(String swissProtId) {
		Criteria proteinCriteria = super.getSession().createCriteria(Protein.class);

		proteinCriteria.add(Restrictions.eq("swissProtId", swissProtId));

		// Don't include obsolete genes
		Criteria genomicSequenceCriteria = proteinCriteria.createCriteria("genomicSequence");
		genomicSequenceCriteria.add(Restrictions.isNull("obsoleteDate"));

		proteinCriteria.setFetchMode("genomicSequence", FetchMode.JOIN);

		List results = proteinCriteria.list();

		if (results == null || results.size() == 0) {
			return null;
		}

		if (results.size() > 1) {
			throw new RuntimeException("Multiple proteins returned for a swissProtId.  Cannot proceed.");
		}

		return (Protein) results.get(0);
	}

	// end of test

	public List<BlastResultsView> getBlastResults(String ncbiProteinId) {

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(BlastResultsView.class);
		if (ncbiProteinId != null) {
			criteria.add(Restrictions.eq("ncbiProteinId", ncbiProteinId));
			return criteria.list();
		}
		return null;
	}

	public List<Object[]> getGenomicSequenceIdsForProteinIds(List<String> ids) {

		Criteria criteria = super.getSession().createCriteria(Protein.class);
		CriteriaUtils.in(criteria, "id", CastHelper.stringListToListOfLongs(ids));
		criteria.createAlias("genomicSequence", "genomicSequence");
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("id"));
		projList.add(Projections.property("genomicSequence.id"));
		criteria.setProjection(projList);
		return criteria.list();

	}

	public List<Object[]> getProteinIdsForGenomicSequenceIds(List<String> ids) {

		List<Object[]> result = new ArrayList<Object[]>();

		for (List chunk : Utils.separateIntoChunks(ids)) {
			Criteria criteria = super.getSession().createCriteria(GenomicSequence.class);
			CriteriaUtils.in(criteria, "id", CastHelper.stringListToListOfLongs(chunk));
			criteria.createAlias("proteins", "proteins");
			ProjectionList projList = Projections.projectionList();
			projList.add(Projections.property("id"));
			projList.add(Projections.property("proteins.id"));
			criteria.setProjection(projList);
			result.addAll(criteria.list());
		}
		return result;

	}

	public Object getCDSCountForGenomicSequenceIds(List<String> ids) {
		Criteria criteria = super.getSession().createCriteria(Feature.class);
		criteria.createAlias("genomicSequence", "genomicSequence");
		criteria.add(Restrictions.eq("type", "CDS"));

		for (List chunk : Utils.separateIntoChunks(ids)) {

			CriteriaUtils.in(criteria, "genomicSequence.id", CastHelper.stringListToListOfLongs(chunk));
		}
		criteria.setProjection(Projections.rowCount());
		return criteria.uniqueResult();

	}

	public List<Object[]> getGenomicSequenceIdsForNcbiGenomeAccession(List<String> ncbiGenomeAccessions, OrganismType ot) {

		Criteria criteria = super.getSession().createCriteria(GenomicSequence.class, "genomicSequence");

		CriteriaUtils.in(criteria, "ncbiAccession", ncbiGenomeAccessions);
		if (ot != null) {
			CriteriaUtils.eqString(criteria, "family", ot.getFamilyName(), "");
		}
		criteria.setProjection(
				Projections.projectionList().add(Projections.property("ncbiAccession")).add(Projections.property("id")));
		criteria.add(Restrictions.isNull("obsoleteDate"));

		return criteria.list();

	}

	public List<Object[]> getGenomicSequenceIdsForFeatureIds(List<String> ids) {

		Criteria criteria = super.getSession().createCriteria(Feature.class);
		CriteriaUtils.in(criteria, "id", CastHelper.stringListToListOfLongs(ids));
		criteria.setProjection(
				Projections.projectionList().add(Projections.property("id")).add(Projections.property("genomicSequence.id")));

		return criteria.list();
	}

	public List<Object[]> getProteinPrimaryKeysForNcbiProteinIds(List<String> ids) {

		Criteria criteria = super.getSession().createCriteria(Protein.class);
		CriteriaUtils.in(criteria, "ncbiId", ids);
		criteria.setProjection(Projections.projectionList().add(Projections.property("ncbiId")).add(Projections.property("id")));

		return criteria.list();
	}

	public List getAllForClass(Class clazz) {
		return DaoUtils.getAllForClass(super.getSession(), clazz);
	}

	/**
	 * Returns a list of entities identified by the IDs passed in and maintain
	 * the order of the IDs
	 * 
	 * @param idStrings
	 *            - IDs of the entites to be retrieved
	 * @param clazz
	 *            - the domain class of the data to be retrieved
	 */
	public List getByIdsInOrder(List<String> idStrings, Class clazz) {

		CriteriaAliasFetch criteriaAliasFetch = null;
		if (clazz.getName().equals(GenomicSequence.class.getName())) {

			criteriaAliasFetch = new CriteriaAliasFetch();
			List<CriteriaAlias> aliases = new ArrayList<CriteriaAlias>();
			criteriaAliasFetch.setAliases(aliases);

		} else if (clazz.getName().equals(Protein.class.getName())) {

			criteriaAliasFetch = new CriteriaAliasFetch();
			List<CriteriaAlias> aliases = new ArrayList<CriteriaAlias>();
			CriteriaAlias alias = new CriteriaAlias("genomicSequence", "genomicSequence");
			aliases.add(alias);
			criteriaAliasFetch.setAliases(aliases);
		}

		// Put back into the same order as the IDs
		List<? extends Object> results = null;
		List<? extends Object> tempResults = getByIds(idStrings, clazz, null, null, null, criteriaAliasFetch, null, null);
		if (tempResults != null)
			results = Utils.putDataInCorrectOrder(idStrings, tempResults,
					getSessionFactory().getClassMetadata(clazz).getIdentifierPropertyName(), 0);

		return results;
	}

	/**
	 * Returns a count of how many records have the IDs passed in. This can be
	 * used to detect if all of the IDs are valid for the data type.
	 */
	public Object getCountByIds(Class clazz, List<String> ids) {
		Criteria crit = super.getSession().createCriteria(clazz);
		crit.setProjection(Projections.rowCount());
		Disjunction dis = Restrictions.disjunction();
		for (List<String> chunk : Utils.separateIntoChunks(ids)) {
			dis.add(Restrictions.in("id", CastHelper.stringListToListOfLongs(chunk)));
		}
		crit.add(dis);
		return crit.uniqueResult();
	}

	/**
	 * Returns a list of entities identified by the IDs passed in
	 * 
	 * @param idStrings
	 *            - IDs of the entites to be retrieved
	 * @param clazz
	 *            - the domain class of the data to be retrieved
	 * @param eagerProperties
	 *            - list of property paths to be eagerly fetched along with the
	 *            data
	 * @param idStrategy
	 *            - strategy to be applied to the IDs (get before dash, etc)
	 */
	public List getByIds(List<String> idStrings, Class clazz, List<String> eagerProperties, IdStrategy idStrategy,
			String projectionPropertyName, CriteriaAliasFetch aliasFamily, String decorator, ProjectionList projections) {

		String identifierPropertyName = getSessionFactory().getClassMetadata(clazz).getIdentifierPropertyName();
		Type identifierPropertyType = getSessionFactory().getClassMetadata(clazz).getIdentifierType();

		// Use the strategy to process the IDs if required
		if (idStrategy != null)
			idStrings = idStrategy.processIds(idStrings);

		List ids;

		if (identifierPropertyType instanceof LongType)
			ids = CastHelper.stringListToListOfLongs(idStrings);
		else if (identifierPropertyType instanceof IntegerType)
			ids = CastHelper.stringListToListOfIntegers(idStrings);
		else
			ids = idStrings;

		// Break up into chunks if > 1000 items
		if (ids != null && ids.size() > 1000) {

			List objectList = new ArrayList();

			boolean mod = ids.size() % 1000 == 0;
			int loop = ids.size() / 1000;
			for (int i = 0; (i + 1) <= (mod ? loop : loop + 1); i++) {

				getSession(false).enableFilter("swissprot");
				Criteria criteria = super.getSession().createCriteria(clazz);
				// utilize partitioning
				DaoUtils.buildFamilyPartitioning(criteria, aliasFamily, decorator);

				int end = (i + 1) * 1000;
				if (end > ids.size())
					end = ids.size();

				criteria.add(Restrictions.in(identifierPropertyName, ids.subList(i * 1000, end)));
				if (eagerProperties != null && eagerProperties.size() > 0) {

					for (String nextProperty : eagerProperties)
						criteria.setFetchMode(nextProperty, FetchMode.JOIN);

					// Make sure the no duplicated parent objects are returned
					criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				}

				if (projections != null) {

					System.out.println("Utilizing projections for entity - smart thinking!");
					criteria.setProjection(projections);

				} else if (StringUtils.hasLength(projectionPropertyName))
					criteria.setProjection(Projections.property(projectionPropertyName));

				List result = criteria.list();
				objectList.addAll(result);
			}
			return objectList;

		} else if (ids != null) {

			getSession(false).enableFilter("swissprot");

			Criteria criteria = super.getSession().createCriteria(clazz);
			// make use of partioning
			DaoUtils.buildFamilyPartitioning(criteria, aliasFamily, decorator);

			criteria.add(Restrictions.in(getSessionFactory().getClassMetadata(clazz).getIdentifierPropertyName(), ids));
			if (eagerProperties != null && eagerProperties.size() > 0) {

				for (String nextProperty : eagerProperties)
					criteria.setFetchMode(nextProperty, FetchMode.JOIN);

				// Make sure the no duplicated parent objects are returned
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			}

			if (projections != null) {

				// Note - aliases are being added in
				// DaoUtils.buildFamilyPartitioning
				System.out.println("Utilizing projections for entities - smart thinking!");
				criteria.setProjection(projections);

			} else if (StringUtils.hasLength(projectionPropertyName))
				criteria.setProjection(Projections.property(projectionPropertyName));

			List result = criteria.list();
			return result;
		}

		return null;
	}

	public Protein getProteinById(Long proteinId) {
		this.getSession(false).enableFilter("swissprot");
		Protein protein = (Protein) super.getSession().get(Protein.class, proteinId);
		return protein;
	}

	public List<IedbAssays> getIedbAssay(Long proteinId, Long iedbId) {
		Criteria criteria = super.getSession().createCriteria(IedbAssays.class);
		criteria.createAlias("iedbEpitope", "iedbEpitope");

		criteria.add(Restrictions.eq("iedbEpitope.iedbId", iedbId));
		if (proteinId != null) {
			criteria.createAlias("iedbEpitope.iedbSwissIndexes", "swissIndex");
			criteria.createAlias("swissIndex.protein", "protein");

			criteria.add(Restrictions.eq("protein.id", proteinId));
		}

		criteria.addOrder(Order.asc("iedbReference"));

		// add result transformer to remove the extra results after join
		// with assays table
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		List<IedbAssays> list = criteria.list();
		return list;
	}

	@SuppressWarnings(value = { "unchecked" })
	public List<IedbEpitope> getIedbSummary(Long proteinId, String sortBy, String sortOrder) {

		Criteria criteria = super.getSession().createCriteria(IedbEpitope.class);
		criteria.createAlias("iedbSwissIndexes", "swissIndex");
		criteria.createAlias("swissIndex.protein", "protein");
		criteria.createAlias("swissIndex.iedbReference", "reference");
		criteria.add(Restrictions.eq("protein.id", proteinId));

		// Add sort
		if (StringUtils.hasText(sortOrder)) {
			if ("desc".equals(sortOrder)) {
				criteria.addOrder(Order.desc(sortBy));
			} else {
				criteria.addOrder(Order.asc(sortBy));
			}
		}
		criteria.addOrder(Order.asc("reference.journalTitle"));
		return criteria.list();
	}

	public Protein findProteinByProteinGi(String proteinGi) {
		Criteria criteria = super.getSession().createCriteria(Protein.class);
		criteria.createAlias("genomicSequence", "genomicSequence");
		criteria.add(Restrictions.isNull("genomicSequence.obsoleteDate"));
		if (proteinGi != null)
			criteria.add(Restrictions.eq("ncbiGI", proteinGi));
		else
			criteria.add(Restrictions.isNull("ncbiGI"));
		return (Protein) criteria.uniqueResult();
	}

	public Organism getOrganismByName(String organismName) {
		Criteria criteria = super.getSession().createCriteria(Organism.class).add(Restrictions.eq("name", organismName));
		Organism o = (Organism) DataAccessUtils.uniqueResult(criteria.list());
		return o;
	}

	@SuppressWarnings(value = { "unchecked" })
	public List<String> getAlleleByCriteria(String[] assayCategories, String[] mhcAlleleSrcSpecies, OrganismType ot) {
		Criteria criteria = super.getSession().createCriteria(IedbAssays.class);
		criteria.add(Restrictions.isNotNull("mhcAllele"));
		criteria.setProjection(Projections.distinct(Property.forName("mhcAllele")));

		if (assayCategories != null && assayCategories.length > 0 && !Utils.checkForAll(CastHelper.asList(assayCategories))) {
			criteria.add(Restrictions.in("assayCategory", assayCategories));
		}
		if (mhcAlleleSrcSpecies != null && mhcAlleleSrcSpecies.length > 0
				&& !Utils.checkForAll(CastHelper.asList(mhcAlleleSrcSpecies))) {
			criteria.add(Restrictions.in("mhcAlleleSrcSpecies", mhcAlleleSrcSpecies));
		}
		criteria.createAlias("iedbEpitope", "iedbEpitope");
		criteria.createAlias("iedbEpitope.iedbSwissIndexes", "swissIndex");
		criteria.createAlias("swissIndex.protein", "protein");
		criteria.createAlias("protein.genomicSequence", "genomicSequence");

		if (ot.equals(OrganismType.FLAVI_DENGUE)) {
			criteria.createAlias("genomicSequence.organism", "organism");
			criteria.add(Restrictions.ilike("organism.name", OrganismType.FLAVI_DENGUE.getSubFilterType().getHierarchyName(),
					MatchMode.START));
		} else if (ot.equals(OrganismType.FLAVI_HCV)) {
			criteria.createAlias("genomicSequence.organism", "organism");
			criteria.add(Restrictions.ilike("organism.name", OrganismType.FLAVI_HCV.getSubFilterType().getHierarchyName(),
					MatchMode.START));
		}

		criteria.add(Restrictions.eq("genomicSequence.family", ot.getFamilyName()));
		CriteriaUtils.sort(criteria, "mhcAllele", Constants.SORT_ASCENDING);

		List<String> ls = criteria.list();
		List<String> alleles = new ArrayList<String>();
		alleles.add(Constants.ALL);
		alleles.addAll(ls);

		return alleles;
	}

	public List<Object[]> getSegmentTypeAndSubTypeForSegmentIdList(List<String> segmentIds) {
		if (segmentIds == null || segmentIds.size() == 0)
			return null;
		Criteria criteria = super.getSession().createCriteria(GenomicSequence.class);
		criteria.createAlias("strain", "strain");
		criteria.createAlias("organism", "organism");
		ProjectionList projs = Projections.projectionList();
		projs.add(Projections.property("segment"));
		projs.add(Projections.property("organism.name"));
		projs.add(Projections.property("strain.subType"));
		projs.add(Projections.property("id"));
		projs.add(Projections.property("badOrGoodAlignment"));
		projs.add(Projections.property("autocurationSubtype"));
		projs.add(Projections.property("editedAlignedSequence"));

		Disjunction idsDis = Restrictions.disjunction();
		for (List<String> chunk : Utils.separateIntoChunks(segmentIds)) {
			idsDis.add(Restrictions.in("id", CastHelper.stringListToListOfLongs(chunk)));
		}
		criteria.add(idsDis);
		criteria.setProjection(projs);
		return criteria.list();
	}

	public GenomicSequence getGenomicSequenceById(Long genomicSequenceId) {
		return (GenomicSequence) super.getSession().get(GenomicSequence.class, genomicSequenceId);
	}

	public CachedDataWrapper getCachedData() {
		return cachedData;
	}

	public void setCachedData(CachedDataWrapper cachedData) {
		this.cachedData = cachedData;
	}

	public List<DrugInformation> getDrugInformationByDrugId(String drugId) {
		Criteria criteria = super.getSession().createCriteria(DrugInformation.class);

		criteria.add(Restrictions.eq("id", drugId));
		return criteria.list();
	}

	/*
	 * Method returns a list of model objects
	 * 
	 * @param modelClass - the class as in Gene.class
	 * 
	 * @param columnName - the field of the class
	 * 
	 * @param value - the where clause for the columnName
	 * 
	 * @param filterObsoleteDates - true, then dont return those object dates
	 * where obsoleteDate has a value
	 * 
	 * @param filterDistinct - true, then get distinct values returns a list of
	 * model objects
	 */
	public List findDataByClassColumnName(Class modelClass, String columnName, Object value, boolean filterObsoleteDates,
			boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery) {

		Criteria criteria = super.getSession().createCriteria(modelClass);

		if (filterObsoleteDates) {
			criteria.add(Restrictions.isNull("obsoleteDate"));
		}

		if (columnName != null)
			criteria.add(Restrictions.eq(columnName, value));

		if (filterDistinct) {
			// select distinct
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		}

		CriteriaUtils.sort(criteria, sortBy, sortOrder);
		criteria.setCacheable(cacheQuery);
		return criteria.list();
	}

	// returns a list of objects by column name
	public List findDataInByClassColumnName(Class modelClass, String columnName, Collection values, boolean filterObsoleteDates,
			boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery) {

		return DaoUtils.findDataInByClassColumnName(super.getSession(), modelClass, columnName, values, filterObsoleteDates,
				filterDistinct, sortBy, sortOrder, cacheQuery);
	}

	/*
	 * Method returns a list of strings, integers, or other object depending on
	 * the column in the database
	 * 
	 * @param modelClass - the class as in Gene.class @param retrieveColumnName
	 * - the column name to return @param columnName - the field of the class
	 * 
	 * @param value - the where clause for the columnName
	 * 
	 * @param filterObsoleteDates - true, then dont return those object dates
	 * where obsoleteDate has a value
	 * 
	 * @param filterDistinct - true, then get distinct
	 */

	public List findData(Class modelClass, List<String> retrieveColumnNames, List<CriteriaOperators> operators,
			boolean filterObsoleteDates, boolean filterDistinct, String sortBy, String sortOrder, boolean cacheQuery,
			CriteriaAliasFetch aliasFetchModes) {
		return DaoUtils.findData(super.getSession(), modelClass, retrieveColumnNames, operators, filterObsoleteDates,
				filterDistinct, sortBy, sortOrder, cacheQuery, aliasFetchModes);
	}

	/*
	 * return a single column of data from a table using an in statement of
	 * values if your passing in ids - then make sure you convert them to a list
	 * of Longs if they are of that type
	 */
	public List findDataInForSingleColumn(Class modelClass, String retrieveColumnName, String columnName, Collection values,
			boolean filterObsoleteDates, boolean filterDistinct, String obsoleteDateColName) {

		Criteria criteria = super.getSession().createCriteria(modelClass);

		if (filterObsoleteDates) {
			if (obsoleteDateColName != null && obsoleteDateColName.contains(Constants.DOT)) {
				int dotPosition = obsoleteDateColName.indexOf(Constants.DOT);
				String alias = obsoleteDateColName.substring(0, dotPosition);
				criteria.createAlias(alias, alias);

			}

			criteria.add(Restrictions.isNull(obsoleteDateColName));
		}

		if (values != null) {
			CriteriaUtils.in(criteria, columnName, CastHelper.collectionToList(values).toArray());
		}

		if (filterDistinct) {
			// select distinct
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		}

		CriteriaUtils.sort(criteria, retrieveColumnName, Constants.SORT_ASCENDING);

		// criteria for returning only the column
		criteria.setProjection(Projections.projectionList().add(Projections.distinct(Projections.property(retrieveColumnName))));

		return criteria.list();
	}

	/*
	 * Method returns a list of strings, integers, or other objects depending on
	 * the column in the database - for queries like:
	 * 
	 * SELECT PDBACCESSION FROM PDBSTRUCTURE_MST WHERE PDBACCESSION IN (SELECT
	 * PDB_ID FROM DRUG_TARGET)
	 * 
	 * to use a different id for join, then pass in an innerId - such as for a
	 * query like: select distinct this_.SubType as y0_ from
	 * BRCWarehouse.ORGANISMSTRAIN this_ where this_.OrgId in (select
	 * this_.orgId as y0_ from BRCWarehouse.Organism this_)
	 * 
	 * 
	 * @param modelClass - the class as in Gene.class @param retrieveColumnName
	 * - the column name to return @param alias - the inner select class name
	 * 
	 * @param columnName - the field of the inner select class name @param
	 * filterObsoleteDates - true, then dont return those object dates where
	 * obsoleteDate has a value @param filterDistinct - true, then get distinct
	 * values returns a list of values from one database column
	 */
	public List findDataForInnerSelect(Class modelClass, String retrieveColumnName, Class innerClass, String innerClassColumnName,
			String innerId, boolean filterObsoleteDates, boolean filterDistinct, String innerColumnCriteria,
			String innerColumnValue) {

		Criteria criteria = super.getSession().createCriteria(modelClass);

		if (filterObsoleteDates)
			criteria.add(Restrictions.isNull("obsoleteDate"));

		DetachedCriteria pdbIds = DetachedCriteria.forClass(innerClass)
				.setProjection(Projections.projectionList().add(Projections.property(innerClassColumnName)));

		if (innerColumnCriteria != null && innerColumnValue != null)
			pdbIds.add(Restrictions.eq(innerColumnCriteria, innerColumnValue));

		if (innerId == null)
			criteria.add(Subqueries.propertyIn(retrieveColumnName, pdbIds));
		else
			criteria.add(Subqueries.propertyIn(innerId, pdbIds));

		// select distinct
		if (filterDistinct)
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		// criteria for returning only the column
		criteria.setProjection(Projections.projectionList().add(Projections.distinct(Projections.property(retrieveColumnName))));
		CriteriaUtils.sort(criteria, retrieveColumnName, Constants.SORT_ASCENDING);

		return criteria.list();
	}

	/*
	 * select this_.sfn_seq_id,this_.ORGSTRAINID as y0_, sv.nonflu_sfvt_id from
	 * BRCWAREHOUSE.SF_SOURCE_STRAIN_INFO this_ ,
	 * brcwarehouse.flu_strain_sf_variants sf, brcwarehouse.nonflu_sf_variants
	 * sv where this_.SFN_SEQ_ID=18570 and this_.SOURCE_TYPE_AND_ID='IEDB:17338'
	 * and sf.orgstrainid=this_.orgstrainid and sf.sfn_seq_id=this_.sfn_seq_id
	 * and sv.sfn_seq_id=this_.sfn_seq_id and
	 * sf.NONFLU_SFVT_SEQ_ID=sv.NONFLU_SFVT_SEQ_ID ;
	 */

	public List getFeatureVariantsForSourceStrain(Long seqFeaturesId, String sourceStrainAndId, Long orgId,
			boolean filterObsoleteDates, String decorator, Long sequenceId) {

		// SF_SOURCE_STRAIN_INFO
		Criteria criteria = super.getSession().createCriteria(SequenceFeatureSourceStrainInfo.class);
		criteria.createAlias("sequenceFeatures", "sequenceFeatures");
		// NONFLU_SF_VARIANTS
		criteria.createAlias("sequenceFeatures.sequenceVariants", "sequenceVariants");

		if (decorator.equals(OrganismType.FLU.getDecoratorName()))
			// FLU_STRAIN_SF_VARIANTS
			criteria.createAlias("sequenceVariants.strainSequenceFeatureVariants", "strainSourceByDecorator");
		else {

			criteria.createAlias("sequenceVariants.proteinSequenceFeatureVariants", "strainSourceByDecorator");
			criteria.add(Restrictions.eq("strainSourceByDecorator.genomeSequenceId", sequenceId));

		}

		criteria.add(Restrictions.eq("sequenceFeatures.id", seqFeaturesId));
		criteria.add(Restrictions.eq("sourceTypeAndId", sourceStrainAndId));
		criteria.add(Restrictions.eq("orgId", orgId));
		criteria.add(Restrictions.eq("strainSourceByDecorator.strain.id", orgId));

		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.property("sequenceVariants.nonFluSFVTId"));
		criteria.setProjection(projections);

		return criteria.list();
	}

	// Given a list of IDs, return the family for each ID
	public List getDecoratorForProteinIds(List<String> ids) {

		Criteria c = super.getSession().createCriteria(Protein.class);
		c.createAlias("genomicSequence", "genomicSequence");
		CriteriaUtils.in(c, "id", CastHelper.stringListToListOfLongs(ids));
		c.add(Restrictions.isNull("genomicSequence.obsoleteDate"));
		ProjectionList projections = Projections.projectionList();
		c.setProjection(projections);
		projections.add(Projections.property("id"));
		projections.add(Projections.property("genomicSequence.family"));
		return c.list();
	}

	// Given a list of IDs, return the family for each ID
	public List getDecoratorForGenomicSequenceIds(List<String> ids) {

		Criteria c = super.getSession().createCriteria(GenomicSequence.class);
		CriteriaUtils.in(c, "id", CastHelper.stringListToListOfLongs(ids));
		c.add(Restrictions.isNull("obsoleteDate"));
		ProjectionList projections = Projections.projectionList();
		c.setProjection(projections);
		projections.add(Projections.property("id"));
		projections.add(Projections.property(BrcConstants.FAMILY_PARTITION));
		return c.list();
	}

	public Protein getProteinByLocusTag(String locusTag) {
		Criteria proteinCriteria = super.getSession().createCriteria(Protein.class);

		proteinCriteria.add(Restrictions.like("locusTag", locusTag, MatchMode.EXACT));

		// Don't include obsolete genes
		Criteria genomicSequenceCriteria = proteinCriteria.createCriteria("genomicSequence");
		genomicSequenceCriteria.add(Restrictions.isNull("obsoleteDate"));

		proteinCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		proteinCriteria.setFetchMode("genomicSequence", FetchMode.JOIN);

		List results = proteinCriteria.list();

		if (results == null || results.size() == 0) {
			return null;
		}

		if (results.size() > 1) {
			throw new RuntimeException("Multiple proteins returned for a ncbiProteinId.  Cannot proceed.");
		}

		return (Protein) results.get(0);
	}

	public Feature getFeatureById(Long featureId) {
		this.getSession(false).enableFilter("swissprot");
		return (Feature) super.getSession().get(Feature.class, featureId);
	}

	public List<String> getNcbiProteinIdsForNcbiAccessions(List<String> ncbiAccessions, String[] proteinOptions,
			String[] proteinNames) {

		Criteria criteria = super.getSession().createCriteria(Protein.class);
		criteria.createAlias("genomicSequence", "sequence");
		CriteriaUtils.in(criteria, "sequence.ncbiAccession", ncbiAccessions);

		// an example of this being used is in the Working Set where you can
		// convert Genomes to Proteins, a user
		// is given checkboxes to indicate specific types of proteins - like it
		// being a mature peptide
		if (proteinOptions != null && proteinOptions.length > 0) {
			Disjunction dis = Restrictions.disjunction();
			for (String option : proteinOptions) {

				if (option.equals(BrcConstants.PROTEIN_OPTION_MAT_PEPTIDE))
					dis.add(Restrictions.eq("matPeptide", Constants.YES_SYMBOL));
				else if (option.equals(BrcConstants.PROTEIN_OPTION_POLYPROTEIN))
					dis.add(Restrictions.eq("polyproteinFlag", 1));

			}
			criteria.add(dis);
		}

		if (proteinNames != null && proteinNames.length > 0 && !CastHelper.arrayContainsString(proteinNames, "All", true)) {
			List<String> newProteinNames = new ArrayList<String>();
			Disjunction dis = Restrictions.disjunction();
			for (String proteinName : proteinNames) {
				if (proteinName.equals("PB1") || proteinName.equals("PA"))
					proteinName += " ";
				dis.add(Restrictions.ilike("swissProtName", proteinName, MatchMode.START));

			}
			criteria.add(dis);
		}

		criteria.add(Restrictions.isNull("sequence.obsoleteDate"));
		criteria.setProjection(Projections.distinct(Projections.property("ncbiId")));

		return criteria.list();
	}

	public List<String> getNcbiAccessionsIds(List<String> ncbiAccessions, String[] proteinOptions, String[] proteinNames,
			String[] segments) {

		Criteria criteria = super.getSession().createCriteria(Protein.class);
		criteria.createAlias("genomicSequence", "sequence");
		CriteriaUtils.in(criteria, "sequence.ncbiAccession", ncbiAccessions);

		// an example of this being used is in the Working Set where you can
		// convert Genomes to Proteins, a user
		// is given checkboxes to indicate specific types of proteins - like it
		// being a mature peptide
		if (proteinOptions != null && proteinOptions.length > 0) {
			Disjunction dis = Restrictions.disjunction();
			for (String option : proteinOptions) {

				if (option.equals(BrcConstants.PROTEIN_OPTION_MAT_PEPTIDE))
					dis.add(Restrictions.eq("matPeptide", Constants.YES_SYMBOL));
				else if (option.equals(BrcConstants.PROTEIN_OPTION_POLYPROTEIN))
					dis.add(Restrictions.eq("polyproteinFlag", 1));

			}
			criteria.add(dis);
		}

		if (proteinNames != null && proteinNames.length > 0 && !CastHelper.arrayContainsString(proteinNames, "All", true)) {
			List<String> newProteinNames = new ArrayList<String>();
			Disjunction dis = Restrictions.disjunction();
			for (String proteinName : proteinNames) {
				if (proteinName.equals("PB1") || proteinName.equals("PA"))
					proteinName += " ";
				dis.add(Restrictions.ilike("swissProtName", proteinName, MatchMode.START));

			}
			criteria.add(dis);
		}

		// BRC-9722: allow user to pick which segment set wants to convert from.
		if (segments != null && segments.length > 0 && !Arrays.asList(segments).contains("all")) {
			criteria.add(Restrictions.in("sequence.segment", segments));
		}

		criteria.add(Restrictions.isNull("sequence.obsoleteDate"));
		criteria.setProjection(Projections.distinct(Projections.property("sequence.ncbiAccession")));

		return criteria.list();
	}

	public List<String> getNcbiAccessionsForNcbiProteinIds(List<String> ncbiProteinIds, String[] segments) {

		Criteria criteria = super.getSession().createCriteria(GenomicSequence.class);
		criteria.createAlias("proteins", "protein");

		CriteriaUtils.in(criteria, "protein.ncbiProteinIdBase", ncbiProteinIds);
		criteria.add(Restrictions.isNull("obsoleteDate"));

		// BRC-9722: allow user to pick which segment set wants to convert from.
		if (segments != null && segments.length > 0 && !Arrays.asList(segments).contains("all"))
			criteria.add(Restrictions.in("segment", segments));

		criteria.setProjection(Projections.distinct(Projections.property("ncbiAccession")));

		return criteria.list();
	}

	/*
	 * returns the count of a field for the model class optional: distinct,
	 * modelField and inValues
	 */
	public Object getCount(Class modelClass, String countField, boolean distinct, String modelField, List inValues) {

		return DaoUtils.getCount(super.getSession(), modelClass, countField, distinct, modelField, inValues);
	}

	/*
	 * returns the sum of a field for the model class optional: modelField and
	 * inValues
	 */
	public Object getSum(Class modelClass, String sumOfField, String modelField, List<String> inValues) {
		return DaoUtils.getSum(super.getSession(), modelClass, sumOfField, modelField, inValues);
	}

	/*
	 * returns list of object[] currently method only support use of one
	 * sqlGroupProjection
	 * 
	 * 
	 * example sql generated with all method fields populated:
	 * 
	 * select this_.FEATURE_TYPE as y0_,
	 * to_char(substr(aa_variant_sequence,1,4000)) as y1, count(*) as y2_ from
	 * BRCWAREHOUSE.STRAIN_FEATURE_VARIANTS this_ where this_.FVN_SEQ_ID=107
	 * group by this_.FEATURE_TYPE, to_char(substr(aa_variant_sequence,1,4000))
	 * 
	 * @param operators - if null, no restrictions will be added
	 * 
	 * Note: results are ordered by first value of groupFields argument if not
	 * sepecified.
	 */
	public List findGroupCount(Class modelClass, List<String> groupFields, String sqlGroupExpression,
			List<CriteriaOperators> operators, String sqlOrderExpression, CriteriaAliasFetch aliasFetchModes) {

		Criteria criteria = super.getSession().createCriteria(modelClass);

		ProjectionList projList = Projections.projectionList();

		// add group fields
		for (String projectionField : groupFields)
			projList.add(Projections.groupProperty(projectionField));

		if (sqlGroupExpression != null) {
			Projection p = Projections.sqlGroupProjection(sqlGroupExpression + " as y1", sqlGroupExpression,
					new String[] { "y1" }, new Type[] { StandardBasicTypes.STRING });
			projList.add(p);
		}

		// add count(*) to select statement
		projList.add(Projections.rowCount(), "groupCount");
		criteria.setProjection(projList);
		// add where statements
		CriteriaRestrictionBuilder.buildRestrictions(criteria, operators);
		if (sqlOrderExpression != null) {
			criteria.addOrder(new NativeSQLOrderNoParens(sqlOrderExpression, true));
		} else {
			String orderByField = groupFields.get(0);
			criteria.addOrder(Order.asc(orderByField));
		}

		CriteriaRestrictionBuilder.addAliases(criteria, aliasFetchModes);
		CriteriaRestrictionBuilder.addFetchModes(criteria, aliasFetchModes);

		return criteria.list();
	}

	public Long findCount(Class modelClass, String countField, boolean distinct, List<CriteriaOperators> operators,
			CriteriaAliasFetch aliasFetchModes) {

		Criteria criteria = super.getSession().createCriteria(modelClass);

		CriteriaRestrictionBuilder.buildRestrictions(criteria, operators);
		CriteriaRestrictionBuilder.addAliases(criteria, aliasFetchModes);
		CriteriaRestrictionBuilder.addFetchModes(criteria, aliasFetchModes);

		if (countField == null)
			criteria.setProjection(Projections.rowCount());
		else {
			if (distinct == true)
				criteria.setProjection(Projections.countDistinct(countField));
			else
				criteria.setProjection(Projections.count(countField));
		}

		Long o = (Long) criteria.uniqueResult();
		if (o == null)
			return Long.parseLong("0");
		else
			return o;
	}

	public List<Object[]> getJalviewSegmentFieldsBySegmentIdList(List<String> segmentIds) {
		Criteria criteria = super.getSession().createCriteria(GenomicSequence.class);
		criteria.createAlias("strain", "strain");
		criteria.createAlias("organism", "organism");
		criteria.createAlias("proteins", "protein");
		ProjectionList projs = Projections.projectionList();
		projs.add(Projections.count("protein.swissProtName"));
		projs.add(Projections.groupProperty("segment"));
		projs.add(Projections.groupProperty("organism.name"));
		// BRC-9945: updated to strain.subType instead of autocuration_subtype
		projs.add(Projections.groupProperty("strain.subType"));
		projs.add(Projections.groupProperty("id"));
		projs.add(Projections.groupProperty("autocurationSubtype"));
		CriteriaUtils.in(criteria, "id", CastHelper.stringListToListOfLongs(segmentIds));
		criteria.setProjection(projs);
		return criteria.list();
	}

	public String getCountForMissingAlignSeq(List<String> segmentIds) {
		Criteria criteria = super.getSession().createCriteria(GenomicSequence.class);
		criteria.add(Restrictions.isNull("alignedSequence"));
		ProjectionList projs = Projections.projectionList();
		projs.add(Projections.count("id"));
		CriteriaUtils.in(criteria, "id", CastHelper.stringListToListOfLongs(segmentIds));
		criteria.setProjection(projs);
		return String.valueOf(criteria.list().get(0));
	}

	/**
	 * Looks up a Strain(TaxonOrg) by its name
	 */
	public List<Strain> getStrainByName(String strainName, String familyName) {

		Criteria criteria = super.getSession().createCriteria(Strain.class);
		criteria.setFetchMode("sequences", FetchMode.JOIN);

		Criteria sequenceCriteria = criteria.createCriteria("sequences");
		sequenceCriteria.add(Restrictions.isNull("obsoleteDate"));

		// Ignore case matching on both fullname or name fields
		criteria.add(Restrictions.or(Restrictions.ilike("name", strainName, MatchMode.EXACT),
				Restrictions.ilike("fullName", strainName, MatchMode.EXACT)));
		if (familyName != null)
			CriteriaUtils.eqString(criteria, BrcConstants.FAMILY_PARTITION, familyName);

		List results = criteria.list();
		if (results == null || results.size() == 0) {
			return null;
		}

		return results;
	}

	/**
	 * find the geneomic sequence by the strain name and family name, family
	 * name is needed for vipr since viruses can have the same strain name used
	 * across virus families
	 * 
	 * @param strainName
	 * @param sortBy
	 * @param sortOrder
	 * @return
	 */
	public List<GenomicSequence> getSequenceListByStrainName(String strainName, String familyName, String sortBy,
			String sortOrder) {
		Criteria criteria = super.getSession().createCriteria(GenomicSequence.class);
		criteria.createAlias("organism", "organism");
		criteria.createAlias("strain", "strain");

		Disjunction strainNameDis = Restrictions.disjunction();
		strainNameDis.add(Restrictions.ilike("strain.fullName", strainName, MatchMode.EXACT));
		strainNameDis.add(Restrictions.ilike("strain.name", strainName, MatchMode.EXACT));
		criteria.add(strainNameDis);

		criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, familyName));

		// Don't include obsolete
		criteria.add(Restrictions.isNull("obsoleteDate"));

		if (StringUtils.hasText(sortBy)) {
			CriteriaUtils.sort(criteria, sortBy, sortOrder);
		}

		List results = criteria.list();
		if (results == null || results.size() == 0) {
			return null;
		}
		return results;
	}

	@SuppressWarnings(value = { "unchecked" })
	public Map<OrganismType, List<String>> getLocations() {
		return DaoUtils.getLocations(super.getSession());
	}

	public List<String> getLocations(OrganismType ot) {
		Map<OrganismType, List<String>> map = getLocations();
		if (map != null) {
			return map.get(ot);
		} else {
			return null;
		}
	}

	public List<String> getContinents(String familyName) {
		return DaoUtils.getContinents(super.getSession(), familyName);
	}

	public List<Object[]> getLocationsWithContients(String familyName) {
		return DaoUtils.getLocationsWithContinent(super.getSession(), familyName);
	}

	public List<Hierarchy> getHierarchy() {
		Map<String, Hierarchy> viprMap = cachedData.getViprHierarcyMap();
		List<Hierarchy> families = new ArrayList<Hierarchy>();

		for (String family : viprMap.keySet()) {
			String famName = viprMap.get(family).getName();
			// Verify that family is in OrganismType - this prevents data that
			// is loaded but shouldn't appear in the UI from appearing
			if (OrganismType.getByFamilyName(famName) != null) {
				families.add(viprMap.get(family));
			}
		}

		Collections.sort(families, new Comparator<Hierarchy>() {

			public int compare(Hierarchy o1, Hierarchy o2) {
				return o1.getName().compareTo(o2.getName());
			}

		});
		return families;
	}

	public Collection getChildrenHierarchy(String id) {
		Collection hierarchy = null;
		for (Hierarchy h : getHierarchy()) {
			hierarchy = getChildrenHierarchy(h, id);
			if (hierarchy != null) {
				break;
			}
		}
		return hierarchy;
	}

	public Object getHierarchy(String id) {
		Object hierarchy = null;
		for (Hierarchy h : getHierarchy()) {
			hierarchy = getHierarchy(h, id);
			if (hierarchy != null) {
				break;
			}
		}
		return hierarchy;
	}

	private Collection getChildrenHierarchy(Hierarchy root, String id) {
		Collection hierarchy = null;

		if (root.getId().equals(id)) {
			hierarchy = root.getChildren();
		} else {
			if (!root.getIsChildStrain()) {
				Iterator<Object> iter = root.getChildren().iterator();
				for (; hierarchy == null && iter.hasNext();) {
					Object oc = iter.next();
					Hierarchy hc = (Hierarchy) oc;
					hierarchy = getChildrenHierarchy(hc, id);
				}
			}
		}
		return hierarchy;
	}

	private Object getHierarchy(Hierarchy root, String id) {
		Object hierarchy = null;
		if (root.getId().equals(id)) {
			hierarchy = root;
		} else {
			if (!root.getIsChildStrain()) {
				Iterator<Object> iter = root.getChildren().iterator();
				for (; hierarchy == null && iter.hasNext();) {
					Object oc = iter.next();
					Hierarchy hc = (Hierarchy) oc;
					hierarchy = getHierarchy(hc, id);
				}
			}
		}
		return hierarchy;
	}

	public HierarchyStrain getHierarchyStrain(String id) {
		HierarchyStrain result = null;
		for (Hierarchy h : getHierarchy()) {
			result = findHierarchyStrain(h, id);
			if (result != null) {
				break;
			}
		}
		return result;
	}

	private HierarchyStrain findHierarchyStrain(Hierarchy root, String id) {
		HierarchyStrain result = null;

		Iterator<Object> iter = root.getChildren().iterator();
		for (; result == null && iter.hasNext();) {
			Object oc = iter.next();
			if (oc instanceof Hierarchy) {
				Hierarchy hc = (Hierarchy) oc;
				result = findHierarchyStrain(hc, id);
			} else { // oc is
				HierarchyStrain hs = (HierarchyStrain) oc;
				if (id.equals(String.valueOf(hs.getId()))) {
					result = hs;
				}
			}
		}
		return result;
	}

	public int getViprStrainsCount(String searchTerm, String decorator) {
		Criteria criteria = super.getSession().createCriteria(Strain.class);
		criteria.createAlias("sequences", "sequences", Criteria.INNER_JOIN);
		criteria.add(Restrictions.ilike("name", searchTerm, MatchMode.ANYWHERE));
		criteria.add(Restrictions.isNull("sequences.obsoleteDate"));
		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType != null && orgType.getApplicationType().equals(ApplicationType.VIPR)) {
			if (orgType.getSubFilterType() != null) {
				String orgAlias = "organism";
				criteria.createAlias("organism", orgAlias, Criteria.INNER_JOIN);
				orgType.addSubFilterCriteria(criteria, orgAlias);
			} else {
				String familyName = OrganismType.getFamilyNameByDecoratorName(decorator);
				criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, familyName));
			}
		} else {
			// If no decorator specified, only get vipr related data
			List<String> supportedViprFamilies = new ArrayList<String>();
			for (OrganismType type : OrganismType.getByApplicationType(ApplicationType.VIPR)) {
				supportedViprFamilies.add(type.getFamilyName());
			}
			CriteriaUtils.in(criteria, BrcConstants.FAMILY_PARTITION, supportedViprFamilies);
		}
		criteria.setProjection(Projections.rowCount());
		// criteria.addOrder(Order.asc("name"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		return ((Long) criteria.list().get(0)).intValue();
	}

	public int getViprSpeciesCount(String searchTerm, String decorator) {
		Criteria criteria = super.getSession().createCriteria(Organism.class);
		criteria.add(Restrictions.or(Restrictions.ilike("species", searchTerm, MatchMode.ANYWHERE),
				Restrictions.ilike("name", searchTerm, MatchMode.ANYWHERE)));
		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType != null && orgType.getApplicationType().equals(ApplicationType.VIPR)) {
			String familyName = OrganismType.getFamilyNameByDecoratorName(decorator);
			criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, familyName));
		} else {
			// If no decorator specified, only get vipr related data
			List<String> supportedViprFamilies = new ArrayList<String>();
			for (OrganismType type : OrganismType.getByApplicationType(ApplicationType.VIPR)) {
				supportedViprFamilies.add(type.getFamilyName());
			}
			CriteriaUtils.in(criteria, BrcConstants.FAMILY_PARTITION, supportedViprFamilies);
		}

		ProjectionList projections = Projections.projectionList();
		projections = Projections.projectionList();
		projections.add(Projections.property("family"));
		projections.add(Projections.property("subFamily"));
		projections.add(Projections.property("genus"));
		projections.add(Projections.property("species"));
		criteria.setProjection(Projections.distinct(projections));
		List list = criteria.list();
		return list.size();
	}

	public int getViprSubfamilyCount(String searchTerm, String decorator) {
		Criteria criteria = super.getSession().createCriteria(Organism.class);
		criteria.add(Restrictions.ilike("subFamily", searchTerm, MatchMode.ANYWHERE));
		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType != null && orgType.getApplicationType().equals(ApplicationType.VIPR)) {
			String familyName = OrganismType.getFamilyNameByDecoratorName(decorator);
			criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, familyName));
		} else {
			// If no decorator specified, only get vipr related data
			List<String> supportedViprFamilies = new ArrayList<String>();
			for (OrganismType type : OrganismType.getByApplicationType(ApplicationType.VIPR)) {
				supportedViprFamilies.add(type.getFamilyName());
			}
			CriteriaUtils.in(criteria, BrcConstants.FAMILY_PARTITION, supportedViprFamilies);
		}

		ProjectionList projections = Projections.projectionList();
		projections = Projections.projectionList();
		projections.add(Projections.property("family"));
		projections.add(Projections.property("subFamily"));
		criteria.setProjection(Projections.distinct(projections));
		List list = criteria.list();
		return list.size();
	}

	public int getViprGenusCount(String searchTerm, String decorator) {
		Criteria criteria = super.getSession().createCriteria(Organism.class);
		criteria.add(Restrictions.ilike("genus", searchTerm, MatchMode.ANYWHERE));
		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType != null && orgType.getApplicationType().equals(ApplicationType.VIPR)) {
			String familyName = OrganismType.getFamilyNameByDecoratorName(decorator);
			criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, familyName));
		} else {
			// If no decorator specified, only get vipr related data
			List<String> supportedViprFamilies = new ArrayList<String>();
			for (OrganismType type : OrganismType.getByApplicationType(ApplicationType.VIPR)) {
				supportedViprFamilies.add(type.getFamilyName());
			}
			CriteriaUtils.in(criteria, BrcConstants.FAMILY_PARTITION, supportedViprFamilies);
		}

		ProjectionList projections = Projections.projectionList();
		projections = Projections.projectionList();
		projections.add(Projections.property("family"));
		projections.add(Projections.property("subFamily"));
		projections.add(Projections.property("genus"));
		criteria.setProjection(Projections.distinct(projections));
		List list = criteria.list();
		return list.size();
	}

	public Collection getViprStrains(String searchTerm, String decorator, Long maxRecord) {
		Criteria criteria = super.getSession().createCriteria(Strain.class);
		criteria.createAlias("sequences", "sequences", Criteria.INNER_JOIN);
		criteria.add(Restrictions.ilike("name", searchTerm, MatchMode.ANYWHERE));
		criteria.add(Restrictions.isNull("sequences.obsoleteDate"));
		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType != null && orgType.getApplicationType().equals(ApplicationType.VIPR)) {
			if (orgType.getSubFilterType() != null) {
				String orgAlias = "organism";
				criteria.createAlias("organism", orgAlias, Criteria.INNER_JOIN);
				orgType.addSubFilterCriteria(criteria, orgAlias);
			} else {
				String familyName = OrganismType.getFamilyNameByDecoratorName(decorator);
				criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, familyName));
			}
		} else {
			// If no decorator specified, only get vipr related data
			List<String> supportedViprFamilies = new ArrayList<String>();
			for (OrganismType type : OrganismType.getByApplicationType(ApplicationType.VIPR)) {
				supportedViprFamilies.add(type.getFamilyName());
			}
			CriteriaUtils.in(criteria, BrcConstants.FAMILY_PARTITION, supportedViprFamilies);
		}
		criteria.addOrder(Order.asc("name"));
		criteria.setProjection(Projections
				.distinct(Projections.projectionList().add(Projections.property("id")).add(Projections.property("name"))));
		if (maxRecord != null) {
			criteria.setMaxResults(maxRecord.intValue());
		}

		List list = criteria.list();
		return list;
	}

	public Collection getViprSpecies(String searchTerm, String decorator, Long maxRecord) {
		Criteria criteria = super.getSession().createCriteria(Organism.class);
		criteria.add(Restrictions.or(Restrictions.ilike("species", searchTerm, MatchMode.ANYWHERE),
				Restrictions.ilike("name", searchTerm, MatchMode.ANYWHERE)));
		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType != null && orgType.getApplicationType().equals(ApplicationType.VIPR)) {
			String familyName = OrganismType.getFamilyNameByDecoratorName(decorator);
			criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, familyName));
		} else {
			// If no decorator specified, only get vipr related data
			List<String> supportedViprFamilies = new ArrayList<String>();
			for (OrganismType type : OrganismType.getByApplicationType(ApplicationType.VIPR)) {
				supportedViprFamilies.add(type.getFamilyName());
			}
			CriteriaUtils.in(criteria, BrcConstants.FAMILY_PARTITION, supportedViprFamilies);
		}
		criteria.addOrder(Order.asc("species"));

		if (maxRecord != null) {
			criteria.setMaxResults(maxRecord.intValue());
		}

		List list = criteria.list();
		return list;
	}

	public Collection getViprGenuses(String searchTerm, String decorator, Long maxRecord) {
		Criteria criteria = super.getSession().createCriteria(Organism.class);
		criteria.add(Restrictions.ilike("genus", searchTerm, MatchMode.ANYWHERE));
		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType != null && orgType.getApplicationType().equals(ApplicationType.VIPR)) {
			String familyName = OrganismType.getFamilyNameByDecoratorName(decorator);
			criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, familyName));
		} else {
			// If no decorator specified, only get vipr related data
			List<String> supportedViprFamilies = new ArrayList<String>();
			for (OrganismType type : OrganismType.getByApplicationType(ApplicationType.VIPR)) {
				supportedViprFamilies.add(type.getFamilyName());
			}
			CriteriaUtils.in(criteria, BrcConstants.FAMILY_PARTITION, supportedViprFamilies);
		}
		criteria.addOrder(Order.asc("genus"));

		if (maxRecord != null) {
			criteria.setMaxResults(maxRecord.intValue());
		}
		ProjectionList projections = Projections.projectionList();
		projections = Projections.projectionList();
		projections.add(Projections.property("family"));
		projections.add(Projections.property("subFamily"));
		projections.add(Projections.property("genus"));
		criteria.setProjection(Projections.distinct(projections));

		List list = CastHelper.removeDuplicates(criteria.list());
		return list;
	}

	public Collection getViprSubfamilys(String searchTerm, String decorator, Long maxRecord) {
		Criteria criteria = super.getSession().createCriteria(Organism.class);
		criteria.add(Restrictions.ilike("subFamily", searchTerm, MatchMode.ANYWHERE));
		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType != null && orgType.getApplicationType().equals(ApplicationType.VIPR)) {
			String familyName = OrganismType.getFamilyNameByDecoratorName(decorator);
			criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, familyName));
		} else {
			// If no decorator specified, only get vipr related data
			List<String> supportedViprFamilies = new ArrayList<String>();
			for (OrganismType type : OrganismType.getByApplicationType(ApplicationType.VIPR)) {
				supportedViprFamilies.add(type.getFamilyName());
			}
			CriteriaUtils.in(criteria, BrcConstants.FAMILY_PARTITION, supportedViprFamilies);
		}
		criteria.addOrder(Order.asc("subFamily"));

		if (maxRecord != null) {
			criteria.setMaxResults(maxRecord.intValue());
		}

		ProjectionList projections = Projections.projectionList();
		projections = Projections.projectionList();
		projections.add(Projections.property("family"));
		projections.add(Projections.property("subFamily"));
		criteria.setProjection(Projections.distinct(projections));

		List list = CastHelper.removeDuplicates(criteria.list());
		return list;
	}

	@Override
	public List<VbrcOrthologGroup> getViprOrthologGroups(String decorator) {
		Criteria criteria = super.getSession().createCriteria(VbrcOrthologGroup.class);
		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType != null && orgType.getApplicationType().equals(ApplicationType.VIPR)) {
			String familyName = OrganismType.getFamilyNameByDecoratorName(decorator);
			criteria.add(Restrictions.eq("familyName", familyName));
			criteria.createCriteria("vbrcAnnotation", "vbrcAnnotation");
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			return criteria.list();
		} else {
			return new ArrayList<VbrcOrthologGroup>();
		}
	}

	@Override
	public VbrcOrthologGroup getViprOrthologGroup(String familyName, Long orthologGroupId) {
		Criteria criteria = super.getSession().createCriteria(VbrcOrthologGroup.class);
		criteria.add(Restrictions.eq("familyName", familyName));
		criteria.add(Restrictions.eq("id.orthologGroupId", new BigDecimal(orthologGroupId)));
		List list = criteria.list();
		if (list.size() > 0) {
			return (VbrcOrthologGroup) list.get(0);
		}
		return null;
	}

	public Protein getMatPeptideById(String id) {
		Criteria proteinCriteria = super.getSession().createCriteria(Protein.class);

		proteinCriteria.add(Restrictions.like("ncbiId", id));

		// Don't include obsolete genes
		Criteria genomicSequenceCriteria = proteinCriteria.createCriteria("genomicSequence");
		genomicSequenceCriteria.add(Restrictions.isNull("obsoleteDate"));

		proteinCriteria.setFetchMode("genomicSequence", FetchMode.JOIN);

		List results = proteinCriteria.list();

		if (results == null || results.size() == 0) {
			return null;
		}

		if (results.size() > 1) {
			throw new RuntimeException("Multiple proteins returned for a mat_peptide id.  Cannot proceed.");
		}

		return (Protein) results.get(0);
	}

	@SuppressWarnings(value = { "unchecked" })
	public List<String> getFOMASubTypes(String host) {
		// We must use upper() here so that the subtypes will appear properly on
		// the UI
		Criteria criteria = super.getSession().createCriteria(FOMA.class);
		criteria.add(Restrictions.isNotNull("subType"));
		criteria.add(Restrictions.eq("host", host));
		criteria.setProjection(Projections.distinct(Projections.property("subType")));
		List<String> results = (List<String>) criteria.list();
		List<String> returnList = new ArrayList<String>();
		for (String result : results) {
			returnList.add(result.toUpperCase());
		}
		return returnList;
	}

	@SuppressWarnings(value = { "unchecked" })
	public List<String> getFOMASegments(String subType, String host) {
		Criteria criteria = super.getSession().createCriteria(FOMA.class);
		criteria.add(Restrictions.isNotNull("segment"));
		criteria.add(Restrictions.eq("subType", subType));
		criteria.add(Restrictions.eq("host", host));
		criteria.setProjection(Projections.distinct(Projections.property("segment")));
		List<Integer> results = (List<Integer>) criteria.list();
		List<String> returnList = new ArrayList<String>();
		for (Integer result : results) {
			returnList.add(result.toString());
		}
		return returnList;
	}

	@SuppressWarnings(value = { "unchecked" })
	public List<String> getFOMAHosts() {
		Criteria criteria = super.getSession().createCriteria(FOMA.class);
		criteria.add(Restrictions.isNotNull("host"));
		criteria.setProjection(Projections.distinct(Projections.property("host")));
		criteria.setCacheable(true);
		return criteria.list();
	}

	@SuppressWarnings(value = { "unchecked" })
	public List<String> getProteinFOMASegments(String subType, String host) {
		Criteria criteria = super.getSession().createCriteria(ProteinFOMA.class);
		criteria.add(Restrictions.isNotNull("segment"));
		criteria.add(Restrictions.eq("subType", subType));
		criteria.add(Restrictions.eq("host", host));
		criteria.setProjection(Projections
				.distinct(Projections.projectionList().add(Projections.property("segment")).add(Projections.property("name"))));
		List<Object[]> results = (List<Object[]>) criteria.list();
		List<String> returnList = new ArrayList<String>();
		for (Object[] result : results) {
			returnList.add(((Integer) result[0]).toString() + " " + (String) result[1]);
		}
		return returnList;
	}

	@SuppressWarnings(value = { "unchecked" })
	public List<String> getFOMAProteinName(String segmentNumber, String subType) {
		Criteria criteria = super.getSession().createCriteria(ProteinFOMA.class);
		criteria.add(Restrictions.eq("segment", new Integer(segmentNumber)));
		criteria.add(Restrictions.eq("subType", subType));

		criteria.setProjection(Projections.distinct(Projections.property("name")));
		criteria.addOrder(Order.asc("name"));
		List<String> results = (List<String>) criteria.list();
		return results;
	}

	/**
	 * Given a list of strain IDs, returns a list of the segments that belong to
	 * those strains, taking into account which segment numbers the user wants
	 */
	public Object getSegmentCountForStrainIdList(List<String> strainIds, String[] displayColumns) {
		return getSegmentCountForStrains(strainIds, false, "strain.id", displayColumns);
	}

	/**
	 * Given a list of strain names, returns a list of the segments that belong
	 * to those strains, taking into account which segment numbers the user
	 * wants
	 */
	public Object getSegmentCountForStrainNamesList(List<String> strainNames, String[] displayColumns) {
		return getSegmentCountForStrains(strainNames, true, "strain.name", displayColumns);
	}

	private Object getSegmentCountForStrains(List<String> identifiers, boolean isStringIdentifier, String idPropertyName,
			String[] displayColumns) {
		// Look at the list of display columns to determine which segments the
		// user wants
		List<String> selectedSegments = new ArrayList<String>();
		if (displayColumns != null) {
			for (String displayCol : displayColumns) {
				if (displayCol.length() > 0) {
					// Look for column names that start with a digit and add to
					// list
					Character firstDigit = displayCol.charAt(0);
					if (Character.isDigit(firstDigit)) {
						selectedSegments.add(firstDigit.toString());
					}
				}
			}
		}

		// Now query to get data, filtering if required by the segment number
		// list
		Criteria criteria = super.getSession().createCriteria(GenomicSequence.class);
		criteria.createAlias("strain", "strain");
		criteria.setProjection(Projections.rowCount());
		if (isStringIdentifier) {
			CriteriaUtils.in(criteria, idPropertyName, identifiers);
		} else {
			CriteriaUtils.in(criteria, idPropertyName, CastHelper.stringListToListOfLongs(identifiers));
		}
		if (selectedSegments != null && selectedSegments.size() > 0) {
			CriteriaUtils.in(criteria, "segment", selectedSegments);
		}
		criteria.add(Restrictions.isNull("obsoleteDate"));
		return criteria.uniqueResult();
	}

	public Map<String, String> getCountryRegionMap() {
		Map<String, String> returnMap = new HashMap<String, String>();
		Session session = super.getSession();
		Criteria criteria = session.createCriteria(ContinentCountryGroup.class);
		criteria.addOrder(Order.asc("country"));
		List<ContinentCountryGroup> results = criteria.list();
		for (ContinentCountryGroup result : results) {
			returnMap.put(result.getCountry(), result.getContinentDetail());
		}
		return returnMap;
	}

	public List<Object[]> getViprFeatureVariantTypeCount(Long proteinId, String family) {

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(SequenceFeatures.class);
		ProjectionList pList = Projections.projectionList();

		pList.add(Projections.groupProperty("orgSpecies"));
		pList.add(Projections.groupProperty("category"));
		pList.add(Projections.rowCount(), SequenceFeatureVariantsSearchDaoImpl.SEQUENCE_FEATURE_COUNT_COLUMN_ALIAS);
		pList.add(Projections.groupProperty("orgGenus"));
		criteria.setProjection(pList);

		criteria.createAlias("sequenceVariants", "sequenceVariants");
		criteria.createAlias("sequenceVariants.proteinSequenceFeatureVariants", "nfpv");
		criteria.createAlias("nfpv.protein", "protein");

		criteria.add(Restrictions.eq("protein.id", proteinId));
		criteria.add(
				Restrictions.not(Restrictions.like("sequenceVariants.nonFluSFVTId", SequenceVariants.VT_UNKNOWN, MatchMode.END)));
		criteria.add(Restrictions.isNull("sequenceVariants.obsoleteDate"));
		criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, family));
		criteria.add(Restrictions.eq("featureGroup", SequenceFeatures.FEATURE_GROUP_SFVT));

		criteria.addOrder(Order.asc("category"));
		List<Object[]> data = criteria.list();

		return data;
	}

	/*
	 * returns a List of TableFields - this represents 7 pieces of data from
	 * PrimerProbe and 1 value from SequenceVariants (the VT ), since
	 * PrimerProbe is not linked to SequenceFeatures, the double query and
	 * repackaging is performed
	 */
	public List findPrimerProbesForSequence(Long sequenceId, String segment, String subType, String orgStrainId) {

		List results = new ArrayList();

		// limit return to SequenceFeature.AssayId,
		// SequenceVariants.nonFluSFVTId

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(SequenceFeatures.class);
		criteria.add(Restrictions.eq("featureGroup", SequenceFeatures.FEATURE_GROUP_PRIMER_PROBE));
		criteria.add(Restrictions.eq("segmentName", segment));
		criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, OrganismType.FLU.getFamilyName()));

		criteria.createAlias("strainSequenceFeatureVariants", "strainSequenceFeatureVariants");
		criteria.add(Restrictions.eq("strainSequenceFeatureVariants.subType", subType));

		criteria.createAlias("strainSequenceFeatureVariants.sequenceVariants", "sequenceVariants");

		criteria.createAlias("strainSequenceFeatureVariants.strain", "strain");
		criteria.add(Restrictions.eq("strain.subType", subType));
		criteria.createAlias("strain.sequences", "sequences");
		criteria.add(Restrictions.eq("sequences.id", sequenceId));

		criteria.add(Restrictions.eq("strain.id", new Long(orgStrainId)));

		criteria.createAlias("sequenceFeatureVariantCount", "sequenceFeatureVariantCount");
		String field = "sequenceFeatureVariantCount." + BrcConstants.FAMILY_PARTITION;
		criteria.add(Restrictions.eq(field, OrganismType.FLU.getFamilyName()));

		ProjectionList projections = Projections.projectionList();
		projections = Projections.projectionList();
		projections.add(Projections.property("assayId"));
		projections.add(Projections.property("sequenceVariants.nonFluSFVTId"));
		criteria.setProjection(projections);

		List<Object[]> fvnList = criteria.list();

		if (fvnList.isEmpty())
			return results;

		List<String> assayidList = new ArrayList<String>();

		for (int i = 0; i < fvnList.size(); i++) {

			Object[] item = fvnList.get(i);
			String assayId = (String) item[0];
			if (assayId != null)
				assayidList.add(assayId);
		}

		if (assayidList.isEmpty())
			return results;

		criteria = session.createCriteria(PrimerProbeMetaData.class);
		criteria.add(Restrictions.in("assayId", assayidList));
		CriteriaUtils.sort(criteria, BrcConstants.FUNC_FEATUREIDENTIFIER_PRIMER_NUMERICVAL, Constants.SORT_ASCENDING);
		results = criteria.list();

		List<TableFields> tblFields = new ArrayList<TableFields>();
		for (Object item : results) {
			TableFields element = new TableFields();
			PrimerProbeMetaData primerData = (PrimerProbeMetaData) item;
			String primerProbeAssayId = primerData.getAssayId();
			element.setField2(primerProbeAssayId);
			// find matching assayId - dont use this as the starting data cause
			// the sort needs to be on AssayId
			for (int i = 0; i < fvnList.size(); i++) {

				Object[] objectArray = fvnList.get(i);
				String sequenceFeatureAssayId = (String) objectArray[0];
				if (primerProbeAssayId.equals(sequenceFeatureAssayId)) {
					String vt = (String) objectArray[1];
					element.setField1(vt);
					break;
				}
			}

			element.setField3(primerData.getLabTestName());
			element.setField4(primerData.getGenomSegmentTargeted());
			element.setField5(primerData.getAmpliconSize());
			element.setField6(primerData.getTestTarget());
			element.setField7(primerData.getPublication());
			element.setField8(primerData.getId());

			tblFields.add(element);

		}

		return tblFields;

	}

	public List<Object[]> getGenomicSequenceAccessionsForProteinIds(List<String> ids) {

		Criteria criteria = super.getSession().createCriteria(Protein.class);
		CriteriaUtils.in(criteria, "id", CastHelper.stringListToListOfLongs(ids));
		criteria.createAlias("genomicSequence", "genomicSequence");
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("genomicSequence.ncbiAccession"));
		criteria.setProjection(projList);
		return criteria.list();

	}

	public List<String> getSequenceFeatureProteinSymbols(OrganismType ot) {

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(SequenceFeatures.class);

		String proteinColumn = "proteinGeneSymbol";
		if (OrganismType.POX.equals(ot)) {
			proteinColumn = "proteinName";
		}
		criteria.setProjection(Projections.distinct(Projections.property(proteinColumn)));

		ot.filterByOrganismType(criteria);
		criteria.add(Restrictions.eq("featureGroup", SequenceFeatures.FEATURE_GROUP_SFVT));
		criteria.add(Restrictions.isNotNull(proteinColumn));
		criteria.addOrder(new LowerCaseOrder(proteinColumn, true));

		List<String> data = criteria.list();

		return data;
	}

	public List<NameValue> getSequenceFeatureDengueTypes() {

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(SequenceFeatures.class);

		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("orgSpecies"));
		projList.add(Projections.property("referenceStrainGBAccession"));
		criteria.setProjection(Projections.distinct(projList));

		criteria.add(Restrictions.eq(BrcConstants.FAMILY_PARTITION, OrganismType.FLAVI_DENGUE.getFamilyName()));
		criteria.add(Restrictions.ilike("orgSpecies", OrganismType.FLAVI_DENGUE.getSubFilterType().getHierarchyName(),
				MatchMode.START));
		criteria.add(Restrictions.eq("featureGroup", SequenceFeatures.FEATURE_GROUP_SFVT));
		criteria.addOrder(new LowerCaseOrder("orgSpecies", true));

		List<Object[]> data = criteria.list();
		List<NameValue> nvList = new ArrayList<NameValue>();
		for (Object[] item : data) {
			String dengueType = (String) item[0];
			String accession = (String) item[1];
			StringBuilder displayValue = new StringBuilder();
			displayValue.append(dengueType);
			displayValue.append(Constants.SPACE);
			displayValue.append(Constants.LEFT_BRACE);
			displayValue.append(accession);
			displayValue.append(Constants.RIGHT_BRACE);
			nvList.add(new NameValue(dengueType, displayValue.toString()));

		}
		return nvList;
	}

	public Object[] getTextIdxColNameAndTableName(String category) {

		Criteria criteria = super.getSession().createCriteria(WHTextIdxMst.class);
		ProjectionList projList = Projections.projectionList();
		projList.add(Projections.property("tiColName"));
		projList.add(Projections.property("tiTableName"));
		criteria.setProjection(projList);
		CriteriaUtils.eqString(criteria, "tiCategory", category);
		CriteriaUtils.eqString(criteria, "tiStatus", "ACTIVE");

		return (Object[]) criteria.list().get(0);

	}

	public List<Object[]> getViprSFVTByProtein(List<Long> pIds, String proteinName, OrganismType ot) {
		Criteria criteria = super.getSession().createCriteria(ProteinSequenceFeatureVariants.class);
		criteria.createAlias("protein", "protein");
		criteria.createAlias("sequenceFeatures", "sf");
		criteria.createAlias("sequenceVariants", "sv");
		// Obsolete with new way!!!
		criteria.add(Restrictions.isNull("sv.obsoleteDate"));
		criteria.add(
				Restrictions.or(Restrictions.eq("sf.featureGroup", "SFVT"), Restrictions.eq("sf.featureGroup", "DEPRECATED")));

		criteria.add(Restrictions.not(Restrictions.like("sv.nonFluSFVTId", SequenceVariants.VT_UNKNOWN, MatchMode.END)));
		if (ot != null) {
			criteria.add(Restrictions.eq("sf.family", ot.getFamilyName()));
			criteria.add(Restrictions.eq("sv.family", ot.getFamilyName()));
		}

		CriteriaUtils.in(criteria, "protein.id", pIds);
		if (StringUtils.hasLength(proteinName)) {
			criteria.add(Restrictions.eq("sf.proteinName", proteinName));
		}
		criteria.add(Restrictions.and(Restrictions.isNotNull("proteinPosition"),
				Restrictions.not(Restrictions.like("proteinPosition", "N/A", MatchMode.ANYWHERE))));

		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.property("protein.id"));
		projections.add(Projections.property("sf.featureIdentifier"));
		projections.add(Projections.property("sf.featureName"));
		projections.add(Projections.property("sf.category"));
		projections.add(Projections.property("proteinPosition"));
		projections.add(Projections.property("sv.nonFluSFVTId"));

		criteria.setProjection(projections);
		return criteria.list();
	}

	/**
	 * Host Factor v2.0 function to retrieve the Bioset List data for the give
	 * bioset metadata ids
	 */
	public List<Object[]> getSvBiosetListColumnsByMatrixUserDefId(String resultMatrixId, String biosetIds) {

		StringBuilder sql = new StringBuilder();

		String dose = "TREATMENT_AMOUNT";
		String time = "TREATMENT_DURATION";
		String strainLine = "SUBJ_STRAIN_NAMES";
		sql.append("select TREATMENT_AGENT, TREATMENT_DURATION, TREATMENT_AMOUNT,");
		sql.append(" sum(abs(EXPRESSION_FLAG_1)), bm_seq_id, SUBJ_STRAIN_NAMES, TREATMENT2");
		sql.append(" from (");
		sql.append("select distinct ");
		sql.append(" bim.TREATMENT_AGENT,");
		sql.append(" bim.").append(time).append(", ");
		sql.append(" bim.").append(dose).append(", sbl.EXPRESSION_FLAG_1, sbl.bm_seq_id").append(", ");
		sql.append(
				" CASE WHEN SUBJ_SPECIE_NAME in (:cellLineHosts) THEN CELL_LINE_NAME ELSE SUBJ_STRAIN_NAMES END as SUBJ_STRAIN_NAMES, ");
		sql.append(" BIOSET_LIST_SEQ_ID, bim.TREATMENT2");
		sql.append(" from brcwarehouse.hf_bioset_list sbl ");
		sql.append(" inner join brcwarehouse.HF_BIOSET_METADATA bm on sbl.BM_SEQ_ID = bm.bm_seq_id");
		sql.append(" inner join brcwarehouse.HF_BIOSET_INFO_MV bim on bm.bm_seq_id = bim.BM_SEQ_ID");
		sql.append(" inner join brcwarehouse.HF_BM_ASSAY_LINK bal on bm.BM_SEQ_ID = bal.BM_SEQ_ID");

		String clause = " WHERE ";
		if (StringUtils.hasLength(resultMatrixId)) {
			sql.append(clause);
			sql.append("sbl.result_matrix_user_def_id = \'");
			sql.append(resultMatrixId);
			sql.append("\'");
			clause = " and ";
		}

		if (StringUtils.hasLength(biosetIds)) {
			sql.append(clause);
			sql.append("sbl.BM_SEQ_ID in (");
			sql.append(biosetIds);
			sql.append(")");
		}
		sql.append(") ");
		sql.append(" group by TREATMENT_AGENT, ").append(time).append(", ").append(dose).append(", bm_seq_id").append(", ")
				.append(strainLine).append(", treatment2");

		Session session = getSession(false);
		Query query = session.createSQLQuery(sql.toString());

		query.setParameterList("cellLineHosts", HostFactorHostType.getCellLineHosts());

		return query.list();

	}

	public List<Object[]> getPatternProbeCounts(String resultMatrixId, List<Long> biosetInfoIds, String searchKeyword) {

		String idString = CastHelper.listToStringNoEmpty(biosetInfoIds, Constants.COMMA);

		// Any change here must have matching changes in HostFactorReagentDao
		// sql
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT measurement_pattern,count(*) Probe_count FROM (");
		hql.append(" SELECT sbl.probe_id, sbl.result_matrix_user_def_id, listagg(sbl.expression_flag_1,' ') WITHIN");
		hql.append(" GROUP ( ORDER BY ");
		hql.append(" NVL (sbi.treat_agent1_name, 'X'), ");
		hql.append(
				" TO_NUMBER ( NVL ( sbi.TREATMENT1_DURATION_HOURS, SUBSTR ( sbi.treat_agent1_dur_val, 1, INSTR (sbi.treat_agent1_dur_val, '/', 1) - 1))), ");
		hql.append(" sbi.TREAT_AGENT1_AMT_VAL, ");
		hql.append(" NVL (s.subj_strain_name, 'X')");
		hql.append(")");
		hql.append(" AS measurement_pattern");
		hql.append(" FROM brcwarehouse.sv_bioset_list sbl, brcwarehouse.sv_bioset_info sbi, sv_subject s");

		if (StringUtils.hasLength(searchKeyword))
			hql.append(", BRCWAREHOUSE.SV_REAGENT_ANNOTATION reg");

		hql.append(" WHERE sbi.bioset_info_seq_id = sbl.bioset_info_seq_id");
		hql.append(" and sbi.subj_user_def_id = s.subj_user_def_id(+)");

		if (StringUtils.hasLength(searchKeyword))
			hql.append(" and sbl.probe_id= reg.PROBE_SPOT_ID ");

		hql.append(" and sbl. RESULT_MATRIX_USER_DEF_ID = \'");
		hql.append(resultMatrixId);
		hql.append("\' and sbl.bioset_info_seq_id in (");
		hql.append(idString);
		hql.append(")");

		String keywordDBValue = null;
		if (StringUtils.hasLength(searchKeyword)) {
			keywordDBValue = "\'" + searchKeyword.toLowerCase() + "\'";
		}

		if (StringUtils.hasLength(searchKeyword)) {

			hql.append(" and(");
			hql.append(" lower(reg.GENE) like "); // 'cxcl10'
			hql.append(keywordDBValue);
			hql.append(" or lower(reg.GENE_NAME) like ");
			hql.append(keywordDBValue);
			hql.append(" or lower(reg.GB_ACC) like ");
			hql.append(keywordDBValue);
			hql.append(" or lower(reg.GENE_SYMBOL) like ");
			hql.append(keywordDBValue);
			hql.append(")");

		}
		hql.append(
				" GROUP BY sbl.probe_id, sbl.result_matrix_user_def_id ) GROUP BY measurement_pattern ORDER BY Probe_count desc");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());
		List retValue = (List) query.list();
		return retValue;

	}

	public List<Object[]> getPatternProbeCountsBm(String resultMatrixId, List<Long> biosetInfoIds, String searchKeyword) {

		String idString = CastHelper.listToStringNoEmpty(biosetInfoIds, Constants.COMMA);

		// Any change here must have matching changes in HostFactorReagentDao
		// sql
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT measurement_pattern,count(*) Probe_count FROM (");
		// pattern group
		sql.append(" SELECT probe_id, result_matrix_user_def_id, listagg(expression_flag_1,' ') WITHIN");
		sql.append(" GROUP ( ORDER BY ");
		sql.append(" NVL (treatment_agent, 'X'), ");
		sql.append(" TREATMENT_DURATION_sort, ");
		sql.append(" treatment_amount_sort, ");
		sql.append(" NVL (subj_strain_names, 'X') ");
		sql.append(")");
		sql.append(" AS measurement_pattern");
		sql.append(" FROM (");
		// inner distinct
		sql.append(" SELECT distinct sbl.probe_id, sbl.result_matrix_user_def_id, sbl.expression_flag_1, ");
		sql.append(" bim.treatment_agent, bim.TREATMENT_DURATION_sort, bim.treatment_amount_sort, bim.subj_strain_names ");
		sql.append(" FROM brcwarehouse.hf_bioset_list sbl ");
		sql.append(" inner join brcwarehouse.hf_bioset_metadata hbm on sbl.BM_SEQ_ID = hbm.BM_SEQ_ID ");
		sql.append(" inner join BRCWAREHOUSE.HF_BIOSET_INFO_MV bim on hbm.BM_SEQ_ID=bim.BM_SEQ_ID ");

		if (StringUtils.hasLength(searchKeyword))
			sql.append("left join BRCWAREHOUSE.SV_REAGENT_ANNOTATION reg on sbl.probe_id = reg.probe_spot_id");
		sql.append(" WHERE sbl.RESULT_MATRIX_USER_DEF_ID = :resultMatrixId");
		sql.append(" and lower(bim.treatment_agent) not in (:mockList)");
		if (biosetInfoIds != null && biosetInfoIds.size() > 0)
			sql.append(" and hbm.bm_seq_id in (:biosetIds) and sbl.bm_seq_id in (:biosetIds)");

		if (StringUtils.hasLength(searchKeyword)) {
			sql.append(" and sbl.probe_id= reg.PROBE_SPOT_ID ");
			sql.append(" and(");
			sql.append(" lower(reg.GENE) like :searchKeyword"); // 'cxcl10'
			sql.append(" or lower(reg.GENE_NAME) like :searchKeyword");
			sql.append(" or lower(reg.GB_ACC) like :searchKeyword");
			sql.append(" or lower(reg.GENE_SYMBOL) like :searchKeyword)");

		}
		// end inner select distinct
		sql.append(") GROUP BY probe_id, result_matrix_user_def_id");
		// end pattern group
		sql.append(") GROUP BY measurement_pattern ORDER BY Probe_count desc");

		Session session = getSession(false);
		Query query = session.createSQLQuery(sql.toString());
		query.setString("resultMatrixId", resultMatrixId);
		query.setParameterList("mockList", Arrays
				.asList(new String[] { Constants.HF_MOCK_INFECTION.toLowerCase(), Constants.HF_MOCK_STIMULATION.toLowerCase() }));

		if (biosetInfoIds != null && biosetInfoIds.size() > 0)
			query.setParameterList("biosetIds", biosetInfoIds);
		if (StringUtils.hasLength(searchKeyword)) {
			query.setString("searchKeyword", searchKeyword.toLowerCase());
		}

		List retValue = (List) query.list();
		return retValue;

	}

	// Vipr human clinical metadata
	public List<String> findCountriesWithHumanClinicalMetadata(String orgFamily) {

		Criteria criteria = super.getSession().createCriteria(StudySample.class);
		criteria.createAlias("host", "host");
		criteria.createAlias("host.species", "species", CriteriaSpecification.LEFT_JOIN);

		criteria.add(Restrictions.eq("host.family", orgFamily));
		criteria.add(Restrictions.eq("species.category", "H"));
		if (orgFamily.equalsIgnoreCase("Flaviviridae"))
			criteria.add(Restrictions.eq("host.organismName", "Dengue virus"));
		criteria.setProjection(Projections.distinct(Projections.property("collectionCountry")));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.add(Restrictions.isNotNull("collectionCountry"));
		criteria.addOrder(Order.asc("collectionCountry"));
		return criteria.list();
	}

	public List<String> findHostSpeciesWithHumanClinicalMetadata(String orgFamily) {

		Criteria criteria = super.getSession().createCriteria(Species.class);
		criteria.createAlias("hosts", "host");

		criteria.add(Restrictions.eq("host.family", orgFamily));
		if (orgFamily.equalsIgnoreCase("Flaviviridae"))
			criteria.add(Restrictions.eq("host.organismName", "Dengue virus"));
		criteria.setProjection(Projections.distinct(Projections.property("scientificName")));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.add(Restrictions.isNotNull("scientificName"));
		criteria.addOrder(Order.asc("scientificName"));
		return criteria.list();
	}

	public List<String> findDataSourcesWithHumanClinicalMetadata(String orgFamily) {

		Criteria criteria = super.getSession().createCriteria(StudySample.class);
		criteria.createAlias("host", "host");
		criteria.createAlias("host.species", "species", CriteriaSpecification.LEFT_JOIN);
		criteria.createAlias("dataSource", "dataSource", CriteriaSpecification.LEFT_JOIN);

		criteria.add(Restrictions.eq("host.family", orgFamily));
		criteria.add(Restrictions.eq("species.category", "H"));
		criteria.setProjection(Projections.distinct(Projections.property("dataSource.sourceName")));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.add(Restrictions.isNotNull("dataSource.sourceName"));
		criteria.addOrder(Order.asc("dataSource.sourceName"));
		return criteria.list();
	}

	public List<String> findCollectorsWithHumanClinicalMetadata(String orgFamily) {

		Criteria criteria = super.getSession().createCriteria(StudySample.class);
		criteria.createAlias("host", "host");
		criteria.createAlias("host.species", "species", CriteriaSpecification.LEFT_JOIN);
		criteria.add(Restrictions.eq("host.family", orgFamily));
		criteria.add(Restrictions.eq("species.category", "H"));
		criteria.setProjection(Projections.distinct(Projections.property("institutionName")));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.add(Restrictions.isNotNull("institutionName"));
		criteria.addOrder(Order.asc("institutionName"));
		return criteria.list();
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

		// there are problems with this - for Phenotype Human Surveillance,
		// download of all records, some update queries seem
		// to exist out there - no idea where they are coming from or how they
		// are even associated with this session - but they are getting invoked
		// on flush:
		// update CEIRS.HUMAN_SURVEILLANCE_DATA_V set HOST_AGE=16,
		// HOST_AGE_UNIT='Y', FULL_STRAIN_NAME='A/Alaska/3149/2012(H3N2)',
		// HOST_SEX='F', SEROTYPE='H3N2', SPECIES_CATEGORY='H',
		// STRAIN_NAME='A/Alaska/3149/2012', VACCINATION_DATE=2012-09-24,
		// VACCINATION_STATUS='Yes' where SAMPLE_SEQ_ID=1632299 2: update
		// CEIRS.HUMAN_SURVEILLANCE_DATA_V set HOST_AGE=16, HOST_AGE_UNIT='Y',
		// FULL_STRAIN_NAME='A/Alaska/3149/2012(H3N2)', HOST_SEX='F',
		// SEROTYPE='H3N2', SPECIES_CATEGORY='H',
		// STRAIN_NAME='A/Alaska/3149/2012', VACCINATION_DATE=2012-09-24,
		// VACCINATION_STATUS='Yes' where SAMPLE_SEQ_ID=1632300
		// [10:23:32][WARN] SQL Error: 1733, SQLState: 42000
		// [10:23:32][ERROR] ORA-01733: virtual column not allowed here
		try {
			super.getSession().flush();
			super.getSession().clear();
		} catch (Exception e) {
			System.err.println("Errors attempting to flush and clear the session: " + e);
		}
	}

	public List<String> getGenusTypesByDecorator(String decorator) {

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(Organism.class);
		criteria.createAlias("sequences", "sequences");
		criteria.setProjection(Projections.distinct(Projections.property("genus")));

		criteria.add(Restrictions.eq("sequences.family", OrganismType.getFamilyNameByDecoratorName(decorator)));

		OrganismType orgType = OrganismType.getByDecoratorName(decorator);

		if (orgType != null && orgType.getSubFilterType() != null) {
			orgType.addSubFilterCriteria(criteria, "this");
		}

		criteria.add(Restrictions.isNull("sequences.obsoleteDate"));
		criteria.add(Restrictions.isNotNull("genus"));

		criteria.addOrder(new LowerCaseOrder("genus", true));
		List<String> data = criteria.list();

		return data;
	}

	public List<Object[]> getSpeciesByGenus(String genus, String decorator) {

		Session session = super.getSession();
		Criteria criteria = session.createCriteria(Organism.class);
		criteria.createAlias("sequences", "sequences");
		criteria.setProjection(Projections.projectionList().add(Projections.distinct(Projections.property("id")))
				.add(Projections.property("name")));
		criteria.add(Restrictions.eq("sequences.family", OrganismType.getFamilyNameByDecoratorName(decorator)));

		OrganismType orgType = OrganismType.getByDecoratorName(decorator);
		if (orgType.getSubFilterType() != null) {
			orgType.addSubFilterCriteria(criteria, "this");
		}
		criteria.add(Restrictions.eq("genus", genus));
		criteria.add(Restrictions.isNull("sequences.obsoleteDate"));
		criteria.addOrder(new LowerCaseOrder("name", true));
		List<Object[]> data = criteria.list();

		return data;
	}

	public List<Object[]> getProteinSuggestions(String orgId, String taxonName, String hcvSubtype, String decorator,
			String strainName, String segment, String term) {
		Session session = super.getSession();
		Criteria criteria = session.createCriteria(Protein.class);

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("ncbiGeneSymbol"));
		retrieveColumn.add(Projections.property("swissProtName"));
		retrieveColumn.add(Projections.property("spProtName2"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.createAlias("genomicSequence", "sequences");
		criteria.createAlias("strain", "strain");

		criteria.add(Restrictions.eq("sequences.organism.id", Long.valueOf(orgId)));

		criteria.add(Restrictions.eq("sequences.family", OrganismType.getFamilyNameByDecoratorName(decorator)));

		if (StringUtils.hasLength(strainName)) {
			criteria.add(Restrictions.eq("strain.fullName", strainName));
		}
		if (StringUtils.hasLength(segment)) {
			criteria.add(Restrictions.eq("sequences.segment", segment));
		}
		if (OrganismType.FLAVI_DENGUE.getDecoratorName().equals(decorator)) {

			criteria.createAlias("sequences.taxon", "taxon");
			criteria.add(Restrictions.eq("taxon.name", taxonName));
		}
		// HCV check for subtype
		else if (OrganismType.FLAVI_HCV.getDecoratorName().equals(decorator)) {
			criteria.add(Restrictions.eq("strain.subType", hcvSubtype));
		}
		Disjunction ors = Restrictions.disjunction();
		ors.add(Restrictions.ilike("ncbiGeneSymbol", term, MatchMode.ANYWHERE));
		ors.add(Restrictions.ilike("swissProtName", term, MatchMode.ANYWHERE));
		ors.add(Restrictions.ilike("spProtName2", term, MatchMode.ANYWHERE));
		criteria.add(ors);

		criteria.addOrder(Order.asc("ncbiGeneSymbol"));
		criteria.addOrder(Order.asc("swissProtName"));
		criteria.addOrder(Order.asc("spProtName2"));

		return criteria.list();
	}

	public List<String> getProteinGeneSymbols(String decorator, List<Long> genomicSequenceIds) {
		Session session = super.getSession();
		Criteria criteria = session.createCriteria(Protein.class);

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("ncbiGeneSymbol"));
		// retrieveColumn.add(Projections.property("swissProtName"));
		// retrieveColumn.add(Projections.property("spProtName2"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.createAlias("genomicSequence", "sequences");

		criteria.add(Restrictions.eq("sequences.family", OrganismType.getFamilyNameByDecoratorName(decorator)));

		criteria.add(Restrictions.in("sequences.id", genomicSequenceIds));

		return criteria.list();
	}

	public List<String> getAccessionSuggestions(String orgId, String taxonName, String subType, String decorator,
			String strainName, String segment, String protein, String term) {

		OrganismType ot = OrganismType.getByDecoratorName(decorator);
		Session session = super.getSession();
		Criteria criteria = session.createCriteria(Protein.class);

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("ncbiProteinIdBase"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.createAlias("genomicSequence", "sequences");
		criteria.createAlias("strain", "strain");

		if (OrganismType.FLU.equals(ot)) {
			if (StringUtils.hasText(subType) && "Influenza A Virus".equals(orgId)) {
				criteria.add(Restrictions.like("strain.subType", subType, MatchMode.ANYWHERE));
			}
			criteria.add(Restrictions.eq("sequences.orgName", orgId));
		} else {
			criteria.add(Restrictions.eq("sequences.organism.id", Long.valueOf(orgId)));
			if (OrganismType.FLAVI_DENGUE.getDecoratorName().equals(decorator)) {

				criteria.createAlias("sequences.taxon", "taxon");
				criteria.add(Restrictions.eq("taxon.name", taxonName));
			}
			// HCV check for subtype
			else if (OrganismType.FLAVI_HCV.getDecoratorName().equals(decorator)) {
				criteria.add(Restrictions.eq("strain.subType", subType));
			}

		}

		criteria.add(Restrictions.eq("sequences.family", OrganismType.getFamilyNameByDecoratorName(decorator)));
		if (StringUtils.hasLength(strainName)) {
			criteria.add(Restrictions.like("strain.fullName", strainName, MatchMode.ANYWHERE));
		}
		if (StringUtils.hasLength(segment)) {
			criteria.add(Restrictions.eq("sequences.segment", segment));
		}

		if (StringUtils.hasLength(protein)) {
			if (OrganismType.FLU.equals(ot)) {
				criteria.add(Restrictions.like("swissProtName", protein, MatchMode.START));
			} else {
				Disjunction ors = Restrictions.disjunction();
				ors.add(Restrictions.ilike("ncbiGeneSymbol", protein));
				ors.add(Restrictions.ilike("swissProtName", protein));
				ors.add(Restrictions.ilike("spProtName2", protein));
				criteria.add(ors);
			}
		}

		criteria.addOrder(Order.asc("ncbiProteinIdBase"));
		return criteria.list();
	}

	public List getOne(Class aClass) {

		DetachedCriteria query = DetachedCriteria.forClass(aClass);
		List results = query.getExecutableCriteria(super.getSession()).setMaxResults(1).list();
		return results;
	}

	public List<PDBRecord> findIdenticalAASequencePdbRecords(Long proteinId, Long uniqueAASequenceId, Integer size) {
		if (proteinId == null || uniqueAASequenceId == null) {
			return null;
		}
		Criteria criteria = super.getSession().createCriteria(PDBRecord.class);
		criteria.createAlias("structure", "structure");
		criteria.createAlias("protein", "protein");
		criteria.createAlias("protein.genomicSequence", "genomicSequence");
		criteria.createAlias("protein.sequenceSet", "AASequence");

		criteria.add(Restrictions.eq("AASequence.uniqueAASequenceid", uniqueAASequenceId));
		criteria.add(Restrictions.ne("protein.id", proteinId));
		criteria.add(Restrictions.isNull("genomicSequence.obsoleteDate"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		if (size != null && size > 0) {
			criteria.setFirstResult(0);
			criteria.setMaxResults(size);
		}

		List<PDBRecord> results = criteria.list();
		return results;

	}

	public List<String> getDengueVirusSubtypesForSegmentIds(List<Long> segmentIds) {

		// Special Treatment for Dengue
		Criteria criteria = super.getSession().createCriteria(GenomicSequence.class);
		criteria.createAlias("taxon", "taxon", Criteria.INNER_JOIN);
		criteria.createAlias("organism", "organism");
		criteria.add(Restrictions.like("organism.name", SubFilterType.DENGUE.getHierarchyName()));
		criteria.add(Restrictions.eq("organism.genus", "Flavivirus"));
		CriteriaUtils.in(criteria, "id", segmentIds);
		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.distinct(Projections.property("taxon.name")));
		criteria.setProjection(projections);

		return criteria.list();
	}

	public List<String> getDengueVirusSubtypesForProteinIds(List<Long> proteinIds) {

		// Special Treatment for Dengue
		Criteria criteria = super.getSession().createCriteria(Protein.class);
		criteria.createAlias("genomicSequence", "sequence");
		criteria.createAlias("sequence.taxon", "taxon", Criteria.INNER_JOIN);
		criteria.createAlias("organism", "organism");
		criteria.add(Restrictions.like("organism.name", SubFilterType.DENGUE.getHierarchyName()));
		criteria.add(Restrictions.eq("organism.genus", "Flavivirus"));
		CriteriaUtils.in(criteria, "id", proteinIds);

		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.distinct(Projections.property("taxon.name")));
		criteria.setProjection(projections);

		return criteria.list();
	}

	public List<String> getHCVVirusSubtypesForSegmentIds(List<Long> segmentIds) {

		Criteria criteria = super.getSession().createCriteria(GenomicSequence.class);
		criteria.createAlias("organism", "organism");
		criteria.createAlias("strain", "strain", Criteria.INNER_JOIN);
		criteria.add(Restrictions.like("organism.name", SubFilterType.HCV.getHierarchyName()));
		criteria.add(Restrictions.eq("organism.genus", "Hepacivirus"));
		CriteriaUtils.in(criteria, "id", segmentIds);

		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.distinct(Projections.property("subType")));
		criteria.setProjection(projections);

		return criteria.list();
	}

	public List<String> getHCVVirusSubtypesForProteinIds(List<Long> proteinIds) {

		Criteria criteria = super.getSession().createCriteria(Protein.class);
		criteria.createAlias("organism", "organism");
		criteria.createAlias("strain", "strain", Criteria.INNER_JOIN);
		criteria.add(Restrictions.like("organism.name", SubFilterType.HCV.getHierarchyName()));
		criteria.add(Restrictions.eq("organism.genus", "Hepacivirus"));
		CriteriaUtils.in(criteria, "id", proteinIds);

		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.distinct(Projections.property("strain.subType")));
		criteria.setProjection(projections);

		return criteria.list();
	}

	public List<String> getPoxOrthologIdsForProteinIds(List<Long> proteinIds) {

		Criteria criteria = super.getSession().createCriteria(Protein.class);
		criteria.createAlias("organism", "organism");
		criteria.createAlias("whOrthologs", "whOrthologs");
		criteria.createAlias("whOrthologs.whOrthologMaster", "whOrthologMaster");
		criteria.add(Restrictions.like("organism.family", OrganismType.POX.getFamilyName()));

		CriteriaUtils.in(criteria, "id", proteinIds);

		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.distinct(Projections.property("whOrthologMaster.id")));
		criteria.setProjection(projections);

		return criteria.list();
	}

	public List<String> getInfluenzaProteinSymbolsForProteinIds(List<Long> proteinIds) {

		Criteria criteria = super.getSession().createCriteria(Protein.class);
		criteria.createAlias("organism", "organism");
		criteria.add(Restrictions.like("organism.family", OrganismType.FLU.getFamilyName()));
		CriteriaUtils.in(criteria, "id", proteinIds);

		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.distinct(Projections.property("swissProtName")));
		criteria.setProjection(projections);

		return criteria.list();
	}

	public List<IedbAssays> getIedbAssaysByEpitopeIds(List<Long> iedbIds) {
		Criteria criteria = super.getSession().createCriteria(IedbAssays.class);
		criteria.createAlias("iedbEpitope", "iedbEpitope");

		criteria.add(Restrictions.in("iedbEpitope.iedbId", iedbIds));
		List<IedbAssays> list = criteria.list();
		return list;
	}

	/**
	 * This method is created to provide better performance on Host Factor
	 * keyword search (BRC-10854). This will return a list of bioset metadata id
	 * that has the matching reagent metadata.
	 */
	public List<Long> getBiosetMetatdataIds(Map userInputs) {
		String[] experimentType = Utils.getArrayValueFromInputs(userInputs, HostFactorFormFields.FORM_FIELD_EXPERIMENT_TYPE);
		String keywordSearch = (String) userInputs.get(HostFactorFormFields.FORM_FIELD_KEYWORD_SEARCH);
		if (!StringUtils.hasLength(keywordSearch) || keywordSearch.contains("keyword"))
			return null;
		Criteria subquery = getSession().createCriteria(BiosetList.class, "svBiosetList")
				.createAlias("svBiosetList.reagentAnnotation", "reagentAnnotation")
				.setProjection(Projections.distinct(Projections.property("svBiosetList.biosetMetadata.id")));
		List<NameValue> subPropertyValues = new ArrayList<NameValue>();
		// determine which columns to filter with based on the
		// experiment/analyte type
		HostFactorExperimentsSearchDao.buildKeywordCriteria(experimentType, subPropertyValues);

		Disjunction ors2 = KeywordProcessor.processKeywordIntoDisjunction(keywordSearch, subPropertyValues);
		subquery.add(ors2);

		return subquery.list();
	}

	/*
	 * this method uses the dao criteria to refine the query
	 */
	public Map searchPanelExperiments(String criteriaPath, Class modelClass, List<String> groupFields, String sqlGroupExpression,
			Map userInputs, CriteriaAliasFetch aliasFetchModes) {

		Criteria criteria = super.getSession().createCriteria(modelClass);

		if (criteriaPath.equals(SearchDaoMapper.HostFactorExperiments.getSearchKey()))
			HostFactorExperimentsSearchDao.buildQueryCriteria(criteria, userInputs);
		else if (criteriaPath.equals(SearchDaoMapper.HostFactorBiosets.getSearchKey()))
			HostFactorBiosetsSearchDao.buildQueryCriteria(criteria, userInputs);
		else if (criteriaPath.equals(SearchDaoMapper.HostFactorPatterns.getSearchKey()))
			HostFactorPatternsSearchDao.buildQueryCriteria(criteria, userInputs);

		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.distinct(Projections.property("id")));

		if (criteriaPath.equals(SearchDaoMapper.PlasmidData.getSearchKey())) {
			String[] virusSpecie = Utils.getArrayValueFromInputs(userInputs, PlasmidFormFields.FORM_FIELD_VIRUS_SPECIES);
			CriteriaUtils.addOrs(criteria, virusSpecie, "virusSpecies2", false, false);
			String[] geneProrein = Utils.getArrayValueFromInputs(userInputs, PlasmidFormFields.FORM_FIELD_GENE_PROTEIN_CATEGORY);
			CriteriaUtils.addOrs(criteria, geneProrein, "geneProteinCategory", false, false);
		}

		boolean skipNullFamily = false;
		for (String field : groupFields) {
			projections.add(Projections.property(field));
			if (field != null && field.equalsIgnoreCase(HostFactorModelFields.EXPERIMENT_VIRUS_FAMILY))
				skipNullFamily = true;
		}
		criteria.setProjection(projections);

		CriteriaRestrictionBuilder.addAliases(criteria, aliasFetchModes);
		CriteriaRestrictionBuilder.addFetchModes(criteria, aliasFetchModes);

		if (skipNullFamily)
			criteria.add(Restrictions.isNotNull(HostFactorModelFields.EXPERIMENT_VIRUS_FAMILY));

		List<Object[]> data = criteria.list();
		// dont know how to write a select projection over another projection,
		// so using a map to filter the data and build the count
		Map<Object, Integer> fieldCount = new HashMap<Object, Integer>();
		for (Object[] item : data) {

			Object groupField = item[1];

			if (fieldCount.containsKey(groupField)) {

				Integer currentCount = fieldCount.get(groupField);
				int updatedCount = currentCount.intValue() + 1;
				fieldCount.put(groupField, new Integer(updatedCount));

			} else
				fieldCount.put(groupField, new Integer(1));
		}

		return fieldCount;
	}

	/*
	 * this method uses the dao criteria to refine the query
	 */
	public Map searchPanelHostFactorCount(String criteriaPath, Class modelClass, List<String> groupFields,
			String sqlGroupExpression, Map userInputs, CriteriaAliasFetch aliasFetchModes) {

		Criteria criteria = super.getSession().createCriteria(modelClass);

		if (criteriaPath.equals(SearchDaoMapper.HostFactorExperiments.getSearchKey()))
			HostFactorExperimentsSearchDao.buildQueryCriteria(criteria, userInputs);
		else if (criteriaPath.equals(SearchDaoMapper.HostFactorBiosets.getSearchKey()))
			HostFactorBiosetsSearchDao.buildQueryCriteria(criteria, userInputs);
		else if (criteriaPath.equals(SearchDaoMapper.HostFactorPatterns.getSearchKey()))
			HostFactorPatternsSearchDao.buildQueryCriteria(criteria, userInputs);

		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.distinct(Projections.property("id")));

		for (String field : groupFields)
			projections.add(Projections.property(field));

		criteria.setProjection(projections);

		CriteriaRestrictionBuilder.addAliases(criteria, aliasFetchModes);
		CriteriaRestrictionBuilder.addFetchModes(criteria, aliasFetchModes);

		List<Object[]> data = criteria.list();
		// dont know how to write a select projection over another projection,
		// so using a map to filter the data and build the count
		Map<String, Integer> fieldCount = new HashMap<String, Integer>();
		for (Object[] item : data) {

			// Bioset Name
			String groupField = (String) item[1];
			// Probe Count (svProbeCounts.probeCount)
			Long probeCount = (Long) item[2];
			Integer hostFactorCount = new Integer(probeCount.intValue());

			fieldCount.put(groupField, hostFactorCount);
		}

		return fieldCount;
	}

	public List<Object[]> findExperimentBiosetData(Long expId) {
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT distinct");
		hql.append(" b.bmUserDefId,"); // 0
		hql.append(" p.probeCount,"); // 1
		hql.append(" bi.treatmentAgent,"); // 2
		hql.append(" bi.treatmentAmount,"); // 3
		hql.append(" bi.treatmentDuration,"); // 4
		hql.append(" bi.subjectStrainNames,");// 5
		hql.append(" bi.subjectSpeciesName,");// 6
		hql.append(" b.resultMatrixUserDefId,");// 7
		hql.append(" b.id,");// 8
		hql.append(" bi.cellLineName");// 9
		hql.append(" FROM BiosetMetadata b");
		hql.append(" left outer join b.assays a");
		hql.append(" left outer join a.experiment e");
		hql.append(" left outer join e.dpeAssayTreatmentLinks l");
		hql.append(" left outer join b.probeCounts p");
		hql.append(" left outer join b.biosetInfos bi");
		hql.append(" where e.id = :expSeqId");
		hql.append(" and lower(l.treatmentAgentName) not in (:mockList)");
		hql.append(" and a.id = l.assay.id");
		hql.append(" order by b.bmUserDefId asc");

		Query query = this.getSessionFactory().getCurrentSession().createQuery(hql.toString());
		query.setLong("expSeqId", expId);
		query.setParameterList("mockList", Arrays
				.asList(new String[] { Constants.HF_MOCK_INFECTION.toLowerCase(), Constants.HF_MOCK_STIMULATION.toLowerCase() }));

		return (List) query.list();
	}

	public List<Object[]> findExperimentSampleData(Long expId) {

		// using decode to force mock values to the bottom of the sort
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM (");
		sql.append("select distinct ha.ASSY_NAME, "); // 0
		sql.append(" hbins.SUBJ_SPECIE_NAME, hbins.SUBJ_STRAIN_NAMES, hbins.SUBJ_STRAIN_CHARCTR,"); // 1,2,3
		sql.append(" hbins.SUBJ_GENDER, hbinsa.SUBJ_AGE, hbi.SAMPL_TYPE as SAMPLE_SOURCE,"); // 4,5,6
		sql.append(" decode(instr(lower(ha.ASSY_NAME),'mock',1),0,datl.TREATMENT_AGENT_NAME,null) TREATMENT_NAME,"); // 7
		sql.append(" decode(instr(lower(ha.ASSY_NAME),'mock',1),0,bim.TREATMENT_AMOUNT,null) TREATMENT_AMOUNT,"); // 8
		sql.append(" hbo.SAMPL_TYPE, ha.ext_rep_id, bim.TREATMENT_TYPE,"); // 9,10,11
		sql.append(" decode(instr(lower(ha.ASSY_NAME),'mock',1),0,bim.TREATMENT_DURATION,null) TREATMENT_DURATION,"); // 12
		sql.append(" hbins.CELL_LINE_NAME, hbins.CELL_TYPE, hbins.CELL_LINE_CHARCTR, eru.EXT_URL,"); // 13,14,15,16
		sql.append(" bim.TREATMENT_AMOUNT_SORT, bim.TREATMENT_DURATION_SORT, hbinsa.SUBJ_AGE_UOM"); // 17,18,19
		sql.append(" from brcwarehouse.HF_ASSAY ha");
		sql.append(" left outer join BRCWAREHOUSE.HF_EXT_REPOSITORY_URL eru on ha.ASSY_SEQ_ID = eru.ASSAY_SEQ_ID");
		sql.append(" left outer join BRCWAREHOUSE.HF_BIOSAMPLE hbo on ha.SAMPL_SEQ_ID = hbo.SAMPL_SEQ_ID");
		sql.append(" left outer join BRCWAREHOUSE.HF_SAMPLING_EVENT hbout on hbo.SAMPL_SEQ_ID = hbout.SAMPL_SEQ_ID_OUTPUT");
		sql.append(" left outer join BRCWAREHOUSE.HF_SAMPLING_EVENT hbin on hbout.SAMPL_SEQ_ID_INPUT = hbin.SAMPL_SEQ_ID_OUTPUT");
		sql.append(" left outer join BRCWAREHOUSE.HF_BIOSAMPLE hbi on hbin.SAMPL_SEQ_ID_OUTPUT = hbi.SAMPL_SEQ_ID");
		sql.append(" left outer join BRCWAREHOUSE.HF_SUBJECT hbins on hbin.SUBJ_SEQ_ID = hbins.SUBJ_SEQ_ID");
		sql.append(" left outer join BRCWAREHOUSE.HF_SUBJECT_ASSESSMENT hbinsa on hbins.SUBJ_SEQ_ID = hbinsa.SUBJ_SEQ_ID");
		sql.append(" left outer join BRCWAREHOUSE.HF_EXPERIMENT he on ha.EXP_SEQ_ID = he.EXP_SEQ_ID");
		sql.append(" left outer join BRCWAREHOUSE.HF_BM_ASSAY_LINK bal on ha.ASSY_SEQ_ID = bal.ASSAY_SEQ_ID ");
		sql.append(" left outer join BRCWAREHOUSE.HF_BIOSET_INFO_MV bim on bal.BM_SEQ_ID = bim.BM_SEQ_ID");
		sql.append(" left outer join BRCWAREHOUSE.HF_DISEASE_LIST dl on hbout.DIS_SEQ_ID = dl.DIS_SEQ_ID");
		sql.append(
				" left outer join BRCWAREHOUSE.HF_DPE_ASSAY_TREATMENT_LINK datl on ha.EXP_SEQ_ID = datl.EXP_SEQ_ID and ha.ASSY_SEQ_ID = datl.ASSAY_SEQ_ID");
		sql.append(" where ha.exp_seq_id = :expId)");
		sql.append(" ORDER BY TREATMENT_NAME, to_number(TREATMENT_AMOUNT_SORT), ");
		sql.append(" to_number(TREATMENT_DURATION_SORT), ASSY_NAME");

		Session session = getSession(false);
		Query query = session.createSQLQuery(sql.toString());

		if (expId == null)
			return null;
		else
			query.setLong("expId", expId);

		return (List) query.list();

	}

	public List<String> getUSStates(String familyName) {
		return DaoUtils.getUSStates(super.getSession(), familyName);
	}

	// probably a way to do this with criteria
	public List<Object[]> findCountsForIEDB(Set<Long> iedbSet) {

		StringBuilder hql = new StringBuilder();
		hql.append("select IEDB_ID, count(distinct PROTEINID) from BRCWAREHOUSE.MODULE_IEDB_ROLLEDUP_MV ");
		hql.append(" where IEDB_ID in (");
		hql.append(CastHelper.arrayToStringNoTrailingComma(iedbSet.toArray()));
		hql.append(") GROUP BY IEDB_ID ORDER BY IEDB_ID");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		return (List) query.list();

	}

	public List<Long> findReferencePositionWithSF(String proteinAccession) {

		Criteria criteria = super.getSession().createCriteria(SFVTPosLookup.class);
		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("position"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.add(Restrictions.eq("proteinAccession", proteinAccession));
		criteria.add(Restrictions.eq("isSF", "Y"));
		criteria.addOrder(Order.asc("position"));
		return criteria.list();
	}

	public List<String> findDrugClass(String orgFamily) {
		Criteria criteria = super.getSession().createCriteria(DrugBankMaster.class);
		// orgFamily is passed in as null for vipr level search
		if (StringUtils.hasLength(orgFamily)) {
			criteria.createAlias("drugVirusLinks", "drugVirusLinks");
			criteria.createAlias("drugVirusLinks.organism", "organism");
			criteria.add(Restrictions.eq("organism.family", orgFamily));
		}

		criteria.add(Restrictions.isNotNull("drugClass"));
		criteria.add(Restrictions.isNull("obsoleteDate"));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("drugClass"));
		criteria.setProjection(Projections.distinct(retrieveColumn));
		criteria.addOrder(Order.asc("drugClass"));
		return criteria.list();

	}

	// find drug FDA status
	public List<String> findDrugGroup() {
		Criteria criteria = super.getSession().createCriteria(DrugBankMaster.class);
		criteria.add(Restrictions.isNotNull("drugGroups"));
		criteria.add(Restrictions.isNull("obsoleteDate"));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("drugGroups"));
		criteria.setProjection(Projections.distinct(retrieveColumn));
		criteria.addOrder(Order.asc("drugGroups"));

		return criteria.list();

	}

	public List<String> findDrugTarget() {
		Criteria criteria = super.getSession().createCriteria(DrugBankMaster.class);
		criteria.createAlias("drugTargets", "drugTargets");
		criteria.add(Restrictions.isNotNull("drugTargets.targetName"));
		criteria.add(Restrictions.isNull("obsoleteDate"));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("drugTargets.targetName"));
		criteria.setProjection(Projections.distinct(retrieveColumn));
		criteria.addOrder(Order.asc("drugTargets.targetName"));

		return criteria.list();

	}

	public List<Object[]> getDrugTaxonomy() {
		Criteria criteria = super.getSession().createCriteria(DrugVirusLink.class);
		criteria.createAlias("drugMaster", "drugMaster");
		criteria.add(Restrictions.isNull("drugMaster.obsoleteDate"));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("family"));
		retrieveColumn.add(Projections.property("subfamily"));
		retrieveColumn.add(Projections.property("genus"));
		retrieveColumn.add(Projections.property("species"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.addOrder(Order.asc("family"));
		criteria.addOrder(Order.asc("subfamily"));
		criteria.addOrder(Order.asc("genus"));
		criteria.addOrder(Order.asc("species"));

		return criteria.list();

	}

	public PlasmidSummary findPlasmidById(String plasmidId) {
		PlasmidSummary plasmid = (PlasmidSummary) super.getSession().get(PlasmidSummary.class, plasmidId);
		return plasmid;
	}

	public PlasmidSummary findPlasmidSeq(String plasmidId) {
		PlasmidSummary plasmid = (PlasmidSummary) super.getSession().get(PlasmidSummary.class, plasmidId);
		return plasmid;
	}

	public PlasmidVector findVectorById(String plasmidvector) {
		PlasmidVector vector = (PlasmidVector) super.getSession().get(PlasmidVector.class, plasmidvector);
		return vector;
	}

	public List<PlasmidPublication> findPubById(String plasmidId) {
		// PlasmidPublication pub = (PlasmidPublication)
		// super.getSession().get(PlasmidPublication.class, plasmidId);
		// return pub;

		Criteria criteria = super.getSession().createCriteria(PlasmidPublication.class);
		criteria.add(Restrictions.eq("plasmidNameId", plasmidId));

		// if (criteria.list() != null && criteria.list().size() > 0)
		// return (PlasmidPublication) criteria.list().get(0);
		// return null;

		List<PlasmidPublication> list = criteria.list();
		return list;
	}

	public List<PlasmidFeature> findFeaById(String plasmidId) {
		// PlasmidPublication pub = (PlasmidPublication)
		// super.getSession().get(PlasmidPublication.class, plasmidId);
		// return pub;

		Criteria criteria = super.getSession().createCriteria(PlasmidFeature.class);
		criteria.add(Restrictions.eq("plasmidNameId", plasmidId));
		criteria.add(Restrictions.eq("featureType", "CDS"));

		// if (criteria.list() != null && criteria.list().size() > 0)
		// return (PlasmidPublication) criteria.list().get(0);
		// return null;

		List<PlasmidFeature> list = criteria.list();
		return list;
	}

	public List<PlasmidFeature> findPlasmidAnnotationById(String plasmidId) {
		// PlasmidPublication pub = (PlasmidPublication)
		// super.getSession().get(PlasmidPublication.class, plasmidId);
		// return pub;

		Criteria criteria = super.getSession().createCriteria(PlasmidFeature.class);
		criteria.add(Restrictions.eq("plasmidNameId", plasmidId));

		// if (criteria.list() != null && criteria.list().size() > 0)
		// return (PlasmidPublication) criteria.list().get(0);
		// return null;

		List<PlasmidFeature> list = criteria.list();
		return list;
	}

	public DrugBankMaster findAntiviralDrugById(Long drugSeqId) {
		DrugBankMaster drug = (DrugBankMaster) super.getSession().get(DrugBankMaster.class, drugSeqId);
		return drug;
	}

	public DrugBankMaster findAntiviralDrugByHetId(String hetId) {
		Criteria criteria = super.getSession().createCriteria(DrugBankMaster.class);
		criteria.add(Restrictions.eq("hetId", hetId));
		if (criteria.list() != null && criteria.list().size() > 0)
			return (DrugBankMaster) criteria.list().get(0);
		return null;
	}

	public List<String> findDrugPDBIdsByHetId(String hetId) {
		Criteria criteria = super.getSession().createCriteria(DrugPdbSpeciesMap.class);
		criteria.add(Restrictions.eq("hetId", hetId));
		criteria.add(Restrictions.isNotNull("pdbId"));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("pdbId"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.addOrder(Order.asc("pdbId"));
		return criteria.list();
	}

	public DrugBankTarget findAntiviralDrugTargetById(Long targetSeqId) {
		DrugBankTarget drugTarget = (DrugBankTarget) super.getSession().get(DrugBankTarget.class, targetSeqId);
		return drugTarget;
	}

	public List<String> findDrugPDBSpecies(String hetId) {
		Criteria criteria = super.getSession().createCriteria(DrugPdbSpeciesMap.class);
		criteria.add(Restrictions.eq("hetId", hetId));
		criteria.add(Restrictions.isNotNull("species"));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("species"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.addOrder(Order.asc("species"));
		return criteria.list();

	}

	public List<DrugPdbSpeciesMap> findDrugPdbBySpecies(String hetId, String species) {
		Criteria criteria = super.getSession().createCriteria(DrugPdbSpeciesMap.class);

		criteria.add(Restrictions.eq("hetId", hetId));
		criteria.add(Restrictions.eq("species", species));
		criteria.add(Restrictions.isNotNull("ncbiReference"));
		criteria.addOrder(Order.asc("ncbiReference"));

		List<DrugPdbSpeciesMap> list = criteria.list();
		return list;
	}

	public List<Object[]> findDrugPdbBindingSite(String hetId, String ncbiReference, String pdbId) {
		Criteria criteria = super.getSession().createCriteria(DrugPdbBindingSiteOnRef.class);
		criteria.add(Restrictions.isNotNull("ncbiReference"));
		criteria.add(Restrictions.isNotNull("refResidueNumber"));

		criteria.add(Restrictions.eq("hetId", hetId));
		criteria.add(Restrictions.eq("ncbiReference", ncbiReference));
		criteria.add(Restrictions.eq("pdbId", pdbId));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("refResidue"));
		retrieveColumn.add(Projections.property("refResidueNumber"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.addOrder(Order.asc("refResidueNumber"));

		return criteria.list();

	}

	// this is used to show the drug binding site structure experiment general
	// info for each pdb
	public DrugPdbSpeciesMap findDrugPdbSpeciesMap(String hetId, String ncbiReference, String pdbId) {
		Criteria criteria = super.getSession().createCriteria(DrugPdbSpeciesMap.class);
		criteria.add(Restrictions.eq("hetId", hetId));
		criteria.add(Restrictions.eq("ncbiReference", ncbiReference));
		criteria.add(Restrictions.eq("pdbId", pdbId));
		List<DrugPdbSpeciesMap> list = criteria.list();

		if (list != null && list.size() > 0)
			return list.get(0);

		return null;

	}

	public DrugRef findDrugReference(String ncbiReference) {
		DrugRef drugRef = (DrugRef) super.getSession().get(DrugRef.class, ncbiReference);
		return drugRef;

	}

	// check if a drug binding pdb has a link in protein structure
	public String findDrugBindingPdbFamily(String pdbId) {
		Criteria criteria = super.getSession().createCriteria(PDBStructure.class);
		criteria.createAlias("proteinRecords", "pdbRecord");
		criteria.createAlias("pdbRecord.protein", "protein");
		criteria.createAlias("protein.genomicSequence", "genomicSequence");
		criteria.add(Restrictions.isNull("genomicSequence.obsoleteDate"));

		criteria.add(Restrictions.eq("pdbRecord.dbname", "PDB"));
		// Make sure a PDB file exists
		criteria.add(CommonCriteria.pdbFileExists(null));
		criteria.add(Restrictions.ilike("pdbRecord.accession", pdbId));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("genomicSequence.family"));
		criteria.setProjection(Projections.distinct(retrieveColumn));
		if (criteria.list() != null && criteria.list().size() > 0)
			return criteria.list().get(0).toString();
		else
			return null;
	}

	public List<String> findDrugMutationSpecies(Long drugSeqId) {
		Criteria criteria = super.getSession().createCriteria(DrugBankMaster.class);
		criteria.createAlias("drugMutations", "drugMutations");

		criteria.add(Restrictions.isNull("obsoleteDate"));
		criteria.add(Restrictions.isNotNull("drugMutations.mutationSpecies"));
		criteria.add(Restrictions.eq("id", drugSeqId));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("drugMutations.mutationSpecies"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.addOrder(Order.asc("drugMutations.mutationSpecies"));
		return criteria.list();

	}

	public List<DrugMutation> findDrugMutations(Long drugSeqId, String species) {
		Criteria criteria = super.getSession().createCriteria(DrugMutation.class);
		criteria.createAlias("drugMaster", "drugMaster");

		criteria.add(Restrictions.isNull("drugMaster.obsoleteDate"));
		criteria.add(Restrictions.eq("drugMaster.id", drugSeqId));
		criteria.add(Restrictions.eq("mutationSpecies", species));
		List<DrugMutation> list = criteria.list();
		return list;

	}

	public SequenceFeatures getResistanceRiskAnnotationSequenceFeature(Long sfnSeqId) {
		return (SequenceFeatures) super.getSession().load(SequenceFeatures.class, sfnSeqId);
	}

	public String getResistanceRiskAnnotationVTNumber(Long sfnSeqId, String variance) {
		Criteria criteria = super.getSession().createCriteria(SequenceVariants.class);
		criteria.createAlias("sequenceFeatures", "sf");
		criteria.add(Restrictions.eq("sf.id", sfnSeqId));
		criteria.add(Restrictions.eq("nonClobVariantSequence", variance));
		// ideally statement below should be used instead of using nonClob one,
		// but it seems run a little
		// slower than the statement above. It is ok for now since vipr
		// resistance sf variant sequences have not exceeds 4000 in length
		// criteria.add(Expression.sql("DBMS_LOB.COMPARE(VARIANT_SEQUENCE,'" +
		// variance + "')=0"));
		List<SequenceVariants> list = criteria.list();
		if (list != null && list.size() > 0) {
			SequenceVariants sv = list.get(0);
			int vtPartStartIndex = sv.getNonFluSFVTId().indexOf("VT-");
			String vtNumber = sv.getNonFluSFVTId().substring(vtPartStartIndex);
			return vtNumber;
		} else {
			return Constants.NOT_AVAILABLE;
		}

	}

	public SequenceFeatures getDrugBindingSiteSequenceFeature(String refAcc, String pdbId) {
		Criteria criteria = super.getSession().createCriteria(SequenceFeatures.class);
		criteria.add(Restrictions.eq("featureGroup", SequenceFeatures.FEATURE_GROUP_SFVT));
		criteria.add(Restrictions.eq("referenceStrainProteinAccession", refAcc));
		criteria.add(Restrictions.eq("pdbAccession", pdbId));
		criteria.add(Restrictions.eq("sequenceFeatureType", SequenceFeatures.FEATURE_ANTIVIRAL_TYPE));
		criteria.add(Restrictions.ilike("featureName", SequenceFeatures.FEATURE_ANTIVIRAL_BINDING_SITE_NAME, MatchMode.ANYWHERE));

		List<SequenceFeatures> result = criteria.list();
		if (result != null)
			return result.get(0);
		return null;

	}

	public List<Object[]> getInfluenzaSfvtProteinListByVirusType(InfluenzaType virusType) {
		Criteria criteria = super.getSession().createCriteria(SequenceFeatures.class);

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("proteinName"));
		retrieveColumn.add(Projections.property("proteinGeneSymbol"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.add(Restrictions.eq("orgId", virusType.getCodeLong()));

		return criteria.list();
	}

	public List<String> getVirusTypeByStrainName(Strain virusStrain) {
		Criteria criteria = super.getSession().createCriteria(Strain.class);
		criteria.setProjection(Projections.property("organism.id"));

		criteria.add(Restrictions.eq("name", virusStrain.getName()));
		List<String> list = criteria.list();

		return list;
	}

	public List<Long> getExperimentIdsByNames(String[] experiementNames) {
		Criteria criteria = super.getSession().createCriteria(HfExperiment.class);
		criteria.setProjection(Projections.property("id"));

		criteria.add(Restrictions.in("expUserDefName", experiementNames));
		List<Long> list = criteria.list();

		return list;
	}

	public List<String> getHpiExperimentNames(List<Long> experiementIds) {
		Criteria criteria = super.getSession().createCriteria(HfExperiment.class);
		criteria.setProjection(Projections.property("expUserDefName"));

		if (experiementIds == null || experiementIds.size() <= 0) {
			// criteria.add(Restrictions.in("id", experiementIds));
		} else {
			criteria.add(Restrictions.in("id", experiementIds));
		}
		ProjectionList retrieveColumn = Projections.projectionList();
		// retrieveColumn.add(Projections.property("id"));
		retrieveColumn.add(Projections.property("expUserDefName"));
		criteria.setProjection(Projections.distinct(retrieveColumn));
		// if (criteria.list() != null && criteria.list().size() > 0)
		// return criteria.list();
		// else
		// return null;
		return criteria.list();

	}

	public List<Long> getHpiExperimentIdByName(String expName) {
		Criteria criteria = super.getSession().createCriteria(HfExperiment.class);
		// criteria.setProjection(Projections.property("id"));

		if (expName == null || expName.length() <= 0) {
			return null;
		} else {
			criteria.add(Restrictions.ilike("expUserDefName", expName));
		}
		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("id"));
		// retrieveColumn.add(Projections.property("expUserDefName"));
		criteria.setProjection(Projections.distinct(retrieveColumn));
		// if (criteria.list() != null && criteria.list().size() > 0)
		// return criteria.list();
		// else
		// return null;
		return criteria.list();

	}

	public List<Long> getHpiExperimentIds(List<Long> experiementIds) {
		Criteria criteria = super.getSession().createCriteria(HfExpPos.class);
		criteria.setProjection(Projections.property("experimentId"));

		if (experiementIds == null || experiementIds.size() <= 0) {
			// criteria.add(Restrictions.in("experimentId", experiementIds));
		} else {
			criteria.add(Restrictions.in("experimentId", experiementIds));
		}
		criteria.uniqueResult();
		List<Long> list = criteria.list();

		return list;
	}

	/*
	 * column: refseq/gbAcc/gene/unigeneId/uniprotAc
	 */
	public List<Object[]> getHpiResultByColumnAndIds(String column, List<String> ids) {
		Criteria criteria = super.getSession().createCriteria(HfExpPos.class);
		ProjectionList pl = Projections.projectionList();
		pl.add(Projections.property("experimentId"));
		pl.add(Projections.property(column));
		criteria.setProjection(Projections.distinct(pl));
		criteria.addOrder(Order.asc("experimentId"));
		criteria.addOrder(Order.asc(column));

		List<Object[]> list = new ArrayList<Object[]>();
		if (ids == null || ids.size() <= 0) {
			// criteria.add(Restrictions.in("experimentId", experiementIds));
		} else {
			criteria.add(Restrictions.in(column, ids));
			// criteria.uniqueResult();
			list = criteria.list();
		}

		return list;
	}

	/*
	 * Gets the HfExperiment by its id
	 */
	public HfExperiment getHfExperimentById(Long id) {
		HfExperiment hf = null;
		if (id == null) {
			return hf;
		}

		Criteria criteria = super.getSession().createCriteria(HfExperiment.class);
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);

		List<HfExperiment> list = new ArrayList<HfExperiment>();
		criteria.add(Restrictions.eq("id", id));
		list = criteria.list();
		if (list.size() > 0) {
			hf = list.get(0);
		}

		return hf;
	}

	// BRC-11509
	public List<String> getModuleColorsByExperimentId(long experimentId) {
		Criteria criteria = super.getSession().createCriteria(HfCorrGeneInfo.class);

		criteria.add(Restrictions.eq("experimentId", experimentId));
		criteria.setProjection(Projections.distinct(Property.forName("moduleId")));

		List<String> colors = criteria.list();
		if (colors != null && colors.size() > 0) {

			return colors;
		} else {
			return null;
		}

	}

	// probably a way to do this with criteria
	public List<String> findModuleColors(long experimentId) {

		StringBuilder hql = new StringBuilder();
		// BRC-11751
		// if (isRNASeqExperiment(experimentId))
		// hql.append("select distinct MODULE_ID from
		// BRCWAREHOUSE.HF_CORR_MOD_INFO ");
		// else
		hql.append("select distinct MODULE_ID from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(experimentId);
		// hql.append("");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		return (List) query.list();

	}

	public List<String> findMetadataCategories(long experimentId) {

		StringBuilder hql = new StringBuilder();
		hql.append("select distinct METADATA_CATEGORY from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(experimentId);
		// BRC-11692 Add order by clause
		hql.append(" GROUP BY METADATA_CATEGORY ORDER BY METADATA_CATEGORY ASC");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		return (List) query.list();

	}

	public List<String> getGeneIdsbyModuleId(Long experimentId, String moduleId) {
		Criteria criteria = super.getSession().createCriteria(HfCorrConnectivity.class);
		// criteria.createAlias("moduleColors", "mc");
		criteria.add(Restrictions.eq("experimentId", experimentId));
		criteria.add(Restrictions.eq("moduleId", "'" + moduleId + "'"));
		criteria.setProjection(Projections.distinct(Property.forName("hostFactorId1")));

		List<String> genes = criteria.list();
		if (genes != null && genes.size() > 0) {

			return genes;
		} else {
			return null;
		}

	}

	// probably a way to do this with criteria
	public List<HfCorrGeneInfo> findModuleGenes(long experimentId, String moduleId) {
		// experimentId = 222;
		StringBuilder hql = new StringBuilder();
		// BRC-11751
		if (isRNASeqExperiment(experimentId))
			hql.append(
					"select distinct HOST_FACTOR_ID, GENE_SYMBOL, GENE_NAME, ENTREZ_GENE_ID, GB_ACC from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		else
			hql.append(
					"select distinct PROBE_ID, GENE_SYMBOL, GENE_NAME, ENTREZ_GENE_ID, GB_ACC from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(experimentId);
		hql.append(" and MODULE_ID = '");
		hql.append(moduleId.trim());
		hql.append("'");
		if (isRNASeqExperiment(experimentId))
			hql.append(" Order by HOST_FACTOR_ID asc");
		else
			hql.append(" Order by PROBE_ID asc");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		List<HfCorrGeneInfo> hfList = new ArrayList<HfCorrGeneInfo>();

		for (Object i : query.list()) {
			HfCorrGeneInfo hc = new HfCorrGeneInfo();

			hc.setHostFactorId(String.valueOf(((Object[]) i)[0]));
			hc.setGeneSymbol(String.valueOf(((Object[]) i)[1]));
			hc.setGeneName(String.valueOf(((Object[]) i)[2]));
			hc.setEntrezGeneId(String.valueOf(((Object[]) i)[3]));
			hc.setGbAccession(String.valueOf(((Object[]) i)[4]));
			hfList.add(hc);
		}
		return hfList;
	}

	public List<Object[]> getExperimentGeneListForDownload(Long expId) {

		StringBuilder hql = new StringBuilder();
		// BRC-11751
		if (isRNASeqExperiment(expId))
			hql.append("select distinct HOST_FACTOR_ID, MODULE_ID, MODULE_COLOR  from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		else
			hql.append("select distinct PROBE_ID, MODULE_ID, MODULE_COLOR  from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL	 ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(expId);

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		return (List) query.list();

	}

	public List<Object[]> getModuleMetaData(Long expId) {

		Criteria criteria = super.getSession().createCriteria(HfCorrGeneInfo.class);
		// criteria.createAlias("drugMaster", "drugMaster");
		// criteria.add(Restrictions.isNull("drugMaster.obsoleteDate"));
		criteria.add(Restrictions.eq("experimentId", expId));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("moduleColor"));
		retrieveColumn.add(Projections.property("virusPVal"));
		retrieveColumn.add(Projections.property("MOIPval"));
		retrieveColumn.add(Projections.property("timePVal"));
		// retrieveColumn.add(Projections.property("replicate"));
		criteria.setProjection(Projections.distinct(retrieveColumn));

		criteria.addOrder(Order.asc("moduleId"));
		criteria.addOrder(Order.asc("virusPVal"));
		criteria.addOrder(Order.asc("MOIPval"));
		criteria.addOrder(Order.asc("timePVal"));
		// criteria.addOrder(Order.asc("replicate"));

		return criteria.list();

	}

	public List<Object[]> findModuleMetaData(Long expId) {

		StringBuilder hql = new StringBuilder();

		hql.append(
				"select distinct MODULE_ID, pvalue_display(VIRUS_PVAL),  pvalue_display(MOI_PVAL),  pvalue_display(TIME_PVAL), pvalue_display(VIRUS_STRENGTH),  pvalue_display(MOI_STRENGTH), pvalue_display(TIME_STRENGTH) from BRCWAREHOUSE.HF_CORR_MOD_INFO ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(expId);
		// hql.append("");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		return (List) query.list();

	}

	public List<BigDecimal> getPrimarykeys(long experimentId, String moduleId, List<String> geneId) {
		// experimentId = 222;
		String geneIdList = "(";
		for (String gene : geneId) {
			if ("(".equals(geneIdList))
				geneIdList = geneIdList + "'";
			else
				geneIdList = geneIdList + ",'";
			geneIdList = geneIdList + gene;
			geneIdList = geneIdList + "'";
		}
		geneIdList = geneIdList + ")";

		StringBuilder hql = new StringBuilder();
		hql.append("select CORR_MOD_MEMBERSHIP_SEQ_ID from BRCWAREHOUSE.HF_CORR_MOD_MEMBERSHIP ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(experimentId);
		hql.append(" and MODULE_ID = '");
		hql.append(moduleId);
		hql.append("'");
		hql.append(" and HOST_FACTOR_ID in ");
		hql.append(geneIdList);

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		return (List) query.list();

	}

	public List<Object[]> getModuleGeneSignificance(long expSeqId, String moduleColor, String metaDataDimension) {

		Criteria criteria = super.getSession().createCriteria(HfCorrGeneInfo.class);
		criteria.add(Restrictions.eq("experimentId", expSeqId));
		criteria.add(Restrictions.eq("moduleId", "'" + moduleColor + "'"));

		ProjectionList retrieveColumn = Projections.projectionList();
		retrieveColumn.add(Projections.property("hostFactorId"));
		if ("virus".equalsIgnoreCase(metaDataDimension))
			retrieveColumn.add(Projections.property("virusPVal"));
		else if ("moi".equalsIgnoreCase(metaDataDimension))
			retrieveColumn.add(Projections.property("MOIPval"));
		else if ("time".equalsIgnoreCase(metaDataDimension))
			retrieveColumn.add(Projections.property("timePVal"));

		criteria.setProjection(Projections.distinct(retrieveColumn));

		return criteria.list();

	}

	@Override
	public List<HfCorrGeneInfo> findModuleGeneSignificance(long expSeqId, String moduleColor, String metaDataDimension) {
		// expSeqId = 222;
		StringBuilder hql = new StringBuilder();
		// BRC-11751
		if (isRNASeqExperiment(expSeqId)) {
			hql.append(
					"select distinct HOST_FACTOR_ID, GENE_SIG_PVALUE, GENE_SYMBOL, GENE_NAME, ENTREZ_GENE_ID, GB_ACC from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL  ");
			// if ("virus".equalsIgnoreCase(metaDataDimension))
			// hql.append("select HOST_FACTOR_ID, VIRUS_PVAL from
			// BRCWAREHOUSE.HF_CORR_GENE_INFO ");
			// else if ("moi".equalsIgnoreCase(metaDataDimension))
			// hql.append("select HOST_FACTOR_ID, MOI_PVAL from
			// BRCWAREHOUSE.HF_CORR_GENE_INFO ");
			// else if ("time".equalsIgnoreCase(metaDataDimension))
			// hql.append("select HOST_FACTOR_ID, TIME_PVAL from
			// BRCWAREHOUSE.HF_CORR_GENE_INFO ");
		} else {
			hql.append(
					"select distinct PROBE_ID, GENE_SIG_PVALUE, GENE_SYMBOL, GENE_NAME, ENTREZ_GENE_ID, GB_ACC from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL  ");
		}
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(expSeqId);
		hql.append(" and MODULE_ID = '");
		hql.append(moduleColor.trim());
		hql.append("'");
		hql.append(" and METADATA_CATEGORY = '");
		hql.append(metaDataDimension.trim());
		hql.append("'");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		List<HfCorrGeneInfo> hfList = new ArrayList<HfCorrGeneInfo>();

		for (Object i : query.list()) {
			HfCorrGeneInfo hc = new HfCorrGeneInfo();
			hc.setProbeId(String.valueOf(((Object[]) i)[0]));
			hc.setGeneSignificancePValue(new BigDecimal(String.valueOf(((Object[]) i)[1])));

			hc.setGeneSymbol(String.valueOf(((Object[]) i)[2]));
			hc.setGeneName(String.valueOf(((Object[]) i)[3]));
			hc.setEntrezGeneId(String.valueOf(((Object[]) i)[4]));
			hc.setGbAccession(String.valueOf(((Object[]) i)[5]));

			hfList.add(hc);
		}
		return hfList;
		// return (List) query.list();
	}

	@Override
	public List<Object[]> findMetaDataByCategory(long expSeqId) {

		StringBuilder hql = new StringBuilder();

		hql.append(
				"select MODULE_ID, METADATA_CATEGORY, pvalue_display(CORR_PVALUE), pvalue_display(CORR_STRENGTH) from BRCWAREHOUSE.HF_CORR_MODULE_META_ALL ");

		hql.append(" where EXP_SEQ_ID = ");

		hql.append(expSeqId);

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		return (List) query.list();
	}

	public static final long[] rNASeQExps = { 222, 223, 224 };

	@Override
	public boolean isRNASeqExperiment(long expSeqId) {
		for (int i = 0; i < rNASeQExps.length; i++)
			if (rNASeQExps[i] == expSeqId)
				return true;
		return false;
	}

	// For Enrichment Analysis
	public List<HfCorrGeneInfo> findGenesData(long experimentId, String moduleId, List<String> geneId, String modString) {
		// experimentId = 222;
		StringBuilder hql = new StringBuilder();
		String idString = CastHelper.listToStringWithQuotes(geneId);
		// BRC-11751
		if (isRNASeqExperiment(experimentId))
			hql.append(
					"select distinct HOST_FACTOR_ID, ENTREZ_GENE_ID, GENE_SYMBOL from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		else
			hql.append("select distinct PROBE_ID, ENTREZ_GENE_ID, GENE_SYMBOL from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(experimentId);
		hql.append(" and MODULE_ID = '");
		hql.append(moduleId.trim());
		hql.append("'");
		if (null != modString) {
			hql.append(" and METADATA_CATEGORY = '");
			hql.append(modString.trim());
			hql.append("'");
		}

		if (isRNASeqExperiment(experimentId))
			hql.append(" and HOST_FACTOR_ID in (");
		else
			hql.append(" and PROBE_ID in (");

		hql.append(idString);
		hql.append(")");
		// hql.append(moduleId.trim());

		if (isRNASeqExperiment(experimentId))
			hql.append(" Order by HOST_FACTOR_ID asc");
		else
			hql.append(" Order by PROBE_ID asc");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		List<HfCorrGeneInfo> hfList = new ArrayList<HfCorrGeneInfo>();

		for (Object i : query.list()) {
			HfCorrGeneInfo hc = new HfCorrGeneInfo();

			hc.setHostFactorId(String.valueOf(((Object[]) i)[0]));
			hc.setGeneSymbol(String.valueOf(((Object[]) i)[2]));

			hc.setEntrezGeneId(String.valueOf(((Object[]) i)[1]));

			hfList.add(hc);
		}
		return hfList;
	}

	public List<HfCorrGeneInfo> findGenesDatabyExprmtId(long expSeqId) {
		StringBuilder hql = new StringBuilder();
		// String idString = CastHelper.listToStringWithQuotes(geneId);
		// BRC-11751
		if (isRNASeqExperiment(expSeqId))
			hql.append(
					"select distinct HOST_FACTOR_ID, ENTREZ_GENE_ID, GENE_SYMBOL from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		else
			hql.append("select distinct PROBE_ID, ENTREZ_GENE_ID, GENE_SYMBOL from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(expSeqId);
		if (isRNASeqExperiment(expSeqId))
			hql.append(" Order by HOST_FACTOR_ID asc");
		else
			hql.append(" Order by PROBE_ID asc");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		List<HfCorrGeneInfo> hfList = new ArrayList<HfCorrGeneInfo>();

		for (Object i : query.list()) {
			HfCorrGeneInfo hc = new HfCorrGeneInfo();

			hc.setHostFactorId(String.valueOf(((Object[]) i)[0]));
			hc.setGeneSymbol(String.valueOf(((Object[]) i)[2]));

			hc.setEntrezGeneId(String.valueOf(((Object[]) i)[1]));

			hfList.add(hc);
		}
		return hfList;
	}

	@Override
	public List<SpProject> getSystemBiologyProjectDetails() {
		Criteria criteria = super.getSession().createCriteria(SpProject.class);

		criteria.add(Restrictions.eq("programCode", "SP3"));
		List<SpProject> list = new ArrayList<SpProject>();
		list = criteria.list();

		List<HfExperiment> hfExperiments = GetAllHFExperiments();

		List<HfExperiment> lipidExperiments = new ArrayList<HfExperiment>();
		List<HfExperiment> proteinExperiments = new ArrayList<HfExperiment>();
		List<HfExperiment> transcriptExperiments = new ArrayList<HfExperiment>();
		List<HfExperiment> fluomicsExperiments = new ArrayList<HfExperiment>();
		List<HfExperiment> sysInfluenzaExperiments = new ArrayList<HfExperiment>();
		List<HfExperiment> sysInflLipidExperiments = new ArrayList<HfExperiment>();
		List<HfExperiment> sysInflProteinExperiments = new ArrayList<HfExperiment>();
		List<HfExperiment> sysInflTranscriptExperiments = new ArrayList<HfExperiment>();
		for (HfExperiment ex : hfExperiments) {
			if (!ex.getExpUserDefName().endsWith("Expression")) {
				if (ex.getExpUserDefName().startsWith("E_R_")) {
					fluomicsExperiments.add(ex);
				} else if ("SI".equalsIgnoreCase(ex.getInstitutionCode())) {
					// sysInfluenzaExperiments.add(ex);
					if (HfExperiment.LIPID_EXPERIMENT.equalsIgnoreCase(ex.getExperimentType()))
						sysInflLipidExperiments.add(ex);
					else if (HfExperiment.PROTEIN_EXPERIMENT.equalsIgnoreCase(ex.getExperimentType()))
						sysInflProteinExperiments.add(ex);
					else if (HfExperiment.TRANSCRIPT_EXPERIMENT.equalsIgnoreCase(ex.getExperimentType()))
						sysInflTranscriptExperiments.add(ex);
				} else if ("SV".equalsIgnoreCase(ex.getInstitutionCode())) {
					if (HfExperiment.LIPID_EXPERIMENT.equalsIgnoreCase(ex.getExperimentType()))
						lipidExperiments.add(ex);
					else if (HfExperiment.PROTEIN_EXPERIMENT.equalsIgnoreCase(ex.getExperimentType()))
						proteinExperiments.add(ex);
					else if (HfExperiment.TRANSCRIPT_EXPERIMENT.equalsIgnoreCase(ex.getExperimentType()))
						transcriptExperiments.add(ex);
				}
			}
		}
		String prgrm_cd = "SP3";
		if (list.size() == 0) {
			StringBuilder hql = new StringBuilder();
			hql.append(
					"select PROJECT_DESCRIPTION, FUNDING_ORGANIZATION, PI_LIST, CO_INVESTIGATOR_LIST,PUBLICATION_LIST, PROJECT_HOME_URL, PROJECT_NAME from BRCWAREHOUSE.SP_PROJECT ");
			hql.append(" where PROGRAM_CODE = '" + "SP3" + "'");

			Session session = getSession(false);
			Query query = session.createSQLQuery(hql.toString());
			List<SpProject> hfList = new ArrayList<SpProject>();
			for (Object i : query.list()) {
				SpProject hc = new SpProject();

				hc.setProjectDescription(String.valueOf(((Object[]) i)[0]));
				hc.setFundingOrganization(String.valueOf(((Object[]) i)[1]));
				hc.setPoiList(String.valueOf(((Object[]) i)[2]));
				hc.setCoiList(String.valueOf(((Object[]) i)[3]));
				hc.setPublicationList(String.valueOf(((Object[]) i)[4]));
				hc.setProjecthomeURL(String.valueOf(((Object[]) i)[5]));
				hc.setProjectName(String.valueOf(((Object[]) i)[6]));

				if ("Systems Virology".equalsIgnoreCase(hc.getProjectName())) {
					hc.setLipidExperiments(lipidExperiments);
					hc.setProteinExperiments(proteinExperiments);
					hc.setTranscriptExperiments(transcriptExperiments);
				}
				if ("Systems Influenza".equalsIgnoreCase(hc.getProjectName())) {
					hc.setSysInflLipidExperiments(sysInflLipidExperiments);
					hc.setSysInflProteinExperiments(sysInflProteinExperiments);
					hc.setSysInflTranscriptExperiments(sysInflTranscriptExperiments);
				} else if ("FLUOMICS".equalsIgnoreCase(hc.getProjectName()))
					hc.setFluomicsExperiments(fluomicsExperiments);

				hfList.add(hc);
			}

			return hfList;
		}

		return list;
	}

	@Override
	public List<HfExperiment> GetAllHFExperiments() {

		StringBuilder hql = new StringBuilder();
		hql.append(
				"select EXP_SEQ_ID, EXP_USER_DEF_NAME, EXP_TYPE, EXP_VIRUSBRC_ACCESSION, INSTITUTION_CODE from BRCWAREHOUSE.HF_EXPERIMENT");
		hql.append(" Order by EXP_USER_DEF_NAME asc");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		List<HfExperiment> exList = new ArrayList<HfExperiment>();
		for (Object i : query.list()) {
			HfExperiment ex = new HfExperiment();

			ex.setId(new Long(String.valueOf(((Object[]) i)[0])));
			ex.setExpUserDefName(String.valueOf(((Object[]) i)[1]));
			ex.setExperimentType(String.valueOf(((Object[]) i)[2]));
			ex.setVirusbrcAccession(String.valueOf(((Object[]) i)[3]));
			ex.setInstitutionCode(String.valueOf(((Object[]) i)[4]));
			exList.add(ex);
		}
		return exList;

	}

	@Override
	public List<HfCorrGeneInfo> findModuleGeneDataByEntrezGeneID(List<String> entrezGeneId, String expId, Map entrezIds) {
		// experimentId = 222;
		StringBuilder hql = new StringBuilder();
		// long experimentId = new Long(getExperimentIdByEntrezGeneId(new
		// Long(entrezGeneId.get(0)))).longValue();
		System.out.println("lookupdaoimpl: " + entrezGeneId.toString() + " entrezIds: " + entrezIds.toString());
		String ls = CastHelper.longListToStringNoEmpty(entrezGeneId);
		// BRC-11751
		if (isRNASeqExperiment(new Long(expId)))
			hql.append(
					"select distinct HOST_FACTOR_ID, GENE_SYMBOL, GENE_NAME, ENTREZ_GENE_ID, GB_ACC from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		else
			hql.append(
					"select distinct PROBE_ID, GENE_SYMBOL, GENE_NAME, ENTREZ_GENE_ID, GB_ACC from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(expId);
		hql.append(" and trim(ENTREZ_GENE_ID) in (");
		hql.append(ls);
		hql.append(")");
		if (isRNASeqExperiment(new Long(expId)))
			hql.append(" Order by HOST_FACTOR_ID asc");
		else
			hql.append(" Order by PROBE_ID asc");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		List<HfCorrGeneInfo> hfList = new ArrayList<HfCorrGeneInfo>();

		for (Object i : query.list()) {
			HfCorrGeneInfo hc = new HfCorrGeneInfo();
			hc.setExperimentId(new Long(expId).longValue());
			hc.setHostFactorId(String.valueOf(((Object[]) i)[0]));
			hc.setGeneSymbol(String.valueOf(((Object[]) i)[1]));
			hc.setGeneName(String.valueOf(((Object[]) i)[2]));
			hc.setEntrezGeneId(String.valueOf(((Object[]) i)[3]));
			hc.setGbAccession(String.valueOf(((Object[]) i)[4]));
			hc.setGoTerm(entrezIds.get(String.valueOf(((Object[]) i)[3])).toString());
			hfList.add(hc);
		}
		return hfList;
	}

	private void longListToStringNoEmpty(List<Long> entrezGeneId) {
		// TODO Auto-generated method stub

	}

	private String getExperimentIdByEntrezGeneId(long entrezGeneId) {
		StringBuilder hql = new StringBuilder();

		// BRC-11751

		hql.append("select distinct EXP_SEQ_ID from BRCWAREHOUSE.HF_CORR_GENE_SIG_PVAL_ALL ");
		hql.append(" where ENTREZ_GENE_ID = ");
		hql.append(entrezGeneId);

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		return String.valueOf(query.list().get(0));
	}

	// BRC-11805
	@Override
	public List<HfCorrEdgeAll> getEdgedata(String expId, String moduleId) {
		// experimentId = 222;
		StringBuilder hql = new StringBuilder();
		// long experimentId = new Long(getExperimentIdByEntrezGeneId(new
		// Long(entrezGeneId.get(0)))).longValue();

		if (isRNASeqExperiment(new Long(expId)))
			hql.append("select source_hf_id_gene_symbol, target_hf_id_gene_symbol, WEIGHT from BRCWAREHOUSE.HF_CORR_EDGE_ALL");

		else
			hql.append("select SOURCE_PROBE_ID, TARGET_PROBE_ID, WEIGHT from BRCWAREHOUSE.HF_CORR_EDGE_ALL ");

		hql.append(" where EXP_SEQ_ID = ");
		hql.append(expId);
		hql.append(" and MODULE_ID = '");
		hql.append(moduleId.trim());
		hql.append("'");
		hql.append(" and source_hf_id_gene_symbol !='" + "-'");
		hql.append(" and target_hf_id_gene_symbol !='" + "-'");
		hql.append(" and DISPLAY = 'Y'");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		List<HfCorrEdgeAll> hfList = new ArrayList<HfCorrEdgeAll>();

		for (Object i : query.list()) {
			HfCorrEdgeAll hc = new HfCorrEdgeAll();
			hc.setSource(String.valueOf(((Object[]) i)[0]));
			hc.setTarget(String.valueOf(((Object[]) i)[1]));
			hc.setWeight(new BigDecimal(String.valueOf(((Object[]) i)[2])));
			hfList.add(hc);
		}
		System.out.println("edge list size: " + hfList.size());
		return hfList;
	}

	// BRC-11085 second phase
	@Override
	public List<HfCorrNodeAll> getNodelist(String expId, String moduleId) {

		StringBuilder hql = new StringBuilder();
		if (isRNASeqExperiment(new Long(expId)))
			hql.append(
					"select distinct node.PROBE_ID, node.HF_ID_GENE_SYMBOL, node.STATISTICAL_MEASURE, node.QUANTITATIVE_MEASURE, node.STATISTICAL_MEASURE_DISPLAY from BRCWAREHOUSE.HF_CORR_NODE_ALL node ");
		else
			hql.append(
					"select distinct node.PROBE_ID, node.HF_ID_GENE_SYMBOL, node.STATISTICAL_MEASURE, node.QUANTITATIVE_MEASURE, node.STATISTICAL_MEASURE_DISPLAY  from BRCWAREHOUSE.HF_CORR_NODE_ALL node ");
		hql.append(
				"INNER JOIN BRCWAREHOUSE.HF_CORR_EDGE_ALL edge on edge.EXP_SEQ_ID = node.EXP_SEQ_ID and edge.MODULE_ID = node.MODULE_ID ");
		hql.append(" where node.EXP_SEQ_ID = ");
		hql.append(expId);
		hql.append(" and node.MODULE_ID = '");
		hql.append(moduleId.trim());
		hql.append("'");
		// hql.append(" and node.HF_ID_GENE_SYMBOL !='" + "-'");
		hql.append(" and edge.SOURCE_HF_ID_GENE_SYMBOL !='" + "-'");
		hql.append(" and edge.TARGET_HF_ID_GENE_SYMBOL !='" + "-'");
		hql.append(
				"AND (node.HF_ID_GENE_SYMBOL = edge.SOURCE_HF_ID_GENE_SYMBOL OR node.HF_ID_GENE_SYMBOL = edge.TARGET_HF_ID_GENE_SYMBOL)");
		hql.append(" and edge.DISPLAY = 'Y'");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());
		List<HfCorrNodeAll> nodeList = new ArrayList<HfCorrNodeAll>();

		for (Object i : query.list()) {
			HfCorrNodeAll node = new HfCorrNodeAll();
			node.setProbeId(String.valueOf(((Object[]) i)[0]));
			node.setGeneSymbol(String.valueOf(((Object[]) i)[1]));
			node.setStatMeasure(new BigDecimal(String.valueOf(((Object[]) i)[2])));
			node.setQuantMeasure(new BigDecimal(String.valueOf(((Object[]) i)[3])));
			node.setSmDisplay(String.valueOf(((Object[]) i)[4]));
			nodeList.add(node);
		}
		System.out.println("node list size: " + nodeList.size());
		return nodeList;
	}

	// BRC-11805
	@Override
	public Map<String, String> getNodedata(String expId, String moduleId) {

		StringBuilder hql = new StringBuilder();
		if (isRNASeqExperiment(new Long(expId)))
			hql.append("select distinct node.PROBE_ID, node.HF_ID_GENE_SYMBOL from BRCWAREHOUSE.HF_CORR_NODE_ALL node ");
		else
			hql.append("select distinct node.PROBE_ID, node.HF_ID_GENE_SYMBOL from BRCWAREHOUSE.HF_CORR_NODE_ALL node ");
		hql.append(
				"INNER JOIN BRCWAREHOUSE.HF_CORR_EDGE_ALL edge on edge.EXP_SEQ_ID = node.EXP_SEQ_ID and edge.MODULE_ID = node.MODULE_ID ");
		hql.append(" where node.EXP_SEQ_ID = ");
		hql.append(expId);
		hql.append(" and node.MODULE_ID = '");
		hql.append(moduleId.trim());
		hql.append("'");
		hql.append(" and node.HF_ID_GENE_SYMBOL !='" + "-'");
		hql.append(" and edge.SOURCE_HF_ID_GENE_SYMBOL !='" + "-'");
		hql.append(" and edge.TARGET_HF_ID_GENE_SYMBOL !='" + "-'");
		hql.append(
				"AND (node.HF_ID_GENE_SYMBOL = edge.SOURCE_HF_ID_GENE_SYMBOL OR node.HF_ID_GENE_SYMBOL = edge.TARGET_HF_ID_GENE_SYMBOL)");
		hql.append(" and edge.DISPLAY = 'Y'");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());
		Map<String, String> nodeList = new HashMap<String, String>();
		for (Object i : query.list()) {
			nodeList.put(String.valueOf(((Object[]) i)[0]), String.valueOf(((Object[]) i)[1]));
		}

		System.out.println("node list size: " + nodeList.size());
		if (nodeList != null && nodeList.size() > 0) {
			return nodeList;
		} else {
			return null;
		}
	}

	@Override
	public int getTotalNode(String expId, String moduleId) {

		StringBuilder hql = new StringBuilder();
		hql.append("select distinct node.PROBE_ID, node.HF_ID_GENE_SYMBOL from BRCWAREHOUSE.HF_CORR_NODE_ALL node ");
		hql.append(
				"INNER JOIN BRCWAREHOUSE.HF_CORR_EDGE_ALL edge on edge.EXP_SEQ_ID = node.EXP_SEQ_ID and edge.MODULE_ID = node.MODULE_ID ");
		hql.append(" where node.EXP_SEQ_ID = ");
		hql.append(expId);
		hql.append(" and node.MODULE_ID = '");
		hql.append(moduleId.trim());
		hql.append("'");
		hql.append(" and node.HF_ID_GENE_SYMBOL !='" + "-'");
		hql.append(" and edge.SOURCE_HF_ID_GENE_SYMBOL !='" + "-'");
		hql.append(" and edge.TARGET_HF_ID_GENE_SYMBOL !='" + "-'");
		hql.append(
				"AND (node.HF_ID_GENE_SYMBOL = edge.SOURCE_HF_ID_GENE_SYMBOL OR node.HF_ID_GENE_SYMBOL = edge.TARGET_HF_ID_GENE_SYMBOL)");
		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());
		return query.list().size();

	}

	@Override
	public int getTotalEdge(String expId, String moduleId) {
		StringBuilder hql = new StringBuilder();
		// long experimentId = new Long(getExperimentIdByEntrezGeneId(new
		// Long(entrezGeneId.get(0)))).longValue();
		hql.append("select source_hf_id_gene_symbol, target_hf_id_gene_symbol, WEIGHT from BRCWAREHOUSE.HF_CORR_EDGE_ALL ");
		hql.append(" where EXP_SEQ_ID = ");
		hql.append(expId);
		hql.append(" and MODULE_ID = '");
		hql.append(moduleId.trim());
		hql.append("'");
		hql.append(" and source_hf_id_gene_symbol !='" + "-'");
		hql.append(" and target_hf_id_gene_symbol !='" + "-'");
		// hql.append(" and DISPLAY = 'Y'");

		Session session = getSession(false);
		Query query = session.createSQLQuery(hql.toString());

		List<HfCorrEdgeAll> hfList = new ArrayList<HfCorrEdgeAll>();
		return query.list().size();

	}

}
