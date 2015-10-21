# digital-atg
ATG related configurations and source files

REST Support:

1) To output the topology information: 

curl -L -v -b customer_cooks.txt -H "Content-Type: application/json" "http://52.2.31.4:20180/rest/model/rax/deployment/TopologyActor/info"

2) To create a new agent: 

curl -L -v -b customer_cookies.txt -H "Content-Type: application/json" -d "{"targetName":\"Production\", "agentDisplayName":\"store-test01\", "description":\"store-test01\", "transportURL":\"rmi://test.com:20560/atg/epub/AgentTransport\" }" "http://52.2.31.4:20180/rest/model/rax/deployment/TopologyActor/update"

URL support:

1) To obtain topology as an XML: 
http://52.2.31.4:20180/raxcloud/info.jsp

2) To add a new agent: 
http://52.2.31.4:20180/raxcloud/updateTopology.jsp

