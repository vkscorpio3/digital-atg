<%@ taglib prefix="dsp" uri="http://www.atg.com/taglibs/daf/dspjspTaglib1_0" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<dsp:page>

<dsp:importbean bean="/atg/epub/deployment/TopologyEditFormHandler"/>

<dsp:droplet name="/atg/dynamo/droplet/Switch">
  <dsp:param bean="TopologyEditFormHandler.formError" name="value"/>
  <dsp:oparam name="true">
   <h2>The following Errors were found while updating the topology</h2>
    <dsp:droplet name="/atg/dynamo/droplet/ErrorMessageForEach">
       <dsp:param name="exceptions" bean="TopologyEditFormHandler.formExceptions"/>
       <dsp:oparam name="output">
          <b>
          <dsp:valueof param="message"/>
          </b>
          <p>
      </dsp:oparam>
    </dsp:droplet>
     <P>
     <P>
  </dsp:oparam>
  <dsp:oparam name="false">
    <p> Update Topology:
    <dsp:form action="updateTopology.jsp" method="post">
 
    <p>Target Name: <dsp:input type="text" bean="TopologyEditFormHandler.targetName"/> </p>
    <dsp:input type="hidden" bean="TopologyEditFormHandler.transportType" value="RMI"/>
    <p>Agent Name:<dsp:input type="text" bean="TopologyEditFormHandler.agentDisplayName" /></p>
    <p>Description:<dsp:input type="text" bean="TopologyEditFormHandler.description" /></p>
    <p>Uri: (host:port) <dsp:input type="text" bean="TopologyEditFormHandler.transportURL" /></p>
    <p><dsp:input type="hidden" bean="TopologyEditFormHandler.successURL" value="updateTopology.jsp"/>
    <p><dsp:input type="hidden" bean="TopologyEditFormHandler.failureURL" value="updateTopology.jsp"/>
 	<p><p></p></p>
 	<dsp:input type="submit" value="Update Topology" bean="TopologyEditFormHandler.addAgent"/>
   </dsp:form>
  </dsp:oparam>
</dsp:droplet>


</dsp:page>