export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS

java -cp build:$AWS_SDK_CLASSPATH pt.ulisboa.tecnico.cnv.webserver.WebServer

