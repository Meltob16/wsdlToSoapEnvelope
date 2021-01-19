
send testdata to docker:
cd projectFolder
docker build -t containerName .
docker run -p 8080:8080  containerName
then in a new window, 
curl --verbose -o testData/responseGetOperations.txt --data-binary '@c:/pathtoProject/testData/testWsdl.txt' -X PUT http://localhost:8080/wsdlToTemplate/ -H "connection: keep-alive" -H "content-type: text/plain"
