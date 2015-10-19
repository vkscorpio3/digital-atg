/**
 * 
 */
package com.rax.deployment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import atg.deployment.common.DeploymentException;
import atg.deployment.server.DeploymentServer;
import atg.nucleus.ServiceException;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.DynamoServlet;

/**
 * 
 * @author 
 *
 */
public class TopologyInfoDroplet extends DynamoServlet {
	
	private static final String ENCODING = "UTF-8";
	
	//output parameters
	private static final String PARAM_TOPOLOGY = "topology";
	private static final String PARAM_ERROR_MESSAGE = "errorMsg";
	
	//open parameters
	private static final String OPARAM_OUTPUT = "output";
	private static final String OPARAM_ERROR = "error";

	DeploymentServer mDeploymentServer;
	
	public DeploymentServer getDeploymentServer() {
		return mDeploymentServer;
	}
	public void setDeploymentServer(DeploymentServer pDeploymentServer) {
		this.mDeploymentServer = pDeploymentServer;
	}
	
	// -------------------------
	/**
	 * Checks that Nucleus components are  running, if its not, then throw
	 * a ServiceException.
	 * 
	 * @throws ServiceException
	*/
	public void doStartService() throws ServiceException {
		if (getDeploymentServer () == null) {
			throw new ServiceException("Deployment Server component is not running.");
		}
		
		super.doStartService();
	}

	@Override

	public void service(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse) throws ServletException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			getDeploymentServer().exportTopologyToXML(baos);
			String topologyConfig = new String(baos.toByteArray(),ENCODING);
			try {
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = domFactory.newDocumentBuilder();
				Document doc = builder.parse(topologyConfig);
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty (OutputKeys.INDENT, "yes");
				
				StreamResult result = new StreamResult (topologyConfig);
				DOMSource source = new DOMSource (doc);
				transformer.transform(source, result);
				String xmlOutput = result.getWriter().toString();
				pRequest.setParameter(PARAM_TOPOLOGY, xmlOutput);
				
				vlogDebug("The topology is:" + xmlOutput);
				
				pRequest.serviceParameter(OPARAM_OUTPUT, pRequest, pResponse);
				
				
			} catch (Exception e) {
				vlogError (e.getMessage(),e);
				
				pRequest.setParameter(PARAM_ERROR_MESSAGE, e.getMessage());
				pRequest.serviceParameter(OPARAM_ERROR, pRequest, pResponse);
			}
			
			
		} catch (DeploymentException e) {
			vlogError (e.getMessage(),e);
			
			pRequest.setParameter(PARAM_ERROR_MESSAGE, e.getMessage());
			pRequest.serviceParameter(OPARAM_ERROR, pRequest, pResponse);
		}
	}
	
}
