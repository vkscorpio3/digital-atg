/**
 * 
 */
package com.rax.deployment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import atg.apache.xml.serialize.OutputFormat;
import atg.apache.xml.serialize.XMLSerializer;
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
			vlogDebug("TopologyInfoDroplet: service processing droplet " );
			getDeploymentServer().exportTopologyToXML(baos);
			String topologyConfig = new String(baos.toByteArray(),ENCODING);
			
			vlogDebug("TopologyInfoDroplet: topology: "  + topologyConfig );
			try {
				//String xmlOutput = format (topologyConfig);
				pRequest.setParameter(PARAM_TOPOLOGY, topologyConfig);
				
				vlogDebug("Setting output parameter");
				
				pRequest.serviceParameter(OPARAM_OUTPUT, pRequest, pResponse);
				
				
			} catch (Exception e) {
				vlogError (e,e.getMessage());
				
				pRequest.setParameter(PARAM_ERROR_MESSAGE, e.getMessage());
				pRequest.serviceParameter(OPARAM_ERROR, pRequest, pResponse);
			}
			
			
		} catch (DeploymentException e) {
			vlogError (e.getMessage(),e);
			
			pRequest.setParameter(PARAM_ERROR_MESSAGE, e.getMessage());
			pRequest.serviceParameter(OPARAM_ERROR, pRequest, pResponse);
		}
	}
	
	
	 public String format(String unformattedXml) {
        try {
            final Document document = parseXmlFile(unformattedXml);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(80);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	 }
	        
    private Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
	
}
