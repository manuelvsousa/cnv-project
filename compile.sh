if [ $# -ne 1 ]; then
	echo "Invalid number of commandline arguments. Pass the aws classpath directory as the first argument without ending '/' character"
	exit 1
fi

AWS_SDK_CLASSPATH=$1"/lib/aws-java-sdk"*[0-9].jar # only get the main jar, only one ending in the version
CP=$(echo lib/*.jar | tr ' ' ':'):$(echo $AWS_SDK_CLASSPATH)

rm -r build
mkdir build

export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS
javac -cp $CP $(find . -name "*.java") -d build
java -cp build BitTool build/pt/ulisboa/tecnico/cnv/solver build/pt/ulisboa/tecnico/cnv/solver
#java -cp build pt.ulisboa.tecnico.cnv.webserver.DynamicTool build/pt/ulisboa/tecnico/cnv/solver build/pt/ulisboa/tecnico/cnv/solver


