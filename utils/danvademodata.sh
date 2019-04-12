#!/bin/bash

echo
echo "Create demo data..."
curl -s -o /dev/null -w "%{http_code}" -u admin:$1 -X POST "http://localhost:8080/alfresco/s/foundation/demodata/danva"

echo
echo "Create groups..."
curl -s -o /dev/null -w "%{http_code}" -u admin:$1 -X POST -H "Content-Type:application/json" -d '{"groups":{"secretary":{"Branch":"write","NewApplication":"write"},"boardmember":{"Workflow":"write"}},"users":[]}' "http://localhost:8080/alfresco/s/foundation/groupsandusers"

echo
echo "Create users... (Bruce and Britney)"
curl -s -o /dev/null -w "%{http_code}" -u admin:$1 -X POST -H "Content-Type: application/json" -d '{"userName":"bruce", "firstName":"Bruce", "lastName":"Lee", "email":"bruce@kung.fu", "password":"bruce", "groups":["GROUP_secretary"]}' "http://localhost:8080/alfresco/s/api/people"
curl -s -o /dev/null -w "%{http_code}" -u admin:$1 -X POST -H "Content-Type: application/json" -d '{"userName":"britney", "firstName":"Britney", "lastName":"Spears", "email":"britney@hollywood.com", "password":"britney", "groups":["GROUP_boardmember"]}' "http://localhost:8080/alfresco/s/api/people"

