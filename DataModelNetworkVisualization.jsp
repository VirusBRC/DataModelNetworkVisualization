<%@ include file="/WEB-INF/contents/pages/Include/springMVCTagLibs.jsp"%> 
<!-- For MS IE/Edge compatibility:-->
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<link rel="stylesheet" type="text/css" media="all"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.css" />

<script src="https://marvl.infotech.monash.edu/webcola/cola.v3.min.js"></script>
<script src="https://rawgit.com/tgdwyer/WebCola/master/WebCola/cola.min.js"></script>
<script src="https://rawgit.com/cytoscape/cytoscape.js-cola/master/cytoscape-cola.js"></script>

<script type="text/javascript"
	src="/brc/javascripts/dataModel/cytoscape-cola.js"></script>
	
<script src="https://cdnjs.cloudflare.com/ajax/libs/qtip2/2.2.0/jquery.qtip.min.js"></script>
<link href="https://cdnjs.cloudflare.com/ajax/libs/qtip2/2.2.0/jquery.qtip.min.css" rel="stylesheet" type="text/css" />
<script src="https://cdn.rawgit.com/cytoscape/cytoscape.js-qtip/2.7.0/cytoscape-qtip.js"></script>
  <script src="https://code.jquery.com/jquery-1.12.4.js"></script> 
   <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script> 
<link href="/brc/css/style.css" rel="stylesheet" type="text/css" />

<div id="main-content">

	<div id="content">

		<div class="columns">
			<div class="stdColumn" style="width: 95%">
				<h1 class="page-title">Data Model Network Visualization</h1>
			</div>
		</div>

		<div id="dataModelsTable1" style="float: left; width: 100%;">
			<div id="search-form-container" style="padding: 5px">
				<div class="searchResultContainer columnStd" style="width: 98%;">

					<form:form method="POST" commandName="${dynamicJSPBeanName}"
						action="${searchActionPath}" id="hostFactorDataModelSearchBean"
						cssClass="${uniqueIDC}Form" onsubmit="return false">
						<input type="hidden" name="decorator" value="${decorator}" />
						<input type="hidden" name="expSeqId" value="${expSeqId}" />
						<input type="hidden" name="moduleColor" value="${moduleColor}" />
						<input type="hidden" name="isDMNV" value="true" />
						<input type="hidden" name="fromContext"
							value="${selectionContext}" />
						<input type="hidden" value="" name="method" />
						<input type="hidden" name="selectedItemsList"
							id="${uniqueIDC}selectedItemsList" value="" />
						<form:hidden path="expAccession" />
						<form:hidden path="resultMatrixUserDefId" />

						<div style="width: 100%; padding-bottom: 20px;" id="toolBar">
							<p class="example_note" style="margin-top: 10px">You are viewing your chosen module as a network in which the nodes are host factors and the edges represent the strength of correlation (adjacency) between the expression of the respective nodes.  All nodes in the chosen module are always displayed in the default network. The default network shows up to a total of 2000 edges. For modules with less than 2000 edges, all edges for all nodes are displayed. For modules that have more edges than 2000, the strongest edges (highest adjacency values) are shown for each node.</p>
						</div>
						<div>
							<jsp:include
								page="/WEB-INF/contents/pages/SearchResult/CommonSearchResultsOperations.jsp"
								flush="true">
								<jsp:param name="showDisplaySetting" value="false" />
								<jsp:param name="toolbar" value="DM_Network_Visualization" />
								<jsp:param name="formClass" value="${uniqueIDC}" />
								<jsp:param name="selectModBar" value="true" />
								<jsp:param name="selectItemBar" value="false" />
								<jsp:param name="moduleColor" value="${moduleColor}" />
								<jsp:param name="uniqueIDC" value="${uniqueIDC}" />
							</jsp:include>
						</div>
						<div class=columns>
    					 <div class="columnStd" style="width:85%; margin-right:0%; margin-top:0%">	
							<div id="cy"
							style="position: relative; width: 99%; height: 750px;" />
							<script>
							var nodesList = '${nodeList}';
							var nodeValues = jQuery.parseJSON(nodesList);
							var edgesList = '${edgeList}';
							var edgeValues = jQuery.parseJSON(edgesList);
							var modColor = '${moduleColor}';
							var totalEdge = '${totalEdgeNum}';
							var totalNode = '${totalNodeNum}';
							var cy = cytoscape({
								  container: document.getElementById('cy'),
								  elements: {
								    nodes: nodeValues,
								    edges: edgeValues
								  },
								 style: [
			       				 {
						          selector: 'node',
						          style: {
						           'background-color': modColor,
						            'label': 'data(label)',
						        	'border-width': 0.5,
						            'width': 10,
						            'height': 10,
						            'font-size': 10,
						        	
 						         
						          }
					        	},
						        {
						          selector: 'edge',
						          style: {
						            'width':'mapData(weight, 0, 1, 0, 10)',
						          	'line-color':'#ccc',
	 					            'target-arrow-color': '#ccc',
						            'target-arrow-shape': 'triangle',
						            'target-endpoint': 'outside-to-node',
						            'opacity': 0.6
						          }
						        },
						        {
						          selector: 'node.highlight',
						          style: {
						            'background-color': 'data(module)'
						            
						          }
						        },
						        
						        {
						          selector: 'node.semitransp',
						          style: { opacity: '0.2' }
						        },
						        {
						          selector: 'edge.highlight',
						          style: { 
							          'line-color': '#e60',
							          'target-arrow-color': '#e60',
							          'label': 'data(weight)',
							          
							          'font-size': '10',
							      		'color': '#555'
						           }
						        },
						        {
						          selector: 'edge.hidden',
						          style: { opacity: '0.0' }
						        },
						        {
						          selector: 'edge.semitransp',
						          style: { opacity: '0.2' }
						        }
						      ],
						      layout: {
						       name:'cola', 
						       randomize: false, // kose-bilkent will randomize node positions
						       //refresh: 5, // number of ticks per frame; higher is faster but more jerky
						       //maxSimulationTime: 2000, // max length in ms to run the layout
					           animate: false,
					           ungrabifyWhileSimulating: false, // so you can't drag nodes during layout
					           fit: true, // on every layout reposition of nodes, fit the viewport
					       	   nodeOverlap: 0,
						       avoidOverlap: true,
							
						      },
						 
						      
							});
							
