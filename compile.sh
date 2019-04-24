AWS_SDK_CLASSPATH="aws/lib/aws-java-sdk-1.11.536.jar"

rm -r build
mkdir build

export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS
javac -cp $AWS_SDK_CLASSPATH $(find . -name "*.java") -d build
java -cp build BitTool build/pt/ulisboa/tecnico/cnv/solver build/pt/ulisboa/tecnico/cnv/solver
java -cp build pt.ulisboa.tecnico.cnv.webserver.DynamicTool build/pt/ulisboa/tecnico/cnv/solver build/pt/ulisboa/tecnico/cnv/solver


