# Description

This scripts are used as deplyment automation in ec2 instances. During the first boot the machine will automaticly clone the project repository, compile and execute it.
Every time the instance is restarted, will also pull new code from the repository, and compile it in the next boot.

# On reboot

`@reboot /home/ec2-user/gitpull.sh` in `crontab` (as ec2 user)

and add

`su ec2-user -c "/bin/bash /home/ec2-user/start.sh"` in `/etc/rc.d/rc.local`