// 						cy.nodes(function(element){
// 					        if( element.isNode() && element.degree()<1){
// 					            cy.remove(element)
// 					        }
					        
// 					    });
	
						console.log("node size: " + cy.nodes().length);
						console.log("edge size: " + cy.edges().length);
						
					   cy.$('node').qtip({
					   		
						  content: function(){ 
						  			 
						  			let mem = "; Membership: " + this.data('member');
						  			let pval= "; pvalue=" + this.data('pVal');
						  			let con = "; Connection: " + this.connectedEdges().size();
						  			let tip= this.data('label').concat(mem).concat(pval).concat(con); 
						  			console.log("connection:" + tip);
						  			return tip; },
						  position: {
						    my: 'top center',
						    at: 'bottom center'
						  },
						  style: {
						    classes: 'qtip-bootstrap',
						    tip: {
						      width: 4,
						      height: 3
						    }
						  }
						});     
					    
					     var selected;
					     cy.on("tap", "node", function(e) {
					     var sel = e.target;
					   
					     if (sel === selected) {
					        cy.elements().removeClass('semitransp');
					        cy.elements().removeClass('highlight');
					        return;
					     }
					     selected = sel;
					     cy.elements().removeClass('semitransp');
					     cy.elements().removeClass('highlight');
					     cy
					       .elements()
					       .difference(sel.neighborhood())
					       .not(sel)
					       .addClass('semitransp');
					     sel
					       .addClass('highlight')
					       .neighborhood()
					       .addClass('highlight');
					   
					   		
					   });
					 	 
					     cy.on("tap", "edge", function(e) {
					     var sel = e.target;
					   
					     if (sel === selected) {
					        cy.elements().removeClass('semitransp');
					        cy.elements().removeClass('highlight');
					        return;
					     }
					     selected = sel;
					     cy.elements().removeClass('semitransp');
					     cy.elements().removeClass('highlight');
					     cy
					       .elements()
					       .difference(sel.connectedNodes())
					       .not(sel)
					       .addClass('semitransp');
					     sel
					       .addClass('highlight')
					       .connectedNodes()
					       .addClass('highlight');
					   		
					   });
					 		
							var defaults = {
							  zoomFactor: 0.05, // zoom factor per zoom tick
							  zoomDelay: 45, // how many ms between zoom ticks
							  minZoom: 0.1, // min zoom level
							  maxZoom: 10, // max zoom level
							  fitPadding: 50, // padding when fitting
							  panSpeed: 10, // how many ms in between pan ticks
							  panDistance: 10, // max pan distance per tick
							  panDragAreaSize: 75, // the length of the pan drag box in which the vector for panning is calculated (bigger = finer control of pan speed and direction)
							  panMinPercentSpeed: 0.25, // the slowest speed we can pan by (as a percent of panSpeed)
							  panInactiveArea: 8, // radius of inactive area in pan drag box
							  panIndicatorMinOpacity: 0.5, // min opacity of pan indicator (the draggable nib); scales from this to 1.0
							  zoomOnly: false, // a minimal version of the ui only with zooming (useful on systems with bad mousewheel resolution)
							  fitSelector: undefined, // selector of elements to fit
							  animateOnFit: function() { // whether to animate on fit
							    return false;
							  },
							  fitAnimationDuration: 1000, // duration of animation on fit
							
							  // icon class names
							  sliderHandleIcon: 'fa fa-minus',
							  zoomInIcon: 'fa fa-plus',
							  zoomOutIcon: 'fa fa-minus',
							  resetIcon: 'fa fa-expand'
							};
							
							
							cy.panzoom(defaults);
							
							function b64toBlob(dataURI) {
							  var byteString = atob(dataURI.split(',')[1]);
							  var ab = new ArrayBuffer(byteString.length);
							  var ia = new Uint8Array(ab);
							
							  for (var i = 0; i < byteString.length; i++) {
							    ia[i] = byteString.charCodeAt(i);
							  }
							  return new Blob([ab], {
							    type: 'image/jpeg'
							  });
							}
						function dnld() {
						  var png64 = cy.png({
						  'bg': 'white'
						  });
							
							var imgBlob = b64toBlob(png64);
							 saveAs(imgBlob, "graph.png");
						}
							
							var allNodes = cy.elements('node');
							var allEdges = cy.elements('edge');
							
							for (var i = 0; i< cy.nodes().length; i++)
							{
								console.log(cy.nodes()[i].data());
							}
						function unFilter() {
  							 
  							 	allNodes.restore();
  							 	allEdges.restore();
  							 
 							}
 						
						
						function colaLayout(){
							var options = {
						      
						       name:'cola', randomize: false,
						        ready: function(){},
								stop: function(){},
								refresh: 1, // number of ticks per frame; higher is faster but more jerky
						       	maxSimulationTime: 2000, // max length in ms to run the layout
						            ungrabifyWhileSimulating: false, // so you can't drag nodes during layout
						            fit: true, // on every layout reposition of nodes, fit the viewport
						            padding: 0,
						    	 handleDisconnected: false,
						    };
 							var layout = cy.layout( options );
							layout.run();
							console.log("node size: " + cy.nodes().length);
							console.log("edge size: " + cy.edges().length);
							
							
							
						}
						
					
						function filterNode(wthreshold){
						 	
						 	unFilter();
					       	console.log("Weight threshold " + wthreshold);
							var filteredEles = cy.elements('[weight<' + wthreshold + ']');
 							cy.remove(filteredEles);
  							colaLayout();
								        
						
						}
						$(function() {
							    var wthreshold = 0;
							    var remove = 0;
							   // Edge weight slider 
							    $( "#w-slider" ).slider({
							    //  range: true,
							      min: 0,
							      max: 100,
							      values: 10,
							      slide: function( event, ui ) {
							      $("#weightRange2" ).val(" > " + ui.value/100 );
							         wthreshold = ui.value/100;
							         console.log("Weight threshold of slider " + wthreshold);
									filterNode(wthreshold);
							         if (remove === 1) {
							         	removeOrphan();
							         }
							        $( "#nodeNum" ).val(cy.nodes().length  );
							        $( "#edgeNum" ).val(cy.edges().length );
									}
								  });
								 $("#weightRange2").val( " > " + $("#w-slider").slider("value")/100);
								 $( "#nodeNum" ).val(cy.nodes().length  );
							     $( "#edgeNum" ).val(cy.edges().length );  
								 
								 $( "#checkbox-1" ).click(function(){

            						if($(this).prop("checked") == true){
										remove = 1;
										removeOrphan();
										 $( "#nodeNum" ).val(cy.nodes().length);
							       		 $( "#edgeNum" ).val(cy.edges().length);
           							 }

            						else if($(this).prop("checked") == false){
										remove = 0;
               						}

        							});
  								
  						});	
															
								
 						function removeOrphan(){
							cy.nodes(function(element){
					        	if( element.isNode() && element.degree()<1){
					            	cy.remove(element)
					        	}
					    	});
	  						colaLayout();
						}
							        
							        						
						$(function() {
						    $( document ).tooltip();
						    $("#nodeNum").width(30);
    						$("#edgeNum").width(30);
						  });
						</script>
