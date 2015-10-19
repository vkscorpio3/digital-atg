package com.rax.deployment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import atg.core.util.StringUtils;
import atg.deployment.DeploymentManager;
import atg.deployment.common.DeploymentException;
import atg.deployment.server.DeploymentServer;
import atg.droplet.DropletException;
import atg.droplet.GenericFormHandler;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

public class UpdateTopologyFormHandler extends GenericFormHandler {
	
	private static final String ENCODING = "UTF-8";
	private static final String URI_PATTERN = "||RMI_URI_PORT||" ;
	private static final String AGENT_NAME_PATTERN = "||AGENT_NAME||" ;
	
	private static final String AGENT_NODE = "<agent><agent-name>||AGENT_NAME||</agent-name><principal-asset>NONE</principal-asset><include-asset-destination>/atg/epub/file/WWWFileSystem</include-asset-destination><include-asset-destination>/atg/epub/file/ConfigFileSystem</include-asset-destination><transport><transport-type> RMI</transport-type><rmi-uri>rmi://||RMI_URI_PORT||/atg/epub/AgentTransport</rmi-uri> </transport> </agent>";
	
	DeploymentServer mDeploymentServer;
	DeploymentManager mDeploymentManager;
	
	public DeploymentServer getDeploymentServer() {
		return mDeploymentServer;
	}
	public void setDeploymentServer(DeploymentServer pDeploymentServer) {
		this.mDeploymentServer = pDeploymentServer;
	}

	public DeploymentManager getDeploymentManager() {
		return mDeploymentManager;
	}
	
	public void setDeploymentManager(DeploymentManager pDeploymentManager) {
		this.mDeploymentManager = pDeploymentManager;
	}	
	
	// Inputs for agent name and uri
	String  mAgentName;
	String  mUri;
	String 	mTargetName;
	
	public String getAgentName() {
		return mAgentName;
	}
	public void setAgentName(String pAgentName) {
		this.mAgentName = pAgentName;
	}
	public String getUri() {
		return mUri;
	}
	public void setUri(String pUri) {
		this.mUri = pUri;
	}
	public String getTargetName() {
		return mTargetName;
	}
	public void setTargetName(String pTargetName) {
		this.mTargetName = pTargetName;
	}

	
	// Typically new agents added should not be essential agents
	boolean mEssentialAgent = false;
	
	public boolean isEssentialAgent() {
		return mEssentialAgent;
	}
	public void setEssentialAgent(boolean pEssentialAgent) {
		mEssentialAgent = pEssentialAgent;
	}
	
	
	String mSuccessUrl;
	String mErrorUrl;
	
	
	public String getSuccessUrl() {
		return mSuccessUrl;
	}
	public void setSuccessUrl(String pSuccessUrl) {
		this.mSuccessUrl = pSuccessUrl;
	}
	public String getErrorUrl() {
		return mErrorUrl;
	}
	public void setErrorUrl(String pErrorUrl) {
		this.mErrorUrl = pErrorUrl;
	}
	
	public boolean handleUpdateTopology (DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse)
		throws ServletException, IOException {
		
		if  (getUri() == null) {
			addFormException(new DropletException(""));
		}
		if  (getTargetName() == null) {
			addFormException(new DropletException(""));
		}
		if  (getAgentName() == null) {
			addFormException(new DropletException(""));
		}
		
		if (!checkFormRedirect(null, getErrorUrl(), pRequest, pResponse)) {
			return true;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			getDeploymentServer().exportTopologyToXML(baos);
		} catch (DeploymentException e) {
			vlogError (e.getMessage(),e);
			
			addFormException (new DropletException(""));
		}
		

		if (!checkFormRedirect(null, getErrorUrl(), pRequest, pResponse)) {
			return true;
		}
		String topologyConfig = new String(baos.toByteArray(),ENCODING);
		// Check if the given agent name and uri exists, if so,
		// no update is necessary
		try {
			if (!isDuplicateConfigs (topologyConfig)) {
				update (topologyConfig);
			} else {
				vlogDebug("*****Agent is already listed .. no updates necessary************");
				vlogDebug("TOPOLOGY DETAILS: ***"  + prettyPrint(topologyConfig));
				
			}
		} catch (Exception e) {
			vlogError (e.getMessage(),e);
			
			addFormException (new DropletException(""));
		}
			
		return checkFormRedirect(getSuccessUrl(), getErrorUrl(), pRequest, pResponse);
		
	}
	
