all:
	javac src/auxillary/auxFuncs.java
	javac src/customer/chooseHotel.java 
	javac src/customer/knowYourCustomer.java
	javac src/customer/processCustomer.java
	javac src/customer/customerInterface.java
	javac src/projectInterface.java 
	jar cfmv projectInterface.jar Manifest.txt projectInterface.class
	mv src/*/*.class ./bin
	mv src/*/*.jar ./bin
	clear
	java -jar ./bin/projectInterface.jar