package com.rax.deployment;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import atg.core.util.StringUtils;
import atg.deployment.common.DeploymentException;
import atg.deployment.server.topology.TargetDef;
import atg.deployment.server.topology.TopologyDef;

import atg.deployment.server.ui.TopologyEditFormHandler;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

public class UpdateTopologyFormHandler extends TopologyEditFormHandler {
	String mTargetName;
	
	public String getTargetName() {
		return mTargetName;
	}
	public void setTargetName(String pTargetName) {
		this.mTargetName = pTargetName;
	}


	public void preAddAgent (DynamoHttpServletRequest pRequest,DynamoHttpServletResponse pResponse)
	 throws DeploymentException, IOException, ServletException{
		vlogDebug("UpdateTopologyFormHandler: preAddAgent called");
		setTargetID (findTarget (getTargetName()));
		setAgentEssential (Boolean.FALSE);
		String [] assetDestinations = {"/atg/epub/file/WWWFileSystem" , "/atg/epub/file/ConfigFileSystem"};
		setIncludeAssetDestinations (assetDestinations);
		super.preAddAgent(pRequest, pResponse);
	}
	
	@SuppressWarnings("unchecked")
	private String findTarget(String targetName) throws DeploymentException {
		if (StringUtils.isNotEmpty(targetName)) {
			TopologyDef tDef = getDeploymentServer().getTopologyManager().getSurrogateTopology();
			if (tDef != null) {
				List <TargetDef> targets = tDef.getTargets();
				if (targets != null) {
					Iterator <TargetDef>it = targets.iterator();
					TargetDef target = it.next();
					String displayName = target.getDisplayName();
					if (StringUtils.isNotEmpty(displayName)) {
						if (displayName.trim().equalsIgnoreCase(getTargetName().trim())) {
							vlogDebug("Target to edit :" + target.getID());
							return target.getID();
						}
					}
				}
				
			}
		}
		return null;
	}
	
}