</div>
</div>
<div class="columnEnd" style="width:15%; margin-top:10%">

<p></p>
<legend>Filter:</legend>
<fieldset>
<div>
  <label for="weightRange2">Edge Weight (Strength):</label>
  <input type="text" id="weightRange2" readonly style="border:0; color:#f6931f; font-weight:bold;" >

 
<div id="w-slider"></div>
</div>
 <p></p>
 <div>
   <input type="checkbox" name="checkbox-1" id="checkbox-1"> <label for="checkbox-1">Remove Orphan Node</label>
    
 </div>
				</fieldset>
<p></p>
<legend>Displaying:</legend>
<fieldset>
<p>
  <h3>Nodes </h3><input type="text" id="nodeNum" readonly style="border:0; color:#f6931f; font-weight:bold"><label for="amount"> out of ${totalNodeNum}</label>
</p>
<p>
  <h2>Edges </h2><input type="text" id="edgeNum" readonly style="border:0; color:#f6931f; font-weight:bold"><label for="amount"> out of ${totalEdgeNum}</label>
</p>
</fieldset>				
				</div>
				</div>
				</form:form>
			</div>
		</div>
	</div>
</div>





<script type="text/javascript">
function loadHFResultsToolbar(aFormClass, extraFunc) {
  var hfToolUrl = "/brc/hf_dataModel.spg?method=getToolBar&decorator=${decorator}";
  new Ajax.Request(hfToolUrl, {
    asynchronous: true,
    parameters: {
      showDisplaySetting: 'false',
      toolbar: 'DM_Network_Visualization',
      formName: '${dynamicJSPBeanName}',
      selectItemBar: 'true',
      uniqueIDC: '${uniqueIDC}',
      selectionContext: '${selectionContext}',
      customRecordCount: '${customRecordCount}',
      formClass: aFormClass,
      expSeqId: '${expSeqId}',
      resultMatrixUserDefId: '${resultMatrixUserDefId}'
    },
    onSuccess: function(transport) {
      var result = transport.responseText;
      jQuery('.hfReagentDetailsToolBar').html('');
      jQuery('.hfReagentDetailsToolBar').html(result);
      updateDisplayCount(ajaxRollupCount, customRecordCount, uniqueIDC);
      if (extraFunc) {
        extraFunc();
      }
    },
    onError: function(transport) {
      jQuery('.hfReagentDetailsToolBar').text("Error loading toolbar.");
    }
  });
}

