package com.ngc.brc.web.actions.search.common.hostFactor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ngc.brc.commons.constants.Constants;
import com.ngc.brc.commons.constants.DownloadConstants;
import com.ngc.brc.commons.constants.SystemErrorCodes;
import com.ngc.brc.commons.helperobjects.AjaxDisplayColumns;
import com.ngc.brc.commons.helperobjects.NameTwoValues;
import com.ngc.brc.commons.helperobjects.NameValue;
import com.ngc.brc.commons.helperobjects.SelectBox;
import com.ngc.brc.commons.utils.BeanKeys;
import com.ngc.brc.commons.utils.CastHelper;
import com.ngc.brc.commons.utils.ClassUtils;
import com.ngc.brc.commons.utils.IdList;
import com.ngc.brc.commons.utils.ItemSelectionHelper;
import com.ngc.brc.commons.utils.ResultsAndSelectedIdsCache;
import com.ngc.brc.commons.utils.Utils;
import com.ngc.brc.commons.utils.io.FileExtension;
import com.ngc.brc.commons.utils.io.ZipUtils;
import com.ngc.brc.commons.web.action.helper.CommonHelper;
import com.ngc.brc.dao.util.CriteriaAlias;
import com.ngc.brc.dao.util.CriteriaAliasFetch;
import com.ngc.brc.dao.util.CriteriaOperators;
import com.ngc.brc.model.hostFactor.HfExperiment;
import com.ngc.brc.model.nonpersistent.HfCorrEdgeAll;
import com.ngc.brc.model.nonpersistent.HfCorrGeneInfo;
import com.ngc.brc.model.nonpersistent.HfCorrNodeAll;
import com.ngc.brc.services.lookup.LookupService;
import com.ngc.brc.util.common.HostFactorFormFields;
import com.ngc.brc.web.actions.download.Downloadable;
import com.ngc.brc.web.actions.download.helper.DownloadHelper;
import com.ngc.brc.web.delegates.search.common.BaseSearchDelegate;
import com.ngc.brc.web.delegates.search.common.hostFactor.HostFactorDataModelViewDelegate;
import com.ngc.brc.web.delegates.view.common.AjaxViewDelegate;
import com.ngc.brc.web.delegates.view.common.BaseViewDelegate;
import com.ngc.brc.web.forms.common.DownloadLTBoxBean;
import com.ngc.brc.web.forms.search.common.BaseBean;
import com.ngc.brc.web.forms.search.common.HostFactorCrossSearchBean;
import com.ngc.brc.web.forms.search.common.systemvirology.HostFactorBean;
import com.ngc.brc.web.util.breadcrumbs.BreadcrumbHelper;
import com.ngc.brc.web.util.enums.BreadcrumbType;

@Controller(value = "hf_dataModel")
@RequestMapping(value = "/hf_dataModel" + Constants.SERVLET_FILTER)
public class HostFactorDataModelController extends HostFactorBaseSearchController implements Downloadable {
	@Resource
	private LookupService lookupService;

	@Resource(name = "hostFactorDataModelSearchDelegate")
	private BaseSearchDelegate searchDelegate;
	@Resource(name = "hostFactorDataModelViewDelegate")
	private BaseViewDelegate viewDelegate;

	public static final String HTML_CLASS_HFC_DETAILS = "hfcDetails";
	public static final String HTML_CLASS_GENE_SIGNIFICANCE = "hfgsDetails";
	public static final String DOWNLOAD_SELECTED_HFC_IDS = "selectedHfcIds";

	// these are the ids selected in the UI - they are used to build the
	// detailed operation data
	private static final String HF_RESULTS_COMBO_IDS = "hfSelectedResultsComboIds";

	@Override
	public int getPageSize() {
		return 500;
	}

	// ItemSelectionLookupAction.registerClearSelectedIds is called in
	// ExperimentDetails.jsp before invoking this method - calling this method
	// submits up to two sets
	// of ids - one for the ids on the left side, and another for the ids on the
	// top row

