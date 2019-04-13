find . -type f -name "*java" > sources.txt
rm -r build
mkdir build
javac @sources.txt -d build
rm sources.txt
echo "Compiling done."
echo "Running bit tool"
javac BitTool.java -d build 
java -cp build BitTool build/pt/ulisboa/tecnico/cnv/solver build/pt/ulisboa/tecnico/cnv/solver/
echo "Finished."


