#!/bin/sh
git clone git@github.com:manuelsousa7/cnv-project.git /home/ec2-user/cnv-project
cd /home/ec2-user/cnv-project && find . -type f -name "*.class" -delete
cd /home/ec2-user/cnv-project && /bin/bash compile.sh /home/ec2-user/aws-java-sdk-1.11.534
/bin/bash /home/ec2-user/cnv-project/run_server.sh /home/ec2-user/aws-java-sdk-1.11.534
