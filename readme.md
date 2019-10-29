# Requirement

```bash
sudo apt-get install oracle-java13-installer
sudo apt-get install oracle-java13-set-default
```

# Build

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=9090"
```

or

```bash
mvnw package
java -Dserver.port=8081 -jar target/faucet-0.0.1-SNAPSHOT.jar org.powfaucet.faucet.FaucetApplication 
```

# Deploy

```bash
wget https://github.com/AdoptOpenJDK/openjdk12-binaries/releases/download/jdk-12.0.2%2B10/OpenJDK12U-jdk_x64_linux_hotspot_12.0.2_10.tar.gz
sudo update-alternatives --install /usr/bin/java java /home/ubuntu/programs/jdk-12.0.2+10/bin/java 2
sudo update-alternatives --config java

sudo update-alternatives --install /usr/bin/javac javac /home/ubuntu/programs/jdk-12.0.2+10/bin/javac 2
sudo update-alternatives --config javac
```
