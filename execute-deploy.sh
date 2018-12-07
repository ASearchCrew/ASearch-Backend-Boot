#!/bin/bash
touch /home/ec2-user/springboot/asearch-deploy/test.txt
/home/ec2-user/springboot/asearch-deploy/deploy.sh > /dev/null 2> /dev/null < /dev/null &