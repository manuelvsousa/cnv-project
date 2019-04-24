if [ $# -ne 1 ]; then
	echo "Invalid number of commandline arguments. Pass the aws classpath directory as the first argument without ending '/' character"
	exit 1
fi

export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS

CP=$(echo lib/*.jar | tr ' ' ':'):$(echo $1/lib/*.jar | tr ' ' ':'):$(echo $1/third-party/lib/*.jar | tr ' ' ':'):build

java -cp $CP pt.ulisboa.tecnico.cnv.webserver.WebServer