var url = "/brc/hostFactorResults.spg?method=LoadMoreData&decorator=${decorator}";
var pageTo = '${pageTo}';
var customRecordCount = '${customRecordCount}';
var ajaxCurrentCount = '${ajaxCurrentCount}';
var pageSize = '${pageSize}';
var uniqueIDC = '${uniqueIDC}';
var ajaxRollupCount = '${ajaxRollupCount}';

function loadResultTable() {
  jQuery('#loadingImage').show();
  jQuery('#loadMoreDataButton').hide();
  pageTo++;
  ajaxCurrentCount += pageSize;
  updateDisplayCount(ajaxCurrentCount, customRecordCount, uniqueIDC);

  new Ajax.Request(url, {
    parameters: {
      pageTo: pageTo,
      selectionContext: '${selectionContext}',
      customRecordCount: customRecordCount,
      ajaxCurrentCount: ajaxCurrentCount
    },
    onSuccess: function(transport) {

      var result = transport.responseText.evalJSON();
      jQuery("." + uniqueIDC + "Scroll").append(result.ajaxBuiltTableData);
      showHideScrollBar();

      var ajaxRollUpCount = result.ajaxRollupCount;
      updateDisplayCount(ajaxRollUpCount, customRecordCount, uniqueIDC);

      var loadMoreIndicator = result.LoadMoreIndicator;
      handleLoadMoreImage(loadMoreIndicator);

    },
    onError: function(transport) {
      jQuery('#ajax-append').text("Error loading data.");
      jQuery('#loadingImage').hide();
      jQuery('#loadMoreDataButton').hide();
    }
  });
}

function load(){
$.ajax({
      
        success: function() {
            //location.href = "admin.php";
            load();
        }
    });
  
}

</script>
