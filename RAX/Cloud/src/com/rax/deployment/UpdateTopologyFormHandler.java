/**
 	#################### COPYRIGHT #####################################
    Copyright 2001, 2015, Rackspace Inc and/or its affiliates. All rights 
    reserved.
  	UNIX is a registered trademark of The Open Group.
  	This software and related documentation are provided under a license
  	agreement containing restrictions on use and disclosure and are
  	protected by intellectual property laws. Except as expressly permitted
  	in your license agreement or allowed by law, you may not use, copy,
  	reproduce, translate, broadcast, modify, license, transmit, distribute,
  	exhibit, perform, publish, or display any part, in any form, or by any
  	means. Reverse engineering, disassembly, or decompilation of this
  	software, unless required by law for interoperability, is prohibited.
  	The information contained herein is subject to change without notice
  	and is not warranted to be error-free. If you find any errors, please
  	report them to us in writing.

  	This software or hardware and documentation may provide access to or
  	information on content, products, and services from third parties.
  	Rackspace Inc, and its affiliates are not responsible for and
  	expressly disclaim all warranties of any kind with respect to
  	third-party content, products, and services. Rackspace Inc. and
  	its affiliates will not be responsible for any loss, costs, or damages
  	incurred due to your access to or use of third-party content, products,
  	or services.
 */

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
