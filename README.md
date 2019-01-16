The data model network visualization repository contains the code enable the  visualization of networks generated from the Host Factor Experiment Data Models recently implemented for systems biology experiments.  We use the commonly employed framework of Cytoscape.js to do that.  


- Data 
For the network to display in cytoscape, we need two data type:  Node data and Edge data.  Here are the example node and edge data provided by JCVI.  These data are module specific and are store in oracle database

A node contains the following data:
Module
Probe ID
Gene Symbol
Gene Name 
Entrez ID
Genbank accession
Quantitative measure
Method/Measurement type
Statistical measure

Sample node data ("Blue", "A_23_P379293", "ear6", "Eosionophil-associated, ribonuclease a family member 6", "93719", "NM_053111","0.86", "Pearson's correlation", "0.0347")

Edge data contain Module, Source Symbol, Target Symbol, and Weight.

- Retrieve data from database
- Transform data for cytoscape user-defined
- Display in cytoscape
DataModelNetworkVisualization.jsp utilizes cytoscape.js to present the node and edge in the viewer.  Major feature implmented:

Displays detailed information of node/edge upon tapping. 
Downloads displayed image in .PNG format.
Filters edge by edge weight with “Remove Orphaned Node” option
Dynamically displays number node and edge along with percentile after filtering