	private void update(String topologyConfig) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
		if (topologyConfig != null) {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder db  = domFactory.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(topologyConfig)));
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xp = xpf.newXPath();
			XPathExpression targetExpr = xp.compile("/publishing-deployment-topology/target");
			XPathExpression targetNameExpr = xp.compile("target-name");
			
			NodeList targetNodes = (NodeList) targetExpr.evaluate(doc, XPathConstants.NODESET);
			// targetNodes -- iterate over these to find if the target-name is the one that we need
			boolean isFragmentAdded = false;
			for (int i = 0; i < targetNodes.getLength() ; i++) {
				NodeList tarNameList = (NodeList) targetNameExpr.evaluate(targetNodes.item(i), XPathConstants.NODESET);
				String targetName = tarNameList.item(0).getTextContent();
				// Add it to correct target
				if (getTargetName().equalsIgnoreCase(targetName)) {
					vlogDebug("Now adding xml fragment to the document");
					
					appendXmlFragment (db,targetNodes.item(i),modifiedAgent());
					isFragmentAdded = true;
					break;
				}
			}
			if (isFragmentAdded) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				Source xmlSource = new DOMSource(doc);
				Result outputTarget = new StreamResult(outputStream);
				TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
				InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
				try {
					getDeploymentServer().importTopologyFromXML(is);
				} catch (DeploymentException e) {
					vlogError (e.getMessage(),e);
				}
			}
		}
		
	}
	
	/**
	 * Check existence of existing agent/uri combination
	 * 
	 * @param topologyConfig: The current topology definition
	 * 
	 * @return true in case the agent already exists, false otherwise
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 */
	protected boolean isDuplicateConfigs(String topologyConfig) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		if (topologyConfig != null) {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder db  = domFactory.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(topologyConfig)));
			
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xp = xpf.newXPath();
			XPathExpression targetExpr = xp.compile("/publishing-deployment-topology/target/target-name");
			// ensure we have the appropriate target, 
			NodeList targetNameNodes = (NodeList) targetExpr.evaluate(doc, XPathConstants.NODESET);
			String targetName = targetNameNodes.item(0).getTextContent();
			if (!getTargetName().equalsIgnoreCase(targetName)) {
				// TODO throw custom exception
			}
			XPathExpression agentExpr = xp.compile("/publishing-deployment-topology/target/agent/");
			
			NodeList agents = (NodeList) agentExpr.evaluate(doc, XPathConstants.NODESET);
			XPathExpression nameExpr = xp.compile("agent-name");
			XPathExpression rmiExpr = xp.compile("transport/rmi-uri");
			
			for (int i = 0; i < agents.getLength() ; i++) {
				NodeList agName = (NodeList) nameExpr.evaluate(agents.item(i), XPathConstants.NODESET);
				// Compare agent name
				if (agName != null && agName.getLength() > 0) {
					String agentName = agName.item(0).getTextContent();
					if (StringUtils.isNotEmpty(agentName)) {
						if (agentName.trim().equalsIgnoreCase(getAgentName())) {
							vlogDebug ("Obtained match for agent name :" + agentName);
						
							return true;
						}
					}
				}
				
				// If no return until this point, check the rmi uri
				NodeList rmiName = (NodeList) rmiExpr.evaluate(agents.item(i), XPathConstants.NODESET);
				// Compare agent name
				if (rmiName != null && rmiName.getLength() > 0) {
					String rmiNm = rmiName.item(0).getTextContent();
					if (StringUtils.isNotEmpty(rmiNm)) {
						if (rmiNm.trim().contains(getUri())) {
							vlogDebug("Obtained match for uri  :" + rmiNm);
							return true;
						}
					}
				}
			}
			
		}
		return false;
	}
	
	private String prettyPrint (String xml) {
		
		if (xml != null) {
			vlogDebug("Input xml feed: " + xml);
			
			try {
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = domFactory.newDocumentBuilder();
				Document doc = builder.parse(xml);
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty (OutputKeys.INDENT, "yes");
				
				StreamResult result = new StreamResult (xml);
				DOMSource source = new DOMSource (doc);
				transformer.transform(source, result);
				return result.getWriter().toString();
				
			} catch (Exception e) {
				vlogError (e.getMessage(),e);
				
			}
		}
		vlogDebug("UpdateTopologyFormHandler->prettyPrint: XML feed returning null");
		
		return null;
		
	}
	
	// Modify agent string with the appropriate agent name and uri
	private String modifiedAgent() {
		String replacementString = AGENT_NODE;
		replacementString.replace(AGENT_NAME_PATTERN, getAgentName());
		replacementString.replace(URI_PATTERN, getUri());
		vlogDebug("Replacement string xml: " + replacementString);
		return replacementString;
	}
	
	 /**
	   * @param docBuilder the parser
	   * @param parent node to add fragment to
	   * @param fragment a well formed XML fragment
	   */
	  private  void appendXmlFragment(
	      DocumentBuilder docBuilder, Node parent,
	      String fragment) throws IOException, SAXException {
	    Document doc = parent.getOwnerDocument();
	    Node fragmentNode = docBuilder.parse(
	        new InputSource(new StringReader(fragment)))
	        .getDocumentElement();
	    fragmentNode = doc.importNode(fragmentNode, true);
	    parent.appendChild(fragmentNode);
	  }	
}
