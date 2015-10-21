<%@ taglib prefix="dsp" uri="http://www.atg.com/taglibs/daf/dspjspELTaglib1_0" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jstl/core" %>

<dsp:page>

<dsp:importbean bean="/rax/deployment/TopologyInfoDroplet"/>
<html>
  <head><title>RAX Cloud Topology</title></head>
  <body>
   
  <dsp:droplet name="TopologyInfoDroplet">
    <dsp:oparam name="output">
      <dsp:getvalueof var="tp" param="topology"/>
    </dsp:oparam>
  </dsp:droplet>
 <p>Topology Information: <c:out value="${tp}"/>
 
  </body>
</html>
</dsp:page>