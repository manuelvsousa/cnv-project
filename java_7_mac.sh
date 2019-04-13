# ensure java 7 on mac version 1.7.0_79
if [[ "$OSTYPE" == "darwin"* ]]; then
	export JAVA_HOME=`/usr/libexec/java_home -v 1.7.0_79`
	export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS
	#export JDK_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk
	#export JRE_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/jre
	#export JAVA_ROOT=/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk
	#export PATH=/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/bin:$PATH
	#export SDK_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk
fi