	@RequestMapping(params = "method=getToolBar")
	public ModelAndView getToolBar(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute HostFactorBean hfBean, BindingResult bindingResult, ModelMap modelMap) throws Exception {

		// adds CommonSearchResultsOperations.jsp to the HostFactorResults.jsp:
		// this is required because the
		// selectionContext is generated on call of ajaxHFResults,
		// but the toolbars are placed early in the page and not part of the
		// ajax refreshing or appending more data - so without this they would
		// never have a selectionContext built for them

		String selectionContext = (String) BeanKeys.findRequestValue(request, BeanKeys.SELECTION_CONTEXT, false);
		// values required on CommonSearchResultsOperations.jsp
		modelMap.addAttribute(BeanKeys.SELECTABLE_COUNT, BeanKeys.findRequestValue(request, BeanKeys.CUSTOM_RECORD_COUNT, false));
		modelMap.addAttribute(BeanKeys.SELECTION_CONTEXT, selectionContext);
		modelMap.addAttribute(BeanKeys.ITEMS_SELECTED, "0");
		String expSeqId = (String) BeanKeys.findRequestValue(request, "expSeqId", false);
		modelMap.addAttribute("expSeqId", expSeqId);
		String resultMatrixUserDefId = (String) BeanKeys.findRequestValue(request,
				HostFactorFormFields.FORM_FIELD_RESULT_MATRIX_USER_DEF_ID, false);
		modelMap.addAttribute(HostFactorFormFields.FORM_FIELD_RESULT_MATRIX_USER_DEF_ID, resultMatrixUserDefId);
		String value = ((AjaxViewDelegate) getViewDelegate()).getUniqueIdentifier();
		if (null != value)
			modelMap.addAttribute(BeanKeys.UNIQUE_INDENTIFIER, value);
		else
			modelMap.addAttribute(BeanKeys.UNIQUE_INDENTIFIER, HTML_CLASS_HFC_DETAILS);
		request.setAttribute(BeanKeys.UNIQUE_INDENTIFIER, HTML_CLASS_HFC_DETAILS);
		// since the toolbar is dynamically built by JS loadHFResultsToolbar, we
		// need to send this back as
		// an attribute for the Toolbar.jsp to create the validateByFormClass
		// instead of the validateByForm
		// validateFormByClass (commonUtils.js will add Form to the "value" to
		// distinguish it from other classes on the page)

		// forward to CommonSearchResultsOperations.jsp
		return getBaseModelAndView(((HostFactorDataModelViewDelegate) getViewDelegate()).getToolBarPath(),
				getViewDelegate().getBeanName(), modelMap, hfBean);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(params = "method=SubmitForm")
	public ModelAndView SubmitForm(HttpServletRequest request, @ModelAttribute HostFactorBean baseBean,
			BindingResult bindingResult, ModelMap modelMap) throws Exception {
		String decorator = CommonHelper.getDecorator(request);
		String pageTo = request.getParameter("pageTo");

		List<HfExperiment> expList = null;
		String fromContext = request.getParameter(BreadcrumbHelper.FROM_CONTEXT);
		expList = lookupService.findDataByClassColumnName(HfExperiment.class, "id", new Long(request.getParameter("expSeqId")),
				false, false, null, null, false);

		HfExperiment experiment = null;
		/*
		 * List<Breadcrumb> breadcrumbs =
		 * BreadcrumbHelper.getPageBreadcrumb(request);
		 * 
		 * List bcs = new ArrayList<Breadcrumb>(); for (int i = 0; i <
		 * breadcrumbs.size() - 1; i++) { bcs.add(breadcrumbs.get(i)); }
		 */
		BreadcrumbHelper.setPageBreadcrumb(request, BreadcrumbHelper.getDetailBreadcrumbs(request, getBreadcrumbLabelBase()));
		// BreadcrumbHelper.setPageBreadcrumb(request, bcs);

		String selectionContext = (String) BeanKeys.findRequestValue(request, BeanKeys.SELECTION_CONTEXT, false);
		String threadedSearchKey = (String) BeanKeys.findRequestValue(request, BeanKeys.THREAD_SEARCH_KEY, false);
		if (StringUtils.hasLength(threadedSearchKey)) {
			// use the threaded search key, because the form is saved
			System.out.println("Found a threaded search key: " + threadedSearchKey);
			selectionContext = threadedSearchKey;
		}

		if (selectionContext == null) {
			selectionContext = CommonHelper.buildSelectionContext();
			baseBean.setSelectionContext(selectionContext);
		}

		if (null == pageTo || !Utils.isInteger(pageTo)) {
			pageTo = "1";
		}

		request.setAttribute("topColumnNames", getTopColumnNames(true,
				lookupService.isRNASeqExperiment(new Long(request.getParameter("expSeqId")).longValue())));

		request.setAttribute("isHFDM", true);

		List<String> selectedIds = ItemSelectionHelper.handleSelection(request, selectionContext).getIds();

		String idsToString = String.valueOf(selectedIds).replace("[", "").replace("]", "");

		modelMap.addAttribute(BeanKeys.SELECTED_ITEMS, idsToString);
		request.setAttribute(BeanKeys.SELECTED_ITEMS, idsToString);
		modelMap.addAttribute(BeanKeys.ITEMS_SELECTED, selectedIds.size());
		request.setAttribute(BeanKeys.ITEMS_SELECTED, selectedIds.size());

		request.setAttribute("selectModBar", true);
		modelMap.addAttribute("expSeqId", request.getParameter("expSeqId"));
		modelMap.addAttribute("moduleColor", request.getParameter("moduleColor"));
		request.setAttribute("expSeqId", request.getParameter("expSeqId"));
		request.setAttribute("moduleColor", request.getParameter("moduleColor"));
		request.setAttribute("showEnrichment", true);
		request.setAttribute(BeanKeys.UNIQUE_INDENTIFIER, HTML_CLASS_HFC_DETAILS);
		String tileValue = getViewDelegate().getResultTilePath();
		String beanName = getViewDelegate().getBeanName();

		List<HfCorrGeneInfo> genesList = lookupService.getFindModuleGenes(new Long(request.getParameter("expSeqId")).longValue(),
				request.getParameter("moduleColor"));
		List<String> genes = null;
		System.out.println("size of gene list: " + genesList.size());

		// Set up the pagination line and some parameters
		String sortBy = "gene";
		String sortOrder = "incr";
		int rowsPerPage = 50;

		int totalCount = genesList.size();

		String baseUrl = "hf_dataModel.spg?method=SubmitForm&decorator=" + decorator;
		baseUrl += "&moduleColor=";
		baseUrl += request.getParameter("moduleColor");
		baseUrl += "&expSeqId=";
		baseUrl += request.getParameter("expSeqId");

		request.setAttribute("showPaginationLine", "true");
		request.setAttribute(Constants.SORT_FIELD, (sortBy != null) ? sortBy : "");
		request.setAttribute(Constants.SORT_ORDER, (sortOrder != null) ? sortOrder : "");
		request.setAttribute(BeanKeys.SELECTION_CONTEXT, selectionContext);
		request.setAttribute(BreadcrumbHelper.FROM_CONTEXT, fromContext);
		request.setAttribute(Constants.SORT_URL, baseUrl);
		request.setAttribute(Constants.ROWS_PER_PAGE_KEY, rowsPerPage);
		request.setAttribute("pageBreadcrumbs", BreadcrumbHelper.getPageBreadcrumb(request));
		// pageable
		List<HfCorrGeneInfo> results = Utils.afterQueryPaging(genesList, Integer.parseInt(pageTo), rowsPerPage);
		// Set up the pagination line.
		CommonHelper.setPaginationProperties(totalCount, rowsPerPage, request, null, selectionContext);
		CommonHelper.setPagination(baseUrl, request, selectionContext);
		modelMap.addAttribute("resultList", results);
		request.setAttribute("resultList", results);

		ModelAndView modelView = super.getBaseModelAndView(tileValue, beanName, modelMap, baseBean);
		return modelView;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(params = "method=getModuleMetaData")
	private ModelAndView getModuleMetaData(HttpServletRequest request, @ModelAttribute HostFactorBean baseBean,
			BindingResult bindingResult, ModelMap modelMap) throws Exception {
		String decorator = CommonHelper.getDecorator(request);

		// Set up the pagination line and some parameters
		String sortBy = "gene";
		String sortOrder = "incr";
		int rowsPerPage = 50;
		String pageTo = request.getParameter("pageTo");

		StringBuffer params = new StringBuffer();
		String selectionContext = CommonHelper.buildSelectionContext();
		request.setAttribute(BeanKeys.SELECTION_CONTEXT, selectionContext);
		baseBean.setContext(selectionContext);

		String mDimension = request.getParameter("xval");
		String mColor = request.getParameter("yval");

		System.out.println("Module Color- xval from request: " + mDimension);
		System.out.println("module medata dimention/yval from request: " + mColor);
		System.out.println("expSeqId from request: " + request.getParameter("expSeqId"));

		if (null == pageTo || !Utils.isInteger(pageTo)) {
			pageTo = "1";
			selectionContext = CommonHelper.buildSelectionContext();
			request.setAttribute(BeanKeys.SELECTION_CONTEXT, selectionContext);
			baseBean.setContext(selectionContext);
		} else {
			selectionContext = (String) request.getAttribute(BeanKeys.SELECTION_CONTEXT);
			if (null == selectionContext || (String.valueOf(selectionContext)).length() < 1) {
				selectionContext = CommonHelper.buildSelectionContext();
			}
			request.setAttribute(BeanKeys.SELECTION_CONTEXT, selectionContext);
			baseBean.setContext(String.valueOf(selectionContext));
		}

		List<HfCorrGeneInfo> genes = lookupService
				.findModuleGeneSignificance(new Long(request.getParameter("expSeqId")).longValue(), mDimension, mColor);
		System.out.println("GenesSignificance map size: " + genes.size());
		request.setAttribute("topColumnNames", getGSTopColumnNames(true,
				lookupService.isRNASeqExperiment(new Long(request.getParameter("expSeqId")).longValue())));

		String baseUrl = "hf_dataModel.spg?method=getModuleMetaData&decorator=" + decorator;
		baseUrl += "&yval=";
		baseUrl += mColor;
		baseUrl += "&xval=";
		baseUrl += mDimension;
		baseUrl += "&expSeqId=";
		baseUrl += request.getParameter("expSeqId");

		request.setAttribute("showPaginationLine", "true");
		request.setAttribute(Constants.SORT_FIELD, (sortBy != null) ? sortBy : "");
		request.setAttribute(Constants.SORT_ORDER, (sortOrder != null) ? sortOrder : "");
		request.setAttribute(BeanKeys.SELECTION_CONTEXT, selectionContext);
		request.setAttribute(Constants.SORT_URL, baseUrl);
		request.setAttribute(Constants.ROWS_PER_PAGE_KEY, rowsPerPage);

		// pageable
		int totalCount = genes.size();
		// List<HfCorrGeneInfo> results = Utils.afterQueryPaging(genes,
		// Integer.parseInt(pageTo), rowsPerPage);
		List<HfCorrGeneInfo> results = Utils.afterQueryPaging(genes, Integer.parseInt(pageTo), rowsPerPage);
		// Set up the pagination line.
		CommonHelper.setPaginationProperties(totalCount, rowsPerPage, request, null, selectionContext);
		CommonHelper.setPagination(baseUrl, request, selectionContext);

		if (lookupService.isRNASeqExperiment(new Long(request.getParameter("expSeqId"))))
			modelMap.addAttribute("isRNASeq", "yes");
		modelMap.addAttribute("resultList", results);
		request.setAttribute("resultList", results);
		List<String> selectedIds = ItemSelectionHelper.handleSelection(request, selectionContext).getIds();

		String idsToString = String.valueOf(selectedIds).replace("[", "").replace("]", "");

		modelMap.addAttribute(BeanKeys.SELECTED_ITEMS, idsToString);
		request.setAttribute(BeanKeys.SELECTED_ITEMS, idsToString);
		modelMap.addAttribute(BeanKeys.ITEMS_SELECTED, selectedIds.size());
		request.setAttribute(BeanKeys.ITEMS_SELECTED, selectedIds.size());
		request.setAttribute("selectGeneSignificanceBar", true);
		modelMap.addAttribute("expSeqId", request.getParameter("expSeqId"));
		modelMap.addAttribute("moduleColor", mColor);
		modelMap.addAttribute("dimension", mDimension);
		request.setAttribute("expSeqId", request.getParameter("expSeqId"));
		request.setAttribute("moduleColor", mColor);
		request.setAttribute("dimension", mDimension);

		request.setAttribute(BeanKeys.UNIQUE_INDENTIFIER, HTML_CLASS_HFC_DETAILS);
		String tileValue = "page.hostFactor.dataModels.geneSignificance";
		;
		String beanName = getViewDelegate().getBeanName();

		ModelAndView modelView = super.getBaseModelAndView(tileValue, beanName, modelMap, baseBean);
		return modelView;

	}

	@SuppressWarnings("unchecked")
	@RequestMapping(params = "method=viewNetworkVisualization")
	private ModelAndView viewNetworkVisualization(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute HostFactorBean baseBean, BindingResult bindingResult, ModelMap modelMap) throws Exception {
		String decorator = CommonHelper.getDecorator(request);

		String expSeqId = request.getParameter("expSeqId");
		String mColor = request.getParameter("moduleColor");
		String isDMGL = request.getParameter("isDMGL");

		System.out.println("Module Color from request: " + mColor);
		System.out.println("isDMGL from request: " + isDMGL);
		System.out.println("expSeqId from request: " + expSeqId);

		List<HfCorrEdgeAll> edgesList = lookupService.getEdgedata(expSeqId, mColor);
		List<HfCorrNodeAll> nodeList = lookupService.getNodelist(expSeqId, mColor);
		int totalNodeNum = lookupService.getTotalNode(expSeqId, mColor);
		int totalEdgeNum = lookupService.getTotalEdge(expSeqId, mColor);

		System.out.println("Total number of nodes: " + totalNodeNum);
		System.out.println("Total number of edges: " + totalEdgeNum);

		System.out.println("Node size: " + nodeList.size());
		System.out.println("Edge size: " + edgesList.size());

		List<String> genes = null;
		// Map<String> nodes = null;
		List<Object[]> lst = null;

		JSONArray nodeJSONList = new JSONArray();
		JSONObject dataObj = null;
		JSONObject nodeObj = null;
		JSONObject edgeObj = null;
		for (HfCorrNodeAll n : nodeList) {
			// System.out.println("Found a Target for node: " + n + " :: source:
			// " + e.getSource()+" target: "+e.getTarget());
			dataObj = new JSONObject();
			nodeObj = new JSONObject();
			// nodeObj.put("id", n.getGeneSymbol());
			nodeObj.put("id", n.getProbeId());
			nodeObj.put("label", n.getGeneSymbol());

			nodeObj.put("pVal", n.getStatMeasure());
			nodeObj.put("member", n.getQuantMeasure());
			dataObj.put("data", nodeObj);
			nodeJSONList.add((dataObj));
		}

		modelMap.addAttribute("nodeList", nodeJSONList);
		modelMap.addAttribute("totalNodeNum", totalNodeNum);

		// Map<String, JSONArray> mMap = new HashMap<String, JSONArray>();
		JSONArray edgeJSONList = new JSONArray();
		// for (HfCorrNodeAll n : nodeList) {
		// // int i = 1;
		// for (HfCorrEdgeAll e : edgesList) {
		// if ((n.getGeneSymbol()).equalsIgnoreCase(e.getSource())) {
		// dataObj = new JSONObject();
		// edgeObj = new JSONObject();
		// edgeObj.put("id", e.getSource() + e.getTarget());
		// edgeObj.put("source", e.getSource());
		// if (null != e.getTarget()) {
		// edgeObj.put("target", e.getTarget());
		// edgeObj.put("weight", e.getWeight());
		// // edgeObj.put("weight",i+10);
		// dataObj.put("data", edgeObj);
		// edgeJSONList.add(dataObj);
		// }
		// }
		// }
		// }
		//

		for (HfCorrEdgeAll e : edgesList) {
			dataObj = new JSONObject();
			edgeObj = new JSONObject();
			edgeObj.put("id", e.getSource() + e.getTarget());
			edgeObj.put("source", e.getSource());
			if (null != e.getTarget()) {
				edgeObj.put("target", e.getTarget());
				edgeObj.put("weight", e.getWeight());
				// edgeObj.put("weight",i+10);
				dataObj.put("data", edgeObj);
				edgeJSONList.add(dataObj);
			}

		}

		/*
		 * if (edgeJSONList != null) {
		 * 
		 * @SuppressWarnings("rawtypes") Iterator it = edgeJSONList.iterator();
		 * while (it.hasNext()) { System.out.println(it.next().toString()); } }
		 */

		if (nodeJSONList != null) {
			@SuppressWarnings("rawtypes")
			Iterator it = nodeJSONList.iterator();
			while (it.hasNext()) {
				System.out.println(it.next().toString());
			}
		}

		modelMap.addAttribute("edgeList", edgeJSONList);
		modelMap.addAttribute("totalEdgeNum", totalEdgeNum);

		// System.out.println("CastHelper.stringArrayToJSONArray(nodes.toArray(new
		// String[nodes.size()])): "+
		// CastHelper.stringArrayToJSONArray(nodes.toArray(new
		// String[nodes.size()])).toString());
		// JSONArray nodesArray =
		// CastHelper.stringArrayToJSONArray(nodes.toArray(new
		// String[nodes.size()]));
		modelMap.addAttribute("expSeqId", request.getParameter("expSeqId"));
		modelMap.addAttribute("moduleColor", mColor);
		request.setAttribute("selectModBar", true);
		request.setAttribute("expSeqId", request.getParameter("expSeqId"));
		request.setAttribute("moduleColor", mColor);
		request.setAttribute("totalEdgeNum", totalEdgeNum);
		request.setAttribute("totalNodeNum", totalNodeNum);

		request.setAttribute(BeanKeys.UNIQUE_INDENTIFIER, HTML_CLASS_HFC_DETAILS);
		String tileValue = "page.hostFactor.dataModels.network";
		String beanName = getViewDelegate().getBeanName();

		ModelAndView modelView = super.getBaseModelAndView(tileValue, beanName, modelMap, baseBean);
		return modelView;

	}

	@Override
	@RequestMapping(params = "method=PrepForDownloadLightbox")
	public ModelAndView PrepForDownloadLightbox(HttpServletRequest request, HttpServletResponse response,
			DownloadLTBoxBean downloadBean, ModelMap modelMap) throws Exception {
		ModelAndView mv = super.setUpPrepForDownloadLightbox(request, response, downloadBean, modelMap,
				super.getRequestMapping());

		System.out.println("PrepForDownloadLightbox expSeqId from request: " + request.getParameter("expSeqId"));
		System.out.println("PrepForDownloadLightbox isHFC from request: " + request.getParameter("isHFC"));
		modelMap.addAttribute("expSeqId", request.getParameter("expSeqId"));
		request.setAttribute("expSeqId", request.getParameter("expSeqId"));
		request.setAttribute("isHFC", true);
		// request.setParameter("isHFC", "true");

		return mv;
	}

	@Override
	@RequestMapping(params = "method=PrepForGeneSignificanceDownloadLightbox")
	public ModelAndView PrepForGeneSignificanceDownloadLightbox(HttpServletRequest request, HttpServletResponse response,
			DownloadLTBoxBean downloadBean, ModelMap modelMap) throws Exception {
		ModelAndView mv = super.setUpPrepForDownloadLightbox(request, response, downloadBean, modelMap,
				super.getRequestMapping());

		String selectionContext = null;
		selectionContext = (String) request.getAttribute("selectionContext");
		selectionContext = (String) request.getParameter("selectionContext");
		selectionContext = (String) BeanKeys.findRequestValue(request, BeanKeys.SELECTION_CONTEXT, false);
		List<String> ids = null;
		IdList selectedIdList = ItemSelectionHelper.handleSelection(request, selectionContext);
		if (selectedIdList != null && selectedIdList.getIdCount() != 0) {
			ids = selectedIdList.getIds();
		}
		System.out.println("PrepForGeneSignificanceDownloadLightbox isHFGS from request: " + request.getParameter("isHFGS"));
		System.out.println("PrepForGeneSignificanceDownloadLightbox isDMEA from request: " + request.getParameter("isDMEA"));
		System.out.println("PrepForGeneSignificanceDownloadLightbox isDMEA from request: " + request.getAttribute("isDMEA"));
		System.out
				.println("PrepForGeneSignificanceDownloadLightbox entrezIDs from request: " + request.getParameter("entrezIDs"));

		System.out.println("PrepForGeneSignificanceDownloadLightbox termEnterzIDMap from request: "
				+ request.getParameter("termEnterzIDMap"));
		System.out.println("PrepForGeneSignificanceDownloadLightbox termEnterzIDMap from request attribute: "
				+ request.getAttribute("termEnterzIDMap"));
		System.out.println("PrepForGeneSignificanceDownloadLightbox DMEAMap from request: " + request.getParameter("DMEAMap"));
		System.out.println("PrepForGeneSignificanceDownloadLightbox xval from request: " + request.getParameter("xval"));
		System.out.println("PrepForGeneSignificanceDownloadLightbox yval from request: " + request.getParameter("yval"));
		System.out.println("PrepForGeneSignificanceDownloadLightbox xval from request: " + request.getParameter("moduleColor"));
		System.out.println("PrepForGeneSignificanceDownloadLightbox yval from request: " + request.getParameter("dimension"));
		System.out.println(
				"PrepForGeneSignificanceDownloadLightbox isGeneList from request: " + request.getParameter("isGeneList"));
		System.out.println("PrepForGeneSignificanceDownloadLightbox expSeqId from request: " + request.getParameter("expSeqId"));
		modelMap.addAttribute("expSeqId", request.getParameter("expSeqId"));
		request.setAttribute("expSeqId", request.getParameter("expSeqId"));
		modelMap.addAttribute("selectionContext", request.getParameter("selectionContext"));
		if ("true".equalsIgnoreCase((String) request.getAttribute("isCTSNL"))
				|| "true".equalsIgnoreCase((String) request.getParameter("isCTSNL"))) {
			modelMap.addAttribute("isCTSNL", true);
			request.setAttribute("isCTSNL", true);
		}
		if ("true".equalsIgnoreCase((String) request.getAttribute("isCTSGL"))
				|| "true".equalsIgnoreCase((String) request.getParameter("isCTSGL"))) {
			modelMap.addAttribute("isCTSGL", true);
			request.setAttribute("isCTSGL", true);
		}
		if ("true".equalsIgnoreCase((String) request.getAttribute("isHFGS"))
				|| "true".equalsIgnoreCase((String) request.getParameter("isHFGS"))) {
			modelMap.addAttribute("isHFGS", true);
			request.setAttribute("isHFGS", true);
		}
		if ("true".equalsIgnoreCase((String) request.getAttribute("isDMEA"))
				|| "true".equalsIgnoreCase((String) request.getParameter("isDMEA"))) {
			Map<String, IdList> cache = (Map<String, IdList>) request.getSession().getAttribute("cachedSelectedIds");
			IdList idList = null;
			for (Map.Entry<String, IdList> entry : cache.entrySet()) {
				if (null != entry.getValue() && (entry.getValue().toString().length() != 0)) {
					idList = entry.getValue();
					List<String> id = idList.getIds();
					modelMap.addAttribute("dmEntrezIDs", id);
					request.setAttribute("dmEntrezIDs", id);
					// request.setAttribute("termEnterzIDMap", termEnterzIDMap);
					break;
				}
			}

			modelMap.addAttribute("DMEAMap", request.getParameter("DMEAMap"));
			request.setAttribute("DMEAMap", request.getParameter("DMEAMap"));
			modelMap.addAttribute("entrezIDs", request.getParameter("entrezIDs"));
			request.setAttribute("termEnterzIDMap", request.getParameter("termEnterzIDMap"));
			modelMap.addAttribute("termEnterzIDMap", request.getParameter("termEnterzIDMap"));
			request.setAttribute("entrezIDs", request.getParameter("entrezIDs"));
			modelMap.addAttribute("isDMEA", true);

			request.setAttribute("isDMEA", true);
		} else {
			modelMap.addAttribute("isGeneList", true);
			request.setAttribute("isGeneList", true);
			// request.setParameter("isGeneList", true);
		}

		request.setAttribute("isDMNV", request.getAttribute("isDMNV"));
		request.setAttribute("moduleColor", request.getParameter("moduleColor"));
		// request.setAttribute("DMEAMap", request.getParameter("DMEAMap"));

		request.setAttribute("dimension", request.getParameter("dimension"));

		request.setAttribute("selectionContext", request.getParameter("selectionContext"));

		return mv;
	}

	@Override
	@RequestMapping(params = "method=SubmitDownloadOptions")
	public ModelAndView SubmitDownloadOptions(HttpServletRequest request, HttpServletResponse response,
			DownloadLTBoxBean downloadBean, BindingResult bindingResult, ModelMap modelMap) throws Exception {

		modelMap.addAttribute("expSeqId", request.getParameter("expSeqId"));
		System.out.println("SubmitDownloadOptions isHFGS from request: " + request.getParameter("isHFGS"));
		System.out.println("SubmitDownloadOptions isDMEA from request: " + request.getParameter("isDMEA"));
		System.out.println("SubmitDownloadOptions termEnterzIDMap from request: " + request.getParameter("termEnterzIDMap"));
		System.out.println("SubmitDownloadOptions entrezIDs from request: " + request.getParameter("entrezIDs"));
		System.out.println("SubmitDownloadOptions DMEAMap from request: " + request.getParameter("DMEAMap"));
		System.out.println("SubmitDownloadOptions xval from request: " + request.getParameter("moduleColor"));
		System.out.println("SubmitDownloadOptions yval from request: " + request.getParameter("dimension"));
		System.out.println("SubmitDownloadOptions isGeneList from request: " + request.getParameter("isGeneList"));
		System.out.println("SubmitDownloadOptions expSeqId from request: " + request.getParameter("expSeqId"));

		request.setAttribute("expSeqId", request.getParameter("expSeqId"));
		request.setAttribute("termEnterzIDMap", request.getParameter("termEnterzIDMap"));
		request.setAttribute("entrezIDs", request.getParameter("entrezIDs"));
		request.setAttribute("modColor", request.getParameter("modColor"));
		request.setAttribute("metaData", request.getParameter("metaData"));
		request.setAttribute("DMEAMap", request.getParameter("DMEAMap"));
		request.setAttribute("selectionContext", request.getParameter("selectionContext"));
		String expId = request.getParameter("expSeqId");
		if (!isNumeric(expId)) {
			expId = request.getParameter("dmEntrezIDs").substring(1, 4);
			request.setAttribute("expSeqId", expId);
		}
		ModelAndView mv = null;
		if ("true".equalsIgnoreCase((String) request.getAttribute("isHFGS"))
				|| "true".equalsIgnoreCase((String) request.getParameter("isHFGS"))
				|| "true".equalsIgnoreCase((String) request.getAttribute("isDMEA"))
				|| "true".equalsIgnoreCase((String) request.getParameter("isDMEA"))
				|| "true".equalsIgnoreCase((String) request.getAttribute("isGeneList"))
				|| "true".equalsIgnoreCase((String) request.getParameter("isGeneList"))
				|| "true".equalsIgnoreCase((String) request.getAttribute("isCTSGL"))
				|| "true".equalsIgnoreCase((String) request.getParameter("isCTSGL"))
				|| "true".equalsIgnoreCase((String) request.getAttribute("isCTSNL"))
				|| "true".equalsIgnoreCase((String) request.getParameter("isCTSNL")))
			mv = DownloadSpecialTabDelimited(downloadBean, request, response, modelMap);
		else
			mv = getDownloadHelper().downloadFile(request, response, Constants.HCFFILES_LOCATION,
					request.getParameter("expSeqId") + "_IntramodularConnectivity.txt.gz", "json");
		// request.setAttribute("isHFC", true);
		return mv;
	}

	@Override
	@RequestMapping(params = "method=DownloadSpecialTabDelimited", method = RequestMethod.POST)
	public ModelAndView DownloadSpecialTabDelimited(DownloadLTBoxBean downloadBean, HttpServletRequest request,
			HttpServletResponse response, ModelMap modelMap) throws Exception {
		System.out.println("DownloadSpecialTabDelimited isHFGS: " + request.getParameter("isHFGS"));
		System.out.println("DownloadSpecialTabDelimited isDMEA: " + request.getParameter("isDMEA"));
		System.out.println("DownloadSpecialTabDelimited termEnterzIDMap: " + request.getParameter("termEnterzIDMap"));
		System.out.println("DownloadSpecialTabDelimited entrezIDs: " + request.getParameter("entrezIDs"));
		System.out.println("DownloadSpecialTabDelimited DMEAMap: " + request.getParameter("DMEAMap"));
		System.out.println("DownloadSpecialTabDelimited isGeneList: " + request.getAttribute("isGeneList"));
		System.out.println("DownloadSpecialTabDelimited expSeqId from request: " + request.getParameter("expSeqId"));
		System.out.println("DownloadSpecialTabDelimited moduleColor from request: " + request.getParameter("moduleColor"));
		System.out.println("DownloadSpecialTabDelimited dimension from request: " + request.getParameter("dimension"));
		String selectionContext = (String) BeanKeys.findRequestValue(request, BeanKeys.SELECTION_CONTEXT, false);
		List<HfCorrGeneInfo> resultList = (List<HfCorrGeneInfo>) request.getSession().getAttribute("resultsTable");
		modelMap.addAttribute("resultList", resultList);
		if (null != resultList)
			System.out.println("SubmitDownloadOptions resultList IS NOT NULL ");
		String submittedFileName = request.getParameter(DownloadConstants.DOWNLOAD_FILE_NAME);
		String fileName = ZipUtils.checkFileNameAndExtension(submittedFileName, FileExtension.TEXT);

		return getDownloadHelper().writeOutputFile(request, response, fileName, "text/plain", null);
	}

	// @Override
	@RequestMapping(params = "method=DownloadSpecialTabDelimited1", method = RequestMethod.POST)
	public ModelAndView DownloadSpecialTabDelimited1(DownloadLTBoxBean downloadBean, HttpServletRequest request,
			HttpServletResponse response, ModelMap modelMap) throws Exception {

		NameTwoValues nv = new NameTwoValues(this.getViewDelegate().getPageHeading(downloadBean), null);
		modelMap.addAttribute(Constants.STORE_METRIC_DATA, nv);
		return super.setUpDownloadSpecialTabDelimited(downloadBean, request, response, modelMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	/*
	 * override here, because we dont want the previousUrl to go to this action
	 * - but to the parent action
	 */
	public Map buildUserInputsForThreadedDownload(HttpServletRequest request) {

		String selectionContext = (String) BeanKeys.findRequestValue(request, BeanKeys.SELECTION_CONTEXT, false);
		HostFactorBean hfBean = (HostFactorBean) ResultsAndSelectedIdsCache.getBean(request, selectionContext);

		// this is logic is not guaranteed, but will do for now
		StringBuilder searchValue = new StringBuilder();
		searchValue.append("expAccession=");
		searchValue.append(hfBean.getExpAccession());

		String previousUrl = BreadcrumbHelper.getFuzzyMatchDetailBreadcrumb(request, searchValue.toString(),
				BreadcrumbType.Detail);

		IdList ids = ItemSelectionHelper.handleSelection(request, selectionContext);
		List<String> hfSelectedResultsComboIds = ids.getIds();
		// cleaned list - without selected patterns
		List<String> cleanSelectedResultsComboIds = new ArrayList<String>();

		List<String> selectedHfcIds = new ArrayList<String>();

		for (String id : hfSelectedResultsComboIds) {

			id = id.trim();
			// this data is mixed patterns and selected ids
			if (!id.contains(Constants.SPACE) && id.contains(Constants.DASH)) {

				int hfcIdPosition = id.indexOf(Constants.DASH);
				String hfcId = id.substring(0, hfcIdPosition);
				hfcId = hfcId.trim();
				if (hfcId.length() > 1) {
					if (!selectedHfcIds.contains(hfcId))
						selectedHfcIds.add(hfcId);
				}
				cleanSelectedResultsComboIds.add(id);
			}

		}

		Map userInputs = ClassUtils.getPropertiesAsKVPMap(hfBean);
		userInputs.put(DOWNLOAD_SELECTED_HFC_IDS, selectedHfcIds);
		// userInputs.put(HF_RESULTS_COMBO_IDS, cleanSelectedResultsComboIds);

		userInputs.put(BeanKeys.PREVIOUS_URL, Constants.APP_PREFIX + previousUrl);
		request.setAttribute(DownloadHelper.NO_PROGRESS_BAR, DownloadHelper.NO_PROGRESS_BAR);
		return userInputs;
	}

	@SuppressWarnings("unchecked")
	@Override
	// This creates a header table for the Excel download tab "Operations"
	// listing the Experiment and Bioset Names:
	public List<NameValue> getOperationsData(String experimentId, Map userInputs) {

		List<NameValue> nvList = new ArrayList<NameValue>();
		List<String> retrieveColumnNames = new ArrayList<String>();
		retrieveColumnNames.add("expUserDefName");
		retrieveColumnNames.add("biosetMetadata.bmUserDefId");

		List<CriteriaOperators> operators = new ArrayList<CriteriaOperators>();

		CriteriaAliasFetch aliasFetchModes = new CriteriaAliasFetch();
		aliasFetchModes.getAliases().add(new CriteriaAlias("assays", "assay"));
		aliasFetchModes.getAliases().add(new CriteriaAlias("assay.biosetMetadatas", "biosetMetadata"));

		CriteriaOperators op1 = new CriteriaOperators("id", new Long(experimentId), CriteriaOperators.EQUAL);
		operators.add(op1);

		// the selected rows in the Host Factor Results table - these are
		// SvBiosetList ids
		List<String> selectedBiosetIds = (List<String>) userInputs.get(HostFactorReagentDetails.DOWNLOAD_SELECTED_BIOSET_IDS);
		if (selectedBiosetIds != null && selectedBiosetIds.size() > 0) {

			aliasFetchModes.getAliases().add(new CriteriaAlias("biosetMetadata.biosetList", "biosetList"));
			CriteriaOperators op2 = new CriteriaOperators("biosetList.id", CastHelper.stringListToListOfLongs(selectedBiosetIds),
					CriteriaOperators.IN);
			operators.add(op2);
		}

		List<Object[]> data = getSearchDelegate().getLookupService().findData(HfExperiment.class, retrieveColumnNames, operators,
				false, false, false, aliasFetchModes);

		List<String> experimentNames = new ArrayList<String>();
		List<String> biosetNames = new ArrayList<String>();

		for (Object[] item : data) {
			experimentNames.add((String) item[0]);
			String biosetName = (String) item[1];
			if (!biosetNames.contains(biosetName))
				biosetNames.add(biosetName);
		}

		experimentNames = CastHelper.removeDuplicates(experimentNames);

		String experimentStr = CastHelper.listToStringNoEmpty(experimentNames);
		String biosetStr = CastHelper.listToStringNoEmpty(biosetNames, Constants.REGEX_NEWLINE);

		nvList.add(new NameValue("Experiment Selections", experimentStr));
		nvList.add(new NameValue("Bioset Selections", biosetStr));
		return nvList;
	}

	@Override
	public List getOperationsDetailsData(String experimentId, Map userInputs) {

		List<String> retrieveColumnNames = new ArrayList<String>();

		retrieveColumnNames.add("biosetMetadata.id");
		retrieveColumnNames.add("biosetMetadata.resultMatrixUserDefId");

		List<CriteriaOperators> operators = new ArrayList<CriteriaOperators>();
		CriteriaAliasFetch aliasFetchModes = new CriteriaAliasFetch();
		aliasFetchModes.getAliases().add(new CriteriaAlias("assays", "assay"));
		aliasFetchModes.getAliases().add(new CriteriaAlias("assay.biosetMetadatas", "biosetMetadata"));

		CriteriaOperators op1 = new CriteriaOperators("id", new Long(experimentId), CriteriaOperators.EQUAL);
		operators.add(op1);

		// the selected rows in the Host Factor Results table - these are
		// SvBiosetList ids
		List<String> selectedBiosetIds = (List<String>) userInputs.get(HostFactorReagentDetails.DOWNLOAD_SELECTED_BIOSET_IDS);
		if (selectedBiosetIds != null && selectedBiosetIds.size() > 0) {

			aliasFetchModes.getAliases().add(new CriteriaAlias("biosetMetadata.biosetList", "biosetList"));
			CriteriaOperators op2 = new CriteriaOperators("biosetList.id", CastHelper.stringListToListOfLongs(selectedBiosetIds),
					CriteriaOperators.IN);
			operators.add(op2);
		}

		List<Object[]> data = getSearchDelegate().getLookupService().findData(HfExperiment.class, retrieveColumnNames, operators,
				false, false, false, aliasFetchModes);

		List<Long> svBiosetInformationIds = new ArrayList<Long>();
		List<String> resultMatrixUserDefIds = new ArrayList<String>();

		for (Object[] item : data) {
			svBiosetInformationIds.add((Long) item[0]);
			resultMatrixUserDefIds.add((String) item[1]);
		}

		resultMatrixUserDefIds = CastHelper.removeDuplicates(resultMatrixUserDefIds);
		String biosetIdStr = CastHelper.listToStringNoEmpty(svBiosetInformationIds);

		// list of SV_BIOSET_LIST.id-reagentAnnotation.id - use these to get the
		// data
		List<String> hfSelectedResultsComboIds = (List<String>) userInputs.get(HF_RESULTS_COMBO_IDS);
		return super.getHostFactorReagentDetailsDownloadData(resultMatrixUserDefIds, biosetIdStr, hfSelectedResultsComboIds);
	}

	@Override
	public BaseBean buildFormFromPreviousSearch(BaseBean baseBean, HttpServletRequest request, String fromContext) {
		return null;
	}

	@Override
	public void copyPropertiesFromArg1ToArg2(HostFactorCrossSearchBean storedBean, HostFactorCrossSearchBean currentPageBean) {
	}

	@Override
	public StringBuffer generateTabDownloadData(Object data, List displayColumns, Map userInputs) {

		StringBuffer tabFile = new StringBuffer();

		List<String> columnNamesOnly = new ArrayList<String>();
		for (AjaxDisplayColumns columnData : (List<AjaxDisplayColumns>) displayColumns) {
			tabFile.append(columnData.getDisplayName()).append(Constants.REGEX_TAB);
			columnNamesOnly.add(columnData.getDisplayName());
		}

		tabFile.append(Constants.REGEX_NEWLINE);

		String bodyData = ((AjaxViewDelegate) getViewDelegate()).buildDisplayTable(data, columnNamesOnly, false, userInputs);
		tabFile.append(bodyData);
		return tabFile;
	}

	// returns the selected IDs - for the experiment page, these ids can be
	// used, but for other pages, the experiment ids have to be retrieved using
	// these ids
	@Override
	public List<String> getSelectedIds(HttpServletRequest request, Map userInputs) {

		String expAccId = (String) userInputs.get(HostFactorFormFields.FORM_FIELD_EXPERIMENT_ID);
		List<String> ids = new ArrayList<String>();
		if (StringUtils.hasLength(expAccId))
			ids.add(expAccId);
		else
			super.sendDeveloperEmail(getClass().getName() + ".getSelectedIds",
					"No value found for " + HostFactorFormFields.FORM_FIELD_EXPERIMENT_ID, SystemErrorCodes.HFR_100);

		return ids;
	}

	@Override
	public Object getRecordCount(IdList ids, Map presentationParameters) {
		return ids.getIdCount();
	}

	@Override
	public void prepareDataFromQuickSearch(BaseBean baseBean, HttpServletRequest request) throws Exception {
	}

	private void buildIdListsFromSourceContext(HttpServletRequest request, String hfBiosetPatternsContext,
			HostFactorBean baseBean) {

		IdList patternIdList = ResultsAndSelectedIdsCache.getSelectedIds(request, hfBiosetPatternsContext);
		IdList biosetIds = patternIdList.getSecondaryIds();

		// ids from the left side of the page
		if (patternIdList.getIds() != null) {
			String measurementPattern = CastHelper.listToStringNoEmpty(patternIdList.getIds());
			baseBean.setMeasurementPattern(measurementPattern);
		}

		// ids from the inner part of the table
		if (biosetIds != null) {
			String biosetIdsStr = CastHelper.listToStringNoEmpty(biosetIds.getIds());
			baseBean.setBiosetIds(biosetIdsStr);
		}
	}

	public static List<AjaxDisplayColumns> getTopColumnNames(boolean isHTML, boolean isRNASeq) {

		List<AjaxDisplayColumns> dataColumnNames = new ArrayList<AjaxDisplayColumns>();

		List<AjaxDisplayColumns> columnNames = new ArrayList<AjaxDisplayColumns>();

		String checkbox = SelectBox.buildCheckBox(null, HTML_CLASS_HFC_DETAILS, SelectBox.NAME_ALL_ITEMS, Constants.EMPTY_STRING,
				null);
		// BRC-11720
		if (isRNASeq)
			columnNames.add(
					new AjaxDisplayColumns("Genes", null, "width: 50px; vertical-align: middle; white-space: nowrap;", isHTML));
		else
			columnNames.add(new AjaxDisplayColumns("Probe ID", null, "width: 50px; vertical-align: middle; white-space: nowrap;",
					isHTML));
		columnNames.add(
				new AjaxDisplayColumns("Gene Symbol", null, "width: 50px; vertical-align: middle; white-space: nowrap;", isHTML));
		columnNames.add(
				new AjaxDisplayColumns("Gene Name", null, "width: 50px; vertical-align: middle; white-space: nowrap;", isHTML));
		columnNames.add(new AjaxDisplayColumns("Entrez Gene ID", null,
				"width: 50px; vertical-align: middle; white-space: nowrap;", isHTML));
		columnNames.add(new AjaxDisplayColumns("GenBank Accession", null,
				"width: 50px; vertical-align: middle; white-space: nowrap;", isHTML));

		return columnNames;
	}

	public static List<AjaxDisplayColumns> getGSTopColumnNames(boolean isHTML, boolean isRNASeq) {

		List<AjaxDisplayColumns> dataColumnNames = new ArrayList<AjaxDisplayColumns>();

		List<AjaxDisplayColumns> columnNames = new ArrayList<AjaxDisplayColumns>();

		String checkbox = SelectBox.buildCheckBox(null, HTML_CLASS_HFC_DETAILS, SelectBox.NAME_ALL_ITEMS, Constants.EMPTY_STRING,
				null);

		// BRC-11720
		if (isRNASeq)
			columnNames.add(
					new AjaxDisplayColumns("Genes", null, "width: 40px; vertical-align: middle; white-space: nowrap;", isHTML));
		else
			columnNames.add(new AjaxDisplayColumns("Probe ID", null, "width: 40px; vertical-align: middle; white-space: nowrap;",
					isHTML));

		columnNames.add(new AjaxDisplayColumns("Significance", null, "width: 40px; vertical-align: middle; white-space: nowrap;",
				isHTML));

		columnNames.add(
				new AjaxDisplayColumns("Gene Symbol", null, "width: 50px; vertical-align: middle; white-space: nowrap;", isHTML));
		columnNames.add(
				new AjaxDisplayColumns("Gene Name", null, "width: 50px; vertical-align: middle; white-space: nowrap;", isHTML));
		columnNames.add(new AjaxDisplayColumns("Entrez Gene ID", null,
				"width: 50px; vertical-align: middle; white-space: nowrap;", isHTML));
		columnNames.add(new AjaxDisplayColumns("GenBank Accession", null,
				"width: 50px; vertical-align: middle; white-space: nowrap;", isHTML));

		return columnNames;
	}

	@Override
	@RequestMapping(params = "method=SendReactomeData")
	public ModelAndView SendReactomeData(DownloadLTBoxBean downloadBean, HttpServletRequest request, HttpServletResponse response,
			ModelMap modelMap) throws Exception {
		return setupSendReactomeData(downloadBean, request, response, modelMap);
	}

	@Override
	// This is for downloading data only!!! The Reactome send data to some
	// server is a separate method
	public ModelAndView DownloadReactomeData(DownloadLTBoxBean downloadBean, HttpServletRequest request,
			HttpServletResponse response, ModelMap modelMap) throws Exception {
		return setupDownloadReactomeData(downloadBean, request, response, modelMap);
	}

	public BaseSearchDelegate getSearchDelegate() {
		return searchDelegate;
	}

	public void setSearchDelegate(BaseSearchDelegate searchDelegate) {
		this.searchDelegate = searchDelegate;
	}

	public BaseViewDelegate getViewDelegate() {
		return viewDelegate;
	}

	public void setViewDelegate(BaseViewDelegate viewDelegate) {
		this.viewDelegate = viewDelegate;
	}

	// page heading
	private void buildPageHeading(HttpServletRequest request, String value) {

		StringBuilder pageHeading = new StringBuilder();
		pageHeading.append(getBreadcrumbLabelBase());
		pageHeading.append(Constants.SPACE);
		pageHeading.append(Constants.LEFT_CLOSE);
		pageHeading.append(value);
		pageHeading.append(Constants.RIGHT_CLOSE);

		request.setAttribute(BeanKeys.PAGE_HEADING, pageHeading);
	}

	protected String getBreadcrumbLabelBase() {
		return "Data Model one";
	}

	private boolean isNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}
}

