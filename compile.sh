AWS_SDK_CLASSPATH="../aws/aws-java-sdk-1.11.534.jar"

rm -r build
mkdir build
javac -cp $AWS_SDK_CLASSPATH $(find src -name "*.java") -d build
java -cp build pt.ulisboa.tecnico.cnv.bittool.BitTool build/pt/ulisboa/tecnico/cnv/solver build/pt/ulisboa/tecnico/cnv/solver/


